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

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * KaitaiStream is an implementation of
 * <a href="https://github.com/kaitai-io/kaitai_struct/wiki/Kaitai-Struct-stream-API">Kaitai Struct stream API</a>
 * for Java. Internally, it uses either RandomAccessFile or
 * ByteArrayInputStream to read local files and in-memory buffers.
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
    private KaitaiSeekableStream st;

    /**
     * Initializes a stream, reading from a local file with specified fileName. Internally, RandomAccessFile would be
     * used to allow seeking within the stream and reading arbitrary bytes.
     * @param fileName file to read
     * @throws FileNotFoundException
     */
    public KaitaiStream(String fileName) throws FileNotFoundException {
        st = new RAFWrapper(fileName, "r");
    }

    /**
     * Initializes a stream that will get data from given byte array when read. Internally, ByteArrayInputStream will
     * be used.
     * @param arr byte array to read
     */
    public KaitaiStream(byte[] arr) {
        st = new BAISWrapper(arr);
    }

    /**
     * Closes the stream safely. If there was an open file associated with it, closes that file.
     * For streams that were reading from in-memory array, does nothing.
     */
    public void close() throws IOException {
        st.close();
    }

    //region Stream positioning

    /**
     * Check if stream pointer is at the end of stream.
     * @return true if we are located at the end of the stream
     * @throws IOException
     */
    public boolean isEof() throws IOException {
        return st.isEof();
    }

    /**
     * Set stream pointer to designated position.
     * @param newPos new position (offset in bytes from the beginning of the stream)
     * @throws IOException
     */
    public void seek(long newPos) throws IOException {
        st.seek(newPos);
    }

    /**
     * Get current position of a stream pointer.
     * @return pointer position, number of bytes from the beginning of the stream
     * @throws IOException
     */
    public long pos() throws IOException {
        return st.pos();
    }

    /**
     * Get total size of the stream in bytes.
     * @return size of the stream in bytes
     * @throws IOException
     */
    public long size() throws IOException {
        return st.size();
    }

    //endregion

    //region Integer numbers

    //region Signed

    /**
     * Reads one signed 1-byte integer, returning it properly as Java's "byte" type.
     * @return 1-byte integer read from a stream
     * @throws IOException
     */
    public byte readS1() throws IOException {
        int t = st.read();
        if (t < 0) {
            throw new EOFException();
        } else {
            return (byte) t;
        }
    }

    //region Big-endian

    public short readS2be() throws IOException {
        int b1 = st.read();
        int b2 = st.read();
        if ((b1 | b2) < 0) {
            throw new EOFException();
        } else {
            return (short) ((b1 << 8) + (b2 << 0));
        }
    }

    public int readS4be() throws IOException {
        int b1 = st.read();
        int b2 = st.read();
        int b3 = st.read();
        int b4 = st.read();
        if ((b1 | b2 | b3 | b4) < 0) {
            throw new EOFException();
        } else {
            return (b1 << 24) + (b2 << 16) + (b3 << 8) + (b4 << 0);
        }
    }

    public long readS8be() throws IOException {
        long b1 = readU4be();
        long b2 = readU4be();
        return (b1 << 32) + (b2 << 0);
    }

    //endregion

    //region Little-endian

    public short readS2le() throws IOException {
        int b1 = st.read();
        int b2 = st.read();
        if ((b1 | b2) < 0) {
            throw new EOFException();
        } else {
            return (short) ((b2 << 8) + (b1 << 0));
        }
    }

    public int readS4le() throws IOException {
        int b1 = st.read();
        int b2 = st.read();
        int b3 = st.read();
        int b4 = st.read();
        if ((b1 | b2 | b3 | b4) < 0) {
            throw new EOFException();
        } else {
            return (b4 << 24) + (b3 << 16) + (b2 << 8) + (b1 << 0);
        }
    }

    public long readS8le() throws IOException {
        long b1 = readU4le();
        long b2 = readU4le();
        return (b2 << 32) + (b1 << 0);
    }

    //endregion

    //endregion

    //region Unsigned

    public int readU1() throws IOException {
        int t = st.read();
        if (t < 0) {
            throw new EOFException();
        } else {
            return t;
        }
    }

    //region Big-endian

    public int readU2be() throws IOException {
        int b1 = st.read();
        int b2 = st.read();
        if ((b1 | b2) < 0) {
            throw new EOFException();
        } else {
            return (b1 << 8) + (b2 << 0);
        }
    }

    public long readU4be() throws IOException {
        long b1 = st.read();
        long b2 = st.read();
        long b3 = st.read();
        long b4 = st.read();
        if ((b1 | b2 | b3 | b4) < 0) {
            throw new EOFException();
        } else {
            return (b1 << 24) + (b2 << 16) + (b3 << 8) + (b4 << 0);
        }
    }

    public long readU8be() throws IOException {
        return readS8be();
    }

    //endregion

    //region Little-endian

    public int readU2le() throws IOException {
        int b1 = st.read();
        int b2 = st.read();
        if ((b1 | b2) < 0) {
            throw new EOFException();
        } else {
            return (b2 << 8) + (b1 << 0);
        }
    }

    public long readU4le() throws IOException {
        long b1 = st.read();
        long b2 = st.read();
        long b3 = st.read();
        long b4 = st.read();
        if ((b1 | b2 | b3 | b4) < 0) {
            throw new EOFException();
        } else {
            return (b4 << 24) + (b3 << 16) + (b2 << 8) + (b1 << 0);
        }
    }

    public long readU8le() throws IOException {
        return readS8le();
    }

    //endregion

    //endregion

    //endregion

    //region Floating point numbers

    //region Big-endian

    public float readF4be() throws IOException {
        return wrapBufferBe(4).getFloat();
    }

    public double readF8be() throws IOException {
        return wrapBufferBe(8).getDouble();
    }

    //endregion

    //region Little-endian

    public float readF4le() throws IOException {
        return wrapBufferLe(4).getFloat();
    }

    public double readF8le() throws IOException {
        return wrapBufferLe(8).getDouble();
    }

    //endregion

    //endregion

    //region Byte arrays

    /**
     * Reads designated number of bytes from the stream.
     * @param n number of bytes to read
     * @return read bytes as byte array
     * @throws IOException
     * @throws EOFException if there were less bytes than requested available in the stream
     */
    public byte[] readBytes(long n) throws IOException {
        if (n > Integer.MAX_VALUE) {
            throw new RuntimeException(
                    "Java byte arrays can be indexed only up to 31 bits, but " + n + " size was requested"
            );
        }
        byte[] buf = new byte[(int) n];

        // Reading 0 bytes is always ok and never raises an exception
        if (n == 0)
            return buf;

        int readCount = st.read(buf);
        if (readCount < n) {
            throw new EOFException();
        }
        return buf;
    }

    private static final int DEFAULT_BUFFER_SIZE = 4 * 1024;

    /**
     * Reads all the remaining bytes in a stream as byte array.
     * @return all remaining bytes in a stream as byte array
     * @throws IOException
     */
    public byte[] readBytesFull() throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int readCount;
        while (-1 != (readCount = st.read(buffer)))
            baos.write(buffer, 0, readCount);

        return baos.toByteArray();
    }

    /**
     * Reads next len bytes from the stream and ensures that they match expected
     * fixed byte array. If they differ, throws a {@link UnexpectedDataError}
     * runtime exception.
     * @param len number of bytes to read
     * @param expected contents to be expected
     * @return read bytes as byte array, which are guaranteed to equal to expected
     * @throws IOException
     * @throws UnexpectedDataError
     */
    public byte[] ensureFixedContents(int len, byte[] expected) throws IOException {
        byte[] actual = readBytes(len);
        if (!Arrays.equals(actual, expected))
            throw new UnexpectedDataError(actual, expected);
        return actual;
    }

    //endregion

    //region Strings

    public String readStrEos(String encoding) throws IOException {
        return new String(readBytesFull(), Charset.forName(encoding));
    }

    public String readStrByteLimit(long len, String encoding) throws IOException {
        return new String(readBytes(len), Charset.forName(encoding));
    }

    public String readStrz(String encoding, int term, boolean includeTerm, boolean consumeTerm, boolean eosError) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        Charset cs = Charset.forName(encoding);
        while (true) {
            int c = st.read();
            if (c < 0) {
                if (eosError) {
                    throw new RuntimeException("End of stream reached, but no terminator " + term + " found");
                } else {
                    return new String(buf.toByteArray(), cs);
                }
            } else if (c == term) {
                if (includeTerm)
                    buf.write(c);
                if (!consumeTerm)
                    st.seek(st.pos() - 1);
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
    public byte[] processXor(byte[] data, int key) {
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
    public byte[] processXor(byte[] data, byte[] key) {
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
    public byte[] processRotateLeft(byte[] data, int amount, int groupSize) {
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
     * @throws IOException
     */
    public byte[] processZlib(byte[] data) throws IOException {
        Inflater ifl = new Inflater();
        ifl.setInput(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte buf[] = new byte[ZLIB_BUF_SIZE];
        while (!ifl.finished()) {
            try {
                int decBytes = ifl.inflate(buf);
                baos.write(buf, 0, decBytes);
            } catch (DataFormatException e) {
                throw new IOException(e);
            }
        }
        ifl.end();
        return baos.toByteArray();
    }

    //endregion

    private ByteBuffer wrapBufferLe(int count) throws IOException {
        return ByteBuffer.wrap(readBytes(count)).order(ByteOrder.LITTLE_ENDIAN);
    }

    private ByteBuffer wrapBufferBe(int count) throws IOException {
        return ByteBuffer.wrap(readBytes(count)).order(ByteOrder.BIG_ENDIAN);
    }

    interface KaitaiSeekableStream {
        void close() throws IOException;
        long pos() throws IOException;
        long size() throws IOException;
        void seek(long l) throws IOException;
        int read() throws IOException;
        int read(byte[] buf) throws IOException;
        boolean isEof() throws IOException;
    }

    static class BAISWrapper extends ByteArrayInputStream implements KaitaiSeekableStream {
        public BAISWrapper(byte[] bytes) {
            super(bytes);
        }

        @Override
        public long pos() {
            return pos;
        }

        @Override
        public long size() {
            return count;
        }

        @Override
        public void seek(long newPos) {
            if (newPos > Integer.MAX_VALUE) {
                throw new RuntimeException(
                        "Java in-memory ByteArrays can be indexed only up to 31 bits, but " + newPos + " offset was requested"
                );
            } else {
                pos = (int) newPos;
            }
        }

        @Override
        public boolean isEof() {
            return !(this.pos < this.count);
        }
    }

    static class RAFWrapper extends RandomAccessFile implements KaitaiSeekableStream {
        public RAFWrapper(String fileName, String r) throws FileNotFoundException {
            super(fileName, r);
        }

        @Override
        public long pos() throws IOException {
            return getFilePointer();
        }

        @Override
        public long size() throws IOException {
            return length();
        }

        @Override
        public boolean isEof() throws IOException {
            return !(getFilePointer() < length());
        }
    }

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
