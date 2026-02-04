/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

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
        final S3Client aws = Mockito.mock(S3Client.class);
        final ResponseInputStream<GetObjectResponse> response =
            new ResponseInputStream<>(
                GetObjectResponse.builder().eTag("test-etag").build(),
                AbortableInputStream.create(
                    new ByteArrayInputStream(
                        content.getBytes(StandardCharsets.UTF_8)
                    )
                )
            );
        Mockito.doReturn(response).when(aws)
            .getObject(Mockito.any(GetObjectRequest.class));
        final Region region = Mockito.mock(Region.class);
        Mockito.doReturn(aws).when(region).aws();
        final Bucket bucket = Mockito.mock(Bucket.class);
        Mockito.doReturn(region).when(bucket).region();
        final Ocket ocket = new AwsOcket(bucket, "test.txt");
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ocket.read(baos);
        MatcherAssert.assertThat(
            "should be equal to content",
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
        final S3Client aws = Mockito.mock(S3Client.class);
        Mockito.doReturn(
            PutObjectResponse.builder().build()
        ).when(aws).putObject(
            Mockito.any(PutObjectRequest.class),
            Mockito.any(RequestBody.class)
        );
        final Region region = Mockito.mock(Region.class);
        Mockito.doReturn(aws).when(region).aws();
        final Bucket bucket = Mockito.mock(Bucket.class);
        Mockito.doReturn(region).when(bucket).region();
        final Ocket ocket = new AwsOcket(bucket, "test-3.txt");
        final String content = "text \u20ac\n\t\rtest";
        ocket.write(
            new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)),
            HeadObjectResponse.builder().build()
        );
        Mockito.verify(aws).putObject(
            Mockito.any(PutObjectRequest.class),
            Mockito.any(RequestBody.class)
        );
    }

    /**
     * AwsOcket can throw if object not found.
     * @throws Exception If fails
     */
    @Test(expected = OcketNotFoundException.class)
    public void throwsWhenObjectNotFound() throws Exception {
        final S3Client aws = Mockito.mock(S3Client.class);
        Mockito.doThrow(
            S3Exception.builder().message("not found").build()
        ).when(aws).headObject(
            Mockito.any(HeadObjectRequest.class)
        );
        final Region region = Mockito.mock(Region.class);
        Mockito.doReturn(aws).when(region).aws();
        final Bucket bucket = Mockito.mock(Bucket.class);
        Mockito.doReturn(region).when(bucket).region();
        final Ocket ocket = new AwsOcket(bucket, "test-99.txt");
        ocket.meta();
    }

}
