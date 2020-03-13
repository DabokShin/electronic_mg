package kst.ksti.chauffeur.service;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.location.GnssStatus;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import kst.ksti.chauffeur.common.Global;
import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.activity.AllocDetailActivity;
import kst.ksti.chauffeur.activity.MainActivity;
import kst.ksti.chauffeur.activity.TmpSplashActivity;
import kst.ksti.chauffeur.common.GetTMapTimeMachine;
import kst.ksti.chauffeur.common.UIThread;
import kst.ksti.chauffeur.listner.ChangeStatusInterface;
import kst.ksti.chauffeur.listner.ReverseGeocodingInterface;
import kst.ksti.chauffeur.listner.TMapTimeMachineCallback;
import kst.ksti.chauffeur.model.AllocationSchedule;
import kst.ksti.chauffeur.model.StartRoadsale;
import kst.ksti.chauffeur.net.DataInterface;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.utility.DateUtils;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.PrefUtil;
import kst.ksti.chauffeur.utility.Util;

import static android.speech.tts.TextToSpeech.ERROR;

public class MyLocationService extends Service {
    private static final String TAG = "MyLocationService";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 0.0f;
    private static final int NOTIFICATION_ID = 1;
    private static final int MINUTE_MILLISEC = 60 * 1000;

    private float mTouchX, mTouchY;
    private int mViewX, mViewY;
    private WindowManager.LayoutParams params;
    private WindowManager mManager;
    private GestureDetectorCompat gestureDetector;

    private Timer timer;
    private long allocationIdx = -1;
    private int scheduleSize = 0;

    private int satelliteCount = 0;
    private float[] cn0DbHz = new float[]{0};

    private Location mLastLocation;  // 'MacaronApp.lastLocation'과 비교하여 속도, 각도를 추출하기 위해 정의됨.

    private TextToSpeech tts;

    private String floatingBtnUri = "";

    private Runnable timer5sec = new Runnable() {
        @Override
        public void run() {
            if(mLastLocation != null) {
                sendLocationInfo(mLastLocation);
            }

            UIThread.executeInUIThread(timer5sec, 5000);
        }
    };

    private int[] deviceSize;

    private AllocationSchedule nearbySchedule;
    private long floating_allocDetailIdx;

    @Override
    public void onCreate() {
        Logger.d("MyLocationService onCreate()");

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != ERROR) {
                    // 언어를 선택한다.
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

        // 서비스의 노티 생성 =====================================================================
        startForeground(NOTIFICATION_ID, createNotification(this));

        // 위치정보 ===============================================================================
        MyLocationServiceManager remoteModel = MyLocationServiceManager.getInstance();
        remoteModel.setMyLocationService(this);

        initializeLocationManager();

        // 위성개수와 신호세기 받아오는곳
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getGnssStatus();
        } else {
            getGpsStatus();
        }

        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, LOCATION_DISTANCE, gpsListener);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, LOCATION_DISTANCE, netListener);

            UIThread.executeInUIThread(timer5sec, (MacaronApp.chauffeur==null)?5000:MacaronApp.chauffeur.tdcsIntervalMS);

        } catch (SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

        // 운행임박 ===============================================================================
        setNearByDrive();

        // 플로팅버튼 =============================================================================
        deviceSize = Util.getDeviceSize(this);
        setFloatingAction();
    }

    /**
     * 서비스의 foreground 노티 생성
     */
    private Notification createNotification(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("macaron://tmapend"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context,
                Util.getNotiChId(this, Global.NotiAlarmChannelID.CHANNEL_LOC, context.getString(R.string.notification_chauffeur_position), context.getString(R.string.notification_chauffeur_position)))
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.chauffeur_small_icon)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round))
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(getString(R.string.txt_noti_cont))
                .setContentIntent(contentIntent);

        return notificationBuilder.build();
    }


    // ============================================================================================
    // ==================================  위치  ==================================
    // ============================================================================================

    /**
     * 오레오 버전 이상 위성개수, 신호세기
     */
    @TargetApi(Build.VERSION_CODES.N)
    @SuppressLint("MissingPermission")
    private void getGnssStatus() {
        mLocationManager.registerGnssStatusCallback(new GnssStatus.Callback() {
            @Override
            public void onStarted() {
                super.onStarted();
            }

            @Override
            public void onStopped() {
                super.onStopped();
            }

            @Override
            public void onFirstFix(int ttffMillis) {
                super.onFirstFix(ttffMillis);
            }

            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
                super.onSatelliteStatusChanged(status);
                try {
                    satelliteCount = status.getSatelliteCount();

                    cn0DbHz = new float[satelliteCount];

                    for (int i = 0; i < satelliteCount; i++) {
                        cn0DbHz[i] = status.getCn0DbHz(i);
                    }

                } catch (Exception e) {
                }
            }
        });

    }

    /**
     * 오레오 버전 미만 위성개수, 신호세기
     */
    @SuppressLint("MissingPermission")
    private void getGpsStatus() {
        mLocationManager.addGpsStatusListener(new GpsStatus.Listener() {
            @Override
            public void onGpsStatusChanged(int event) {
                switch (event) {
                    case GpsStatus.GPS_EVENT_STARTED:
                        break;
                    case GpsStatus.GPS_EVENT_FIRST_FIX:
                        break;
                    case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                        try {
                            GpsStatus status = mLocationManager.getGpsStatus(null);
                            Iterable sats = status.getSatellites();
                            Iterator satI = sats.iterator();
                            int count = 0;
                            ArrayList<Float> tmpCn0DbHz = new ArrayList<>();

                            while (satI.hasNext()) {
                                GpsSatellite gpssatellite = (GpsSatellite) satI.next();
                                if (gpssatellite.usedInFix()) {
                                    tmpCn0DbHz.add(gpssatellite.getSnr());
                                    count++;
                                }
                            }

                            if(tmpCn0DbHz.size() > 0) {
                                cn0DbHz = new float[tmpCn0DbHz.size()];
                                for(int i=0; i<cn0DbHz.length; i++) {
                                    cn0DbHz[i] = tmpCn0DbHz.get(i);
                                }
                            } else {
                                cn0DbHz = new float[]{0};
                            }

                            satelliteCount = count;

                        } catch (Exception e) {
                            e.printStackTrace();
                            satelliteCount = 0;
                            cn0DbHz = new float[]{0};
                        }
                        break;

                    case GpsStatus.GPS_EVENT_STOPPED:
//                            Log.i(TAG, "onGpsStatusChanged(): GPS stopped");
                        break;
                }
            }
        });
    }

    /**
     * LocationManager 초기화
     */
    private void initializeLocationManager() {
        mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
    }

    /**
     * GPS 정보 로그찍기
     */
    private void showLogOfLocationInfo(Location location) {
        double speed = Double.parseDouble(String.format("%.3f", (location.getSpeed() / 1000f * 3600f)));

        float bearing;
        try {
            bearing = location.getBearing() - MacaronApp.lastLocation.getBearing();
        } catch (Exception e) {
            bearing = location.getBearing();
        }

        String logInfo = "\n위도: " + location.getLatitude() +
                " / 경도: " + location.getLongitude() +
                " / 각도 : " + bearing +
                " / 위성개수 : " + satelliteCount +
                " / 신호세기 : " + Arrays.toString(cn0DbHz) +
                " / 속도 : " + speed +
                " / 정확도 : " + location.getAccuracy() +
                " / 제공프로바이더 : " + location.getProvider();

//        Logger.i("onLocationChanged: " + logInfo);
    }

    /**
     * 위치정보 갱신
     * @param location location
     * @param provider provider
     */
    @SuppressLint("MissingPermission")
    public void updateWithNewLocation(Location location, String provider) throws NullPointerException {
        if(location != null) {
            if (LocationManager.GPS_PROVIDER.equals(provider)) {
                mLastLocation = location; // gps 위치정보

            } else {
                long gpsGenTime = 0;
                try {
                    gpsGenTime = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getTime(); // 마지막으로 수신된 GPS 위치정보
                } catch (Exception e) {}

                long curTime = System.currentTimeMillis(); // 현재 시간
                if ((curTime - gpsGenTime) > 20000) { // gps 정보가 20초 이상 오래된 정보이면 네트워크 위치정보 사용
                    mLastLocation = location;
                }
            }

            if(MacaronApp.lastLocation == null) {
                // ggomzzin
                MacaronApp.lastLocation = mLastLocation;
                Logger.e("## 서비스에서 위치정보 수신성공");
            }

            showLogOfLocationInfo(location);
        }
    }

    /**
     * GPS 프로바이더
     */
    private LocationListener gpsListener = new LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
        @Override
        public void onProviderEnabled(String provider) {

        }
        @Override
        public void onProviderDisabled(String provider) {

        }
        @Override
        public void onLocationChanged(Location location) {
            try {
                updateWithNewLocation(location, "gps");
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 네트워크 프로바이더
     */
    private LocationListener netListener = new LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
        @Override
        public void onProviderEnabled(String provider) {

        }
        @Override
        public void onProviderDisabled(String provider) {

        }
        @Override
        public void onLocationChanged(Location location) {
            try {
                updateWithNewLocation(location, "network");
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 관제서버로 위치정보 전송
     */
    private void sendLocationInfo(Location location) {

        if(MacaronApp.lastLocation == null || location == null) {
            return;
        }

        float bearing = 0;
        double speed = 0;

        try {
            bearing = location.getBearing() - MacaronApp.lastLocation.getBearing();
            speed = Double.parseDouble(String.format("%.3f", (MacaronApp.lastLocation.getSpeed() / 1000f * 3600f)));
        } catch (Exception ignored) {}

        if(location.getLatitude() > 0 && location.getLongitude() > 0) {
            // ggomzzin
            MacaronApp.lastLocation = location;
        }

        try {
            DataInterface.getInstance(Global.getTDCSUrl()).sendLocationInfo(
                    String.valueOf(MacaronApp.chauffeur.chauffeurIdx),
                    "1",
                    MacaronApp.lastLocation.getLatitude(),
                    MacaronApp.lastLocation.getLongitude(),
                    DateUtils.getCurrentDate("yyyy-MM-dd HH:mm:ss"),
                    1.0,
                    MacaronApp.lastLocation.getAltitude(),
                    speed,
                    bearing,  //각도
                    MacaronApp.lastLocation.getAccuracy(),
                    88.0,
                    System.currentTimeMillis(),
                    new DataInterface.ResponseCallback<ResponseData<Object>>() {
                        @Override
                        public void onSuccess(ResponseData<Object> response) {
                            Logger.d("GPS" + mLastLocation.toString());
                        }

                        @Override
                        public void onError(ResponseData<Object> response) {
                            Logger.e("On Error GPS");
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            Logger.e("On Failure GPS" + t.toString());
                        }
                    });
        } catch (Exception e) {
        }
    }


    // ============================================================================================
    // ==================================  운행임박  ==================================
    // ============================================================================================

    /**
     * 운행임박 호출
     */
    private void setNearByDrive() {
        timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (MacaronApp.nearByDriveStartCheck) {
                    Logger.d("request nearbydrive service");
                    requestReserveList();
                }
            }
        }, 0, MINUTE_MILLISEC * 5); // 5분 마다 한번씩
                           //}, 0, MINUTE_MILLISEC); // 테스트
    }

    /**
     * 운행임박 호출
     */
    private void requestReserveList() {
        HashMap<String, Object> params = new HashMap<>();
        // 운행임박은 항상 1페이지의 데이터만 가져온다.
        params.put("page", Global.FIRST_PAGE);
        params.put("limit", Global.LIST_LIMIT_COUNT);
        DataInterface.getInstance().receiveAllocSchedule(this, params, false, new DataInterface.ResponseCallback<ResponseData<AllocationSchedule>>() {
            @Override
            public void onSuccess(ResponseData<AllocationSchedule> response) {
                if ("S000".equals(response.getResultCode())) {
                    if(response.getList() != null && response.getList().size() > 0) {
                        for (AllocationSchedule s : response.getList()) {
                            long diff = s.resvDatetime - System.currentTimeMillis();    // 남은 예약시간
                            if(!"ALLOCATED".equals(s.allocationStatus)) continue;       // 배차 상태가 아니면 continue;
                            if(diff > (MINUTE_MILLISEC * 60 * 3)) continue;             // 남은 예약시간이 3시간 보다 크면 continue;

                            checkNearBy(s, response.getList().size());
                            break;
                        }
                    }
                } else {
                    Logger.d("예약 목록 receive 실패");
                }
            }

            @Override
            public void onError(ResponseData<AllocationSchedule> response) {

            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
    }

    /**
     * 운행임박 호출
     */
    private boolean checkNearBy(AllocationSchedule s, int size) {

        nearbySchedule = s;
        scheduleSize = size;

        // 내 현재 위치를 Tmap에서 받아온다.
        Logger.d("LOG1 : NearBy 현재 내 위치 받아오기 시작");
        Util.getLocationInfomationParams(this, reverseGeocodingInterfaceTmapTimeMachine, null, null);

        return false;
    }

    /**
     * Tmap에서 위치정보 받아오는 콜백리스너
     */
    private ReverseGeocodingInterface reverseGeocodingInterfaceTmapTimeMachine = new ReverseGeocodingInterface() {
        @Override
        public void onSuccess(final HashMap<String, Object> params, ChangeStatusInterface changeStatusInterface) {
            Logger.d("LOG1 : NearBy 현재 내 위치 받아오기 성공");

            try
            {
                // 티맵 타임머신 api 호출
                GetTMapTimeMachine timeMachine = new GetTMapTimeMachine(params,
                        nearbySchedule.resvOrgPoi, nearbySchedule.resvOrgAddress, nearbySchedule.resvOrgLat, nearbySchedule.resvOrgLon, new TMapTimeMachineCallback() {
                    @Override
                    public void onSuccess(StartRoadsale roadsale) {
                        long diff = nearbySchedule.resvDatetime - System.currentTimeMillis();
                        Logger.d("LOG2 : current = " + System.currentTimeMillis() + ", reserveDateTime = " + nearbySchedule.resvDatetime + ", diff = " + diff);

                        long nearbyTime = (roadsale.estmTime * 1000) + (10 * 60 * 1000);  // 운행임박 공식 일반종료 예상시간 + 10분
                        Logger.d("LOG2 : NearBy 티맵 타임머신 api 성공, 예상시간 + 10분 =  " + nearbyTime + "(초 * 1000)");

                        // 테스트 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
                        //TestNearbyNoti(diff, nearbyTime);
                        // 테스트 종료 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ

                        if (diff <= nearbyTime && "ALLOCATED".equals(nearbySchedule.allocationStatus)) {
                            if( ! (MacaronApp.appStatus == MacaronApp.AppStatus.FOREGROUND || MacaronApp.appStatus == MacaronApp.AppStatus.RETURNED_TO_FOREGROUND)) {
                                if(!MacaronApp.isNews) {
                                    MacaronApp.isNews = true;
                                    Logger.d("LOG1 : [배달임박] 플로팅 버튼 애니메이션 시작");
                                    startBtnAnimation(Global.URI.TMAP_END, 0);
                                }
                                else
                                    floatingBtnData(Global.URI.TMAP_END, 0);
                            }

                            if (allocationIdx == nearbySchedule.allocationIdx) {
                                // 한번 호출하면 다시 호출 안함
                                return;
                            }

                            // 운행임박 시간을 음성으로 알려준다.
                            String remainTime = "";
                            long hour = diff / (3600 * 1000);
                            long min = diff / (60 * 1000) % 60;
                            if(hour >= 1)
                                remainTime = "다음 예약 시간까지 " + hour + "시간" + min + "분이 남았습니다. 지금 바로 출발 버튼을 누르고, 예약지로 이동해주세요.";
                            else
                                remainTime = "다음 예약 시간까지 " + min + "분이 남았습니다. 지금 바로 출발 버튼을 누르고, 예약지로 이동해주세요.";

                            // 알림 받은 문장을 읽는다.
                            if(PrefUtil.getOptionTTS(getApplicationContext())) {
                                // 음성입력 권한 체크
                                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    tts.speak(remainTime, TextToSpeech.QUEUE_FLUSH, null, "NearByVoice");
                                }
                                else {
                                    HashMap<String, String> map = new HashMap<>();
                                    map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "NearByVoice");
                                    tts.speak(remainTime, TextToSpeech.QUEUE_FLUSH, map);
                                }
                            }

                            // 운행임박 상세 호출
                            allocationIdx = nearbySchedule.allocationIdx;
                            startActivity(scheduleSize, nearbySchedule.resvDatetime);
                        }
                    }

                    @Override
                    public void onFailure() {
                        Logger.d("LOG1 : NearBy 티맵 타임머신 api 실패");
                    }
                });
                timeMachine.execute();
            }
            catch(Exception e) {
                Logger.d("LOG1 : NearBy 티맵 타임머신 api Exception - " + e.getMessage());
            }
        }

        private void TestNearbyNoti(long diff, long nearbyTime)
        {
            // 예약 남은시간
            long _sec = (diff / 1000) % 60;
            long _min = (diff / 1000) / 60 % 60;
            long _hour = diff / (60 * 60 * 1000);
            String test_resvTime = ", 예약 남은 시간 : " + _hour + "시" + _min + "분" + _sec + "초, ";

            _sec = (nearbyTime / 1000) % 60;
            _min = (nearbyTime / 1000) / 60 % 60;
            _hour = nearbyTime / (60 * 60 * 1000);
            String test_nearbyTime = "예상시간 : " + _hour + "시" + _min + "분" + _sec + "초";
            String toastMessage = "allocID : " + nearbySchedule.allocationIdx + test_resvTime + test_nearbyTime;
//                        Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG).show();

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(),
                    Util.getNotiChId(getApplicationContext(), Global.NotiAlarmChannelID.CHANNEL_PUSH, getString(R.string.notification_allocation), getString(R.string.notification_allocation)))
                    .setSmallIcon(R.drawable.chauffeur_small_icon)
                    .setPriority(NotificationCompat.PRIORITY_MAX)                       // 헤드업 알림
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setContentTitle("배달임박 테스트")
                    .setContentText(toastMessage)      // 긴 텍스트 적용
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(toastMessage))      // 긴 텍스트 적용
                    //.setContentText(body)
                    .setAutoCancel(true)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

            Intent intent = new Intent(getApplicationContext(), TmpSplashActivity.class);   // 액티비티 아무거나 넣었음.. 테스트기 때문에... 클릭 하지 마세요...

            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder.setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.notify(Util.createID(), notificationBuilder.build());
            }
        }

        @Override
        public void onError(HashMap<String, Object> params, ChangeStatusInterface changeStatusInterface, final String errorMsg) {
            Logger.d("LOG1 : NearBy 현재 내 위치 받아오기 에러");
            UIThread.executeInUIThread(new Runnable() {
                @Override
                public void run() {
                    Logger.d(errorMsg);
                }
            });
        }

        @Override
        public void onGpsError(HashMap<String, Object> params, ChangeStatusInterface changeStatusInterface) {
            Logger.d("LOG1 : NearBy 현재 내 위치 받아오기 GPS 에러");
        }
    };

    /**
     * 운행임박 호출
     */
    private void startActivity(int size, long resvTime) {
        Logger.d("call nearbydrive allocationIdx = " + allocationIdx);
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("allocationIdx", allocationIdx);
        intent.putExtra("scheduleCount", size);
        intent.putExtra("a_date", resvTime);
        intent.putExtra("a_type", "nearby");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    // ============================================================================================
    // ==================================   플로핑 버튼  ==================================
    // ============================================================================================

    public View floatingView;
    public ImageView serviceMintCircle;
    public ImageView serviceSubMintView;

    private Animation animMintBtn;
    private Animation animSubMintBtn;

    private LayoutInflater inflate;

    private void setFloatingAction() {
        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.O || Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) {
            try {
                initFloationButton();
            } catch (Exception e){
                Toast.makeText(this, "다른 앱 위에 표시되는 앱 권한을 허용해 주세요.", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                initFloationButton();
            } else {
                Toast.makeText(this, "다른 앱 위에 표시되는 앱 권한을 허용해 주세요.", Toast.LENGTH_LONG).show();
            }

        } else {
            initFloationButton();
        }
    }

    /**
     * 플로팅버튼 셋팅
     */
    private void initFloationButton() {
        mManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        inflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        floatingView = inflate.inflate(R.layout.service_floating_btn_common, null);
        initFloatingView();

        initBtnAnimation();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }
        gestureDetector = new GestureDetectorCompat(this, new SingleTapConfirm());

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = (int) Util.convertDpToPixel(25.3f, MyLocationService.this);
        params.y = (int) Util.convertDpToPixel(472f, MyLocationService.this);

        if (PrefUtil.getFloatingLocationX(MyLocationService.this) > 0) {
            params.x = PrefUtil.getFloatingLocationX(MyLocationService.this);
        }

        if (PrefUtil.getFloatingLocationY(MyLocationService.this) > 0) {
            params.y = PrefUtil.getFloatingLocationY(MyLocationService.this);
        }

//        mManager.addView(mViewChangeBtn, params);
        mManager.addView(floatingView, params);
    }

    private void initFloatingView() {
        floatingView.setOnTouchListener(mViewTouchListener);
        floatingView.setVisibility(View.GONE);

        serviceMintCircle = floatingView.findViewById(R.id.service_mint_circle);
        serviceSubMintView = floatingView.findViewById(R.id.service_sub_mint_view);
    }

    private void setFloationView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 다른 앱 위에 표시되는 앱 권한 체크
            if (!Settings.canDrawOverlays(this)) {
                // 권한 사용 안하는 중
                Logger.e("LOG1 : Unuse canDrawOverlays");
            } else {
                if(mManager != null && floatingView != null) {
                    mManager.removeView(floatingView);
                    mManager.addView(floatingView, params);
                }
            }
        } else {
            if(mManager != null && floatingView != null) {
                mManager.removeView(floatingView);
                mManager.addView(floatingView, params);
            }
        }
    }

    /**
     * 애니메이션 초기화
     */
    private void initBtnAnimation() {
        animMintBtn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.service_mint_animation);
        animSubMintBtn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.service_sub_mint_animation);
    }

    public void startBtnAnimation(String uri, long alloc_idx) {
        floatingBtnUri = uri;
        floating_allocDetailIdx = alloc_idx;
        UIThread.executeInUIThread(animationThread);
    }

    public Runnable animationThread = new Runnable() {
        @Override
        public void run() {
            setBtnAnimation();

            if(MacaronApp.isNews) {
                UIThread.executeInUIThread(animationThread, 800);
            }
        }
    };

    public void floatingBtnData(String uri, long alloc_idx) {
        floatingBtnUri = uri;
        floating_allocDetailIdx = alloc_idx;
    }

    /**
     * 애니메이션 실행메소드
     */
    private void setBtnAnimation() {
        serviceMintCircle.startAnimation(animMintBtn);
        serviceSubMintView.setVisibility(View.VISIBLE);

        serviceSubMintView.startAnimation(animSubMintBtn);
    }

    public void cancelBtnAnimation() {
        serviceMintCircle.clearAnimation();
        serviceSubMintView.clearAnimation();
        UIThread.removeUIThread(animationThread);
    }

    /**
     * 플로팅버튼을 눌렀을 때 실행로직
     */
    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            String uri;

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);

            switch(MacaronApp.topActivity)
            {
                case Global.TOP_SCREEN.ALLOCSELECT:
                    uri = Global.URI.ALLOC_SELECT;
                    break;
                default:
                    if(floatingBtnUri.equals(Global.URI.ALLOC_DETAIL)) {
                        uri = Global.URI.ALLOC_DETAIL;
                        intent.putExtra("a_detail", floating_allocDetailIdx);
                        intent.putExtra("a_title", "예약 상세");
                        intent.putExtra("a_flags", false);
                        intent.putExtra("allocActivityType", AllocDetailActivity.ACTIVITY_TYPE_DEFAULT);
                    }
                    else
                        uri = Global.URI.TMAP_END;
                    break;
            }

            intent.setData(Uri.parse(uri));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(MyLocationService.this, 0, intent, 0);
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }

            floatingBtnUri = Global.URI.TMAP_END;   // 기본 URI

            return true;
        }
    }

    public View.OnTouchListener mViewTouchListener = new View.OnTouchListener() {

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (gestureDetector.onTouchEvent(event)) {
                return true;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mTouchX = event.getRawX();
                    mTouchY = event.getRawY();
                    mViewX = params.x;
                    mViewY = params.y;
                    break;

                case MotionEvent.ACTION_MOVE:
                    params.x = mViewX + (int) (event.getRawX() - mTouchX);
                    params.y = mViewY + (int) (event.getRawY() - mTouchY);
                    mManager.updateViewLayout(floatingView, params);
                    break;

                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    if(deviceSize[0] < params.x + Util.convertDpToPixel(90f, MyLocationService.this)) {
                        params.x = deviceSize[0] - (int)Util.convertDpToPixel(90f, MyLocationService.this);
                    } else if(params.x < 0) {
                        params.x = 0;
                    }

                    if(deviceSize[1] < params.y + Util.convertDpToPixel(90f, MyLocationService.this)) {
                        params.y = deviceSize[1] - (int)Util.convertDpToPixel(90f, MyLocationService.this);
                    } else if(params.y < 0) {
                        params.y = 0;
                    }

                    PrefUtil.setFloatingLocationX(MyLocationService.this, params.x);
                    PrefUtil.setFloatingLocationY(MyLocationService.this, params.y);
                    break;
            }

            return false;
        }
    };


    // ============================================================================================
    // ============================================================================================
    // ============================================================================================

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public MyLocationService getService(){
            return MyLocationService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        String type = intent.getStringExtra("type");

        if(!TextUtils.isEmpty(type)) {
            switch (type) {
                case "showFloatingButton":
                    setFloationView();
                    break;
            }
        }

        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.e("onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Logger.e("MyLocationService onDestroy");
        super.onDestroy();

        if (mLocationManager != null) {
            try {
                mLocationManager.removeUpdates(gpsListener);
                mLocationManager.removeUpdates(netListener);
            } catch (Exception ex) {
                Log.i(TAG, "fail to remove location listener, ignore", ex);
            }
        }

        if (floatingView != null) {
            ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(floatingView);
            floatingView = null;
        }

        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        if(tts != null) {
            //tts.shutdown();
        }

        try {
            UIThread.removeUIThread(timer5sec);
        } catch (Exception e){}

    }

}
