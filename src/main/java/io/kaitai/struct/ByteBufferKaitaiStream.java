/**
 * Copyright 2015-2025 Kaitai Project: MIT license
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.kaitai.struct;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

/**
 * An implementation of {@link KaitaiStream} backed by a {@link ByteBuffer}.
 * Any underlying implementation of {@link ByteBuffer} can be used, for example:
 * <ul>
 *     <li>{@link ByteBuffer} returned as result of {@link ByteBuffer#wrap}, wrapping
 *         a byte array into a buffer.</li>
 *     <li>{@link MappedByteBuffer} backed by {@link FileChannel}</li>
 * </ul>
 */
public class ByteBufferKaitaiStream extends KaitaiStream {
    private FileChannel fc;
    private ByteBuffer bb;

    /**
     * Initializes a stream, reading from a local file with specified {@code fileName}.
     * Internally, {@link FileChannel} + {@link MappedByteBuffer} will be used.
     * @param fileName file to read
     * @throws IOException if file can't be read
     */
    public ByteBufferKaitaiStream(String fileName) throws IOException {
        fc = FileChannel.open(Paths.get(fileName), StandardOpenOption.READ);
        bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
    }

    /**
     * Initializes a stream that will get data from the given array on read and put data
     * into the array on write. Internally, a {@link ByteBuffer} is used to wrap the given
     * array.
     * @param arr byte array to read from or write to
     */
    public ByteBufferKaitaiStream(byte[] arr) {
        fc = null;
        bb = ByteBuffer.wrap(arr);
    }

    /**
     * Initializes a stream that will get data from given {@link ByteBuffer} on read and
     * put data into it on write.
     * @param buffer {@link ByteBuffer} to read from or write to
     */
    public ByteBufferKaitaiStream(ByteBuffer buffer) {
        fc = null;
        bb = buffer;
    }

    /**
     * Initializes a stream that will write data into a fixed byte buffer in memory.
     * @param size size of buffer in bytes
     */
    public ByteBufferKaitaiStream(long size) {
        if (size > Integer.MAX_VALUE) {
            throw new RuntimeException("Java ByteBuffer can't be longer than Integer.MAX_VALUE");
        }
        fc = null;
        bb = ByteBuffer.allocate((int) size);
    }

    /**
     * Provide a read-only version of the {@link ByteBuffer} backing the data of this instance.
     * <p>
     * This way one can access the underlying raw bytes associated with this structure, but it is
     * important to note that the caller needs to know what this raw data is: depending on the
     * hierarchy of user types, how the format has been described and how a user type is actually
     * used, it might be that one accesses all data of some format or only a special substream
     * view of it. We can't know currently, so one needs to keep that in mind when authoring a KSY
     * and e.g. use substreams with user types whenever such a type most likely needs to access its
     * underlying raw data. Using a substream in KSY and directly passing some raw data to a user
     * type outside of normal KS parse order is equivalent and will provide the same results. If no
     * substream is used instead, the here provided data might differ depending on the context in
     * which the associated type was parsed, because the underlying {@link ByteBuffer} might
     * contain the data of all parent types and such as well and not only the one the caller is
     * actually interested in.
     * </p>
     * <p>
     * The returned {@link ByteBuffer} is always rewound to position 0, because this stream was
     * most likely used to parse a type already, in which case the former position would have been
     * at the end of the buffer. Such a position doesn't help a common reading user much and that
     * fact can easily be forgotten, repositioning to another index than the start is pretty easy
     * as well. Rewinding/repositioning doesn't even harm performance in any way.
     * </p>
     * @return read-only {@link ByteBuffer} to access raw data for the associated type.
     */
    public ByteBuffer asRoBuffer() {
        ByteBuffer retVal = bb.asReadOnlyBuffer();
        retVal.rewind();

        return retVal;
    }

    /**
     * Closes the stream safely, writing the buffered bits to the underlying byte stream
     * first (if applicable). If there was an open file associated with the stream, closes
     * that file.
     * <p>
     * If the last read/write/seek operation in the stream was {@link #writeBitsIntBe(int, long)} or
     * {@link #writeBitsIntLe(int, long)} and the stream ended at an unaligned bit
     * position (i.e. not at a byte boundary), writes a final byte with buffered bits to
     * the underlying stream before closing the stream.
     * </p>
     * <p>
     * Regardless of whether the closure is successful or not, always relinquishes the
     * underlying resources. In accordance with {@link java.io.Closeable#close()},
     * subsequent calls have no effect. Once this method has been called, read and write
     * operations, seeking or accessing the state using {@link #pos()}, {@link #size()} or
     * {@link #isEof()} on this stream will typically throw a {@link NullPointerException}.
     * </p>
     * @implNote
     * <p>
     * Unfortunately, there is no simple way to close memory-mapped {@link ByteBuffer} in
     * Java and unmap underlying file. As {@link MappedByteBuffer} documentation suggests,
     * "mapped byte buffer and the file mapping that it represents remain valid until the
     * buffer itself is garbage-collected". Thus, the best we can do is to delete all
     * references to it, which breaks all subsequent <code>read..</code> methods with
     * {@link NullPointerException}. Afterwards, a call to {@link System#gc()} will
     * typically release the mmap, if garbage collection will be triggered.
     * </p>
     * <p>
     * There is a <a href="https://bugs.openjdk.org/browse/JDK-4724038">JDK-4724038 request
     * for adding unmap method</a> filed at Java bugtracker since 2002, but as of 2018, it
     * is still unresolved.
     * </p>
     * <p>
     * A couple of unsafe approaches (such as using JNI, or using reflection to invoke JVM
     * internal APIs) have been suggested and used with some success, but these are either
     * unportable or dangerous (may crash JVM), so we're not using them in this general
     * purpose code.
     * </p>
     * <p>
     * For more examples and suggestions, see:
     * <a href="https://stackoverflow.com/q/2972986">How to unmap a file from memory
     * mapped using FileChannel in java?</a>
     * </p>
     * @throws IOException if {@link FileChannel} can't be closed
     */
    @Override
    public void close() throws IOException {
        Exception exc = null;
        try {
            if (bitsWriteMode) {
                writeAlignToByte();
            } else {
                alignToByte();
            }
        } catch (Exception e) {
            exc = e;
            throw e;
        } finally {
            bb = null;
            if (fc != null) try {
                fc.close();
            } catch (IOException e) {
                if (exc != null) {
                    // deliver FileChannel.close() exception as primary, the one from
                    // writeAlignToByte() as suppressed
                    e.addSuppressed(exc);
                }
                throw e;
            } finally {
                fc = null;
            }
        }
    }

    //region Stream positioning

    @Override
    public boolean isEof() {
        return !(bb.hasRemaining() || (!bitsWriteMode && bitsLeft > 0));
    }

    @Override
    public void seek(int newPos) {
        if (bitsWriteMode) {
            writeAlignToByte();
        } else {
            alignToByte();
        }
        bb.position(newPos);
    }

    @Override
    public void seek(long newPos) {
        if (newPos > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Java ByteBuffer can't be seeked past Integer.MAX_VALUE");
        }
        seek((int) newPos);
    }

    @Override
    public int pos() {
        return bb.position() + ((bitsWriteMode && bitsLeft > 0) ? 1 : 0);
    }

    @Override
    public long size() {
        return bb.limit();
    }

    //endregion

    //region Reading

    //region Integer numbers

    //region Signed

    /**
     * Reads one signed 1-byte integer, returning it properly as Java's "byte" type.
     * @return 1-byte integer read from a stream
     */
    @Override
    public byte readS1() {
        alignToByte();
        return bb.get();
    }

    //region Big-endian

    @Override
    public short readS2be() {
        alignToByte();
        bb.order(ByteOrder.BIG_ENDIAN);
        return bb.getShort();
    }

    @Override
    public int readS4be() {
        alignToByte();
        bb.order(ByteOrder.BIG_ENDIAN);
        return bb.getInt();
    }

    @Override
    public long readS8be() {
        alignToByte();
        bb.order(ByteOrder.BIG_ENDIAN);
        return bb.getLong();
    }

    //endregion

    //region Little-endian

    @Override
    public short readS2le() {
        alignToByte();
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getShort();
    }

    @Override
    public int readS4le() {
        alignToByte();
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt();
    }

    @Override
    public long readS8le() {
        alignToByte();
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getLong();
    }

    //endregion

    //endregion

    //region Unsigned

    @Override
    public int readU1() {
        alignToByte();
        return bb.get() & 0xff;
    }

    //region Big-endian

    @Override
    public int readU2be() {
        alignToByte();
        bb.order(ByteOrder.BIG_ENDIAN);
        return bb.getShort() & 0xffff;
    }

    @Override
    public long readU4be() {
        alignToByte();
        bb.order(ByteOrder.BIG_ENDIAN);
        return bb.getInt() & 0xffffffffL;
    }

    //endregion

    //region Little-endian

    @Override
    public int readU2le() {
        alignToByte();
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getShort() & 0xffff;
    }

    @Override
    public long readU4le() {
        alignToByte();
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt() & 0xffffffffL;
    }

    //endregion

    //endregion

    //endregion

    //region Floating point numbers

    //region Big-endian

    @Override
    public float readF4be() {
        alignToByte();
        bb.order(ByteOrder.BIG_ENDIAN);
        return bb.getFloat();
    }

    @Override
    public double readF8be() {
        alignToByte();
        bb.order(ByteOrder.BIG_ENDIAN);
        return bb.getDouble();
    }

    //endregion

    //region Little-endian

    @Override
    public float readF4le() {
        alignToByte();
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getFloat();
    }

    @Override
    public double readF8le() {
        alignToByte();
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getDouble();
    }

    //endregion

    //endregion

    //region Byte arrays

    @Override
    protected byte[] readBytesNotAligned(long n) {
        byte[] buf = new byte[toByteArrayLength(n)];
        bb.get(buf);
        return buf;
    }

    /**
     * Reads all the remaining bytes in a stream as byte array.
     * @return all remaining bytes in a stream as byte array
     */
    @Override
    public byte[] readBytesFull() {
        alignToByte();
        byte[] buf = new byte[bb.remaining()];
        bb.get(buf);
        return buf;
    }

    @Override
    public byte[] readBytesTerm(byte term, boolean includeTerm, boolean consumeTerm, boolean eosError) {
        alignToByte();
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        while (true) {
            if (!bb.hasRemaining()) {
                if (eosError) {
                    throw new RuntimeException("End of stream reached, but no terminator " + term + " found");
                }
                return buf.toByteArray();
            }
            byte c = bb.get();
            if (c == term) {
                if (includeTerm)
                    buf.write(c);
                if (!consumeTerm)
                    bb.position(bb.position() - 1);
                return buf.toByteArray();
            }
            buf.write(c);
        }
    }

    @Override
    public byte[] readBytesTermMulti(byte[] term, boolean includeTerm, boolean consumeTerm, boolean eosError) {
        alignToByte();
        int unitSize = term.length;
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        byte[] c = new byte[unitSize];
        while (true) {
            int restSize = bb.remaining();
            if (restSize < unitSize) {
                if (eosError) {
                    throw new RuntimeException("End of stream reached, but no terminator " + byteArrayToHex(term) + " found");
                }
                bb.get(c, 0, restSize);
                buf.write(c, 0, restSize);
                return buf.toByteArray();
            }
            bb.get(c);
            if (Arrays.equals(c, term)) {
                if (includeTerm)
                    buf.write(c, 0, c.length); // see the comment about `buf.write(c)` below
                if (!consumeTerm)
                    bb.position(bb.position() - unitSize);
                return buf.toByteArray();
            }
            // we could also just use `buf.write(c)`, but then Java thinks that it could throw
            // `IOException` when it really can't (Java 11 adds `ByteArrayOutputStream.writeBytes`
            // for this reason, but we still want to support Java 7+)
            buf.write(c, 0, c.length);
        }
    }

    //endregion

    @Override
    public KaitaiStream substream(long n) {
        if (n > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Java ByteBuffer can't be limited beyond Integer.MAX_VALUE");
        }

        ByteBuffer newBuffer = bb.slice();
        newBuffer.limit((int) n);

        bb.position(bb.position() + (int) n);

        return new ByteBufferKaitaiStream(newBuffer);
    }
    //endregion

    //region Writing

    //region Integer numbers

    //region Signed

    /**
     * Writes one signed 1-byte integer.
     */
    @Override
    public void writeS1(byte v) {
        writeAlignToByte();
        bb.put(v);
    }

    //region Big-endian

    @Override
    public void writeS2be(short v) {
        writeAlignToByte();
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putShort(v);
    }

    @Override
    public void writeS4be(int v) {
        writeAlignToByte();
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putInt(v);
    }

    @Override
    public void writeS8be(long v) {
        writeAlignToByte();
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putLong(v);
    }

    //endregion

    //region Little-endian

    @Override
    public void writeS2le(short v) {
        writeAlignToByte();
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putShort(v);
    }

    @Override
    public void writeS4le(int v) {
        writeAlignToByte();
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(v);
    }

    @Override
    public void writeS8le(long v) {
        writeAlignToByte();
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putLong(v);
    }

    //endregion

    //endregion

    //endregion

    //region Floating point numbers

    //region Big-endian

    @Override
    public void writeF4be(float v) {
        writeAlignToByte();
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putFloat(v);
    }

    @Override
    public void writeF8be(double v) {
        writeAlignToByte();
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putDouble(v);
    }

    //endregion

    //region Little-endian

    @Override
    public void writeF4le(float v) {
        writeAlignToByte();
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putFloat(v);
    }

    @Override
    public void writeF8le(double v) {
        writeAlignToByte();
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putDouble(v);
    }

    //endregion

    //endregion

    //region Byte arrays

    @Override
    protected void writeBytesNotAligned(byte[] buf) {
        bb.put(buf);
    }

    //endregion

    //endregion
}
