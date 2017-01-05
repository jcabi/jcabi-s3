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

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.s3.transfer.model.UploadResult;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import lombok.EqualsAndHashCode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;

/**
 * Amazon S3 bucket.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.1
 */
@Immutable
@EqualsAndHashCode(of = { "bkt", "name" })
@Loggable(Loggable.DEBUG)
final class AwsOcket implements Ocket {

    /**
     * Bucket we're in.
     */
    private final transient Bucket bkt;

    /**
     * Object name.
     */
    private final transient String name;

    /**
     * Public ctor.
     * @param bucket Bucket name
     * @param obj Object name
     */
    AwsOcket(final Bucket bucket, final String obj) {
        this.bkt = bucket;
        this.name = obj;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public Bucket bucket() {
        return this.bkt;
    }

    @Override
    public String key() {
        return this.name;
    }

    @Override
    public ObjectMetadata meta() throws IOException {
        try {
            final AmazonS3 aws = this.bkt.region().aws();
            final long start = System.currentTimeMillis();
            final ObjectMetadata meta = aws.getObjectMetadata(
                new GetObjectMetadataRequest(this.bkt.name(), this.name)
            );
            Logger.info(
                this,
                // @checkstyle LineLength (1 line)
                "metadata loaded for ocket '%s' in bucket '%s' in %[ms]s (etag=%s)",
                this.name, this.bkt.name(),
                System.currentTimeMillis() - start,
                meta.getETag()
            );
            return meta;
        } catch (final AmazonS3Exception ex) {
            throw new OcketNotFoundException(
                String.format(
                    "ocket '%s' not found in '%s', can't fetch meta()",
                    this.name, this.bkt.name()
                ),
                ex
            );
        } catch (final AmazonServiceException ex) {
            throw new IOException(
                String.format(
                    "failed to fetch meta of '%s' in '%s'",
                    this.name, this.bkt
                    ),
                ex
            );
        }
    }

    @Override
    public boolean exists() throws IOException {
        try {
            final AmazonS3 aws = this.bkt.region().aws();
            final long start = System.currentTimeMillis();
            final ObjectListing listing = aws.listObjects(
                new ListObjectsRequest()
                    .withBucketName(this.bkt.name())
                    .withPrefix(this.name)
                    .withMaxKeys(1)
            );
            final boolean exists = !listing.getObjectSummaries().isEmpty();
            Logger.info(
                this,
                "ocket '%s' existence checked in bucket '%s' in %[ms]s (%b)",
                this.name, this.bkt.name(),
                System.currentTimeMillis() - start,
                exists
            );
            return exists;
        } catch (final AmazonServiceException ex) {
            throw new IOException(
                String.format(
                    "failed to check existence of '%s' in '%s'",
                    this.name, this.bkt
                ),
                ex
            );
        }
    }

    @Override
    public void read(final OutputStream output) throws IOException {
        final AmazonS3 aws = this.bkt.region().aws();
        try {
            final long start = System.currentTimeMillis();
            final S3Object obj = aws.getObject(
                new GetObjectRequest(this.bkt.name(), this.name)
            );
            final InputStream input = obj.getObjectContent();
            final int bytes = IOUtils.copy(input, output);
            input.close();
            Logger.info(
                this,
                // @checkstyle LineLength (1 line)
                "loaded %d byte(s) from ocket '%s' in bucket '%s' in %[ms]s (etag=%s)",
                bytes, this.name, this.bkt.name(),
                System.currentTimeMillis() - start,
                obj.getObjectMetadata().getETag()
            );
        } catch (final AmazonS3Exception ex) {
            throw new OcketNotFoundException(
                String.format(
                    "ocket '%s' not found in '%s'",
                    this.name, this.bkt.name()
                ),
                ex
            );
        } catch (final AmazonServiceException ex) {
            throw new IOException(
                String.format(
                    "failed to read the content of '%s' in '%s'",
                    this.name, this.bkt
                ),
                ex
            );
        }
    }

    @Override
    public void write(final InputStream input, final ObjectMetadata meta)
        throws IOException {
        final CountingInputStream cnt = new CountingInputStream(input);
        try {
            final AmazonS3 aws = this.bkt.region().aws();
            final long start = System.currentTimeMillis();
            final TransferManager tmgr = new TransferManager(aws);
            final Upload upload = tmgr.upload(
                this.bkt.name(), this.name, cnt, meta
            );
            final UploadResult result = upload.waitForUploadResult();
            Logger.info(
                this,
                // @checkstyle LineLength (1 line)
                "saved %d byte(s) to ocket '%s' in bucket '%s' in %[ms]s (etag=%s)",
                cnt.getByteCount(), this.name, this.bkt.name(),
                System.currentTimeMillis() - start,
                result.getETag()
            );
            tmgr.shutdownNow(false);
        } catch (final AmazonServiceException ex) {
            throw new IOException(
                String.format(
                    "failed to write content to '%s' in '%s'",
                    this.name, this.bkt
                ),
                ex
            );
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException(
                String.format(
                    "writing to '%s' in '%s' interrupted",
                    this.name, this.bkt
                ),
                ex
            );
        } finally {
            cnt.close();
        }
    }

    @Override
    public int compareTo(final Ocket ocket) {
        return this.key().compareTo(ocket.key());
    }
}
