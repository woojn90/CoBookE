package com.android.woojn.coursebookmarkapplication.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.android.woojn.coursebookmarkapplication.R;
import com.android.woojn.coursebookmarkapplication.model.Section;
import com.android.woojn.coursebookmarkapplication.model.SectionDetail;

import io.realm.Realm;

import static com.android.woojn.coursebookmarkapplication.util.RealmDbUtility.getNewIdByClass;

/**
 * Created by wjn on 2017-02-15.
 */

public class FloatingService extends Service {

    private View mView;
    private WindowManager mManager;
    private WindowManager.LayoutParams mParams;
    private float mTouchX, mTouchY;
    private int mViewX, mViewY;
    private int mSectionId;
    private Intent mWebIntent;

    private View.OnTouchListener mViewTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mTouchX = event.getRawX();
                    mTouchY = event.getRawY();
                    mViewX = mParams.x;
                    mViewY = mParams.y;
                    break;
                case MotionEvent.ACTION_MOVE:
                    int x = (int) (event.getRawX() - mTouchX);
                    int y = (int) (event.getRawY() - mTouchY);
                    mParams.x = mViewX + x;
                    mParams.y = mViewY + y;

                    mManager.updateViewLayout(mView, mParams);
                    break;
            }
            return true;
        }
    };
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            onClickButton(view.getId());
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.floating_buttons, null);
        mParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_TOAST,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        mParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;

        Button buttonSaveAndContinue = (Button) mView.findViewById(R.id.btn_save_and_continue);
        Button buttonSaveAndClose = (Button) mView.findViewById(R.id.btn_save_and_close);
        mView.setOnTouchListener(mViewTouchListener);
        buttonSaveAndContinue.setOnClickListener(mOnClickListener);
        buttonSaveAndClose.setOnClickListener(mOnClickListener);

        mManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mManager.addView(mView, mParams);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSectionId = intent.getIntExtra("sectionId", 0);
//        String url = intent.getStringExtra("url");
//        mWebIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//        startActivity(mWebIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mView != null) {
            mManager.removeView(mView);
            mView = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void onClickButton(int viewId) {
        int newSectionDetailId = getNewIdByClass(SectionDetail.class);
        Realm realm = Realm.getDefaultInstance();
        Section section = realm.where(Section.class).equalTo("id", mSectionId).findFirst();
        realm.beginTransaction();
        SectionDetail sectionDetail = realm.createObject(SectionDetail.class, newSectionDetailId);
        sectionDetail.setTitle("Test Title");
        sectionDetail.setDesc("Test Description");
        sectionDetail.setUrl("https://www.naver.com");
        section.getSectionDetails().add(sectionDetail);
        realm.commitTransaction();
        Toast.makeText(getApplicationContext(), "저장되었습니다.", Toast.LENGTH_LONG).show();

        switch (viewId) {
            case R.id.btn_save_and_continue:
                Log.d("Check", "btn_save_and_continue");
                break;
            case R.id.btn_save_and_close:
                Log.d("Check", "btn_save_and_close");
                stopSelf();
                break;
        }
    }
}
