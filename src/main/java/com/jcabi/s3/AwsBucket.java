/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import java.io.IOException;
import java.util.Iterator;
import lombok.EqualsAndHashCode;

/**
 * Amazon S3 bucket.
 *
 * @since 0.1
 */
@EqualsAndHashCode(of = { "regn", "bkt" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.TooManyMethods")
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
        final AmazonS3 aws = this.regn.aws();
        final boolean result;
        try {
            result = aws.doesBucketExistV2(this.bkt);
        } catch (final AmazonServiceException ex) {
            throw new IOException(
                String.format(
                    "failed to check existence of '%s' bucket",
                    this.bkt
                ),
                ex
            );
        }
        Logger.debug(this, "Does bucket '%s' exist? %b", this.bkt, result);
        return result;
    }

    @Override
    @SuppressWarnings("PMD.GuardLogStatement")
    public void remove(final String key) throws IOException {
        try {
            final AmazonS3 aws = this.regn.aws();
            final long start = System.currentTimeMillis();
            aws.deleteObject(new DeleteObjectRequest(this.bkt, key));
            Logger.info(
                this,
                "ocket '%s' removed in bucket '%s' in %[ms]s",
                key, this.bkt, System.currentTimeMillis() - start
            );
        } catch (final AmazonServiceException ex) {
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
