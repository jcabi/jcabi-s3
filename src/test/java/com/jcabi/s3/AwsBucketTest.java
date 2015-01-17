/**
 * Copyright (c) 2012-2014, jcabi.com
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

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Test case for {@link AwsBucket}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.1
 */
@SuppressWarnings("PMD.TooManyMethods")
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
        MatcherAssert.assertThat(ocket, Matchers.notNullValue());
    }

    /**
     * AwsBucket can correctly check the existence of the existing bucket.
     * @throws IOException If fails
     */
    @Test
    public void existsExistingBucket() throws IOException {
        final String bucketName = "existing.bucket.com";
        final Region region = Mockito.mock(Region.class);
        final AmazonS3 aws = Mockito.mock(AmazonS3.class);
        Mockito.when(region.aws()).thenReturn(aws);
        Mockito.when(aws.doesBucketExist(bucketName)).thenReturn(true);
        final Bucket bucket = new AwsBucket(region, bucketName);
        Assert.assertTrue(bucket.exists());
    }

    /**
     * AwsBucket can correctly check the existence of the non-existing bucket.
     * @throws IOException If fails
     */
    @Test
    public void existsNonExistingBucket() throws IOException {
        final String bucketName = "non.existing.bucket.com";
        final Region region = Mockito.mock(Region.class);
        final AmazonS3 aws = Mockito.mock(AmazonS3.class);
        Mockito.when(region.aws()).thenReturn(aws);
        Mockito.when(aws.doesBucketExist(bucketName)).thenReturn(false);
        final Bucket bucket = new AwsBucket(region, bucketName);
        Assert.assertFalse(bucket.exists());
    }

    /**
     * AwsBucket can throw a proper exception.
     * @throws IOException If succeeds
     */
    @Test(expected = IOException.class)
    public void existsThrowsIOException() throws IOException {
        final String bucketName = "throwing.bucket.com";
        final Region region = Mockito.mock(Region.class);
        final AmazonS3 aws = Mockito.mock(AmazonS3.class);
        Mockito.when(region.aws()).thenReturn(aws);
        Mockito.when(aws.doesBucketExist(bucketName)).thenThrow(
            new AmazonServiceException("Test exception")
        );
        final Bucket bucket = new AwsBucket(region, bucketName);
        bucket.exists();
    }

    /**
     * AwsBucket supports listing large buckets.
     * @throws Exception if test fails
     */
    @Test
    public void supportsListingLargeBuckets() throws Exception {
        final ListingLargeBucketsTestData data =
            new ListingLargeBucketsTestData();
        final Region region = this.mockRegionForListingLargeBuckets(data);
        final Bucket bucket = new AwsBucket(region, data.bucketName());
        final Iterator<String> actual = bucket.list(data.prefix()).iterator();
        Assert.assertTrue(actual.hasNext());
        Assert.assertEquals(data.firstItem(), actual.next());
        Assert.assertTrue(actual.hasNext());
        Assert.assertEquals(data.secondItem(), actual.next());
        Assert.assertFalse(actual.hasNext());
    }

    /**
     * Returns a mocked region for supportsListingLargeBuckets test.
     * @param data Test data
     * @return Mocked region
     */
    private Region mockRegionForListingLargeBuckets(
        final ListingLargeBucketsTestData data
    ) {
        final Region region = Mockito.mock(Region.class);
        final AmazonS3 aws = Mockito.mock(AmazonS3.class);
        Mockito.when(region.aws()).thenReturn(aws);
        final ObjectListing firstResponse = Mockito.mock(ObjectListing.class);
        Mockito.when(firstResponse.getNextMarker()).thenReturn(
            data.secondItem()
        );
        final List<S3ObjectSummary> firstSummaries =
            new ArrayList<S3ObjectSummary>(1);
        final S3ObjectSummary firstSummary = new S3ObjectSummary();
        firstSummary.setKey(data.firstItem());
        firstSummaries.add(firstSummary);
        Mockito.when(firstResponse.getObjectSummaries())
            .thenReturn(firstSummaries);
        final ObjectListing secondResponse = Mockito.mock(ObjectListing.class);
        Mockito.when(secondResponse.getNextMarker()).thenReturn(null);
        final List<S3ObjectSummary> secondSummaries =
            new ArrayList<S3ObjectSummary>(1);
        final S3ObjectSummary secondSummary = new S3ObjectSummary();
        secondSummary.setKey(data.secondItem());
        secondSummaries.add(secondSummary);
        Mockito.when(secondResponse.getObjectSummaries())
            .thenReturn(secondSummaries);
        Mockito.when(aws.listObjects(Mockito.any(ListObjectsRequest.class)))
            .thenAnswer(
                //@checkstyle AnonInnerLengthCheck (25 lines)
                new Answer<ObjectListing>() {
                    private int counter;
                    @Override
                    public ObjectListing answer(
                        final InvocationOnMock invocation) {
                        final ListObjectsRequest request =
                            (ListObjectsRequest) invocation.getArguments()[0];
                        Assert.assertEquals(
                            data.bucketName(), request.getBucketName()
                        );
                        Assert.assertEquals(data.prefix(), request.getPrefix());
                        final ObjectListing result;
                        if (counter == 0) {
                            Assert.assertNull(request.getMarker());
                            result = firstResponse;
                        } else {
                            Assert.assertEquals(
                                data.secondItem(), request.getMarker()
                            );
                            result = secondResponse;
                        }
                        counter = counter + 1;
                        return result;
                    }
                }
            );
        return region;
    }

    private static class ListingLargeBucketsTestData {

        /**
         * Returns test bucket name.
         * @return Bucket name
         */
        public final String bucketName() {
            return "large.bucket";
        }

        /**
         * Returns test key prefix.
         * @return Key prefix
         */
        public final String prefix() {
            return "prefix";
        }

        /**
         * Returns test first item.
         * @return First item
         */
        public final String firstItem() {
            return "firstItem";
        }

        /**
         * Returns test secondItem.
         * @return Second item
         */
        public final String secondItem() {
            return "secondItem";
        }

    }

}
