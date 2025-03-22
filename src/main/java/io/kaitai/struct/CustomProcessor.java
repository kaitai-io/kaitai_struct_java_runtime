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

/**
 * A custom encoder/decoder interface. Implementing classes can be called from
 * inside a .ksy file using {@code process: XXX} syntax.
 * <p>
 * Custom processing classes which need to be used from .ksy files that will be
 * compiled in {@code --read-write} mode should implement this interface. For
 * generated format libraries that are read-only (only capable of parsing, not
 * serialization), it's enough to implement {@link CustomDecoder}.
 */
public interface CustomProcessor extends CustomDecoder {
    /**
     * Encodes a given byte array, according to some custom algorithm (specific
     * to implementing class) and parameters given in the constructor, returning
     * another byte array.
     * <p>
     * This method is used in serialization. The inverse operation is
     * {@link #decode(byte[])}, which must return the same byte array as
     * {@code src} when given the encoded byte array returned by this method.
     * @param src source byte array
     * @return encoded byte array
     */
    byte[] encode(byte[] src);
}
