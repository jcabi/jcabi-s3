/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3;

import com.jcabi.log.Logger;
import com.jcabi.s3.cached.CdRegion;
import com.jcabi.s3.retry.ReRegion;
import java.util.Locale;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

/**
 * Extension that creates and drops an AWS subj.
 *
 * @since 0.3
 */
final class BucketRule implements BeforeEachCallback, AfterEachCallback {

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
    public void beforeEach(final ExtensionContext ctx) throws Exception {
        Assumptions.assumeTrue(
            BucketRule.KEY != null && !BucketRule.KEY.isEmpty(),
            "system property failsafe.s3.key is not set, skipping"
        );
        this.create();
    }

    @Override
    public void afterEach(final ExtensionContext ctx) throws Exception {
        if (this.subj != null) {
            this.drop();
        }
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
        final S3Client aws = this.subj.region().aws();
        aws.createBucket(
            CreateBucketRequest.builder().bucket(name).build()
        );
        Logger.info(this, "S3 bucket %s created", name);
    }

    /**
     * Drop S3 subj.
     * @throws Exception If fails
     */
    private void drop() throws Exception {
        final S3Client aws = this.subj.region().aws();
        try {
            aws.headBucket(
                HeadBucketRequest.builder()
                    .bucket(this.subj.name())
                    .build()
            );
            aws.deleteBucket(
                DeleteBucketRequest.builder()
                    .bucket(this.subj.name())
                    .build()
            );
            Logger.info(this, "S3 bucket %s deleted", this.subj.name());
        } catch (final NoSuchBucketException ex) {
            Logger.info(
                this, "S3 bucket %s already gone", this.subj.name()
            );
        }
    }

}
