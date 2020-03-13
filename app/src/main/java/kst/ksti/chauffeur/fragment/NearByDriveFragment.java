package kst.ksti.chauffeur.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
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

import kst.ksti.chauffeur.common.Global;
import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.common.AnalyticsHelper;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.databinding.FrNearbyDriveBinding;
import kst.ksti.chauffeur.listner.ChangeStatusInterface;
import kst.ksti.chauffeur.listner.OnTitleListener;
import kst.ksti.chauffeur.model.AllocationSchedule;
import kst.ksti.chauffeur.net.DataInterface;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.MacaronCustomDialog;
import kst.ksti.chauffeur.utility.OnSingleClickListener;
import kst.ksti.chauffeur.utility.Util;

/**
 * 운행임박 화면
 */
public class NearByDriveFragment extends NativeFragment implements OnTitleListener {

    private FrNearbyDriveBinding mBind;
    private ArrayList<String> serviceList = null;
    private long idx = 0;
    private int count = 0;
    private long a_date = -1;
    private MacaronCustomDialog macaronCustomDialog;

    private boolean isThreeTimeCheck = false;  // true : 예약시간 3시간 이내  /  false :  예약시간 3시간 초과

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nativeBaseActivity.cancelLoadingViewAnimation();

        Bundle bundle = getArguments();
        if (bundle != null) {
            idx = Long.valueOf(bundle.getString("idx", "0"));
            count = Integer.valueOf(bundle.getString("scheduleCount", "0"));
            a_date = bundle.getLong("a_date", -1);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fr_nearby_drive, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Logger.d(getResources().getString(R.string.begin_fragment));    // 프래그먼트 호출 로그
        
        SetTitle("배달임박");

        SetDividerVisibility(true);
        SetTitleListener(this);
        setDrawerLayoutEnable(false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AnalyticsHelper.getInstance(getContext()).sendScreenFromJson(nativeMainActivity, getClass().getSimpleName());

        mBind = FrNearbyDriveBinding.bind(getView());

        initUI();
        initEventListener();

        getAllocation();

        // 탑 화면 세팅
        MacaronApp.topActivity = Global.TOP_SCREEN.NEARBYDRIVE;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // 탑 화면 세팅
        MacaronApp.topActivity = Global.TOP_SCREEN.MAIN;
    }

    /**
     * UI 초기화
     */
    private void initUI() {
        mBind.title2.tvTitle.setBackgroundResource(R.drawable.rounded_rectangle_pink);
        mBind.title2.btnDrawerOpen.setVisibility(View.GONE);
        mBind.title2.btnTitleBack.setVisibility(View.GONE);

        if(MacaronApp.nearByDrivingStatusCheck) {
            mBind.btnTmapStart.setText("출발");
        } else {
            mBind.btnTmapStart.setText("확인");
        }
    }

    /**
     * 이벤트 리스너 관리
     */
    private void initEventListener() {
        mBind.title2.btnDrawerOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventForTitleView(v);
            }
        });
        mBind.title2.btnTitleBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventForTitleView(v);
            }
        });

        //운행임박시 네비게이션을 실행시키고 출발지 도착을 화면에 띄어놓는다.
        mBind.btnTmapStart.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if(MacaronApp.nearByDrivingStatusCheck) {
                    // 시작버튼
                    if(a_date > -1) {
                        isThreeTimeCheck = (beforeTimeMillisToHourMinute(a_date) < 3);
                    }
                    showIsStartDriveDialog(isThreeTimeCheck);

                } else {
                    // 확인버튼
                    nativeBaseActivity.GoNativeBackStack();
                }
            }
        });
    }

    public static int beforeTimeMillisToHourMinute(long resvTime) {
        long dateCurr = System.currentTimeMillis();

        if (resvTime < dateCurr)
            return -1;

        return (int) ((resvTime - dateCurr) / (1000 * 60 * 60 ));
    }

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

            macaronCustomDialog = new MacaronCustomDialog(nativeMainActivity, title, message, "확인", "취소",
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

            macaronCustomDialog = new MacaronCustomDialog(nativeMainActivity, title, message, "확인",
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

        // 탑 화면 세팅
        MacaronApp.topActivity = Global.TOP_SCREEN.MAIN;

        nativeBaseActivity.playLoadingViewAnimation();

        try {
            TMapView mMapView = new TMapView(nativeMainActivity);
            mMapView.setSKTMapApiKey(Global.TMAP_APIKEY);
            TMapTapi tMapTapi = new TMapTapi(nativeMainActivity);

            if(tMapTapi.isTmapApplicationInstalled()) {
                changeAllocStatusAndGoNextScreen(MacaronApp.currAllocation.allocationIdx, AppDef.AllocationStatus.DEPART, changeStatusInterfaceAllocStatus);

            } else {
                nativeBaseActivity.cancelLoadingViewAnimation();
                ArrayList result = tMapTapi.getTMapDownUrl();
                if(result != null) {
                    nativeBaseActivity.showTmapNotInstallDialog(result);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            nativeBaseActivity.cancelLoadingViewAnimation();
        }
    }

    /**
     * 배차 상태변경 콜백
     */
    private ChangeStatusInterface changeStatusInterfaceAllocStatus = new ChangeStatusInterface() {
        @Override
        public void onSuccess(ResponseData<Object> response) {
            AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("탑승시작", "탑승시작 성공", "", Global.FA_EVENT_NAME.CHAUFFEUR_DEPART);

            if(count > 0) {
                MacaronApp.scheduleCount = count;
            }
            GoNativeScreenReplaceAdd(new OrgArrivedFragment(), null, 1);
        }

        @Override
        public void onErrorCode(ResponseData<Object> response) {
            AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("탑승시작", "탑승시작 실패", "", Global.FA_EVENT_NAME.CHAUFFEUR_DEPART);

            switch (response.getResultCode()) {
                case Global.ErrorCode.EC901:
                    showErrorCode901_Dialog(nativeMainActivity, response.getError());
                    break;
                case Global.ErrorCode.EC902:
                    showErrorCode902_Dialog(nativeMainActivity, response.getError());
                    break;
                case Global.ErrorCode.EC201:
                    showErrorCode201_Dialog(nativeMainActivity, response.getError());
                    break;
                default:
                    showErrorCodeEtcDialog(nativeMainActivity, response.getError());
                    break;
            }
            nativeBaseActivity.cancelLoadingViewAnimation();
        }

        @Override
        public void onError() {
            AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("탑승시작", "탑승시작 실패", "", Global.FA_EVENT_NAME.CHAUFFEUR_DEPART);
            nativeBaseActivity.cancelLoadingViewAnimation();
        }

        @Override
        public void onFailed(Throwable t) {
            AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("탑승시작", "탑승시작 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.CHAUFFEUR_DEPART);
            nativeBaseActivity.cancelLoadingViewAnimation();
        }
    };

    /**
     * 예약 상세정보 호출
     */
    private void getAllocation() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("allocationIdx", idx);

        DataInterface.getInstance().getAllocation(nativeMainActivity, params, new DataInterface.ResponseCallback<ResponseData<AllocationSchedule>>() {
            @Override
            public void onSuccess(ResponseData<AllocationSchedule> response) {
                if ("S000".equals(response.getResultCode())) {
                    if(MacaronApp.nearByDrivingStatusCheck) {
                        MacaronApp.currAllocation = response.getData();
                    }

                    initNearByInfoUI(response.getData());

                } else {
                    Logger.d("예약상세 receive 실패");
                }

                nativeBaseActivity.cancelLoadingViewAnimation();
            }

            @Override
            public void onError(ResponseData<AllocationSchedule> response) {
                nativeBaseActivity.cancelLoadingViewAnimation();
            }

            @Override
            public void onFailure(Throwable t) {
                nativeBaseActivity.cancelLoadingViewAnimation();
            }
        });
    }

    private void initNearByInfoUI(AllocationSchedule allocationSchedule) {
        if(allocationSchedule != null) {
            showDate(allocationSchedule);
            showDrivePath(allocationSchedule);
            showServiceList(allocationSchedule);
            showPaymentInfo(allocationSchedule);
            showEstmInfo(allocationSchedule);

        } else {
            Toast.makeText(nativeMainActivity, "에러가 발생하였습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 일정
     */
    private void showDate(AllocationSchedule allocationSchedule) {
        try {
            mBind.reserveTime.setText(getReserveTimeString(allocationSchedule.resvDatetime));
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

            mBind.org.setText(poi);
            mBind.orgAddress.setText(address);

            int lineCnt = mBind.org.getMaxLines();

            for(int i = 0; i < lineCnt; i++) {
                ImageView imageView = new ImageView(nativeMainActivity);
                imageView.setImageResource(R.drawable.ellipse_1_copy_2);

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.topMargin = (int)Util.convertDpToPixel(5, nativeMainActivity);
                layoutParams.leftMargin = (int)Util.convertDpToPixel(7, nativeMainActivity);
                imageView.setLayoutParams(layoutParams);

                mBind.repeatDotLayout.addView(imageView);
            }

            if(!TextUtils.isEmpty(allocationSchedule.resvDstPoi))
                poi = allocationSchedule.resvDstPoi;
            else
                poi = allocationSchedule.resvDstAddress;

            if(!TextUtils.isEmpty(allocationSchedule.resvDstAddress)) {
                address = allocationSchedule.resvDstAddress;
            }

            mBind.dst.setText(poi);
            mBind.dstAddress.setText(address);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 서비스
     */
    private void showServiceList(AllocationSchedule allocationSchedule) {
        try {
            serviceList = allocationSchedule.serviceNameList;

            StringBuilder stringBuilder = new StringBuilder();
            if (serviceList != null && serviceList.size() > 0) {
                for(int i=0; i<serviceList.size(); i++) {
                    stringBuilder.append(serviceList.get(i));

                    if(i < serviceList.size() - 1) {
                        stringBuilder.append(", ");
                    }
                }

                mBind.tvServiceKind.setText(stringBuilder.toString());
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
            mBind.fareCat.setText(getFareCatStr(allocationSchedule.fareCat));
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
        if (fareCat.equals("OFFLINE"))
            return "직접결제";
        else if (fareCat.equals("APPCARD"))
            return "앱 결제";
        else
            return "";
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
            mBind.estDist.setText(td + "km" + " " + totalMin + "분");
        } catch (NullPointerException e) {
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

        String amPm = "";
        if (calendar.get(Calendar.AM_PM) == 0)
            amPm = "오전";
        else
            amPm = "오후";

        if (hour == 0 && amPm.equals("오후")) {
            hour = 12;
        }

        return year + "년 " + month + "월 " + day + "일 " + amPm + " " + hour + "시 " + min + "분";
    }

    @Override
    public void onTitleBackPress() {
    }

    @Override
    public void onTitleClosePress() {
    }

    @Override
    public void onSidelistClicked() {
    }

    @Override
    public void doBack() {
    }

}
