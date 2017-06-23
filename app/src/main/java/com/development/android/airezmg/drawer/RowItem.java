package com.development.android.airezmg.drawer;

/**
 * Created by penaen on 16/05/2016.
 */
public class RowItem {

    private String title;
    private int icon;

    public RowItem(String title, int icon){
        this.title = title;
        this.icon = icon;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
