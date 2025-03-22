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
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

/**
 * An implementation of {@link KaitaiStream} backed by a {@link RandomAccessFile}.
 *
 * Allows reading from local files. Generally, one would want to use
 * {@link ByteBufferKaitaiStream} instead, as it most likely would be faster,
 * but there are two situations when one should consider this one instead:
 *
 * <ul>
 * <li>Processing many small files. Every ByteBuffer invocation requires a mmap
 * call, which can be relatively expensive (per file).</li>
 * <li>Accessing extra-long files (&gt;31 bits positioning). Unfortunately, Java's
 * implementation of mmap uses ByteBuffer, which is not addressable beyond 31 bit
 * offsets, even if you use a 64-bit platform.</li>
 * </ul>
 */
public class RandomAccessFileKaitaiStream extends KaitaiStream {
    protected RandomAccessFile raf;

    public RandomAccessFileKaitaiStream(String fileName) throws IOException {
        raf = new RandomAccessFile(fileName, "r");
    }

    public RandomAccessFileKaitaiStream(RandomAccessFile raf) {
        this.raf = raf;
    }

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
            try {
                raf.close();
            } catch (IOException e) {
                if (exc != null) {
                    // deliver RandomAccessFile.close() exception as primary, the one from
                    // writeAlignToByte() as suppressed
                    e.addSuppressed(exc);
                }
                throw e;
            }
        }
    }

    //region Stream positioning

    @Override
    public boolean isEof() {
        try {
            return !(raf.getFilePointer() < raf.length() || (!bitsWriteMode && bitsLeft > 0));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void seek(int newPos) {
        seek((long) newPos);
    }

    @Override
    public void seek(long newPos) {
        if (bitsWriteMode) {
            writeAlignToByte();
        } else {
            alignToByte();
        }
        try {
            raf.seek(newPos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int pos() {
        try {
            // FIXME cast
            return (int) raf.getFilePointer() + ((bitsWriteMode && bitsLeft > 0) ? 1 : 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long size() {
        try {
            return raf.length();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //endregion

    //region Reading

    //region Integer numbers

    //region Signed

    @Override
    public byte readS1() {
        alignToByte();
        try {
            int t = raf.read();
            if (t < 0) {
                throw new EOFException();
            } else {
                return (byte) t;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //region Big-endian

    @Override
    public short readS2be() {
        alignToByte();
        try {
            int b1 = raf.read();
            int b2 = raf.read();
            if ((b1 | b2) < 0) {
                throw new EOFException();
            } else {
                return (short) ((b1 << 8) + (b2 << 0));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int readS4be() {
        alignToByte();
        try {
            int b1 = raf.read();
            int b2 = raf.read();
            int b3 = raf.read();
            int b4 = raf.read();
            if ((b1 | b2 | b3 | b4) < 0) {
                throw new EOFException();
            } else {
                return (b1 << 24) + (b2 << 16) + (b3 << 8) + (b4 << 0);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long readS8be() {
        alignToByte();
        long b1 = readU4be();
        long b2 = readU4be();
        return (b1 << 32) + (b2 << 0);
    }

    //endregion

    //region Little-endian

    @Override
    public short readS2le() {
        alignToByte();
        try {
            int b1 = raf.read();
            int b2 = raf.read();
            if ((b1 | b2) < 0) {
                throw new EOFException();
            } else {
                return (short) ((b2 << 8) + (b1 << 0));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int readS4le() {
        alignToByte();
        try {
            int b1 = raf.read();
            int b2 = raf.read();
            int b3 = raf.read();
            int b4 = raf.read();
            if ((b1 | b2 | b3 | b4) < 0) {
                throw new EOFException();
            } else {
                return (b4 << 24) + (b3 << 16) + (b2 << 8) + (b1 << 0);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long readS8le() {
        alignToByte();
        long b1 = readU4le();
        long b2 = readU4le();
        return (b2 << 32) + (b1 << 0);
    }

    //endregion

    //endregion

    //region Unsigned

    @Override
    public int readU1() {
        alignToByte();
        try {
            int t = raf.read();
            if (t < 0) {
                throw new EOFException();
            } else {
                return t;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //region Big-endian

    @Override
    public int readU2be() {
        alignToByte();
        try {
            int b1 = raf.read();
            int b2 = raf.read();
            if ((b1 | b2) < 0) {
                throw new EOFException();
            } else {
                return (b1 << 8) + (b2 << 0);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long readU4be() {
        alignToByte();
        try {
            long b1 = raf.read();
            long b2 = raf.read();
            long b3 = raf.read();
            long b4 = raf.read();
            if ((b1 | b2 | b3 | b4) < 0) {
                throw new EOFException();
            } else {
                return (b1 << 24) + (b2 << 16) + (b3 << 8) + (b4 << 0);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //endregion

    //region Little-endian

    @Override
    public int readU2le() {
        alignToByte();
        try {
            int b1 = raf.read();
            int b2 = raf.read();
            if ((b1 | b2) < 0) {
                throw new EOFException();
            } else {
                return (b2 << 8) + (b1 << 0);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long readU4le() {
        alignToByte();
        try {
            long b1 = raf.read();
            long b2 = raf.read();
            long b3 = raf.read();
            long b4 = raf.read();
            if ((b1 | b2 | b3 | b4) < 0) {
                throw new EOFException();
            } else {
                return (b4 << 24) + (b3 << 16) + (b2 << 8) + (b1 << 0);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //endregion

    //endregion

    //endregion

    //region Floating point numbers

    //region Big-endian

    @Override
    public float readF4be() {
        return Float.intBitsToFloat(readS4be());
    }

    @Override
    public double readF8be() {
        return Double.longBitsToDouble(readS8be());
    }

    //endregion

    //region Little-endian

    @Override
    public float readF4le() {
        return Float.intBitsToFloat(readS4le());
    }

    @Override
    public double readF8le() {
        return Double.longBitsToDouble(readS8le());
    }

    //endregion

    //endregion

    //region Byte arrays

    @Override
    protected byte[] readBytesNotAligned(long n) {
        byte[] buf = new byte[toByteArrayLength(n)];
        try {
            raf.readFully(buf);
            return buf;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final int DEFAULT_BUFFER_SIZE = 4 * 1024;

    @Override
    public byte[] readBytesFull() {
        alignToByte();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int readCount;
        try {
            while (-1 != (readCount = raf.read(buffer)))
                baos.write(buffer, 0, readCount);

            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] readBytesTerm(byte term, boolean includeTerm, boolean consumeTerm, boolean eosError) {
        alignToByte();
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try {
            while (true) {
                int c = raf.read();
                if (c < 0) {
                    if (eosError) {
                        throw new RuntimeException("End of stream reached, but no terminator " + term + " found");
                    }
                    return buf.toByteArray();
                }
                if ((byte) c == term) {
                    if (includeTerm)
                        buf.write(c);
                    if (!consumeTerm)
                        raf.seek(raf.getFilePointer() - 1);
                    return buf.toByteArray();
                }
                buf.write(c);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] readBytesTermMulti(byte[] term, boolean includeTerm, boolean consumeTerm, boolean eosError) {
        alignToByte();
        int unitSize = term.length;
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        byte[] c = new byte[unitSize];
        try {
            int readCount = 0;
            while (true) {
                // Inspired by the [Java implementation of
                // `RandomAccessFile.readFully`](https://github.com/openjdk/jdk/blob/2f2223d7524c/src/java.base/share/classes/java/io/RandomAccessFile.java#L539-L547)
                // (we want something like readFully() here in the sense that we'd like to read all
                // `unitSize` bytes if available, but we don't want to throw an EOF exception)
                int count = raf.read(c, readCount, unitSize - readCount);
                if (count >= 0) {
                    readCount += count;
                    if (readCount < unitSize) {
                        // we have received some bytes, but we need `unitSize` bytes, so we'll
                        // continue reading
                        continue;
                    }
                } else {
                    if (eosError) {
                        throw new RuntimeException("End of stream reached, but no terminator " + byteArrayToHex(term) + " found");
                    }
                    buf.write(c, 0, readCount);
                    return buf.toByteArray();
                }
                if (Arrays.equals(c, term)) {
                    if (includeTerm)
                        buf.write(c, 0, c.length); // see the comment about `buf.write(c)` in `ByteBufferKaitaiStream.readBytesTermMulti`
                    if (!consumeTerm)
                        raf.seek(raf.getFilePointer() - unitSize);
                    return buf.toByteArray();
                }
                buf.write(c, 0, c.length); // see the comment about `buf.write(c)` in `ByteBufferKaitaiStream.readBytesTermMulti`
                readCount = 0;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public KaitaiStream substream(long n) {
        // This implementation mirrors what ksc was doing up to v0.10, and the fallback that
        // it is still doing in case something non-trivial has to happen with the byte contents.
        //
        // Given that RandomAccessFile-based stream is not really efficient anyway, this seems
        // to be a reasonable fallback without resorting to a special limiting implementation.
        //
        // If and when somebody will come up with a reason why substreams have to implemented
        // for RAF, feel free to contribute relevant implementation with some rationale (e.g. a
        // benchmark).

        return new ByteBufferKaitaiStream(readBytes(n));
    }

    //region Helper methods
    //endregion

    //endregion

    //region Writing

    protected void ensureBytesLeftToWrite(long n) throws IOException {
        ensureBytesLeftToWrite(n, raf.getFilePointer());
    }

    //region Integer numbers

    //region Signed

    /**
     * Writes one signed 1-byte integer.
     */
    @Override
    public void writeS1(byte v) {
        writeAlignToByte();
        try {
            ensureBytesLeftToWrite(1);
            raf.write(v);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //region Big-endian

    @Override
    public void writeS2be(short v) {
        writeAlignToByte();
        try {
            ensureBytesLeftToWrite(2);
            raf.write((v >>> 8) & 0xFF);
            raf.write((v >>> 0) & 0xFF);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeS4be(int v) {
        writeAlignToByte();
        try {
            ensureBytesLeftToWrite(4);
            raf.write((v >>> 24) & 0xFF);
            raf.write((v >>> 16) & 0xFF);
            raf.write((v >>>  8) & 0xFF);
            raf.write((v >>>  0) & 0xFF);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeS8be(long v) {
        writeAlignToByte();
        try {
            ensureBytesLeftToWrite(8);
            raf.write((int)(v >>> 56) & 0xFF);
            raf.write((int)(v >>> 48) & 0xFF);
            raf.write((int)(v >>> 40) & 0xFF);
            raf.write((int)(v >>> 32) & 0xFF);
            raf.write((int)(v >>> 24) & 0xFF);
            raf.write((int)(v >>> 16) & 0xFF);
            raf.write((int)(v >>>  8) & 0xFF);
            raf.write((int)(v >>>  0) & 0xFF);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //endregion

    //region Little-endian

    @Override
    public void writeS2le(short v) {
        writeAlignToByte();
        try {
            ensureBytesLeftToWrite(2);
            raf.write((v >>> 0) & 0xFF);
            raf.write((v >>> 8) & 0xFF);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeS4le(int v) {
        writeAlignToByte();
        try {
            ensureBytesLeftToWrite(4);
            raf.write((v >>>  0) & 0xFF);
            raf.write((v >>>  8) & 0xFF);
            raf.write((v >>> 16) & 0xFF);
            raf.write((v >>> 24) & 0xFF);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeS8le(long v) {
        writeAlignToByte();
        try {
            ensureBytesLeftToWrite(8);
            raf.write((int)(v >>>  0) & 0xFF);
            raf.write((int)(v >>>  8) & 0xFF);
            raf.write((int)(v >>> 16) & 0xFF);
            raf.write((int)(v >>> 24) & 0xFF);
            raf.write((int)(v >>> 32) & 0xFF);
            raf.write((int)(v >>> 40) & 0xFF);
            raf.write((int)(v >>> 48) & 0xFF);
            raf.write((int)(v >>> 56) & 0xFF);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //endregion

    //endregion

    //endregion

    //region Floating point numbers

    //region Big-endian

    @Override
    public void writeF4be(float v) {
        writeS4be(Float.floatToIntBits(v));
    }

    @Override
    public void writeF8be(double v) {
        writeS8be(Double.doubleToLongBits(v));
    }

    //endregion

    //region Little-endian

    @Override
    public void writeF4le(float v) {
        writeS4le(Float.floatToIntBits(v));
    }

    @Override
    public void writeF8le(double v) {
        writeS8le(Double.doubleToLongBits(v));
    }

    //endregion

    //endregion

    //region Byte arrays

    @Override
    protected void writeBytesNotAligned(byte[] buf) {
        try {
            ensureBytesLeftToWrite(buf.length);
            raf.write(buf);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //endregion

    //endregion
}
