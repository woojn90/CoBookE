package com.android.woojn.coursebookmarkapplication.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import com.android.woojn.coursebookmarkapplication.R;

/**
 * Created by wjn on 2017-02-24.
 */

public class SettingUtility {

    public static void toggleGridColumn(Context context, MenuItem menuItem) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int beforeNumberOfColumn = sharedPreferences.getInt(context.getString(R.string.pref_key_item_number_of_grid_column), 2);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (beforeNumberOfColumn == 2) {
            editor.putInt(context.getString(R.string.pref_key_item_number_of_grid_column), 3);
            editor.apply();
            menuItem.setIcon(R.drawable.ic_fab_grid_2x2);
        } else {
            editor.putInt(context.getString(R.string.pref_key_item_number_of_grid_column), 2);
            editor.apply();
            menuItem.setIcon(R.drawable.ic_fab_grid_3x3);
        }
    }

    public static boolean isDeleteWithConfirm(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean deletedWithConfirm = sharedPreferences.getBoolean(context.getString(R.string.settings_key_delete_with_confirm), true);
        return deletedWithConfirm;
    }
}
