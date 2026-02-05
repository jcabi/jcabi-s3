/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3.retry;

import com.jcabi.s3.Ocket;
import com.jcabi.s3.fake.FkBucket;
import java.io.File;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test case for {@link ReOcket}.
 *
 * @since 0.5
 */
final class ReOcketTest {

    @Test
    void delegatesKeyToOrigin(@TempDir final File temp) {
        final String key = String.format("%s.txt", UUID.randomUUID());
        MatcherAssert.assertThat(
            "key was not delegated to origin",
            new ReOcket(
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
            "bucket name was not delegated to origin",
            new ReOcket(
                bucket.ocket(UUID.randomUUID().toString())
            ).bucket().name(),
            Matchers.equalTo(name)
        );
    }

    @Test
    void delegatesExistsForNonExistingOcket(@TempDir final File temp)
        throws Exception {
        MatcherAssert.assertThat(
            "exists was not delegated for non-existing ocket",
            new ReOcket(
                new FkBucket(
                    temp, UUID.randomUUID().toString()
                ).ocket(UUID.randomUUID().toString())
            ).exists(),
            Matchers.is(false)
        );
    }

    @Test
    void delegatesExistsForExistingOcket(@TempDir final File temp)
        throws Exception {
        final FkBucket bucket = new FkBucket(
            temp, UUID.randomUUID().toString()
        );
        final Ocket ocket = bucket.ocket(
            String.format("%s.dat", UUID.randomUUID())
        );
        new Ocket.Text(ocket).write(UUID.randomUUID().toString());
        MatcherAssert.assertThat(
            "exists was not delegated for existing ocket",
            new ReOcket(bucket.ocket(ocket.key())).exists(),
            Matchers.is(true)
        );
    }

    @Test
    void delegatesReadToOrigin(@TempDir final File temp) throws Exception {
        final String content = String.format("héllo-%s", UUID.randomUUID());
        final FkBucket bucket = new FkBucket(
            temp, UUID.randomUUID().toString()
        );
        final Ocket ocket = bucket.ocket(
            String.format("%s.csv", UUID.randomUUID())
        );
        new Ocket.Text(ocket).write(content);
        MatcherAssert.assertThat(
            "read was not delegated to origin",
            new Ocket.Text(new ReOcket(bucket.ocket(ocket.key()))).read(),
            Matchers.equalTo(content)
        );
    }

    @Test
    void delegatesWriteToOrigin(@TempDir final File temp) throws Exception {
        final String content = String.format("héllo-%s", UUID.randomUUID());
        final FkBucket bucket = new FkBucket(
            temp, UUID.randomUUID().toString()
        );
        final Ocket ocket = bucket.ocket(
            String.format("%s.txt", UUID.randomUUID())
        );
        new Ocket.Text(new ReOcket(ocket)).write(content);
        MatcherAssert.assertThat(
            "write was not delegated to origin",
            new Ocket.Text(bucket.ocket(ocket.key())).read(),
            Matchers.equalTo(content)
        );
    }

    @Test
    void delegatesMetaToOrigin(@TempDir final File temp) throws Exception {
        final FkBucket bucket = new FkBucket(
            temp, UUID.randomUUID().toString()
        );
        final Ocket ocket = bucket.ocket(
            String.format("%s.xml", UUID.randomUUID())
        );
        new Ocket.Text(ocket).write(UUID.randomUUID().toString());
        MatcherAssert.assertThat(
            "meta was not delegated to origin",
            new ReOcket(bucket.ocket(ocket.key())).meta(),
            Matchers.notNullValue()
        );
    }

    @Test
    void comparesWithAnotherOcket(@TempDir final File temp) {
        final FkBucket bucket = new FkBucket(
            temp, UUID.randomUUID().toString()
        );
        MatcherAssert.assertThat(
            "comparison was not delegated correctly",
            new ReOcket(bucket.ocket("aaa.txt")).compareTo(
                bucket.ocket("zzz.txt")
            ),
            Matchers.lessThan(0)
        );
    }

    @Test
    void delegatesToStringToOrigin(@TempDir final File temp) {
        final FkBucket bucket = new FkBucket(
            temp, UUID.randomUUID().toString()
        );
        final Ocket origin = bucket.ocket(
            String.format("%s.txt", UUID.randomUUID())
        );
        MatcherAssert.assertThat(
            "toString was not delegated to origin",
            new ReOcket(origin).toString(),
            Matchers.equalTo(origin.toString())
        );
    }

}
