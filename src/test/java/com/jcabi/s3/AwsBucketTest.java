/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3;

import java.io.IOException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Test case for {@link AwsBucket}.
 *
 * @since 0.1
 */
@SuppressWarnings("PMD.TooManyMethods")
final class AwsBucketTest {

    @Test
    void findsAndReturnsOckets() throws Exception {
        final Region region = Mockito.mock(Region.class);
        final Bucket bucket = new AwsBucket(region, "example.com");
        final Ocket ocket = bucket.ocket("test");
        MatcherAssert.assertThat("should be not null", ocket, Matchers.notNullValue());
    }

    @Test
    void existsExistingBucket() throws IOException {
        final Region region = Mockito.mock(Region.class);
        final S3Client aws = Mockito.mock(S3Client.class);
        Mockito.when(region.aws()).thenReturn(aws);
        Mockito.when(
            aws.headBucket(Mockito.any(HeadBucketRequest.class))
        ).thenReturn(HeadBucketResponse.builder().build());
        final Bucket bucket = new AwsBucket(
            region, "existing.bucket.com"
        );
        MatcherAssert.assertThat(
            "should be true",
            bucket.exists(),
            Matchers.is(true)
        );
    }

    @Test
    void existsNonExistingBucket() throws IOException {
        final Region region = Mockito.mock(Region.class);
        final S3Client aws = Mockito.mock(S3Client.class);
        Mockito.when(region.aws()).thenReturn(aws);
        Mockito.when(
            aws.headBucket(Mockito.any(HeadBucketRequest.class))
        ).thenThrow(
            NoSuchBucketException.builder().message("no bucket").build()
        );
        final Bucket bucket = new AwsBucket(
            region, "non.existing.bucket.com"
        );
        MatcherAssert.assertThat(
            "should be false",
            bucket.exists(),
            Matchers.is(false)
        );
    }

    @Test
    void existsThrowsIoException() throws IOException {
        final Region region = Mockito.mock(Region.class);
        final S3Client aws = Mockito.mock(S3Client.class);
        Mockito.when(region.aws()).thenReturn(aws);
        Mockito.when(
            aws.headBucket(Mockito.any(HeadBucketRequest.class))
        ).thenThrow(
            S3Exception.builder().message("Test exception").build()
        );
        final Bucket bucket = new AwsBucket(
            region, "throwing.bucket.com"
        );
        Assertions.assertThrows(
            IOException.class,
            bucket::exists,
            "should throw IOException"
        );
    }

}
