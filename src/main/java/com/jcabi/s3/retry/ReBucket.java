/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3.retry;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.RetryOnFailure;
import com.jcabi.s3.Bucket;
import com.jcabi.s3.Ocket;
import com.jcabi.s3.Region;
import java.io.IOException;
import java.util.Iterator;
import lombok.EqualsAndHashCode;

/**
 * Region that retries a few times before giving up.
 *
 * @since 0.5
 */
@Immutable
@EqualsAndHashCode(of = "origin")
@Loggable(Loggable.DEBUG)
public final class ReBucket implements Bucket {

    /**
     * Original bucket.
     */
    private final transient Bucket origin;

    /**
     * Public ctor.
     * @param bkt Bucket we're in
     */
    public ReBucket(final Bucket bkt) {
        this.origin = bkt;
    }

    @Override
    public String toString() {
        return this.origin.toString();
    }

    @Override
    public Region region() {
        return new ReRegion(this.origin.region());
    }

    @Override
    public String name() {
        return this.origin.name();
    }

    @Override
    public Ocket ocket(final String key) {
        return new ReOcket(this.origin.ocket(key));
    }

    @Override
    public boolean exists() throws IOException {
        return this.origin.exists();
    }

    @Override
    @RetryOnFailure(verbose = false)
    public void remove(final String key) throws IOException {
        this.origin.remove(key);
    }

    @Override
    @RetryOnFailure(verbose = false)
    public Iterable<String> list(final String pfx) throws IOException {
        final Iterable<String> list = this.origin.list(pfx);
        // @checkstyle AnonInnerLengthCheck (50 lines)
        return new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    private final Iterator<String> iter =
                        list.iterator();

                    @Override
                    @RetryOnFailure(verbose = false)
                    public boolean hasNext() {
                        return this.iter.hasNext();
                    }

                    @Override
                    @RetryOnFailure(verbose = false)
                    public String next() {
                        return this.iter.next();
                    }

                    @Override
                    @RetryOnFailure(verbose = false)
                    public void remove() {
                        this.iter.remove();
                    }
                };
            }
        };
    }

    @Override
    public int compareTo(final Bucket bkt) {
        return this.origin.name().compareTo(bkt.name());
    }
}
