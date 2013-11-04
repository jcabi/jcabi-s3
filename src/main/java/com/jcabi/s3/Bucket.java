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
public interface Bucket {

    /**
     * Get region we're in.
     * @return Region
     */
    @NotNull
    Region region();

    /**
     * Get bucket name.
     * @return Bucket name
     */
    @NotNull
    String name();

    /**
     * Get object.
     * @param key Name of it in the bucket
     * @return Ocket
     */
    @NotNull
    Ocket ocket(@NotNull String key);

    /**
     * Delete object from bucket.
     * @param key Name of it in the bucket
     * @throws OcketNotFoundException If not found
     */
    void remove(@NotNull String key) throws OcketNotFoundException;

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
        public Prefixed(final Bucket bucket, final String pfx) {
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
        public void remove(final String key) throws OcketNotFoundException {
            this.origin.remove(this.extend(key));
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
