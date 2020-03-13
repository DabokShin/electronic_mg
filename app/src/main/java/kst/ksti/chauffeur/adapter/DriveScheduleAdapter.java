package kst.ksti.chauffeur.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.activity.AllocDetailActivity;
import kst.ksti.chauffeur.activity.MainActivity;
import kst.ksti.chauffeur.model.AllocationSchedule;
import kst.ksti.chauffeur.utility.DateUtils;
import kst.ksti.chauffeur.utility.OnSingleClickListener;
import kst.ksti.chauffeur.utility.Util;


public class DriveScheduleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<AllocationSchedule> mList;
    private WeakReference<MainActivity> activityWeakReference;

    private static final int SINGLE_LINE = 1;
    private static final int MULTI_LINE  = 2;

    public static final int CALL_TYPE_DRIVING = 0;  // 정상호출
    public static final int CALL_TYPE_RESV    = 1;  // 좌측메뉴 예약목록
    private int callType;

    public DriveScheduleAdapter(Context context, ArrayList<AllocationSchedule> list, int callType) {
        activityWeakReference = new WeakReference<>((MainActivity)context);
        this.mList = list;
        this.callType = callType;
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

    private class ScheduleItemViewHolder extends RecyclerView.ViewHolder {
        TextView scheduleTime;
        TextView tvOriPoi;
        TextView tvOriAddress;
        TextView tvResvPoi;
        TextView tvResvAddress;
        TextView imgNum;
        TextView beforeSchedule;
        LinearLayout ll_schedule_item;
        boolean isSingle;  // true: 싱글 / false: 멀티

        ScheduleItemViewHolder(View view, boolean isSingle) {
            super(view);
            this.scheduleTime = view.findViewById(R.id.schedule_time);
            this.beforeSchedule = view.findViewById(R.id.before_schedule_time);
            this.tvOriPoi = view.findViewById(R.id.orgPoi);
            this.tvOriAddress = view.findViewById(R.id.oriAddress);
            this.tvResvPoi = view.findViewById(R.id.resvPoi);
            this.tvResvAddress = view.findViewById(R.id.resvAddress);
            this.imgNum = view.findViewById(R.id.schedule_num);
            this.ll_schedule_item = view.findViewById(R.id.ll_schedule_item);
            this.isSingle = isSingle;
        }

        void init(final int pos) {
            if(mList == null || mList.size() <= pos)
                return;

            // 예약 날짜 셋팅
            String MMdd = getReserveTimeIn("MM/dd", mList.get(pos).resvDatetime);
            String HHmm = getReserveTimeIn("HH:mm", mList.get(pos).resvDatetime);
            String convertedString = Util.getDayOfWeek(mList.get(pos).resvDatetime);

            String resvDateTime = MMdd + "/" + convertedString + "  " + HHmm;
            scheduleTime.setText(resvDateTime);

            String resvDate = getReserveTimeIn("MM/dd", mList.get(pos).resvDatetime);    // 예약 날짜
            String nowDate = DateUtils.getCurrentDate("MM/dd");                             // 현재 날짜

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_WEEK, 1);
            Date tomorrow = calendar.getTime();
            String tomorrowDate = DateUtils.getDate("MM/dd", tomorrow);
            int afterDay =DateUtils.afterDay(mList.get(pos).resvDatetime);

            // 미운행
            if(mList.get(pos).unrunYn != null && mList.get(pos).unrunYn.equals("Y"))
            {
                ll_schedule_item.setBackgroundResource(R.drawable.schedule_box_gray);
                imgNum.setBackgroundResource(R.drawable.small_gray_box);
                imgNum.setText(R.string.txt_unrun);
                imgNum.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                imgNum.setTextColor(Color.parseColor("#636363"));

                beforeSchedule.setText("");
            }
            else {
                if(resvDate.equals(nowDate))
                {
                    ll_schedule_item.setBackgroundResource(R.drawable.schedule_box_blue);
                    imgNum.setBackgroundResource(R.drawable.small_white_box);
                    imgNum.setText("오늘");
                    imgNum.setTextColor(Color.parseColor("#000000"));

                    beforeSchedule.setText(getDifferenceFromResvTime(mList.get(pos).resvDatetime));

                }
                else if(resvDate.equals(tomorrowDate))
                {
                    ll_schedule_item.setBackgroundResource(R.drawable.schedule_box_gray);
                    imgNum.setBackgroundResource(R.drawable.small_gray_box);
                    imgNum.setText("내일");
                    imgNum.setTextColor(Color.parseColor("#636363"));

                    beforeSchedule.setText(getDifferenceFromResvTime(mList.get(pos).resvDatetime));
                }
                else
                {
                    ll_schedule_item.setBackgroundResource(R.drawable.schedule_box_gray);
                    imgNum.setBackgroundResource(R.drawable.small_gray_box);
                    imgNum.setText("D+" + afterDay);
                    imgNum.setTextColor(Color.parseColor("#636363"));

                    beforeSchedule.setText("");
                }
            }


            if(pos == 0)    // 첫 번째 예약만 글자에 색깔을 넣는다
                beforeSchedule.setTextColor(Color.parseColor("#cc0000"));
            else
                beforeSchedule.setTextColor(Color.parseColor("#FFFFFF"));

            ll_schedule_item.setPadding(0, 0, 0, 0);



            if(!TextUtils.isEmpty(mList.get(pos).resvOrgPoi)) {
                tvOriPoi.setText(mList.get(pos).resvOrgPoi);
            }
            else {
                tvOriPoi.setText(mList.get(pos).resvOrgAddress);
            }
            tvOriAddress.setText(mList.get(pos).resvOrgAddress);

            if(!TextUtils.isEmpty(mList.get(pos).resvDstPoi)) {
                tvResvPoi.setText(mList.get(pos).resvDstPoi);
            } else {
                tvResvPoi.setText(mList.get(pos).resvDstAddress);
            }
            tvResvAddress.setText(mList.get(pos).resvDstAddress);

            ll_schedule_item.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    checkAllocationStatus(mList.get(pos).allocationStatus, pos);
                }
            });
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_drive_schedule, viewGroup, false);
        return new ScheduleItemViewHolder(view, false);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder scheduleItem, int i) {
        ((ScheduleItemViewHolder)scheduleItem).init(i);
    }

    private void checkAllocationStatus(String status, int position) {
        switch (status) {
            case "ALLOCATED": {
                Intent intent = new Intent(activityWeakReference.get(), AllocDetailActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("a_detail", mList.get(position).allocationIdx);
                intent.putExtra("a_title", "예약 상세");

                switch (callType) {
                    case CALL_TYPE_DRIVING:
                        intent.putExtra("a_flags", true);
                        intent.putExtra("a_date", mList.get(position).resvDatetime);
                        intent.putExtra("allocActivityType", AllocDetailActivity.ACTIVITY_TYPE_DEFAULT);
                        break;

                    case CALL_TYPE_RESV:
                        intent.putExtra("a_flags", false);
                        intent.putExtra("allocActivityType", AllocDetailActivity.ACTIVITY_TYPE_RESV);
                        break;
                }

                activityWeakReference.get().playLoadingViewAnimation();
                activityWeakReference.get().startActivity(intent);
                activityWeakReference.get().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                break;
            }

            default: {
                if(callType == CALL_TYPE_RESV) {
                    Intent intent = new Intent(activityWeakReference.get(), AllocDetailActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("a_detail", mList.get(position).allocationIdx);
                    intent.putExtra("a_title", "예약 상세");
                    intent.putExtra("a_flags", false);
                    intent.putExtra("allocActivityType", AllocDetailActivity.ACTIVITY_TYPE_RESV);

                    activityWeakReference.get().playLoadingViewAnimation();
                    activityWeakReference.get().startActivity(intent);
                    activityWeakReference.get().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                }
                break;
            }
        }
    }

    public void addItems(List<AllocationSchedule> object) {
        if (mList != null) {
            int startPosition = mList.size();
            mList.addAll(object);
            notifyItemRangeInserted(startPosition, mList.size());
        }
    }

    private String getDifferenceFromResvTime(long resvTime) {
        return beforeTimeMillisToHourMinute(resvTime, System.currentTimeMillis());
    }

    /*
     * 현재시간과의 차이를 "시간:분"으로 나타내준다.
     */
    public static String beforeTimeMillisToHourMinute(long date1, long dateCurr) {

        if (date1 < dateCurr)
            return "";
        int diffInHour = 0;
        int diffnMin = 0;

        diffInHour = (int) ((date1 - dateCurr) / (1000 * 60 * 60 ));
        int remaining = (int) ((date1 - dateCurr) % (1000 * 60 * 60 ));
        diffnMin = remaining / (1000 * 60 );

        String hour = String.valueOf(diffInHour);
        String minute = String.valueOf(diffnMin);

        if(hour.length() < 2) hour = "0"+hour;
        if(minute.length() < 2) minute = "0"+minute;

        return hour + ":" + minute;
    }

    private String getReserveTimeIn(String dateFormat, long reserTimeinMillis) {
        return DateUtils.getTime(dateFormat, reserTimeinMillis);
    }

    @Override
    public int getItemCount() {
        return (null != mList ? mList.size() : 0);
    }

}
