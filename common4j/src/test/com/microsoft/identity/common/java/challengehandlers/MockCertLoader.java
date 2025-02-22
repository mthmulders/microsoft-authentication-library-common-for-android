// Copyright (c) Microsoft Corporation.
// All rights reserved.
//
// This code is licensed under the MIT License.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files(the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and / or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions :
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
package com.microsoft.identity.common.java.challengehandlers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.microsoft.identity.common.java.exception.ClientException;
import com.microsoft.identity.common.java.util.JWSBuilder;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(prefix = "m")
public class MockCertLoader implements IDeviceCertificateLoader{

    private final String MOCK_CERT_THUMBPRINT = "thumbprint1234";

    private boolean mValidIssuer = true;
    private PrivateKey mPrivateKey = mock(PrivateKey.class);
    private PublicKey mPublicKey = mock(PublicKey.class);
    private X509Certificate mCert = mock(X509Certificate.class);

    @Nullable
    @Override
    public IDeviceCertificate loadCertificate(@Nullable String tenantId) {
        return new IDeviceCertificate() {
            @Override
            public boolean isValidIssuer(List<String> certAuthorities) {
                return mValidIssuer;
            }

            @Override
            public X509Certificate getCertificate() {
                return mCert;
            }

            @Override
            public PrivateKey getPrivateKey() {
                return mPrivateKey;

            }

            @Override
            public PublicKey getPublicKey() {
                return mPublicKey;

            }

            @Override
            public String getThumbPrint() {
                return MOCK_CERT_THUMBPRINT;
            }
        };
    }

    /**
     * Generates a signed jwt based on the mock objects.
     *
     * @param nonce PKeyAuth nonce.
     * @param submitUrl PKeyAuth submitUrl.
     */
    public String getMockSignedJwt(@NonNull final String nonce,
                                   @NonNull final String submitUrl){
        return nonce + ":" +
                submitUrl + ":" +
                mPrivateKey.hashCode() + ":" +
                mPublicKey.hashCode() + ":" +
                mCert.hashCode();
    }

    /**
     * Returns a mock jws builder, which returns a signed jwt based on the mock objects
     * (If the matching objects are provided).
     *
     * @param nonce PKeyAuth nonce.
     * @param submitUrl PKeyAuth submitUrl.
     */
    public JWSBuilder getMockJwsBuilder(@NonNull final String nonce,
                                        @NonNull final String submitUrl) throws ClientException {
        final JWSBuilder mockJwsBuilder = mock(JWSBuilder.class);
        when(
                mockJwsBuilder.generateSignedJWT(
                        nonce,
                        submitUrl,
                        mPrivateKey,
                        mPublicKey,
                        mCert
                )
        ).thenReturn(getMockSignedJwt(nonce, submitUrl));
        return mockJwsBuilder;
    }
}
