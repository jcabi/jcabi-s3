/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3;

import com.amazonaws.services.s3.AmazonS3;
import java.io.IOException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;

/**
 * Integration case for {@link AwsBucket}.
 *
 * @since 0.3
 */
@SuppressWarnings("PMD.JUnit5TestShouldBePackagePrivate")
public final class AwsBucketITCase {

    /**
     * Bucket we're working with.
     * @checkstyle VisibilityModifier (3 lines)
     */
    @Rule
    public final transient BucketRule rule = new BucketRule();

    /**
     * AwsBucket can list objects in a bucket.
     * @throws Exception If fails
     */
    @Test
    public void listsObjectsInBucket() throws Exception {
        final String name = "a/b/test.txt";
        final Bucket bucket = this.rule.bucket();
        new Ocket.Text(bucket.ocket(name)).write("test");
        try {
            MatcherAssert.assertThat(
                "should be in list",
                bucket.list(""),
                Matchers.allOf(
                    Matchers.<String>iterableWithSize(1),
                    Matchers.hasItem(name)
                )
            );
            MatcherAssert.assertThat(
                "should be in list",
                bucket.list("a/"),
                Matchers.allOf(
                    Matchers.<String>iterableWithSize(1),
                    Matchers.hasItem(name)
                )
            );
            MatcherAssert.assertThat(
                "should be empty list",
                bucket.list("alpha"),
                Matchers.emptyIterable()
            );
        } finally {
            bucket.remove(name);
        }
    }

    /**
     * AwsBucket can list objects in a prefixed bucket.
     * @throws Exception If fails
     */
    @Test
    public void listsObjectsInPrefixedBucket() throws Exception {
        final String name = "foo/bar/file.txt";
        final Bucket bucket = this.rule.bucket();
        new Ocket.Text(bucket.ocket(name)).write("hey");
        final Bucket bkt = new Bucket.Prefixed(bucket, "foo/");
        try {
            final String item = "bar/file.txt";
            MatcherAssert.assertThat(
                "should be exists in list",
                bkt.list(""),
                Matchers.allOf(
                    Matchers.<String>iterableWithSize(1),
                    Matchers.hasItem(item)
                )
            );
            MatcherAssert.assertThat(
                "should be exists in list",
                bkt.list("bar/"),
                Matchers.allOf(
                    Matchers.<String>iterableWithSize(1),
                    Matchers.hasItem(item)
                )
            );
            MatcherAssert.assertThat(
                "should be empty list",
                bkt.list("foo"),
                Matchers.emptyIterable()
            );
        } finally {
            bucket.remove(name);
        }
    }

    /**
     * AwsBucket can list objects in a prefixed bucket, without collisions.
     * @throws Exception If fails
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void listsInPrefixedBucketWithoutCollisions() throws Exception {
        final Bucket bucket = this.rule.bucket();
        final String[] names = {"alpha/", "alpha/beta.xml"};
        for (final String name : names) {
            new Ocket.Text(bucket.ocket(name)).write("");
        }
        final Bucket bkt = new Bucket.Prefixed(bucket, names[0]);
        try {
            MatcherAssert.assertThat(
                "should has item in list",
                bkt.list(""),
                Matchers.allOf(
                    Matchers.<String>iterableWithSize(1),
                    Matchers.hasItem("beta.xml")
                )
            );
        } finally {
            for (final String name : names) {
                bucket.remove(name);
            }
        }
    }

    /**
     * AwsBucket can correctly check the existence of the existing bucket.
     * @throws IOException If fails
     */
    @Test
    public void existsExistingBucket() throws IOException {
        final Bucket bucket = this.rule.bucket();
        MatcherAssert.assertThat(
            "should be true",
            bucket.exists(),
            Matchers.is(true)
        );
    }

    /**
     * AwsBucket can correctly check the existence of the non-existing bucket.
     * @throws IOException If fails
     */
    @Test
    public void existsNonExistingBucket() throws IOException {
        final Bucket bucket = this.rule.bucket();
        final AmazonS3 aws = bucket.region().aws();
        aws.deleteBucket(bucket.name());
        MatcherAssert.assertThat(
            "should be false",
            bucket.exists(),
            Matchers.is(false)
        );
    }

}
