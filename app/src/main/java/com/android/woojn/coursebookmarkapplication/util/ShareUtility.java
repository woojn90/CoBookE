package com.android.woojn.coursebookmarkapplication.util;

import android.content.Context;
import android.content.Intent;

import com.android.woojn.coursebookmarkapplication.R;
import com.android.woojn.coursebookmarkapplication.model.Course;
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
        context.startActivity(intent.createChooser(intent, context.getString(R.string.action_share)));
    }

    private static String getTextByRealmObject(RealmObject realmObject) {
        String textToShare = null;
        if (realmObject instanceof Item) {
            Item item = (Item) realmObject;
            textToShare = item.getTitle();
            textToShare += "\n" + item.getUrl();
        } else if (realmObject instanceof Section) {
            int itemCount = 1;
            Section section = (Section) realmObject;
            textToShare = "[" + section.getTitle() + "]";

            for (Item item : section.getItems()) {
                textToShare += "\n\n" + itemCount++ + ") " + item.getTitle();
                textToShare += "\n" + item.getUrl();
            }
        } else if (realmObject instanceof Course) {
            int sectionCount = 1;
            Course course = (Course) realmObject;
            textToShare = course.getTitle();
            if (!course.getDesc().isEmpty()) {
                textToShare += "\n" + course.getDesc();
            }

            for (Section section : course.getSections()) {
                int itemCount = 1;
                textToShare += "\n\n\n[" + sectionCount++ + ". " + section.getTitle() + "]";

                for (Item item : section.getItems()) {
                    textToShare += "\n\n" + itemCount++ + ") " + item.getTitle();
                    textToShare += "\n" + item.getUrl();
                }
            }
        }
        return textToShare;
    }

}
