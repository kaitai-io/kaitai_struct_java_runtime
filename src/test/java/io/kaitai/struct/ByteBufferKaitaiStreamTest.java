package io.kaitai.struct;

import java.nio.BufferUnderflowException;

public class ByteBufferKaitaiStreamTest extends KaitaiStreamTest {
    @org.testng.annotations.BeforeMethod
    public void setUp() {
        stream = new ByteBufferKaitaiStream(new byte[] { '1', '2', '3', '4', '5' });
    }

    @Override
    Class getEOFClass() {
        return BufferUnderflowException.class;
    }
}
