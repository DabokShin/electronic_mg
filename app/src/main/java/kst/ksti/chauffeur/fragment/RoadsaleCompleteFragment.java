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

import kst.ksti.chauffeur.common.Global;
import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.activity.AllocDetailActivity;
import kst.ksti.chauffeur.common.AnalyticsHelper;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.common.UIThread;
import kst.ksti.chauffeur.databinding.FrRoadsaleCompleteBinding;
import kst.ksti.chauffeur.listner.ChangeStatusInterface;
import kst.ksti.chauffeur.listner.OnTitleListener;
import kst.ksti.chauffeur.listner.ReverseGeocodingInterface;
import kst.ksti.chauffeur.net.DataInterface;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.OnSingleClickListener;
import kst.ksti.chauffeur.utility.PrefUtil;
import kst.ksti.chauffeur.utility.Util;

/**
 * 일반운행 완료 화면
 */
public class RoadsaleCompleteFragment extends NativeFragment implements OnTitleListener {
    private FrRoadsaleCompleteBinding mBind;
    private TMapView mMapView;
    private boolean isDstCheck = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MacaronApp.nearByDrivingStatusCheck = false;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fr_roadsale_complete, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Logger.d(getResources().getString(R.string.begin_fragment));    // 프래그먼트 호출 로그
        MacaronApp.allocStatus = AppDef.AllocationStatus.ROADSALE;

        SetTitle("고객 위치로 이동");
        SetDividerVisibility(true);
        SetTitleListener(this);
        setDrawerLayoutEnable(false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AnalyticsHelper.getInstance(getContext()).sendScreenFromJson(nativeMainActivity, getClass().getSimpleName());

        mBind = FrRoadsaleCompleteBinding.bind(getView());

        boolean isTMapStart = true;
        Bundle bundle = getArguments();
        if (bundle != null) {
            isTMapStart = bundle.getBoolean("isTMap", true);
        }

        initTmap(isTMapStart);

        initUI();
        initEventListener();
    }

    /**
     * Tmap의 View, Marker, 호출 부분 초기화
     */
    private void initTmap(boolean isTMap) {
        mMapView = new TMapView(nativeMainActivity);
        mMapView.setSKTMapApiKey(Global.TMAP_APIKEY);
        mBind.tMapView.addView(mMapView);

        setTmapMarker(mMapView);
        UIThread.executeInUIThread(orgCurrentLocationCheckThread, 5000);

        // TMap 실행
        if(isTMap) {
            if(!isDstCheck) {
                isDstCheck = true;

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
        }
        else
            nativeBaseActivity.cancelLoadingViewAnimation();
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
            intent.putExtra("a_detail", MacaronApp.currStartRoadsale.roadSaleIdx);
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
     * 운행완료버튼 터치
     */
    private void destArrBtnAction() {
        nativeBaseActivity.playLoadingViewAnimation();

        endRoadsale();
        //changeAllocStatusAndGoNextScreen(MacaronApp.currStartRoadsale.roadSaleIdx, AppDef.AllocationStatus.ARRIVAL, changeStatusInterface);
    }

    /**
     * 배차 상태변경 콜백
     */
    private ChangeStatusInterface changeStatusInterface = new ChangeStatusInterface() {
        @Override
        public void onSuccess(ResponseData<Object> response) {
            AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("배달완료", "배달완료 성공", "", Global.FA_EVENT_NAME.CHAUFFEUR_ROADSALE);
            //switchToPayment(MacaronApp.currStartRoadsale.fareCat);
        }

        @Override
        public void onErrorCode(ResponseData<Object> response) {
            AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("배달완료", "배달완료 실패", "", Global.FA_EVENT_NAME.CHAUFFEUR_ROADSALE);
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
            AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("배달완료", "배달완료 실패", "", Global.FA_EVENT_NAME.CHAUFFEUR_ROADSALE);
            nativeBaseActivity.cancelLoadingViewAnimation();
        }

        @Override
        public void onFailed(Throwable t) {
            AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("배달완료", "배달완료 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.CHAUFFEUR_ROADSALE);
            nativeBaseActivity.cancelLoadingViewAnimation();
        }
    };

    private void endRoadsale()
    {
        // 내 현재 위치를 Tmap에서 받아온다.
        Util.getLocationInfomationParams(nativeMainActivity, reverseGeocodingInterfaceEndRoadsale, null, null);
    }

    /**
     * Tmap에서 위치정보 받아오는 콜백리스너
     */
    private ReverseGeocodingInterface reverseGeocodingInterfaceEndRoadsale = new ReverseGeocodingInterface() {
        @Override
        public void onSuccess(final HashMap<String, Object> params, ChangeStatusInterface changeStatusInterface) {
            Logger.d("## TMap My Position : Geocoding onSuccess()");

            // 일반운행 종료 서버 호출
            sendCompleteRoadsale(params);
        }

        @Override
        public void onError(HashMap<String, Object> params, ChangeStatusInterface changeStatusInterface, final String errorMsg) {
            Logger.d("## TMap My Position : Geocoding onError()");
            UIThread.executeInUIThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(nativeMainActivity, errorMsg, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onGpsError(HashMap<String, Object> params, ChangeStatusInterface changeStatusInterface) {
            Logger.d("## TMap My Position : Geocoding onGpsError()");
        }
    };

    /**
     * 일반영업 종료를 서버에 알려준다
     */
    private void sendCompleteRoadsale(final HashMap<String, Object> myPosition)
    {
        HashMap<String, Object> params = new HashMap<>();
        params.put("roadSaleIdx", MacaronApp.currStartRoadsale.roadSaleIdx);
        params.put("poi", myPosition.get("poi"));
        params.put("lat", myPosition.get("lat"));
        params.put("lon", myPosition.get("lon"));
        params.put("address", myPosition.get("address"));

        DataInterface.getInstance().sendCompleteRoadsale(nativeMainActivity, params, new DataInterface.ResponseCallback<ResponseData<Object>>() {
            @Override
            public void onSuccess(ResponseData<Object> response) {
                if ("S000".equals(response.getResultCode())) {
                    try {
                        Toast.makeText(nativeMainActivity, "배달을 종료합니다.", Toast.LENGTH_SHORT).show();

                        // 일반운행 인덱스 초기화
                        MacaronApp.currStartRoadsale.roadSaleIdx = 0;

                        GoNativeScreenReplaceAdd(new DrivingFragment(), null, 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("배달 종료", "배달 종료 실패", "", Global.FA_EVENT_NAME.CHAUFFEUR_ROADSALE);
                    Logger.d("일반운행 종료 실패");
                }

                nativeBaseActivity.cancelLoadingViewAnimation();
            }

            @Override
            public void onError(ResponseData<Object> response) {
                AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("배달 종료", "배달 종료 실패", "", Global.FA_EVENT_NAME.CHAUFFEUR_ROADSALE);
                nativeBaseActivity.cancelLoadingViewAnimation();
            }

            @Override
            public void onFailure(Throwable t) {
                nativeBaseActivity.cancelLoadingViewAnimation();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        isTMapExecuteCheck = false;
        mBind.tvDriveTime.setText(beforeTimeMillisToHourMinute(System.currentTimeMillis(), PrefUtil.getStartTime(nativeMainActivity)));
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
                            moveTMap();
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
}
