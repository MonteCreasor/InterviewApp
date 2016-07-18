package monte.apps.interviewapp.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import monte.apps.interviewapp.R;
import monte.apps.interviewapp.web.FourSquareClient;
import monte.apps.interviewapp.web.dto.Photo;
import monte.apps.interviewapp.web.dto.PhotosDto;
import monte.apps.interviewapp.web.dto.Venue;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailsActivity extends AppCompatActivity {
    /** Logging tag. */
    private static final String TAG = "DetailsActivity";

    private static final String EXTRA_VENUE = "extra_venue";

    @BindView(R.id.photo_image_view) ImageView mImageView;
    @BindView(R.id.venue_icon_image_view) ImageView mIconImageView;
    @BindView(R.id.venue_name_text_view) TextView mNameTextView;
    @BindView(R.id.venue_address_text_view) TextView mAddressTextView;

    private Venue mVenue;

    public static Intent makeIntent(Context context, @NonNull Venue venue) {
        final Intent intent = new Intent(context, DetailsActivity.class);
        intent.putExtra(EXTRA_VENUE, venue);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        ButterKnife.bind(this, findViewById(android.R.id.content));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mVenue = (Venue) getIntent().getSerializableExtra(EXTRA_VENUE);

        getPhotos();
    }

    private void getPhotos() {
        Call<PhotosDto> venuePhotos =
                FourSquareClient.buildRestAdapter()
                        .getVenuePhotos(
                                mVenue.getId(),
                                FourSquareClient.VERSION_PHOTOS);

        venuePhotos.enqueue(new Callback<PhotosDto>() {
            @Override
            public void onResponse(
                    Call<PhotosDto> call, Response<PhotosDto> response) {
                Log.d(TAG, "onResponse: ");
                PhotosDto photosDto = response.body();
                if (photosDto != null) {
                    List<Photo> photos = photosDto.getPhotos();
                    if (photos.size() > 0) {
                        loadPhoto(photos.get(0));
                    }
                }
            }

            @Override
            public void onFailure(Call<PhotosDto> call, Throwable t) {
                Log.d(TAG, "onFailure: ");
            }
        });
    }

    private void loadPhoto(Photo photo) {
        String url = photo.getPrefix() + photo.getWidth() + "x" + photo.getHeight() + photo.getSuffix();
        Uri uri = Uri.parse(url);
        Picasso.with(this)
                .load(uri)
                .into(mImageView);
    }
}
