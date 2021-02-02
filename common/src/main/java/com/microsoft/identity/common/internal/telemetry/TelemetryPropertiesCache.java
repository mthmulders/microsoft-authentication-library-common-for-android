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
package com.microsoft.identity.common.internal.telemetry;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.microsoft.identity.common.internal.cache.SharedPreferencesFileManager;

import java.util.UUID;

/**
 * Tracks properties used in telemetry.
 */
class TelemetryPropertiesCache {

    private static final String SHARED_PREFS_NAME = "com.microsoft.common.telemetry-properties";

    /**
     * Cached properties.
     */
    private static class Properties {

        /**
         * The randomly generated identifier for this device.
         */
        static final String DEVICE_ID_GUID = "device_id_guid";
    }

    private final SharedPreferencesFileManager mSharedPrefs;

    TelemetryPropertiesCache(@NonNull final Context context) {
        mSharedPrefs = new SharedPreferencesFileManager(context, SHARED_PREFS_NAME);
    }

    /**
     * Gets or creates the stable device id for this installation.
     *
     * @return The String ID used to refer to this device.
     */
    synchronized String getOrCreateRandomStableDeviceId() {
        String deviceId = mSharedPrefs.getString(Properties.DEVICE_ID_GUID);

        if (TextUtils.isEmpty(deviceId)) {
            deviceId = UUID.randomUUID().toString();
            mSharedPrefs.putString(Properties.DEVICE_ID_GUID, deviceId);
        }

        return deviceId;
    }

}
