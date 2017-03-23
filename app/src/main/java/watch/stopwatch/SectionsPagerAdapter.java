package watch.stopwatch;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = SectionsPagerAdapter.class.getSimpleName();
    private final int PAGES = 3;

    private ArrayList<WatchFragment> fragment_list;

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
        fragment_list = new ArrayList<>(PAGES);
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a WatchFragment (defined as a static inner class below).
        WatchFragment fragment = WatchFragment.newInstance(position + 1);
        fragment_list.add(position, fragment);
        return fragment;
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return PAGES;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "SECTION 1";
            case 1:
                return "SECTION 2";
            case 2:
                return "SECTION 3";
        }
        return null;
    }

    public TextView getStopwatchTV() {
        View view = fragment_list.get(0).getView();
        return (TextView) view.findViewById(R.id.time_text);
    }

    public ImageView getStopwatchNeedle() {
        View view = fragment_list.get(0).getView();
        return (ImageView) view.findViewById(R.id.needle_list);
    }

    public TextView getStopwatchButtonText(){
        View view = fragment_list.get(0).getView();
        return (TextView)view.findViewById(R.id.btn_text_action);
    }

    /**
     * Fragment inner class
     */
    public static class WatchFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public WatchFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static WatchFragment newInstance(int sectionNumber) {
            WatchFragment fragment = new WatchFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.watch_layout, container, false);
            TextView text = (TextView)rootView.findViewById(R.id.time_text);

            // adjust the layout basing on the section number
            int section =  getArguments().getInt(ARG_SECTION_NUMBER);
            switch(section){
                case 1:
                    text.setText(R.string.time_default_stopwatch);
                    break;
                case 2:
                    text.setText(R.string.time_default_timer);
                    break;
                default:
                    rootView = inflater.inflate(R.layout.fragment_main, container, false);
                    TextView textView = (TextView) rootView.findViewById(R.id.section_label);
                    textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            }
            return rootView;
        }
    }
}
