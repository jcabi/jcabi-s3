/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import java.io.IOException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link AwsBucket}.
 *
 * @since 0.1
 */
@SuppressWarnings({"PMD.TooManyMethods",
    "PMD.JUnit5TestShouldBePackagePrivate"})
public final class AwsBucketTest {

    /**
     * AwsBucket can find and return ockets.
     * @throws Exception If fails
     */
    @Test
    public void findsAndReturnsOckets() throws Exception {
        final Region region = Mockito.mock(Region.class);
        final Bucket bucket = new AwsBucket(region, "example.com");
        final Ocket ocket = bucket.ocket("test");
        MatcherAssert.assertThat("should be not null", ocket, Matchers.notNullValue());
    }

    /**
     * AwsBucket can correctly check the existence of the existing bucket.
     * @throws IOException If fails
     */
    @Test
    public void existsExistingBucket() throws IOException {
        final Region region = Mockito.mock(Region.class);
        final AmazonS3 aws = Mockito.mock(AmazonS3.class);
        Mockito.when(region.aws()).thenReturn(aws);
        final String name = "existing.bucket.com";
        Mockito.when(aws.doesBucketExistV2(name)).thenReturn(true);
        final Bucket bucket = new AwsBucket(region, name);
        MatcherAssert.assertThat(
            "should be true",
            bucket.exists(),
            Matchers.is(true)
        );
    }

    /**
     * AwsBucket can correctly check the existence of the non-existing bucket.
     * @throws IOException If fails
     */
    @Test
    public void existsNonExistingBucket() throws IOException {
        final Region region = Mockito.mock(Region.class);
        final AmazonS3 aws = Mockito.mock(AmazonS3.class);
        Mockito.when(region.aws()).thenReturn(aws);
        final String name = "non.existing.bucket.com";
        Mockito.when(aws.doesBucketExistV2(name)).thenReturn(false);
        final Bucket bucket = new AwsBucket(region, name);
        MatcherAssert.assertThat(
            "should be false",
            bucket.exists(),
            Matchers.is(false)
        );
    }

    /**
     * AwsBucket can throw a proper exception.
     * @throws IOException If succeeds
     */
    @Test(expected = IOException.class)
    public void existsThrowsIoException() throws IOException {
        final Region region = Mockito.mock(Region.class);
        final AmazonS3 aws = Mockito.mock(AmazonS3.class);
        Mockito.when(region.aws()).thenReturn(aws);
        final String name = "throwing.bucket.com";
        Mockito.when(aws.doesBucketExistV2(name)).thenThrow(
            new AmazonServiceException("Test exception")
        );
        final Bucket bucket = new AwsBucket(region, name);
        bucket.exists();
    }

}
