package monte.apps.interviewapp.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import monte.apps.interviewapp.R;
import monte.apps.interviewapp.permissions.RequestPermissionsActivity;
import monte.apps.interviewapp.utils.ActivityUtils;
import monte.apps.interviewapp.utils.CircleTransform;
import monte.apps.interviewapp.utils.IntentUtils;
import monte.apps.interviewapp.utils.LocaleUtils;
import monte.apps.interviewapp.views.ExpandingEntryCardView;
import monte.apps.interviewapp.views.ExpandingEntryCardView.EntryTag;
import monte.apps.interviewapp.views.TouchPointManager;
import monte.apps.interviewapp.web.FourSquareClient;
import monte.apps.interviewapp.web.dto.Photo;
import monte.apps.interviewapp.web.dto.VenueCompact;
import monte.apps.interviewapp.web.dto.VenueComplete;
import monte.apps.interviewapp.web.dto.VenueDto;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailsActivity extends BaseActivity
        implements ExpandingEntryCardView.ExpandingEntryCardViewListener {
    public static final String MIMETYPE_SMS = "vnd.android-dir/mms-sms";

    /**
     * Logging tag.
     */
    private static final String TAG = "DetailsActivity";
    private static final String EXTRA_VENUE = "extra_venue";
    private static final int MAX_COMMENTS = 10;

    private final View.OnClickListener mEntryClickHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final Object entryTagObject = v.getTag();
            if (entryTagObject == null
                    || !(entryTagObject instanceof ExpandingEntryCardView.EntryTag)) {
                Log.w(TAG, "EntryTag was not used correctly");
                return;
            }
            final EntryTag entryTag = (EntryTag) entryTagObject;
            final Intent intent = entryTag.getIntent();

            // All action activities are started as a new task.
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (Intent.ACTION_CALL.equals(intent.getAction())) {
                if (RequestPermissionsActivity.requestPermissionAndStartAction(
                        DetailsActivity.this, intent)) {
                    return;
                }
            }

            // Launch the activity.
            ActivityUtils.startActivityWithErrorToast(DetailsActivity.this, intent);
        }
    };

    @BindView(R.id.photo_image_view)
    ImageView mImageView;
    @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.no_data_card)
    ExpandingEntryCardView mNoDataCard;
    @BindView(R.id.location_card)
    ExpandingEntryCardView mLocationCard;
    @BindView(R.id.contact_card)
    ExpandingEntryCardView mContactCard;
    @BindView(R.id.stats_card)
    ExpandingEntryCardView mStatsCard;
    @BindView(R.id.comments_card)
    ExpandingEntryCardView mCommentsCard;
    @BindView(R.id.scroll_view)
    NestedScrollView mNestedScrollView;
    private VenueCompact mVenue;
    private VenueComplete mVenueComplete;

    public static Intent makeIntent(Context context, @NonNull VenueCompact venue) {
        final Intent intent = new Intent(context, DetailsActivity.class);
        intent.putExtra(EXTRA_VENUE, venue);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        ButterKnife.bind(this, findViewById(android.R.id.content));

        mVenue = (VenueCompact) getIntent().getSerializableExtra(EXTRA_VENUE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mCollapsingToolbarLayout.setTitle(mVenue.getName());

        initializeViews();

        loadFullDetailsAsync();
    }

    private VenueComplete loadFullDetails(String id) {
        try {
            return FourSquareClient.buildRestAdapter()
                    .getVenue(mVenue.getId()).execute().body().getVenue();
        } catch (IOException e) {
            Log.d(TAG, "loadFullDetails: IOException!");
            return null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void loadFullDetailsAsync() {
        Call<VenueDto> venueDto =
                FourSquareClient.buildRestAdapter()
                        .getVenue(mVenue.getId());
        venueDto.enqueue(new Callback<VenueDto>() {
            @Override
            public void onResponse(Call<VenueDto> call, Response<VenueDto> response) {
                VenueDto venueDto = response.body();
                if (venueDto != null) {
                    loadCompleteVenue(venueDto.getVenue());
                }
            }

            @Override
            public void onFailure(Call<VenueDto> call, Throwable t) {
                Log.d(TAG, "onFailure: ");
            }
        });
    }

    private void initializeViews() {
        loadCompactVenue(mVenue);
    }

    private void loadCompactVenue(VenueCompact venue) {
        List<List<ExpandingEntryCardView.Entry>> entries;

        entries = locationToEntries(mVenue);
        mLocationCard.setOnClickListener(mEntryClickHandler);
        if (!entries.isEmpty()) {
            mLocationCard.setTitle("Location");
            mLocationCard.setVisibility(View.VISIBLE);
            mLocationCard.initialize(
                    entries,
                    /* numInitialVisibleEntries = */ 1,
                    /* isExpanded = */ false,
                    /* isAlwaysExpanded = */ false,
                    /* expandingEntryCardViewListener = */ this,
                    /* scroller = */ mNestedScrollView);
        }

        entries = contactToEntries(mVenue.getContact());
        mContactCard.setOnClickListener(mEntryClickHandler);
        if (!entries.isEmpty()) {
            mContactCard.setTitle("Contact");
            mContactCard.setVisibility(View.VISIBLE);
            mContactCard.initialize(
                    entries,
                    /* numInitialVisibleEntries = */ 1,
                    /* isExpanded = */ false,
                    /* isAlwaysExpanded = */ false,
                    /* expandingEntryCardViewListener = */ this,
                    /* scroller = */ mNestedScrollView);
        }
    }

    private void loadCompleteVenue(VenueComplete venue) {
        if (venue == null) {
            Snackbar.make(findViewById(android.R.id.content),
                          "Unable to access venue details",
                          Snackbar.LENGTH_SHORT).show();
            return;
        }

        mVenueComplete = venue;
        Photo photo = venue.getBestPhoto();
        if (photo != null) {
            loadPhoto(photo);
        } else {
            VenueComplete.Photos photos = venue.getPhotos();
            if (photos != null && photos.getCount() > 0) {
                if (!photos.getGroups().isEmpty()) {
                    VenueComplete.Group<Photo> photoGroup = photos.getGroups().get(0);
                    if (!photoGroup.getItems().isEmpty()) {
                        loadPhoto(photoGroup.getItems().get(0));
                    }
                }
            }
        }

        List<List<ExpandingEntryCardView.Entry>> entries;

        entries = tipsToEntries(venue.getTips());
        mCommentsCard.setOnClickListener(mEntryClickHandler);
        if (!entries.isEmpty()) {
            mCommentsCard.setTitle("Comments");
            mCommentsCard.setVisibility(View.VISIBLE);
            mCommentsCard.initialize(
                    entries,
                    /* numInitialVisibleEntries = */ Math.min(3, entries.size()),
                    /* isExpanded = */ false,
                    /* isAlwaysExpanded = */ false,
                    /* expandingEntryCardViewListener = */ this,
                    /* scroller = */ mNestedScrollView);
        }
    }

    private List<List<ExpandingEntryCardView.Entry>> tipsToEntries(VenueComplete.Tips tips) {
        List<List<ExpandingEntryCardView.Entry>> entries = new ArrayList<>();

        if (tips.getCount() == 0 || tips.getGroups().isEmpty()) {
            return entries;
        }

        List<VenueComplete.Tip> allTips = null;
        for (final VenueComplete.Group<VenueComplete.Tip> tipGroup : tips.getGroups()) {
            if ("All tips".equals(tipGroup.getName())) {
                allTips = tipGroup.getItems();
                break;
            }
        }

        if (allTips == null || allTips.isEmpty()) {
            return entries;
        }

        for (final VenueComplete.Tip tip : allTips) {
            if (entries.size() == MAX_COMMENTS) {
                break;
            }

            if (TextUtils.isEmpty(tip.getText())) {
                continue;
            }

            entries.add(new ArrayList<ExpandingEntryCardView.Entry>(1));

            ExpandingEntryCardView.Builder builder = new ExpandingEntryCardView.Builder();
            builder.id(-1);

            VenueComplete.User user = tip.getUser();

            String header = "";
            if (!TextUtils.isEmpty(user.getFirstName())) {
                header = tip.getUser().getFirstName();
            }
            if (!TextUtils.isEmpty(user.getLastName())) {
                if (!header.isEmpty()) {
                    header += " ";
                }
                header += user.getFirstName();
            }
            if (header.isEmpty()) {
                header = "User: " + user.getId();
            }
            if (!TextUtils.isEmpty(header)) {
                builder.header(header);
            }

            if (user.getPhoto() != null) {
                Photo photo = user.getPhoto();
                String url = photo.getPrefix() + "64x64" + photo.getSuffix();
                builder.iconUri(Uri.parse(url));
                builder.iconTransformation(new CircleTransform());
            } else {
                builder.icon(ContextCompat.getDrawable(this, R.drawable.ic_person_black_24dp));
            }

            builder.text(tip.getText());
            entries.get(entries.size() - 1).add(builder.build());
        }

        return entries;
    }

    private void loadPhoto(Photo photo) {
        String url =
                photo.getPrefix() + photo.getWidth() + "x" + photo.getHeight()
                        + photo.getSuffix();
        Uri uri = Uri.parse(url);
        Picasso.with(this)
                .load(uri)
                .into(mImageView);
    }

    private List<List<ExpandingEntryCardView.Entry>> locationToEntries(VenueCompact venue) {
        List<List<ExpandingEntryCardView.Entry>> entries = new ArrayList<>();

        VenueCompact.Location location = venue.getLocation();
        if (location == null) {
            return entries;
        }

        if (!location.getFormattedAddress().isEmpty()) {
            // Combine the lines of the formatted address into a single string.
            String text = "";
            for (final String line : location.getFormattedAddress()) {
                if (!text.isEmpty()) {
                    text += "\n";
                }
                text += line;
            }

            /*
            Uri iconUri = null;
            if (!venue.getCategories().isEmpty()) {
                for (final VenueCompact.Category category : venue.getCategories()) {
                    VenueCompact.Icon icon = category.getIcon();
                    String path = icon.getPrefix()
                            + "bg_"
                            + FourSquareClient.ICON_SIZE
                            + icon.getSuffix();
                    if (!TextUtils.isEmpty(path)) {
                        iconUri = Uri.parse(path);
                    }
                }
            }
            */

            entries.add(new ArrayList<ExpandingEntryCardView.Entry>(1));
            entries.get(entries.size() - 1).add(
                    new ExpandingEntryCardView.Builder()
                            .id(-1)
                            .header("Address")
                            .icon(ContextCompat.getDrawable(this, R.drawable.ic_place_24dp))
                            .text(text)
                            .intent(IntentUtils.getMapIntent(
                                    this, location.getLat(), location.getLng()))
                            .iconResourceId(android.R.drawable.ic_menu_call)
                            .build()
            );

            if (location.getDistance() > 0) {
                entries.add(new ArrayList<ExpandingEntryCardView.Entry>(1));
                entries.get(entries.size() - 1).add(
                        new ExpandingEntryCardView.Builder()
                                .id(-1)
                                .header("Distance")
                                .icon(ContextCompat.getDrawable(this, R.drawable.default_icon))
                                .text(LocaleUtils.getPrintableDistanceFromMeters(
                                        this, location.getDistance(), 1))
                                .build()
                );
            }

            if (!TextUtils.isEmpty(location.getCrossStreet())) {
                entries.add(new ArrayList<ExpandingEntryCardView.Entry>(1));
                entries.get(entries.size() - 1).add(
                        new ExpandingEntryCardView.Builder()
                                .id(-1)
                                .header("Cross street")
                                .icon(ContextCompat.getDrawable(this, R.drawable.default_icon))
                                .text(location.getCrossStreet())
                                .iconResourceId(android.R.drawable.ic_menu_agenda)
                                .build()
                );
            }

        }

        return entries;
    }

    private List<List<ExpandingEntryCardView.Entry>> contactToEntries(
            VenueCompact.Contact
                    contact) {
        List<List<ExpandingEntryCardView.Entry>> entries = new ArrayList<>();
        if (contact == null) {
            return entries;
        }

        if (!TextUtils.isEmpty(contact.getPhone())) {
            final Drawable phoneIcon =
                    ContextCompat.getDrawable(this, android.R.drawable.ic_menu_call)
                            .mutate();
            entries.add(new ArrayList<ExpandingEntryCardView.Entry>(1));
            entries.get(entries.size() - 1).add(
                    new ExpandingEntryCardView.Builder()
                            .id(-1)
                            .header("Phone")
                            .icon(ContextCompat.getDrawable(this, R.drawable.ic_phone_24dp))
                            .text(contact.getFormattedPhone())
                            .intent(IntentUtils.getCallIntent(this, contact.getPhone()))
                            .iconResourceId(R.drawable.ic_phone_24dp)
                            .build()
            );
        }

        if (!TextUtils.isEmpty(contact.getFacebook())) {
            entries.add(new ArrayList<ExpandingEntryCardView.Entry>(1));
            entries.get(entries.size() - 1).add(
                    new ExpandingEntryCardView.Builder()
                            .id(-1)
                            .text(contact.getFacebook())
                            .icon(ContextCompat.getDrawable(this, R.drawable.ic_facebook))
                            .header("Facebook")
                            .intent(IntentUtils.getOpenFacebookIntent(
                                    this,
                                    contact.getFacebook(),
                                    contact.getFacebookUsername()))
                            .build()
            );
        }

        if (!TextUtils.isEmpty(contact.getTwitter())) {
            entries.add(new ArrayList<ExpandingEntryCardView.Entry>(1));
            entries.get(entries.size() - 1).add(
                    new ExpandingEntryCardView.Builder()
                            .id(-1)
                            .header("Twitter")
                            .icon(ContextCompat.getDrawable(this, R.drawable.ic_twitter_color))
                            .text(contact.getTwitter())
                            .intent(IntentUtils.getTwitterFollowIntent(this, contact.getTwitter()))
                            .iconResourceId(android.R.drawable.ic_menu_agenda)
                            .build()
            );
        }

        return entries;
    }

    private List<List<ExpandingEntryCardView.Entry>> detailsToEntries(VenueCompact venue) {
        List<List<ExpandingEntryCardView.Entry>> entries = new ArrayList<>();

        if (venue.getStats() != null) {
            VenueCompact.Stats stats = venue.getStats();
            entries.add(new ArrayList<ExpandingEntryCardView.Entry>(1));

            entries.get(entries.size() - 1).add(
                    new ExpandingEntryCardView.Builder()
                            .id(-1)
                            .header("header")
                            .text("Checkins: " + stats.getCheckinsCount())
                            .build());

            entries.get(entries.size() - 1).add(
                    new ExpandingEntryCardView.Builder()
                            .id(-1)
                            .header("header")
                            .text("users: " + stats.getUsersCount())
                            .build());

            entries.get(entries.size() - 1).add(
                    new ExpandingEntryCardView.Builder()
                            .id(-1)
                            .header("header")
                            .text("tips: " + stats.getUsersCount())
                            .build());
        }

        return entries;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            TouchPointManager.getInstance().setPoint((int) ev.getRawX(), (int) ev.getRawY());
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * Not used
     */
    @Override
    public void onCollapse(int heightDelta) {
    }

    /**
     * Not used
     */
    @Override
    public void onExpand() {
    }

    /**
     * Not used
     */
    @Override
    public void onExpandDone() {
    }
}
