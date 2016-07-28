/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package monte.apps.interviewapp.permissions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Trace;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import monte.apps.interviewapp.R;

/**
 * Activity that asks the user for all {@link #getDesiredPermissions} if any of {@link
 * #getRequiredPermissions} are missing.
 * <p>
 * NOTE: This can behave oddly in the case where the final permission you are requesting causes an
 * application restart.
 */
public abstract class RequestPermissionsActivityBase extends Activity {
    public static final String ACTIVITY_INTENT = "previous_intent";
    /**
     * Logging tag.
     */
    private static final String TAG = "PermissionsActivityBase";
    private static final int PERMISSIONS_REQUEST_ALL_PERMISSIONS = 1;
    private Intent mActivityIntent;

    /**
     * If any permissions the app needs are missing, open an Activity to prompt the user for these
     * permissions. Moreover, if required finish the current activity.
     * <p>
     * This is designed to be called inside {@link android.app.Activity#onCreate}
     */
    protected static boolean startPermissionActivity(
            Activity activity,
            String[] requiredPermissions,
            Class<?> subClass) {
        if (!RequestPermissionsActivity.hasPermissions(activity, requiredPermissions)) {
            final Intent intent = new Intent(activity, subClass);
            intent.putExtra(ACTIVITY_INTENT, activity.getIntent());
            activity.startActivity(intent);
            activity.finish();
            return true;
        }

        return false;
    }

    public static boolean requestPermissionAndStartAction(
            Activity activity,
            Intent actionIntent,
            String[] requiredPermissions,
            Class<?> subClass) {
        if (!RequestPermissionsActivity.hasPermissions(activity, requiredPermissions)) {
            Intent subClassIntent = new Intent(activity, subClass);
            subClassIntent.putExtra(ACTIVITY_INTENT, actionIntent);
            activity.startActivity(subClassIntent);
            requiresRationale(activity, requiredPermissions);
            return true;
        }

        return false;
    }

    protected static boolean requiresRationale(Activity activity, String[] permissions) {
            for (String permission : permissions) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    Log.d(TAG, "requiresRationale: " + permission + " = true");
                } else {
                    Log.d(TAG, "requiresRationale: " + permission + " = false");
                }
            }
            return true;
    }

    protected static boolean hasPermissions(Context context, String[] permissions) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(context, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
    }

    /**
     * @return list of permissions that are needed in order for {@link #ACTIVITY_INTENT} to operate.
     * You only need to return a single permission per permission group you care about.
     */
    protected abstract String[] getRequiredPermissions();

    /**
     * @return list of permissions that would be useful for {@link #ACTIVITY_INTENT} to operate. You
     * only need to return a single permission per permission group you care about.
     */
    protected abstract String[] getDesiredPermissions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityIntent = (Intent) getIntent().getExtras().get(ACTIVITY_INTENT);

        // Only start a requestPermissions() flow when first starting this activity the first time.
        // The process is likely to be restarted during the permission flow (necessary to enable
        // permissions) so this is important to track.
        if (savedInstanceState == null) {
            requestPermissions();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (permissions.length > 0 && isAllGranted(permissions, grantResults)) {
            mActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(mActivityIntent);
            finish();
            overridePendingTransition(0, 0);
        } else {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                PermissionsUtil.notifyPermissionGranted(this, permissions[0]);
            }
            Toast.makeText(this, R.string.missing_required_permission, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private boolean isAllGranted(String permissions[], int[] grantResult) {
        for (int i = 0; i < permissions.length; i++) {
            if (grantResult[i] != PackageManager.PERMISSION_GRANTED
                    && isPermissionRequired(permissions[i])) {
                return false;
            }
        }
        return true;
    }

    private boolean isPermissionRequired(String p) {
        return Arrays.asList(getRequiredPermissions()).contains(p);
    }

    private void requestPermissions() {
        // Construct a list of missing permissions
        final ArrayList<String> unsatisfiedPermissions = new ArrayList<>();
        for (String permission : getDesiredPermissions()) {
            int result = ActivityCompat.checkSelfPermission(this, permission);

            if (ActivityCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                unsatisfiedPermissions.add(permission);
            }
        }
        if (unsatisfiedPermissions.size() == 0) {
            throw new RuntimeException("Request permission activity was called even"
                                               + " though all permissions are satisfied.");
        }
        ActivityCompat.requestPermissions(this,
                                          unsatisfiedPermissions.toArray(
                                                  new String[unsatisfiedPermissions.size()]),
                                          PERMISSIONS_REQUEST_ALL_PERMISSIONS);
    }
}
