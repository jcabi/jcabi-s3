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
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
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
    public Bucket bucket() {
        return new CdBucket(this.origin.bucket());
    }

    @Override
    public String key() {
        return this.origin.key();
    }

    @Override
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
