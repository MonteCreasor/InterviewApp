package monte.apps.interviewapp.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by monte on 2016-07-20.
 */

public final class IntentUtils {
    /** Logging tag. */
    private static final String TAG = "IntentUtils";

    private IntentUtils() {
        throw new AssertionError();
    }

    public static Intent getOpenFacebookIntent(
            Context context,
            @NonNull String id,
            String userName) {
        try {
            if (!id.startsWith("fb://page/")) {
                id = "fb://page/".concat(id);
            }
            context.getPackageManager().getPackageInfo("com.facebook.katana", 0);
            return new Intent(Intent.ACTION_VIEW, Uri.parse(id));
        } catch (Exception e) {
            return new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.facebook.com/"
                                      + (!TextUtils.isEmpty(userName) ? userName : "")));
        }
    }

    @Nullable
    public static Intent getCallIntent(Context context, String phoneNumber) {
        return returnIntentOnlyIfResolved(
                context,
                new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null)));
    }

    @Nullable
    public static Intent getDialIntent(Context context, String phoneNumber) {
        return returnIntentOnlyIfResolved(
                context,
                new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null)));
    }

    @Nullable
    public static Intent getTwitterFollowIntent(Context context, String twitterName) {
        Uri uri = Uri.parse("https://twitter.com/intent/follow?user_id=" + twitterName);
        return returnIntentOnlyIfResolved(
                context,
                new Intent(Intent.ACTION_VIEW, uri));
    }

    @Nullable
    public static Intent getMapIntent(Context context, double lat, double lng) {
        Uri uri = Uri.parse("geo:" + lat + "," + lng);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");
        return returnIntentOnlyIfResolved(context, intent);
    }

    @Nullable
    public static Intent getNavigationIntent(Context context, double lat, double lng) {
        Uri uri = Uri.parse("google.navigation:q=" + lat + "," + lng);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");
        return returnIntentOnlyIfResolved(context, intent);
    }

    private static Intent returnIntentOnlyIfResolved(Context context, Intent intent) {
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            return intent;
        }

        Log.w(TAG, "Unable to resolve " + intent.getAction() + " intent");
        return null;
    }
}
