/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3;

import com.amazonaws.services.s3.AmazonS3;
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
        // @checkstyle MagicNumberCheck (3 line)
        final String name = String.format(
            "%s.s3.jcabi.com",
            RandomStringUtils.randomAlphabetic(5)
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
        if (aws.doesBucketExistV2(this.subj.name())) {
            aws.deleteBucket(this.subj.name());
            Logger.info(this, "S3 bucket %s deleted", this.subj.name());
        }
    }

}
