/**
 * Copyright 2015-2021 Kaitai Project: MIT license
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

import java.util.Map;

/**
 * This interface is implemented by each {@link KaitaiStruct} successor, if
 * class was generated with positional information.
 * <p>
 * If you want to work with generated structures in the generic way, you can use
 * following code snipped to deal with positions information:
 * <code>
 * final KaitaiStruct struct = ...;
 * // Generator generates classes, that implements this interface,
 * // if debug mode/positions-info is enabled
 * if (struct instanceof PositionInfo) {
 *     final PositionInfo info = (PositionInfo)struct;
 *     //...
 * }
 * </code>
 *
 * @since 0.10
 */
public interface PositionInfo {
    /**
     * Information about each struct field. If field is an array, then corresponding
     * {@code Span} will be of {@link ArraySpan} instance. Map keys is equals to the
     * names of the java methods/fields in the generated class.
     *
     * @return the map from field name to field span information.
     */
    Map<String, Span> _spans();
}
