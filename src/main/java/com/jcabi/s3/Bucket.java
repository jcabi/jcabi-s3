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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Amazon S3 bucket.
 *
 * <p>You get an instance of this interface from {@link Region}, for example:
 *
 * <pre> Region region = new Region.Simple(key, secret);
 * Bucket bucket = region.bucket("my.example.com");
 * for (String key : bucket.list("")) {
 *   System.out.println(
 *     "key: " + key + ", last modified: "
 *     + bucket.get(key).meta().getLastModified()
 *   );
 * }</pre>
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.1
 */
@Immutable
@SuppressWarnings("PMD.TooManyMethods")
public interface Bucket extends Comparable<Bucket> {

    /**
     * Get region we're in.
     * @return Region
     */
    Region region();

    /**
     * Get bucket name.
     * @return Bucket name
     */
    String name();

    /**
     * Get object.
     * @param key Name of it in the bucket
     * @return Ocket
     */
    Ocket ocket(String key);

    /**
     * Checks if the bucket exists.
     * @return If the bucket exists {@code true}, otherwise {@code false}
     * @throws IOException If any failure happens
     */
    boolean exists() throws IOException;

    /**
     * Delete object from bucket.
     * @param key Name of it in the bucket
     * @throws IOException If not found or any other failure
     */
    void remove(String key)
        throws IOException;

    /**
     * List object names with a given prefix.
     * @param pfx Prefix to use
     * @return Iterable of names
     * @throws IOException If fails
     * @since 0.3
     */
    Iterable<String> list(String pfx) throws IOException;

    /**
     * Creates bucket with specified origin bucket and prefix.
     *
     * <p>Basically this class is used to cut off ocket keys of underlying
     * bucket by some string known as prefix. If key is not started
     * with prefix, it will be omitted
     *
     * <p>Example of usage:
     * <pre>
     * final Region region = new MkRegion(
     *   new TemporaryFolder().newFolder()
     * );
     * final Bucket bucket = region.bucket("test");
     * new Ocket.Text(bucket.ocket("a/first.txt")).write("");
     * new Ocket.Text(bucket.ocket("a/b/hello.txt")).write("");
     * new Ocket.Text(bucket.ocket("a/b/f/2.txt")).write("");
     * Bucket.Prefixed prefixed = new Bucket.Prefixed(
     *   bucket, "a/b/"
     * );
     * Iterable&lt;String&gt; list = prefixed.list(
     *   ""
     * ); // contains "hello.txt" and "f/2.txt"
     * </pre>
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = { "origin", "prefix" })
    @Loggable(Loggable.DEBUG)
    final class Prefixed implements Bucket {
        /**
         * Original encapsulated bucket.
         */
        private final transient Bucket origin;
        /**
         * Prefix.
         */
        private final transient String prefix;
        /**
         * Public ctor.
         * @param bucket Original bucket
         * @param pfx Prefix
         */
        public Prefixed(final Bucket bucket, final String pfx) {
            this.origin = bucket;
            this.prefix = pfx;
        }
        @Override
        public Region region() {
            return this.origin.region();
        }
        @Override
        public String name() {
            return this.origin.name();
        }
        @Override
        public Ocket ocket(final String key) {
            return this.origin.ocket(this.extend(key));
        }
        @Override
        public boolean exists() throws IOException {
            return this.origin.exists();
        }
        @Override
        public void remove(final String key) throws IOException {
            this.origin.remove(this.extend(key));
        }
        @Override
        public Iterable<String> list(final String pfx) throws IOException {
            return Iterables.filter(
                Iterables.transform(
                    this.origin.list(this.extend(pfx)),
                    new Function<String, String>() {
                        @Override
                        public String apply(final String input) {
                            final String name;
                            if (input.length()
                                < Bucket.Prefixed.this.prefix.length()) {
                                name = input;
                            } else {
                                name = input.substring(
                                    Bucket.Prefixed.this.prefix.length()
                                );
                            }
                            return name;
                        }
                    }
                ),
                new Predicate<String>() {
                    @Override
                    public boolean apply(final String input) {
                        return !input.isEmpty();
                    }
                }
            );
        }

        @Override
        public int compareTo(final Bucket bucket) {
            return this.origin.compareTo(bucket);
        }
        /**
         * Extend name with a prefix.
         * @param name The name to extend
         * @return Extended
         */
        private String extend(final String name) {
            return String.format("%s%s", this.prefix, name);
        }
    }

}
