/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3;

import com.jcabi.aspects.Loggable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import lombok.EqualsAndHashCode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Amazon S3 bucket.
 *
 * @since 0.1
 */
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
    public HeadObjectResponse meta() throws IOException {
        try {
            return this.bkt.region().aws().headObject(
                HeadObjectRequest.builder()
                    .bucket(this.bkt.name())
                    .key(this.name)
                    .build()
            );
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
    public boolean exists() throws IOException {
        try {
            return !this.bkt.region().aws().listObjectsV2(
                ListObjectsV2Request.builder()
                    .bucket(this.bkt.name())
                    .prefix(this.name)
                    .maxKeys(1)
                    .build()
            ).contents().isEmpty();
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
    public void read(final OutputStream output) throws IOException {
        try {
            IOUtils.copy(
                this.bkt.region().aws().getObject(
                    GetObjectRequest.builder()
                        .bucket(this.bkt.name())
                        .key(this.name)
                        .build()
                ),
                output
            );
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
    public void write(final InputStream input, final HeadObjectResponse meta)
        throws IOException {
        try (BoundedInputStream cnt = BoundedInputStream.builder()
            .setInputStream(input).get()) {
            final PutObjectRequest.Builder req = PutObjectRequest.builder()
                .bucket(this.bkt.name())
                .key(this.name);
            if (meta.contentType() != null) {
                req.contentType(meta.contentType());
            }
            if (meta.contentEncoding() != null) {
                req.contentEncoding(meta.contentEncoding());
            }
            if (meta.contentLength() != null && meta.contentLength() > 0L) {
                this.bkt.region().aws().putObject(
                    req.contentLength(meta.contentLength()).build(),
                    RequestBody.fromInputStream(cnt, meta.contentLength())
                );
            } else {
                this.bkt.region().aws().putObject(
                    req.build(),
                    RequestBody.fromBytes(IOUtils.toByteArray(cnt))
                );
            }
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
