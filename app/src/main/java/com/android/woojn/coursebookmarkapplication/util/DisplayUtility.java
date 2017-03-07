package com.android.woojn.coursebookmarkapplication.util;

import static com.android.woojn.coursebookmarkapplication.Constants.FIELD_NAME_ID;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.android.woojn.coursebookmarkapplication.R;
import com.android.woojn.coursebookmarkapplication.model.Folder;
import com.android.woojn.coursebookmarkapplication.model.Item;

import java.lang.reflect.Field;

import io.realm.Realm;

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

    public static void showItemDialog(Context context, final int itemId, final String beforeTitle, final String beforeDesc) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View dialogView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.dialog_item, null);
        builder.setView(dialogView);
        final EditText editTextTitle = (EditText) dialogView.findViewById(R.id.et_item_title);
        final EditText editTextDesc = (EditText) dialogView.findViewById(R.id.et_item_desc);

        editTextTitle.setText(beforeTitle);
        editTextDesc.setText(beforeDesc);

        builder.setTitle(R.string.string_item_info);
        builder.setNeutralButton(R.string.string_cancel, null);
        builder.setPositiveButton(R.string.string_update, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = editTextTitle.getText().toString().trim();
                String Desc = editTextDesc.getText().toString().trim();

                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                Item item = realm.where(Item.class).equalTo(FIELD_NAME_ID, itemId).findFirst();
                item.setTitle(title);
                item.setDesc(Desc);
                realm.commitTransaction();
                realm.close();
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.show();
        editTextTitle.requestFocus();
        editTextTitle.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        editTextDesc.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editTextDesc.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    if (alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled()) {
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                        return true;
                    }
                }
                return false;
            }
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String afterTitle = editTextTitle.getText().toString();
                String afterDesc = editTextDesc.getText().toString();

                if (!afterTitle.trim().isEmpty() && !afterDesc.trim().isEmpty()) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        };
        editTextTitle.addTextChangedListener(textWatcher);
        editTextDesc.addTextChangedListener(textWatcher);
    }

    public static void showFolderDialog(Context context, final int folderId, final String beforeTitle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View dialogView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.dialog_folder, null);
        builder.setView(dialogView);
        final EditText editTextTitle = (EditText) dialogView.findViewById(R.id.et_folder_title);

        editTextTitle.setText(beforeTitle);

        builder.setTitle(R.string.string_folder_info);
        builder.setNeutralButton(R.string.string_cancel, null);
        builder.setPositiveButton(R.string.string_update, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = editTextTitle.getText().toString().trim();

                Realm realm = Realm.getDefaultInstance();
                Folder folder = realm.where(Folder.class).equalTo(FIELD_NAME_ID, folderId).findFirst();
                realm.beginTransaction();
                folder.setTitle(title);
                realm.commitTransaction();
                realm.close();
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.show();
        editTextTitle.requestFocus();
        editTextTitle.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editTextTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    if (alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled()) {
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                        return true;
                    }
                }
                return false;
            }
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String afterTitle = editTextTitle.getText().toString();

                if (!afterTitle.trim().isEmpty()) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        };
        editTextTitle.addTextChangedListener(textWatcher);
    }
}
