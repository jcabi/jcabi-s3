/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.jcabi.log.Logger;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Iterator for large lists returned by S3.
 * @since 0.10
 */
class AwsListIterator implements Iterator<String> {

    /**
     * Region we're in.
     */
    private final transient Region region;

    /**
     * Bucket name.
     */
    private final transient String bucket;

    /**
     * Prefix to use.
     */
    private final transient String prefix;

    /**
     * Partial S3 response iterator.
     */
    private transient List<String> partial;

    /**
     * Next marker for partial response or null if response contains no more
     * data.
     */
    private transient String marker;

    /**
     * Constructs AwsListIterator.
     * @param rgn Region we're in
     * @param bkt Bucket name
     * @param pfx Key prefix
     */
    AwsListIterator(final Region rgn, final String bkt,
        final String pfx) {
        this.prefix = pfx;
        this.region = rgn;
        this.bucket = bkt;
    }

    @Override
    public final boolean hasNext() {
        if (this.partial == null || this.partial.isEmpty()
            && this.marker != null) {
            this.partial = this.load();
        }
        return !this.partial.isEmpty();
    }

    @Override
    public final String next() {
        if (!this.hasNext()) {
            throw new NoSuchElementException(
                "There are no more elements in this iterator"
            );
        }
        return this.partial.remove(0);
    }

    @Override
    public final void remove() {
        throw new UnsupportedOperationException("Remove is not supported");
    }

    /**
     * Loads next portion of data from S3.
     * @return A list with next portion of data from S3
     */
    @SuppressWarnings("PMD.GuardLogStatement")
    private List<String> load() {
        try {
            final AmazonS3 aws = this.region.aws();
            final long start = System.currentTimeMillis();
            final ObjectListing listing = aws.listObjects(
                new ListObjectsRequest()
                    .withBucketName(this.bucket)
                    .withPrefix(this.prefix)
                    .withMarker(this.marker)
            );
            this.marker = listing.getNextMarker();
            final List<String> list = new LinkedList<>();
            for (final S3ObjectSummary sum
                : listing.getObjectSummaries()) {
                list.add(sum.getKey());
            }
            Logger.info(
                this,
                "listed %d ocket(s) with prefix '%s' in bucket '%s' in %[ms]s",
                listing.getObjectSummaries().size(), this.prefix,
                this.bucket, System.currentTimeMillis() - start
            );
            return list;
        } catch (final AmazonServiceException ex) {
            throw new IllegalStateException(
                String.format(
                    "failed to load a list of objects in '%s', prefix=%s",
                    this.bucket, this.prefix
                ),
                ex
            );
        }
    }

}
