/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

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
     * Continuation token for next page, or null if no more pages.
     */
    private transient String token;

    /**
     * Whether more pages may be available.
     */
    private transient boolean more;

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
        this.more = true;
    }

    @Override
    public final boolean hasNext() {
        if (this.partial == null || this.partial.isEmpty()
            && this.more) {
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
    private List<String> load() {
        try {
            final ListObjectsV2Request.Builder req =
                ListObjectsV2Request.builder()
                    .bucket(this.bucket)
                    .prefix(this.prefix);
            if (this.token != null) {
                req.continuationToken(this.token);
            }
            final ListObjectsV2Response listing =
                this.region.aws().listObjectsV2(req.build());
            if (listing.isTruncated()) {
                this.token = listing.nextContinuationToken();
            } else {
                this.more = false;
            }
            final List<String> list = new LinkedList<>();
            for (final S3Object sum : listing.contents()) {
                list.add(sum.key());
            }
            return list;
        } catch (final S3Exception ex) {
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
