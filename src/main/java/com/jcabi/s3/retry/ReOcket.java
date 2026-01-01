/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3.retry;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.RetryOnFailure;
import com.jcabi.s3.Bucket;
import com.jcabi.s3.Ocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import lombok.EqualsAndHashCode;

/**
 * Ocket that retries a few times before giving up.
 *
 * @since 0.5
 */
@Immutable
@EqualsAndHashCode(of = "origin")
@Loggable(Loggable.DEBUG)
public final class ReOcket implements Ocket {

    /**
     * Original ocket.
     */
    private final transient Ocket origin;

    /**
     * Public ctor.
     * @param okt Ocket we're in
     */
    public ReOcket(final Ocket okt) {
        this.origin = okt;
    }

    @Override
    public String toString() {
        return this.origin.toString();
    }

    @Override
    public Bucket bucket() {
        return new ReBucket(this.origin.bucket());
    }

    @Override
    public String key() {
        return this.origin.key();
    }

    @Override
    @RetryOnFailure(verbose = false)
    public ObjectMetadata meta() throws IOException {
        return this.origin.meta();
    }

    @Override
    @RetryOnFailure(verbose = false)
    public boolean exists() throws IOException {
        return this.origin.exists();
    }

    @Override
    @RetryOnFailure(verbose = false)
    public void read(final OutputStream output) throws IOException {
        this.origin.read(output);
    }

    @Override
    @RetryOnFailure(verbose = false)
    public void write(final InputStream input, final ObjectMetadata meta)
        throws IOException {
        this.origin.write(input, meta);
    }

    @Override
    public int compareTo(final Ocket okt) {
        return this.origin.key().compareTo(okt.key());
    }
}
