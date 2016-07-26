package monte.apps.interviewapp.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.telecom.TelecomManager;
import android.util.Log;
import android.widget.Toast;

import monte.apps.interviewapp.R;
import monte.apps.interviewapp.views.TouchPointManager;

/**
 * Created by monte on 2016-07-26.
 */

public class ActivityUtils {
    /** Logging tag. */
    private static final String TAG = "ActivityUtils";

    /**
     * Attempts to start an activity and displays a toast with a provided error message if the
     * activity is not found, instead of throwing an exception.
     *
     * @param context to start the activity with.
     * @param intent to start the activity with.
     */
    public static void startActivityWithErrorToast(Context context, Intent intent) {
        try {
            if (Intent.ACTION_CALL.equals(intent.getAction())) {
                // All dialer-initiated calls should pass the touch point to the InCallUI
                if (TouchPointManager.getInstance().hasValidPoint()) {
                    Bundle extras = new Bundle();
                    extras.putParcelable(TouchPointManager.TOUCH_POINT,
                                         TouchPointManager.getInstance().getPoint());
                    intent.putExtra(TelecomManager.EXTRA_OUTGOING_CALL_EXTRAS, extras);
                }
                ((Activity) context).startActivityForResult(intent, 0);
            } else {
                context.startActivity(intent);
            }
        } catch (SecurityException ex) {
            Toast.makeText(context, R.string.start_activity_no_permission, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "App does not have permission to launch " + intent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(context, R.string.start_activity_missing_app, Toast.LENGTH_SHORT).show();
        }
    }
}
