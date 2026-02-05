/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3;

import java.io.IOException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;

/**
 * Integration case for {@link AwsBucket}.
 *
 * @since 0.3
 */
final class AwsBucketITCase {

    /**
     * Bucket we're working with.
     * @checkstyle VisibilityModifier (3 lines)
     */
    @RegisterExtension
    final transient BucketRule rule = new BucketRule();

    /**
     * AwsBucket can list objects with empty prefix.
     * @throws Exception If fails
     */
    @Test
    void listsObjectsWithEmptyPrefix() throws Exception {
        final String name = "a/b/test.txt";
        final Bucket bucket = this.rule.bucket();
        new Ocket.Text(bucket.ocket(name)).write("test");
        try {
            MatcherAssert.assertThat(
                "should be in list",
                bucket.list(""),
                Matchers.hasItem(name)
            );
        } finally {
            bucket.remove(name);
        }
    }

    /**
     * AwsBucket can list objects with a matching prefix.
     * @throws Exception If fails
     */
    @Test
    void listsObjectsWithMatchingPrefix() throws Exception {
        final String name = "a/b/test2.txt";
        final Bucket bucket = this.rule.bucket();
        new Ocket.Text(bucket.ocket(name)).write("test");
        try {
            MatcherAssert.assertThat(
                "should be in list",
                bucket.list("a/"),
                Matchers.hasItem(name)
            );
        } finally {
            bucket.remove(name);
        }
    }

    /**
     * AwsBucket returns empty list for non-matching prefix.
     * @throws Exception If fails
     */
    @Test
    void listsObjectsWithNonMatchingPrefix() throws Exception {
        final String name = "a/b/test3.txt";
        final Bucket bucket = this.rule.bucket();
        new Ocket.Text(bucket.ocket(name)).write("test");
        try {
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
     * AwsBucket can list objects in a prefixed bucket with empty prefix.
     * @throws Exception If fails
     */
    @Test
    void listsObjectsInPrefixedBucketWithEmptyPrefix() throws Exception {
        final String name = "foo/bar/file.txt";
        final Bucket bucket = this.rule.bucket();
        new Ocket.Text(bucket.ocket(name)).write("hey");
        try {
            MatcherAssert.assertThat(
                "should be in list",
                new Bucket.Prefixed(bucket, "foo/").list(""),
                Matchers.hasItem("bar/file.txt")
            );
        } finally {
            bucket.remove(name);
        }
    }

    /**
     * AwsBucket can list objects in a prefixed bucket with sub-prefix.
     * @throws Exception If fails
     */
    @Test
    void listsObjectsInPrefixedBucketWithSubPrefix() throws Exception {
        final String name = "foo/bar/file2.txt";
        final Bucket bucket = this.rule.bucket();
        new Ocket.Text(bucket.ocket(name)).write("hey");
        try {
            MatcherAssert.assertThat(
                "should be in list",
                new Bucket.Prefixed(bucket, "foo/").list("bar/"),
                Matchers.hasItem("bar/file2.txt")
            );
        } finally {
            bucket.remove(name);
        }
    }

    /**
     * AwsBucket returns empty list in prefixed bucket for wrong prefix.
     * @throws Exception If fails
     */
    @Test
    void listsEmptyInPrefixedBucketForWrongPrefix() throws Exception {
        final String name = "foo/bar/file3.txt";
        final Bucket bucket = this.rule.bucket();
        new Ocket.Text(bucket.ocket(name)).write("hey");
        try {
            MatcherAssert.assertThat(
                "should be empty list",
                new Bucket.Prefixed(bucket, "foo/").list("foo"),
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
    void listsInPrefixedBucketWithoutCollisions() throws Exception {
        final Bucket bucket = this.rule.bucket();
        final String[] names = {"alpha/", "alpha/beta.xml"};
        for (final String name : names) {
            new Ocket.Text(bucket.ocket(name)).write("");
        }
        try {
            MatcherAssert.assertThat(
                "should has item in list",
                new Bucket.Prefixed(bucket, names[0]).list(""),
                Matchers.hasItem("beta.xml")
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
    void existsExistingBucket() throws IOException {
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
    void existsNonExistingBucket() throws IOException {
        final Bucket bucket = this.rule.bucket();
        final S3Client aws = bucket.region().aws();
        aws.deleteBucket(
            DeleteBucketRequest.builder()
                .bucket(bucket.name())
                .build()
        );
        MatcherAssert.assertThat(
            "should be false",
            bucket.exists(),
            Matchers.is(false)
        );
    }

}
