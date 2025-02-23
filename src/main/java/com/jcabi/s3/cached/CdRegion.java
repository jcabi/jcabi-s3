/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3.cached;

import com.amazonaws.services.s3.AmazonS3;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.s3.Bucket;
import com.jcabi.s3.Region;
import lombok.EqualsAndHashCode;

/**
 * Cached region.
 *
 * @since 0.8
 */
@Immutable
@EqualsAndHashCode(of = "origin")
@Loggable(Loggable.DEBUG)
public final class CdRegion implements Region {

    /**
     * Original region.
     */
    private final transient Region origin;

    /**
     * Public ctor.
     * @param reg Region we're in
     */
    public CdRegion(final Region reg) {
        this.origin = reg;
    }

    @Override
    public String toString() {
        return this.origin.toString();
    }

    @Override
    public Bucket bucket(final String name) {
        return new CdBucket(this.origin.bucket(name));
    }

    @Override
    public AmazonS3 aws() {
        return this.origin.aws();
    }
}
