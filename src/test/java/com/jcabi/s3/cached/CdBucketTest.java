/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3.cached;

import com.jcabi.s3.Ocket;
import com.jcabi.s3.fake.FkBucket;
import java.io.File;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test case for {@link CdBucket}.
 *
 * @since 0.8
 */
final class CdBucketTest {

    @Test
    void delegatesNameToOrigin(@TempDir final File temp) {
        final String name = UUID.randomUUID().toString();
        MatcherAssert.assertThat(
            "name was not delegated to origin",
            new CdBucket(new FkBucket(temp, name)).name(),
            Matchers.equalTo(name)
        );
    }

    @Test
    void delegatesRegionToOrigin(@TempDir final File temp) {
        MatcherAssert.assertThat(
            "region was not returned from delegation",
            new CdBucket(
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
            new CdBucket(
                new FkBucket(temp, UUID.randomUUID().toString())
            ).ocket(key).key(),
            Matchers.equalTo(key)
        );
    }

    @Test
    void delegatesExistsToOrigin(@TempDir final File temp) throws Exception {
        MatcherAssert.assertThat(
            "exists was not delegated to origin",
            new CdBucket(
                new FkBucket(temp, UUID.randomUUID().toString())
            ).exists(),
            Matchers.is(true)
        );
    }

    @Test
    void delegatesRemoveToOrigin(@TempDir final File temp) throws Exception {
        final FkBucket origin = new FkBucket(
            temp, UUID.randomUUID().toString()
        );
        final String key = String.format("%s.txt", UUID.randomUUID());
        new Ocket.Text(origin.ocket(key)).write(
            UUID.randomUUID().toString()
        );
        new CdBucket(origin).remove(key);
        MatcherAssert.assertThat(
            "ocket was not removed via delegation",
            origin.ocket(key).exists(),
            Matchers.is(false)
        );
    }

    @Test
    void delegatesListToOrigin(@TempDir final File temp) throws Exception {
        final FkBucket origin = new FkBucket(
            temp, UUID.randomUUID().toString()
        );
        final String key = String.format("%s.txt", UUID.randomUUID());
        new Ocket.Text(origin.ocket(key)).write(
            UUID.randomUUID().toString()
        );
        MatcherAssert.assertThat(
            "list was not delegated to origin",
            new CdBucket(origin).list(""),
            Matchers.hasItem(key)
        );
    }

    @Test
    void comparesWithAnotherBucket(@TempDir final File temp) {
        MatcherAssert.assertThat(
            "comparison did not return negative for earlier name",
            new CdBucket(
                new FkBucket(
                    temp, String.format("aaa-%s", UUID.randomUUID())
                )
            ).compareTo(
                new CdBucket(
                    new FkBucket(
                        temp,
                        String.format("zzz-%s", UUID.randomUUID())
                    )
                )
            ),
            Matchers.lessThan(0)
        );
    }

    @Test
    void delegatesToStringToOrigin(@TempDir final File temp) {
        final FkBucket origin = new FkBucket(
            temp, UUID.randomUUID().toString()
        );
        MatcherAssert.assertThat(
            "toString was not delegated to origin",
            new CdBucket(origin).toString(),
            Matchers.equalTo(origin.toString())
        );
    }

}
