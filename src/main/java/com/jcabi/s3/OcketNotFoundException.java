/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3;

import java.io.IOException;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * When ocket is not found in bucket.
 *
 * @since 0.1
 */
public class OcketNotFoundException extends IOException {

    /**
     * Serialization marker.
     */
    private static final long serialVersionUID = -2854185226779232378L;

    /**
     * Public ctor.
     * @param msg Message to show
     * @param cause Cause of it
     */
    public OcketNotFoundException(final String msg,
        final S3Exception cause) {
        super(msg, cause);
    }
}
