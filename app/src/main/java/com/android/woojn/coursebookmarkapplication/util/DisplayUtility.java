package com.android.woojn.coursebookmarkapplication.util;

import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;

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

    public static SpannableString getUnderlineText(String text) {
        if (text != null && text.length() != 0) {
            SpannableString content = new SpannableString(text);
            content.setSpan(new UnderlineSpan(), 0, text.length(), 0);
            return content;
        }
        return null;
    }

}
