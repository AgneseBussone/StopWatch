package watch.stopwatch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

/**
 * Fills the data for settings listview
 */

public class CustomExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> groups;
    private Map<String, List<String[]>> group_detail;

    public CustomExpandableListAdapter(Context context, List<String> expandableListTitle,
                                       Map<String, List<String[]>> expandableListDetail) {
        this.context = context;
        this.groups = expandableListTitle;
        this.group_detail = expandableListDetail;
    }

    @Override
    public int getGroupCount() {
        return this.groups.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.group_detail.get(this.groups.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.groups.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.group_detail.get(this.groups.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String listTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.settings_group, null);
        }
        TextView listTitleTextView = (TextView) convertView.findViewById(R.id.group_title);
        listTitleTextView.setText(listTitle);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String[] expandedListText = (String[]) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.settings_item, null);
        }
        TextView expandedListTextView = (TextView) convertView.findViewById(R.id.settings_item);
        expandedListTextView.setText(expandedListText[0]);
        expandedListTextView.setTag(expandedListText[1]); // preference key
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
