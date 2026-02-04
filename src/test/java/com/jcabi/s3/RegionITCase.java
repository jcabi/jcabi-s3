/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

/**
 * Integration case for {@link Region}.
 *
 * @since 0.1
 */
@SuppressWarnings("PMD.JUnit5TestShouldBePackagePrivate")
public final class RegionITCase {

    /**
     * Bucket we're working with.
     * @checkstyle VisibilityModifier (3 lines)
     */
    @Rule
    public final transient BucketRule rule = new BucketRule();

    /**
     * Region can connect to AWS and check bucket existence.
     * @throws Exception If fails
     */
    @Test
    public void connectsToAmazon() throws Exception {
        final Bucket bucket = this.rule.bucket();
        final S3Client aws = bucket.region().aws();
        MatcherAssert.assertThat(
            "should be true",
            aws.headBucket(
                HeadBucketRequest.builder()
                    .bucket(bucket.name())
                    .build()
            ),
            Matchers.notNullValue()
        );
    }

}
