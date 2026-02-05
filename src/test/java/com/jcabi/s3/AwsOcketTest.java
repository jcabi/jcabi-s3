/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

/**
 * Test case for {@link AwsOcket}.
 *
 * @since 0.1
 */
final class AwsOcketTest {

    @Test
    void readsContentFromAwsObject() throws Exception {
        final String content = "some text \u20ac\n\t\rtest";
        final S3Client aws = Mockito.mock(S3Client.class);
        Mockito.doReturn(
            new ResponseInputStream<>(
                GetObjectResponse.builder().eTag("test-etag").build(),
                AbortableInputStream.create(
                    new ByteArrayInputStream(
                        content.getBytes(StandardCharsets.UTF_8)
                    )
                )
            )
        ).when(aws).getObject(Mockito.any(GetObjectRequest.class));
        final Region region = Mockito.mock(Region.class);
        Mockito.doReturn(aws).when(region).aws();
        final Bucket bucket = Mockito.mock(Bucket.class);
        Mockito.doReturn(region).when(bucket).region();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new AwsOcket(bucket, "test.txt").read(baos);
        MatcherAssert.assertThat(
            "should be equal to content",
            baos.toString(StandardCharsets.UTF_8.name()),
            Matchers.equalTo(content)
        );
    }

    @Test
    void writesContentToAwsObject() throws Exception {
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
        new AwsOcket(bucket, "test-3.txt").write(
            new ByteArrayInputStream(
                "text \u20ac\n\t\rtest".getBytes(StandardCharsets.UTF_8)
            ),
            HeadObjectResponse.builder().build()
        );
        Mockito.verify(aws).putObject(
            Mockito.any(PutObjectRequest.class),
            Mockito.any(RequestBody.class)
        );
    }

    @Test
    void throwsWhenObjectNotFound() throws Exception {
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
        Assertions.assertThrows(
            OcketNotFoundException.class,
            new AwsOcket(bucket, "test-99.txt")::meta,
            "should throw OcketNotFoundException"
        );
    }

    @Test
    void existsForExistingObject() throws Exception {
        final S3Client aws = Mockito.mock(S3Client.class);
        Mockito.when(
            aws.listObjectsV2(Mockito.any(ListObjectsV2Request.class))
        ).thenReturn(
            ListObjectsV2Response.builder()
                .contents(
                    S3Object.builder()
                        .key(UUID.randomUUID().toString())
                        .build()
                ).build()
        );
        final Region region = Mockito.mock(Region.class);
        Mockito.doReturn(aws).when(region).aws();
        final Bucket bucket = Mockito.mock(Bucket.class);
        Mockito.doReturn(region).when(bucket).region();
        Mockito.doReturn(UUID.randomUUID().toString()).when(bucket).name();
        MatcherAssert.assertThat(
            "existing object was not reported as existing",
            new AwsOcket(bucket, UUID.randomUUID().toString()).exists(),
            Matchers.is(true)
        );
    }

    @Test
    void doesntExistForAbsentObject() throws Exception {
        final S3Client aws = Mockito.mock(S3Client.class);
        Mockito.when(
            aws.listObjectsV2(Mockito.any(ListObjectsV2Request.class))
        ).thenReturn(
            ListObjectsV2Response.builder()
                .contents(Collections.emptyList())
                .build()
        );
        final Region region = Mockito.mock(Region.class);
        Mockito.doReturn(aws).when(region).aws();
        final Bucket bucket = Mockito.mock(Bucket.class);
        Mockito.doReturn(region).when(bucket).region();
        Mockito.doReturn(UUID.randomUUID().toString()).when(bucket).name();
        MatcherAssert.assertThat(
            "non-existing object was reported as existing",
            new AwsOcket(bucket, UUID.randomUUID().toString()).exists(),
            Matchers.is(false)
        );
    }

    @Test
    void throwsOnExistsWhenAwsFails() {
        final S3Client aws = Mockito.mock(S3Client.class);
        Mockito.when(
            aws.listObjectsV2(Mockito.any(ListObjectsV2Request.class))
        ).thenThrow(
            S3Exception.builder().message("access denied").build()
        );
        final Region region = Mockito.mock(Region.class);
        Mockito.doReturn(aws).when(region).aws();
        final Bucket bucket = Mockito.mock(Bucket.class);
        Mockito.doReturn(region).when(bucket).region();
        Mockito.doReturn(UUID.randomUUID().toString()).when(bucket).name();
        Assertions.assertThrows(
            IOException.class,
            () -> new AwsOcket(
                bucket, UUID.randomUUID().toString()
            ).exists(),
            "exists did not throw IOException on S3 failure"
        );
    }

    @Test
    void writesContentWithKnownLength() throws Exception {
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
        Mockito.doReturn(UUID.randomUUID().toString()).when(bucket).name();
        final String content = UUID.randomUUID().toString();
        final byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        new AwsOcket(bucket, UUID.randomUUID().toString()).write(
            new ByteArrayInputStream(bytes),
            HeadObjectResponse.builder()
                .contentType("text/plain")
                .contentLength((long) bytes.length)
                .contentEncoding("UTF-8")
                .build()
        );
        Mockito.verify(aws).putObject(
            Mockito.any(PutObjectRequest.class),
            Mockito.any(RequestBody.class)
        );
    }

    @Test
    void throwsOnWriteWhenAwsFails() {
        final S3Client aws = Mockito.mock(S3Client.class);
        Mockito.doThrow(
            S3Exception.builder().message("write failed").build()
        ).when(aws).putObject(
            Mockito.any(PutObjectRequest.class),
            Mockito.any(RequestBody.class)
        );
        final Region region = Mockito.mock(Region.class);
        Mockito.doReturn(aws).when(region).aws();
        final Bucket bucket = Mockito.mock(Bucket.class);
        Mockito.doReturn(region).when(bucket).region();
        Mockito.doReturn(UUID.randomUUID().toString()).when(bucket).name();
        final Ocket ocket = new AwsOcket(
            bucket, UUID.randomUUID().toString()
        );
        Assertions.assertThrows(
            IOException.class,
            () -> ocket.write(
                new ByteArrayInputStream(new byte[0]),
                HeadObjectResponse.builder().build()
            ),
            "write did not throw IOException on S3 failure"
        );
    }

    @Test
    void throwsOnReadWhenAwsFails() {
        final S3Client aws = Mockito.mock(S3Client.class);
        Mockito.doThrow(
            S3Exception.builder().message("read failed").build()
        ).when(aws).getObject(
            Mockito.any(GetObjectRequest.class)
        );
        final Region region = Mockito.mock(Region.class);
        Mockito.doReturn(aws).when(region).aws();
        final Bucket bucket = Mockito.mock(Bucket.class);
        Mockito.doReturn(region).when(bucket).region();
        Mockito.doReturn(UUID.randomUUID().toString()).when(bucket).name();
        final Ocket ocket = new AwsOcket(
            bucket, UUID.randomUUID().toString()
        );
        Assertions.assertThrows(
            OcketNotFoundException.class,
            () -> ocket.read(new ByteArrayOutputStream()),
            "read did not throw when S3 object is missing"
        );
    }

}
