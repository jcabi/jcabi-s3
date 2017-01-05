/**
 * Copyright (c) 2012-2017, jcabi.com
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
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.5
 */
@Immutable
@EqualsAndHashCode(of = "origin")
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.TooManyMethods")
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
                final Iterator<String> iterator = list.iterator();
                return new Iterator<String>() {
                    @Override
                    @RetryOnFailure(verbose = false)
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }
                    @Override
                    @RetryOnFailure(verbose = false)
                    public String next() {
                        return iterator.next();
                    }
                    @Override
                    @RetryOnFailure(verbose = false)
                    public void remove() {
                        iterator.remove();
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
