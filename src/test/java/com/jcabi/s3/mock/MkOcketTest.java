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
import com.jcabi.s3.Bucket;
import com.jcabi.s3.Ocket;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test case for {@link MkOcket}.
 *
 * @author Piotr Pradzynski (prondzyn@gmail.com)
 * @version $Id$
 * @since 0.10.1
 */
public final class MkOcketTest {

    /**
     * Temp directory.
     * @checkstyle VisibilityModifierCheck (5 lines)
     */
    @Rule
    public final transient TemporaryFolder temp = new TemporaryFolder();

    /**
     * Commonly used bucket.
     */
    private transient Bucket bucket;

    /**
     * Ocket to write in it.
     */
    private transient Ocket write;

    /**
     * Sets up common part of tests.
     * @throws Exception If fails
     */
    @Before
    public void setUp() throws Exception {
        this.bucket = new MkRegion(this.temp.newFolder()).bucket("test");
        this.write = this.bucket.ocket("hello.txt");
    }

    /**
     * MkOcket can read Content Type and Length from metadata.
     * @throws Exception If fails
     */
    @Test
    public void readsContentTypeAndLengthFromMetadata() throws Exception {
        final String text = "hello, world!";
        new Ocket.Text(this.write).write(text);
        final ObjectMetadata metadata = new Ocket.Text(
            this.bucket.ocket(this.write.key())
        ).meta();
        MatcherAssert.assertThat(
            metadata.getContentType(),
            Matchers.is("text/plain")
        );
        MatcherAssert.assertThat(
            metadata.getContentLength(),
            Matchers.equalTo((long) text.length())
        );
    }

    /**
     * MkOcket can read date from metadata.
     * @throws Exception If fails
     */
    @Test
    public void readsDateFromMetadata() throws Exception {
        final ObjectMetadata metadata = new Ocket.Text(
            this.bucket.ocket(this.write.key())
        ).meta();
        MatcherAssert.assertThat(
            metadata.getRawMetadataValue(Headers.DATE),
            Matchers.notNullValue()
        );
    }

}
