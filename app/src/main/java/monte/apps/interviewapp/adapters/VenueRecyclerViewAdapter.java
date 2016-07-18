package monte.apps.interviewapp.adapters;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import monte.apps.interviewapp.R;
import monte.apps.interviewapp.fragments.VenueFragment;
import monte.apps.interviewapp.web.FourSquareClient;
import monte.apps.interviewapp.web.dto.Venue;
import monte.apps.interviewapp.web.dto.VenuesDto;

public class VenueRecyclerViewAdapter
        extends RecyclerView.Adapter<VenueRecyclerViewAdapter.ViewHolder> {
    /** Logging tag. */
    private static final String TAG = "VenueRecyclerViewAdapter";

    private final List<Venue> mValues;
    private final VenueFragment.VenueFragmentListener mListener;

    public VenueRecyclerViewAdapter(
            List<Venue> items,
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

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Venue venue = mValues.get(position);
        holder.mItem = venue;

        List<Venue.Category> categories = venue.getCategories();
        if (categories.size() > 0 && categories.get(0).getIcon() != null) {
            Venue.Icon icon = categories.get(0).getIcon();
            String path = icon.getPrefix()
                    + "bg_"
                    + FourSquareClient.ICON_SIZE
                    + icon.getSuffix();
            if (!TextUtils.isEmpty(path)) {
                Picasso.with(holder.mView.getContext())
                        .load(Uri.parse(path))
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

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onVenueClicked(holder.mItem);
                }
            }
        });

        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (null != mListener) {
                    mListener.onVenueLongClicked(holder.mItem);
                }
                return false;
            }
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
        public Venue mItem;

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
