/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3;

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import java.io.IOException;
import java.util.Iterator;
import lombok.EqualsAndHashCode;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Amazon S3 bucket.
 *
 * @since 0.1
 */
@EqualsAndHashCode(of = { "regn", "bkt" })
@Loggable(Loggable.DEBUG)
final class AwsBucket implements Bucket {

    /**
     * Region we're in.
     */
    private final transient Region regn;

    /**
     * Bucket name.
     */
    private final transient String bkt;

    /**
     * Public ctor.
     * @param reg Region we're in
     * @param name Bucket name
     */
    AwsBucket(final Region reg, final String name) {
        this.regn = reg;
        this.bkt = name;
    }

    @Override
    public String toString() {
        return this.bkt;
    }

    @Override
    public Region region() {
        return this.regn;
    }

    @Override
    public String name() {
        return this.bkt;
    }

    @Override
    public Ocket ocket(final String key) {
        return new AwsOcket(this, key);
    }

    @Override
    public boolean exists() throws IOException {
        final S3Client aws = this.regn.aws();
        boolean result = true;
        try {
            aws.headBucket(
                HeadBucketRequest.builder().bucket(this.bkt).build()
            );
        } catch (final NoSuchBucketException ex) {
            result = false;
        } catch (final S3Exception ex) {
            throw new IOException(
                String.format(
                    "failed to check existence of '%s' bucket",
                    this.bkt
                ),
                ex
            );
        }
        Logger.debug(
            this, "Does bucket '%s' exist? %b", this.bkt, result
        );
        return result;
    }

    @Override
    public void remove(final String key) throws IOException {
        try {
            this.regn.aws().deleteObject(
                DeleteObjectRequest.builder()
                    .bucket(this.bkt)
                    .key(key)
                    .build()
            );
            Logger.info(
                this,
                "ocket '%s' removed in bucket '%s'",
                key, this.bkt
            );
        } catch (final S3Exception ex) {
            throw new IOException(
                String.format(
                    "failed to remove '%s' bucket",
                    this.bkt
                ),
                ex
            );
        }
    }

    @Override
    public Iterable<String> list(final String pfx) {
        return new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                return new AwsListIterator(
                    AwsBucket.this.regn, AwsBucket.this.bkt, pfx
                );
            }
        };
    }

    @Override
    public int compareTo(final Bucket bucket) {
        return this.name().compareTo(bucket.name());
    }

}
