/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3;

import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;

/**
 * Integration case for {@link AwsOcket}.
 *
 * @since 0.1
 */
@SuppressWarnings("PMD.JUnit5TestShouldBePackagePrivate")
public final class AwsOcketITCase {

    /**
     * Bucket we're working with.
     * @checkstyle VisibilityModifier (3 lines)
     */
    @Rule
    public final transient BucketRule rule = new BucketRule();

    /**
     * AwsOcket can read and write S3 content.
     * @throws Exception If fails
     */
    @Test
    public void readsAndWritesObjectContent() throws Exception {
        final Bucket bucket = this.rule.bucket();
        final String name = "a/b/c/test.txt";
        final Ocket.Text ocket = new Ocket.Text(bucket.ocket(name));
        final String content = "text \u20ac\n\t\rtest";
        ocket.write(content);
        ocket.write(content);
        try {
            MatcherAssert.assertThat(
                "should be equal to content",
                ocket.read(),
                Matchers.equalTo(content)
            );
        } finally {
            bucket.remove(name);
        }
    }

    /**
     * AwsOcket can read and write large S3 content.
     * @throws Exception If fails
     */
    @Test
    public void readsAndWritesLargeObjectContent() throws Exception {
        final Bucket bucket = this.rule.bucket();
        final String name = "test-44.txt";
        final Ocket.Text ocket = new Ocket.Text(bucket.ocket(name));
        final String data = RandomStringUtils.random(100_000);
        ocket.write(data);
        try {
            MatcherAssert.assertThat(
                "should be equal to large content",
                ocket.read(),
                Matchers.equalTo(data)
            );
        } finally {
            bucket.remove(name);
        }
    }

    /**
     * AwsOcket can check S3 object existence.
     * @throws Exception If fails
     */
    @Test
    public void checksObjectExistenceInBucket() throws Exception {
        final Bucket bucket = this.rule.bucket();
        final String name = "a/b/ffo/test.txt";
        new Ocket.Text(bucket.ocket(name)).write("test me");
        try {
            MatcherAssert.assertThat(
                "should be true",
                bucket.ocket(name).exists(),
                Matchers.is(true)
            );
            MatcherAssert.assertThat(
                "should be false",
                bucket.ocket("a/b/ffo/test-2.txt").exists(),
                Matchers.is(false)
            );
        } finally {
            bucket.remove(name);
        }
    }

    /**
     * Region can throw when ocket is absent.
     * @throws Exception If fails
     */
    @Test(expected = OcketNotFoundException.class)
    public void throwsWhenObjectIsAbsent() throws Exception {
        final Bucket bucket = this.rule.bucket();
        new Ocket.Text(bucket.ocket("key-is-absent.txt")).read();
    }

}
