package watch.stopwatch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Laps item list class.
 * Each element contains two string: the absolute format and the relative format
 */

public class MyArrayAdapter extends BaseAdapter {

    private List<String[]> data;
    private Context context;

    MyArrayAdapter(Context context, List<String[]> list){
        data = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            // inflate the layout
            convertView = LayoutInflater.from(context).inflate(R.layout.lap_list_item, parent, false);
        }
        TextView number = (TextView) convertView.findViewById(R.id.lapItem_number);
        TextView value = (TextView)convertView.findViewById(R.id.lapItem_value);
        number.setText(String.valueOf(position + 1));
        value.setText(data.get(position)[0]);

        // make the item not clickable
        convertView.setEnabled(false);

        return convertView;
    }
}
