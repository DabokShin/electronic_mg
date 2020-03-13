package kst.ksti.chauffeur.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kst.ksti.chauffeur.common.Global;
import kst.ksti.chauffeur.BuildConfig;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.common.AnalyticsHelper;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.common.DeepLik;
import kst.ksti.chauffeur.common.UIThread;
import kst.ksti.chauffeur.databinding.ActivitySplashBinding;
import kst.ksti.chauffeur.model.AppInfo;
import kst.ksti.chauffeur.net.DataInterface;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.utility.BackPressCloseHandler;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.MacaronCustomDialog;
import kst.ksti.chauffeur.utility.PrefUtil;

public class SplashActivity extends BaseActivity<ActivitySplashBinding> {

    private String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CALL_PHONE, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private final int PERMISSIONS_REQUEST_ACCOUNTS = 100;
    private final int REQ_CODE_OVERLAY_PERMISSION = 101;
    private static final int LOADING_DELAY_TIME = 2000;

    private MacaronCustomDialog macaronCustomDialog;
    private BackPressCloseHandler backPressCloseHandler;

    private long alloc_idx = -1;
    private String a_title;
    private boolean a_flags;
    private String a_type;

    private Runnable splashFinishedThread = new Runnable() {
        @Override
        public void run() {
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.d(getResources().getString(R.string.begin_activity));    // 액티비티 호출 로그

        if (!isTaskRoot()) {
            final Intent intent = getIntent();
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(intent.getAction())) {
                Logger.e("Main Activity is not the root.  Finishing Main Activity instead of launching.");
                finish();
                return;
            }
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Intent intent = getIntent();
        if (intent != null) {
            alloc_idx = intent.getLongExtra("a_detail", -1);
            a_title = intent.getStringExtra("a_title");
            a_flags = intent.getBooleanExtra("a_flags", true);
            a_type = intent.getStringExtra("a_type");
        }

        setBind(R.layout.activity_splash);
        setLogoImageAnimation();
        PrefUtil.setBackKeyCheck(getApplicationContext(), false);
        checkPushKey();
        getAppVersion();

        backPressCloseHandler = new BackPressCloseHandler(this);
    }

    /**
     * 로고이미지 애니메이션 적용
     */
    private void setLogoImageAnimation() {
        String str = PrefUtil.getArea(SplashActivity.this);

        // 영광 무안에 따른 이미지 변경
        if(str != null || !str.isEmpty()) {
            if(str.equals("영광")) {
                getBind().splash01.setImageResource(R.drawable.splash_icon_1);
                getBind().splash03.setText("영광군 배달");
            }
            else if(str.equals("무안")) {
                getBind().splash01.setImageResource(R.drawable.splash_icon_2);
                getBind().splash03.setText("무안군 배달");
            }
        }


        getBind().splash01.setAlpha(0f);
        getBind().splash01.animate().alpha(1f).setStartDelay(300).setDuration(500).translationY(-50).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                getBind().splash01.setVisibility(View.VISIBLE);
            }
        }).withLayer();

        getBind().splash02.setAlpha(0f);
        getBind().splash02.animate().alpha(1f).setStartDelay(600).setDuration(500).translationY(-50).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                getBind().splash02.setVisibility(View.VISIBLE);
            }
        }).withLayer();

        getBind().splash03.setAlpha(0f);
        getBind().splash03.animate().alpha(1f).setStartDelay(600).setDuration(500).translationY(-50).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                getBind().splash03.setVisibility(View.VISIBLE);
            }
        }).withLayer();

        getBind().splash04.setAlpha(0f);
        getBind().splash04.animate().alpha(1f).setStartDelay(600).setDuration(500).translationY(-50).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                getBind().splash04.setVisibility(View.GONE);
            }
        }).withLayer();
    }

    /**
     * 푸시토큰키 체크
     */
    private void checkPushKey() {
        if (PrefUtil.getPushKey(SplashActivity.this).equals("")) {
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(SplashActivity.this, new OnSuccessListener<InstanceIdResult>() {
                @Override
                public void onSuccess(InstanceIdResult instanceIdResult) {
                    String token = instanceIdResult.getToken();
                    Logger.d("token onSuccess = " + token);
                    PrefUtil.setPushKey(SplashActivity.this, token);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AnalyticsHelper.getInstance(this).sendScreenFromJson(this, SplashActivity.class.getSimpleName());
    }

    /**
     * 버전체크 서버통신
     */
    private void getAppVersion() {
        DataInterface.getInstance().getAppVersion(SplashActivity.this, new DataInterface.ResponseCallback<ResponseData<AppInfo>>(){
            @Override
            public void onSuccess(ResponseData<AppInfo> response) {
                Logger.d("response: " + response.getResultCode());
                if(response.getResultCode().equals("S000")){
                    systemAlertCheck((ArrayList<AppInfo>) response.getList());
                } else {
                    showDialog(SplashActivity.this, null, "네트웍상태를 확인해 주세요.");
                }
            }

            @Override
            public void onError(ResponseData<AppInfo> response) {
                showDialog(SplashActivity.this, null, "네트웍상태를 확인해 주세요.");
            }

            @Override
            public void onFailure(Throwable t) {
                showDialog(SplashActivity.this, null, "네트웍상태를 확인해 주세요.");
            }
        });
    }

    /**
     * 통신에러일 때 보여줄 팝업
     * @param context context
     * @param title 팝업 제목
     * @param msg 파업 내용
     */
    private void showDialog(Context context, String title, String msg) {
        macaronCustomDialog = new MacaronCustomDialog(context, title, msg, "확인", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                macaronCustomDialog.dismiss();
                finish();
            }
        });

        try {
            macaronCustomDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 시스템 알림 체크
     * @param appInfo 내려받은 앱정보
     */
    private void systemAlertCheck(ArrayList<AppInfo> appInfo) {
        HashMap<String, String> appVersionDetail = getAppVersionDetailInfo(appInfo, true);

        if (!appVersionDetail.isEmpty()) {
            final String systemAction = appVersionDetail.get(Global.AppVersionCheck.ACTION);

            macaronCustomDialog = new MacaronCustomDialog(
                    this,
                    null,
                    appVersionDetail.get(Global.AppVersionCheck.MESSAGE),
                    "확인",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            macaronCustomDialog.dismiss();
                            deepLinkAction(new DeepLik().separator(systemAction));
                        }
                    },
                    true);

            macaronCustomDialog.setCancelable(false);
            macaronCustomDialog.show();

        } else {
            appVersionCheck(appInfo);
        }
    }

    /**
     * 버전체크
     * @param appInfo 내려받은 앱정보
     */
    private void appVersionCheck(ArrayList<AppInfo> appInfo) {
        HashMap<String, String> appVersionDetail = getAppVersionDetailInfo(appInfo, false);

        if(!appVersionDetail.isEmpty()) {
            int appVersion = changeVersionNameToInt(BuildConfig.VERSION_NAME);
            if(appVersion < changeVersionNameToInt(appVersionDetail.get(Global.AppVersionCheck.VERSION))) {
                final String action = appVersionDetail.get(Global.AppVersionCheck.ACTION);

                macaronCustomDialog = new MacaronCustomDialog(this,
                        null,
                        appVersionDetail.get(Global.AppVersionCheck.MESSAGE),
                        "확인",
                        "닫기",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                macaronCustomDialog.dismiss();
                                goGoogleMarket();
                            }
                        },
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                macaronCustomDialog.dismiss();
                                deepLinkAction(new DeepLik().separator(action));
                            }
                        },
                        false);

                macaronCustomDialog.setCancelable(false);
                macaronCustomDialog.show();
            } else {
                startLoading();
            }

        } else {
            terminationToDataReceiveFail();
        }
    }

    /**
     * 딥링크 분기처리 함수
     * @param object 딥링크 정보
     */
    private void deepLinkAction(Object object) {
        try {
            if (object instanceof String) {
                onlyPathDeepLink(String.valueOf(object));
            } else {
                startLoading();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 딥링크 안에 쿼리정보가 없을 경우 수행되는 함수
     */
    private void onlyPathDeepLink(String path) {
        switch (path) {
            case Global.DEEP_LINK.EXIT:
                finish();
                break;

            case Global.DEEP_LINK.MAIN:
                startLoading();
                break;

            default:
                finish();
                break;
        }
    }

    /**
     * AppInfo 정보를 못받았거나 앱이랑 맞지 않았을때 앱종료
     */
    private void terminationToDataReceiveFail() {
        Toast.makeText(SplashActivity.this, getString(R.string.data_receive_fail), Toast.LENGTH_SHORT).show();
        UIThread.executeInUIThread(splashFinishedThread, 1500);
    }

    /**
     * 서버에서 받은 appInfo를 hashMap으로 반환
     * @param appInfo 내려받은 정보
     * @param systemCheck 시스템알림/버전체크 구분
     */
    private HashMap<String, String> getAppVersionDetailInfo(ArrayList<AppInfo> appInfo, boolean systemCheck) {
        HashMap<String, String> hashMap = new HashMap<>();

        for(int i=0; i<appInfo.size(); i++) {
            if(systemCheck) {
                if(appInfo.get(i).confCat.equalsIgnoreCase(Global.AppVersionCheck.CHAUFFEUR_SYSTEM)) {
                    if(appInfo.get(i).name.equalsIgnoreCase(Global.AppVersionCheck.MESSAGE)) {
                        hashMap.put(Global.AppVersionCheck.MESSAGE, appInfo.get(i).confValue);
                    }
                    if(appInfo.get(i).name.equalsIgnoreCase(Global.AppVersionCheck.ACTION)) {
                        hashMap.put(Global.AppVersionCheck.ACTION, appInfo.get(i).confValue);
                    }
                }

            } else {
                if(appInfo.get(i).confCat.equalsIgnoreCase(Global.AppVersionCheck.CHAUFFEUR_VERSION)) {
                    if(appInfo.get(i).name.equalsIgnoreCase(Global.AppVersionCheck.VERSION)) {
                        hashMap.put(Global.AppVersionCheck.VERSION, appInfo.get(i).confValue);
                    }
                    if(appInfo.get(i).name.equalsIgnoreCase(Global.AppVersionCheck.MESSAGE)) {
                        hashMap.put(Global.AppVersionCheck.MESSAGE, appInfo.get(i).confValue);
                    }
                    if(appInfo.get(i).name.equalsIgnoreCase(Global.AppVersionCheck.ACTION)) {
                        hashMap.put(Global.AppVersionCheck.ACTION, appInfo.get(i).confValue);
                    }
                }
            }
        }

        return hashMap;
    }

    /**
     * 구글마켓으로 이동
     */
    private void goGoogleMarket() {
        Intent i = new Intent(android.content.Intent.ACTION_VIEW);
        i.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
        startActivity(i);
        finish();
    }

    /**
     * 버전정보를 int형으로 반환
     * @param version 버전정보
     * @return 1.0.1 -> 10001 -> 1 x 10000, 0 x 100, 1 x 1
     */
    private int changeVersionNameToInt(String version) {
        int cipher = 10000;
        int verCipher = 0;
        int ver = 0;
        int lastVer = 0;

        String tmp[] = version.split("d");
        StringBuilder stringBuilder = new StringBuilder();

        for(int i=0; i<tmp.length; i++) {
            stringBuilder.append(tmp[i]);
        }

        String temp[] = stringBuilder.toString().split("\\.");
        StringBuilder versionNameBuilder = new StringBuilder();

        for(int i=0; i< temp.length; i++) {
            versionNameBuilder.append(temp[i]);
            try
            {
                ver = Integer.parseInt(temp[i]);    // 자릿수의 숫자를 뽑는다.
                verCipher = ver * cipher;           // 각 자릿수의 숫자를 두자리로 만들어준다.
                lastVer += verCipher;               // 모든 자릿수를 더해준다.

                cipher /= 100;
            }
            catch(Exception e)
            {
                Logger.e("LOG1 : 버전 자릿수 생성 실패");
            }
        }

        return lastVer;
    }

    /**
     * 2초 딜레이 후, 로직수행
     */
    private void startLoading() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(arePermissionsEnabled()) {
                        startLoginActivity();
                    } else {
                        //requestMultiplePermissions();

                        Intent intent = new Intent(SplashActivity.this, AppAccessright.class);

                        if(alloc_idx > 0 && !TextUtils.isEmpty(a_title)) {
                            intent.putExtra("a_detail", alloc_idx);
                            intent.putExtra("a_title", a_title);
                            intent.putExtra("a_flags", a_flags);
                            intent.putExtra("a_type", a_type);

                            Logger.e("## ============================= 11");
                        }

                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);

                    }
                } else {
                    startLoginActivity();
                }

            }
        }, LOADING_DELAY_TIME);
    }

    /**
     * 권한허용여부 체크
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean arePermissionsEnabled(){
        for(String permission : permissions){
            if(checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }

        if (!Settings.canDrawOverlays(this)) {
            return false;
        }

        return true;
    }


    /**
     * 로그인화면으로 이동
     */
    private void startLoginActivity() {
        String joinType = PrefUtil.getRegJoinType(this);

        // 쇼퍼 회원가입중일 경우는 가입 프로세스를 탄다.
        if(joinType == null || !joinType.isEmpty()) {
            if(joinType.equals(Global.JOIN_TYPE.CHAUFFEUR)) {     // 쇼퍼등록
                Logger.d("LOG1 : 쇼퍼회원 가입중");

                getRegistChauffeurStatus(SplashActivity.this);
            }
            else if(joinType.equals(Global.JOIN_TYPE.COMPANY)) {   // 법인등록
                Logger.d("LOG1 : 법인 가입중");

                getRegistCompanyStatusSignUp(SplashActivity.this);
            }
            else {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);

                if(alloc_idx > 0 && !TextUtils.isEmpty(a_title)) {
                    intent.putExtra("a_detail", alloc_idx);
                    intent.putExtra("a_title", a_title);
                    intent.putExtra("a_flags", a_flags);
                    intent.putExtra("a_type", a_type);

                    Logger.e("## ============================= 11");
                }

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
            }
        }
        else if(!isFinishing()) {

            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);

            if(alloc_idx > 0 && !TextUtils.isEmpty(a_title)) {
                intent.putExtra("a_detail", alloc_idx);
                intent.putExtra("a_title", a_title);
                intent.putExtra("a_flags", a_flags);
                intent.putExtra("a_type", a_type);

                Logger.e("## ============================= 11");
            }

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        }
    }

    /*//
    Util.isExecutionStatus(this)
     //*/

    private void startActivity(){
        if(!isFinishing()) {
            Intent intent;

            Logger.e("## activityStatus = " + PrefUtil.getActivityStatus(getApplicationContext()));

            if(PrefUtil.getActivityStatus(getApplicationContext()).equals(AppDef.ActivityStatus.MAIN.toString())) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }

            intent.putExtra("a_detail", alloc_idx);
            intent.putExtra("a_title", a_title);
            intent.putExtra("a_flags", a_flags);
            intent.putExtra("a_type", a_type);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);

        }
    }


    /**
     * 권한요청(필수권한, 다른앱위에그리기 권한)
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestMultiplePermissions(){
        boolean check = false;
        List<String> remainingPermissions = new ArrayList<>();

        for(String permission : permissions){
            if(checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                remainingPermissions.add(permission);
                check = true;
            }
        }

        if(check) {
            requestPermissions(remainingPermissions.toArray(new String[remainingPermissions.size()]), PERMISSIONS_REQUEST_ACCOUNTS);
        } else {
            if (!Settings.canDrawOverlays(this)) {
                requestPermissionSystemAlertWindow();
            } else {
                startLoginActivity();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCOUNTS:
                if(grantResults.length > 0) {
                    boolean permissionCheck = true;
                    for(int result : grantResults) {
                        // 필수권한 중 거부된 권한이 있는지 체크.
                        if(result != PackageManager.PERMISSION_GRANTED) {
                            permissionCheck = false;
                        }
                    }

                    if(permissionCheck) {
                        checkSystemAlertWindow();

                    } else {
                        Toast.makeText(SplashActivity.this, "앱을 정상적으로 이용하려면 권한동의 설정이 필요합니다.", Toast.LENGTH_SHORT).show();

                        UIThread.executeInUIThread(splashFinishedThread, 1000);
                    }
                }
                break;
        }
    }

    /**
     * SYSTEM_ALERT_WINDOW 권한체크
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void checkSystemAlertWindow() {
        if (!Settings.canDrawOverlays(this)) {
            requestPermissionSystemAlertWindow();
        } else {
            startLoginActivity();
        }
    }

    /**
     * SYSTEM_ALERT_WINDOW 권한 요청
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissionSystemAlertWindow() {
        String msg = "배달을 위해<br><font color='#ff1c74'>[다른 앱 위에 그리기]</font>에 대한 권한을 허용해 주셔야 합니다.<br>지금 설정해 주세요.";
        macaronCustomDialog = new MacaronCustomDialog(SplashActivity.this, null, Html.fromHtml(msg), "설정", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                macaronCustomDialog.dismiss();

                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQ_CODE_OVERLAY_PERMISSION);
            }
        }, false);
        macaronCustomDialog.setCancelable(false);
        macaronCustomDialog.show();
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_CODE_OVERLAY_PERMISSION: {
                if (Settings.canDrawOverlays(this)) {
                    startLoginActivity();
                } else {
                    Toast.makeText(SplashActivity.this, "앱을 정상적으로 이용하려면 권한동의 설정이 필요합니다.", Toast.LENGTH_SHORT).show();

                    UIThread.executeInUIThread(splashFinishedThread, 1000);
                }
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        backPressCloseHandler.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        UIThread.removeUIThread(splashFinishedThread);
    }

}
