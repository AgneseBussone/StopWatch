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

import java.util.List;
import java.util.Map;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = SectionsPagerAdapter.class.getSimpleName();
    private final int PAGES = 3;
    private Fragment stopwatch_fragment;
    private Fragment timer_fragment;
    private final int STOPWATCH_INDEX = 0;
    private final int TIMER_INDEX = 1;

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    // Do NOT try to save references to the Fragments in getItem(),
    // because getItem() is not always called. If the Fragment
    // was already created then it will be retrieved from the FragmentManger
    // and not here (i.e. getItem() won't be called again).
    @Override
    public Fragment getItem(int position) {
        return WatchFragment.newInstance(position);
    }

    // Here we can finally safely save a reference to the created
    // Fragment, no matter where it came from (either getItem() or
    // FragmentManger). Simply save the returned Fragment from
    // super.instantiateItem() into an appropriate reference depending
    // on the ViewPager position.
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
        // save the appropriate reference depending on position
        switch (position){
            case STOPWATCH_INDEX: stopwatch_fragment = createdFragment; break;
            case TIMER_INDEX: timer_fragment = createdFragment; break;
            default: break;
        }
        return createdFragment;
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

    // Security checks
    private View getView(int index){
        Fragment f = null;
        switch (index){
            case STOPWATCH_INDEX:
                f = stopwatch_fragment;
                break;
            case TIMER_INDEX:
                f = timer_fragment;
                break;
            default: break;
        }
        return (f != null) ? f.getView() : null;
    }

    public TextView getStopwatchTV() {
        View view = getView(STOPWATCH_INDEX);
        return (view != null) ? (TextView) view.findViewById(R.id.time_text) : null;
    }

    public ImageView getStopwatchNeedle() {
        View view = getView(STOPWATCH_INDEX);
        return (view != null) ? (ImageView) view.findViewById(R.id.needle_list) : null;
    }

    public TextView getStopwatchButtonText(){
        View view = getView(STOPWATCH_INDEX);
        return (view != null) ? (TextView)view.findViewById(R.id.btn_text_action) : null;
    }

    public ImageView getStopwatchButton(){
        View view = getView(STOPWATCH_INDEX);
        return (view != null) ? (ImageView) view.findViewById(R.id.bigBtn) : null;
    }

    public TextView getStopwatchModeText(){
        View view = getView(STOPWATCH_INDEX);
        return (view != null) ? (TextView)view.findViewById(R.id.btn_text_mode) : null;
    }

    public TextView getTimerTV() {
        View view = getView(TIMER_INDEX);
        return (view != null) ? (TextView) view.findViewById(R.id.time_text) : null;
    }

    public ImageView getTimerNeedle() {
        View view = getView(TIMER_INDEX);
        return (view != null) ? (ImageView) view.findViewById(R.id.needle_list) : null;
    }

    public TextView getTimerButtonText(){
        View view = getView(TIMER_INDEX);
        return (view != null) ? (TextView)view.findViewById(R.id.btn_text_action) : null;
    }

    public TextView getTimerModeText(){
        View view = getView(TIMER_INDEX);
        return (view != null) ? (TextView)view.findViewById(R.id.btn_text_mode) : null;
    }

    public ImageView getTimerButton(){
        View view = getView(TIMER_INDEX);
        return (view != null) ? (ImageView) view.findViewById(R.id.bigBtn) : null;
    }

    public ImageView getAddMinBtn(){
        View view = getView(TIMER_INDEX);
        return (view != null) ? (ImageView)view.findViewById(R.id.addMinBtn) : null;
    }

    public ImageView getAddSecBtn(){
        View view = getView(TIMER_INDEX);
        return (view != null) ? (ImageView)view.findViewById(R.id.addSecBtn) : null;
    }

    public CircleFillView getCircleFillView(){
        View view = getView(TIMER_INDEX);
        return (view != null) ? (CircleFillView)view.findViewById(R.id.circleFillView) : null;
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
                case 0:
                    // stopwatch
                    text.setText(R.string.time_default_stopwatch);
                    addTimeLayout.setVisibility(View.INVISIBLE);
                    circle.setVisibility(View.INVISIBLE);
                    break;
                case 1:
                    // timer
                    text.setText(R.string.time_default_timer);
                    addTimeLayout.setVisibility(View.VISIBLE);
                    circle.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    // settings
                    rootView = inflater.inflate(R.layout.settings_layout, container, false);

                    // get the expandable list
                    ExpandableListView settingsList = (ExpandableListView) rootView.findViewById(R.id.settingExpandableList);

                    // create the hash table with groups and subitems
                    Map<GroupInfo, List<Item>> settingsDetail = SettingsData.getData();

                    // set the adapter for the view
                    ExpandableListAdapter listAdapter = new CustomExpandableListAdapter(getContext(), settingsDetail);
                    settingsList.setAdapter(listAdapter);
                    settingsList.setOnChildClickListener(new PreferenceClickListener(getContext()));
            }
            return rootView;
        }
    }
}
