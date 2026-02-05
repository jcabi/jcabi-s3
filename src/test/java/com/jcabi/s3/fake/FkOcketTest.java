/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3.fake;

import com.jcabi.s3.Bucket;
import com.jcabi.s3.Ocket;
import java.io.File;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

/**
 * Test case for {@link FkOcket}.
 *
 * @since 0.10.1
 */
final class FkOcketTest {

    @Test
    void readsContentTypeAndLengthFromMetadata(@TempDir final File temp)
        throws Exception {
        final Bucket bucket = new FkRegion(temp).bucket("test");
        final Ocket write = bucket.ocket("hello.txt");
        final String text = "hello, world!";
        new Ocket.Text(write).write(text);
        final HeadObjectResponse metadata = new Ocket.Text(
            bucket.ocket(write.key())
        ).meta();
        MatcherAssert.assertThat(
            "should be text/plain content-type",
            metadata.contentType(),
            Matchers.is("text/plain")
        );
        MatcherAssert.assertThat(
            "should be equal to content length",
            metadata.contentLength(),
            Matchers.equalTo((long) text.length())
        );
    }

    @Test
    void readsDateFromMetadata(@TempDir final File temp) throws Exception {
        final Bucket bucket = new FkRegion(temp).bucket("test");
        final Ocket write = bucket.ocket("hello.txt");
        final HeadObjectResponse metadata = new Ocket.Text(
            bucket.ocket(write.key())
        ).meta();
        MatcherAssert.assertThat(
            "should be not null",
            metadata.lastModified(),
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
            String.format("%s.txt", UUID.randomUUID())
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
            String.format("%s.txt", UUID.randomUUID())
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
        final Ocket first = bucket.ocket(String.format("aaa-%s", UUID.randomUUID()));
        final Ocket second = bucket.ocket(String.format("zzz-%s", UUID.randomUUID()));
        MatcherAssert.assertThat(
            "comparison did not return negative for earlier key",
            first.compareTo(second),
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

}
