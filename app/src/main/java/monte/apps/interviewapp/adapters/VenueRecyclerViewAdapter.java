package monte.apps.interviewapp.adapters;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.util.List;

import monte.apps.interviewapp.R;
import monte.apps.interviewapp.fragments.VenueFragment;
import monte.apps.interviewapp.web.FourSquareClient;
import monte.apps.interviewapp.web.dto.VenueCompact;

public class VenueRecyclerViewAdapter
        extends RecyclerView.Adapter<VenueRecyclerViewAdapter.ViewHolder> {
    /** Logging tag. */
    private static final String TAG = "VenueRecyclerViewAdapter";

    private final List<VenueCompact> mValues;
    private final VenueFragment.VenueFragmentListener mListener;

    public VenueRecyclerViewAdapter(
            List<VenueCompact> items,
            VenueFragment.VenueFragmentListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_venue_item, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Called when a view created by this adapter has been recycled.
     * <p>
     * <p>A view is recycled when a {@link LayoutManager} decides that it no longer needs to be
     * attached to its parent {@link RecyclerView}. This can be because it has fallen out of
     * visibility or a set of cached views represented by views still attached to the parent
     * RecyclerView. If an item view has large or expensive data bound to it such as large bitmaps,
     * this may be a good place to release those resources.</p>
     * <p>
     * RecyclerView calls this method right before clearing ViewHolder's internal data and sending
     * it to RecycledViewPool. This way, if ViewHolder was holding valid information before being
     * recycled, you can call {@link ViewHolder#getAdapterPosition()} to
     * get its adapter position.
     *
     * @param holder The ViewHolder for the view being recycled
     */
    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        Picasso.with(holder.mView.getContext()).cancelRequest(holder.mImageView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final int stablePosition = holder.getAdapterPosition();
        final VenueCompact venue = mValues.get(stablePosition);
        holder.mItem = venue;

        List<VenueCompact.Category> categories = venue.getCategories();
        if (categories.size() > 0 && categories.get(0).getIcon() != null) {
            if ("Museum of Healthcare".equals(venue.getName())) {
                throw new AssertionError("fuckme");
            }
            VenueCompact.Icon icon = categories.get(0).getIcon();
            String path = icon.getPrefix()
                    + "bg_"
                    + FourSquareClient.ICON_SIZE
                    + icon.getSuffix();
            if (!TextUtils.isEmpty(path)) {
                Glide.with(holder.mView.getContext())
                        .load(Uri.parse(path))
                        .into(holder.mImageView);
            } else {
                Glide.with(holder.mView.getContext())
                        .load("")
                        .into(holder.mImageView);
            }
        }

        if (venue.getLocation() != null
                && venue.getLocation().getFormattedAddress().size() > 0) {
            String text = "";
            for (final String line : venue.getLocation()
                    .getFormattedAddress()) {
                if (!text.isEmpty()) {
                    text += "\n";
                }
                text += line;
            }

            holder.mAddressTextView.setText(text);
        } else {
            holder.mAddressTextView.setText("Address unknown");
        }

        holder.mNameTextView.setText(venue.getName());

        holder.mView.setOnClickListener(v -> {
            if (null != mListener) {
                mListener.onVenueClicked(venue);
            }
        });

        holder.mView.setOnLongClickListener(view -> {
            if (null != mListener) {
                mListener.onVenueLongClicked(venue);
                return true;
            }

            return false;
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mImageView;
        public final TextView mNameTextView;
        public final TextView mAddressTextView;
        public VenueCompact mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mImageView = (ImageView) view.findViewById(R.id.imageView);
            mAddressTextView = (TextView) view.findViewById(R.id.addressTextView);
            mNameTextView = (TextView) view.findViewById(R.id.nameTextView);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mAddressTextView.getText() + "'";
        }
    }
}
