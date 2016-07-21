package monte.apps.interviewapp.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import monte.apps.interviewapp.activities.DetailsActivity;

/**
 * Created by monte on 2016-07-20.
 */

public final class IntentUtils {
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
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null));
        return intent.resolveActivity(context.getPackageManager()) != null ? intent: null;
    }

    @Nullable
    public static Intent getDialIntent(Context context, String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null));
        return intent.resolveActivity(context.getPackageManager()) != null ? intent: null;
    }

    @Nullable
    public static Intent getTwitterFollowIntent(Context context, String twitterName) {
        Uri uri = Uri.parse("https://twitter.com/intent/follow?user_id=" + twitterName);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        return intent.resolveActivity(context.getPackageManager()) != null ? intent: null;
    }

    @Nullable
    public static Intent getMapIntent(Context context, double lat, double lng) {
        Uri uri = Uri.parse("geo:" + lat + "," + lng);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");
        return intent.resolveActivity(context.getPackageManager()) != null ? intent: null;
    }

    @Nullable
    public static Intent getNavigationIntent(Context context, double lat, double lng) {
        Uri uri = Uri.parse("google.navigation:q=" + lat + "," + lng);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");
        return intent.resolveActivity(context.getPackageManager()) != null ? intent: null;
    }
}
