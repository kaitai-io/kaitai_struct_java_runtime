package io.kaitai.structures;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;

public class KaitaiStream {
    private KaitaiSeekableStream st;

    public KaitaiStream(String fileName) throws FileNotFoundException {
        st = new RAFWrapper(fileName, "r");
    }

    public KaitaiStream(byte[] arr) {
        st = new BAISWrapper(arr);
    }

    public long pos() throws IOException {
        return st.pos();
    }

    public void seek(long newPos) throws IOException {
        st.seek(newPos);
    }

    public boolean isEof() throws IOException {
        return st.isEof();
    }

    public byte readS1() throws IOException {
        int t = st.read();
        if (t < 0) {
            throw new EOFException();
        } else {
            return (byte) t;
        }
    }

    public int readU1() throws IOException {
        int t = st.read();
        if (t < 0) {
            throw new EOFException();
        } else {
            return t;
        }
    }

    public int readU2le() throws IOException {
        int b1 = st.read();
        int b2 = st.read();
        if ((b1 | b2) < 0) {
            throw new EOFException();
        } else {
            return (b2 << 8) + (b1 << 0);
        }
    }

    public int readU2be() throws IOException {
        int b1 = st.read();
        int b2 = st.read();
        if ((b1 | b2) < 0) {
            throw new EOFException();
        } else {
            return (b1 << 8) + (b2 << 0);
        }
    }

    public short readS2le() throws IOException {
        int b1 = st.read();
        int b2 = st.read();
        if ((b1 | b2) < 0) {
            throw new EOFException();
        } else {
            return (short) ((b2 << 8) + (b1 << 0));
        }
    }

    public short readS2be() throws IOException {
        int b1 = st.read();
        int b2 = st.read();
        if ((b1 | b2) < 0) {
            throw new EOFException();
        } else {
            return (short) ((b1 << 8) + (b2 << 0));
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

    public long readU8le() throws IOException {
        return readS8le();
    }

    public long readU8be() throws IOException {
        return readS8be();
    }

    public long readS8le() throws IOException {
        long b1 = readU4le();
        long b2 = readU4le();
        return (b2 << 32) + (b1 << 0);
    }

    public long readS8be() throws IOException {
        long b1 = readU4be();
        long b2 = readU4be();
        return (b1 << 32) + (b2 << 0);
    }

    public byte[] readBytes(long n) throws IOException {
        if (n > Integer.MAX_VALUE) {
            throw new RuntimeException(
                    "Java byte arrays can be indexed only up to 31 bits, but " + n + " size was requested"
            );
        }
        byte[] buf = new byte[(int) n];
        st.read(buf);
        return buf;
    }

    public byte[] ensureFixedContents(int len, byte[] expected) throws IOException {
        byte[] actual = readBytes(len);
        if (!Arrays.equals(actual, expected)) {
            throw new RuntimeException(
                    "Unexpected fixed contents: got " + byteArrayToHex(actual) +
                    " , was waiting for " + byteArrayToHex(expected)
            );
        }
        return actual;
    }

    public String readStrByteLimit(int len, String encoding) throws IOException {
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

    private static String byteArrayToHex(byte[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (i > 0)
                sb.append(' ');
            sb.append(String.format("%02x", arr[i]));
        }
        return sb.toString();
    }

    interface KaitaiSeekableStream {
        public long pos() throws IOException;;
        public void seek(long l) throws IOException;
        public int read() throws IOException;
        public int read(byte[] buf) throws IOException;
        public boolean isEof() throws IOException;
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
        public boolean isEof() throws IOException {
            return !(getFilePointer() < length());
        }
    }
}
