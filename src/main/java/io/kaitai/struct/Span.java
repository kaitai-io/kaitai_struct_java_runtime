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

/**
 * Information about positions of parsed value in the streams.
 *
 * @since 0.10
 */
public class Span {
    /** Offset from begin of the root stream, for which that span was created. */
    public final long offset;
    /**
     * Offset from begin of the stream, from which value was parsed. This is relative
     * position, to get an absolute position (relative to the root stream) use
     * {@link #absoluteStart()}. That offset is always non-negative.
     */
    public final long start;
    /**
     * Offset from begin of the stream, from which value was parsed. This is relative
     * position, to get an absolute position (relative to the root stream) use
     * {@link #absoluteEnd()}.
     * <p>
     * If that offset is negative, then value wasn't parsed yet or exception was
     * thrown while parsing value.
     */
    public long end = -1;

    /**
     * Creates a span that starts at the current stream offset and ends at
     * the unknown position.
     *
     * @param io the stream to get the positional information
     */
    public Span(KaitaiStream io) {
        this(io.offset(), io.pos());
    }
    private Span(long offset, long start) {
        this.offset = offset;
        this.start = start;
    }

    /**
     * Offset to the start of this span relative to the root stream.
     *
     * @return start offset from the root stream
     */
    public long absoluteStart() { return offset + start; }
    /**
     * Offset to the end of this span relative to the root stream.
     * <p>
     * If that offset is negative, then value wasn't parsed yet or exception was
     * thrown while parsing value.
     *
     * @return start offset from the root stream or negative value if value not yet parsed
     */
    public long absoluteEnd() { return end < 0 ? -1 : offset + end; }
    /**
     * Size of this span in bytes.
     * <p>
     * If size is negative, then value wasn't parsed yet or exception was
     * thrown while parsing value.
     *
     * @return size of the span in bytes
     */
    public long size() { return end < 0 ? -1 : end - start; }
}
