/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3.retry;

import com.jcabi.s3.Bucket;
import com.jcabi.s3.Ocket;
import com.jcabi.s3.Region;
import com.jcabi.s3.fake.FkRegion;
import java.io.File;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test case for {@link ReRegion}.
 *
 * @since 0.5
 */
final class ReRegionTest {

    @Test
    void delegatesBucketCreation(@TempDir final File temp) {
        final String name = UUID.randomUUID().toString();
        MatcherAssert.assertThat(
            "bucket name was not delegated to origin",
            new ReRegion(new FkRegion(temp)).bucket(name).name(),
            Matchers.equalTo(name)
        );
    }

    @Test
    void delegatesToStringToOrigin(@TempDir final File temp) {
        final Region origin = new FkRegion(temp);
        MatcherAssert.assertThat(
            "toString was not delegated to origin",
            new ReRegion(origin).toString(),
            Matchers.equalTo(origin.toString())
        );
    }

    @Test
    void readsAndWritesThroughDecoratedBucket(@TempDir final File temp)
        throws Exception {
        final Region region = new ReRegion(new FkRegion(temp));
        final Bucket bucket = region.bucket(UUID.randomUUID().toString());
        final String key = String.format("%s.txt", UUID.randomUUID());
        final String content = String.format("héllo-%s", UUID.randomUUID());
        new Ocket.Text(bucket.ocket(key)).write(content);
        MatcherAssert.assertThat(
            "content was not read through decorated bucket",
            new Ocket.Text(bucket.ocket(key)).read(),
            Matchers.equalTo(content)
        );
    }

}
