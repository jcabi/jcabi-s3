/*
 * Copyright (c) 2012-2025 Yegor Bugayenko
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
