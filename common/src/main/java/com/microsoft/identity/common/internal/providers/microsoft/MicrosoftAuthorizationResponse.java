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
package com.microsoft.identity.common.internal.providers.microsoft;

import com.google.gson.annotations.Expose;
import com.microsoft.identity.common.internal.providers.oauth2.AuthorizationResponse;

import java.net.URL;

/**
 * Sub class of {@link AuthorizationResponse} which encapsulates additional parameters
 * specific to Microsoft in addition to the default OAuth2 AuthorizationResponse.
 */
public class MicrosoftAuthorizationResponse extends AuthorizationResponse {

    public final static String CLOUD_INSTANCE_NAME = "cloud_instance_name";
    public final static String CLOUD_INSTANCE_HOST_NAME = "cloud_instance_host_name";
    public final static String CLOUD_GRAPH_HOST_NAME = "cloud_graph_host_name";
    public final static String SESSION_STATE = "session_state";

    public final static String DEVICE_CODE = "device_code";
    public final static String USER_CODE = "user_code";
    public final static String VERIFICATION_URI = "verification_uri";
    public final static String EXPIRES_IN = "expires_in";
    public final static String INTERVAL = "interval";
    public final static String MESSAGE = "message";

    @Expose()
    protected String mCorrelationId;
    @Expose()
    protected String mCloudInstanceName;
    @Expose()
    protected String mCloudInstanceHostName;
    @Expose()
    protected String mCloudGraphHostName;
    @Expose()
    protected String mSessionState;

    // Device Code Flow Fields
    @Expose()
    protected String mDeviceCode;
    @Expose()
    protected String mUserCode;
    @Expose()
    protected String mVerificationUri;
    @Expose()
    protected String mExpiresIn;
    @Expose()
    protected String mInterval;
    @Expose()
    protected String mMessage;

    /**
     * Constructor of {@link MicrosoftAuthorizationResponse}.
     *
     * @param code  The authorization code generated by the authorization server.
     * @param state "state" parameter from the client authorization request.
     */
    public MicrosoftAuthorizationResponse(String code, String state) {
        super(code, state);
    }

    /**
     * Getter method for correlation id.
     *
     * @return correlation id of the request.
     */
    public String getCorrelationId() {
        return mCorrelationId;
    }

    /**
     * Setter method for correlation id.
     *
     * @param correlationId correlation id of the request.
     */
    public void setCorrelationId(final String correlationId) {
        mCorrelationId = correlationId;
    }

    public String getCloudInstanceName() { return mCloudInstanceName;}

    public String getCloudInstanceHostName() { return mCloudInstanceHostName;}

    public String getCloudGraphHostName() { return mCloudGraphHostName;}

    public String getSessionState() { return mSessionState;}

    /**
     * Getter method for the device code used in Device Code Flow.
     *
     * @return device code of the request (null in non-DCF cases).
     */
    public String getDeviceCode() { return mDeviceCode;}

    /**
     * Getter method for the user code used in Device Code Flow.
     *
     * @return user code of the request (null in non-DCF cases).
     */
    public String getUserCode() { return mUserCode;}

    /**
     * Getter method for the verification uri used in Device Code Flow.
     *
     * @return verification uri of the request (null in non-DCF cases).
     */
    public String getVerificationUri() { return mVerificationUri;}

    /**
     * Getter method for the expiration interval used in Device Code Flow.
     *
     * @return expiration interval of the request (null in non-DCF cases).
     */
    public String getExpiresIn() { return mExpiresIn;}

    /**
     * Getter method for the polling waiting interval used in Device Code Flow.
     *
     * @return waiting interval of the request (null in non-DCF cases).
     */
    public String getInterval() { return mInterval;}

    /**
     * Getter method for the authentication message used in Device Code Flow.
     *
     * @return message of the request (null in non-DCF cases).
     */
    public String getMessage() { return mMessage;}
}
