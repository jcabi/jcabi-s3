/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3.cached;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.s3.Bucket;
import com.jcabi.s3.Ocket;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import lombok.EqualsAndHashCode;

/**
 * Cached ocket.
 *
 * @since 0.8
 */
@Immutable
@EqualsAndHashCode(of = "origin")
@Loggable(Loggable.DEBUG)
public final class CdOcket implements Ocket {

    /**
     * Original ocket.
     */
    private final transient Ocket origin;

    /**
     * Public ctor.
     * @param okt Ocket original
     */
    public CdOcket(final Ocket okt) {
        this.origin = okt;
    }

    @Override
    public String toString() {
        return this.origin.toString();
    }

    @Override
    public Bucket bucket() {
        return new CdBucket(this.origin.bucket());
    }

    @Override
    public String key() {
        return this.origin.key();
    }

    @Override
    @Cacheable
    public ObjectMetadata meta() throws IOException {
        return this.origin.meta();
    }

    @Override
    @Cacheable
    public boolean exists() throws IOException {
        return this.origin.exists();
    }

    @Override
    public void read(final OutputStream output) throws IOException {
        output.write(this.read());
    }

    @Override
    @Cacheable.FlushAfter
    public void write(final InputStream input, final ObjectMetadata meta)
        throws IOException {
        this.origin.write(input, meta);
    }

    @Override
    public int compareTo(final Ocket ocket) {
        return this.origin.compareTo(ocket);
    }

    /**
     * Read byte array.
     * @return Bytes
     * @throws IOException If fails
     */
    @Cacheable
    private byte[] read() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.origin.read(baos);
        return baos.toByteArray();
    }

}
