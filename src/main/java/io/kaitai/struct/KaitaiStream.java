/**
 * Copyright 2015-2016 Kaitai Project: MIT license
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
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * KaitaiStream is an implementation of
 * <a href="https://github.com/kaitai-io/kaitai_struct/wiki/Kaitai-Struct-stream-API">Kaitai Struct stream API</a>
 * for Java. Internally, it uses a ByteBuffer (either a MappedByteBuffer
 * backed by FileChannel, or a regular wrapper over a given byte array).
 *
 * It provides a wide variety of simple methods to read (parse) binary
 * representations of primitive types, such as integer and floating
 * point numbers, byte arrays and strings, and also provides stream
 * positioning / navigation methods with unified cross-language and
 * cross-toolkit semantics.
 *
 * Typically, end users won't access Kaitai Stream class manually, but
 * would describe a binary structure format using .ksy language and
 * then would use Kaitai Struct compiler to generate source code in
 * desired target language.  That code, in turn, would use this class
 * and API to do the actual parsing job.
 */
public class KaitaiStream {
    private final FileChannel fc;
    private final ByteBuffer bb;
    private int bitsLeft = 0;
    private long bits = 0;

    /**
     * Initializes a stream, reading from a local file with specified fileName.
     * Internally, FileChannel + MappedByteBuffer will be used.
     * @param fileName file to read
     * @throws IOException if file can't be read
     */
    public KaitaiStream(String fileName) throws IOException {
        fc = FileChannel.open(Paths.get(fileName), StandardOpenOption.READ);
        bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
    }

    /**
     * Initializes a stream that will get data from given byte array when read.
     * Internally, ByteBuffer wrapping given array will be used.
     * @param arr byte array to read
     */
    public KaitaiStream(byte[] arr) {
        fc = null;
        bb = ByteBuffer.wrap(arr);
    }

    /**
     * Provide a read-only version of the {@link ByteBuffer} backing the data of this instance.
     * This way one can access the underluying raw bytes associated with this structure, but it is
     * important to note that the caller needs to know what this raw data is: Depending on the
     * hierarchy of user types, how the format has been described and how a user type is actually
     * used, it might be that one accesses all data of some format or only a special substream
     * view of it. We can't know currently, so one needs to keep that in mind when authoring a KSY
     * and e.g. use substreams with user types whenever such a type most likely needs to access its
     * underluying raw data. Using a substream in KSY and directly passing some raw data to a user
     * type outside of normal KS parse order is equivalent and will provide the same results. If no
     * substream is used instead, the here provided data might differ depending on the context in
     * which the associated type was parsed, because the underluying {@link ByteBuffer} might
     * contain the data of all parent types and such as well and not only the one the caller is
     * actually interested in.
     * @return read-only {@link ByteBuffer} to access raw data for the associated type.
     */
    public ByteBuffer asRoBuffer() {
      return this.bb.asReadOnlyBuffer();
    }

    /**
     * Closes the stream safely. If there was an open file associated with it, closes that file.
     * For streams that were reading from in-memory array, does nothing.
     * @throws IOException if FileChannel can't be closed
     */
    public void close() throws IOException {
        if (fc != null)
            fc.close();
    }

    //region Stream positioning

    /**
     * Check if stream pointer is at the end of stream.
     * @return true if we are located at the end of the stream
     */
    public boolean isEof() {
        return !bb.hasRemaining();
    }

    /**
     * Set stream pointer to designated position.
     * @param newPos new position (offset in bytes from the beginning of the stream)
     */
    public void seek(int newPos) {
        bb.position(newPos);
    }

    public void seek(long newPos) {
        if (newPos > Integer.MAX_VALUE) {
            throw new RuntimeException("Java ByteBuffer can't be seeked past Integer.MAX_VALUE");
        }
        bb.position((int) newPos);
    }

    /**
     * Get current position of a stream pointer.
     * @return pointer position, number of bytes from the beginning of the stream
     */
    public int pos() {
        return bb.position();
    }

    /**
     * Get total size of the stream in bytes.
     * @return size of the stream in bytes
     */
    public long size() {
        return bb.limit();
    }

    //endregion

    //region Integer numbers

    //region Signed

    /**
     * Reads one signed 1-byte integer, returning it properly as Java's "byte" type.
     * @return 1-byte integer read from a stream
     */
    public byte readS1() {
        return bb.get();
    }

    //region Big-endian

    public short readS2be() {
        bb.order(ByteOrder.BIG_ENDIAN);
        return bb.getShort();
    }

    public int readS4be() {
        bb.order(ByteOrder.BIG_ENDIAN);
        return bb.getInt();
    }

    public long readS8be() {
        bb.order(ByteOrder.BIG_ENDIAN);
        return bb.getLong();
    }

    //endregion

    //region Little-endian

    public short readS2le() {
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getShort();
    }

    public int readS4le() {
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt();
    }

    public long readS8le() {
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getLong();
    }

    //endregion

    //endregion

    //region Unsigned

    public int readU1() {
        return bb.get() & 0xff;
    }

    //region Big-endian

    public int readU2be() {
        bb.order(ByteOrder.BIG_ENDIAN);
        return bb.getShort() & 0xffff;
    }

    public long readU4be() {
        bb.order(ByteOrder.BIG_ENDIAN);
        return bb.getInt() & 0xffffffffL;
    }

    public long readU8be() {
        return readS8be();
    }

    //endregion

    //region Little-endian

    public int readU2le() {
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getShort() & 0xffff;
    }

    public long readU4le() {
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt() & 0xffffffffL;
    }

    public long readU8le() {
        return readS8le();
    }

    //endregion

    //endregion

    //endregion

    //region Floating point numbers

    //region Big-endian

    public float readF4be() {
        bb.order(ByteOrder.BIG_ENDIAN);
        return bb.getFloat();
    }

    public double readF8be() {
        bb.order(ByteOrder.BIG_ENDIAN);
        return bb.getDouble();
    }

    //endregion

    //region Little-endian

    public float readF4le() {
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getFloat();
    }

    public double readF8le() {
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getDouble();
    }

    //endregion

    //endregion

    //region Unaligned bit values

    public long readBitsInt(int n) {
        int bitsNeeded = n - bitsLeft;
        if (bitsNeeded > 0) {
            // 1 bit  => 1 byte
            // 8 bits => 1 byte
            // 9 bits => 2 bytes
            int bytesNeeded = ((bitsNeeded - 1) / 8) + 1;
            byte[] buf = readBytes(bytesNeeded);
            for (byte b : buf) {
                bits <<= 8;
                bits |= b;
                bitsLeft += 8;
            }
        }

        // raw mask with required number of 1s, starting from lowest bit
        long mask = (1 << n) - 1;
        // shift mask to align with highest bits available in "bits"
        int shiftBits = bitsLeft - n;
        mask <<= shiftBits;
        // derive reading result
        long res = (bits & mask) >> shiftBits;
        // clear top bits that we've just read => AND with 1s
        bitsLeft -= n;
        mask = (1 << bitsLeft) - 1;
        bits &= mask;

        return res;
    }

    //endregion

    //region Byte arrays

    /**
     * Reads designated number of bytes from the stream.
     * @param n number of bytes to read
     * @return read bytes as byte array
     */
    public byte[] readBytes(long n) {
        if (n > Integer.MAX_VALUE) {
            throw new RuntimeException(
                    "Java byte arrays can be indexed only up to 31 bits, but " + n + " size was requested"
            );
        }
        byte[] buf = new byte[(int) n];
        bb.get(buf);
        return buf;
    }

    /**
     * Reads all the remaining bytes in a stream as byte array.
     * @return all remaining bytes in a stream as byte array
     */
    public byte[] readBytesFull() {
        byte[] buf = new byte[bb.remaining()];
        bb.get(buf);
        return buf;
    }

    /**
     * Checks that next bytes in the stream match match expected fixed byte array.
     * It does so by determining number of bytes to compare, reading them, and doing
     * the actual comparison. If they differ, throws a {@link UnexpectedDataError}
     * runtime exception.
     * @param expected contents to be expected
     * @return read bytes as byte array, which are guaranteed to equal to expected
     * @throws UnexpectedDataError if read data from stream isn't equal to given data
     */
    public byte[] ensureFixedContents(byte[] expected) {
        byte[] actual = readBytes(expected.length);
        if (!Arrays.equals(actual, expected))
            throw new UnexpectedDataError(actual, expected);
        return actual;
    }

    //endregion

    //region Strings

    public String readStrEos(String encoding) {
        return new String(readBytesFull(), Charset.forName(encoding));
    }

    public String readStrByteLimit(long len, String encoding) {
        return new String(readBytes(len), Charset.forName(encoding));
    }

    public String readStrz(String encoding, int term, boolean includeTerm, boolean consumeTerm, boolean eosError) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        Charset cs = Charset.forName(encoding);
        while (true) {
            if (!bb.hasRemaining()) {
                if (eosError) {
                    throw new RuntimeException("End of stream reached, but no terminator " + term + " found");
                } else {
                    return new String(buf.toByteArray(), cs);
                }
            }
            int c = bb.get();
            if (c == term) {
                if (includeTerm)
                    buf.write(c);
                if (!consumeTerm)
                    bb.position(bb.position() - 1);
                return new String(buf.toByteArray(), cs);
            }
            buf.write(c);
        }
    }

    //endregion

    //region Byte array processing

    /**
     * Performs a XOR processing with given data, XORing every byte of input with a single
     * given value.
     * @param data data to process
     * @param key value to XOR with
     * @return processed data
     */
    public static byte[] processXor(byte[] data, int key) {
        int dataLen = data.length;
        byte[] r = new byte[dataLen];
        for (int i = 0; i < dataLen; i++)
            r[i] = (byte) (data[i] ^ key);
        return r;
    }

    /**
     * Performs a XOR processing with given data, XORing every byte of input with a key
     * array, repeating key array many times, if necessary (i.e. if data array is longer
     * than key array).
     * @param data data to process
     * @param key array of bytes to XOR with
     * @return processed data
     */
    public static byte[] processXor(byte[] data, byte[] key) {
        int dataLen = data.length;
        int valueLen = key.length;

        byte[] r = new byte[dataLen];
        int j = 0;
        for (int i = 0; i < dataLen; i++) {
            r[i] = (byte) (data[i] ^ key[j]);
            j = (j + 1) % valueLen;
        }
        return r;
    }

    /**
     * Performs a circular left rotation shift for a given buffer by a given amount of bits,
     * using groups of groupSize bytes each time. Right circular rotation should be performed
     * using this procedure with corrected amount.
     * @param data source data to process
     * @param amount number of bits to shift by
     * @param groupSize number of bytes per group to shift
     * @return copy of source array with requested shift applied
     */
    public static byte[] processRotateLeft(byte[] data, int amount, int groupSize) {
        byte[] r = new byte[data.length];
        switch (groupSize) {
            case 1:
                for (int i = 0; i < data.length; i++) {
                    byte bits = data[i];
                    // http://stackoverflow.com/a/19181827/487064
                    r[i] = (byte) (((bits & 0xff) << amount) | ((bits & 0xff) >>> (8 - amount)));
                }
                break;
            default:
                throw new UnsupportedOperationException("unable to rotate group of " + groupSize + " bytes yet");
        }
        return r;
    }

    private final static int ZLIB_BUF_SIZE = 4096;

    /**
     * Performs an unpacking ("inflation") of zlib-compressed data with usual zlib headers.
     * @param data data to unpack
     * @return unpacked data
     * @throws RuntimeException if data can't be decoded
     */
    public static byte[] processZlib(byte[] data) {
        Inflater ifl = new Inflater();
        ifl.setInput(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte buf[] = new byte[ZLIB_BUF_SIZE];
        while (!ifl.finished()) {
            try {
                int decBytes = ifl.inflate(buf);
                baos.write(buf, 0, decBytes);
            } catch (DataFormatException e) {
                throw new RuntimeException(e);
            }
        }
        ifl.end();
        return baos.toByteArray();
    }

    //endregion

    //region Misc runtime operations

    /**
     * Performs modulo operation between two integers: dividend `a`
     * and divisor `b`. Divisor `b` is expected to be positive. The
     * result is always 0 &lt;= x &lt;= b - 1.
     * @param a dividend
     * @param b divisor
     * @return result
     */
    public static int mod(int a, int b) {
        if (b <= 0)
            throw new ArithmeticException("mod divisor <= 0");
        int r = a % b;
        if (r < 0)
            r += b;
        return r;
    }

    /**
     * Performs modulo operation between two integers: dividend `a`
     * and divisor `b`. Divisor `b` is expected to be positive. The
     * result is always 0 &lt;= x &lt;= b - 1.
     * @param a dividend
     * @param b divisor
     * @return result
     */
    public static long mod(long a, long b) {
        if (b <= 0)
            throw new ArithmeticException("mod divisor <= 0");
        long r = a % b;
        if (r < 0)
            r += b;
        return r;
    }

    //endregion

    /**
     * Exception class for an error that occurs when some fixed content
     * was expected to appear, but actual data read was different.
     */
    public static class UnexpectedDataError extends RuntimeException {
        public UnexpectedDataError(byte[] actual, byte[] expected) {
            super(
                    "Unexpected fixed contents: got " + byteArrayToHex(actual) +
                    " , was waiting for " + byteArrayToHex(expected)
            );
        }

        private static String byteArrayToHex(byte[] arr) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < arr.length; i++) {
                if (i > 0)
                    sb.append(' ');
                sb.append(String.format("%02x", arr[i]));
            }
            return sb.toString();
        }
    }
}
