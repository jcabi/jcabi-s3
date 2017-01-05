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

import com.amazonaws.services.s3.AmazonS3;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.jcabi.s3.cached.CdRegion;
import com.jcabi.s3.retry.ReRegion;
import java.util.Locale;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Rule that creates and drops an AWS subj.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.3
 */
final class BucketRule implements TestRule {

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
    private transient Bucket subj;

    @Override
    public Statement apply(final Statement stmt, final Description desc) {
        // @checkstyle IllegalThrows (10 lines)
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                if (BucketRule.KEY == null || BucketRule.KEY.isEmpty()) {
                    Logger.warn(
                        this,
                        "system property failsafe.s3.key is not set, skipping"
                    );
                } else {
                    BucketRule.this.create();
                    try {
                        stmt.evaluate();
                    } finally {
                        BucketRule.this.drop();
                    }
                }
            }
        };
    }

    /**
     * Get bucket.
     * @return Bucket
     */
    public Bucket bucket() {
        return this.subj;
    }

    /**
     * Create S3 subj.
     * @throws Exception If fails
     */
    private void create() throws Exception {
        final Region region = new CdRegion(
            new ReRegion(
                new Region.Simple(BucketRule.KEY, BucketRule.SECRET)
            )
        );
        final String name = String.format(
            "%s.s3.jcabi.com",
            RandomStringUtils.randomAlphabetic(Tv.FIVE)
                .toLowerCase(Locale.ENGLISH)
        );
        this.subj = region.bucket(name);
        final AmazonS3 aws = this.subj.region().aws();
        aws.createBucket(name);
        Logger.info(this, "S3 bucket %s created", name);
    }

    /**
     * Drop S3 subj.
     * @throws Exception If fails
     */
    private void drop() throws Exception {
        final AmazonS3 aws = this.subj.region().aws();
        if (aws.doesBucketExist(this.subj.name())) {
            aws.deleteBucket(this.subj.name());
            Logger.info(this, "S3 bucket %s deleted", this.subj.name());
        }
    }

}
