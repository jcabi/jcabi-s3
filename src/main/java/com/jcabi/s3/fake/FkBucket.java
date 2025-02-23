/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3.fake;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.s3.Bucket;
import com.jcabi.s3.Ocket;
import com.jcabi.s3.Region;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.EqualsAndHashCode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

/**
 * Mock/fake bucket.
 *
 * @since 0.6
 */
@Immutable
@EqualsAndHashCode(of = "bkt")
@Loggable(Loggable.DEBUG)
public final class FkBucket implements Bucket {

    /**
     * My name.
     */
    private final transient String bkt;

    /**
     * Directory we're working in.
     */
    private final transient String dir;

    /**
     * Ctor.
     * @throws IOException If fails
     * @since 0.17
     */
    public FkBucket() throws IOException {
        this("default");
    }

    /**
     * Ctor.
     * @param name Name of the bucket
     * @throws IOException If fails
     * @since 0.17
     */
    public FkBucket(final String name) throws IOException {
        this(Files.createTempDirectory("jcabi-s3"), name);
    }

    /**
     * Ctor.
     * @param file Directory to keep files in
     * @param name Name of the bucket
     */
    public FkBucket(final File file, final String name) {
        this(file.getAbsolutePath(), name);
    }

    /**
     * Ctor.
     * @param file Directory to keep files in
     * @param name Name of the bucket
     * @since 0.17
     */
    public FkBucket(final Path file, final String name) {
        this(file.toFile(), name);
    }

    /**
     * Ctor.
     * @param file Directory to keep files in
     * @param name Name of the bucket
     * @since 0.17
     */
    public FkBucket(final String file, final String name) {
        this.dir = file;
        this.bkt = name;
    }

    @Override
    public String toString() {
        return this.bkt;
    }

    @Override
    public Region region() {
        return new FkRegion(new File(this.dir));
    }

    @Override
    public String name() {
        return this.bkt;
    }

    @Override
    public Ocket ocket(final String key) {
        return new FkOcket(new File(this.dir), this.bkt, key);
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public void remove(final String key) {
        new File(this.home(), key).delete();
    }

    @Override
    public Iterable<String> list(final String pfx) {
        final File home = this.home();
        return Iterables.transform(
            FileUtils.listFiles(
                new File(home, pfx),
                TrueFileFilter.INSTANCE,
                TrueFileFilter.INSTANCE
            ),
            new Function<File, String>() {
                @Override
                public String apply(final File file) {
                    return FilenameUtils.separatorsToUnix(
                        file.getAbsolutePath().substring(
                            home.getAbsolutePath().length() + 1
                        )
                    );
                }
            }
        );
    }

    @Override
    public int compareTo(final Bucket bucket) {
        return this.bkt.compareTo(bucket.name());
    }

    /**
     * Get my file.
     * @return File
     */
    private File home() {
        if (this.bkt.isEmpty()) {
            throw new IllegalStateException("Ocket name can't be empty");
        }
        return new File(new File(this.dir), this.bkt);
    }

}
