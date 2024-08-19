/**
 * Copyright 2015-2024 Kaitai Project: MIT license
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
 * Annotation, that applied to fields, getters or setters that represents parameter
 * from {@code params} KSY element.
 *
 * @since 0.9
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Parameter {
    /**
     * Original identifier ({@code id} key) from {@code ksy} file.
     *
     * @return Identifier, that can differ from parameter name, if it clash with
     *         Java reserved words. Can not be empty
     */
    String id();
    /**
     * Index of a parameter in sequence of parameters in the type.
     *
     * @return 0-based index of a parameter in {@code params} KSY element
     */
    int index();
    /**
     * Documentation string attached to the parameter, specified in {@code doc}
     * KSY element.
     *
     * @return Documentation string for parameter. If documentation is missed,
     *         returns empty string
     */
    String doc();
}
