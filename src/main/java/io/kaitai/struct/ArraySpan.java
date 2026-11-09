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

import java.util.ArrayList;
import java.util.List;

/**
 * Span that represents positional information of array field and each of it
 * elements. Spans of items is available in the {@link #items} field.
 *
 * @since 0.10
 */
public class ArraySpan extends Span {
    /** Individual span of the each item in the array. */
    public final List<Span> items;

    /**
     * Creates a span of array that starts at the current stream offset and
     * ends at the unknown position.
     *
     * @param io the stream to get positional information
     */
    public ArraySpan(KaitaiStream io) {
        super(io);
        items = new ArrayList<Span>();
    }

    public ArraySpan(KaitaiStream io, int size) {
        super(io);
        items = new ArrayList<Span>(size);
    }

    /**
     * Appends a new span of array item from current stream position to the end-of-stream
     * to this span
     *
     * @param io Stream used to inquire current position
     * @return A new span, added to the internal list of item spans
     */
    public Span addItem(KaitaiStream io) {
        final Span span = new Span(io);
        items.add(span);
        return span;
    }
}
