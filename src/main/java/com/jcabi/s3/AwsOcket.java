/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3;

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import lombok.EqualsAndHashCode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Amazon S3 bucket.
 *
 * @since 0.1
 */
@EqualsAndHashCode(of = { "bkt", "name" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.GuardLogStatement")
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
    public HeadObjectResponse meta() throws IOException {
        try {
            final S3Client aws = this.bkt.region().aws();
            final long start = System.currentTimeMillis();
            final HeadObjectResponse meta = aws.headObject(
                HeadObjectRequest.builder()
                    .bucket(this.bkt.name())
                    .key(this.name)
                    .build()
            );
            Logger.info(
                this,
                // @checkstyle LineLength (1 line)
                "metadata loaded for ocket '%s' in bucket '%s' in %[ms]s (etag=%s)",
                this.name, this.bkt.name(),
                System.currentTimeMillis() - start,
                meta.eTag()
            );
            return meta;
        } catch (final S3Exception ex) {
            throw new OcketNotFoundException(
                String.format(
                    "ocket '%s' not found in '%s', can't fetch meta()",
                    this.name, this.bkt.name()
                ),
                ex
            );
        }
    }

    @Override
    @SuppressWarnings("PMD.GuardLogStatement")
    public boolean exists() throws IOException {
        try {
            final S3Client aws = this.bkt.region().aws();
            final long start = System.currentTimeMillis();
            final ListObjectsV2Response listing = aws.listObjectsV2(
                ListObjectsV2Request.builder()
                    .bucket(this.bkt.name())
                    .prefix(this.name)
                    .maxKeys(1)
                    .build()
            );
            final boolean exists = !listing.contents().isEmpty();
            Logger.info(
                this,
                "ocket '%s' existence checked in bucket '%s' in %[ms]s (%b)",
                this.name, this.bkt.name(),
                System.currentTimeMillis() - start,
                exists
            );
            return exists;
        } catch (final S3Exception ex) {
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
    @SuppressWarnings("PMD.GuardLogStatement")
    public void read(final OutputStream output) throws IOException {
        try {
            final S3Client aws = this.bkt.region().aws();
            final ResponseInputStream<GetObjectResponse> obj = aws.getObject(
                GetObjectRequest.builder()
                    .bucket(this.bkt.name())
                    .key(this.name)
                    .build()
            );
            try (InputStream input = obj) {
                final long start = System.currentTimeMillis();
                final int bytes = IOUtils.copy(input, output);
                Logger.info(
                    this,
                    // @checkstyle LineLength (1 line)
                    "loaded %d byte(s) from ocket '%s' in bucket '%s' in %[ms]s (etag=%s)",
                    bytes, this.name, this.bkt.name(),
                    System.currentTimeMillis() - start,
                    obj.response().eTag()
                );
            }
        } catch (final S3Exception ex) {
            throw new OcketNotFoundException(
                String.format(
                    "ocket '%s' not found in '%s'",
                    this.name, this.bkt.name()
                ),
                ex
            );
        }
    }

    @Override
    @SuppressWarnings("PMD.GuardLogStatement")
    public void write(final InputStream input, final HeadObjectResponse meta)
        throws IOException {
        try (BoundedInputStream cnt = BoundedInputStream.builder()
            .setInputStream(input).get()) {
            final S3Client aws = this.bkt.region().aws();
            final long start = System.currentTimeMillis();
            final PutObjectRequest.Builder req = PutObjectRequest.builder()
                .bucket(this.bkt.name())
                .key(this.name);
            if (meta.contentType() != null) {
                req.contentType(meta.contentType());
            }
            if (meta.contentEncoding() != null) {
                req.contentEncoding(meta.contentEncoding());
            }
            final PutObjectResponse result;
            if (meta.contentLength() != null && meta.contentLength() > 0L) {
                result = aws.putObject(
                    req.contentLength(meta.contentLength()).build(),
                    RequestBody.fromInputStream(cnt, meta.contentLength())
                );
            } else {
                final byte[] bytes = IOUtils.toByteArray(cnt);
                result = aws.putObject(
                    req.contentLength((long) bytes.length).build(),
                    RequestBody.fromBytes(bytes)
                );
            }
            Logger.info(
                this,
                // @checkstyle LineLength (1 line)
                "saved %d byte(s) to ocket '%s' in bucket '%s' in %[ms]s (etag=%s)",
                cnt.getCount(), this.name, this.bkt.name(),
                System.currentTimeMillis() - start,
                result.eTag()
            );
        } catch (final S3Exception ex) {
            throw new IOException(
                String.format(
                    "failed to write content to '%s' in '%s'",
                    this.name, this.bkt
                ),
                ex
            );
        }
    }

    @Override
    public int compareTo(final Ocket ocket) {
        return this.key().compareTo(ocket.key());
    }
}
