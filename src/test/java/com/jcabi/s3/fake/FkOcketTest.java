/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3.fake;

import com.jcabi.s3.Bucket;
import com.jcabi.s3.Ocket;
import java.io.File;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

/**
 * Test case for {@link FkOcket}.
 *
 * @since 0.10.1
 */
final class FkOcketTest {

    @Test
    void readsContentTypeAndLengthFromMetadata(@TempDir final File temp)
        throws Exception {
        final Bucket bucket = new FkRegion(temp).bucket("test");
        final Ocket write = bucket.ocket("hello.txt");
        final String text = "hello, world!";
        new Ocket.Text(write).write(text);
        final HeadObjectResponse metadata = new Ocket.Text(
            bucket.ocket(write.key())
        ).meta();
        MatcherAssert.assertThat(
            "should be text/plain content-type",
            metadata.contentType(),
            Matchers.is("text/plain")
        );
        MatcherAssert.assertThat(
            "should be equal to content length",
            metadata.contentLength(),
            Matchers.equalTo((long) text.length())
        );
    }

    @Test
    void readsDateFromMetadata(@TempDir final File temp) throws Exception {
        final Bucket bucket = new FkRegion(temp).bucket("test");
        final Ocket write = bucket.ocket("hello.txt");
        final HeadObjectResponse metadata = new Ocket.Text(
            bucket.ocket(write.key())
        ).meta();
        MatcherAssert.assertThat(
            "should be not null",
            metadata.lastModified(),
            Matchers.notNullValue()
        );
    }

}
