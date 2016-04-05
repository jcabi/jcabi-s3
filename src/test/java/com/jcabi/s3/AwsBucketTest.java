/**
 * Copyright (c) 2012-2015, jcabi.com
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
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

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
        final Region region = Mockito.mock(Region.class);
        final AmazonS3 aws = Mockito.mock(AmazonS3.class);
        Mockito.when(region.aws()).thenReturn(aws);
        final String name = "existing.bucket.com";
        Mockito.when(aws.doesBucketExist(name)).thenReturn(true);
        final Bucket bucket = new AwsBucket(region, name);
        Assert.assertTrue(bucket.exists());
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
        Mockito.when(aws.doesBucketExist(name)).thenReturn(false);
        final Bucket bucket = new AwsBucket(region, name);
        Assert.assertFalse(bucket.exists());
    }

    /**
     * AwsBucket can throw a proper exception.
     * @throws IOException If succeeds
     */
    @Test(expected = IOException.class)
    public void existsThrowsIOException() throws IOException {
        final Region region = Mockito.mock(Region.class);
        final AmazonS3 aws = Mockito.mock(AmazonS3.class);
        Mockito.when(region.aws()).thenReturn(aws);
        final String name = "throwing.bucket.com";
        Mockito.when(aws.doesBucketExist(name)).thenThrow(
            new AmazonServiceException("Test exception")
        );
        final Bucket bucket = new AwsBucket(region, name);
        bucket.exists();
    }

    /**
     * AwsBucket supports listing large buckets.
     * @throws Exception if test fails
     */
    @Test
    public void supportsListingLargeBuckets() throws Exception {
        final String name = "large.bucket";
        final String prefix = "prefix";
        final String first = "first";
        final String second = "second";
        final Region region = Mockito.mock(Region.class);
        final RegionExpectations expectations = new RegionExpectations(region);
        expectations
            .expectResponse(first, null, second)
            .expectResponse(second, second, null)
            .apply(name, prefix);
        final Bucket bucket = new AwsBucket(region, name);
        final Iterator<String> actual = bucket.list(prefix).iterator();
        MatcherAssert.assertThat(actual.hasNext(), Matchers.equalTo(true));
        MatcherAssert.assertThat(first, Matchers.equalTo(actual.next()));
        MatcherAssert.assertThat(actual.hasNext(), Matchers.equalTo(true));
        MatcherAssert.assertThat(second, Matchers.equalTo(actual.next()));
        MatcherAssert.assertThat(actual.hasNext(), Matchers.equalTo(false));
        expectations.verify(name, prefix);
    }

    /**
     * Expectations.
     */
    private static class RegionExpectations {
        /**
         * Mocked s3 service.
         */
        private final transient AmazonS3 aws;
        /**
         * Responses.
         */
        private final transient List<ObjectListing> responses;
        /**
         * Expected markers.
         */
        private final transient List<String> markers;
        /**
         * Constructs region expectations.
         * @param region Mocked region
         */
        RegionExpectations(final Region region) {
            super();
            this.aws = AwsBucketTest.RegionExpectations.mockAws(region);
            this.responses = new ArrayList<ObjectListing>(0);
            this.markers = new ArrayList<String>(0);
        }
        /**
         * Expect request with start marker and provide in response single item,
         * notify that marker is a next marker to request.
         * @param item Item to respond with
         * @param start Start marker to expect
         * @param marker Next marker
         * @return This instance
         */
        public RegionExpectations expectResponse(final String item,
            final String start, final String marker) {
            final ObjectListing response = new ObjectListing();
            response.setNextMarker(marker);
            final S3ObjectSummary summary = new S3ObjectSummary();
            summary.setKey(item);
            response.getObjectSummaries().add(summary);
            this.responses.add(response);
            this.markers.add(start);
            return this;
        }
        /**
         * Apply expectations.
         * @param bucket Bucket name
         * @param prefix Request prefix
         */
        public void apply(final String bucket, final String prefix) {
            for (int idx = 0; idx < this.markers.size(); ++idx) {
                Mockito.when(
                    this.aws.listObjects(
                        Mockito.argThat(
                            ListObjectsRequestArgumentMatcher.instance(
                                bucket, prefix, this.markers.get(idx)
                            )
                        )
                    )
                ).thenReturn(this.responses.get(idx));
            }
        }
        /**
         * Verify expected invocations.
         * @param bucket Bucket name
         * @param prefix Request prefix
         * @checkstyle JavadocLocationCheck (25 lines)
         */
        private void verify(final String bucket, final String prefix) {
            for (int idx = 0; idx < this.markers.size(); ++idx) {
                Mockito.verify(this.aws).listObjects(
                    Mockito.argThat(
                        ListObjectsRequestArgumentMatcher.instance(
                            bucket, prefix, this.markers.get(idx)
                        )
                    )
                );
            }
        }
        /**
         * Make AWS.
         * @param region Region
         * @return AWS
         */
        private static AmazonS3 mockAws(final Region region) {
            final AmazonS3 aws = Mockito.mock(AmazonS3.class);
            Mockito.when(region.aws()).thenReturn(aws);
            return aws;
        }
    }

    /**
     * Matcher.
     */
    private static class ListObjectsRequestArgumentMatcher implements
        ArgumentMatcher<ListObjectsRequest> {
        /**
         * Bucket name.
         */
        private final transient String bucket;
        /**
         * Prefix.
         */
        private final transient String prefix;
        /**
         * Marker.
         */
        private final transient String marker;
        /**
         * Constructs argument matcher.
         * @param bkt Expected bucket
         * @param pfx Expected prefix
         * @param mrk Expected marker
         */
        ListObjectsRequestArgumentMatcher(final String bkt,
            final String pfx, final String mrk) {
            super();
            this.bucket = bkt;
            this.prefix = pfx;
            this.marker = mrk;
        }
        @Override
        public boolean matches(final Object arg) {
            boolean result = false;
            if (arg instanceof ListObjectsRequest) {
                final ListObjectsRequest obj = (ListObjectsRequest) arg;
                result = true;
                if (!this.bucket.equals(obj.getBucketName())) {
                    result = false;
                }
                if (!this.prefix.equals(obj.getPrefix())) {
                    result = false;
                }
                if (this.marker == null && obj.getMarker() != null) {
                    result = false;
                }
                if (this.marker != null
                    && !this.marker.equals(obj.getMarker())) {
                    result = false;
                }
            }
            return result;
        }
        /**
         * Constructs argument matcher instance.
         * @param bkt Expected bucket
         * @param pfx Expected prefix
         * @param mrk Expected marker
         * @return Matcher instance
         */
        public static ListObjectsRequestArgumentMatcher instance(
            final String bkt, final String pfx, final String mrk) {
            return new ListObjectsRequestArgumentMatcher(bkt, pfx, mrk);
        }
    }
}
