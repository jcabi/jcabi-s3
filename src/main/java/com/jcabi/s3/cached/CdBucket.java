/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3.cached;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.s3.Bucket;
import com.jcabi.s3.Ocket;
import com.jcabi.s3.Region;
import java.io.IOException;
import lombok.EqualsAndHashCode;

/**
 * Cached bucket.
 *
 * @since 0.8
 */
@Immutable
@EqualsAndHashCode(of = "origin")
@Loggable(Loggable.DEBUG)
public final class CdBucket implements Bucket {

    /**
     * Original bucket.
     */
    private final transient Bucket origin;

    /**
     * Public ctor.
     * @param bkt Bucket original
     */
    public CdBucket(final Bucket bkt) {
        this.origin = bkt;
    }

    @Override
    public String toString() {
        return this.origin.toString();
    }

    @Override
    public Region region() {
        return new CdRegion(this.origin.region());
    }

    @Override
    public String name() {
        return this.origin.name();
    }

    @Override
    public Ocket ocket(final String key) {
        return new CdOcket(this.origin.ocket(key));
    }

    @Override
    public boolean exists() throws IOException {
        return this.origin.exists();
    }

    @Override
    public void remove(final String key) throws IOException {
        this.origin.remove(key);
    }

    @Override
    public Iterable<String> list(final String pfx) throws IOException {
        return this.origin.list(pfx);
    }

    @Override
    public int compareTo(final Bucket bucket) {
        return this.origin.compareTo(bucket);
    }
}
