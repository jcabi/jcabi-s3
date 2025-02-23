/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link AwsOcket}.
 *
 * @since 0.1
 */
@SuppressWarnings("PMD.JUnit5TestShouldBePackagePrivate")
public final class AwsOcketTest {

    /**
     * AwsOcket can read ocket content.
     * @throws Exception If fails
     */
    @Test
    public void readsContentFromAwsObject() throws Exception {
        final String content = "some text \u20ac\n\t\rtest";
        final S3Object object = new S3Object();
        object.setObjectContent(
            IOUtils.toInputStream(content, StandardCharsets.UTF_8)
        );
        final AmazonS3 aws = Mockito.mock(AmazonS3.class);
        Mockito.doReturn(object).when(aws)
            .getObject(Mockito.any(GetObjectRequest.class));
        final Region region = Mockito.mock(Region.class);
        Mockito.doReturn(aws).when(region).aws();
        final Bucket bucket = Mockito.mock(Bucket.class);
        Mockito.doReturn(region).when(bucket).region();
        final Ocket ocket = new AwsOcket(bucket, "test.txt");
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ocket.read(baos);
        MatcherAssert.assertThat(
            baos.toString(StandardCharsets.UTF_8.name()),
            Matchers.equalTo(content)
        );
    }

    /**
     * AwsOcket can write ocket content.
     * @throws Exception If fails
     */
    @Test
    public void writesContentToAwsObject() throws Exception {
        final AmazonS3 aws = Mockito.mock(AmazonS3.class);
        Mockito.doReturn(new PutObjectResult()).when(aws).putObject(
            Mockito.any(PutObjectRequest.class)
        );
        final Region region = Mockito.mock(Region.class);
        Mockito.doReturn(aws).when(region).aws();
        final Bucket bucket = Mockito.mock(Bucket.class);
        Mockito.doReturn(region).when(bucket).region();
        final Ocket ocket = new AwsOcket(bucket, "test-3.txt");
        final String content = "text \u20ac\n\t\rtest";
        ocket.write(
            new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)),
            new ObjectMetadata()
        );
        Mockito.verify(aws).putObject(Mockito.any(PutObjectRequest.class));
    }

    /**
     * AwsOcket can throw if object not found.
     * @throws Exception If fails
     */
    @Test(expected = OcketNotFoundException.class)
    public void throwsWhenObjectNotFound() throws Exception {
        final AmazonS3 aws = Mockito.mock(AmazonS3.class);
        Mockito.doThrow(new AmazonS3Exception("")).when(aws).getObjectMetadata(
            Mockito.any(GetObjectMetadataRequest.class)
        );
        final Region region = Mockito.mock(Region.class);
        Mockito.doReturn(aws).when(region).aws();
        final Bucket bucket = Mockito.mock(Bucket.class);
        Mockito.doReturn(region).when(bucket).region();
        final Ocket ocket = new AwsOcket(bucket, "test-99.txt");
        ocket.meta();
    }

}
