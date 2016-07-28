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

import android.Manifest.permission;
import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Utility class to help with runtime permissions.
 */
public class PermissionsUtil {
    /**
     * Each permission in this list is a cherry-picked permission from a particular permission
     * group. Granting a permission group enables access to all permissions in that group so we
     * only need to check a single permission in each group.
     * Note: This assumes that the app has correctly requested for all the relevant permissions
     * in its Manifest file.
     */
    public static final String CALENDAR = permission.WRITE_CALENDAR;
    public static final String CAMERA = permission.CAMERA;
    public static final String CONTACTS = permission.READ_CONTACTS;
    public static final String LOCATION = permission.ACCESS_FINE_LOCATION;
    public static final String MICROPHONE = permission.RECORD_AUDIO;
    public static final String PHONE = permission.CALL_PHONE;
    public static final String SENSORS = permission.BODY_SENSORS;
    public static final String SMS = permission.SEND_SMS;
    public static final String STORAGE = permission.WRITE_EXTERNAL_STORAGE;

    public static boolean hasCalendarPermissions(Context context) {
        return hasPermission(context, CALENDAR);
    }

    public static boolean hasCameraPermssions(Context context) {
        return hasPermission(context, CAMERA);
    }

    public static boolean hasContactsPermissions(Context context) {
        return hasPermission(context, CONTACTS);
    }

    public static boolean hasLocationPermissions(Context context) {
        return hasPermission(context, LOCATION);
    }

    public static boolean hasMicrophonePermissions(Context context) {
        return hasPermission(context, MICROPHONE);
    }

    public static boolean hasPhonePermissions(Context context) {
        return hasPermission(context, PHONE);
    }

    public static boolean hasSensorsPermissions(Context context) {
        return hasPermission(context, SENSORS);
    }

    public static boolean hasSmsPermission(Context context) {
        return hasPermission(context, SMS);
    }

    public static boolean hasStoragePermissions(Context context) {
        return hasPermission(context, STORAGE);
    }

    public static boolean hasPermission(Context context, String permission) {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasAppOp(Context context, String appOp) {
        final AppOpsManager appOpsManager =
                (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        final int mode = appOpsManager.checkOpNoThrow(appOp, Process.myUid(),
                                                      context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    /**
     * Rudimentary methods wrapping the use of a LocalBroadcastManager to simplify the process of
     * notifying other classes when a particular fragment is notified that a permission is granted.
     * <p>
     * To be notified when a permission has been granted, create a new broadcast receiver and
     * register it using {@link #registerPermissionReceiver(Context, BroadcastReceiver, String)}
     * <p>
     * E.g.
     * <pre>
     * final BroadcastReceiver receiver = new BroadcastReceiver() {
     *    {@literal @}Override
     *     public void onReceive(Context context, Intent intent) {
     *         refreshContactsView();
     *     }
     * }
     * PermissionsUtil.registerPermissionReceiver(getActivity(), receiver, READ_CONTACTS);
     * </pre>
     * If you register to listen for multiple permissions, you can identify which permission was
     * granted by inspecting {@link Intent#getAction()}.
     * <p>
     * In the fragment that requests for the permission, be sure to call {@link
     * #notifyPermissionGranted(Context, String)} when the permission is granted so that any
     * interested listeners are notified of the change.
     */
    public static void registerPermissionReceiver(
            Context context, BroadcastReceiver receiver,
            String permission) {
        final IntentFilter filter = new IntentFilter(permission);
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter);
    }

    public static void unregisterPermissionReceiver(Context context, BroadcastReceiver receiver) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
    }

    public static void notifyPermissionGranted(Context context, String permission) {
        final Intent intent = new Intent(permission);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
