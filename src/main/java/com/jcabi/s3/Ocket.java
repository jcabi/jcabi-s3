/**
 * Copyright (c) 2012-2013, JCabi.com
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
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.CharEncoding;

/**
 * Amazon S3 object abstraction.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface Ocket {

    /**
     * Get object key.
     * @return Key
     */
    String key();

    /**
     * Object metadata.
     * @return Metadata
     * @throws IOException If fails
     */
    ObjectMetadata meta() throws IOException;

    /**
     * Read content.
     * @param output Where to write
     * @throws IOException If fails
     */
    void read(OutputStream output) throws IOException;

    /**
     * Write new content to the object.
     * @param input Where to get content
     * @param meta Metadata to save
     * @throws IOException If fails
     */
    void write(InputStream input, ObjectMetadata meta) throws IOException;

    /**
     * Plain and simple S3 object with supplementary functions.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "origin")
    @Loggable(Loggable.DEBUG)
    final class Plain {
        /**
         * Original encapsulated ocket.
         */
        private final transient Ocket origin;
        /**
         * Public ctor.
         * @param ocket Original ocket
         */
        public Plain(final Ocket ocket) {
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
            return baos.toString(CharEncoding.UTF_8);
        }
        /**
         * Read content as string.
         * @param text Text to write
         * @throws IOException If fails
         */
        public void write(final String text) throws IOException {
            final ObjectMetadata meta = new ObjectMetadata();
            meta.setContentType("text/plain");
            meta.setContentLength((long) text.getBytes(Charsets.UTF_8).length);
            meta.setContentEncoding(CharEncoding.UTF_8);
            this.origin.write(
                new ByteArrayInputStream(text.getBytes(CharEncoding.UTF_8)),
                meta
            );
        }
    }

}
