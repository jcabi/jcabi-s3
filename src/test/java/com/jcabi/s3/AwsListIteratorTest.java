/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

/**
 * Test case for {@link AwsListIterator}.
 *
 * @since 0.10
 */
final class AwsListIteratorTest {

    @Test
    void reportsNoElementsForEmptyListing() {
        final Region region = Mockito.mock(Region.class);
        final S3Client aws = Mockito.mock(S3Client.class);
        Mockito.when(region.aws()).thenReturn(aws);
        Mockito.when(
            aws.listObjectsV2(Mockito.any(ListObjectsV2Request.class))
        ).thenReturn(
            ListObjectsV2Response.builder()
                .contents(Collections.emptyList())
                .isTruncated(false)
                .build()
        );
        MatcherAssert.assertThat(
            "iterator was not empty for empty listing",
            new AwsListIterator(
                region, UUID.randomUUID().toString(), ""
            ).hasNext(),
            Matchers.is(false)
        );
    }

    @Test
    void iteratesOverSinglePageOfResults() {
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
            "single page result was not iterated correctly",
            new AwsListIterator(
                region, UUID.randomUUID().toString(), ""
            ).next(),
            Matchers.equalTo(key)
        );
    }

    @Test
    void iteratesOverMultiplePages() {
        final Region region = Mockito.mock(Region.class);
        final S3Client aws = Mockito.mock(S3Client.class);
        Mockito.when(region.aws()).thenReturn(aws);
        final String first = UUID.randomUUID().toString();
        final String second = UUID.randomUUID().toString();
        Mockito.when(
            aws.listObjectsV2(Mockito.any(ListObjectsV2Request.class))
        ).thenReturn(
            ListObjectsV2Response.builder()
                .contents(S3Object.builder().key(first).build())
                .isTruncated(true)
                .nextContinuationToken("token-abc")
                .build()
        ).thenReturn(
            ListObjectsV2Response.builder()
                .contents(S3Object.builder().key(second).build())
                .isTruncated(false)
                .build()
        );
        final AwsListIterator iterator = new AwsListIterator(
            region, UUID.randomUUID().toString(), ""
        );
        iterator.next();
        MatcherAssert.assertThat(
            "second page was not iterated after first",
            iterator.next(),
            Matchers.equalTo(second)
        );
    }

    @Test
    void throwsNoSuchElementWhenExhausted() {
        final Region region = Mockito.mock(Region.class);
        final S3Client aws = Mockito.mock(S3Client.class);
        Mockito.when(region.aws()).thenReturn(aws);
        Mockito.when(
            aws.listObjectsV2(Mockito.any(ListObjectsV2Request.class))
        ).thenReturn(
            ListObjectsV2Response.builder()
                .contents(Collections.emptyList())
                .isTruncated(false)
                .build()
        );
        Assertions.assertThrows(
            NoSuchElementException.class,
            () -> new AwsListIterator(
                region, UUID.randomUUID().toString(), ""
            ).next(),
            "next() did not throw on exhausted iterator"
        );
    }

    @Test
    void throwsOnRemove() {
        Assertions.assertThrows(
            UnsupportedOperationException.class,
            () -> new AwsListIterator(
                Mockito.mock(Region.class),
                UUID.randomUUID().toString(), ""
            ).remove(),
            "remove() did not throw"
        );
    }

}
