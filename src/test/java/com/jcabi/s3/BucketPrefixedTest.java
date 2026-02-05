/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3;

import com.jcabi.s3.fake.FkRegion;
import java.io.File;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test case for {@link Bucket.Prefixed}.
 *
 * @since 0.1
 */
final class BucketPrefixedTest {

    @Test
    void delegatesExistsToOrigin(@TempDir final File temp) throws Exception {
        final Bucket bucket = new FkRegion(temp).bucket(
            UUID.randomUUID().toString()
        );
        MatcherAssert.assertThat(
            "exists was not delegated",
            new Bucket.Prefixed(bucket, "pfx/").exists(),
            Matchers.is(true)
        );
    }

    @Test
    void delegatesRegionToOrigin(@TempDir final File temp) {
        final Bucket bucket = new FkRegion(temp).bucket(
            UUID.randomUUID().toString()
        );
        MatcherAssert.assertThat(
            "region was not delegated",
            new Bucket.Prefixed(bucket, "pfx/").region(),
            Matchers.notNullValue()
        );
    }

    @Test
    void delegatesNameToOrigin(@TempDir final File temp) {
        final String name = UUID.randomUUID().toString();
        final Bucket bucket = new FkRegion(temp).bucket(name);
        MatcherAssert.assertThat(
            "name was not delegated",
            new Bucket.Prefixed(bucket, "pfx/").name(),
            Matchers.equalTo(name)
        );
    }

    @Test
    void removesOcketWithPrefix(@TempDir final File temp) throws Exception {
        final Bucket bucket = new FkRegion(temp).bucket(
            UUID.randomUUID().toString()
        );
        final String key = String.format("data/%s.txt", UUID.randomUUID());
        new Ocket.Text(bucket.ocket(key)).write(
            UUID.randomUUID().toString()
        );
        new Bucket.Prefixed(bucket, "data/").remove(
            key.substring("data/".length())
        );
        MatcherAssert.assertThat(
            "ocket was not removed with prefix",
            bucket.ocket(key).exists(),
            Matchers.is(false)
        );
    }

    @Test
    void createsOcketWithPrefix(@TempDir final File temp) throws Exception {
        final Bucket bucket = new FkRegion(temp).bucket(
            UUID.randomUUID().toString()
        );
        final String content = UUID.randomUUID().toString();
        new Ocket.Text(
            new Bucket.Prefixed(bucket, "dir/").ocket("file.txt")
        ).write(content);
        MatcherAssert.assertThat(
            "ocket was not created with prefix",
            new Ocket.Text(bucket.ocket("dir/file.txt")).read(),
            Matchers.equalTo(content)
        );
    }

    @Test
    void filtersEmptyNamesFromList(@TempDir final File temp) throws Exception {
        final Bucket bucket = new FkRegion(temp).bucket(
            UUID.randomUUID().toString()
        );
        new Ocket.Text(bucket.ocket("x/one.txt")).write(
            UUID.randomUUID().toString()
        );
        MatcherAssert.assertThat(
            "empty names were not filtered from list",
            new Bucket.Prefixed(bucket, "x/").list(""),
            Matchers.not(Matchers.hasItem(""))
        );
    }

}
