package com.android.woojn.coursebookmarkapplication.async;

import static com.android.woojn.coursebookmarkapplication.Constants.FIELD_NAME_ID;

import android.os.AsyncTask;
import android.os.Build;
import android.text.Html;

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
                // TODO: og: 태그 없을 때 다른 tag로 찾기 enhance
                Elements imgSrcPrefixHttp = doc.select("img[src^=http]");

                realm.beginTransaction();
                item.setTitle(doc.title());
                if (imgSrcPrefixHttp.size() > 0) {
                    item.setImageUrl(imgSrcPrefixHttp.get(0).attr("src"));
                } else {
                    Elements imgSrc = doc.select("img[src]");
                    if (imgSrc.size() > 0) {
                        item.setImageUrl(item.getUrl() + imgSrc.get(0).attr("src"));
                    }
                }
                item.setVisited(true);
                realm.commitTransaction();
                return null;
            }

            realm.beginTransaction();
            for (Element tag : ogTags) {
                String property = tag.attr("property");
                String content = tag.attr("content");

                if ("og:title".equals(property)) {
                    item.setTitle(convertTextFromHtml(content));
                } else if ("og:description".equals(property)) {
                    item.setDesc(convertTextFromHtml(content));
                } else if ("og:image".equals(property)) {
                    item.setImageUrl(convertTextFromHtml(content));
                }
            }
            item.setVisited(true);
            realm.commitTransaction();
            realm.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String convertTextFromHtml(String html) {
        String convertText;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            convertText = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString();
        } else {
            convertText = Html.fromHtml(html).toString();
        }
        return convertText;
    }
}
