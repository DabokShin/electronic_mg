package kst.ksti.chauffeur.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapView;

import java.util.ArrayList;
import java.util.HashMap;

import kst.ksti.chauffeur.activity.WebViewActivity;
import kst.ksti.chauffeur.common.Global;
import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.activity.AllocDetailActivity;
import kst.ksti.chauffeur.common.AnalyticsHelper;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.common.UIThread;
import kst.ksti.chauffeur.databinding.FrDestArrivedBinding;
import kst.ksti.chauffeur.listner.ChangeStatusInterface;
import kst.ksti.chauffeur.listner.OnTitleListener;
import kst.ksti.chauffeur.model.AllocationDist;
import kst.ksti.chauffeur.net.DataInterface;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.OnSingleClickListener;
import kst.ksti.chauffeur.utility.PrefUtil;
import kst.ksti.chauffeur.utility.Util;

/**
 * 운행완료 화면
 */
public class DestArrivedFragment extends NativeFragment implements OnTitleListener {

    private FrDestArrivedBinding mBind;
    private TMapView mMapView;
    private boolean isDstCheck = false;

    private String finishDriveInfo = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MacaronApp.nearByDrivingStatusCheck = false;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fr_dest_arrived, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Logger.d(getResources().getString(R.string.begin_fragment));    // 프래그먼트 호출 로그
        MacaronApp.allocStatus = AppDef.AllocationStatus.CHECKIN;

        SetTitle("고객 위치로 이동");
        SetDividerVisibility(true);
        SetTitleListener(this);
        setDrawerLayoutEnable(false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AnalyticsHelper.getInstance(getContext()).sendScreenFromJson(nativeMainActivity, getClass().getSimpleName());

        mBind = FrDestArrivedBinding.bind(getView());

        initTmap();
        initUI();
        initEventListener();
    }

    /**
     * Tmap의 View, Marker, 호출 부분 초기화
     */
    private void initTmap() {
        mMapView = new TMapView(nativeMainActivity);
        mMapView.setSKTMapApiKey(Global.TMAP_APIKEY);
        mBind.tMapView.addView(mMapView);

        setTmapMarker(mMapView);
        UIThread.executeInUIThread(orgCurrentLocationCheckThread, 5000);

        if(!isDstCheck) {
            isDstCheck = true;

            if (nativeMainActivity.tMapTapi.isTmapApplicationInstalled()) {
                if(! Util.goTmapInvokeRoute(nativeMainActivity.tMapTapi, MacaronApp.currAllocation, false)) {
                    nativeBaseActivity.cancelLoadingViewAnimation();
                    Toast.makeText(nativeMainActivity, "TMap 실행오류", Toast.LENGTH_SHORT).show();
                }

            } else {
                nativeBaseActivity.cancelLoadingViewAnimation();
                ArrayList result = nativeMainActivity.tMapTapi.getTMapDownUrl();
                if(result != null) {
                    nativeBaseActivity.showTmapNotInstallDialog(result);
                }
            }
        }
    }

    /**
     * UI 초기화
     */
    private void initUI() {
        mBind.allocDetailBtn.setVisibility(View.GONE);
        mBind.moveTmapBtn.setVisibility(View.GONE);

        mBind.title.btnDrawerOpen.setVisibility(View.GONE);
        mBind.title.btnDrawerOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventForTitleView(v);
            }
        });

        mBind.title.btnTitleBack.setVisibility(View.GONE);
        mBind.title.btnTitleBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventForTitleView(v);
            }
        });

        String poi = "";
        String address = "";
        if(!TextUtils.isEmpty(MacaronApp.currAllocation.resvDstPoi))
            poi = MacaronApp.currAllocation.resvDstPoi;
        else
            poi = MacaronApp.currAllocation.resvDstAddress;

        address = MacaronApp.currAllocation.resvDstAddress;

        mBind.resvDstPoi.setText(poi);
        mBind.resvAddress.setText(address);
    }

    /**
     * 이벤트 리스너 관리
     */
    private void initEventListener() {
        mBind.btnDestArr.setOnClickListener(onSingleClickListener);
        mBind.btnMyLocation.setOnClickListener(onSingleClickListener);
        mBind.allocDetailBtn.setOnClickListener(onSingleClickListener);
        mBind.moveTmapBtn.setOnClickListener(onSingleClickListener);
    }

    private OnSingleClickListener onSingleClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            switch (v.getId()) {
                case R.id.btnDestArr:
                    destArrBtnAction();
                    break;

                case R.id.btnMyLocation:
                    goTmapViewCenter(true);
                    break;

                case R.id.allocDetailBtn:
//                    goAllocDetailActivity();
                    break;

                case R.id.moveTmapBtn:
//                    moveTmap();
                    break;
            }
        }
    };

    private void goAllocDetailActivity() {
        try {
            Intent intent = new Intent(nativeMainActivity, AllocDetailActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("a_detail", MacaronApp.currAllocation.allocationIdx);
            intent.putExtra("a_title", "예약 상세 보기");
            intent.putExtra("a_flags", false);
            intent.putExtra("allocActivityType", AllocDetailActivity.ACTIVITY_TYPE_DRIVE);
            nativeBaseActivity.playLoadingViewAnimation();
            nativeMainActivity.startActivity(intent);
            nativeMainActivity.overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tmap 재실행 하기
     */
    private void moveTmap() {
        try {
            nativeBaseActivity.playLoadingViewAnimation();

            if(nativeMainActivity.tMapTapi.isTmapApplicationInstalled()) {
                if(! Util.goTmapInvokeRoute(nativeMainActivity.tMapTapi, MacaronApp.currAllocation, false)) {
                    nativeBaseActivity.cancelLoadingViewAnimation();
                    Toast.makeText(nativeMainActivity, "TMap 실행오류", Toast.LENGTH_SHORT).show();
                }

            } else {
                nativeBaseActivity.cancelLoadingViewAnimation();
                Toast.makeText(nativeMainActivity, "TMap 실행오류", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 운행완료버튼 터치
     */
    private void destArrBtnAction() {
        nativeBaseActivity.playLoadingViewAnimation();
        changeAllocStatusAndGoNextScreen(MacaronApp.currAllocation.allocationIdx, AppDef.AllocationStatus.ARRIVAL, changeStatusInterface);
    }

    /**
     * 배차 상태변경 콜백
     */
    private ChangeStatusInterface changeStatusInterface = new ChangeStatusInterface() {
        @Override
        public void onSuccess(ResponseData<Object> response) {
            AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("배달완료", "배달완료 성공", "", Global.FA_EVENT_NAME.CHAUFFEUR_ARRIVAL);
            //switchToPayment(MacaronApp.currAllocation.fareCat);
            GoNativeScreenReplaceAdd(new DrivingFragment(), null, 1);

            Intent intent = new Intent(nativeMainActivity, WebViewActivity.class);
            intent.putExtra("targetUrl", "http://y.admin.macaront.com/feedback/index.do");
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

            startActivity(intent);
        }

        @Override
        public void onErrorCode(ResponseData<Object> response) {
            AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("배달완료", "배달완료 실패", "", Global.FA_EVENT_NAME.CHAUFFEUR_ARRIVAL);
            switch (response.getResultCode()) {
                case Global.ErrorCode.EC901:
                    showErrorCode901_Dialog(nativeMainActivity, response.getError());
                    break;
                case Global.ErrorCode.EC902:
                    showErrorCode902_Dialog(nativeMainActivity, response.getError());
                    break;
                default:
                    showErrorCodeEtcDialog(nativeMainActivity, response.getError());
                    break;
            }
            nativeBaseActivity.cancelLoadingViewAnimation();
        }

        @Override
        public void onError() {
            AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("배달완료", "배달완료 실패", "", Global.FA_EVENT_NAME.CHAUFFEUR_ARRIVAL);
            nativeBaseActivity.cancelLoadingViewAnimation();
        }

        @Override
        public void onFailed(Throwable t) {
            AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("배달완료", "배달완료 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.CHAUFFEUR_ARRIVAL);
            nativeBaseActivity.cancelLoadingViewAnimation();
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        String dist = "0.0Km";
        String fareCat = "";
        String time = "";

        isTMapExecuteCheck = false;

        finishDriveInfo = "";
        switch(MacaronApp.currAllocation.fareCat)
        {
            case "APPCARD":
                fareCat = "앱 결제";
                break;
            case "OFFLINE":
                fareCat = "직접 결제";
                break;
            default:
        }
        float km = MacaronApp.currAllocation.realDist / 1000;
        float m = MacaronApp.currAllocation.realDist  % 1000 * 0.001f;
        dist = String.format("%.1f", (km + m));


        time = beforeTimeMillisToHourMinute(System.currentTimeMillis(), PrefUtil.getStartTime(nativeMainActivity));
        finishDriveInfo = time + " / " + fareCat;
        mBind.tvDriveTime.setText(dist + "km / "+  finishDriveInfo);

        getAllocationRealDist();
    }

    /**
     * 현재시간과의 차이를 "시간:분"으로 나타내준다.
     */
    public static String beforeTimeMillisToHourMinute(long currentTime, long startTime) {
        if (currentTime < startTime)
            return "0시 0분";

        int diffInHour;
        int diffnMin;

        diffInHour = (int) ((currentTime - startTime) / (1000 * 60 * 60 ));
        int remaining = (int) ((currentTime - startTime) % (1000 * 60 * 60 ));
        diffnMin = remaining / (1000 * 60 );

        return String.valueOf(diffInHour) + "시 " + String.valueOf(diffnMin)  + "분" ;
    }

    /**
     * 결제방법에 따른 분기처리
     * @param payCat 결제방법
     */
    private void switchToPayment(String payCat) {
        Bundle bundle = new Bundle();
        switch (payCat) {
            case "APPCARD":
                bundle.putString("arrowBack", "y");
                bundle.putString("title", "앱 결제 요금 입력");
                GoNativeScreen(new InputPayFragment(), bundle, 1);
                break;
            case "OFFLINE":
                bundle.putString("arrowBack", "y");
                bundle.putString("title", "직접 결제 요금 입력");
                GoNativeScreen(new InputPayFragment(), bundle, 1);
                break;
            default:
                Toast.makeText(nativeMainActivity, "결제방식이 존재하지 않습니다." ,Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 운행 이동거리 요청
     */
    private void getAllocationRealDist() {

        HashMap<String, Object> params = new HashMap<>();
        params.put("allocationIdx", MacaronApp.currAllocation.allocationIdx);

        DataInterface.getInstance().getAllocationRealDist(nativeMainActivity, params, new DataInterface.ResponseCallback<ResponseData<AllocationDist>>() {
            @Override
            public void onSuccess(ResponseData<AllocationDist> response) {
                if ("S000".equals(response.getResultCode())) {
                    float km = response.getData().realDist / 1000;
                    float m = response.getData().realDist % 1000 * 0.001f;
                    String dist = String.format("%.1f", (km + m));

                    mBind.tvDriveTime.setText(dist + "km / "+  finishDriveInfo);
                } else {

                }
            }

            @Override
            public void onError(ResponseData<AllocationDist> response) {

            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMapView.removeAllMarkerItem();
        UIThread.removeUIThread(orgCurrentLocationCheckThread);
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

    /**
     * Tmap지도에 마커 찍기
     */
    private void setTmapMarker(TMapView mapView) {
        if(mapView != null && MacaronApp.currAllocation != null) {
            // 출발 아이콘
            TMapMarkerItem markerItem1 = new TMapMarkerItem();
            TMapPoint tMapPoint1 = new TMapPoint(MacaronApp.currAllocation.resvDstLat , MacaronApp.currAllocation.resvDstLon);
            Bitmap bitmap = BitmapFactory.decodeResource(nativeMainActivity.getResources(), R.drawable.marker_direction_guidance_dst);
            markerItem1.setIcon(bitmap); // 마커 아이콘 지정
            markerItem1.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
            markerItem1.setTMapPoint(tMapPoint1); // 마커의 좌표 지정
            markerItem1.setName(MacaronApp.currAllocation.resvDstPoi); // 마커의 타이틀 지정
            mapView.addMarkerItem("markerItem1", markerItem1);  // 지도에 마커 추가

            mapView.setOnClickListenerCallBack(new TMapView.OnClickListenerCallback() {
                @Override
                public boolean onPressEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
                    return false;
                }

                @Override
                public boolean onPressUpEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
                    for(TMapMarkerItem tmp : arrayList) {
                        if(tmp.getID().equals("markerItem1")) {
                            moveTMap();
                        }
                    }
                    return false;
                }
            });

            if(MacaronApp.lastLocation != null) {
                setCurrentLocationMarker(mapView);

                double tmpDistance = 2.5;
                double tmpLat = (Math.abs(MacaronApp.currAllocation.resvDstLat - MacaronApp.lastLocation.getLatitude())) * tmpDistance;
                double tmpLon = (Math.abs(MacaronApp.currAllocation.resvDstLon - MacaronApp.lastLocation.getLongitude())) * tmpDistance;
                mapView.zoomToSpan(tmpLat, tmpLon);
            }

            goTmapViewCenter(false);
        }
    }

    private boolean isTMapExecuteCheck = false;

    private void goTmapViewCenter(boolean isAnimation) {
        mMapView.setCenterPoint(MacaronApp.currAllocation.resvDstLon , MacaronApp.currAllocation.resvDstLat, isAnimation);
    }

    /**
     * 티맵 실행
     */
    private void moveTMap() {
        try {
            if(!isTMapExecuteCheck) {
                isTMapExecuteCheck = true;
                nativeBaseActivity.playLoadingViewAnimation();

                if(nativeMainActivity.tMapTapi.isTmapApplicationInstalled()) {
                    if(! Util.goTmapInvokeRoute(nativeMainActivity.tMapTapi, MacaronApp.currAllocation, false)) {
                        nativeBaseActivity.cancelLoadingViewAnimation();
                        Toast.makeText(nativeMainActivity, "TMap 실행오류", Toast.LENGTH_SHORT).show();
                        isTMapExecuteCheck = false;
                    }

                } else {
                    isTMapExecuteCheck = false;
                    nativeBaseActivity.cancelLoadingViewAnimation();
                    ArrayList result = nativeMainActivity.tMapTapi.getTMapDownUrl();
                    if(result != null) {
                        nativeBaseActivity.showTmapNotInstallDialog(result);
                    }
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 5초마다 내위치 마커 새로그리기
     */
    private Runnable orgCurrentLocationCheckThread = new Runnable() {
        @Override
        public void run() {
            changeCurrentLocationMarker(mMapView);
            UIThread.executeInUIThread(orgCurrentLocationCheckThread, 5000);
        }
    };

    /**
     * TMap 기존 내위치 마커 지우기
     */
    private void changeCurrentLocationMarker(TMapView mapView) {
        if(mapView != null && MacaronApp.lastLocation != null) {

            TMapMarkerItem markerItem = mapView.getMarkerItemFromID("markerItem2");
            if(markerItem != null) {
                mapView.removeMarkerItem("markerItem2");
            }

            setCurrentLocationMarker(mapView);
        }
    }

    /**
     * TMap 내위치 마커 그리기
     */
    private void setCurrentLocationMarker(TMapView mapView) {
        if(mapView != null) {
            // 내 위치
            TMapMarkerItem markerItem2 = new TMapMarkerItem();
            TMapPoint tMapPoint2 = new TMapPoint(MacaronApp.lastLocation.getLatitude() , MacaronApp.lastLocation.getLongitude());
            Bitmap bitmap2 = BitmapFactory.decodeResource(nativeMainActivity.getResources(), R.drawable.macaron_car);
            markerItem2.setIcon(bitmap2);
            markerItem2.setTMapPoint(tMapPoint2);
            markerItem2.setName("현재위치"); // 마커의 타이틀 지정

            mapView.addMarkerItem("markerItem2", markerItem2);
        }
    }

}

