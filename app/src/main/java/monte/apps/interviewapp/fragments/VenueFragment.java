package monte.apps.interviewapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import monte.apps.interviewapp.R;
import monte.apps.interviewapp.adapters.VenueRecyclerViewAdapter;
import monte.apps.interviewapp.web.dto.Venue;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link
 * VenueFragmentListener} interface.
 */
public class VenueFragment extends Fragment {

    private VenueFragmentListener mListener;

    private RecyclerView mRecyclerView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public VenueFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_venue_list, container,
                                     false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            mRecyclerView = (RecyclerView) view;
            mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        }
        return view;
    }

    public void setAdapter(VenueRecyclerViewAdapter adapter) {
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof VenueFragmentListener) {
            mListener = (VenueFragmentListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated to
     * the activity and potentially other fragments contained in that activity.
     * <p/>
     * See the Android Training lesson <a href= "http://developer.android
     * .com/training/basics/fragments/communicating.html" >Communicating with
     * Other Fragments</a> for more information.
     */
    public interface VenueFragmentListener {
        void onVenueClicked(Venue venue);

        void onVenueLongClicked(Venue item);
    }
}
