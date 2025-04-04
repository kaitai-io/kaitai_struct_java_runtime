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
 * A custom decoder interface. Implementing classes can be called from inside a
 * .ksy file using `process: XXX` syntax.
 * <p>
 * This interface is sufficient for custom processing routines that will only be
 * used from generated format libraries that are read-only (only capable of
 * parsing, not serialization). To support generated source files compiled in
 * {@code --read-write} mode, implement {@link CustomProcessor} instead.
 */
public interface CustomDecoder {
    /**
     * Decodes a given byte array, according to some custom algorithm
     * (specific to implementing class) and parameters given in the
     * constructor, returning another byte array.
     * <p>
     * This method is used in parsing. Its counterpart is
     * {@link CustomProcessor#encode(byte[])}, which is used in serialization.
     * @param src source byte array
     * @return decoded byte array
     */
    byte[] decode(byte[] src);
}
