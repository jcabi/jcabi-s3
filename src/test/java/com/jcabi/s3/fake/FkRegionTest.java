/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3.fake;

import com.jcabi.s3.Bucket;
import com.jcabi.s3.Ocket;
import com.jcabi.s3.Region;
import java.io.File;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test case for {@link FkRegion}.
 *
 * @since 0.8.1
 */
final class FkRegionTest {

    @Test
    void readsWritesContentFromFiles(@TempDir final File temp)
        throws Exception {
        final Region region = new FkRegion(temp);
        final Bucket bucket = region.bucket("test");
        final Ocket ocket = bucket.ocket("hello.txt");
        new Ocket.Text(ocket).write("hello, world!");
        MatcherAssert.assertThat(
            "should contains string",
            new Ocket.Text(bucket.ocket(ocket.key())).read(),
            Matchers.containsString("world!")
        );
    }

}
