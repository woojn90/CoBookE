package com.android.woojn.coursebookmarkapplication.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.woojn.coursebookmarkapplication.R;
import com.android.woojn.coursebookmarkapplication.model.Section;
import com.android.woojn.coursebookmarkapplication.model.SectionDetail;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;

import static com.android.woojn.coursebookmarkapplication.util.RealmDbUtility.setTextViewEmptyVisibility;

/**
 * Created by wjn on 2017-02-07.
 */

public class CourseSectionAdapter extends RealmRecyclerViewAdapter<Section, CourseSectionAdapter.CourseSectionViewHolder> {

    private Context mContext;
    private OnRecyclerViewClickListener mListener;

    public CourseSectionAdapter(Context context, OrderedRealmCollection data, OnRecyclerViewClickListener listener) {
        super(context, data, true);
        this.mListener = listener;
        this.mContext = context;
    }

    public interface OnRecyclerViewClickListener {
        void onItemClick(int id, int viewId);
        void onItemLongClick(int id);
    }

    @Override
    public CourseSectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.course_section_list_item, parent, false);
        return new CourseSectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CourseSectionViewHolder holder, int position) {
        Section section = getData().get(position);

        holder.itemView.setTag(section.getId());
        holder.textViewSectionTitle.setText(section.getTitle() + " (" + section.getSearchWord() + ")");
        CourseSectionDetailAdapter adapter = new CourseSectionDetailAdapter(mContext, section.getSectionDetails(), holder);
        holder.recyclerViewCourseSectionDetail.setAdapter(adapter);
        setTextViewEmptyVisibility(SectionDetail.class, section.getId(), holder.textViewCourseSectionDetailEmpty);
    }

    class CourseSectionViewHolder extends RecyclerView.ViewHolder
        implements View.OnLongClickListener, CourseSectionDetailAdapter.OnRecyclerViewClickListener {

        @BindView(R.id.tv_section_title)
        TextView textViewSectionTitle;
        @BindView(R.id.tv_course_section_detail_empty)
        TextView textViewCourseSectionDetailEmpty;
        @BindView(R.id.rv_course_section_detail_list)
        RecyclerView recyclerViewCourseSectionDetail;

        public CourseSectionViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            recyclerViewCourseSectionDetail.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            itemView.setOnLongClickListener(this);
        }

        @OnClick({R.id.btn_search_section_detail, R.id.btn_share_section, R.id.btn_delete_section})
        public void onClick(View view) {
            int id = (int) itemView.getTag();
            int viewId = view.getId();

            if (viewId == R.id.btn_search_section_detail) {
                // TODO: 검색 완료 후 overlay로 저장할 때, 하도록 변경
                setTextViewEmptyVisibility(SectionDetail.class, id, textViewCourseSectionDetailEmpty);
            }
            mListener.onItemClick(id, viewId);
        }

        @Override
        public boolean onLongClick(View view) {
            int id = (int) itemView.getTag();
            mListener.onItemLongClick(id);
            return true;
        }

        @Override
        public void onItemClick(int id, int viewId) {
            switch (viewId) {
                case -1:
                    Log.d("Check", "CourseSectionAdapter move sectionDetail id : " + id + ", viewID : " + viewId);
                    break;
                case R.id.btn_share_section_detail:
                    Log.d("Check", "CourseSectionAdapter share sectionDetail id : " + id + ", viewID : " + viewId);
                    break;
                case R.id.btn_delete_section_detail:
                    Log.d("Check", "CourseSectionAdapter delete sectionDetail id : " + id + ", viewID : " + viewId);
                    // TODO: alert 띄우고 삭제하도록 수정
                    deleteSectionDetail(id);
                    break;
            }
        }

        private void deleteSectionDetail(int sectionDetailId) {
            Realm realm = Realm.getDefaultInstance();
            SectionDetail sectionDetail = realm.where(SectionDetail.class).equalTo("id", sectionDetailId).findFirst();
            realm.beginTransaction();
            sectionDetail.deleteFromRealm();
            realm.commitTransaction();
            realm.close();
            setTextViewEmptyVisibility(SectionDetail.class, (int) itemView.getTag(), textViewCourseSectionDetailEmpty);
        }
    }
}