/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.jcabi.aspects.Loggable;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Amazon S3 abstraction.
 *
 * <p>To get an instance of this interface, instantiate {@code Region.Simple},
 * for example:
 *
 * <pre> Region region = new Region.Simple(key, secret);
 * Bucket bucket = region.bucket("my.example.com");
 * bucket.remove("README.txt");</pre>
 *
 * @since 0.1
 */
public interface Region {

    /**
     * Get bucket.
     * @param name Name of the bucket to get
     * @return Bucket
     */
    Bucket bucket(String name);

    /**
     * Get a client.
     * @return Amazon S3
     */
    AmazonS3 aws();

    /**
     * Simple implementation.
     *
     * @since 0.1
     */
    @ToString
    @EqualsAndHashCode(of = "server")
    @Loggable(Loggable.DEBUG)
    final class Simple implements Region {
        /**
         * AWS.
         */
        private final transient AmazonS3 server;

        /**
         * Public ctor.
         * @param key Amazon key
         * @param secret Amazon secret
         */
        public Simple(final String key, final String secret) {
            this(key, secret, "us-east-1");
        }

        /**
         * Public ctor.
         * @param key Amazon key
         * @param secret Amazon secret
         * @param region Region
         */
        public Simple(final String key, final String secret,
            final String region) {
            this(
                AmazonS3ClientBuilder.standard()
                    .withRegion(region)
                    .withCredentials(
                        new AWSStaticCredentialsProvider(
                            new BasicAWSCredentials(key, secret)
                        )
                    )
                    .build()
            );
        }

        /**
         * Public ctor.
         * @param aws Amazon S3 server
         */
        public Simple(final AmazonS3 aws) {
            this.server = aws;
        }

        @Override
        public Bucket bucket(final String name) {
            return new AwsBucket(this, name);
        }

        @Override
        public AmazonS3 aws() {
            return this.server;
        }
    }
}
