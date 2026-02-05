/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3.fake;

import com.jcabi.s3.Bucket;
import com.jcabi.s3.Ocket;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

/**
 * Test case for {@link FkOcket}.
 *
 * @since 0.10.1
 */
@SuppressWarnings("PMD.TooManyMethods")
final class FkOcketTest {

    @Test
    void readsContentTypeFromMetadata(@TempDir final File temp)
        throws Exception {
        final Bucket bucket = new FkRegion(temp).bucket(
            UUID.randomUUID().toString()
        );
        final Ocket ocket = bucket.ocket(
            String.format("%s.txt", UUID.randomUUID())
        );
        new Ocket.Text(ocket).write(UUID.randomUUID().toString());
        MatcherAssert.assertThat(
            "should be text/plain content-type",
            bucket.ocket(ocket.key()).meta().contentType(),
            Matchers.is("text/plain")
        );
    }

    @Test
    void readsContentLengthFromMetadata(@TempDir final File temp)
        throws Exception {
        final Bucket bucket = new FkRegion(temp).bucket(
            UUID.randomUUID().toString()
        );
        final Ocket ocket = bucket.ocket(
            String.format("%s.dat", UUID.randomUUID())
        );
        new Ocket.Text(ocket).write(UUID.randomUUID().toString());
        MatcherAssert.assertThat(
            "should be positive content length",
            bucket.ocket(ocket.key()).meta().contentLength(),
            Matchers.greaterThan(0L)
        );
    }

    @Test
    void readsDateFromMetadata(@TempDir final File temp) throws Exception {
        final Bucket bucket = new FkRegion(temp).bucket(
            UUID.randomUUID().toString()
        );
        final Ocket ocket = bucket.ocket(
            String.format("%s.log", UUID.randomUUID())
        );
        MatcherAssert.assertThat(
            "should be not null",
            bucket.ocket(ocket.key()).meta().lastModified(),
            Matchers.notNullValue()
        );
    }

    @Test
    void checksNonExistingOcket(@TempDir final File temp) throws Exception {
        final Bucket bucket = new FkRegion(temp).bucket(
            UUID.randomUUID().toString()
        );
        MatcherAssert.assertThat(
            "non-existing ocket was reported as existing",
            bucket.ocket(UUID.randomUUID().toString()).exists(),
            Matchers.is(false)
        );
    }

    @Test
    void checksExistingOcket(@TempDir final File temp) throws Exception {
        final Bucket bucket = new FkRegion(temp).bucket(
            UUID.randomUUID().toString()
        );
        final Ocket ocket = bucket.ocket(
            String.format("%s.xml", UUID.randomUUID())
        );
        new Ocket.Text(ocket).write(UUID.randomUUID().toString());
        MatcherAssert.assertThat(
            "existing ocket was not reported as existing",
            bucket.ocket(ocket.key()).exists(),
            Matchers.is(true)
        );
    }

    @Test
    void readsWrittenContent(@TempDir final File temp) throws Exception {
        final Bucket bucket = new FkRegion(temp).bucket(
            UUID.randomUUID().toString()
        );
        final Ocket ocket = bucket.ocket(
            String.format("%s.csv", UUID.randomUUID())
        );
        final String content =
            String.format("%s \u00e9\u00e8\u00ea", UUID.randomUUID());
        new Ocket.Text(ocket).write(content);
        MatcherAssert.assertThat(
            "written content was not read back correctly",
            new Ocket.Text(bucket.ocket(ocket.key())).read(),
            Matchers.equalTo(content)
        );
    }

    @Test
    void comparesWithAnotherOcket(@TempDir final File temp) {
        final Bucket bucket = new FkRegion(temp).bucket(
            UUID.randomUUID().toString()
        );
        MatcherAssert.assertThat(
            "comparison did not return negative for earlier key",
            bucket.ocket(
                String.format("aaa-%s", UUID.randomUUID())
            ).compareTo(
                bucket.ocket(
                    String.format("zzz-%s", UUID.randomUUID())
                )
            ),
            Matchers.lessThan(0)
        );
    }

    @Test
    void returnsBucket(@TempDir final File temp) {
        final String name = UUID.randomUUID().toString();
        final Bucket bucket = new FkRegion(temp).bucket(name);
        MatcherAssert.assertThat(
            "bucket was not returned correctly",
            bucket.ocket(UUID.randomUUID().toString()).bucket().name(),
            Matchers.equalTo(name)
        );
    }

    @Test
    void returnsKey(@TempDir final File temp) {
        final String key = String.format("%s.txt", UUID.randomUUID());
        final Bucket bucket = new FkRegion(temp).bucket(
            UUID.randomUUID().toString()
        );
        MatcherAssert.assertThat(
            "key was not returned correctly",
            bucket.ocket(key).key(),
            Matchers.equalTo(key)
        );
    }

    @Test
    void representsItselfAsString(@TempDir final File temp) {
        final String key = UUID.randomUUID().toString();
        MatcherAssert.assertThat(
            "string representation did not match key",
            new FkRegion(temp).bucket(
                UUID.randomUUID().toString()
            ).ocket(key).toString(),
            Matchers.equalTo(key)
        );
    }

    @Test
    void readsEncodingFromMetadata(@TempDir final File temp) throws Exception {
        final Bucket bucket = new FkRegion(temp).bucket(
            UUID.randomUUID().toString()
        );
        final Ocket ocket = bucket.ocket(
            String.format("%s.htm", UUID.randomUUID())
        );
        new Ocket.Text(ocket).write(UUID.randomUUID().toString());
        MatcherAssert.assertThat(
            "encoding was not UTF-8",
            bucket.ocket(ocket.key()).meta().contentEncoding(),
            Matchers.equalTo("UTF-8")
        );
    }

    @Test
    void readsAndWritesBinaryContent(@TempDir final File temp)
        throws Exception {
        final Bucket bucket = new FkRegion(temp).bucket(
            UUID.randomUUID().toString()
        );
        final Ocket ocket = bucket.ocket(
            String.format("%s.bin", UUID.randomUUID())
        );
        final byte[] bytes = UUID.randomUUID().toString()
            .getBytes(StandardCharsets.UTF_8);
        ocket.write(
            new ByteArrayInputStream(bytes),
            HeadObjectResponse.builder().build()
        );
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        bucket.ocket(ocket.key()).read(output);
        MatcherAssert.assertThat(
            "binary content was not preserved after write and read",
            output.toByteArray(),
            Matchers.equalTo(bytes)
        );
    }

    @Test
    void throwsOnReadingNonExistingOcket(@TempDir final File temp) {
        final Ocket ocket = new FkRegion(temp).bucket(
            UUID.randomUUID().toString()
        ).ocket(UUID.randomUUID().toString());
        Assertions.assertThrows(
            IOException.class,
            () -> ocket.read(new ByteArrayOutputStream()),
            "reading non-existing ocket did not throw"
        );
    }

    @Test
    void writesToNestedPath(@TempDir final File temp) throws Exception {
        final Bucket bucket = new FkRegion(temp).bucket(
            UUID.randomUUID().toString()
        );
        final Ocket ocket = bucket.ocket(
            String.format("a/b/%s.txt", UUID.randomUUID())
        );
        final String content = UUID.randomUUID().toString();
        new Ocket.Text(ocket).write(content);
        MatcherAssert.assertThat(
            "content in nested path was not read correctly",
            new Ocket.Text(bucket.ocket(ocket.key())).read(),
            Matchers.equalTo(content)
        );
    }

}
