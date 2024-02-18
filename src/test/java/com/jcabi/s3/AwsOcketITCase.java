/*
 * Copyright (c) 2012-2024, jcabi.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the jcabi.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
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
            MatcherAssert.assertThat(ocket.read(), Matchers.equalTo(content));
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
            MatcherAssert.assertThat(ocket.read(), Matchers.equalTo(data));
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
                bucket.ocket(name).exists(),
                Matchers.is(true)
            );
            MatcherAssert.assertThat(
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
