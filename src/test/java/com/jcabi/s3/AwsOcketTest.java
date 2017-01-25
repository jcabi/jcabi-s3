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
package com.jcabi.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link AwsOcket}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.1
 */
public final class AwsOcketTest {

    /**
     * AwsOcket can read ocket content.
     * @throws Exception If fails
     */
    @Test
    public void readsContentFromAwsObject() throws Exception {
        final String content = "some text \u20ac\n\t\rtest";
        final S3Object object = new S3Object();
        object.setObjectContent(
            IOUtils.toInputStream(content, StandardCharsets.UTF_8)
        );
        final AmazonS3 aws = Mockito.mock(AmazonS3.class);
        Mockito.doReturn(object).when(aws)
            .getObject(Mockito.any(GetObjectRequest.class));
        final Region region = Mockito.mock(Region.class);
        Mockito.doReturn(aws).when(region).aws();
        final Bucket bucket = Mockito.mock(Bucket.class);
        Mockito.doReturn(region).when(bucket).region();
        final Ocket ocket = new AwsOcket(bucket, "test.txt");
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ocket.read(baos);
        MatcherAssert.assertThat(
            baos.toString(StandardCharsets.UTF_8.name()),
            Matchers.equalTo(content)
        );
    }

    /**
     * AwsOcket can write ocket content.
     * @throws Exception If fails
     */
    @Test
    public void writesContentToAwsObject() throws Exception {
        final AmazonS3 aws = Mockito.mock(AmazonS3.class);
        Mockito.doReturn(new PutObjectResult()).when(aws).putObject(
            Mockito.any(PutObjectRequest.class)
        );
        final Region region = Mockito.mock(Region.class);
        Mockito.doReturn(aws).when(region).aws();
        final Bucket bucket = Mockito.mock(Bucket.class);
        Mockito.doReturn(region).when(bucket).region();
        final Ocket ocket = new AwsOcket(bucket, "test-3.txt");
        final String content = "text \u20ac\n\t\rtest";
        ocket.write(
            new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)),
            new ObjectMetadata()
        );
        Mockito.verify(aws).putObject(Mockito.any(PutObjectRequest.class));
    }

    /**
     * AwsOcket can throw if object not found.
     * @throws Exception If fails
     */
    @Test(expected = OcketNotFoundException.class)
    public void throwsWhenObjectNotFound() throws Exception {
        final AmazonS3 aws = Mockito.mock(AmazonS3.class);
        Mockito.doThrow(new AmazonS3Exception("")).when(aws).getObjectMetadata(
            Mockito.any(GetObjectMetadataRequest.class)
        );
        final Region region = Mockito.mock(Region.class);
        Mockito.doReturn(aws).when(region).aws();
        final Bucket bucket = Mockito.mock(Bucket.class);
        Mockito.doReturn(region).when(bucket).region();
        final Ocket ocket = new AwsOcket(bucket, "test-99.txt");
        ocket.meta();
    }

}
