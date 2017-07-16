package watch.stopwatch;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Fills the data for settings listview
 */

public class CustomExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private Map<GroupInfo, List<Item>> group_detail;
    private GroupInfo[] keySet;

    // array of image reference for the shortcut
    private ImageView[] shortcuts;

    public CustomExpandableListAdapter(Context context, Map<GroupInfo, List<Item>> expandableListDetail) {
        this.context = context;
        this.group_detail = expandableListDetail;
        Set<GroupInfo> keys = group_detail.keySet();
        keySet = keys.toArray(new GroupInfo[keys.size()]);
        shortcuts = new ImageView[keySet.length];
    }

    @Override
    public int getGroupCount() {
        return this.keySet.length;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.group_detail.get(this.keySet[groupPosition]).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.keySet[groupPosition];
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.group_detail.get(this.keySet[groupPosition]).get(childPosition);
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

    public ImageView getImageShortcut(int groupPosition){ return shortcuts[groupPosition]; }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final GroupInfo info = keySet[groupPosition];
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.settings_group, null);
        }
        TextView listTitleTextView = (TextView) convertView.findViewById(R.id.group_title);
        listTitleTextView.setText(info.title);

        ImageView icon = (ImageView)convertView.findViewById(R.id.group_icon);
        setShortcut(icon, info);
        // add image shortcut to the array
        shortcuts[groupPosition] = icon;
        // add listener to the image
        icon.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    ImageView image = (ImageView) v;
                    String tag = String.valueOf(image.getTag());
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor;
                    switch (info.setting) {
                        case ID_SOUND:
                            if (tag.equals(context.getString(R.string.vibrate_only))) {
                                image.setImageResource(info.imageResourceON);
                                tag = context.getString(R.string.soundAndVibrate);
                            } else if(tag.equals(context.getString(R.string.sound_only))) {
                                image.setImageResource(info.imageResourceOFF);
                                tag = context.getString(R.string.none);
                            } else if(tag.equals(context.getString(R.string.soundAndVibrate))){
                                image.setImageResource(info.imageResourceOFF);
                                tag = context.getString(R.string.vibrate_only);
                            } else{ // none
                                image.setImageResource(info.imageResourceON);
                                tag = context.getString(R.string.sound_only);
                            }
                            // save the preference
                            editor = sp.edit();
                            editor.putString(context.getString(R.string.KEY_SOUND),tag);
                            editor.apply();
                            break;
                        case ID_TOUCHBTN:
                            if (tag.equals(context.getString(R.string.vibrate_only))) {
                                image.setImageResource(info.imageResourceOFF);
                                tag = context.getString(R.string.none);
                            } else if(tag.equals(context.getString(R.string.sound_only))) {
                                image.setImageResource(info.imageResourceON);
                                tag = context.getString(R.string.soundAndVibrate);
                            } else if(tag.equals(context.getString(R.string.soundAndVibrate))){
                                image.setImageResource(info.imageResourceOFF);
                                tag = context.getString(R.string.sound_only);
                            } else{ // none
                                image.setImageResource(info.imageResourceON);
                                tag = context.getString(R.string.vibrate_only);
                            }
                            // save the preference
                            editor = sp.edit();
                            editor.putString(context.getString(R.string.KEY_TOUCHBTN),tag);
                            editor.apply();
                            break;
                        case ID_NIGHT:
                            if (tag.equals("No")) {
                                image.setImageResource(info.imageResourceON);
                                tag = "Yes";
                            } else{
                                image.setImageResource(info.imageResourceOFF);
                                tag = "No";
                            }
                            // save the preference
                            editor = sp.edit();
                            editor.putString(context.getString(R.string.KEY_NIGHT),tag);
                            editor.apply();
                            // Call recreate() to recreate the MainActivity
                            if(context instanceof MainActivity){
                                ((MainActivity)context).recreate();
                            }
                            break;
                        case ID_RATE:
                            // for the smiling face there's nothing to do
                            if (tag.equals("off")) {
                                image.setImageResource(info.imageResourceON);
                                tag = "on";
                            } else{
                                image.setImageResource(info.imageResourceOFF);
                                tag = "off";
                            }
                            break;
                        default:
                            break;
                    }
                    // set the new tag
                    image.setTag(tag);
                }
                return true;
            }
        });
        return convertView;
    }

    // read the preference an set image and tag accordingly
    private void setShortcut(ImageView imageView, GroupInfo groupInfo) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String pref;
        switch(groupInfo.setting){
            case ID_SOUND:
                pref = sp.getString(context.getString(R.string.KEY_SOUND), context.getString(R.string.KEY_SOUND_DEFAULT));
                imageView.setTag(pref);
                if(pref.equals(context.getString(R.string.vibrate_only)) || pref.equals(context.getString(R.string.none))){
                    imageView.setImageResource(groupInfo.imageResourceOFF);
                }
                else{
                    imageView.setImageResource(groupInfo.imageResourceON);
                }
                break;
            case ID_TOUCHBTN:
                pref = sp.getString(context.getString(R.string.KEY_TOUCHBTN), context.getString(R.string.KEY_TOUCHBTN_DEFAULT));
                imageView.setTag(pref);
                if(pref.equals(context.getString(R.string.sound_only)) || pref.equals(context.getString(R.string.none))){
                    imageView.setImageResource(groupInfo.imageResourceOFF);
                }
                else{
                    imageView.setImageResource(groupInfo.imageResourceON);
                }
                break;
            case ID_NIGHT:
                pref = sp.getString(context.getString(R.string.KEY_NIGHT), context.getString(R.string.KEY_NIGHT_DEFAULT));
                imageView.setTag(pref);
                if(pref.equals("No")){
                    imageView.setImageResource(groupInfo.imageResourceOFF);
                }
                else{
                    imageView.setImageResource(groupInfo.imageResourceON);
                }
                break;
            case ID_RATE:
                // for the smiling face there's nothing to do
                imageView.setTag("on");
                imageView.setImageResource(groupInfo.imageResourceON);
                break;
            default: break;
        }
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final Item item = (Item) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.settings_item, null);
        }
        TextView expandedListTextView = (TextView) convertView.findViewById(R.id.settings_item);
        expandedListTextView.setText(item.text);

        // assign the id to the parent view (avoid findViewById step in the childClickListener)
        convertView.setTag(item.id);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
