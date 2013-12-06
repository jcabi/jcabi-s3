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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import java.io.IOException;
import java.util.Iterator;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Amazon S3 bucket.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@Immutable
@SuppressWarnings("PMD.TooManyMethods")
public interface Bucket {

    /**
     * Get region we're in.
     * @return Region
     */
    @NotNull(message = "region is never NULL")
    Region region();

    /**
     * Get bucket name.
     * @return Bucket name
     */
    @NotNull(message = "bucket name is never NULL")
    String name();

    /**
     * Get object.
     * @param key Name of it in the bucket
     * @return Ocket
     */
    @NotNull(message = "ocket is never NULL")
    Ocket ocket(@NotNull(message = "S3 key can't be NULL") String key);

    /**
     * Delete object from bucket.
     * @param key Name of it in the bucket
     * @throws IOException If not found or any other failure
     */
    void remove(@NotNull(message = "S3 key can't be NULL") String key)
        throws IOException;

    /**
     * List object names with a given prefix.
     * @param pfx Prefix to use
     * @return Iterable of names
     * @throws IOException If fails
     * @since 0.3
     */
    Iterable<String> list(@NotNull(message = "prefix can't be NULL")
        String pfx) throws IOException;

    /**
     * Prefixed.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = { "origin", "prefix" })
    @Loggable(Loggable.DEBUG)
    final class Prefixed implements Bucket {
        /**
         * Original encapsulated bucket.
         */
        private final transient Bucket origin;
        /**
         * Prefix.
         */
        private final transient String prefix;
        /**
         * Public ctor.
         * @param bucket Original bucket
         * @param pfx Prefix
         */
        public Prefixed(
            @NotNull(message = "bucket can't be NULL") final Bucket bucket,
            @NotNull(message = "prefix can't be NULL") final String pfx) {
            this.origin = bucket;
            this.prefix = pfx;
        }
        @Override
        public Region region() {
            return this.origin.region();
        }
        @Override
        public String name() {
            return this.origin.name();
        }
        @Override
        public Ocket ocket(final String key) {
            return this.origin.ocket(this.extend(key));
        }
        @Override
        public void remove(final String key) throws IOException {
            this.origin.remove(this.extend(key));
        }
        @Override
        public Iterable<String> list(final String pfx) throws IOException {
            // @checkstyle AnonInnerLength (50 lines)
            return new Iterable<String>() {
                @Override
                public Iterator<String> iterator() {
                    final Iterator<String> list;
                    try {
                        list = Bucket.Prefixed.this.origin
                            .list(Bucket.Prefixed.this.extend(pfx)).iterator();
                    } catch (IOException ex) {
                        throw new IllegalStateException(ex);
                    }
                    return new Iterator<String>() {
                        @Override
                        public boolean hasNext() {
                            return list.hasNext();
                        }
                        @Override
                        public String next() {
                            return list.next().substring(
                                Bucket.Prefixed.this.prefix.length()
                            );
                        }
                        @Override
                        public void remove() {
                            list.remove();
                        }
                    };
                }
            };
        }
        /**
         * Extend name with a prefix.
         * @param name The name to extend
         * @return Extended
         */
        private String extend(final String name) {
            return String.format("%s%s", this.prefix, name);
        }
    }

}
