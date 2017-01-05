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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.s3.Bucket;
import com.jcabi.s3.Ocket;
import com.jcabi.s3.Region;
import java.io.File;
import lombok.EqualsAndHashCode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

/**
 * Mock/fake bucket.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.6
 */
@Immutable
@EqualsAndHashCode(of = "bkt")
@Loggable(Loggable.DEBUG)
public final class MkBucket implements Bucket {

    /**
     * My name.
     */
    private final transient String bkt;

    /**
     * Directory we're working in.
     */
    private final transient String dir;

    /**
     * Ctor.
     * @param file Directory to keep files in
     * @param name Name of the bucket
     */
    public MkBucket(final File file, final String name) {
        this.dir = file.getAbsolutePath();
        this.bkt = name;
    }

    @Override
    public Region region() {
        return new MkRegion(new File(this.dir));
    }

    @Override
    public String name() {
        return this.bkt;
    }

    @Override
    public Ocket ocket(final String key) {
        return new MkOcket(new File(this.dir), this.bkt, key);
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public void remove(final String key) {
        new File(this.home(), key).delete();
    }

    @Override
    public Iterable<String> list(final String pfx) {
        final File home = this.home();
        return Iterables.transform(
            FileUtils.listFiles(
                new File(home, pfx),
                TrueFileFilter.INSTANCE,
                TrueFileFilter.INSTANCE
            ),
            new Function<File, String>() {
                @Override
                public String apply(final File file) {
                    return FilenameUtils.separatorsToUnix(
                        file.getAbsolutePath().substring(
                            home.getAbsolutePath().length() + 1
                        )
                    );
                }
            }
        );
    }

    @Override
    public int compareTo(final Bucket bucket) {
        return this.bkt.compareTo(bucket.name());
    }

    /**
     * Get my file.
     * @return File
     */
    private File home() {
        return new File(new File(this.dir), this.bkt);
    }

}
