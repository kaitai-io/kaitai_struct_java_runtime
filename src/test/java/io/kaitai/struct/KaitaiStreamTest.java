package io.kaitai.struct;

import org.testng.annotations.Test;

import java.nio.BufferUnderflowException;

import static org.testng.Assert.*;

public abstract class KaitaiStreamTest {
    KaitaiStream stream;

    abstract Class getEOFClass();

    @org.testng.annotations.Test
    public void testReadS1() {
        short first = stream.readS1();
        assertEquals(first, 0x31);
        short second = stream.readS1();
        assertEquals(second, 0x32);
    }

    @org.testng.annotations.Test
    public void testReadS2be() {
        short first = stream.readS2be();
        assertEquals(first, 0x3132);
        short second = stream.readS2be();
        assertEquals(second, 0x3334);

        assertThrows(getEOFClass(), new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                stream.readS2be();
            }
        });
    }

    @org.testng.annotations.Test
    public void testReadBytes5() {
        byte[] actual = stream.readBytes(5);
        assertEquals(actual, new byte[] {'1', '2', '3', '4', '5'});
    }

    @org.testng.annotations.Test
    public void testReadBytes6() {
        assertThrows(getEOFClass(), new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                stream.readBytes(6);
            }
        });
    }

    @org.testng.annotations.Test
    public void testSubstream() {
        stream.seek(1);
        assertEquals(stream.pos(), 1);

        final KaitaiStream substream = stream.substream(3);

        assertEquals(substream.pos(), 0);
        assertEquals(stream.pos(), 4);

        byte byte0Sub = substream.readS1();
        assertEquals(byte0Sub, '2');
        assertEquals(substream.pos(), 1);
        assertEquals(stream.pos(), 4);

        byte byte1Sub = substream.readS1();
        assertEquals(byte1Sub, '3');
        assertEquals(substream.pos(), 2);
        assertEquals(stream.pos(), 4);

        byte byte4Main = stream.readS1();
        assertEquals(byte4Main, '5');
        assertEquals(substream.pos(), 2);
        assertEquals(stream.pos(), 5);

        byte byte2Sub = substream.readS1();
        assertEquals(byte2Sub, '4');
        assertEquals(substream.pos(), 3);
        assertEquals(stream.pos(), 5);

        assertThrows(getEOFClass(), new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                substream.readS1();
            }
        });

        assertTrue(substream.isEof());
    }
}