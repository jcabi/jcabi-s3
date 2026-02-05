/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3;

import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Test case for {@link Region.Simple}.
 *
 * @since 0.1
 */
final class RegionSimpleTest {

    @Test
    void returnsBucketByName() {
        final String name = UUID.randomUUID().toString();
        MatcherAssert.assertThat(
            "bucket name did not match the requested name",
            new Region.Simple(
                Mockito.mock(S3Client.class)
            ).bucket(name).name(),
            Matchers.equalTo(name)
        );
    }

    @Test
    void returnsAwsClient() {
        final S3Client aws = Mockito.mock(S3Client.class);
        MatcherAssert.assertThat(
            "aws client was not the same instance",
            new Region.Simple(aws).aws(),
            Matchers.sameInstance(aws)
        );
    }

    @Test
    void createsRegionWithCredentials() {
        MatcherAssert.assertThat(
            "region was not created from credentials",
            new Region.Simple(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
            ).aws(),
            Matchers.notNullValue()
        );
    }

    @Test
    void createsRegionWithCustomRegion() {
        MatcherAssert.assertThat(
            "region was not created with custom AWS region",
            new Region.Simple(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "eu-west-1"
            ).aws(),
            Matchers.notNullValue()
        );
    }

    @Test
    void returnsBucketBoundToItself() {
        final Region region = new Region.Simple(
            Mockito.mock(S3Client.class)
        );
        MatcherAssert.assertThat(
            "bucket region did not match the originating region",
            region.bucket(UUID.randomUUID().toString()).region(),
            Matchers.sameInstance(region)
        );
    }

}
