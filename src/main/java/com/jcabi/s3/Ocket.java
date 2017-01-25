/**
 * Copyright (c) 2012-2017, jcabi.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the jcabi.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jcabi.s3;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Amazon S3 object abstraction.
 *
 * <p>You get an instance of this interface from {@link Bucket}, for example:
 *
 * <pre> Region region = new Region.Simple(key, secret);
 * Bucket bucket = region.bucket("my.example.com");
 * Ocket ocket = bucket.ocket("src/main/README.txt");</pre>
 *
 * <p>In order to read and write plain text content in Unicode we recommend
 * to use {@code Ocket.Text} decorator:
 *
 * <pre> Ocket.Text ocket = new Ocket.Smart(
 *   bucket.ocket("src/main/README.txt")
 * );
 * ocket.write("hello, world!", "text/plain");
 * </pre>
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.1
 */
@Immutable
@SuppressWarnings("PMD.TooManyMethods")
public interface Ocket extends Comparable<Ocket> {

    /**
     * Get bucket we're in.
     * @return Bucket
     */
    Bucket bucket();

    /**
     * Get object key.
     * @return Key
     */
    String key();

    /**
     * Object metadata.
     *
     * <p>Throws {@link OcketNotFoundException} if this object
     * doesn't exist in S3 bucket.</p>
     *
     * @return Metadata
     * @throws IOException If fails
     */
    ObjectMetadata meta() throws IOException;

    /**
     * Check whether this S3 object exists.
     * @return TRUE if it exists in S3, FALSE otherwise
     * @throws IOException If fails
     * @since 0.4
     */
    boolean exists() throws IOException;

    /**
     * Read content.
     *
     * <p>Throws {@link OcketNotFoundException} if this object
     * doesn't exist in S3 bucket.</p>
     *
     * @param output Where to write
     * @throws IOException If fails
     */
    void read(OutputStream output) throws IOException;

    /**
     * Write new content to the object.
     * @param input Where to get content
     * @param meta Metadata to save. Should contains input length for large
     *  object, otherwise multi-part uploads won't be possible.
     * @throws IOException If fails
     */
    void write(InputStream input, ObjectMetadata meta)
        throws IOException;

    /**
     * Unicode text S3 object with supplementary functions.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "origin")
    @Loggable(Loggable.DEBUG)
    final class Text implements Ocket {
        /**
         * Original encapsulated ocket.
         */
        private final transient Ocket origin;
        /**
         * Public ctor.
         * @param ocket Original ocket
         */
        public Text(final Ocket ocket) {
            this.origin = ocket;
        }
        /**
         * Read content as string.
         * @return Content
         * @throws IOException If fails
         */
        public String read() throws IOException {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            this.origin.read(baos);
            return baos.toString(StandardCharsets.UTF_8.name());
        }
        /**
         * Write content as string.
         * @param text Text to write
         * @throws IOException If fails
         */
        public void write(final String text) throws IOException {
            this.write(text, "text/plain");
        }
        /**
         * Write content as string, with a specified content type.
         * @param text Text to write
         * @param type Content type
         * @throws IOException If fails
         */
        public void write(final String text, final String type)
            throws IOException {
            final ObjectMetadata meta = new ObjectMetadata();
            meta.setContentType(type);
            meta.setContentLength(
                (long) text.getBytes(StandardCharsets.UTF_8).length
            );
            meta.setContentEncoding(StandardCharsets.UTF_8.displayName());
            this.origin.write(
                new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)),
                meta
            );
        }
        @Override
        public Bucket bucket() {
            return this.origin.bucket();
        }
        @Override
        public String key() {
            return this.origin.key();
        }
        @Override
        public ObjectMetadata meta() throws IOException {
            return this.origin.meta();
        }
        @Override
        public boolean exists() throws IOException {
            return this.origin.exists();
        }
        @Override
        public void read(final OutputStream output) throws IOException {
            this.origin.read(output);
        }
        @Override
        public void write(final InputStream input, final ObjectMetadata meta)
            throws IOException {
            this.origin.write(input, meta);
        }
        @Override
        public int compareTo(final Ocket ocket) {
            return this.origin.compareTo(ocket);
        }
    }

    /**
     * Ocket with no content at all.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode
    @Loggable(Loggable.DEBUG)
    final class Empty implements Ocket {
        @Override
        public Bucket bucket() {
            throw new UnsupportedOperationException("#bucket()");
        }
        @Override
        public String key() {
            return "empty";
        }
        @Override
        public ObjectMetadata meta() {
            return new ObjectMetadata();
        }
        @Override
        public boolean exists() {
            return true;
        }
        @Override
        public void read(final OutputStream output) {
            // nothing
        }
        @Override
        public void write(final InputStream input, final ObjectMetadata meta) {
            // nothing
        }
        @Override
        public int compareTo(final Ocket ocket) {
            return 0;
        }
    }

}
