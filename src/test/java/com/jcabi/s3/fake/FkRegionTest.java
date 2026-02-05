/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3.fake;

import com.jcabi.s3.Bucket;
import com.jcabi.s3.Ocket;
import com.jcabi.s3.Region;
import java.io.File;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
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

    @Test
    void throwsOnAwsAccess(@TempDir final File temp) {
        Assertions.assertThrows(
            UnsupportedOperationException.class,
            () -> new FkRegion(temp).aws(),
            "aws() did not throw"
        );
    }

    @Test
    void returnsBucketByName(@TempDir final File temp) {
        MatcherAssert.assertThat(
            "bucket was not returned",
            new FkRegion(temp).bucket(UUID.randomUUID().toString()),
            Matchers.notNullValue()
        );
    }

    @Test
    void rejectsNonDirectoryFile(@TempDir final File temp) throws Exception {
        final File file = new File(
            temp, UUID.randomUUID().toString()
        );
        file.createNewFile();
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new FkRegion(file),
            "non-directory file was not rejected"
        );
    }

    @Test
    void representsItselfAsString(@TempDir final File temp) {
        MatcherAssert.assertThat(
            "string representation did not contain directory path",
            new FkRegion(temp).toString(),
            Matchers.containsString(temp.getName())
        );
    }

}
