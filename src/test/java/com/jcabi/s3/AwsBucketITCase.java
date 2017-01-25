/**
 * Copyright (c) 2012-2017, jcabi.com
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

import com.amazonaws.services.s3.AmazonS3;
import java.io.IOException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;

/**
 * Integration case for {@link AwsBucket}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.3
 */
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
                bucket.list(""),
                Matchers.allOf(
                    Matchers.<String>iterableWithSize(1),
                    Matchers.hasItem(name)
                )
            );
            MatcherAssert.assertThat(
                bucket.list("a/"),
                Matchers.allOf(
                    Matchers.<String>iterableWithSize(1),
                    Matchers.hasItem(name)
                )
            );
            MatcherAssert.assertThat(
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
                bkt.list(""),
                Matchers.allOf(
                    Matchers.<String>iterableWithSize(1),
                    Matchers.hasItem(item)
                )
            );
            MatcherAssert.assertThat(
                bkt.list("bar/"),
                Matchers.allOf(
                    Matchers.<String>iterableWithSize(1),
                    Matchers.hasItem(item)
                )
            );
            MatcherAssert.assertThat(
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
    public void listsInPrefixedBucketWithouCollisions() throws Exception {
        final Bucket bucket = this.rule.bucket();
        final String[] names = {"alpha/", "alpha/beta.xml"};
        for (final String name : names) {
            new Ocket.Text(bucket.ocket(name)).write("");
        }
        final Bucket bkt = new Bucket.Prefixed(bucket, names[0]);
        try {
            MatcherAssert.assertThat(
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
            bucket.exists(),
            Matchers.is(true)
        );
    }

}
