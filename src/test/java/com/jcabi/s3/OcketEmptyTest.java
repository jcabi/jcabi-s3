/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

/**
 * Test case for {@link Ocket.Empty}.
 *
 * @since 0.1
 */
final class OcketEmptyTest {

    @Test
    void returnsKeyNamedEmpty() {
        MatcherAssert.assertThat(
            "key was not 'empty'",
            new Ocket.Empty().key(),
            Matchers.equalTo("empty")
        );
    }

    @Test
    void reportsThatItExists() {
        MatcherAssert.assertThat(
            "empty ocket did not report existence",
            new Ocket.Empty().exists(),
            Matchers.is(true)
        );
    }

    @Test
    void returnsNonNullMeta() {
        MatcherAssert.assertThat(
            "meta was null for empty ocket",
            new Ocket.Empty().meta(),
            Matchers.notNullValue()
        );
    }

    @Test
    void readsNothingToOutputStream() throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        new Ocket.Empty().read(output);
        MatcherAssert.assertThat(
            "output was not empty after reading empty ocket",
            output.size(),
            Matchers.equalTo(0)
        );
    }

    @Test
    void acceptsWriteWithoutAction() throws Exception {
        final Ocket ocket = new Ocket.Empty();
        ocket.write(
            new ByteArrayInputStream(
                UUID.randomUUID().toString()
                    .getBytes(StandardCharsets.UTF_8)
            ),
            HeadObjectResponse.builder().build()
        );
        MatcherAssert.assertThat(
            "ocket did not still exist after write",
            ocket.exists(),
            Matchers.is(true)
        );
    }

    @Test
    void throwsOnBucketAccess() {
        Assertions.assertThrows(
            UnsupportedOperationException.class,
            () -> new Ocket.Empty().bucket(),
            "bucket access did not throw"
        );
    }

    @Test
    void comparesToZero() {
        MatcherAssert.assertThat(
            "compareTo did not return zero",
            new Ocket.Empty().compareTo(new Ocket.Empty()),
            Matchers.equalTo(0)
        );
    }

}
