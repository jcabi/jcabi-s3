/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3.fake;

import com.jcabi.s3.Bucket;
import com.jcabi.s3.Ocket;
import com.jcabi.s3.Region;
import java.io.File;
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

}
