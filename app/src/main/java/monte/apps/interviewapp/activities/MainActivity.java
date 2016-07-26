package monte.apps.interviewapp.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.io.IOException;

import monte.apps.interviewapp.R;
import monte.apps.interviewapp.web.FourSquareApi;
import monte.apps.interviewapp.web.FourSquareClient;
import monte.apps.interviewapp.web.dto.VenuesDto;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity
        implements GoogleApiClient.ConnectionCallbacks,
                   GoogleApiClient.OnConnectionFailedListener,
                   LocationListener {
    /**
     * Logging tag.
     */
    private static final String TAG = "MainActivity";

    private static final int PERMISSION_REQUEST = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;

    private ProgressBar mProgressBar;
    private String mLocationPermission =
            Manifest.permission.ACCESS_COARSE_LOCATION;
    private FourSquareApi mFourSquareApi;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Button mFindButton;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFourSquareApi = FourSquareClient.buildRestAdapter();
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        showProgress(false);

        setupPlayServices();

        mFindButton = (Button) findViewById(R.id.button);
        mFindButton.setEnabled(false);
    }

    @Override
    protected void onStart() {
        if (requestLocationPermission()) {
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private void setupPlayServices() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (ActivityCompat.checkSelfPermission(this, mLocationPermission)
                == PackageManager.PERMISSION_GRANTED) {
            setupLocationService();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Unable to get current location",
                       Toast.LENGTH_SHORT).show();
    }

    private boolean setupLocationTracking() {
        if (ActivityCompat.checkSelfPermission(this, mLocationPermission)
                != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
            return false;
        }

        LocationServices.FusedLocationApi
                .requestLocationUpdates(
                        mGoogleApiClient, mLocationRequest, this);

        mLastLocation =
                LocationServices.FusedLocationApi.getLastLocation(
                        mGoogleApiClient);

        if (mLastLocation == null) {
            Snackbar.make(findViewById(android.R.id.content),
                          "Unable to determine last location.",
                          Snackbar.LENGTH_SHORT).show();

            return false;
        }

        return true;
    }

    private void setupLocationService() {
        // Try to conserve power by using low resolution.
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(60 * 1000);
        mLocationRequest.setFastestInterval(10 * 1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_NO_POWER);

        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder()
                        .addLocationRequest(mLocationRequest);

        final PendingResult<LocationSettingsResult> pendingResult =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient, builder.build());

        pendingResult.setResultCallback(
                result -> {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            setupLocationTracking();
                            mFindButton.setEnabled(true);
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                status.startResolutionForResult(
                                        MainActivity.this,
                                        REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However,
                            // we have no way to fix the settings so we
                            // won't show the dialog.
                            Log.w(TAG, "onResult: SETTINGS_CHANGE_UNAVAILABLE");
                            break;
                    }
                });
    }

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                Log.d(TAG, "onActivityResult: resultCode = " + resultCode);
                if (resultCode != RESULT_OK) {
                    showSnackbarLocationSettingsWithRetry();
                } else {
                    mFindButton.setEnabled(true);
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean requestLocationPermission() {
        if (checkSelfPermission(mLocationPermission)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this, mLocationPermission)) {
            showSnackbarPermissionWithRetry();
        } else {
            ActivityCompat.requestPermissions(
                    this, new String[]{mLocationPermission},
                    PERMISSION_REQUEST);
        }

        return false;
    }

    /**
     * API 23 (M) callback received when a permissions request has been
     * completed. Redirect callback to permission helper.
     */
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        // Verify that each required permission has been granted,
        // otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                showSnackbarPermissionWithRetry();
                return;
            }
        }

        Snackbar.make(
                findViewById(android.R.id.content),
                "Location service enabled.",
                Snackbar.LENGTH_SHORT).show();

        mGoogleApiClient.connect();
    }

    private void searchVenues(String query) {
        Call<VenuesDto> call;

        if (mLastLocation != null) {
            String latlng = String.valueOf(
                    mLastLocation.getLatitude())
                    + ","
                    + mLastLocation.getLongitude();
            call = mFourSquareApi.findVenues(latlng, query);
        } else {
            call = mFourSquareApi.findVenuesNear("Paris", query);
        }

        call.enqueue(new Callback<VenuesDto>() {
            @Override
            public void onResponse(
                    Call<VenuesDto> call, Response<VenuesDto> response) {
                showProgress(false);
                if (response.isSuccessful()) {
                    if (response.body().getVenues().isEmpty()) {
                        Toast.makeText(MainActivity.this, "No venues found", Toast.LENGTH_LONG).show();
                        return;
                    }
                    startVenuesActivity(response.body());
                } else {
                    try {
                        Toast.makeText(MainActivity.this, response.errorBody().string(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "onResponse: " + response.errorBody());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<VenuesDto> call, Throwable t) {
                showProgress(false);
                Toast.makeText(MainActivity.this, "No locations found",
                               Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onFindClicked(View view) {
        EditText editText = (EditText) findViewById(R.id.editText);
        String query = editText.getText().toString();
        if (TextUtils.isEmpty(query.trim())) {
            Toast.makeText(this, "Please enter a search term",
                           Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress(true);

        searchVenues(query);
    }

    private void startVenuesActivity(VenuesDto venuesDto) {
        startActivity(
                VenuesActivity.makeIntent(this, venuesDto, mLastLocation));
    }

    private void showProgress(boolean show) {
        if (show) {
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setIndeterminate(true);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mProgressBar.setIndeterminate(false);
        }
    }

    /**
     * Convenience method that shows an snackbar message with a retry button.
     */
    private void showSnackbarLocationSettingsWithRetry() {
        String msg = getString(R.string.location_settings_rationale);

        Snackbar snackbar =
                Snackbar.make(
                        findViewById(android.R.id.content),
                        msg,
                        Snackbar.LENGTH_INDEFINITE);

        snackbar.setAction(
                R.string.permissions_ok_button,
                view -> setupLocationService());

        snackbar.show();
    }

    /**
     * Convenience method that shows an snackbar message with a retry button.
     */
    private void showSnackbarPermissionWithRetry() {
        String msg = getString(R.string.permission_rationale);

        Snackbar snackbar =
                Snackbar.make(
                        findViewById(android.R.id.content),
                        msg,
                        Snackbar.LENGTH_INDEFINITE);

        snackbar.setAction(
                R.string.permissions_ok_button,
                view -> {
                        /// Submit the request.
                        ActivityCompat.requestPermissions(
                                MainActivity.this,
                                new String[]{mLocationPermission},
                                PERMISSION_REQUEST);
                });

        snackbar.show();
    }
}
