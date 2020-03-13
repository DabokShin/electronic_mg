package kst.ksti.chauffeur.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import kst.ksti.chauffeur.databinding.FrOrgArrivedBinding;
import kst.ksti.chauffeur.listner.ChangeStatusInterface;
import kst.ksti.chauffeur.listner.OnTitleListener;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.OnSingleClickListener;
import kst.ksti.chauffeur.utility.Util;

/**
 * 출발지 도착 화면
 */
public class OrgArrivedFragment extends NativeFragment implements OnTitleListener {

    private FrOrgArrivedBinding mBind;
    private TMapView mMapView;
    private boolean isOrgCheck = false;

    private BroadcastReceiver receiver;

    public OrgArrivedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MacaronApp.nearByDrivingStatusCheck = false;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fr_org_arrived, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Logger.d(getResources().getString(R.string.begin_fragment));    // 프래그먼트 호출 로그
        MacaronApp.allocStatus = AppDef.AllocationStatus.DEPART;

        SetTitle("가맹점 위치로 이동");
        SetDividerVisibility(true);
        SetTitleListener(this);
        setDrawerLayoutEnable(false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AnalyticsHelper.getInstance(getContext()).sendScreenFromJson(nativeMainActivity, getClass().getSimpleName());

        setDrawerLayoutEnable(false);
        mBind = FrOrgArrivedBinding.bind(getView());

        initTmap();
        initUI();
        initEventListener();

        // 브로드캐스트 리시버
        IntentFilter filter = new IntentFilter("action.broadcast");

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent != null) {
                    if(intent.getStringExtra("reservation_cancel").equals("y")) {
                        Logger.d("LOG1 : get broadcast - action android:name = action.broadcast");
                        showErrorCode902_Dialog(nativeMainActivity, "예약이 취소 되었습니다.");
                    }
                }
            }
        };

        nativeMainActivity.registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroyView() {
        mMapView.removeAllMarkerItem();
        UIThread.removeUIThread(orgCurrentLocationCheckThread);

        // 브로드캐스트 리시버 해제
        if (receiver != null) {
            nativeMainActivity.unregisterReceiver(receiver);
            receiver = null;
        }

        super.onDestroyView();
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

        if(!isOrgCheck) {
            isOrgCheck = true;

            if (nativeMainActivity.tMapTapi.isTmapApplicationInstalled()) {
                if (!Util.goTmapInvokeRoute(nativeMainActivity.tMapTapi, MacaronApp.currAllocation, true)) {
                    nativeBaseActivity.cancelLoadingViewAnimation();
                    Toast.makeText(nativeMainActivity, "TMap 실행오류", Toast.LENGTH_SHORT).show();
                }

            } else {
                nativeBaseActivity.cancelLoadingViewAnimation();
                ArrayList result = nativeMainActivity.tMapTapi.getTMapDownUrl();
                if (result != null) {
                    nativeBaseActivity.showTmapNotInstallDialog(result);
                }
            }
        }
    }

    /**
     * UI 초기화
     */
    private void initUI() {
//        if(BuildConfig.DEBUG && Global.getIsShowDev()) {
//            mBind.allocDetailBtn.setVisibility(View.VISIBLE);
//            mBind.moveTmapBtn.setVisibility(View.VISIBLE);
//
//            if(MacaronApp.lastLocation != null) {
//                mBind.btnMyLocation.setVisibility(View.VISIBLE);
//            } else {
//                mBind.btnMyLocation.setVisibility(View.GONE);
//            }
//        } else {
//            mBind.allocDetailBtn.setVisibility(View.GONE);
//            mBind.moveTmapBtn.setVisibility(View.GONE);
//            mBind.btnMyLocation.setVisibility(View.GONE);
//        }

        mBind.btnMyLocation.setVisibility(View.VISIBLE);

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
        mBind.btnSrcArrived.setOnClickListener(onSingleClickListener);
        mBind.imgCallCust.setOnClickListener(onSingleClickListener);
        mBind.btnMyLocation.setOnClickListener(onSingleClickListener);
        mBind.allocDetailBtn.setOnClickListener(onSingleClickListener);

        mBind.moveTmapBtn.setOnClickListener(onSingleClickListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        isTMapExecuteCheck = false;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

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
            intent.putExtra("call_customer", false);
            nativeBaseActivity.playLoadingViewAnimation();
            nativeMainActivity.startActivity(intent);
            nativeMainActivity.overridePendingTransition(R.anim.pull_in_up, R.anim.hold);
        } catch (NullPointerException e) {
            e.printStackTrace();
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

    private OnSingleClickListener onSingleClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            switch (v.getId()) {
                case R.id.btnSrcArrived:
                    try {
                        nativeBaseActivity.playLoadingViewAnimation();
                        changeAllocStatusAndGoNextScreen(MacaronApp.currAllocation.allocationIdx, AppDef.AllocationStatus.ORIGIN, changeStatusInterfaceAllocStatus);

                    } catch (Exception e) {
                        e.printStackTrace();
                        nativeBaseActivity.cancelLoadingViewAnimation();
                    }
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

                case R.id.moveTmapBtn:
                    moveTMap();
                    break;
            }
        }
    };

    /**
     * 배차 상태변경 콜백
     */
    private ChangeStatusInterface changeStatusInterfaceAllocStatus = new ChangeStatusInterface() {
        @Override
        public void onSuccess(ResponseData<Object> response) {
            AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("출발지도착", "출발지도착 성공", "", Global.FA_EVENT_NAME.CHAUFFEUR_ORIGIN);
            GoNativeScreen(new CustomerLoadFragment(), null, 1);
        }

        @Override
        public void onErrorCode(ResponseData<Object> response) {
            AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("출발지도착", "출발지도착 실패", "", Global.FA_EVENT_NAME.CHAUFFEUR_ORIGIN);
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
            AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("출발지도착", "출발지도착 실패", "", Global.FA_EVENT_NAME.CHAUFFEUR_ORIGIN);
            nativeBaseActivity.cancelLoadingViewAnimation();
        }

        @Override
        public void onFailed(Throwable t) {
            AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("출발지도착", "출발지도착 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.CHAUFFEUR_ORIGIN);
            nativeBaseActivity.cancelLoadingViewAnimation();
        }
    };

    /**
     * 고객에게 전화하기
     */
    private void callCustomer() {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + MacaronApp.currAllocation.safePhoneno));
        startActivity(intent);
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
