/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3.fake;

import com.jcabi.s3.Bucket;
import com.jcabi.s3.Ocket;
import com.jcabi.s3.Region;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test case for {@link FkBucket}.
 *
 * @since 0.8.1
 */
@SuppressWarnings("PMD.JUnit5TestShouldBePackagePrivate")
public final class FkBucketTest {

    /**
     * Temp directory.
     * @checkstyle VisibilityModifierCheck (5 lines)
     */
    @Rule
    public final transient TemporaryFolder temp = new TemporaryFolder();

    /**
     * MkBucket can list ockets.
     * @throws Exception If fails
     */
    @Test
    @SuppressWarnings("unchecked")
    public void listsOckets() throws Exception {
        final Region region = new FkRegion(this.temp.newFolder());
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

    /**
     * MkBucket can list ockets.
     * @throws Exception If fails
     */
    @Test
    public void listsOcketsWithDifferentPrefixes() throws Exception {
        final Region region = new FkRegion(this.temp.newFolder());
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
