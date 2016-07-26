package monte.apps.interviewapp.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import monte.apps.interviewapp.App;
import monte.apps.interviewapp.views.TouchPointManager;

/**
 * Created by monte on 2016-07-26.
 */

public class PhoneUtils {
    /**
     * Logging tag.
     */
    private static final String TAG = "PhoneUtils";

    private PhoneUtils() {
        throw new AssertionError();
    }

    public static PhoneCallListener installPhoneStateListener(Activity activity) {
        // add PhoneStateListener
        PhoneCallListener phoneListener = new PhoneCallListener(activity);
        TelephonyManager telephonyManager =
                (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);

        return phoneListener;
    }

    public static void uninstallPhoneStateListener(
            Context context,
            PhoneStateListener phoneStateListener) {
        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
    }

    private static class PhoneCallListener extends PhoneStateListener {
        private WeakReference<Activity> mActivityRef;
        private boolean isPhoneCalling = false;

        public PhoneCallListener(Activity activity) {
            mActivityRef = new WeakReference<>(activity);
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.d(TAG, "onCallStateChanged: RINGING (" + incomingNumber + ")");
                    break;

                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.d(TAG, "onCallStateChanged: OFFHOOK");
                    isPhoneCalling = true;
                    break;

                case TelephonyManager.CALL_STATE_IDLE:
                    // run when class initial and phone call ended,
                    // need detect flag from CALL_STATE_OFFHOOK
                    Log.d(TAG, "onCallStateChanged: IDLE");

                    if (isPhoneCalling) {
                        Log.d(TAG, "onCallStateChanged: restarting activity");

                        // restart app
                        Activity activity = mActivityRef.get();
                        if (activity != null) {
                            Intent intent =
                                    activity.getBaseContext()
                                            .getPackageManager()
                                            .getLaunchIntentForPackage(
                                                    activity.getBaseContext().getPackageName());
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            activity.startActivity(intent);
                        }

                        isPhoneCalling = false;
                    }
            }
        }
    }
}
