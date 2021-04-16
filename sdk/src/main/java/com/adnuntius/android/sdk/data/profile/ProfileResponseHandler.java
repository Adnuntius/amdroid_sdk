package com.adnuntius.android.sdk.data.profile;

import com.adnuntius.android.sdk.http.ErrorResponse;

public interface ProfileResponseHandler {
    void onSuccess(final Profile response);
    void onFailure(final ErrorResponse response);
}
