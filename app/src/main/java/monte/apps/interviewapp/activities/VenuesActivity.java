package monte.apps.interviewapp.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import monte.apps.interviewapp.R;
import monte.apps.interviewapp.adapters.VenueRecyclerViewAdapter;
import monte.apps.interviewapp.fragments.VenueFragment;
import monte.apps.interviewapp.web.dto.VenueCompact;
import monte.apps.interviewapp.web.dto.VenuesDto;

public class VenuesActivity extends BaseActivity
        implements OnMapReadyCallback,
                   GoogleMap.OnMarkerClickListener,
                   VenueFragment.VenueFragmentListener {

    /**
     * Logging tag.
     */
    private static final String TAG = "VenuesActivity";

    private static final String EXTRA_VENUES_DTO = "venues_dto";
    private static final String EXTRA_LOCATION = "location";

    private static final float MARKER_HOME_COLOR =
            BitmapDescriptorFactory.HUE_RED;
    private static final float MARKER_SELECTED_COLOR =
            BitmapDescriptorFactory.HUE_GREEN;
    private static final float MARKER_DEFAULT_COLOR =
            BitmapDescriptorFactory.HUE_BLUE;

    /**
     * List of venues
     */
    private List<VenueCompact> mVenues;
    /**
     * Google map instance
     */
    private GoogleMap mMap;
    /**
     * Google fragment containing the map
     */
    private SupportMapFragment mMapFragment;
    /**
     * Fragment containing a RecyclerView showing each venue
     */
    private VenueFragment mVenueFragment;
    /**
     * The current device location (passed in the starting intent)
     */
    private Location mLocation;
    /**
     * The Venues adapter used by the VenueFragment
     */
    private VenueRecyclerViewAdapter mAdapter;
    /**
     * Maps all venues to their associated marker.
     */
    private ArrayMap<VenueCompact, Marker> mMarkers;
    /**
     * Last marker clicked by the user (displayed in selection color)
     */
    private Marker mLastSelectedMarker;

    public static Intent makeIntent(
            Context context,
            VenuesDto venuesDto,
            Location location) {
        Intent intent = new Intent(context, VenuesActivity.class);
        intent.putExtra(EXTRA_VENUES_DTO, venuesDto);
        intent.putExtra(EXTRA_LOCATION, location);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venues);

        // Obtain the SupportMapFragment and get notified when the map is
        // ready to be used.
        mMapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        Intent intent = getIntent();
        VenuesDto venuesDto =
                (VenuesDto) intent.getSerializableExtra(EXTRA_VENUES_DTO);
        mVenues = venuesDto.getResponse().getVenues();
        mLocation = intent.getParcelableExtra(EXTRA_LOCATION);

        mVenueFragment = (VenueFragment)
                getSupportFragmentManager().findFragmentById(R.id.list);

        mAdapter = new VenueRecyclerViewAdapter(mVenues, this);
        mVenueFragment.setAdapter(mAdapter);
    }

    /**
     * Manipulates the map once available. This callback is triggered when the
     * map is ready to be used. This is where we can add markers or lines, add
     * listeners or move the camera. If GooglePlay services are not installed,
     * the user will be prompted to install them.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLoadedCallback(() -> addMarkers(null));
    }

   /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact with
     * fragments in their proper state, you should instead override {@link
     * #onResumeFragments()}.
     */
    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Adds markers to the map. Adds a special red marker to mark position of
     * current device location and a red marker to the passed venue location
     * (optional).
     *
     * @param selectedVenue A venue mark with a red color or null.
     */
    private void addMarkers(@Nullable VenueCompact selectedVenue) {
        mMarkers = new ArrayMap<>(mAdapter.getItemCount() + 1);
        mMap.clear();

        LatLng selectedLatLng = null;

        LatLngBounds.Builder bounds = new LatLngBounds.Builder();

        for (final VenueCompact venue : mVenues) {
            LatLng latLng =
                    new LatLng(
                            venue.getLocation().getLat(),
                            venue.getLocation().getLng());

            if (venue.equals(selectedVenue)) {
                selectedLatLng = latLng;
            } else {
                Marker marker = mMap.addMarker(
                        new MarkerOptions()
                                .position(latLng)
                                .title(venue.getName())
                                .icon(BitmapDescriptorFactory.defaultMarker(
                                        MARKER_DEFAULT_COLOR)));
                if (!mMarkers.containsKey(venue)) {
                    mMarkers.put(venue, marker);
                }
            }

            bounds.include(latLng);
        }

        LatLng userLatLng = null;

        if (mLocation != null) {
            // Add current location marker with a distinct color.
            userLatLng = new LatLng(
                    mLocation.getLatitude(), mLocation.getLongitude());
            Marker marker =
                    mMap.addMarker(
                            new MarkerOptions()
                                    .position(userLatLng)
                                    .title("You are here")
                                    .icon(BitmapDescriptorFactory.defaultMarker(
                                            MARKER_HOME_COLOR)));
            if (!mMarkers.containsKey(null)) {
                mMarkers.put(null, marker);
            }

            bounds.include(userLatLng);
        }

        // If a selected position has been specified, set a distinct color
        // and also center map to that venue location.
        if (selectedVenue != null && selectedLatLng != null) {
            final Marker marker = mMap.addMarker(
                    new MarkerOptions()
                            .position(selectedLatLng)
                            .title(selectedVenue.getName())
                            .icon(BitmapDescriptorFactory
                                          .defaultMarker(
                                                  MARKER_SELECTED_COLOR)));

            mMarkers.put(selectedVenue, marker);

            //CameraUpdate cameraUpdate =
            //        CameraUpdateFactory.newLatLngZoom(selectedLatLng, 12.0f);
            //mMap.animateCamera(cameraUpdate);

            new Handler().postDelayed(() -> onMarkerClick(marker), 200);
        } else if (userLatLng != null) {
            //CameraUpdate cameraUpdate =
            //        CameraUpdateFactory.newLatLngZoom(userLatLng, 12.0f);
            //mMap.animateCamera(cameraUpdate);
        }

        // Enable some useful settings.
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        LatLngBounds latLngBounds = bounds.build();
        LatLng northEast = latLngBounds.northeast;
        LatLng southWest = latLngBounds.southwest;
        Log.d(TAG, "addMarkers: northeast = " + northEast.toString()
                + " southwest = " + southWest.toString());

        mMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(latLngBounds, 200));

        // Set a marker click listener for a nice bounce animation.
        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public void onVenueClicked(VenueCompact venue) {
        onMarkerClick(mMarkers.get(venue));
    }

    @Override
    public void onVenueLongClicked(VenueCompact venue) {
        startActivity(DetailsActivity.makeIntent(this, venue));
    }

    /**
     * Bounce the marker when it is selected.
     *
     * @param marker
     * @return
     */
    @Override
    public boolean onMarkerClick(final Marker marker) {
        // Deselect last selected marker.
        if (mLastSelectedMarker != null) {
            mLastSelectedMarker.setIcon(
                    BitmapDescriptorFactory.defaultMarker(
                            MARKER_DEFAULT_COLOR));
        }

        // Keep track of last selected marker.
        mLastSelectedMarker = marker;

        marker.setIcon(BitmapDescriptorFactory.defaultMarker(
                MARKER_SELECTED_COLOR));
        marker.showInfoWindow();

        mMap.animateCamera(
                CameraUpdateFactory.newLatLng(
                        new LatLng(marker.getPosition().latitude,
                                   marker.getPosition().longitude)));

        final long startTime = SystemClock.uptimeMillis();
        final long duration = 2000;

        Projection projection = mMap.getProjection();
        final LatLng markerLatLng = marker.getPosition();
        Point startPoint = projection.toScreenLocation(markerLatLng);
        startPoint.offset(0, -100);
        final LatLng startLatLng = projection.fromScreenLocation(startPoint);

        final Interpolator interpolator = new BounceInterpolator();

        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - startTime;
                float t = interpolator.getInterpolation(
                        (float) elapsed / duration);
                double lng = t * markerLatLng.longitude
                        + (1 - t) * startLatLng.longitude;
                double lat = t * markerLatLng.latitude
                        + (1 - t) * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });

        // Don't consume event so that title will also be displayed.
        return false;
    }
}
