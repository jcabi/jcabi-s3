/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3.fake;

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
 * Test case for {@link FkOcket}.
 *
 * @since 0.10.1
 */
@SuppressWarnings("PMD.JUnit5TestShouldBePackagePrivate")
public final class FkOcketTest {

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
        this.bucket = new FkRegion(this.temp.newFolder()).bucket("test");
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
