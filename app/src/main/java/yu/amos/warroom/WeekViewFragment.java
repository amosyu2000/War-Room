package yu.amos.warroom;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
    The fragment's life cycle:

    When the fragment is first created by ViewPagerAdapter in the WeekViewActivity activity class,
    newInstance(), onCreate(), onCreateView(), and onResume() are called.

    At this point, the fragment has focus.

    When the fragment loses focus (in this case, only the fragment displayed and the ones directly
    beside it are kept in focus at any given time), onPause() is called.

    When the fragment receives focus again, onResume() is called.
 **/

public class WeekViewFragment extends Fragment {
    // the fragment initialization parameters
    private static final String DAY_NAME = "DAY_NAME";
    private static final String DAY_PRAYERS = "DAY_PRAYERS";

    private TextView textView;
    private LinearLayout linearLayout;
    private String dayName;
    private String[] dayPrayers;

    public WeekViewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     * @return A new instance of fragment WeekViewFragment.
     */
    public static WeekViewFragment newInstance(String dayName, String[] dayPrayers) {
        WeekViewFragment fragment = new WeekViewFragment();
        Bundle args = new Bundle();
        args.putString(DAY_NAME, dayName);
        args.putStringArray(DAY_PRAYERS, dayPrayers);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Store the dayName and dayPrayers that were passed when the fragment was constructed
        if (getArguments() != null) {
            dayName = getArguments().getString(DAY_NAME);
            dayPrayers = getArguments().getStringArray(DAY_PRAYERS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_week_view, container, false);

        textView = rootView.findViewById(R.id.week_fragment_textView);
        textView.setText(dayName);

        linearLayout = rootView.findViewById(R.id.week_fragment_linearLayout);

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        // When onPause() is called, nothing happens
    }

    @Override
    public void onResume() {
        super.onResume();

        linearLayout.removeAllViews();

        for (String prayer : dayPrayers) {
            View box = getLayoutInflater().inflate(R.layout.merge_prayer_box, null);
            EditText name_box = (EditText) box.findViewById(R.id.name_prayer_box);
            EditText desc_box = (EditText) box.findViewById(R.id.desc_prayer_box);

            // We only want the name_box, so we delete desc_box and then re-style name_box
            ((ViewManager) desc_box.getParent()).removeView(desc_box);
            name_box.setText(prayer);
            name_box.setBackgroundResource(android.R.color.transparent);

            // Redo the padding in the view (otherwise the name isn't centered right)
            int padding_in_dp = 8;  // 6 dps
            final float scale = getResources().getDisplayMetrics().density;
            int p = (int) (padding_in_dp * scale + 0.5f);
            name_box.setPadding(p,p,p,p);
            linearLayout.addView(box);
        }
    }
}
