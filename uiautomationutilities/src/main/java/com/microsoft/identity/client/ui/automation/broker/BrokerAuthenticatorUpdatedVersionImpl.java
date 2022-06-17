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
package com.microsoft.identity.client.ui.automation.broker;

import static com.microsoft.identity.client.ui.automation.utils.CommonUtils.FIND_UI_ELEMENT_TIMEOUT;

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import com.microsoft.identity.client.ui.automation.TestContext;
import com.microsoft.identity.client.ui.automation.interaction.PromptHandlerParameters;
import com.microsoft.identity.client.ui.automation.interaction.PromptParameter;
import com.microsoft.identity.client.ui.automation.interaction.microsoftsts.AadPromptHandler;
import com.microsoft.identity.client.ui.automation.logging.Logger;
import com.microsoft.identity.client.ui.automation.utils.UiAutomatorUtils;

import org.junit.Assert;

/**
 * A model for interacting with the Microsoft Authenticator Broker App during UI Test
 * when version number of Authenticator app under test is >= "6.2206.3949"
 * contains changes for new resourceIds for device registration UI
 */
public class BrokerAuthenticatorUpdatedVersionImpl extends BrokerMicrosoftAuthenticator {

    private static final String TAG = BrokerAuthenticatorUpdatedVersionImpl.class.getSimpleName();
    private static final boolean isExpectingMFA = true;

    @Override
    public void performDeviceRegistration(@NonNull final String username,
                                          @NonNull final String password) {

        Logger.i(TAG, "Performing Device Registration for the given account..");
        if (isExpectingMFA) {
            TestContext.getTestContext().getTestDevice().getSettings().addWorkAccount(
                    this,
                    username,
                    password
            );
        }
        else {
            performDeviceRegistrationHelper(
                    username,
                    password,
                    "workPlaceTextField",
                    "workPlaceRegisterButton"
            );


            try {
                // after device registration, make sure that we see the unregister btn to confirm successful
                // registration
                final UiObject unRegisterBtn = UiAutomatorUtils.obtainUiObjectWithResourceId(
                        "com.azure.authenticator:id/unregister_button"
                );
                Assert.assertTrue(
                        "Microsoft Authenticator - Unregister Button appears.",
                        unRegisterBtn.exists()
                );

                Assert.assertTrue(
                        "Microsoft Authenticator - Unregister Button is clickable.",
                        unRegisterBtn.isClickable()
                );

                // after device registration, make sure that the current registration upn matches with
                // with what was passed in
                final UiObject currentRegistration = UiAutomatorUtils.obtainUiObjectWithResourceId(
                        "com.azure.authenticator:id/current_registered_email"
                );

                Assert.assertTrue(
                        "Microsoft Authenticator - Registered account info appears.",
                        currentRegistration.exists()
                );

                Assert.assertTrue(
                        "Microsoft Authenticator - Registered account upn matches provided upn.",
                        currentRegistration.getText().equalsIgnoreCase(username)
                );
            } catch (final UiObjectNotFoundException e) {
                throw new AssertionError(e);
            }
        }
    }

    public void performSharedDeviceRegistration(@NonNull final String username,
                                                @NonNull final String password) {
        Logger.i(TAG, "Performing Shared Device Registration for the given account..");
        performDeviceRegistrationHelper(
                username,
                password,
                "sharedWorkPlaceTextField",
                "sharedWorkPlaceRegisterButton"
        );

        final UiDevice device =
                UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        final UiSelector sharedDeviceConfirmationSelector = new UiSelector()
                .descriptionContains("Shared Device Mode")
                .className("android.widget.ImageView");

        //confirm that we are in Shared Device Mode inside Authenticator
        final UiObject sharedDeviceConfirmation = device.findObject(sharedDeviceConfirmationSelector);
        sharedDeviceConfirmation.waitForExists(FIND_UI_ELEMENT_TIMEOUT);
        Assert.assertTrue(
                "Microsoft Authenticator - Shared Device Confirmation page appears.",
                sharedDeviceConfirmation.exists());

        isInSharedDeviceMode = true;
    }

    private void performDeviceRegistrationHelper(@NonNull final String username,
                                                 @NonNull final String password,
                                                 @NonNull final String emailInputResourceId,
                                                 @NonNull final String registerBtnResourceId) {
        Logger.i(TAG, "Execution of Helper for Device Registration..");
        // open device registration page
        openDeviceRegistrationPage();

        // enter email
        UiAutomatorUtils.handleInput(
                emailInputResourceId,
                username
        );

        // click register
        UiAutomatorUtils.handleButtonClick(registerBtnResourceId);

        final PromptHandlerParameters promptHandlerParameters = PromptHandlerParameters.builder()
                .prompt(PromptParameter.LOGIN)
                .broker(this)
                .consentPageExpected(false)
                .expectingBrokerAccountChooserActivity(false)
                .expectingLoginPageAccountPicker(false)
                .sessionExpected(false)
                .loginHint(username)
                .build();

        final AadPromptHandler aadPromptHandler = new AadPromptHandler(promptHandlerParameters);

        Logger.i(TAG, "Handle AAD Login page prompt for Device Registration..");
        // handle AAD login page
        aadPromptHandler.handlePrompt(username, password);
    }

    @Override
    protected void goToDeviceRegistrationPage() {
        // scroll down the recycler view to find device registration btn
        try {
            // click the 3 dot menu icon in top right
            UiAutomatorUtils.handleButtonClick("com.azure.authenticator:id/menu_overflow");

            // select Settings from drop down
            final UiObject settings = UiAutomatorUtils.obtainUiObjectWithText("Settings");
            settings.click();

            final UiObject deviceRegistration = UiAutomatorUtils.obtainChildInScrollable(
                    "settingsScrollView",
                    "Device Registration"
            );

            assert deviceRegistration != null;

            // click the device registration button
            deviceRegistration.click();
        } catch (final UiObjectNotFoundException e) {
            throw new AssertionError(e);
        }
    }
}
