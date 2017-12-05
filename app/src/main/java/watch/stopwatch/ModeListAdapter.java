package watch.stopwatch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

/**
 * ListAdapter needed to handle the start/stop/lap trigger mode.
 * If you select "clap" for start/stop, it'll be disabled for lap, otherwise the system won't be able to distinguish between
 * start/stop command and lap command.
 * The only exception is "Button only" which is always available no matter what mode you select. Since there are two different buttons for start/stop
 * and lap commands, there are no conflicts in having this option enabled for both of them at the same time.
 */

public class ModeListAdapter extends BaseAdapter {

    private Context context;
    private CharSequence items[];   // list of modes
    private String defaultValue;    // Button only string resource
    private String toBeDisabled;    // mode selected for the other feature, the one that must be disabled for the current one

    ModeListAdapter(Context context, CharSequence data[], String optionToBeDisabled, String btnOnlyResource){
        this.context = context;
        items = data;
        toBeDisabled = optionToBeDisabled;
        defaultValue = btnOnlyResource;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return (toBeDisabled.equals(defaultValue));
    }

    @Override
    public boolean isEnabled(int position) {
            return (areAllItemsEnabled() || !items[position].equals(toBeDisabled));
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Object getItem(int position) {
        return items[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            // inflate the layout
            convertView = LayoutInflater.from(context).inflate(R.layout.checked_textview, parent, false);
        }
        CheckedTextView item = (CheckedTextView)convertView.findViewById(android.R.id.text1);
        item.setText(items[position]);
        convertView.setEnabled(isEnabled(position));

        return convertView;
    }

}
