package kst.ksti.chauffeur.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.activity.DriveHistoryDetailActivity;
import kst.ksti.chauffeur.activity.MainActivity;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.model.AllocationCompleted;
import kst.ksti.chauffeur.utility.DateUtils;
import kst.ksti.chauffeur.utility.OnSingleClickListener;

public class DriveHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<AllocationCompleted> mList;
    private WeakReference<MainActivity> activityWeakReference;

    private static final int SINGLE_LINE = 1;
    private static final int MULTI_LINE  = 2;

    public DriveHistoryAdapter(Context context, ArrayList<AllocationCompleted> list) {
        activityWeakReference = new WeakReference<>((MainActivity) context);
        this.mList = list;
    }

    @Override
    public int getItemViewType(int position) {
        if(mList != null && mList.size() > 0) {
            if(!TextUtils.isEmpty(mList.get(position).resvOrgPoi)) {
                return MULTI_LINE;
            }
        }
        return SINGLE_LINE;
    }

    // RecyclerView에 새로운 데이터를 보여주기 위해 필요한 ViewHolder를 생성해야 할 때 호출됩니다.
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if(viewType == MULTI_LINE) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_drive_history_detail, viewGroup, false);
            return new DriveHistoryItemViewHolder(view, false);

        } else {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_drive_history, viewGroup, false);
            return new DriveHistoryItemViewHolder(view, true);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder driveHistoryItem, int i) {
        ((DriveHistoryItemViewHolder)driveHistoryItem).init(i);
    }

    private void setImage(TextView textView, Drawable drawable) {
        textView.setBackground(drawable);
    }

    public void addItem(AllocationCompleted object) {
        if (mList != null) {
            mList.add(object);
            notifyItemInserted(mList.size());
        }
    }

    public void addItems(List<AllocationCompleted> object) {
        if (mList != null) {
            int startPosition = mList.size();
            mList.addAll(object);
            notifyItemRangeInserted(startPosition, mList.size());
        }
    }

    public void clear() {
        if (mList != null && mList.size() > 0) {
            mList.clear();
        }
    }

    private String getReserveTimeIn(String dateFormat, long reserTimeinMillis) {
        return DateUtils.getTime(dateFormat, reserTimeinMillis);
    }

    @Override
    public int getItemCount() {
        return (null != mList ? mList.size() : 0);
    }

    private class DriveHistoryItemViewHolder extends RecyclerView.ViewHolder {
        TextView scheduleTime;
        TextView depature;
        TextView departureDetail;
        TextView destination;
        TextView historyTvAllocationStatus;
        ConstraintLayout ll_schedule_item;
        boolean isSingle;  // true: 싱글 / false: 멀티

        DriveHistoryItemViewHolder(View view, boolean isSingle) {
            super(view);
            this.scheduleTime = view.findViewById(R.id.schedule_time);
            this.depature = view.findViewById(R.id.departure);
            this.destination = view.findViewById(R.id.destination);
            this.historyTvAllocationStatus = view.findViewById(R.id.historyTvAllocationStatus);
            this.ll_schedule_item = view.findViewById(R.id.ll_schedule_item);
            this.isSingle = isSingle;

            if(! isSingle) {
                this.departureDetail = view.findViewById(R.id.departureDetail);
            }
        }

        void init(final int pos) {
            scheduleTime.setText(getReserveTimeIn("yy/MM/dd HH:mm", mList.get(pos).resvDatetime));

            if(! isSingle) {
                depature.setText("출발지: " + mList.get(pos).resvOrgPoi);
                departureDetail.setText(mList.get(pos).resvOrgAddress);
            } else {
                depature.setText("출발지: " + mList.get(pos).resvOrgAddress);
            }

            if(!TextUtils.isEmpty(mList.get(pos).resvDstPoi)) {
                destination.setText( "도착지: " + mList.get(pos).resvDstPoi);
            } else {
                destination.setText( "도착지: " + mList.get(pos).resvDstAddress);
            }

            if(mList.get(pos).allocationStatus.equalsIgnoreCase(AppDef.AllocationStatus.CHECKOUT.toString())) {
                //setImage(historyTvAllocationStatus, activityWeakReference.get().getDrawable(R.drawable.rec_complete_drive_history));
                historyTvAllocationStatus.setBackgroundResource(R.drawable.rec_complete_drive_history);
                historyTvAllocationStatus.setText("완료");

            } else if(mList.get(pos).allocationStatus.equalsIgnoreCase(AppDef.AllocationStatus.NOSHOW.toString())) {
                //setImage(historyTvAllocationStatus, activityWeakReference.get().getDrawable(R.drawable.rec_cancel_drive_history));
                historyTvAllocationStatus.setPadding(20,0,20,0);
                historyTvAllocationStatus.setBackgroundResource(R.drawable.rec_cancel_drive_history);
                historyTvAllocationStatus.setText("승객미탑승");
            } else if(mList.get(pos).allocationStatus.equalsIgnoreCase(AppDef.AllocationStatus.CANCELED.toString())) {
                //setImage(historyTvAllocationStatus, activityWeakReference.get().getDrawable(R.drawable.rec_miss_drive_history));
                if(mList.get(pos).cancelReasonCat != null && mList.get(pos).cancelReasonCat.equals("UNRUNEND"))
                {
                    historyTvAllocationStatus.setPadding(20,0,20,0);
                    historyTvAllocationStatus.setBackgroundResource(R.drawable.rec_miss_drive_history);
                    historyTvAllocationStatus.setText(R.string.txt_unrun);
                }
                else {
                    historyTvAllocationStatus.setBackgroundResource(R.drawable.rec_cancel_drive_history);
                    historyTvAllocationStatus.setText("취소");
                }
            } else {
                //setImage(historyTvAllocationStatus, activityWeakReference.get().getDrawable(R.drawable.rec_cancel_drive_history));
                historyTvAllocationStatus.setBackgroundResource(R.drawable.rec_cancel_drive_history);
                historyTvAllocationStatus.setText("취소");
            }

            ll_schedule_item.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    Intent intent = new Intent(activityWeakReference.get(), DriveHistoryDetailActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("a_detail", mList.get(pos).allocationIdx);
                    activityWeakReference.get().startActivity(intent);
                    activityWeakReference.get().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                }
            });
        }
    }
}
