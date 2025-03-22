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
 * Annotation, that applied to Kaitai-generated classes. Visualizers can use that
 * annotation to find classes, that contains generated stuff, that should be showed
 * in visualization.
 *
 * @since 0.9
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Generated {
    /**
     * Original identifier ({@code id} key) from {@code ksy} file.
     *
     * @return Identifier, that can differ from class name, if it clash with
     *         Java reserved words. Can not be empty
     */
    String id();
    /**
     * Version of compiler, that generated this class.
     *
     * @return Version string in <a href="https://semver.org/">semver</a> format
     */
    String version();
    /**
     * Class compiled with support of position tracking. That means, that every class
     * has following public fields (in that version of generator):
     * <table>
     * <caption>Position tracking info.</caption>
     * <tr><th>Type</th><th>Field</th><th>Description</th></tr>
     * <tr><td>{@code Map<String, Integer>}</td><td>{@code _attrStart}</td>
     *     <td>Start offset in the root stream, where {@link SeqItem an attribute} or
     *         {@link Instance an instance} with specified name begins.
     *         Used only for attributes/instances, that is not repeated</td>
     * </tr>
     * <tr><td>{@code Map<String, Integer>}</td><td>{@code _attrEnd}</td>
     *     <td>Start offset in the root stream, where {@link SeqItem an attribute} or
     *         {@link Instance an instance} with specified name ends (exclusive).
     *         Used only for attributes/instances, that is not repeated</td>
     * </tr>
     * <tr><td>{@code Map<String, ? extends List<Integer>>}</td><td>{@code _arrStart}</td>
     *     <td>List with start offset in the root stream, where each array element of
     *         repeated {@link SeqItem attribute} or {@link Instance instance} with
     *         specified name begins. Used only for attributes/instances, that is repeated</td>
     * </tr>
     * <tr><td>{@code Map<String, ? extends List<Integer>>}</td><td>{@code _arrEnd}</td>
     *     <td>List with end offset (exclusive) in the root stream, where each array
     *         element of repeated {@link SeqItem attribute} or {@link Instance instance}
     *         with specified name ends. Used only for attributes/instances, that is repeated</td>
     * </tr>
     * </table>
     *
     * @return {@code true}, if position tracking is enabled and {@code false} otherwise
     */
    boolean posInfo();
    /**
     * Determines, if instantiation of user classes (related to user-types, defined
     * in {@code ksy} file) automatically read its content from the stream, or that must
     * be performed manually by calling generated {@code _read()}, {@code _readBE()}
     * or {@code _readLE()} method.
     *
     * @return {@code true}, if generated {@code _read()} method invoked automatically
     *         by class constructors and {@code false}, if it must be called explicitly
     */
    boolean autoRead();
    /**
     * Documentation string attached to the type definition, specified in {@code doc}
     * KSY element.
     *
     * @return Documentation string for a type. If documentation is missed, returns empty string
     */
    String doc();
}
