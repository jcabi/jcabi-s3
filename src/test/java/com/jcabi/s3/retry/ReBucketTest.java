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
 * Test case for {@link ReBucket}.
 *
 * @since 0.5
 */
@SuppressWarnings("PMD.TooManyMethods")
final class ReBucketTest {

    @Test
    void delegatesNameToOrigin(@TempDir final File temp) {
        final String name = UUID.randomUUID().toString();
        MatcherAssert.assertThat(
            "name was not delegated to origin",
            new ReBucket(new FkBucket(temp, name)).name(),
            Matchers.equalTo(name)
        );
    }

    @Test
    void delegatesRegionToOrigin(@TempDir final File temp) {
        MatcherAssert.assertThat(
            "region was not returned from delegation",
            new ReBucket(
                new FkBucket(temp, UUID.randomUUID().toString())
            ).region(),
            Matchers.notNullValue()
        );
    }

    @Test
    void delegatesOcketCreation(@TempDir final File temp) {
        final String key = String.format("%s.txt", UUID.randomUUID());
        MatcherAssert.assertThat(
            "ocket key was not delegated to origin",
            new ReBucket(
                new FkBucket(temp, UUID.randomUUID().toString())
            ).ocket(key).key(),
            Matchers.equalTo(key)
        );
    }

    @Test
    void delegatesExistsToOrigin(@TempDir final File temp) throws Exception {
        MatcherAssert.assertThat(
            "exists was not delegated to origin",
            new ReBucket(
                new FkBucket(temp, UUID.randomUUID().toString())
            ).exists(),
            Matchers.is(true)
        );
    }

    @Test
    void delegatesRemoveToOrigin(@TempDir final File temp) throws Exception {
        final String name = UUID.randomUUID().toString();
        final FkBucket origin = new FkBucket(temp, name);
        final String key = String.format("%s.txt", UUID.randomUUID());
        new Ocket.Text(origin.ocket(key)).write(
            UUID.randomUUID().toString()
        );
        new ReBucket(origin).remove(key);
        MatcherAssert.assertThat(
            "ocket was not removed via delegation",
            origin.ocket(key).exists(),
            Matchers.is(false)
        );
    }

    @Test
    void delegatesListToOrigin(@TempDir final File temp) throws Exception {
        final String name = UUID.randomUUID().toString();
        final FkBucket origin = new FkBucket(temp, name);
        final String key = String.format("%s.txt", UUID.randomUUID());
        new Ocket.Text(origin.ocket(key)).write(
            UUID.randomUUID().toString()
        );
        MatcherAssert.assertThat(
            "list was not delegated to origin",
            new ReBucket(origin).list(""),
            Matchers.hasItem(key)
        );
    }

    @Test
    void comparesWithAnotherBucket(@TempDir final File temp) {
        final ReBucket first = new ReBucket(
            new FkBucket(temp, String.format("aaa-%s", UUID.randomUUID()))
        );
        final ReBucket second = new ReBucket(
            new FkBucket(temp, String.format("zzz-%s", UUID.randomUUID()))
        );
        MatcherAssert.assertThat(
            "comparison did not return negative for earlier name",
            first.compareTo(second),
            Matchers.lessThan(0)
        );
    }

    @Test
    void delegatesToStringToOrigin(@TempDir final File temp) {
        final String name = UUID.randomUUID().toString();
        final FkBucket origin = new FkBucket(temp, name);
        MatcherAssert.assertThat(
            "toString was not delegated to origin",
            new ReBucket(origin).toString(),
            Matchers.equalTo(origin.toString())
        );
    }

}
