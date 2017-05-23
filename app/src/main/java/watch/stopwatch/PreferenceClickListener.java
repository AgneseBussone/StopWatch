package watch.stopwatch;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;

/**
 * Listener to the click on preferences.
 * Based on the tag assigned to the view during the creation, it'll show the correct popup
 */

public class PreferenceClickListener implements ExpandableListView.OnChildClickListener {

    private static final String TAG = PreferenceClickListener.class.getSimpleName();
    private Context context;
    private SharedPreferences sp;

    public PreferenceClickListener(Context context){
        this.context = context;
        sp = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        // Get the id assigned during the creation
        Item.DATA_ID tag = (Item.DATA_ID)v.getTag();

        // Execute different actions basing on the selected preference
        switch (tag) {
            case ID_SOUND:
                showSoundPopup();
                break;
            case ID_RINGTONE:
                Log.d(TAG, "ringtone popup");
                break;
            case ID_START:
                Log.d(TAG, "start popup");
                break;
            case ID_STOP:
                Log.d(TAG, "stop popup");
                break;
            case ID_LAP:
                Log.d(TAG, "lap popup");
                break;
            case ID_TOUCHBTN:
                Log.d(TAG, "touch popup");
                break;
            case ID_SCREEN:
                Log.d(TAG, "screen popup");
                break;
            case ID_NIGHT:
                Log.d(TAG, "night popup");
                break;
            case ID_RATE:
                Log.d(TAG, "rate popup");
                break;
            case ID_DEV:
                Log.d(TAG, "name popup");
                break;
            case ID_VERSION:
                Log.d(TAG, "app popup");
                break;
        }
        return true;
    }

    private void showSoundPopup() {
        // Create the dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setTitle("Timer expired action");
        CharSequence[] items = context.getResources().getTextArray(R.array.sound_pref_items);

        // read current preferences
        String pref = sp.getString(context.getString(R.string.KEY_SOUND), context.getString(R.string.none));
        final boolean[] checkedItem = new boolean[3];
        checkedItem[0] = (pref.equals(context.getString(R.string.sound_only)) || pref.equals(context.getString(R.string.soundAndVibrate)));
        checkedItem[1] = (pref.equals(context.getString(R.string.vibrate_only)) || pref.equals(context.getString(R.string.soundAndVibrate)));
        checkedItem[2] = (pref.equals(context.getString(R.string.none)));

        dialogBuilder.setMultiChoiceItems(items, checkedItem, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                switch(which){
                    case 0:
                    case 1:
                        if(isChecked){
                            checkedItem[which] = true;
                            checkedItem[2] = false;
                            ((AlertDialog) dialog).getListView().setItemChecked(2, false);
                        }
                        break;
                    case 2:
                        if(isChecked){
                            checkedItem[which] = true;
                            checkedItem[0] = checkedItem[1] = false;
                            ((AlertDialog) dialog).getListView().setItemChecked(0, false);
                            ((AlertDialog) dialog).getListView().setItemChecked(1, false);
                        }
                        break;
                }

            }
        });
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // save preferences
                SharedPreferences.Editor editor = sp.edit();
                if(checkedItem[0] && checkedItem [1]){
                    editor.putString(context.getString(R.string.KEY_SOUND), context.getString(R.string.soundAndVibrate));
                    editor.apply();
                }else if(checkedItem[0]){
                    editor.putString(context.getString(R.string.KEY_SOUND), context.getString(R.string.sound_only));
                    editor.apply();
                }else if(checkedItem[1]){
                    editor.putString(context.getString(R.string.KEY_SOUND), context.getString(R.string.vibrate_only));
                    editor.apply();
                }else{
                    editor.putString(context.getString(R.string.KEY_SOUND), context.getString(R.string.none));
                    editor.apply();
                }
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }


}