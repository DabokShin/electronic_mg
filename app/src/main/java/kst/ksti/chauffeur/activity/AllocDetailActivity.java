package kst.ksti.chauffeur.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.skt.Tmap.TMapTapi;
import com.skt.Tmap.TMapView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import kst.ksti.chauffeur.BuildConfig;
import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.common.AnalyticsHelper;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.common.Global;
import kst.ksti.chauffeur.databinding.ActivityAllocdetailBinding;
import kst.ksti.chauffeur.listner.ChangeStatusInterface;
import kst.ksti.chauffeur.model.AllocationSchedule;
import kst.ksti.chauffeur.net.DataInterface;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.MacaronCustomDialog;
import kst.ksti.chauffeur.utility.OnSingleClickListener;
import kst.ksti.chauffeur.utility.Util;

public class AllocDetailActivity extends BaseActivity<ActivityAllocdetailBinding> {

    private long alloc_idx = -1;
    private String a_title;
    private boolean a_flags;
    private long a_date = -1;
    private boolean isCallCustomer = true;
    private MacaronCustomDialog macaronCustomDialog;
    private boolean isThreeTimeCheck = false;  // true : 예약시간 3시간 이내  /  false :  예약시간 3시간 초과

    public static final int ACTIVITY_TYPE_DEFAULT = 0;
    public static final int ACTIVITY_TYPE_DRIVE   = 1;
    public static final int ACTIVITY_TYPE_RESV    = 2;
    private int allocActivityType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.d(getResources().getString(R.string.begin_activity));    // 액티비티 호출 로그

        Thread.setDefaultUncaughtExceptionHandler(((MacaronApp)getApplication()).getUncaughtExceptionHandler(AllocDetailActivity.this));

        //최초예약리스트 와 내용이 다를수 있음.
        Intent intent = getIntent();
        if (intent != null) {
            alloc_idx = intent.getLongExtra("a_detail", -1);
            a_title = intent.getStringExtra("a_title");
            a_flags = intent.getBooleanExtra("a_flags", true);
            allocActivityType = intent.getIntExtra("allocActivityType", 0);
            a_date = intent.getLongExtra("a_date", -1);
            isCallCustomer = intent.getBooleanExtra("call_customer", true);
        }

        setBind(R.layout.activity_allocdetail);
        initTitleBar();

        setLoadingBarLayout(getBind().loadingBarLayout);
        setProgressBar(getBind().progressBar);

        getBind().btnCallCustomer.setOnClickListener(onSingleClickListener);
        getBind().btnConfirm.setOnClickListener(onSingleClickListener);
        getBind().btnStartDrive.setOnClickListener(onSingleClickListener);

        // 고객에게 전화하기 버튼 유무
        if(isCallCustomer) {
            getBind().btnCallCustomer.setVisibility(View.VISIBLE);
        }
        else {
            getBind().btnCallCustomer.setVisibility(View.GONE);
        }

        playLoadingViewAnimation();
        getAllocation(alloc_idx);
    }


    public static int beforeTimeMillisToHourMinute(long resvTime) {
        long dateCurr = System.currentTimeMillis();

        if (resvTime < dateCurr)
            return -1;

        return (int) ((resvTime - dateCurr) / (1000 * 60 * 60 ));
    }

    @Override
    protected void onResume() {
        super.onResume();
        AnalyticsHelper.getInstance(this).sendScreenFromJson(this, AllocDetailActivity.class.getSimpleName());
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private OnSingleClickListener onSingleClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            switch (v.getId()) {
                case R.id.btnCallCustomer:  // 고객에게 전화하기
                    Call(MacaronApp.currAllocation.safePhoneno);
                    break;
                case R.id.btnStartDrive:
                    if(a_date > -1) {
                        isThreeTimeCheck = (beforeTimeMillisToHourMinute(a_date) < 3);
                    }
                    showIsStartDriveDialog(isThreeTimeCheck);
                    break;

                case R.id.btnConfirm:
                    closeAllocDetail();
                    break;
            }
        }
    };

    /**
     * 출발 확인팝업
     */
    private void showIsStartDriveDialog(boolean isThreeTime) {
        if(macaronCustomDialog != null && macaronCustomDialog.isShowing()) {
            macaronCustomDialog.dismiss();
        }

        String title = "출발 확인";
        String message;

        if(isThreeTime) {
            message = "가맹점 위치로 이동하시겠습니까?";

            macaronCustomDialog = new MacaronCustomDialog(AllocDetailActivity.this, title, message, "확인", "취소",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            macaronCustomDialog.dismiss();
                            startDrive();
                        }
                    },
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            macaronCustomDialog.dismiss();
                        }
                    }, false);

        } else {
            message = "가맹점 위치 이동은 3시간 전부터 가능합니다.";

            macaronCustomDialog = new MacaronCustomDialog(AllocDetailActivity.this, title, message, "확인",
                   new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            macaronCustomDialog.dismiss();
                        }
                    }, false);
        }

        try {
            macaronCustomDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startDrive() {
        playLoadingViewAnimation();

        try {
            TMapView mMapView = new TMapView(AllocDetailActivity.this);
            mMapView.setSKTMapApiKey(Global.TMAP_APIKEY);
            TMapTapi tMapTapi = new TMapTapi(AllocDetailActivity.this);

            if(tMapTapi.isTmapApplicationInstalled()) {
                sendAllocStatusAndGoNextScreen(MacaronApp.currAllocation.allocationIdx, AppDef.AllocationStatus.DEPART, changeStatusInterfaceAllocStatus);

            } else {
                cancelLoadingViewAnimation();
                ArrayList result = tMapTapi.getTMapDownUrl();
                if(result != null) {
                    showTmapNotInstallDialog(result);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            cancelLoadingViewAnimation();
        }
    }

    private void closeAllocDetail() {
        finish();

        switch (allocActivityType) {
            case ACTIVITY_TYPE_DEFAULT:
                overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                break;

            case ACTIVITY_TYPE_DRIVE:
                overridePendingTransition(R.anim.hold, R.anim.push_out_up);
                break;

            case ACTIVITY_TYPE_RESV:
                overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                break;
        }
    }

    private ChangeStatusInterface changeStatusInterfaceAllocStatus = new ChangeStatusInterface() {
        @Override
        public void onSuccess(ResponseData<Object> response) {
            AnalyticsHelper.getInstance(AllocDetailActivity.this).sendEvent("탑승시작", "탑승시작 성공", "", Global.FA_EVENT_NAME.CHAUFFEUR_DEPART);

            Intent intent = new Intent(AllocDetailActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("a_type", "allocDetail");
            startActivity(intent);
            overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
        }

        @Override
        public void onErrorCode(ResponseData<Object> response) {
            AnalyticsHelper.getInstance(AllocDetailActivity.this).sendEvent("탑승시작", "탑승시작 실패", "", Global.FA_EVENT_NAME.CHAUFFEUR_DEPART);
            showErrorCodeDialog(AllocDetailActivity.this, response.getError());
            cancelLoadingViewAnimation();
        }

        @Override
        public void onError() {
            AnalyticsHelper.getInstance(AllocDetailActivity.this).sendEvent("탑승시작", "탑승시작 실패", "", Global.FA_EVENT_NAME.CHAUFFEUR_DEPART);
            cancelLoadingViewAnimation();
        }

        @Override
        public void onFailed(Throwable t) {
            AnalyticsHelper.getInstance(AllocDetailActivity.this).sendEvent("탑승시작", "탑승시작 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.CHAUFFEUR_DEPART);
            cancelLoadingViewAnimation();
        }
    };

    /**
     * 에러 다이얼로그
     */
    public void showErrorCodeDialog(Context context, String msg) {
        if(macaronCustomDialog != null && macaronCustomDialog.isShowing()) {
            macaronCustomDialog.dismiss();
        }

        macaronCustomDialog = new MacaronCustomDialog(context, null, msg, "확인", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                macaronCustomDialog.dismiss();
                finish();
                overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
            }
        });

        try {
            macaronCustomDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getReserveTimeString(long milliSeconds) {
        if (milliSeconds == 0) {
            return "";
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR);
        int min = calendar.get(Calendar.MINUTE);

        String amPm;
        if (calendar.get(Calendar.AM_PM) == 0)
            amPm = "오전";
        else
            amPm = "오후";

        if(hour == 0 && amPm.equals("오후")) {
            hour = 12;
        }

        return year + "년 " + month + "월 " + day + "일 " + amPm + " " + hour + "시 " + min + "분";
    }

    private void initTitleBar() {
        if (getBind().title2 != null) {
            String arrowBack = "y";
            if ("y".equals(arrowBack)) {
                getBind().title2.btnTitleBack.setVisibility(View.VISIBLE);
                getBind().title2.btnDrawerOpen.setVisibility(View.GONE);
            } else {
                getBind().title2.btnTitleBack.setVisibility(View.GONE);
                getBind().title2.btnDrawerOpen.setVisibility(View.VISIBLE);
            }

            getBind().title2.tvTitle.setText(a_title);
            getBind().title2.ivDivider.setVisibility(View.VISIBLE);
            getBind().title2.btnTitleBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                }
            });

            if (!a_flags) {
                getBind().btnStartDrive.setVisibility(View.GONE);
            }

            switch (allocActivityType) {
                case ACTIVITY_TYPE_DEFAULT:
                    break;

                case ACTIVITY_TYPE_DRIVE:
                    getBind().btnConfirm.setText("확인");
                    getBind().title2.btnTitleBack.setVisibility(View.GONE);

                    getBind().title2.tvTitle.setTextColor(Color.parseColor("#000000"));
                    getBind().title2.tvTitle.setBackgroundResource(0);
                    getBind().title2.tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                    break;

                case ACTIVITY_TYPE_RESV:
                    getBind().title2.btnTitleBack.setVisibility(View.VISIBLE);
                    getBind().btnLayout.setVisibility(View.GONE);

                    getBind().title2.tvTitle.setTextColor(Color.parseColor("#000000"));
                    getBind().title2.tvTitle.setBackgroundResource(0);
                    getBind().title2.tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                    break;
            }

            if(BuildConfig.DEBUG) {
                if(Global.getDEV()) {
                    String dev_type = Global.getServerType();
                    getBind().title2.tvTitleServerChange.setText(dev_type);
                } else {
                    getBind().title2.tvTitleServerChange.setText("");
                }
                getBind().title2.tvTitleServerChange.setVisibility(View.VISIBLE);

            } else {
                getBind().title2.tvTitleServerChange.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 예약현황정보 요청
     * @param alloc_idx idx
     */
    private void getAllocation(long alloc_idx) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("allocationIdx", alloc_idx);

        DataInterface.getInstance().getAllocation(AllocDetailActivity.this, params, new DataInterface.ResponseCallback<ResponseData<AllocationSchedule>>() {
            @Override
            public void onSuccess(ResponseData<AllocationSchedule> response) {
                if ("S000".equals(response.getResultCode())) {
                    initUI(response.getData());
                }
                else if (Global.ErrorCode.EC201.equals(response.getResultCode())) {
                    showErrorCodeDialog(AllocDetailActivity.this, response.getError());
                }else {
                    Logger.d("예약상세 receive 실패");
                }

                cancelLoadingViewAnimation();
            }

            @Override
            public void onError(ResponseData<AllocationSchedule> response) {
                cancelLoadingViewAnimation();
            }

            @Override
            public void onFailure(Throwable t) {
                cancelLoadingViewAnimation();
            }
        });

    }

    @SuppressLint("DefaultLocale")
    private void initUI(AllocationSchedule allocationSchedule) {
        if (MacaronApp.nearByDrivingStatusCheck) {
            MacaronApp.currAllocation = allocationSchedule;
        }

        if(allocationSchedule != null) {
            showDate(allocationSchedule);
            showDrivePath(allocationSchedule);
            showServiceList(allocationSchedule);
            showPaymentInfo(allocationSchedule);
            showEstmInfo(allocationSchedule);

        } else {
            Toast.makeText(AllocDetailActivity.this, "에러가 발생하였습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 일정
     */
    private void showDate(AllocationSchedule allocationSchedule) {
        try {
            getBind().reserveTime.setText(getReserveTimeString(allocationSchedule.resvDatetime));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 경로
     */
    private void showDrivePath(AllocationSchedule allocationSchedule) {
        try {
            String poi = "";
            String address = "";
            if(!TextUtils.isEmpty(allocationSchedule.resvOrgPoi))
                poi = allocationSchedule.resvOrgPoi;
            else
                poi = allocationSchedule.resvOrgAddress;

            if(!TextUtils.isEmpty(allocationSchedule.resvOrgAddress)) {
                address = allocationSchedule.resvOrgAddress;
            }

            getBind().org.setText(poi);
            getBind().orgAddress.setText(address);

            int lineCnt = getBind().org.getMaxLines();

            for(int i = 0; i < lineCnt; i++) {
                ImageView imageView = new ImageView(AllocDetailActivity.this);
                imageView.setImageResource(R.drawable.ellipse_1_copy_2);

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.topMargin = (int)Util.convertDpToPixel(5, AllocDetailActivity.this);
                layoutParams.leftMargin = (int)Util.convertDpToPixel(7, AllocDetailActivity.this);
                imageView.setLayoutParams(layoutParams);

                getBind().repeatDotLayout.addView(imageView);
            }

            if(!TextUtils.isEmpty(allocationSchedule.resvDstPoi))
                poi = allocationSchedule.resvDstPoi;
            else
                poi = allocationSchedule.resvDstAddress;

            if(!TextUtils.isEmpty(allocationSchedule.resvDstAddress)) {
                address = allocationSchedule.resvDstAddress;
            }

            getBind().dst.setText(poi);
            getBind().dstAddress.setText(address);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 서비스
     */
    private void showServiceList(AllocationSchedule allocationSchedule) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            if (allocationSchedule.serviceNameList != null && allocationSchedule.serviceNameList.size() > 0) {
                for(int i=0; i<allocationSchedule.serviceNameList.size(); i++) {
                    stringBuilder.append(allocationSchedule.serviceNameList.get(i));

                    if(i < allocationSchedule.serviceNameList.size() - 1) {
                        stringBuilder.append(", ");
                    }
                }

                getBind().tvServiceKind.setText(stringBuilder.toString());
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 결제방식
     */
    private void showPaymentInfo(AllocationSchedule allocationSchedule) {
        try {
            getBind().fareCat.setText(getFareCatStr(allocationSchedule.fareCat));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 결제방식 - 현장결제, 앱결제
     */
    private String getFareCatStr(String fareCat) {
        if (fareCat == null)
            return "";

        switch (fareCat) {
            case "OFFLINE":
                return "직접 결제";
            case "APPCARD":
                return "앱 결제";
            default:
                return "";
        }
    }

    /**
     * 예상거리, 예상시간
     */
    private void showEstmInfo(AllocationSchedule allocationSchedule) {
        try {
            String td = String.format("%.1f", (allocationSchedule.estmDist * 0.001));
            long hour = allocationSchedule.estmTime / 3600;
            long min = allocationSchedule.estmTime % 3600 / 60;
            long totalMin = hour * 60 + min;
            getBind().estDist.setText(td + "km" + " " + totalMin + "분");
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

//        if(!PrefUtil.getBackKeyCheck(AllocDetailActivity.this)) {
//            super.onBackPressed();
//        }
    }
}
