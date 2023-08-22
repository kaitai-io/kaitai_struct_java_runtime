/**
 * Copyright 2015-2023 Kaitai Project: MIT license
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.kaitai.struct;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link KaitaiStream} for serialization only.
 */
public class WriteOnlyByteListKaitaiStream extends KaitaiStream {

    private static final byte DEFAULT_BYTE_VALUE = (byte) 0;
    private final List<Byte> LIST;
    int position = 0;

    public WriteOnlyByteListKaitaiStream(List<Byte> list) {
        LIST = list;
    }

    public WriteOnlyByteListKaitaiStream() {
        LIST = new ArrayList<>();
    }

    public List<Byte> getList() {
        return LIST;
    }

    private void setOrAdd(byte value) {
        /*
        Example:
        Let LIST.size be 4, so the list indexes are 0, 1, 2, 3.
        - If position is 3, we want to set the value at index 3,
          so call LIST.set(position, value).
        - If position is 4, we need to add an item to the list.
          Call LIST .add(value) to add the item. The newly added
          item will have index 4.
        - If position is 5, we need to:
            - First call LIST.add(0) once to fill index 4 with the
              default byte value.
            - Then call LIST.add(value) to add the item. The newly
              added item will have index 5.
         */
        if (position < LIST.size()) {
            LIST.set(position, value);
        } else {
            while (position > LIST.size()) {
                LIST.add(DEFAULT_BYTE_VALUE);
            }
            LIST.add(value);
        }
        position++;
    }

    @Override
    public void close() throws IOException {
        try {
            if (bitsWriteMode) {
                writeAlignToByte();
            }
        } catch (Exception e) {
            throw e;
        } finally {
            alignToByte();
        }
    }

    //region Stream positioning
    @Override
    public boolean isEof() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void seek(int newPos) {
        if (bitsWriteMode) {
            writeAlignToByte();
        } else {
            alignToByte();
        }
        position = newPos;
    }

    @Override
    public void seek(long newPos) {
        if (newPos > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Java List can't be seeked past Integer.MAX_VALUE");
        }
        seek((int) newPos);
    }

    @Override
    public int pos() {
        return position + ((bitsWriteMode && bitsLeft > 0) ? 1 : 0);
    }

    @Override
    public long size() {
        throw new UnsupportedOperationException();
    }

    //endregion
    //region Reading
    //region Integer numbers
    //region Signed
    @Override
    public byte readS1() {
        throw new UnsupportedOperationException();
    }

    //region Big-endian
    @Override
    public short readS2be() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int readS4be() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long readS8be() {
        throw new UnsupportedOperationException();
    }

    //endregion
    //region Little-endian
    @Override
    public short readS2le() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int readS4le() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long readS8le() {
        throw new UnsupportedOperationException();
    }

    //endregion
    //endregion
    //region Unsigned
    @Override
    public int readU1() {
        throw new UnsupportedOperationException();
    }

    //region Big-endian
    @Override
    public int readU2be() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long readU4be() {
        throw new UnsupportedOperationException();
    }

    //endregion
    //region Little-endian
    @Override
    public int readU2le() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long readU4le() {
        throw new UnsupportedOperationException();
    }

    //endregion
    //endregion
    //endregion
    //region Floating point numbers
    //region Big-endian
    @Override
    public float readF4be() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double readF8be() {
        throw new UnsupportedOperationException();
    }

    //endregion
    //region Little-endian
    @Override
    public float readF4le() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double readF8le() {
        throw new UnsupportedOperationException();
    }

    //endregion
    //endregion
    //region Byte arrays
    @Override
    protected byte[] readBytesNotAligned(long n) {
        throw new UnsupportedOperationException();

    }

    @Override
    public byte[] readBytesFull() {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] readBytesTerm(byte term, boolean includeTerm, boolean consumeTerm, boolean eosError) {
        throw new UnsupportedOperationException();
    }

    //endregion
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
        setOrAdd(v);
    }

    /*
    Example from Wikipedia:
    The value to write is 0A0B0C0D.

    Big endian:
       index  0   1   2   3
    contents [0A, 0B, 0C, 0D]
     */
    //region Big-endian
    @Override
    public void writeS2be(short v) {
        writeAlignToByte();
        setOrAdd((byte) (v >> 8));
        setOrAdd((byte) v);
    }

    @Override
    public void writeS4be(int v) {
        writeAlignToByte();
        setOrAdd((byte) (v >> (8 * 3)));
        setOrAdd((byte) (v >> (8 * 2)));
        setOrAdd((byte) (v >> (8 * 1)));
        setOrAdd((byte) v);
    }

    @Override
    public void writeS8be(long v) {
        writeAlignToByte();
        setOrAdd((byte) (v >> (8 * 7)));
        setOrAdd((byte) (v >> (8 * 6)));
        setOrAdd((byte) (v >> (8 * 5)));
        setOrAdd((byte) (v >> (8 * 4)));
        setOrAdd((byte) (v >> (8 * 3)));
        setOrAdd((byte) (v >> (8 * 2)));
        setOrAdd((byte) (v >> (8 * 1)));
        setOrAdd((byte) v);
    }

    //endregion
    //region Little-endian

    /*
    Example from wikipedia:
    The value to write is 0A0B0C0D.

    Little endian:
       index  0   1   2   3
    contents [0D, 0C, 0B, 0A]
     */
    @Override
    public void writeS2le(short v) {
        writeAlignToByte();
        setOrAdd((byte) v);
        setOrAdd((byte) (v >> 8));
    }

    @Override
    public void writeS4le(int v) {
        writeAlignToByte();
        setOrAdd((byte) v);
        setOrAdd((byte) (v >> (8 * 1)));
        setOrAdd((byte) (v >> (8 * 2)));
        setOrAdd((byte) (v >> (8 * 3)));
    }

    @Override
    public void writeS8le(long v) {
        writeAlignToByte();
        setOrAdd((byte) v);
        setOrAdd((byte) (v >> (8 * 1)));
        setOrAdd((byte) (v >> (8 * 2)));
        setOrAdd((byte) (v >> (8 * 3)));
        setOrAdd((byte) (v >> (8 * 4)));
        setOrAdd((byte) (v >> (8 * 5)));
        setOrAdd((byte) (v >> (8 * 6)));
        setOrAdd((byte) (v >> (8 * 7)));
    }

    //endregion
    //endregion
    //endregion
    //region Floating point numbers
    //region Big-endian
    @Override
    public void writeF4be(float v) {
        writeAlignToByte();
        writeS4be(Float.floatToIntBits(v));
    }

    @Override
    public void writeF8be(double v) {
        writeAlignToByte();
        writeS8be(Double.doubleToLongBits(v));
    }

    //endregion
    //region Little-endian
    @Override
    public void writeF4le(float v) {
        writeAlignToByte();
        writeS4le(Float.floatToIntBits(v));
    }

    @Override
    public void writeF8le(double v) {
        writeAlignToByte();
        writeS8le(Double.doubleToLongBits(v));
    }

    //endregion
    //endregion
    //region Byte arrays
    @Override
    protected void writeBytesNotAligned(byte[] buf) {
        /*
        In ByteBufferKaitaiStream, this method calls bytebuffer.put().
        Javadoc for the ByteBuffer.put(byte[]) method says:
        "This method transfers the entire content of the given source byte array into this buffer.
        An invocation of this method of the form dst.put(a) behaves in exactly the same way as the
        invocation dst.put(a, 0, a.length)."
         */
        for (byte b : buf) {
            setOrAdd(b);
        }
    }

    //endregion
    //endregion
}
