package kst.ksti.chauffeur;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.multidex.MultiDex;
import android.view.View;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;
import kst.ksti.chauffeur.common.Global;
import kst.ksti.chauffeur.activity.BaseActivity;
import kst.ksti.chauffeur.activity.MainActivity;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.common.UIThread;
import kst.ksti.chauffeur.model.AllocationSchedule;
import kst.ksti.chauffeur.model.AllocationSelectModel;
import kst.ksti.chauffeur.model.ChauffeurInfo;
import kst.ksti.chauffeur.model.StartRoadsale;
import kst.ksti.chauffeur.service.MyLocationService;
import kst.ksti.chauffeur.service.MyLocationServiceManager;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.PrefUtil;

import static android.speech.tts.TextToSpeech.ERROR;

public class MacaronApp extends Application {

    public static ChauffeurInfo chauffeur = new ChauffeurInfo();
    public static AllocationSchedule currAllocation = new AllocationSchedule();
    public static StartRoadsale currStartRoadsale = new StartRoadsale();
    public static Location lastLocation;
    public static int scheduleCount;
    public static boolean nearByDriveStartCheck;     // 운행임박 폴링 Flag : true 허용, false 불가
    public static boolean nearByDrivingStatusCheck;  // 운행임박화면 시작버튼 Flag : true 허용, false 불가
    public static boolean isNews;
    public static boolean isRestoreLogic;            // 앱 복구시 분기 로직 true : 허용, false 불가
    public static List<Integer> notiIDList = new ArrayList<>();
    public static AppDef.ActivityStatus workStatus = AppDef.ActivityStatus.NONE;
    public static AppDef.ChauffeurStatus clientChauffeurStatus = AppDef.ChauffeurStatus.NONE;   // 클라이언트에서 사용 하는 쇼퍼 상태
    public static AppDef.AllocationStatus allocStatus = AppDef.AllocationStatus.NONE;           // 클라이언트에서 사용 하는 배차 상태
    public static String topActivity = Global.TOP_SCREEN.NONE;
    public static ArrayList<AllocationSelectModel> allocSelectList = new ArrayList<>();

    public static TextToSpeech tts;

    private int runningActivityCount;

    public enum AppStatus {
        BACKGROUND,
        RETURNED_TO_FOREGROUND, // or first launch
        FOREGROUND
    }

    public static volatile AppStatus appStatus = AppStatus.BACKGROUND;
    private UncaughtExceptionHandler unCatchExceptionHandler;
    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        context = this;
        Thread.setDefaultUncaughtExceptionHandler(unCatchExceptionHandler);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != ERROR) {
                    // 언어를 선택한다.
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

        scheduleCount = 0;
        nearByDriveStartCheck = false;
        nearByDrivingStatusCheck = true;
        isNews = false;
        isRestoreLogic = true;

        UIThread.initializeHandler();

        // fabric crash report 내부/상용 서버 배포
        if(Global.getCrashReport())
            Fabric.with(this, new Crashlytics());

        registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                runningActivityCount = runningActivityCount + 1;
                if (runningActivityCount == 1) {
                    appStatus = AppStatus.RETURNED_TO_FOREGROUND; // or first launch
                } else if (runningActivityCount > 1) {
                    appStatus = AppStatus.FOREGROUND;
                }

                if(appStatus == AppStatus.FOREGROUND || appStatus == AppStatus.RETURNED_TO_FOREGROUND) {
                    notifyForeground();
                }
            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                boolean isFloationg = false;

                runningActivityCount = runningActivityCount - 1;
                if (runningActivityCount == 0) {
                    appStatus = AppStatus.BACKGROUND;
                }

                // 플로팅 버튼 옵션 체크 or 예약 운행중이 아닐때
                if(PrefUtil.getOptionFloating(getApplicationContext()) || MacaronApp.allocStatus != AppDef.AllocationStatus.NONE) {
                    isFloationg = true;
                }

                if(appStatus == AppStatus.BACKGROUND && isFloationg) {
                    notifyBackground();
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
            }
        });
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    /**
     * onConfigurationChanged()
     * 컴포넌트가 실행되는 동안 단말의 화면이 바뀌면 시스템이 실행 한다.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    private void notifyForeground() {
        MacaronApp.isNews = false;

        if(MyLocationServiceManager.getInstance().getMyLocationService() != null) {
            if (MyLocationServiceManager.getInstance().getMyLocationService().floatingView != null) {
                MyLocationServiceManager.getInstance().getMyLocationService().cancelBtnAnimation();
                MyLocationServiceManager.getInstance().getMyLocationService().floatingView.setVisibility(View.GONE);
                MyLocationServiceManager.getInstance().getMyLocationService().serviceSubMintView.setVisibility(View.GONE);
            }
        }
    }

    private void notifyBackground() {
        // 플로팅버튼 출력
        if(MyLocationServiceManager.getInstance().getMyLocationService() != null) {
            if(MyLocationServiceManager.getInstance().getMyLocationService().floatingView != null) {
                MyLocationServiceManager.getInstance().getMyLocationService().onBind(new Intent().putExtra("type", "showFloatingButton"));
                MyLocationServiceManager.getInstance().getMyLocationService().floatingView.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * App Exception Catch
     */
    public UncaughtExceptionHandler getUncaughtExceptionHandler(Activity activity) {
        if(activity instanceof MainActivity) {
            if(unCatchExceptionHandler == null) {
                unCatchExceptionHandler = new UncaughtExceptionHandler((BaseActivity) activity);
            }
        }
        return unCatchExceptionHandler;
    }

    /**
     * App Exception Catch
     */
    public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        private final BaseActivity baseActivity;

        public UncaughtExceptionHandler(BaseActivity activity) {
            baseActivity = activity;
        }

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            ex.printStackTrace();
            Logger.e("LOG1 : ## 앱이 죽은 이유 = " + ex.toString());
            ex.printStackTrace();

            // fabric crash report 내부/상용 서버 배포
            if(Global.getCrashReport())
                Crashlytics.logException(ex);

            chauffeur = null;
            nearByDriveStartCheck = false;
            MacaronApp.topActivity = Global.TOP_SCREEN.NONE;
            MacaronApp.allocSelectList.clear();
            PrefUtil.setBackKeyCheck(getApplicationContext(), false);
            PrefUtil.setActivityStatus(getApplicationContext(), AppDef.ActivityStatus.NONE.toString());
            stopService(new Intent(getApplicationContext(), MyLocationService.class));

            HashMap<String, Object> params = new HashMap<>();
            params.put("chauffeurStatusCat", AppDef.ChauffeurStatus.EXIT.toString());
            baseActivity.sendChauffeurStatusAndGoNextScreen(params, null);

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) { }

            baseActivity.finishAffinity();
            System.runFinalization();
            System.exit(0); // kill off the crashed app
        }
    }

    //!MacaronApp.chauffeur.chauffeurStatusCat.equals("ROADSALE")

    /**
     * 쇼퍼 일반운행중인지 체크
     */
    public static boolean isCheckLoadSale() {

        if(chauffeur == null)
        {
            Logger.d("LOG1 : 쇼퍼 정보 없음");
            return false;
        }

        if(chauffeur.chauffeurStatusCat == null)
        {
            Logger.d("LOG1 : 쇼퍼 상태 정보 없음");
            return false;
        }

        if(chauffeur.chauffeurStatusCat.equals("ROADSALE"))
        {
            Logger.d("LOG1 : 일반운행중이다");
            return true;
        }

        return false;
    }

    @Override
    public void onTerminate()
    {
        super.onTerminate();

        // TTS 객체가 남아있다면 실행을 중지하고 메모리에서 제거한다.
        if(tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }
}
