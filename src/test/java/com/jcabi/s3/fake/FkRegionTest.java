/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3.fake;

import com.jcabi.s3.Bucket;
import com.jcabi.s3.Ocket;
import com.jcabi.s3.Region;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test case for {@link FkRegion}.
 *
 * @since 0.8.1
 */
@SuppressWarnings("PMD.JUnit5TestShouldBePackagePrivate")
public final class FkRegionTest {

    /**
     * Temp directory.
     * @checkstyle VisibilityModifierCheck (5 lines)
     */
    @Rule
    public final transient TemporaryFolder temp = new TemporaryFolder();

    /**
     * MkRegion can read/write ocket content.
     * @throws Exception If fails
     */
    @Test
    public void readsWritesContentFromFiles() throws Exception {
        final Region region = new FkRegion(this.temp.newFolder());
        final Bucket bucket = region.bucket("test");
        final Ocket ocket = bucket.ocket("hello.txt");
        new Ocket.Text(ocket).write("hello, world!");
        MatcherAssert.assertThat(
            new Ocket.Text(bucket.ocket(ocket.key())).read(),
            Matchers.containsString("world!")
        );
    }

}
