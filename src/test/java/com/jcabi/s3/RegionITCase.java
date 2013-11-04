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

import com.amazonaws.services.s3.AmazonS3;
import com.jcabi.aspects.Tv;
import java.util.Locale;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration case for {@link Region}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.1
 */
public final class RegionITCase {

    /**
     * AWS key.
     */
    private static final String KEY =
        System.getProperty("failsafe.s3.key");

    /**
     * AWS secret.
     */
    private static final String SECRET =
        System.getProperty("failsafe.s3.secret");

    /**
     * Bucket we're working with.
     */
    private transient Bucket bucket;

    /**
     * Create S3 bucket.
     * @throws Exception If fails
     */
    @Before
    public void create() throws Exception {
        if (RegionITCase.KEY == null) {
            return;
        }
        final Region region = new Region.Simple(
            RegionITCase.KEY, RegionITCase.SECRET
        );
        final String name = String.format(
            "%s.s3.jcabi.com",
            RandomStringUtils.randomAlphabetic(Tv.FIVE)
                .toLowerCase(Locale.ENGLISH)
        );
        this.bucket = region.bucket(name);
        final AmazonS3 aws = this.bucket.region().aws();
        aws.createBucket(name);
    }

    /**
     * Drop S3 bucket.
     * @throws Exception If fails
     */
    @After
    public void drop() throws Exception {
        if (RegionITCase.KEY == null) {
            return;
        }
        final AmazonS3 aws = this.bucket.region().aws();
        aws.deleteBucket(this.bucket.name());
    }

    /**
     * Region can read and write S3 content.
     * @throws Exception If fails
     */
    @Test
    public void readsAndWritesObjectContent() throws Exception {
        Assume.assumeThat(RegionITCase.KEY, Matchers.notNullValue());
        final String name = "a/b/c/test.txt";
        final Ocket.Text ocket = new Ocket.Text(this.bucket.ocket(name));
        final String content = "text \u20ac\n\t\rtest";
        ocket.write(content);
        MatcherAssert.assertThat(ocket.read(), Matchers.equalTo(content));
        this.bucket.remove(name);
    }

}
