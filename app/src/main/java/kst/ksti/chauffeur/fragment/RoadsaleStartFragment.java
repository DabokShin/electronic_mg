package kst.ksti.chauffeur.fragment;

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

import kst.ksti.chauffeur.common.Global;
import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.common.AnalyticsHelper;
import kst.ksti.chauffeur.common.UIThread;
import kst.ksti.chauffeur.databinding.FrRoadsaleStartBinding;
import kst.ksti.chauffeur.listner.ChangeStatusInterface;
import kst.ksti.chauffeur.model.StartRoadsale;
import kst.ksti.chauffeur.net.DataInterface;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.OnSingleClickListener;
import kst.ksti.chauffeur.utility.PrefUtil;
import kst.ksti.chauffeur.utility.Util;

/**
 * 일반운행 목적지 이동 화면
 */
public class RoadsaleStartFragment extends NativeFragment {

    private FrRoadsaleStartBinding mBind;
    private TMapView mMapView;
    private boolean isDstCheck = false;
    private boolean isTMapStart = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fr_roadsale_start, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Logger.d(getResources().getString(R.string.begin_fragment));    // 프래그먼트 호출 로그

        SetTitle("고객 위치로 이동");
        SetDividerVisibility(true);
        setDrawerLayoutEnable(false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AnalyticsHelper.getInstance(getContext()).sendScreenFromJson(nativeMainActivity, getClass().getSimpleName());

        mBind = FrRoadsaleStartBinding.bind(getView());

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
    }

    /**
     * UI 초기화
     */
    private void initUI() {
        mBind.moveTmapBtn.setVisibility(View.GONE);

        mBind.title.btnDrawerOpen.setVisibility(View.GONE);
        mBind.title.btnDrawerOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventForTitleView(v);
            }
        });

        mBind.title.btnTitleBack.setVisibility(View.VISIBLE);
        mBind.title.btnTitleBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackProcess();
            }
        });

        if(!TextUtils.isEmpty(MacaronApp.currStartRoadsale.resvDstPoi)) {
            mBind.resvDstPoi.setText(MacaronApp.currStartRoadsale.resvDstPoi);
        } else {
            mBind.resvDstPoi.setText(MacaronApp.currStartRoadsale.resvDstAddress);
        }

        String poi = "";
        String address = "";
        if(!TextUtils.isEmpty(MacaronApp.currStartRoadsale.resvDstPoi))
            poi = MacaronApp.currStartRoadsale.resvDstPoi;
        else
            poi = MacaronApp.currStartRoadsale.resvDstAddress;

        address = MacaronApp.currStartRoadsale.resvDstAddress;

        mBind.resvDstPoi.setText(poi);
        mBind.resvAddress.setText(address);
    }

    /**
     * 이벤트 리스너 관리
     */
    private void initEventListener() {
        mBind.btnBeginDriveNotTMap.setOnClickListener(onSingleClickListener);

        mBind.btnBeginArr.setOnClickListener(onSingleClickListener);
        mBind.btnMyLocation.setOnClickListener(onSingleClickListener);
        mBind.moveTmapBtn.setOnClickListener(onSingleClickListener);
    }

    private OnSingleClickListener onSingleClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            switch (v.getId()) {
                case R.id.btnBeginDriveNotTMap:
                    isTMapStart = false;
                    sendStartRoadsale();
                    break;
                case R.id.btnBeginArr:
                    isTMapStart = true;
                    sendStartRoadsale();
                    break;

                case R.id.btnMyLocation:
                    goTmapViewCenter(true);
                    break;

                case R.id.moveTmapBtn:
                    //moveTmap();
                    break;
            }
        }
    };

    /**
     * Tmap 재실행 하기
     */
    private void moveTmap() {
        try {
            nativeBaseActivity.playLoadingViewAnimation();

            if(nativeMainActivity.tMapTapi.isTmapApplicationInstalled()) {
                if(! Util.goTmapInvokeRoute(nativeMainActivity.tMapTapi, MacaronApp.currStartRoadsale, false)) {
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
     * 목적지 안내시작 터치
     */
    private void beginArrBtnAction() {
        if(!isDstCheck) {
            isDstCheck = true;

            nativeBaseActivity.playLoadingViewAnimation();

            if (nativeMainActivity.tMapTapi.isTmapApplicationInstalled()) {
                if(! Util.goTmapInvokeRoute(nativeMainActivity.tMapTapi, MacaronApp.currStartRoadsale, false)) {
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
//        changeAllocStatusAndGoNextScreen(MacaronApp.currAllocation.allocationIdx, AppDef.AllocationStatus.ARRIVAL, changeStatusInterface);
    }

    /**
     * 배차 상태변경 콜백
     */
    private ChangeStatusInterface changeStatusInterface = new ChangeStatusInterface() {
        @Override
        public void onSuccess(ResponseData<Object> response) {
            AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("배달완료", "배달완료 성공", "", Global.FA_EVENT_NAME.CHAUFFEUR_ARRIVAL);
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
        isTMapExecuteCheck = false;
        mBind.tvDriveTime.setText(Util.longTimeMillisToHourMinute(MacaronApp.currStartRoadsale.estmTime));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMapView.removeAllMarkerItem();
        UIThread.removeUIThread(orgCurrentLocationCheckThread);
    }

    /**
     * Tmap지도에 마커 찍기
     */
    private void setTmapMarker(TMapView mapView) {
        if(mapView != null && MacaronApp.currStartRoadsale != null) {
            // 출발 아이콘
            TMapMarkerItem markerItem1 = new TMapMarkerItem();
            TMapPoint tMapPoint1 = new TMapPoint(MacaronApp.currStartRoadsale.resvDstLat , MacaronApp.currStartRoadsale.resvDstLon);
            Bitmap bitmap = BitmapFactory.decodeResource(nativeMainActivity.getResources(), R.drawable.marker_direction_guidance_dst);
            markerItem1.setIcon(bitmap); // 마커 아이콘 지정
            markerItem1.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
            markerItem1.setTMapPoint(tMapPoint1); // 마커의 좌표 지정
            markerItem1.setName(MacaronApp.currStartRoadsale.resvDstPoi); // 마커의 타이틀 지정
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
                            //moveTMap();
                        }
                    }
                    return false;
                }
            });

            if(MacaronApp.lastLocation != null) {
                setCurrentLocationMarker(mapView);

                double tmpDistance = 2.5;
                double tmpLat = (Math.abs(MacaronApp.currStartRoadsale.resvDstLat - MacaronApp.lastLocation.getLatitude())) * tmpDistance;
                double tmpLon = (Math.abs(MacaronApp.currStartRoadsale.resvDstLon - MacaronApp.lastLocation.getLongitude())) * tmpDistance;
                mapView.zoomToSpan(tmpLat, tmpLon);
            }

            goTmapViewCenter(false);
        }
    }

    private boolean isTMapExecuteCheck = false;

    private void goTmapViewCenter(boolean isAnimation) {
        mMapView.setCenterPoint(MacaronApp.currStartRoadsale.resvDstLon , MacaronApp.currStartRoadsale.resvDstLat, isAnimation);
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
                    if(! Util.goTmapInvokeRoute(nativeMainActivity.tMapTapi, MacaronApp.currStartRoadsale, false)) {
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

    private void sendStartRoadsale()
    {
        HashMap<String, Object> params = new HashMap<>();
        params.put("resvDstPoi", MacaronApp.currStartRoadsale.resvDstPoi);
        params.put("resvDstLat", MacaronApp.currStartRoadsale.resvDstLat);
        params.put("resvDstLon", MacaronApp.currStartRoadsale.resvDstLon);
        params.put("resvDstAddress", MacaronApp.currStartRoadsale.resvDstAddress);
        params.put("estmTime", MacaronApp.currStartRoadsale.estmTime);
        params.put("estmDist", MacaronApp.currStartRoadsale.estmDist);
        params.put("estmTaxiFare", MacaronApp.currStartRoadsale.estmTaxiFare);
        params.put("poi", MacaronApp.currStartRoadsale.realOrgPoi);
        params.put("lat", MacaronApp.currStartRoadsale.realOrgLat);
        params.put("lon", MacaronApp.currStartRoadsale.realOrgLon);
        params.put("address", MacaronApp.currStartRoadsale.address);

        nativeBaseActivity.playLoadingViewAnimation();

        DataInterface.getInstance().sendStartRoadsale(nativeMainActivity, params, new DataInterface.ResponseCallback<ResponseData<StartRoadsale>>() {
            @Override
            public void onSuccess(ResponseData<StartRoadsale> response) {
                if ("S000".equals(response.getResultCode())) {
                    try {
                        AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("배달완료", "배달완료 시작", "", Global.FA_EVENT_NAME.CHAUFFEUR_ROADSALE);

                        MacaronApp.currStartRoadsale = response.getData();

                        // TMap 호출
                        if(isTMapStart)
                            beginArrBtnAction();

                        PrefUtil.setStartTime(nativeMainActivity, System.currentTimeMillis());

                        Bundle bundle = new Bundle();
                        bundle.putBoolean("isTMap", isTMapStart);
                        GoNativeScreen(new RoadsaleCompleteFragment(), bundle, 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("배달완료", "배달완료 실패", "", Global.FA_EVENT_NAME.CHAUFFEUR_ROADSALE);
                    Logger.d("배달완료 실패");
                }
            }

            @Override
            public void onError(ResponseData<StartRoadsale> response) {
                AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("배달완료", "배달완료 실패", "", Global.FA_EVENT_NAME.CHAUFFEUR_ROADSALE);
                nativeBaseActivity.cancelLoadingViewAnimation();
            }

            @Override
            public void onFailure(Throwable t) {
                nativeBaseActivity.cancelLoadingViewAnimation();
            }
        });
    }
}
