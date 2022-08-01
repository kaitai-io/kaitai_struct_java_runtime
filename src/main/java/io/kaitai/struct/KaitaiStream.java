/**
 * Copyright 2015-2022 Kaitai Project: MIT license
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
import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.Deflater;

/**
 * KaitaiStream provides implementation of
 * <a href="https://doc.kaitai.io/stream_api.html">Kaitai Stream API</a>
 * for Java.
 *
 * It provides a wide variety of simple methods to read (parse) binary
 * representations of primitive types, such as integer and floating
 * point numbers, byte arrays and strings, and also provides stream
 * positioning / navigation methods with unified cross-language and
 * cross-toolkit semantics.
 *
 * This is abstract class, which serves as an interface description and
 * a few default method implementations, which are believed to be common
 * for all (or at least most) implementations. Different implementations
 * of this interface may provide way to parse data from local files,
 * in-memory buffers or arrays, remote files, network streams, etc.
 *
 * Typically, end users won't access any of these Kaitai Stream classes
 * manually, but would describe a binary structure format using .ksy language
 * and then would use Kaitai Struct compiler to generate source code in
 * desired target language.  That code, in turn, would use this class
 * and API to do the actual parsing job.
 */
public abstract class KaitaiStream implements Closeable {
    protected int bitsLeft = 0;
    protected long bits = 0;

    @Override
    abstract public void close() throws IOException;

    //region Stream positioning

    /**
     * Check if stream pointer is at the end of stream.
     * @return true if we are located at the end of the stream
     */
    abstract public boolean isEof();

    /**
     * Set stream pointer to designated position (int).
     * @param newPos new position (offset in bytes from the beginning of the stream)
     */
    abstract public void seek(int newPos);

    /**
     * Set stream pointer to designated position (long).
     * @param newPos new position (offset in bytes from the beginning of the stream)
     */
    abstract public void seek(long newPos);

    /**
     * Get current position of a stream pointer.
     * @return pointer position, number of bytes from the beginning of the stream
     */
    abstract public int pos();

    /**
     * Get total size of the stream in bytes.
     * @return size of the stream in bytes
     */
    abstract public long size();

    //endregion

    //region Reading

    //region Integer numbers

    //region Signed

    /**
     * Reads one signed 1-byte integer, returning it properly as Java's "byte" type.
     * @return 1-byte integer read from a stream
     */
    abstract public byte readS1();

    //region Big-endian

    abstract public short readS2be();
    abstract public int readS4be();
    abstract public long readS8be();

    //endregion

    //region Little-endian

    abstract public short readS2le();
    abstract public int readS4le();
    abstract public long readS8le();

    //endregion

    //endregion

    //region Unsigned

    abstract public int readU1();

    //region Big-endian

    abstract public int readU2be();

    abstract public long readU4be();

    /**
     * Reads one unsigned 8-byte integer in big-endian encoding. As Java does not
     * have a primitive data type to accomodate it, we just reuse {@link #readS8be()}.
     * @return 8-byte signed integer (pretending to be unsigned) read from a stream
     */
    public long readU8be() {
        return readS8be();
    }

    //endregion

    //region Little-endian

    abstract public int readU2le();

    abstract public long readU4le();

    /**
     * Reads one unsigned 8-byte integer in little-endian encoding. As Java does not
     * have a primitive data type to accomodate it, we just reuse {@link #readS8le()}.
     * @return 8-byte signed integer (pretending to be unsigned) read from a stream
     */
    public long readU8le() {
        return readS8le();
    }

    //endregion

    //endregion

    //endregion

    //region Floating point numbers

    //region Big-endian

    abstract public float readF4be();
    abstract public double readF8be();

    //endregion

    //region Little-endian

    abstract public float readF4le();
    abstract public double readF8le();

    //endregion

    //endregion

    //region Unaligned bit values

    public void alignToByte() {
        bitsLeft = 0;
        bits = 0;
    }

    public long readBitsIntBe(int n) {
        long res = 0;

        int bitsNeeded = n - bitsLeft;
        bitsLeft = -bitsNeeded & 7; // `-bitsNeeded mod 8`

        if (bitsNeeded > 0) {
            // 1 bit  => 1 byte
            // 8 bits => 1 byte
            // 9 bits => 2 bytes
            int bytesNeeded = ((bitsNeeded - 1) / 8) + 1; // `ceil(bitsNeeded / 8)`
            byte[] buf = readBytes(bytesNeeded);
            for (byte b : buf) {
                // `b` is signed byte, convert to unsigned using the "& 0xff" trick
                res = res << 8 | (b & 0xff);
            }

            long newBits = res;
            res = res >>> bitsLeft | bits << bitsNeeded;
            bits = newBits; // will be masked at the end of the function
        } else {
            res = bits >>> -bitsNeeded; // shift unneeded bits out
        }

        long mask = (1L << bitsLeft) - 1; // `bitsLeft` is in range 0..7, so `(1L << 64)` does not have to be considered
        bits &= mask;

        return res;
    }

    /**
     * Unused since Kaitai Struct Compiler v0.9+ - compatibility with older versions
     *
     * @deprecated use {@link #readBitsIntBe(int)} instead
     */
    @Deprecated
    public long readBitsInt(int n) {
        return readBitsIntBe(n);
    }

    public long readBitsIntLe(int n) {
        long res = 0;
        int bitsNeeded = n - bitsLeft;

        if (bitsNeeded > 0) {
            // 1 bit  => 1 byte
            // 8 bits => 1 byte
            // 9 bits => 2 bytes
            int bytesNeeded = ((bitsNeeded - 1) / 8) + 1; // `ceil(bitsNeeded / 8)`
            byte[] buf = readBytes(bytesNeeded);
            for (int i = 0; i < bytesNeeded; i++) {
                // `buf[i]` is signed byte, convert to unsigned using the "& 0xff" trick
                res |= ((long) (buf[i] & 0xff)) << (i * 8);
            }

            // NB: in Java, bit shift operators on left-hand operand of type `long` work
            // as if the right-hand operand were subjected to `& 63` (`& 0b11_1111`) (see
            // https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.19),
            // so `res >>> 64` is equivalent to `res >>> 0` (but we don't want that)
            long newBits = bitsNeeded < 64 ? res >>> bitsNeeded : 0;
            res = res << bitsLeft | bits;
            bits = newBits;
        } else {
            res = bits;
            bits >>>= n;
        }

        bitsLeft = -bitsNeeded & 7; // `-bitsNeeded mod 8`

        if (n < 64) {
            long mask = (1L << n) - 1;
            res &= mask;
        }
        // if `n == 64`, do nothing
        return res;
    }

    //endregion

    //region Byte arrays

    /**
     * Reads designated number of bytes from the stream.
     * @param n number of bytes to read
     * @return read bytes as byte array
     */
    abstract public byte[] readBytes(long n);

    /**
     * Reads all the remaining bytes in a stream as byte array.
     * @return all remaining bytes in a stream as byte array
     */
    abstract public byte[] readBytesFull();

    abstract public byte[] readBytesTerm(byte term, boolean includeTerm, boolean consumeTerm, boolean eosError);

    /**
     * Checks that next bytes in the stream match match expected fixed byte array.
     * It does so by determining number of bytes to compare, reading them, and doing
     * the actual comparison. If they differ, throws a {@link UnexpectedDataError}
     * runtime exception.
     * @param expected contents to be expected
     * @return read bytes as byte array, which are guaranteed to equal to expected
     * @throws UnexpectedDataError if read data from stream isn't equal to given data
     * @deprecated Not used anymore in favour of validators.
     */
    @Deprecated
    public byte[] ensureFixedContents(byte[] expected) {
        byte[] actual = readBytes(expected.length);
        if (!Arrays.equals(actual, expected))
            throw new UnexpectedDataError(actual, expected);
        return actual;
    }

    public static byte[] bytesStripRight(byte[] bytes, byte padByte) {
        int newLen = bytes.length;
        while (newLen > 0 && bytes[newLen - 1] == padByte)
            newLen--;
        return Arrays.copyOf(bytes, newLen);
    }

    public static byte[] bytesTerminate(byte[] bytes, byte term, boolean includeTerm) {
        int newLen = 0;
        int maxLen = bytes.length;
        while (newLen < maxLen && bytes[newLen] != term)
            newLen++;
        if (includeTerm && newLen < maxLen)
            newLen++;
        return Arrays.copyOf(bytes, newLen);
    }

    /**
     * Checks if supplied number of bytes is a valid number of elements for Java
     * byte array: converts it to int, if it is, or throws an exception if it is not.
     * @param n number of bytes for byte array as long
     * @return number of bytes, converted to int
     */
    protected int toByteArrayLength(long n) {
        if (n > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "Java byte arrays can be indexed only up to 31 bits, but " + n + " size was requested"
            );
        }
        if (n < 0) {
            throw new IllegalArgumentException(
                    "Byte array size can't be negative, but " + n + " size was requested"
            );
        }
        return (int) n;
    }

    //endregion

    //endregion

    //region Writing

    //region Integer numbers

    //region Signed

    /**
     * Writes one signed 1-byte integer.
     */
    abstract public void writeS1(byte v);

    //region Big-endian

    abstract public void writeS2be(short v);

    abstract public void writeS4be(int v);

    abstract public void writeS8be(long v);

    //endregion

    //region Little-endian

    abstract public void writeS2le(short v);

    abstract public void writeS4le(int v);

    abstract public void writeS8le(long v);

    //endregion

    //endregion

    //region Unsigned

    abstract public void writeU1(int v);

    //region Big-endian

    abstract public void writeU2be(int v);

    abstract public void writeU4be(long v);

    abstract public void writeU8be(long v);

    //endregion

    //region Little-endian

    abstract public void writeU2le(int v);

    abstract public void writeU4le(long v);

    abstract public void writeU8le(long v);

    //endregion

    //endregion

    //endregion

    //region Floating point numbers

    //region Big-endian

    abstract public void writeF4be(float v);

    abstract public void writeF8be(double v);

    //endregion

    //region Little-endian

    abstract public void writeF4le(float v);

    abstract public void writeF8le(double v);

    //endregion

    //region Unaligned bit values

    public void writeBitsInt(int n, long v) {
        // Example 1 (n > bitsLeft):
        //            pos()
        // bitsLeft = 3 | bitShift = 10 -> numBytes = 2
        //          \  \v/         \
        //     |01101xxx|xxxxxxxx|xx......|  (x - the bits we want to write, . - no longer part of this field)
        //      \   /\             /\    /
        //       \ /  \__ n = 13 _/  \  /
        //        V                   \/
        //     already written bits | potentially written bits too - we should not assume that fields come always in sequence

        // Example 2 (n < bitsLeft):
        //                  pos()
        //      bitsLeft = 7 |
        //            /     \v
        // |01101100|1xxxxx..|........|
        //            \   /\/
        //            n = 5 \__ bitShift = -2 -> numBytes = 0

        int bitShift = n - bitsLeft;
        int numBytes = bitShift > 0 ? (bitShift / 8 + (bitShift % 8 != 0 ? 1 : 0)) : 0; // ceiled integer division
        int pos = pos();

        if (bitsLeft > 0) {// if the last written byte is filled with partial value, we have to change it too
            --pos;
            ++numBytes;
            bitShift += 8;
        } else {
            bitsLeft = 8;
        }

        byte[] buf = new byte[numBytes];
        if (numBytes > 0) {
            seek(pos);
            buf[0] = readS1();
        }
        if (numBytes > 1) {
            seek(pos + (numBytes - 1));
            buf[numBytes - 1] = readS1();
        }
        seek(pos);

        for (int i = 0; i < numBytes; i++) {
            bitShift -= 8;
            long mask = bitShift > 0 ? (1L << bitsLeft) - 1 : (1L << (bitsLeft + bitShift)) - 1 << (- bitShift);
            long shifted = bitShift > 0 ? v >>> bitShift : v << (- bitShift);
            buf[i] = (byte) ((buf[i] & (mask ^ 0xff)) | (shifted & mask));

            if (i == 0) {
                bitsLeft = 8;
            }
        }

        writeBytes(buf);

        bitsLeft = - bitShift;
    }

    //endregion

    //endregion

    //region Byte arrays

    /**
     * Writes given byte array to the stream.
     * @param buf byte array to write
     */
    abstract public void writeBytes(byte[] buf);

    abstract public void writeBytesLimit(byte[] buf, long size, byte term, byte padByte);

    abstract public void writeStream(KaitaiStream other);

    //endregion

    //endregion

    //region Byte array processing

    /**
     * Performs a XOR processing with given data, XORing every byte of input with a single
     * given value.
     * @param data data to process
     * @param key value to XOR with
     * @return processed data
     */
    public static byte[] processXor(byte[] data, byte key) {
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

    public static byte[] unprocessZlib(byte[] data) {
        Deflater dfl = new Deflater();
        dfl.setInput(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte buf[] = new byte[ZLIB_BUF_SIZE];
        while (!dfl.finished()) {
            int decBytes = dfl.deflate(buf);
            baos.write(buf, 0, decBytes);
        }
        dfl.end();
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

    /**
     * Compares two byte arrays in lexicographical order. Makes extra effort
     * to compare bytes properly, as *unsigned* bytes, i.e. [0x90] would be
     * greater than [0x10].
     * @param a first byte array to compare
     * @param b second byte array to compare
     * @return negative number if a &lt; b, 0 if a == b, positive number if a &gt; b
     * @see Comparable#compareTo(Object)
     */
    public static int byteArrayCompare(byte[] a, byte[] b) {
        if (a == b)
            return 0;
        int al = a.length;
        int bl = b.length;
        int minLen = Math.min(al, bl);
        for (int i = 0; i < minLen; i++) {
            int cmp = (a[i] & 0xff) - (b[i] & 0xff);
            if (cmp != 0)
                return cmp;
        }

        // Reached the end of at least one of the arrays
        if (al == bl) {
            return 0;
        } else {
            return al - bl;
        }
    }

    /**
     * Finds the minimal byte in a byte array, treating bytes as
     * unsigned values.
     * @param b byte array to scan
     * @return minimal byte in byte array as integer
     */
    public static int byteArrayMin(byte[] b) {
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < b.length; i++) {
            int value = b[i] & 0xff;
            if (value < min)
                min = value;
        }
        return min;
    }

    /**
     * Finds the maximal byte in a byte array, treating bytes as
     * unsigned values.
     * @param b byte array to scan
     * @return maximal byte in byte array as integer
     */
    public static int byteArrayMax(byte[] b) {
        int max = 0;
        for (int i = 0; i < b.length; i++) {
            int value = b[i] & 0xff;
            if (value > max)
                max = value;
        }
        return max;
    }

    //endregion

    public byte[] toByteArray() {
        long pos = pos();
        seek(0);
        byte[] r = readBytesFull();
        seek(pos);
        return r;
    }

    /**
     * Exception class for an error that occurs when some fixed content
     * was expected to appear, but actual data read was different.
     *
     * @deprecated Not used anymore in favour of {@code Validation*}-exceptions.
     */
    @Deprecated
    public static class UnexpectedDataError extends RuntimeException {
        public UnexpectedDataError(byte[] actual, byte[] expected) {
            super(
                    "Unexpected fixed contents: got " + byteArrayToHex(actual) +
                    ", was waiting for " + byteArrayToHex(expected)
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

    /**
     * Error that occurs when default endianness should be decided with a
     * switch, but nothing matches (although using endianness expression
     * implies that there should be some positive result).
     */
    public static class UndecidedEndiannessError extends RuntimeException {}

    /**
     * Common ancestor for all error originating from Kaitai Struct usage.
     * Stores KSY source path, pointing to an element supposedly guilty of
     * an error.
     */
    public static class KaitaiStructError extends RuntimeException {
        public KaitaiStructError(String msg, String srcPath) {
            super(srcPath + ": " + msg);
            this.srcPath = srcPath;
        }

        protected String srcPath;
    }

    /**
     * Common ancestor for all validation failures. Stores pointer to
     * KaitaiStream IO object which was involved in an error.
     */
    public static class ValidationFailedError extends KaitaiStructError {
        public ValidationFailedError(String msg, KaitaiStream io, String srcPath) {
            super("at pos " + io.pos() + ": validation failed: " + msg, srcPath);
            this.io = io;
        }

        protected KaitaiStream io;

        protected static String byteArrayToHex(byte[] arr) {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < arr.length; i++) {
                if (i > 0)
                    sb.append(' ');
                sb.append(String.format("%02x", arr[i]));
            }
            sb.append(']');
            return sb.toString();
        }
    }

    /**
     * Signals validation failure: we required "actual" value to be equal to
     * "expected", but it turned out that it's not.
     */
    public static class ValidationNotEqualError extends ValidationFailedError {
        public ValidationNotEqualError(byte[] expected, byte[] actual, KaitaiStream io, String srcPath) {
            super("not equal, expected " + byteArrayToHex(expected) + ", but got " + byteArrayToHex(actual), io, srcPath);
        }

        public ValidationNotEqualError(Object expected, Object actual, KaitaiStream io, String srcPath) {
            super("not equal, expected " + expected + ", but got " + actual, io, srcPath);
        }

        protected Object expected;
        protected Object actual;
    }

    public static class ValidationLessThanError extends ValidationFailedError {
        public ValidationLessThanError(byte[] expected, byte[] actual, KaitaiStream io, String srcPath) {
            super("not in range, min " + byteArrayToHex(expected) + ", but got " + byteArrayToHex(actual), io, srcPath);
        }

        public ValidationLessThanError(Object min, Object actual, KaitaiStream io, String srcPath) {
            super("not in range, min " + min + ", but got " + actual, io, srcPath);
        }

        protected Object min;
        protected Object actual;
    }

    public static class ValidationGreaterThanError extends ValidationFailedError {
        public ValidationGreaterThanError(byte[] expected, byte[] actual, KaitaiStream io, String srcPath) {
            super("not in range, max " + byteArrayToHex(expected) + ", but got " + byteArrayToHex(actual), io, srcPath);
        }

        public ValidationGreaterThanError(Object max, Object actual, KaitaiStream io, String srcPath) {
            super("not in range, max " + max + ", but got " + actual, io, srcPath);
        }

        protected Object max;
        protected Object actual;
    }

    public static class ValidationNotAnyOfError extends ValidationFailedError {
        public ValidationNotAnyOfError(Object actual, KaitaiStream io, String srcPath) {
            super("not any of the list, got " + actual, io, srcPath);
        }

        protected Object actual;
    }

    public static class ValidationExprError extends ValidationFailedError {
        public ValidationExprError(Object actual, KaitaiStream io, String srcPath) {
            super("not matching the expression, got " + actual, io, srcPath);
}

        protected Object actual;
    }
}
