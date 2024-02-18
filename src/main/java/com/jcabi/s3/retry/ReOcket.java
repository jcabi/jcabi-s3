/*
 * Copyright (c) 2012-2024, jcabi.com
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
