/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3;

import com.jcabi.s3.fake.FkBucket;
import java.io.File;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test case for {@link Ocket.Text}.
 *
 * @since 0.1
 */
final class OcketTextTest {

    @Test
    void readsWrittenTextContent(@TempDir final File temp) throws Exception {
        final String content = String.format("%s h\u00e9llo \u20ac", UUID.randomUUID());
        final FkBucket bucket = new FkBucket(
            temp, UUID.randomUUID().toString()
        );
        final Ocket ocket = bucket.ocket(
            String.format("%s.txt", UUID.randomUUID())
        );
        new Ocket.Text(ocket).write(content);
        MatcherAssert.assertThat(
            "text content was not read correctly",
            new Ocket.Text(bucket.ocket(ocket.key())).read(),
            Matchers.equalTo(content)
        );
    }

    @Test
    void writesTextWithSpecifiedContentType(@TempDir final File temp)
        throws Exception {
        final String content = UUID.randomUUID().toString();
        final FkBucket bucket = new FkBucket(
            temp, UUID.randomUUID().toString()
        );
        final Ocket ocket = bucket.ocket(
            String.format("%s.html", UUID.randomUUID())
        );
        new Ocket.Text(ocket).write(content, "text/html");
        MatcherAssert.assertThat(
            "text content with custom type was not read correctly",
            new Ocket.Text(bucket.ocket(ocket.key())).read(),
            Matchers.equalTo(content)
        );
    }

    @Test
    void delegatesKeyToOrigin(@TempDir final File temp) {
        final String key = String.format("%s.txt", UUID.randomUUID());
        MatcherAssert.assertThat(
            "key was not delegated to origin",
            new Ocket.Text(
                new FkBucket(
                    temp, UUID.randomUUID().toString()
                ).ocket(key)
            ).key(),
            Matchers.equalTo(key)
        );
    }

    @Test
    void delegatesBucketToOrigin(@TempDir final File temp) {
        final String name = UUID.randomUUID().toString();
        final FkBucket bucket = new FkBucket(temp, name);
        MatcherAssert.assertThat(
            "bucket was not delegated to origin",
            new Ocket.Text(
                bucket.ocket(UUID.randomUUID().toString())
            ).bucket().name(),
            Matchers.equalTo(name)
        );
    }

    @Test
    void delegatesExistsToOrigin(@TempDir final File temp) throws Exception {
        MatcherAssert.assertThat(
            "exists was not delegated correctly for non-existing ocket",
            new Ocket.Text(
                new FkBucket(
                    temp, UUID.randomUUID().toString()
                ).ocket(UUID.randomUUID().toString())
            ).exists(),
            Matchers.is(false)
        );
    }

    @Test
    void delegatesMetaToOrigin(@TempDir final File temp) throws Exception {
        final FkBucket bucket = new FkBucket(
            temp, UUID.randomUUID().toString()
        );
        final Ocket ocket = bucket.ocket(
            String.format("%s.txt", UUID.randomUUID())
        );
        new Ocket.Text(ocket).write(UUID.randomUUID().toString());
        MatcherAssert.assertThat(
            "meta was not delegated to origin",
            new Ocket.Text(bucket.ocket(ocket.key())).meta(),
            Matchers.notNullValue()
        );
    }

    @Test
    void comparesOcketsByDelegation(@TempDir final File temp) {
        final FkBucket bucket = new FkBucket(
            temp, UUID.randomUUID().toString()
        );
        MatcherAssert.assertThat(
            "comparison was not delegated correctly",
            new Ocket.Text(bucket.ocket("aaa.txt")).compareTo(
                bucket.ocket("zzz.txt")
            ),
            Matchers.lessThan(0)
        );
    }

}
