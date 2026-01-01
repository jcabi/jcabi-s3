/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3.fake;

import com.amazonaws.services.s3.AmazonS3;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.s3.Bucket;
import com.jcabi.s3.Region;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.EqualsAndHashCode;

/**
 * Mock/fake region.
 *
 * @since 0.6
 */
@Immutable
@EqualsAndHashCode
@Loggable(Loggable.DEBUG)
public final class FkRegion implements Region {

    /**
     * Directory we're working in.
     */
    private final transient String dir;

    /**
     * Ctor.
     * @throws IOException If fails
     * @since 0.17
     */
    public FkRegion() throws IOException {
        this(Files.createTempDirectory("jcabi-s3"));
    }

    /**
     * Ctor.
     * @param file Directory to keep files in
     * @since 0.17
     */
    public FkRegion(final Path file) {
        this(file.toFile());
    }

    /**
     * Ctor.
     * @param file Directory to keep files in
     * @since 0.17
     */
    public FkRegion(final String file) {
        this(new File(file));
    }

    /**
     * Ctor.
     * @param file Directory to keep files in
     * @since 0.8.1
     */
    public FkRegion(final File file) {
        this.dir = FkRegion.path(file);
    }

    @Override
    public String toString() {
        return this.dir;
    }

    @Override
    public Bucket bucket(final String name) {
        return new FkBucket(new File(this.dir), name);
    }

    @Override
    public AmazonS3 aws() {
        throw new UnsupportedOperationException("#aws()");
    }

    /**
     * Convert it to a dir.
     * @param file The file
     * @return Absolute path
     */
    private static String path(final File file) {
        if (!file.isDirectory()) {
            throw new IllegalArgumentException(
                String.format("%s is not a directory", file)
            );
        }
        return file.getAbsolutePath();
    }
}
