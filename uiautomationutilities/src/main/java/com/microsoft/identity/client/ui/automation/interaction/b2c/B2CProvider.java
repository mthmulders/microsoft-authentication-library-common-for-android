//  Copyright (c) Microsoft Corporation.
//  All rights reserved.
//
//  This code is licensed under the MIT License.
//
//  Permission is hereby granted, free of charge, to any person obtaining a copy
//  of this software and associated documentation files(the "Software"), to deal
//  in the Software without restriction, including without limitation the rights
//  to use, copy, modify, merge, publish, distribute, sublicense, and / or sell
//  copies of the Software, and to permit persons to whom the Software is
//  furnished to do so, subject to the following conditions :
//
//  The above copyright notice and this permission notice shall be included in
//  all copies or substantial portions of the Software.
//
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//  THE SOFTWARE.
package com.microsoft.identity.client.ui.automation.interaction.b2c;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.microsoft.identity.internal.testutils.labutils.LabConstants;

import lombok.Getter;

/**
 * A class modeling a B2C Provider that is being used during a B2C UI Test
 */
@Getter
public abstract class B2CProvider {

    @NonNull
    private String providerName;

    @Nullable // should be null for LOCAL B2C provider
    private String idpSelectionBtnResourceId;

    @Nullable // should be null for LOCAL B2C provider
    private String domainHint; // this can be used as query param to /authorize endpoint

    public B2CProvider(@NonNull final String providerName,
                       @Nullable final String idpSelectionBtnResourceId,
                       @Nullable final String domainHint) {
        this.providerName = providerName;
        this.idpSelectionBtnResourceId = idpSelectionBtnResourceId;
        this.domainHint = domainHint;
    }

    /**
     * Indicates if the B2C Provider is a non-local IdP
     *
     * @return a boolean indicating if B2C Provider is an external IdP
     */
    protected boolean isExternalIdp() {
        return !(this instanceof Local);
    }

    public static class Local extends B2CProvider {
        public Local() {
            super(LabConstants.B2CProvider.LOCAL, null, null);
        }
    }

    public static class Google extends B2CProvider {
        public Google() {
            super(LabConstants.B2CProvider.GOOGLE,
                    "GoogleExchange",
                    "google.com"
            );
        }
    }

    public static class Facebook extends B2CProvider {
        public Facebook() {
            super(LabConstants.B2CProvider.FACEBOOK,
                    "FacebookExchange",
                    "facebook.com"
            );
        }
    }

    public static class Microsoft extends B2CProvider {
        public Microsoft() {
            super(LabConstants.B2CProvider.MICROSOFT,
                    "MicrosoftAccountExchange",
                    "microsoft.com"
            );
        }
    }
}
