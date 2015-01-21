/**
 * Copyright (c) 2012-2014, jcabi.com
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
import java.util.Iterator;
import java.util.LinkedList;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

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
    public Ocket ocket(@NotNull(message = "key can't be NULL")
        final String key) {
        return new AwsOcket(this, key);
    }

    @Override
    public boolean exists() throws IOException {
        final AmazonS3 aws = this.regn.aws();
        boolean result = false;
        try {
            result = aws.doesBucketExist(this.bkt);
        } catch (final AmazonServiceException ex) {
            throw new IOException(ex);
        }
        Logger.debug(this, "Does bucket '%s' exist? %b", this.bkt, result);
        return result;
    }

    @Override
    public void remove(@NotNull(message = "key can't be NULL")
        final String key) throws IOException {
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
            throw new IOException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<String> list(@NotNull(message = "prefix can't be NULL")
        final String pfx) {
        return new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                return new AwsListIterator(pfx);
            }
        };
    }

    @Override
    public int compareTo(final Bucket bucket) {
        return this.name().compareTo(bucket.name());
    }

    private class AwsListIterator implements Iterator<String> {

        /**
         * Prefix to use.
         */
        private final transient String prefix;

        /**
         * Partial S3 response iterator.
         */
        private transient Iterator<String> partial;

        /**
         * Next marker for partial response or null if response contains no more
         * data.
         */
        private transient String marker;

        /**
         * Constructs AwsListIterator.
         * @param pfx Key prefix
         */
        public AwsListIterator(final String pfx) {
            this.prefix = pfx;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext() {
            this.loadDataIfNeeded();
            return this.partial.hasNext() || this.marker != null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String next() {
            this.loadDataIfNeeded();
            return this.partial.next();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove is not supported");
        }

        /**
         * Loads next portion of data from S3 if current data is already
         * consumed and more data is available.
         */
        private void loadDataIfNeeded() {
            if (this.partial == null || (!this.partial.hasNext()
                && this.marker != null)) {
                this.loadPartialData();
            }
        }

        /**
         * Loads next portion of data from S3.
         */
        private void loadPartialData() {
            try {
                final AmazonS3 aws = AwsBucket.this.regn.aws();
                final long start = System.currentTimeMillis();
                final ObjectListing listing = aws.listObjects(
                    new ListObjectsRequest()
                        .withBucketName(AwsBucket.this.bkt)
                        .withPrefix(this.prefix)
                        .withMarker(this.marker)
                );
                this.marker = listing.getNextMarker();
                final Collection<String> list = new LinkedList<String>();
                for (final S3ObjectSummary sum
                    : listing.getObjectSummaries()) {
                    list.add(sum.getKey());
                }
                this.partial = list.iterator();
                Logger.info(
                    this,
                    StringUtils.join(
                        "listed %d ocket(s) with prefix '%s' in bucket ",
                        "'%s' in %[ms]s"
                    ),
                    listing.getObjectSummaries().size(), this.prefix,
                    AwsBucket.this.bkt, System.currentTimeMillis() - start
                );
            } catch (final AmazonServiceException ex) {
                throw new IllegalStateException(ex);
            }
        }

    }

}

