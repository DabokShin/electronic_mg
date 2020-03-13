package kst.ksti.chauffeur.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.net.Uri;
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

import kst.ksti.chauffeur.common.Global;
import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.activity.AllocDetailActivity;
import kst.ksti.chauffeur.common.AnalyticsHelper;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.common.UIThread;
import kst.ksti.chauffeur.databinding.FrCustLoadBinding;
import kst.ksti.chauffeur.listner.ChangeStatusInterface;
import kst.ksti.chauffeur.listner.OnTitleListener;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.MacaronCustomDialog;
import kst.ksti.chauffeur.utility.OnSingleClickListener;
import kst.ksti.chauffeur.utility.PrefUtil;
import kst.ksti.chauffeur.utility.Util;

/**
 * 고객탑승완료, 미탑승 화면
 */
public class CustomerLoadFragment extends NativeFragment implements OnTitleListener {

    private FrCustLoadBinding mBind;
    private MacaronCustomDialog dialog;
    private TMapView mMapView;

    private AppDef.AllocationStatus allocationStatus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nativeBaseActivity.cancelLoadingViewAnimation();
        MacaronApp.nearByDrivingStatusCheck = false;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fr_cust_load, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Logger.d(getResources().getString(R.string.begin_fragment));    // 프래그먼트 호출 로그
        MacaronApp.allocStatus = AppDef.AllocationStatus.ORIGIN;

        SetTitle("물건 수령 대기");
        SetDividerVisibility(true);
        SetTitleListener(this);
        setDrawerLayoutEnable(false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AnalyticsHelper.getInstance(getContext()).sendScreenFromJson(nativeMainActivity, getClass().getSimpleName());

        mBind = FrCustLoadBinding.bind(getView());

        initTmap();
        initUI();
        initEventListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        isTMapExecuteCheck = false;
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
     * Tmap의 View, Marker, 호출 부분 초기화
     */
    private void initTmap() {
        mMapView = new TMapView(nativeMainActivity);
        mMapView.setSKTMapApiKey(Global.TMAP_APIKEY);
        mBind.tMapView.addView(mMapView);

        setTmapMarker(mMapView);
        UIThread.executeInUIThread(orgCurrentLocationCheckThread, 5000);
    }

    /**
     * UI 초기화
     */
    private void initUI() {
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
        mBind.resvOrgPoi.setText(MacaronApp.currAllocation.resvOrgPoi);

        String poi = "";
        String address = "";
        if(!TextUtils.isEmpty(MacaronApp.currAllocation.resvOrgPoi))
            poi = MacaronApp.currAllocation.resvOrgPoi;
        else
            poi = MacaronApp.currAllocation.resvOrgAddress;

        address = MacaronApp.currAllocation.resvOrgAddress;

        mBind.resvOrgPoi.setText(poi);
        mBind.resvAddress.setText(address);
    }

    /**
     * 이벤트 리스너 관리
     */
    private void initEventListener() {
        mBind.btnLoad.setOnClickListener(onSingleClickListener);
        mBind.btnUnload.setOnClickListener(onSingleClickListener);
        mBind.imgCallCust.setOnClickListener(onSingleClickListener);
        mBind.btnMyLocation.setOnClickListener(onSingleClickListener);
        mBind.allocDetailBtn.setOnClickListener(onSingleClickListener);
    }

    private OnSingleClickListener onSingleClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            switch (v.getId()) {
                case R.id.btnLoad:
                    try {
                        nativeBaseActivity.playLoadingViewAnimation();
                        allocationStatus = AppDef.AllocationStatus.CHECKIN;
                        changeAllocStatusAndGoNextScreen(MacaronApp.currAllocation.allocationIdx, AppDef.AllocationStatus.CHECKIN, changeStatusInterface);

                    } catch (Exception e) {
                        e.printStackTrace();
                        nativeBaseActivity.cancelLoadingViewAnimation();
                    }
                    break;

                case R.id.btnUnload:
                    showNoShowDialog();
                    break;

                case R.id.imgCallCust:
                    callCustomer();
                    break;

                case R.id.btnMyLocation:
                    mMapView.setCenterPoint(MacaronApp.currAllocation.resvOrgLon , MacaronApp.currAllocation.resvOrgLat, true);
                    break;

                case R.id.allocDetailBtn:
                    goAllocDetailActivity();
                    break;
            }
        }
    };

    @Override
    public void onDestroyView() {
        mMapView.removeAllMarkerItem();
        mBind.tMapView.removeView(mMapView);
        UIThread.removeUIThread(orgCurrentLocationCheckThread);
        super.onDestroyView();
    }

    /**
     * 배차 상태변경 콜백
     */
    private ChangeStatusInterface changeStatusInterface = new ChangeStatusInterface() {
        @Override
        public void onSuccess(ResponseData<Object> response) {
            switch (allocationStatus) {
                case CHECKIN:
                    AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("고객탑승완료", "고객탑승완료 성공", "", Global.FA_EVENT_NAME.CHAUFFEUR_CHECKIN);
                    PrefUtil.setStartTime(nativeMainActivity, System.currentTimeMillis());
                    GoNativeScreen(new DestArrivedFragment(), null, 1);
                    break;

                case NOSHOW:
                    GoNativeScreen(new DrivingFragment(), null, 2);
                    break;
            }
        }

        @Override
        public void onErrorCode(ResponseData<Object> response) {
            if(allocationStatus.equals(AppDef.AllocationStatus.CHECKIN)) {
                AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("고객탑승완료", "고객탑승완료 실패", "", Global.FA_EVENT_NAME.CHAUFFEUR_CHECKIN);
            }

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
            if(allocationStatus.equals(AppDef.AllocationStatus.CHECKIN)) {
                AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("고객탑승완료", "고객탑승완료 실패", "", Global.FA_EVENT_NAME.CHAUFFEUR_CHECKIN);
            }
            nativeBaseActivity.cancelLoadingViewAnimation();
        }

        @Override
        public void onFailed(Throwable t) {
            if(allocationStatus.equals(AppDef.AllocationStatus.CHECKIN)) {
                AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("고객탑승완료", "고객탑승완료 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.CHAUFFEUR_CHECKIN);
            }
            nativeBaseActivity.cancelLoadingViewAnimation();
        }
    };

    /**
     * 예약 상세보기 이동
     */
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
            nativeMainActivity.overridePendingTransition(R.anim.pull_in_up, R.anim.hold);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 고객에게 전화
     */
    private void callCustomer() {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + MacaronApp.currAllocation.safePhoneno));
        startActivity(intent);
    }

    /**
     * 고객 미탑승시 보여줄 다이얼로그
     */
    private void showNoShowDialog() {
        String msg = getResources().getString(R.string.txt_unload);

        dialog = new MacaronCustomDialog(
                nativeMainActivity,
                "고객 미탑승 처리",
                msg,
                "네",
                "아니오",
                new View.OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                        nativeBaseActivity.playLoadingViewAnimation();
                        allocationStatus = AppDef.AllocationStatus.NOSHOW;
                        changeAllocStatusAndGoNextScreen(MacaronApp.currAllocation.allocationIdx, AppDef.AllocationStatus.NOSHOW, changeStatusInterface);
                    }
                },
                new View.OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                },
                true);
        dialog.show();
    }

    /**
     * TMapView 초기 세팅 (출발지 마커, 내위치 마커 그리기)
     */
    private void setTmapMarker(TMapView mapView) {
        if(mapView != null && MacaronApp.currAllocation != null) {
            // 출발 아이콘
            TMapMarkerItem markerItem1 = new TMapMarkerItem();
            TMapPoint tMapPoint1 = new TMapPoint(MacaronApp.currAllocation.resvOrgLat , MacaronApp.currAllocation.resvOrgLon);
            Bitmap bitmap = BitmapFactory.decodeResource(nativeMainActivity.getResources(), R.drawable.marker_direction_guidance_org);
            markerItem1.setIcon(bitmap); // 마커 아이콘 지정
            markerItem1.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
            markerItem1.setTMapPoint(tMapPoint1); // 마커의 좌표 지정
            markerItem1.setName(MacaronApp.currAllocation.resvOrgPoi);  // 마커의 타이틀 지정
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
                double tmpLat = (Math.abs(MacaronApp.currAllocation.resvOrgLat - MacaronApp.lastLocation.getLatitude())) * tmpDistance;
                double tmpLon = (Math.abs(MacaronApp.currAllocation.resvOrgLon - MacaronApp.lastLocation.getLongitude())) * tmpDistance;
                mapView.zoomToSpan(tmpLat, tmpLon);
            }

            mapView.setCenterPoint(MacaronApp.currAllocation.resvOrgLon , MacaronApp.currAllocation.resvOrgLat);
        }
    }

    private boolean isTMapExecuteCheck = false;

    /**
     * 티맵 실행
     */
    private void moveTMap() {
        try {
            if(!isTMapExecuteCheck) {
                isTMapExecuteCheck = true;
                nativeBaseActivity.playLoadingViewAnimation();

                if(nativeMainActivity.tMapTapi.isTmapApplicationInstalled()) {
                    if(! Util.goTmapInvokeRoute(nativeMainActivity.tMapTapi, MacaronApp.currAllocation, true)) {
                        nativeBaseActivity.cancelLoadingViewAnimation();
                        Toast.makeText(nativeMainActivity, "Tmap 실행오류", Toast.LENGTH_SHORT).show();
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
