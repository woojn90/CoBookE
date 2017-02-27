package com.android.woojn.coursebookmarkapplication.util;

import android.content.Context;
import android.content.Intent;

import com.android.woojn.coursebookmarkapplication.R;
import com.android.woojn.coursebookmarkapplication.model.Course;
import com.android.woojn.coursebookmarkapplication.model.Folder;
import com.android.woojn.coursebookmarkapplication.model.Item;
import com.android.woojn.coursebookmarkapplication.model.Section;

import io.realm.RealmObject;

/**
 * Created by wjn on 2017-02-21.
 */

public class ShareUtility {

    public static void shareTextByRealmObject(Context context, RealmObject realmObject) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, getTextByRealmObject(realmObject));
        context.startActivity(intent.createChooser(intent, context.getString(R.string.menu_share)));
    }

    private static String getTextByRealmObject(RealmObject realmObject) {
        StringBuffer textToShare = new StringBuffer();
        if (realmObject instanceof Item) {
            Item item = (Item) realmObject;
            textToShare.append(item.getTitle());
            textToShare.append("\n" + item.getUrl());
        } else if (realmObject instanceof Section) {
            int itemCount = 1;
            Section section = (Section) realmObject;
            textToShare.append("[" + section.getTitle() + "]");

            for (Item item : section.getItems()) {
                textToShare.append("\n\n" + itemCount++ + ") " + item.getTitle());
                textToShare.append("\n" + item.getUrl());
            }
        } else if (realmObject instanceof Course) {
            int sectionCount = 1;
            Course course = (Course) realmObject;
            textToShare.append(course.getTitle());
            if (!course.getDesc().isEmpty()) {
                textToShare.append("\n- " + course.getDesc());
            }

            for (Section section : course.getSections()) {
                int itemCount = 1;
                textToShare.append("\n\n\n[" + sectionCount++ + ". " + section.getTitle() + "]");

                for (Item item : section.getItems()) {
                    textToShare.append("\n\n" + itemCount++ + ") " + item.getTitle());
                    textToShare.append("\n" + item.getUrl());
                }
            }
        } else if (realmObject instanceof Folder) {
            Folder folder = (Folder) realmObject;
            textToShare.append("[" + folder.getTitle() + "]\n");
            textToShare = getTextInsideFolder(folder, textToShare, "");
        }
        return textToShare.toString();
    }

    private static StringBuffer getTextInsideFolder(Folder parentFolder, StringBuffer beforeSb, String whiteSpace) {
        StringBuffer afterSb = beforeSb;

        if (parentFolder.getFolders().size() > 0 || parentFolder.getItems().size() > 0) {
            int count = 1;
            for (Folder folder : parentFolder.getFolders()) {
                afterSb.append("\n\n" + whiteSpace + count++ + ") [" + folder.getTitle() + "]");
                afterSb = getTextInsideFolder(folder, beforeSb, whiteSpace + "  ");
            }
            for (Item item : parentFolder.getItems()) {
                afterSb.append("\n\n" + whiteSpace + count++ + ") " + item.getTitle());
                afterSb.append("\n" + whiteSpace + "   " + item.getUrl());
            }
        } else {
            afterSb = beforeSb.append(whiteSpace + " (비어있음)");
        }
        return afterSb;
    }

}
