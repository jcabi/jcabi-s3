/**
 * Copyright (c) 2012-2013, JCabi.com
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

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * Amazon S3 bucket.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.1
 */
@Immutable
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
    public Ocket ocket(@NotNull(message = "key can't be NULL")
        final String key) {
        return new AwsOcket(this, key);
    }

    @Override
    public void remove(@NotNull(message = "key can't be NULL")
        final String key) throws IOException {
        try {
            final AmazonS3 aws = this.regn.aws();
            aws.deleteObject(new DeleteObjectRequest(this.bkt, key));
            Logger.info(
                this,
                "ocket '%s' removed in bucket '%s'",
                key, this.bkt
            );
        } catch (final AmazonServiceException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * {@inheritDoc}
     * @todo #2 Large lists are not supported. The method doesn't
     *  support marker functionality, provided by ObjectListing. We should
     *  create a test that reproduces the problem and fix the method. The
     *  method should return an iterable, which should fetch portions
     *  of objects from S3 on demand.
     */
    @Override
    public Iterable<String> list(@NotNull(message = "prefix can't be NULL")
        final String pfx) throws IOException {
        try {
            final AmazonS3 aws = this.regn.aws();
            final ObjectListing listing = aws.listObjects(
                new ListObjectsRequest()
                    .withBucketName(this.bkt)
                    .withPrefix(pfx)
            );
            final Collection<String> list = new LinkedList<String>();
            for (final S3ObjectSummary sum : listing.getObjectSummaries()) {
                list.add(sum.getKey());
            }
            Logger.info(
                this,
                "listed %d ocket(s) with prefix '%s' in bucket '%s'",
                listing.getObjectSummaries().size(), pfx, this.bkt
            );
            return list;
        } catch (final AmazonServiceException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public int compareTo(final Bucket bucket) {
        return this.name().compareTo(bucket.name());
    }
}
