package monte.apps.interviewapp.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import monte.apps.interviewapp.R;
import monte.apps.interviewapp.adapters.VenueRecyclerViewAdapter;
import monte.apps.interviewapp.fragments.VenueFragment;
import monte.apps.interviewapp.web.dto.Venue;
import monte.apps.interviewapp.web.dto.VenuesDto;

public class VenuesActivity extends AppCompatActivity
        implements OnMapReadyCallback,
                   GoogleMap.OnMarkerClickListener,
                   VenueFragment.VenueFragmentListener {

    /**
     * Logging tag.
     */
    private static final String TAG = "VenuesActivity";

    private static final String EXTRA_VENUES_DTO = "venues_dto";
    private static final String EXTRA_LOCATION = "location";

    private VenuesDto mVenuesDto;
    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private VenueFragment mVenueFragment;
    private Location mLocation;
    private VenueRecyclerViewAdapter mAdapter;

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
        mVenuesDto = (VenuesDto) intent.getSerializableExtra(EXTRA_VENUES_DTO);
        mLocation = intent.getParcelableExtra(EXTRA_LOCATION);

        mVenueFragment = (VenueFragment)
                getSupportFragmentManager().findFragmentById(R.id.list);

        mAdapter = new VenueRecyclerViewAdapter(
                mVenuesDto.getResponse().getVenues(), this);
        mVenueFragment.setAdapter(mAdapter);
    }

    /**
     * Manipulates the map once available. This callback is triggered when the
     * map is ready to be used. This is where we can add markers or lines, add
     * listeners or move the camera. In this case, we just add a marker near
     * Sydney, Australia. If Google Play services is not installed on the
     * device, the user will be prompted to install it inside the
     * SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        addMarkers(null);
    }

    /**
     * Adds markers to the map. Adds a special red marker to mark position of
     * current device location and a red marker to the passed venue location
     * (optional).
     *
     * @param selectedVenue A venue mark with a red color or null.
     */
    private void addMarkers(@Nullable Venue selectedVenue) {
        mMap.clear();

        LatLng selectedLatLng = null;

        List<Venue> venues = mVenuesDto.getResponse().getVenues();
        for (final Venue venue : venues) {
            LatLng latLng =
                    new LatLng(
                            venue.getLocation().getLat(),
                            venue.getLocation().getLng());
            if (venue.equals(selectedVenue)) {
                selectedLatLng = latLng;
            } else {
                mMap.addMarker(
                        new MarkerOptions()
                                .position(latLng)
                                .title(venue.getName()));
            }
        }

        // Add current location marker with a distinct color.
        LatLng userLatLng = new LatLng(
                mLocation.getLatitude(), mLocation.getLongitude());
        mMap.addMarker(
                new MarkerOptions()
                        .position(userLatLng)
                        .title("You are here")
                        .icon(BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory
                                        .HUE_VIOLET)));
        // If a selected position has been specified, set a distinct color
        // and also center map to that venue location.
        if (selectedVenue != null && selectedLatLng != null) {
            final Marker marker =
                mMap.addMarker(new MarkerOptions()
                        .position(selectedLatLng)
                        .title(selectedVenue.getName())
                        .icon(BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_GREEN)));

            mMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(selectedLatLng, 12.0f));

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onMarkerClick(marker);
                }
            }, 200);
        } else {
            mMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(userLatLng, 12.0f));
        }

        // Set a marker click listener for a nice bounce animation.
        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public void onVenueClicked(Venue venue) {
        startActivity(DetailsActivity.makeIntent(this, venue));
    }

    @Override
    public void onVenueLongClicked(Venue venue) {
        addMarkers(venue);
    }

    /**
     * Bounce the marker when it is selected.
     * @param marker
     * @return
     */
    @Override
    public boolean onMarkerClick(final Marker marker) {
        final Handler handler = new Handler();

        final long startTime = SystemClock.uptimeMillis();
        final long duration = 2000;

        Projection projection = mMap.getProjection();
        final LatLng markerLatLng = marker.getPosition();
        Point startPoint = projection.toScreenLocation(markerLatLng);
        startPoint.offset(0, -100);
        final LatLng startLatLng = projection.fromScreenLocation(startPoint);

        final Interpolator interpolator = new BounceInterpolator();

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
                marker.showInfoWindow();

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
