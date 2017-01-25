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
package com.jcabi.s3.mock;

import com.amazonaws.services.s3.Headers;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.s3.Bucket;
import com.jcabi.s3.Ocket;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import javax.activation.MimetypesFileTypeMap;
import lombok.EqualsAndHashCode;

/**
 * Mock/fake ocket.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.6
 */
@Immutable
@EqualsAndHashCode(of = { "bkt", "name" })
@Loggable(Loggable.DEBUG)
public final class MkOcket implements Ocket {

    /**
     * Directory we're working in.
     */
    private final transient String dir;

    /**
     * My bucket.
     */
    private final transient String bkt;

    /**
     * My name.
     */
    private final transient String name;

    /**
     * Ctor.
     * @param file Dir we're in
     * @param bucket Bucket
     * @param key Key
     */
    public MkOcket(final File file, final String bucket, final String key) {
        this.dir = file.getAbsolutePath();
        this.bkt = bucket;
        this.name = key;
    }

    @Override
    public Bucket bucket() {
        return new MkBucket(new File(this.dir), this.bkt);
    }

    @Override
    public String key() {
        return this.name;
    }

    @Override
    public ObjectMetadata meta() {
        final ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(this.file().length());
        final MimetypesFileTypeMap types = new MimetypesFileTypeMap();
        meta.setContentType(types.getContentType(this.file()));
        meta.setHeader(Headers.DATE, new Date());
        meta.setLastModified(new Date());
        meta.setCacheControl("");
        meta.setContentEncoding("UTF-8");
        meta.setContentMD5("abcdef");
        meta.setExpirationTime(new Date());
        return meta;
    }

    @Override
    public boolean exists() {
        return this.file().exists();
    }

    @Override
    public void read(final OutputStream output) throws IOException {
        final InputStream input = new FileInputStream(this.file());
        try {
            while (input.available() > 0) {
                output.write(input.read());
            }
        } finally {
            input.close();
            output.close();
        }
    }

    @Override
    public void write(final InputStream input, final ObjectMetadata meta)
        throws IOException {
        final File file = this.file();
        file.getParentFile().mkdirs();
        final OutputStream output = new FileOutputStream(file);
        try {
            while (input.available() > 0) {
                output.write(input.read());
            }
        } finally {
            output.close();
            input.close();
        }
    }

    @Override
    public int compareTo(final Ocket ocket) {
        return this.name.compareTo(ocket.key());
    }

    /**
     * Get my file.
     * @return File
     */
    private File file() {
        return new File(
            new File(
                new File(this.dir),
                this.bkt
            ),
            this.name
        );
    }

}
