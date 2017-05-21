package watch.stopwatch;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public TextView getTimerTV() {
        View view = fragment_list.get(1).getView();
        return (TextView) view.findViewById(R.id.time_text);
    }

    public ImageView getTimerNeedle() {
        View view = fragment_list.get(1).getView();
        return (ImageView) view.findViewById(R.id.needle_list);
    }

    public TextView getTimerButtonText(){
        View view = fragment_list.get(1).getView();
        return (TextView)view.findViewById(R.id.btn_text_action);
    }


    public ImageView getAddMinBtn(){
        View view = fragment_list.get(1).getView();
        return (ImageView)view.findViewById(R.id.addMinBtn);
    }

    public ImageView getAddSecBtn(){
        View view = fragment_list.get(1).getView();
        return (ImageView)view.findViewById(R.id.addSecBtn);
    }

    public CircleFillView getCircleFillView(){
        View view = fragment_list.get(1).getView();
        return (CircleFillView)view.findViewById(R.id.circleFillView);
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
            RelativeLayout addTimeLayout = (RelativeLayout)rootView.findViewById(R.id.addTimeLayout);
            CircleFillView circle = (CircleFillView)rootView.findViewById(R.id.circleFillView);

            // adjust the layout basing on the section number
            int section =  getArguments().getInt(ARG_SECTION_NUMBER);
            switch(section){
                case 1:
                    // stopwatch
                    text.setText(R.string.time_default_stopwatch);
                    addTimeLayout.setVisibility(View.INVISIBLE);
                    circle.setVisibility(View.INVISIBLE);
                    break;
                case 2:
                    // timer
                    text.setText(R.string.time_default_timer);
                    addTimeLayout.setVisibility(View.VISIBLE);
                    circle.setVisibility(View.VISIBLE);
                    break;
                case 3:
                    // settings
                    rootView = inflater.inflate(R.layout.settings_layout, container, false);

                    // get the expandable list
                    ExpandableListView settingsList = (ExpandableListView) rootView.findViewById(R.id.settingExpandableList);

                    // create the hash table with groups and subitems
                    Map<String, List<String[]>> settingsDetail = SettingsData.getData(getContext());

                    // Get the groups titles
                    List<String> settingsGroupsTitle = new ArrayList<>(settingsDetail.keySet());

                    // set the adapter for the view
                    ExpandableListAdapter listAdapter = new CustomExpandableListAdapter(getContext(), settingsGroupsTitle, settingsDetail);
                    settingsList.setAdapter(listAdapter);
                    settingsList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                        @Override
                        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                            return false;
                        }
                    });
                    //// TODO: 4/12/2017 add onGroupExpandListener, onGroupCollapseListener, onChildClickListener if needed
            }
            return rootView;
        }
    }
}
