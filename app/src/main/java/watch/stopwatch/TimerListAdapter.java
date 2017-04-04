package watch.stopwatch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

/**
 * Adapter for timer preset list.
 */

public class TimerListAdapter extends BaseAdapter {

    private List<Time> preset;
    private Context context;

    // We need to go back to the MainActivity class to set or update the list and the status of the app
    private View.OnClickListener btn_listener;


    TimerListAdapter(List<Time> list, Context context, View.OnClickListener btnListener){
        preset = list;
        this.context = context;
        btn_listener = btnListener;
    }
    @Override
    public int getCount() {
        return preset.size();
    }

    @Override
    public Object getItem(int position) {
        return preset.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            // inflate the layout
            convertView = LayoutInflater.from(context).inflate(R.layout.timer_list_item, parent, false);
        }
        TextView value = (TextView)convertView.findViewById(R.id.timerItem_value);

        value.setText(preset.get(position).getFormattedShortTime());

        /* Edit button */
        ImageButton edit = (ImageButton)convertView.findViewById(R.id.editBtn);
        edit.setOnClickListener(btn_listener);

        /* Delete button */
        ImageButton delete = (ImageButton)convertView.findViewById(R.id.deleteBtn);
        delete.setOnClickListener(btn_listener);

        /* set button */
        ImageButton set = (ImageButton)convertView.findViewById(R.id.setBtn);
        set.setOnClickListener(btn_listener);

        // make the item not clickable
        convertView.setEnabled(false);

        return convertView;
    }
}
