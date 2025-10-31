/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3;

import com.amazonaws.services.s3.AmazonS3;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;

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
        final AmazonS3 aws = bucket.region().aws();
        MatcherAssert.assertThat(
            "should be true",
            aws.doesBucketExistV2(bucket.name()),
            Matchers.is(true)
        );
    }

}
