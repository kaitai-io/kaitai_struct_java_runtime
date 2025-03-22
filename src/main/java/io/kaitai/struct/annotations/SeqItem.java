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
package io.kaitai.struct.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation, that applied to fields, getters or setters that represents an attribute
 * from {@code seq} KSY element.
 *
 * @since 0.9
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface SeqItem {
    /**
     * Original identifier ({@code id} key) from {@code ksy} file.
     *
     * @return Identifier, that can differ from field name, if it clash with
     *         Java reserved words. Empty string, if attribute was unnamed
     */
    String id();
    /**
     * Index of an attribute in sequence of attributes in the type.
     *
     * @return 0-based index of an attribute in {@code seq} KSY element
     */
    int index();
    /**
     * Documentation string attached to the attribute, specified in {@code doc}
     * KSY element.
     *
     * @return Documentation string for and attribute. If documentation is missed,
     *         returns empty string
     */
    String doc();
}
