/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3;

import java.io.IOException;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

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

    @Test
    void removesObjectFromBucket() throws Exception {
        final Region region = Mockito.mock(Region.class);
        final S3Client aws = Mockito.mock(S3Client.class);
        Mockito.when(region.aws()).thenReturn(aws);
        final Bucket bucket = new AwsBucket(
            region, UUID.randomUUID().toString()
        );
        bucket.remove(UUID.randomUUID().toString());
        Mockito.verify(aws).deleteObject(
            Mockito.any(DeleteObjectRequest.class)
        );
    }

    @Test
    void throwsOnRemoveWhenAwsFails() {
        final Region region = Mockito.mock(Region.class);
        final S3Client aws = Mockito.mock(S3Client.class);
        Mockito.when(region.aws()).thenReturn(aws);
        Mockito.doThrow(
            S3Exception.builder().message("delete failed").build()
        ).when(aws).deleteObject(
            Mockito.any(DeleteObjectRequest.class)
        );
        final Bucket bucket = new AwsBucket(
            region, UUID.randomUUID().toString()
        );
        Assertions.assertThrows(
            IOException.class,
            () -> bucket.remove(UUID.randomUUID().toString()),
            "remove did not throw IOException on S3 failure"
        );
    }

    @Test
    void listsObjectsInBucket() {
        final Region region = Mockito.mock(Region.class);
        final S3Client aws = Mockito.mock(S3Client.class);
        Mockito.when(region.aws()).thenReturn(aws);
        final String key = UUID.randomUUID().toString();
        Mockito.when(
            aws.listObjectsV2(Mockito.any(ListObjectsV2Request.class))
        ).thenReturn(
            ListObjectsV2Response.builder()
                .contents(S3Object.builder().key(key).build())
                .isTruncated(false)
                .build()
        );
        MatcherAssert.assertThat(
            "list did not contain the expected key",
            new AwsBucket(
                region, UUID.randomUUID().toString()
            ).list(""),
            Matchers.hasItem(key)
        );
    }

    @Test
    void comparesAlphabetically() {
        final Region region = Mockito.mock(Region.class);
        final String first = String.format("aaa-%s", UUID.randomUUID());
        final String second = String.format("zzz-%s", UUID.randomUUID());
        MatcherAssert.assertThat(
            "compareTo did not return negative for earlier bucket",
            new AwsBucket(region, first).compareTo(
                new AwsBucket(region, second)
            ),
            Matchers.lessThan(0)
        );
    }

}
