/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.s3;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import java.io.IOException;

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
        final AmazonS3Exception cause) {
        super(msg, cause);
    }
}
