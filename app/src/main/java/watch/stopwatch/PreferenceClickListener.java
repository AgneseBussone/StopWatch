package watch.stopwatch;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

/**
 * Listener to the click on preferences.
 * Based on the tag assigned to the view during the creation, it'll show the correct popup
 */

public class PreferenceClickListener implements ExpandableListView.OnChildClickListener {

    private static final String TAG = PreferenceClickListener.class.getSimpleName();
    private Context context;
    private SharedPreferences sp;

    // Variable used to retrieve values from some of the alert dialogs
    // (if declared local, it's must be final, but I need to change the value
    private int alarm_sound_index;
    private Ringtone alarm_sound;

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
            case ID_SOUND: {
                CustomExpandableListAdapter listAdapter = (CustomExpandableListAdapter) parent.getExpandableListAdapter();
                ImageView shortcut = listAdapter.getImageShortcut(groupPosition);
                GroupInfo groupInfo = (GroupInfo)listAdapter.getGroup(groupPosition);
                showSoundPopup(shortcut, groupInfo);
                break;
            }
            case ID_RINGTONE:
                showRingtonePopup();
                break;
            case ID_START:
                showStartStopPopup("Select start mode", context.getString(R.string.KEY_START));
                break;
            case ID_STOP:
                showStartStopPopup("Select stop mode", context.getString(R.string.KEY_STOP));
                break;
            case ID_LAP:
                Toast.makeText(context, "TODO", Toast.LENGTH_SHORT).show();
                break;
            case ID_TOUCHBTN: {
                CustomExpandableListAdapter listAdapter = (CustomExpandableListAdapter) parent.getExpandableListAdapter();
                ImageView shortcut = listAdapter.getImageShortcut(groupPosition);
                GroupInfo groupInfo = (GroupInfo) listAdapter.getGroup(groupPosition);
                showTouchButtonFeedbackPopup(shortcut, groupInfo);
                break;
            }
            case ID_SCREEN:
                showYesNoPopup("Keep the screen always ON", context.getString(R.string.KEY_SCREEN), null, null);
                break;
            case ID_NIGHT: {
                CustomExpandableListAdapter listAdapter = (CustomExpandableListAdapter) parent.getExpandableListAdapter();
                ImageView shortcut = listAdapter.getImageShortcut(groupPosition);
                GroupInfo groupInfo = (GroupInfo)listAdapter.getGroup(groupPosition);
                showYesNoPopup("Night theme", context.getString(R.string.KEY_NIGHT), shortcut, groupInfo);
                break;
            }
            case ID_RATE:
                Toast.makeText(context, "TODO", Toast.LENGTH_SHORT).show();
                break;
            case ID_DEV:
                showDevPopup();
                break;
            case ID_VERSION:
                showVersionPopup();
                break;
        }
        return true;
    }

    private void showStartStopPopup(CharSequence title, final String pref_key){
        // Create the dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setTitle(title);
        final CharSequence[] items = context.getResources().getTextArray(R.array.mode_items);

        // read current preferences
        String pref = sp.getString(pref_key, items[0].toString()); //Default: touch
        int start_index = 0;
        if(!pref.isEmpty()){
            for(int i = 0; i < items.length; i++){
                if(pref.equals(items[i].toString())){
                    start_index = i;
                    break;
                }
            }
        }
        dialogBuilder.setSingleChoiceItems(items, start_index, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // save preference
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(pref_key, items[which].toString());
                editor.apply();
                dialog.dismiss();
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    private void showYesNoPopup(CharSequence title, final String pref_key, final ImageView shortcut, final GroupInfo groupInfo) {
        // Create the dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setTitle(title);
        final CharSequence[] items = context.getResources().getTextArray(R.array.yes_or_no);

        // read current preferences
        String pref = sp.getString(pref_key, items[1].toString()); //default: no
        final int checkedItem = (pref.equals(items[0].toString()))? 0 : 1;
        dialogBuilder.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // save preference
                SharedPreferences.Editor editor = sp.edit();
                String value = items[which].toString();
                editor.putString(pref_key, value);
                editor.apply();
                if(shortcut != null && groupInfo != null){
                    shortcut.setTag(value);
                    if(which == 0) // Yes
                        shortcut.setImageResource(groupInfo.imageResourceON);
                    else
                        shortcut.setImageResource(groupInfo.imageResourceOFF);
                }
                dialog.dismiss();
            }
        });

        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    private void showRingtonePopup() {
        // Create the dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setTitle("Select an alarm sound");
        final Map<String, String> sound_list = getNotifications();
        final CharSequence[] titles = sound_list.keySet().toArray(new CharSequence[sound_list.size()]);

        // read current preferences
        String pref = sp.getString(context.getString(R.string.KEY_RINGTONE_TITLE), "");
        alarm_sound_index = 0;
        if(!pref.isEmpty() && sound_list.containsKey(pref)){
            for(int i = 0; i < titles.length; i++){
                if(pref.equals(titles[i].toString())){
                    alarm_sound_index = i;
                    break;
                }
            }
        }

        dialogBuilder.setSingleChoiceItems(titles, alarm_sound_index, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // play the sample
                if(alarm_sound!= null && alarm_sound.isPlaying())
                    alarm_sound.stop();
                alarm_sound = RingtoneManager.getRingtone(context, Uri.parse(sound_list.get(String.valueOf(titles[which]))));
                alarm_sound.play();
                alarm_sound_index = which;
            }
        });

        dialogBuilder.setPositiveButton("SELECT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // stop the sound
                if(alarm_sound!= null && alarm_sound.isPlaying())
                    alarm_sound.stop();
                // save preference
                SharedPreferences.Editor editor = sp.edit();
                String title = String.valueOf(titles[alarm_sound_index]);
                editor.putString(context.getString(R.string.KEY_RINGTONE_TITLE), title);
                editor.putString(context.getString(R.string.KEY_RINGTONE_URI), sound_list.get(title));
                editor.apply();
                dialog.dismiss();
                Log.d(TAG, "title: " + title + " uri: "+ sound_list.get(title));
            }
        });
        dialogBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // stop the sound
                if(alarm_sound!= null && alarm_sound.isPlaying())
                    alarm_sound.stop();
            }
        });

        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    // Key: title
    // Value: uri
    private Map<String, String> getNotifications() {
        // Get a list of the available alarm sound
        RingtoneManager manager = new RingtoneManager(context);
        manager.setType(RingtoneManager.TYPE_ALARM);
        Cursor cursor = manager.getCursor();

        Map<String, String> list = new HashMap<>();
        while (cursor.moveToNext()) {
            String title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
            String uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX);
            String id = cursor.getString(RingtoneManager.ID_COLUMN_INDEX);

            list.put(title, uri + "/" + id);
        }
        return list;
    }

    private void showDevPopup() {
        // Create the dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setTitle("Creators");
        dialogBuilder.setMessage("Designer: Jeeho Cha\nDevelopers: \n\tAgnese Bussone\n\tFabrizio Perria");
        dialogBuilder.setNeutralButton("Close", null);
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    private void showVersionPopup() {
        //TODO: check the version online
        // Create the dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setTitle("App version");
        dialogBuilder.setMessage(BuildConfig.VERSION_NAME);
        dialogBuilder.setNeutralButton("Close", null);
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    private void showTouchButtonFeedbackPopup(final ImageView shortcut, final GroupInfo groupInfo) {
        // Create the dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setTitle("Central button feedback");
        CharSequence[] items = context.getResources().getTextArray(R.array.sound_pref_items);

        // read current preferences
        String pref = sp.getString(context.getString(R.string.KEY_TOUCHBTN), context.getString(R.string.vibrate_only));
        final boolean[] checkedItems = new boolean[3];
        checkedItems[0] = (pref.equals(context.getString(R.string.sound_only)) || pref.equals(context.getString(R.string.soundAndVibrate)));
        checkedItems[1] = (pref.equals(context.getString(R.string.vibrate_only)) || pref.equals(context.getString(R.string.soundAndVibrate)));
        checkedItems[2] = (pref.equals(context.getString(R.string.none)));

        dialogBuilder.setMultiChoiceItems(items, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                switch(which){
                    case 0:
                    case 1:
                        if(isChecked){
                            checkedItems[which] = true;
                            checkedItems[2] = false;
                            ((AlertDialog) dialog).getListView().setItemChecked(2, false);
                        }
                        break;
                    case 2:
                        if(isChecked){
                            checkedItems[which] = true;
                            checkedItems[0] = checkedItems[1] = false;
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
                if(checkedItems[0] && checkedItems [1]){
                    String value = context.getString(R.string.soundAndVibrate);
                    editor.putString(context.getString(R.string.KEY_TOUCHBTN), value);
                    editor.apply();
                    if(shortcut != null && groupInfo != null){
                        shortcut.setTag(value);
                        shortcut.setImageResource(groupInfo.imageResourceON);
                    }
                }else if(checkedItems[0]){
                    String value = context.getString(R.string.sound_only);
                    editor.putString(context.getString(R.string.KEY_TOUCHBTN), value);
                    editor.apply();
                    if(shortcut != null && groupInfo != null){
                        shortcut.setTag(value);
                        shortcut.setImageResource(groupInfo.imageResourceOFF);
                    }
                }else if(checkedItems[1]){
                    String value = context.getString(R.string.vibrate_only);
                    editor.putString(context.getString(R.string.KEY_TOUCHBTN), value);
                    editor.apply();
                    if(shortcut != null && groupInfo != null){
                        shortcut.setTag(value);
                        shortcut.setImageResource(groupInfo.imageResourceON);
                    }
                }else{
                    String value = context.getString(R.string.none);
                    editor.putString(context.getString(R.string.KEY_TOUCHBTN), value);
                    editor.apply();
                    if(shortcut != null && groupInfo != null){
                        shortcut.setTag(value);
                        shortcut.setImageResource(groupInfo.imageResourceOFF);
                    }
                }
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    private void showSoundPopup(final ImageView shortcut, final GroupInfo groupInfo) {
        // Create the dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setTitle("Timer expired feedback");
        CharSequence[] items = context.getResources().getTextArray(R.array.sound_pref_items);

        // read current preferences
        String pref = sp.getString(context.getString(R.string.KEY_SOUND), context.getString(R.string.none));
        final boolean[] checkedItems = new boolean[3];
        checkedItems[0] = (pref.equals(context.getString(R.string.sound_only)) || pref.equals(context.getString(R.string.soundAndVibrate)));
        checkedItems[1] = (pref.equals(context.getString(R.string.vibrate_only)) || pref.equals(context.getString(R.string.soundAndVibrate)));
        checkedItems[2] = (pref.equals(context.getString(R.string.none)));

        dialogBuilder.setMultiChoiceItems(items, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                switch(which){
                    case 0:
                    case 1:
                        if(isChecked){
                            checkedItems[which] = true;
                            checkedItems[2] = false;
                            ((AlertDialog) dialog).getListView().setItemChecked(2, false);
                        }
                        break;
                    case 2:
                        if(isChecked){
                            checkedItems[which] = true;
                            checkedItems[0] = checkedItems[1] = false;
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
                if(checkedItems[0] && checkedItems [1]){
                    String value = context.getString(R.string.soundAndVibrate);
                    editor.putString(context.getString(R.string.KEY_SOUND), value);
                    editor.apply();
                    if(shortcut != null && groupInfo != null){
                        shortcut.setTag(value);
                        shortcut.setImageResource(groupInfo.imageResourceON);
                    }
                }else if(checkedItems[0]){
                    String value = context.getString(R.string.sound_only);
                    editor.putString(context.getString(R.string.KEY_SOUND), value);
                    editor.apply();
                    if(shortcut != null && groupInfo != null){
                        shortcut.setTag(value);
                        shortcut.setImageResource(groupInfo.imageResourceON);
                    }
                }else if(checkedItems[1]){
                    String value = context.getString(R.string.vibrate_only);
                    editor.putString(context.getString(R.string.KEY_SOUND), value);
                    editor.apply();
                    if(shortcut != null && groupInfo != null){
                        shortcut.setTag(value);
                        shortcut.setImageResource(groupInfo.imageResourceOFF);
                    }
                }else{
                    String value = context.getString(R.string.none);
                    editor.putString(context.getString(R.string.KEY_SOUND), value);
                    editor.apply();
                    if(shortcut != null && groupInfo != null){
                        shortcut.setTag(value);
                        shortcut.setImageResource(groupInfo.imageResourceOFF);
                    }
                }
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }


}
