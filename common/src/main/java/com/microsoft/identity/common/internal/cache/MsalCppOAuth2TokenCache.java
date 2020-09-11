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
package com.microsoft.identity.common.internal.cache;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.microsoft.identity.common.BaseAccount;
import com.microsoft.identity.common.WarningType;
import com.microsoft.identity.common.adal.internal.util.StringExtensions;
import com.microsoft.identity.common.exception.ClientException;
import com.microsoft.identity.common.internal.dto.AccountRecord;
import com.microsoft.identity.common.internal.dto.Credential;
import com.microsoft.identity.common.internal.dto.CredentialType;
import com.microsoft.identity.common.internal.dto.RefreshTokenRecord;
import com.microsoft.identity.common.internal.logging.Logger;
import com.microsoft.identity.common.internal.providers.oauth2.AuthorizationRequest;
import com.microsoft.identity.common.internal.providers.oauth2.OAuth2Strategy;
import com.microsoft.identity.common.internal.providers.oauth2.TokenResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.microsoft.identity.common.internal.authscheme.BearerAuthenticationSchemeInternal.SCHEME_BEARER;

/**
 * Sub class of {@link MsalCppOAuth2TokenCache} to add specific public api's required for MSAL CPP library.
 */
// Suppressing rawtype warnings due to the generic type OAuth2Strategy, AuthorizationRequest, IAccountCredentialAdapter, MsalCppOAuth2TokenCache and MsalOAuth2TokenCache
@SuppressWarnings(WarningType.rawtype_warning)
public class MsalCppOAuth2TokenCache
        <GenericOAuth2Strategy extends OAuth2Strategy,
                GenericAuthorizationRequest extends AuthorizationRequest,
                GenericTokenResponse extends TokenResponse,
                GenericAccount extends BaseAccount,
                GenericRefreshToken extends com.microsoft.identity.common.internal.providers.oauth2.RefreshToken>
        extends MsalOAuth2TokenCache<
        GenericOAuth2Strategy,
        GenericAuthorizationRequest,
        GenericTokenResponse,
        GenericAccount,
        GenericRefreshToken> {

    private static final String TAG = MsalCppOAuth2TokenCache.class.getName();

    /**
     * Constructor of MsalOAuth2TokenCache.
     *
     * @param context                  Context
     * @param accountCredentialCache   IAccountCredentialCache
     * @param accountCredentialAdapter IAccountCredentialAdapter
     */
    // Suppressing unchecked warnings due to casting of IAccountCredentialAdapter with the generics in the call to the constructor of parent class
    @SuppressWarnings(WarningType.unchecked_warning)
    private MsalCppOAuth2TokenCache(final Context context,
                                    final IAccountCredentialCache accountCredentialCache,
                                    final IAccountCredentialAdapter accountCredentialAdapter) {
        super(context, accountCredentialCache, accountCredentialAdapter);
    }

    /**
     * Factory method for creating an instance of MsalCppOAuth2TokenCache
     * <p>
     * NOTE: Currently this is configured for AAD v2 as the only IDP
     *
     * @param context The Application Context
     * @return An instance of the MsalCppOAuth2TokenCache.
     */
    public static MsalCppOAuth2TokenCache create(@NonNull final Context context) {
        final MsalOAuth2TokenCache msalOAuth2TokenCache = MsalOAuth2TokenCache.create(context);

        // Suppressing unchecked warnings due to the generic types not provided while creating object of MsalCppOAuth2TokenCache
        @SuppressWarnings(WarningType.unchecked_warning)
        MsalCppOAuth2TokenCache msalCppOAuth2TokenCache = new MsalCppOAuth2TokenCache(
                context,
                msalOAuth2TokenCache.getAccountCredentialCache(),
                msalOAuth2TokenCache.getAccountCredentialAdapter()
        );

        return msalCppOAuth2TokenCache;
    }

    /**
     * @param accountRecord : AccountRecord associated with the input credentials, can be null.
     * @param credentials   : list of Credential which can include AccessTokenRecord, IdTokenRecord and RefreshTokenRecord.
     * @throws ClientException : If the supplied Account or Credential are null or schema invalid.
     */
    public synchronized void saveCredentials(@Nullable final AccountRecord accountRecord,
                                             @NonNull final Credential... credentials) throws ClientException {
        if (credentials == null || credentials.length == 0) {
            throw new ClientException("Credential array passed in is null or empty");
        }

        RefreshTokenRecord refreshTokenRecord = null;

        for (final Credential credential : credentials) {
            if (credential instanceof RefreshTokenRecord) {
                refreshTokenRecord = (RefreshTokenRecord) credential;
            }
        }
        if (accountRecord != null && refreshTokenRecord != null) {
            // MSAL C++ writes credentials first and then the account.
            // For a new account, this will not be true as the accountRecord will be null.
            // For existing accounts, we would remove the old refresh token if present.
            removeRefreshTokenIfNeeded(accountRecord, refreshTokenRecord);
        }

        saveCredentialsInternal(credentials);
    }

    /**
     * API to save {@link AccountRecord}
     *
     * @param accountRecord : accountRecord to be saved.
     */
    public synchronized void saveAccountRecord(@NonNull final AccountRecord accountRecord) {
        getAccountCredentialCache().saveAccount(accountRecord);
    }

    /**
     * API to clear all cache.
     * Note: This method is intended to be only used for testing purposes.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public synchronized void clearCache() {
        getAccountCredentialCache().clearAll();
    }

    /**
     * API to inspect cache contents.
     * Note: This method is intended to be only used for testing purposes.
     *
     * @return A immutable List of Credentials contained in this cache.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public synchronized List<Credential> getCredentials() {
        return Collections.unmodifiableList(
                getAccountCredentialCache().getCredentials()
        );
    }

    /**
     * Force remove an AccountRecord matching the supplied criteria.
     *
     * @param homeAccountId HomeAccountId of the Account.
     * @param environment   The Environment of the Account.
     * @param realm         The Realm of the Account.
     * @return An {@link AccountDeletionRecord} containing a receipt of the removed Accounts.
     * @throws ClientException
     */
    @VisibleForTesting // private by default for production code
    public synchronized AccountDeletionRecord forceRemoveAccount(@NonNull final String homeAccountId,
                                                                 @Nullable final String environment,
                                                                 @NonNull final String realm) throws ClientException {
        validateNonNull(homeAccountId, "homeAccountId");
        validateNonNull(realm, "realm");

        final boolean mustMatchOnEnvironment = !StringExtensions.isNullOrBlank(environment);

        final List<AccountRecord> removedAccounts = new ArrayList<>();

        for (final AccountRecord accountRecord : getAllAccounts()) {
            boolean matches = accountRecord.getHomeAccountId().equals(homeAccountId)
                    && accountRecord.getRealm().equals(realm);

            if (mustMatchOnEnvironment) {
                matches = matches && accountRecord.getEnvironment().equals(environment);
            }

            if (matches) {
                // Delete the AccountRecord...
                final boolean accountRemoved = getAccountCredentialCache().removeAccount(accountRecord);

                if (accountRemoved) {
                    removedAccounts.add(accountRecord);
                }
            }
        }

        return new AccountDeletionRecord(removedAccounts);
    }

    /**
     * Method to remove Account matched with homeAccountId, environment and realm
     *
     * @param homeAccountId : HomeAccountId of the Account
     * @param environment   : Environment of the Account
     * @param realm         : Realm of the Account
     * @return {@link AccountDeletionRecord}
     */
    public synchronized AccountDeletionRecord removeAccount(@NonNull final String homeAccountId,
                                                            @Nullable final String environment,
                                                            @NonNull final String realm) throws ClientException {
        // TODO This API is potentially problematic for TFW/TFL...
        // Normally on Android, apps are 'sandboxed' such that each app has their own cache
        // and we don't have to worry about 1 app stomping on another's cache
        //
        // TFW/TFL however, "double stacked" their app registrations into a single binary
        // Such that calling removeAccount() will potentially remove the Account being used by
        // another app.
        //
        // This API assumes the *general* case where an app is single stacked. If special
        // accommodations need to come later for Teams then we can reevaluate the logic here.

        validateNonNull(homeAccountId, "homeAccountId");
        validateNonNull(realm, "realm");

        final List<Credential> credentials = getAccountCredentialCache().getCredentialsFilteredBy(
                homeAccountId,
                environment,
                CredentialType.RefreshToken,
                null,
                realm,
                null,
                SCHEME_BEARER
        );

        if (credentials != null && !credentials.isEmpty()) {
            // Get a client id to use for deletion
            final String clientId = credentials.get(0).getClientId();

            // Remove the account
            return removeAccount(
                    environment,
                    clientId,
                    homeAccountId,
                    realm,
                    CredentialType.AccessToken,
                    CredentialType.AccessToken_With_AuthScheme,
                    CredentialType.IdToken,
                    CredentialType.V1IdToken
            );
        } else {
            // Remove was called, but no RTs exist for the account. Force remove it.
            return forceRemoveAccount(homeAccountId, environment, realm);
        }
    }

    /**
     * Gets an immutable {@link List} of {@link AccountRecord} objects.
     *
     * @return {@link List<AccountRecord>}
     */
    public List<AccountRecord> getAllAccounts() {
        return Collections.unmodifiableList(
                getAccountCredentialCache().getAccounts()
        );
    }

    /**
     * Method to get Account matched with homeAccountId, environment and realm
     *
     * @param homeAccountId : HomeAccountId of the Account
     * @param environment   : Environment of the Account
     * @param realm         : Realm of the Account
     * @return {@link AccountRecord}
     * @throws ClientException : throws ClientException if input validation fails
     */
    @Nullable
    public AccountRecord getAccount(@NonNull final String homeAccountId,
                                    @NonNull final String environment,
                                    @NonNull final String realm) throws ClientException {
        final String methodName = ":getAccount";

        validateNonNull(homeAccountId, "homeAccountId");
        validateNonNull(environment, "environment");
        validateNonNull(realm, "realm");

        final List<AccountRecord> accountRecords = getAccountCredentialCache()
                .getAccountsFilteredBy(homeAccountId, environment, realm);

        if (accountRecords == null || accountRecords.isEmpty()) {
            Logger.info(TAG + methodName,
                    "No account found for the passing in "
                            + "homeAccountId: " + homeAccountId
                            + " environment: " + environment
                            + " realm: " + realm
            );
            return null;
        }

        return accountRecords.get(0);
    }

}
