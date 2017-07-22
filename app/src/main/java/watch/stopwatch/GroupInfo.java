package watch.stopwatch;

/**
 * Class that represent a group of settings:
 * title
 * shortcut ON image
 * shortcut OFF image
 */

public class GroupInfo {

    public String title;         // title of the group of settings
    public Item.DATA_ID setting; // preference associated with the shortcut

    // images for the shortcut
    public int imageResourceON;
    public int imageResourceOFF;

    GroupInfo(String title, Item.DATA_ID setting, int imageResourceON, int imageResourceOFF){
        this.title = title;
        this.imageResourceOFF = imageResourceOFF;
        this.imageResourceON = imageResourceON;
        this.setting = setting;
    }
}
