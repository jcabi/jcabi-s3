/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3.fake;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.s3.Bucket;
import com.jcabi.s3.Ocket;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import javax.activation.MimetypesFileTypeMap;
import lombok.EqualsAndHashCode;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

/**
 * Mock/fake ocket.
 *
 * @since 0.6
 */
@Immutable
@EqualsAndHashCode(of = { "bkt", "name" })
@Loggable(Loggable.DEBUG)
public final class FkOcket implements Ocket {

    /**
     * Directory we're working in.
     */
    private final transient String dir;

    /**
     * My bucket.
     */
    private final transient String bkt;

    /**
     * My name.
     */
    private final transient String name;

    /**
     * Ctor.
     * @throws IOException If fails
     * @since 0.17
     */
    public FkOcket() throws IOException {
        this("default", "default-key");
    }

    /**
     * Ctor.
     * @param bucket Bucket
     * @param key Key
     * @throws IOException If fails
     * @since 0.17
     */
    public FkOcket(final String bucket, final String key) throws IOException {
        this(Files.createTempDirectory("jcabi-s3"), bucket, key);
    }

    /**
     * Ctor.
     * @param file Dir we're in
     * @param bucket Bucket
     * @param key Key
     * @since 0.17
     */
    public FkOcket(final Path file, final String bucket, final String key) {
        this(file.toFile(), bucket, key);
    }

    /**
     * Ctor.
     * @param file Dir we're in
     * @param bucket Bucket
     * @param key Key
     */
    public FkOcket(final File file, final String bucket, final String key) {
        this(file.getAbsolutePath(), bucket, key);
    }

    /**
     * Ctor.
     * @param file Dir we're in
     * @param bucket Bucket
     * @param key Key
     * @since 0.17
     */
    public FkOcket(final String file, final String bucket, final String key) {
        this.dir = file;
        this.bkt = bucket;
        this.name = key;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public Bucket bucket() {
        return new FkBucket(new File(this.dir), this.bkt);
    }

    @Override
    public String key() {
        return this.name;
    }

    @Override
    public HeadObjectResponse meta() {
        final MimetypesFileTypeMap types = new MimetypesFileTypeMap();
        return HeadObjectResponse.builder()
            .contentLength(this.file().length())
            .contentType(types.getContentType(this.file()))
            .lastModified(Instant.ofEpochMilli(this.file().lastModified()))
            .contentEncoding("UTF-8")
            .build();
    }

    @Override
    public boolean exists() {
        return this.file().exists();
    }

    @Override
    public void read(final OutputStream output) throws IOException {
        try (
            InputStream input = Files.newInputStream(this.file().toPath());
            OutputStream ous = output
        ) {
            while (input.available() > 0) {
                ous.write(input.read());
            }
        }
    }

    @Override
    public void write(final InputStream input, final HeadObjectResponse meta)
        throws IOException {
        final File file = this.file();
        file.getParentFile().mkdirs();
        try (
            InputStream ins = input;
            OutputStream output = Files.newOutputStream(file.toPath())
        ) {
            while (ins.available() > 0) {
                output.write(input.read());
            }
        }
    }

    @Override
    public int compareTo(final Ocket ocket) {
        return this.name.compareTo(ocket.key());
    }

    /**
     * Get my file.
     * @return File
     */
    public File file() {
        if (this.bkt.isEmpty()) {
            throw new IllegalStateException("Bucket name can't be empty");
        }
        if (this.name.isEmpty()) {
            throw new IllegalStateException("Ocket name can't be empty");
        }
        return new File(
            new File(
                new File(this.dir),
                this.bkt
            ),
            this.name
        );
    }

}
