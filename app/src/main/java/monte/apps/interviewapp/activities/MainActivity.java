package monte.apps.interviewapp.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import monte.apps.interviewapp.R;
import monte.apps.interviewapp.web.FourSquareApi;
import monte.apps.interviewapp.web.FourSquareClient;
import monte.apps.interviewapp.web.dto.VenuesDto;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
                   GoogleApiClient.OnConnectionFailedListener {
    /**
     * Logging tag.
     */
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST = 1;
    private ProgressBar mProgressBar;
    private String mLocationPermission =
            Manifest.permission.ACCESS_COARSE_LOCATION;

    private FourSquareApi mFourSquareApi;
    private GoogleApiClient mGoogleApiClient;

    private Location mLastLocation;

    private Button mFindButton;

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

        if (requestLocationPermission()) {
            mFindButton.setEnabled(true);
            mGoogleApiClient.connect();
        }
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
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(mLocationPermission)
                    == PackageManager.PERMISSION_GRANTED) {
                mLastLocation =
                        LocationServices.FusedLocationApi.getLastLocation(
                                mGoogleApiClient);
                if (mLastLocation == null) {
                    Toast.makeText(this,
                                   "Location services have not been enabled.",
                                   Toast.LENGTH_SHORT).show();
                }
                mFindButton.setEnabled(mLastLocation != null);
            } else {
                Toast.makeText(this, "Location services have not been enabled.",
                               Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
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

    private void searchVenues(String query) {
        String latlng = String.valueOf(
                mLastLocation.getLatitude())
                + ","
                + mLastLocation.getLongitude();
        Call<VenuesDto> call =
                mFourSquareApi.findVenues(
                        latlng, query, FourSquareClient.VERSION);
        call.enqueue(new Callback<VenuesDto>() {
            @Override
            public void onResponse(
                    Call<VenuesDto> call, Response<VenuesDto> response) {
                showProgress(false);
                startVenuesActivity(response.body());
                Log.d(TAG, "onResponse: ");
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Unable to get current location",
                       Toast.LENGTH_SHORT).show();
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
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        /// Submit the request.
                        ActivityCompat.requestPermissions(
                                MainActivity.this,
                                new String[]{mLocationPermission},
                                PERMISSION_REQUEST);
                    }
                });

        snackbar.show();
    }
}
