package io.kaitai.struct;

import java.io.EOFException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.BufferUnderflowException;

import static org.testng.Assert.*;

public class RandomAccessFileKaitaiStreamTest extends KaitaiStreamTest {
    static String TEST_SCRATCH_DIR = "target/test-scratch";

    @org.testng.annotations.BeforeMethod
    public void setUp() throws IOException {
        File testScratchDir = new File(TEST_SCRATCH_DIR);
        if (!testScratchDir.exists()) testScratchDir.mkdirs();

        String testFileName = testScratchDir + "/12345.bin";

        FileWriter writer = new FileWriter(testFileName);
        writer.write("12345");
        writer.close();

        stream = new RandomAccessFileKaitaiStream(testFileName);
    }

    @Override
    Class getEOFClass() {
        return RuntimeException.class;
    }
}