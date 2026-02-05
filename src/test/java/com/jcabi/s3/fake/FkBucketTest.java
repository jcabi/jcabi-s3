/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3.fake;

import com.jcabi.s3.Bucket;
import com.jcabi.s3.Ocket;
import com.jcabi.s3.Region;
import java.io.File;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test case for {@link FkBucket}.
 *
 * @since 0.8.1
 */
final class FkBucketTest {

    @Test
    @SuppressWarnings("unchecked")
    void listsOckets(@TempDir final File temp) throws Exception {
        final Region region = new FkRegion(temp);
        final Bucket bucket = region.bucket("test");
        new Ocket.Text(bucket.ocket("a/first.txt")).write("");
        new Ocket.Text(bucket.ocket("a/b/hello.txt")).write("");
        new Ocket.Text(bucket.ocket("a/b/f/2.txt")).write("");
        new Ocket.Text(bucket.ocket("a/b/c/d/3.txt")).write("");
        MatcherAssert.assertThat(
            "should has all of items",
            new Bucket.Prefixed(bucket, "a/b").list(""),
            Matchers.allOf(
                // @checkstyle MagicNumberCheck (1 line)
                Matchers.<String>iterableWithSize(3),
                Matchers.hasItem("/hello.txt"),
                Matchers.hasItem("/f/2.txt"),
                Matchers.hasItem("/c/d/3.txt")
            )
        );
    }

    @Test
    void listsOcketsWithDifferentPrefixes(@TempDir final File temp)
        throws Exception {
        final Region region = new FkRegion(temp);
        final Bucket bucket = region.bucket("foo");
        new Ocket.Text(bucket.ocket("1/foo.txt")).write("");
        new Ocket.Text(bucket.ocket("1/2/foo.txt")).write("");
        new Ocket.Text(bucket.ocket("1/2/3/foo.txt")).write("");
        MatcherAssert.assertThat(
            "should be in list",
            new Bucket.Prefixed(bucket, "1/").list(""),
            // @checkstyle MagicNumberCheck (1 line)
            Matchers.<String>iterableWithSize(3)
        );
    }

    @Test
    void removesExistingOcket(@TempDir final File temp) throws Exception {
        final String name = UUID.randomUUID().toString();
        final Bucket bucket = new FkRegion(temp).bucket(name);
        final String key = String.format("%s.txt", UUID.randomUUID());
        new Ocket.Text(bucket.ocket(key)).write(
            UUID.randomUUID().toString()
        );
        bucket.remove(key);
        MatcherAssert.assertThat(
            "ocket was not removed",
            bucket.ocket(key).exists(),
            Matchers.is(false)
        );
    }

    @Test
    void reportsThatBucketExists(@TempDir final File temp) {
        MatcherAssert.assertThat(
            "bucket did not report existence",
            new FkBucket(temp, UUID.randomUUID().toString()).exists(),
            Matchers.is(true)
        );
    }

    @Test
    void comparesWithAnotherBucket(@TempDir final File temp) {
        final FkBucket first = new FkBucket(
            temp, String.format("aaa-%s", UUID.randomUUID())
        );
        final FkBucket second = new FkBucket(
            temp, String.format("zzz-%s", UUID.randomUUID())
        );
        MatcherAssert.assertThat(
            "comparison did not return negative for earlier name",
            first.compareTo(second),
            Matchers.lessThan(0)
        );
    }

    @Test
    void returnsRegion(@TempDir final File temp) {
        MatcherAssert.assertThat(
            "region was not returned",
            new FkBucket(temp, UUID.randomUUID().toString()).region(),
            Matchers.notNullValue()
        );
    }

    @Test
    void returnsName(@TempDir final File temp) {
        final String name = UUID.randomUUID().toString();
        MatcherAssert.assertThat(
            "name was not returned correctly",
            new FkBucket(temp, name).name(),
            Matchers.equalTo(name)
        );
    }

    @Test
    void createsOcketByKey(@TempDir final File temp) {
        MatcherAssert.assertThat(
            "ocket was not created",
            new FkBucket(
                temp, UUID.randomUUID().toString()
            ).ocket(UUID.randomUUID().toString()),
            Matchers.notNullValue()
        );
    }

}
