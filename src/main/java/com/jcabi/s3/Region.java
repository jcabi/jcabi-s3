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

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Amazon S3 abstraction.
 *
 * <p>To get an instance of this interface, instantiate {@code Region.Simple},
 * for example:
 *
 * <pre> Region region = new Region.Simple(key, secret);
 * Bucket bucket = region.bucket("my.example.com");
 * bucket.remove("README.txt");</pre>
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.1
 */
@Immutable
public interface Region {

    /**
     * Get bucket.
     * @param name Name of the bucket to get
     * @return Bucket
     */
    Bucket bucket(String name);

    /**
     * Get a client.
     * @return Amazon S3
     */
    AmazonS3 aws();

    /**
     * Simple implementation.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = { "key", "secret" })
    @Loggable(Loggable.DEBUG)
    final class Simple implements Region {
        /**
         * Key.
         */
        private final transient String key;
        /**
         * Secret.
         */
        private final transient String secret;
        /**
         * Public ctor.
         * @param akey Amazon key
         * @param scrt Amazon secret
         */
        public Simple(final String akey, final String scrt) {
            this.key = akey;
            this.secret = scrt;
        }
        @Override
        public Bucket bucket(final String name) {
            return new AwsBucket(this, name);
        }
        @Override
        public AmazonS3 aws() {
            return new AmazonS3Client(
                new BasicAWSCredentials(this.key, this.secret)
            );
        }
    }
}
