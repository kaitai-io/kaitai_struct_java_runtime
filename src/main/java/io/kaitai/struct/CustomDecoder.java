package io.kaitai.struct;

/**
 * A custom decoder interface. Implementing classes can be called
 * from inside a .ksy file using `process: XXX` syntax.
 */
public interface CustomDecoder {
    /**
     * Decodes a given byte array, according to some custom algorithm
     * (specific to implementing class) and parameters given in the
     * constructor, returning another byte array.
     * @param src source byte array
     * @return decoded byte array
     */
    byte[] decode(byte[] src);
}
