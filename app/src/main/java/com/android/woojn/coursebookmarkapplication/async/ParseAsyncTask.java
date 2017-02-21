package com.android.woojn.coursebookmarkapplication.async;

import static com.android.woojn.coursebookmarkapplication.Constants.FIELD_NAME_ID;

import android.os.AsyncTask;

import com.android.woojn.coursebookmarkapplication.model.Item;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import io.realm.Realm;

/**
 * Created by wjn on 2017-02-21.
 */

public class ParseAsyncTask extends AsyncTask<Integer, Void, Void> {
    @Override
    protected Void doInBackground(Integer... params) {
        try {
            Realm realm = Realm.getDefaultInstance();
            Item item = realm.where(Item.class).equalTo(FIELD_NAME_ID, params[0]).findFirst();

            Document doc = Jsoup.connect(item.getUrl()).get();

            Elements ogTags = doc.select("meta[property^=og:]");
            if (ogTags.size() <= 0) {
                // TODO: og: 태그 없으면 title 등 다른 tag로 찾기
                realm.beginTransaction();
                item.setVisited(true);
                realm.commitTransaction();
                return null;
            }

            realm.beginTransaction();
            for (Element tag : ogTags) {
                String property = tag.attr("property");
                String content = tag.attr("content");

                if ("og:title".equals(property)) {
                    item.setTitle(content);
                } else if ("og:description".equals(property)) {
                    item.setDesc(content);
                } else if ("og:image".equals(property)) {
                    item.setImageUrl(content);
                }
            }
            // TODO: 저장된 값이 없어도 방문한 것으로 처리할 지 확인
            item.setVisited(true);
            realm.commitTransaction();
            realm.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
