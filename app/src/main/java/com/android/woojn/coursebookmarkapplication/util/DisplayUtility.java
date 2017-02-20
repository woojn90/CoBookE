package com.android.woojn.coursebookmarkapplication.util;

import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;

import java.lang.reflect.Field;

/**
 * Created by wjn on 2017-02-20.
 */

public class DisplayUtility {

    public static void showPopupMenuIcon(PopupMenu popupMenu) {
        try {
            Field mFieldPopup = popupMenu.getClass().getDeclaredField("mPopup");
            mFieldPopup.setAccessible(true);
            MenuPopupHelper mPopup = (MenuPopupHelper) mFieldPopup.get(popupMenu);
            mPopup.setForceShowIcon(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
