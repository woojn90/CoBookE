package com.android.woojn.coursebookmarkapplication.util;

import com.android.woojn.coursebookmarkapplication.model.Course;
import com.android.woojn.coursebookmarkapplication.model.Section;
import com.android.woojn.coursebookmarkapplication.model.SectionDetail;

import io.realm.Realm;
import io.realm.RealmModel;

/**
 * Created by wjn on 2017-02-09.
 */

public class RealmDbUtility {

    public static <E extends RealmModel> int getNewIdByClass(Realm realm, Class<E> clazz) {
        int newId;
        Number id = realm.where(clazz).max("id");

        if (id == null) {
            newId = 1;
        } else {
            newId = id.intValue() + 1;
        }
        return newId;
    }

    public static void populateTestData(Realm realm, int newCourseId) {
        realm.beginTransaction();
        Course course = realm.createObject(Course.class, newCourseId);
        course.setTitle("Test Course");
        course.setDesc("Test for realm DB");
        course.setFavorite(false);

        int newSectionId1 = getNewIdByClass(realm, Section.class);
        Section section1 = realm.createObject(Section.class, newSectionId1);
        section1.setTitle("밥집");
        int newSectionId2 = getNewIdByClass(realm, Section.class);
        Section section2 = realm.createObject(Section.class, newSectionId2);
        section2.setTitle("카페");
        int newSectionId3 = getNewIdByClass(realm, Section.class);
        Section section3 = realm.createObject(Section.class, newSectionId3);
        section3.setTitle("관광");

        course.getSections().add(section1);
        course.getSections().add(section2);
        course.getSections().add(section3);

        int newSectionDetailId1 = getNewIdByClass(realm, SectionDetail.class);
        SectionDetail sectionDetail1 = realm.createObject(SectionDetail.class, newSectionDetailId1);
        sectionDetail1.setTitle("맛집1");
        sectionDetail1.setDesc("맛있고 친절한 맛집");
        int newSectionDetailId2 = getNewIdByClass(realm, SectionDetail.class);
        SectionDetail sectionDetail2 = realm.createObject(SectionDetail.class, newSectionDetailId2);
        sectionDetail2.setTitle("맛집2");
        sectionDetail2.setDesc("친절하고 좋은 집");
        int newSectionDetailId3 = getNewIdByClass(realm, SectionDetail.class);
        SectionDetail sectionDetail3 = realm.createObject(SectionDetail.class, newSectionDetailId3);
        sectionDetail3.setTitle("카페1");
        sectionDetail3.setDesc("가격 대비 훌륭함");
        int newSectionDetailId4 = getNewIdByClass(realm, SectionDetail.class);
        SectionDetail sectionDetail4 = realm.createObject(SectionDetail.class, newSectionDetailId4);
        sectionDetail4.setTitle("카페2");
        sectionDetail4.setDesc("커피가 아주 맛있다");
        int newSectionDetailId5 = getNewIdByClass(realm, SectionDetail.class);
        SectionDetail sectionDetail5 = realm.createObject(SectionDetail.class, newSectionDetailId5);
        sectionDetail5.setTitle("카페2");
        sectionDetail5.setDesc("커피가 아주 맛있다");

        section1.getSectionDetails().add(sectionDetail1);
        section1.getSectionDetails().add(sectionDetail2);
        section2.getSectionDetails().add(sectionDetail3);
        section2.getSectionDetails().add(sectionDetail4);
        section3.getSectionDetails().add(sectionDetail5);

        realm.copyToRealmOrUpdate(course);
        realm.commitTransaction();
    }
}
