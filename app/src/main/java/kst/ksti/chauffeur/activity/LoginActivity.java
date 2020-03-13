package kst.ksti.chauffeur.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.HashMap;

import kst.ksti.chauffeur.common.Global;
import kst.ksti.chauffeur.BuildConfig;
import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.activity.signup.auth.PhoneAuthActivity;
import kst.ksti.chauffeur.common.AnalyticsHelper;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.common.UIThread;
import kst.ksti.chauffeur.databinding.ActivityLoginBinding;
import kst.ksti.chauffeur.listner.ChangeStatusInterface;
import kst.ksti.chauffeur.listner.ReverseGeocodingInterface;
import kst.ksti.chauffeur.model.ChauffeurInfo;
import kst.ksti.chauffeur.net.DataInterface;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.utility.BackPressCloseHandler;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.MacaronCustomDialog;
import kst.ksti.chauffeur.utility.OnSingleClickListener;
import kst.ksti.chauffeur.utility.PrefUtil;
import kst.ksti.chauffeur.utility.Util;

public class LoginActivity extends BaseActivity<ActivityLoginBinding> {

    private Thread loginParamsThread;

    private MacaronCustomDialog dialog;
    private HashMap<String, Object> loginParams = new HashMap<>();
    private BackPressCloseHandler backPressCloseHandler;
    private int serverChangeClickCount = 1;
    private Toast toast;

    private LocationManager mLocationManager = null;

    private boolean loginActivityExitFlag = false;  // 로그인 액티비티가 죽을때 앱이 종료되었는지 판단하는 플래그
    private boolean gpsCheck = false;
    private static final int MAX_LOGIN_INTERVAL = 3000;
    private int intervalCount = 0;

    private long alloc_idx = -1;
    private String a_title;
    private boolean a_flags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.d(getResources().getString(R.string.begin_activity));    // 액티비티 호출 로그

        Intent intent = getIntent();
        if (intent != null) {
            alloc_idx = intent.getLongExtra("a_detail", -1);
            a_title = intent.getStringExtra("a_title");
            a_flags = intent.getBooleanExtra("a_flags", true);
        }

        MacaronApp.chauffeur = null;
        MacaronApp.workStatus = AppDef.ActivityStatus.NONE;

        gpsCheck = false;
        loginActivityExitFlag = false;
        intervalCount = 0;

        boolean logout = getIntent().getBooleanExtra(Global.INTENT_EXTRA_NAME.LOG_OUT, false);
        String msg = getIntent().getStringExtra(Global.INTENT_EXTRA_NAME.LOG_OUT_MSG);
        if (logout) {
            toast = Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT);
            toast.show();
        }

        initGPS();

        setBind(R.layout.activity_login);

        setLoadingBarLayout(getBind().loadingBarLayout);
        setProgressBar(getBind().progressBar);

        hiddenFunction();

        setButtonClickListener();
        setEditTextChangeEvent();
        setPrefLoginInfo();
        setParams();

        backPressCloseHandler = new BackPressCloseHandler(this);
    }


    // ===========================================================================================
    // 히든기능
    // ===========================================================================================
    private void hiddenFunction() {
        if (BuildConfig.DEBUG) {
            if (Global.getDEV()) {
                String dev_type = Global.getServerType();
                getBind().tvServerChange.setText(dev_type);
            } else {
                getBind().tvServerChange.setText("");
            }
            getBind().tvServerChange.setVisibility(View.VISIBLE);
        } else {
            getBind().tvServerChange.setVisibility(View.GONE);
        }
    }

    // ===========================================================================================
    // ===========================================================================================
    // ===========================================================================================

    @Override
    protected void onResume() {
        super.onResume();
        AnalyticsHelper.getInstance(this).sendScreenFromJson(this, LoginActivity.class.getSimpleName());
        PrefUtil.setActivityStatus(getApplicationContext(), AppDef.ActivityStatus.LOGIN.toString());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        alloc_idx = intent.getLongExtra("a_detail", -1);
        a_title = intent.getStringExtra("a_title");
        a_flags = intent.getBooleanExtra("a_flags", true);
    }

    @Override
    protected void onDestroy() {

        if (!loginActivityExitFlag) {
            PrefUtil.setActivityStatus(getApplicationContext(), AppDef.ActivityStatus.NONE.toString());
            MacaronApp.lastLocation = null;
        }

        super.onDestroy();
    }

    /**
     * 클릭리스너 초기화
     */
    private void setButtonClickListener() {
        getBind().idCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getBind().id.setText("");
                getBind().id.requestFocus();
            }
        });
        getBind().phoneNoCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getBind().phoneNo.setText("");
                getBind().phoneNo.requestFocus();
            }
        });
        getBind().carNoCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getBind().carNo.setText("");
                getBind().carNo.requestFocus();
            }
        });
        getBind().pwCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getBind().pw.setText("");
                getBind().pw.requestFocus();
            }
        });

        // 신규 회원 가입 버튼
        getBind().btnSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(this, PhoneAuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("authType", AppDef.AuthType.PRIVATE);
            startActivity(intent);
        });

        // 법인 등록 버튼
        getBind().btnRegisterCompany.setOnClickListener(v -> {
            Intent intent = new Intent(this, PhoneAuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("authType", AppDef.AuthType.CORPORATE);
            startActivity(intent);
        });

        getBind().btnLogin.setOnClickListener(onSingleClickListener);
        getBind().txtForgetpw.setOnClickListener(onSingleClickListener);

        getBind().imgServerChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BuildConfig.DEBUG) {
//                    if (serverChangeClickCount < 10) {
//                        serverChangeClickCount++;
//
//                    } else {
//                        Global.setDev(!Global.getDEV());
//                        if (!Global.getDEV()) {
//                            getBind().tvServerChange.setText("");
//                            Toast.makeText(getApplicationContext(), "상용서버로 변경됐습니다.", Toast.LENGTH_SHORT).show();
//                        } else {
//                            String dev_type = Global.getServerType();
//                            getBind().tvServerChange.setText(dev_type);
//                            Toast.makeText(getApplicationContext(), "개발서버로 변경됐습니다.", Toast.LENGTH_SHORT).show();
//                        }
//
//                        serverChangeClickCount = 1;
//                        getBind().tvServerChange.setVisibility(View.VISIBLE);
//                        DataInterface.setDataInterface(null);
//                    }
                }
            }
        });
    }

    /**
     * 저장된 로그인정보가 있는지 체크해서 정보가 있으면 입력필드에 쓰기
     */
    private void setPrefLoginInfo() {
        int count = 0;
        if (!TextUtils.isEmpty(PrefUtil.getLoginId(this))) {
            getBind().id.setText(PrefUtil.getLoginId(this));
            count++;
        }

        if (!TextUtils.isEmpty(PrefUtil.getLoginPhoneNo(this))) {
            getBind().phoneNo.setText(PrefUtil.getLoginPhoneNo(this));
            count++;
        }

//        if (!TextUtils.isEmpty(PrefUtil.getLoginCarNo(this))) {
//            getBind().carNo.setText(PrefUtil.getLoginCarNo(this));
//            count++;
//        }

        if(count == 2) {
            getBind().pw.requestFocus();
        }
    }

    /**
     * EditText 이벤트 적용
     */
    private void setEditTextChangeEvent() {
        getBind().id.addTextChangedListener(new InnerTextWatcherClass(getBind().id));
        getBind().carNo.addTextChangedListener(new InnerTextWatcherClass(getBind().carNo));
        getBind().pw.addTextChangedListener(new InnerTextWatcherClass(getBind().pw));

//        getBind().phoneNo.addTextChangedListener(new InnerTextWatcherClass(getBind().phoneNo));


        getBind().phoneNo.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence sequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence sequence, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String afterTextChanged = editable.toString();

                if (afterTextChanged.length() > 0) {
                    getBind().phoneNoCancel.setVisibility(View.VISIBLE);
                } else {
                    getBind().phoneNoCancel.setVisibility(View.GONE);
                }

                if(afterTextChanged.contains("-")) {    // 하이픈이 1개라도 있을 때
                    String hyphenSplit[] = afterTextChanged.split("-");

                    if(hyphenSplit.length == 2) {     // 하이픈이 1개 있을 때
                        if(afterTextChanged.length() == 11) {
                            setHyphen(afterTextChanged,6, true);
                        }

                    } else if(hyphenSplit.length == 3) {   // 하이픈이 2개 있을 때
                        if(afterTextChanged.lastIndexOf("-") == 7 && afterTextChanged.length() == 13) {
                            setHyphen(afterTextChanged,7, true);

                        } else if(afterTextChanged.lastIndexOf("-") == 8 && afterTextChanged.length() == 12) {
                            setHyphen(afterTextChanged,6, true);
                        }
                    }

                } else {    // 하이픈이 1개도 없을 때
                    if(afterTextChanged.length() == 10) {   // 가운데가 3자리 번호일 때
                        setHyphen(afterTextChanged,6, false);

                    } else if(afterTextChanged.length() == 11) {    // 가운데가 4자리 번호일 때
                        setHyphen(afterTextChanged,7, false);
                    }
                }
            }

            private void setHyphen(String afterTextChanged, int p3, boolean isHyphen) {
                if(isHyphen) {
                    afterTextChanged = afterTextChanged.replaceAll("-", "");
                }
                String s1 = afterTextChanged.substring(0,3);
                String s2 = afterTextChanged.substring(3,p3);
                String s3 = afterTextChanged.substring(p3);
                String tmp = s1 + "-" + s2 + "-" + s3;
                getBind().phoneNo.setText(tmp);
                getBind().phoneNo.setSelection(getBind().phoneNo.getText().toString().length());
            }
        });
    }

    /**
     * EditText 이벤트 적용시킨 내부클래스
     */
    private class InnerTextWatcherClass implements TextWatcher {

        private EditText editText;

        InnerTextWatcherClass(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            try {
                switch (editText.getId()) {
                    case R.id.id:
                        if (editable.length() > 0) {
                            getBind().idCancel.setVisibility(View.VISIBLE);
                        } else {
                            getBind().idCancel.setVisibility(View.GONE);
                        }
                        break;

                    case R.id.car_no:
                        if (editable.length() > 0) {
                            getBind().carNoCancel.setVisibility(View.VISIBLE);
                        } else {
                            getBind().carNoCancel.setVisibility(View.GONE);
                        }
                        break;

                    case R.id.pw:
                        if (editable.length() > 0) {
                            getBind().pwCancel.setVisibility(View.VISIBLE);
                        } else {
                            getBind().pwCancel.setVisibility(View.GONE);
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 로그인 파라미터 세팅
     */
    private void setParams() {
        loginParams.put("appToken", PrefUtil.getPushKey(this));
        loginParams.put("appOs", "ANDROID");
        //loginParams.put("appVersion", BuildConfig.VERSION_NAME + "_" + BuildConfig.VERSION_CODE);
        loginParams.put("appVersion", BuildConfig.VERSION_NAME);
//        loginParams.put("appVersion", BuildConfig.VERSION_NAME);
        loginParams.put("mobileModel", Build.MODEL);                    // 모바일 기계 모델명
        loginParams.put("mobileManufacturer", Build.MANUFACTURER);      // 모바일 기계 회사

        loginParamsThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Context ctx = LoginActivity.this.getApplicationContext();
                    AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(ctx);
                    loginParams.put("adsId", adInfo.getId());

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (loginParamsThread != null && loginParamsThread.isAlive())
                        loginParamsThread.interrupt();
                }
            }
        });

        loginParamsThread.start();
    }

    /**
     * 싱글클릭리스너
     */
    private OnSingleClickListener onSingleClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            switch (v.getId()) {
                case R.id.btnLogin:
                    checkGps();
                    break;

                case R.id.txtForgetpw:
                    Util.hideKeyboard(LoginActivity.this);
                    forgetPassword();
                    break;
            }
        }
    };

    /**
     * 위치정보 켜있는지 확인
     */
    @SuppressLint("MissingPermission")
    private void checkGps() {
        if (!Util.chkGPS(LoginActivity.this)) {
            getBaseGpsDialog().show();
        } else {
            clickLoginBtn();
        }
    }

    /**
     * 로그인버튼 클릭액션
     */
    private void clickLoginBtn() {
        Util.hideKeyboard(LoginActivity.this);

        if (checkLoginInput()) {
            if (!gpsCheck) {
                playLoadingViewAnimation();
                UIThread.executeInUIThread(gpsCheckRunnable, 100);

            } else {
                getLoginInfo(loginParams);
            }
        }
    }

    private Runnable gpsCheckRunnable = new Runnable() {
        @Override
        public void run() {
            if (!gpsCheck) {
                if (intervalCount >= MAX_LOGIN_INTERVAL) {
                    UIThread.removeUIThread(gpsCheckRunnable);

                    loginParams.put("lat", 0);
                    loginParams.put("lon", 0);
                    loginParams.put("poi", "");
                    loginParams.put("address", "");

                    showLoginDialog(null, getString(R.string.gps_error_message), getString(R.string.txt_confirm),
                            new View.OnClickListener() {
                                public void onClick(View v) {
                                    dialog.dismiss();
                                    getLoginInfo(loginParams);
                                }
                            },
                            true, false);

                    return;
                }

                intervalCount += 100;
                UIThread.executeInUIThread(gpsCheckRunnable, 100);

            } else {
                UIThread.removeUIThread(gpsCheckRunnable);
                getLoginInfo(loginParams);
            }
        }
    };

    /**
     * 입력정보 확인하고서 정상적이면 통신 파라미터에 추가
     * @return boolean
     */
    private boolean checkLoginInput() {
        // 아이디 확인
        String id = getBind().id.getText().toString().trim();
        if (TextUtils.isEmpty(id)) {
            Toast.makeText(LoginActivity.this, "아이디를 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 핸드폰번호 확인
        String phoneNo = getBind().phoneNo.getText().toString().trim();
        if (TextUtils.isEmpty(phoneNo)) {
            Toast.makeText(LoginActivity.this, "휴대폰번호를 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!Util.isValidPhoneNumber(phoneNo)) {
            Toast.makeText(LoginActivity.this, "올바른 휴대폰 번호가 아닙니다.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 차량번호 확인
//        String carNo = getBind().carNo.getText().toString().trim().replaceAll(" ", "");
//        if (TextUtils.isEmpty(carNo)) {
//            Toast.makeText(LoginActivity.this, "차량번호를 입력해 주세요.", Toast.LENGTH_SHORT).show();
//            return false;
//        }

        // 비밀번호 확인
        String pw = getBind().pw.getText().toString().trim();
        if (TextUtils.isEmpty(pw)) {
            Toast.makeText(LoginActivity.this, "비밀번호를 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }

        loginParams.put("id", id);
        loginParams.put("mobileNo", phoneNo);
//        loginParams.put("carNo", carNo);
        loginParams.put("pwd", pw);

        return true;
    }

    /**
     * 로그인 서버통신
     * @param param id, phoneNo, carNo, pw, appToken, appOs, appVersion, mobileModel, mobileManufacturer, adsId, lat, lon, poi
     */
    private void getLoginInfo(HashMap<String, Object> param) {
        playLoadingViewAnimation();

        DataInterface.getInstance().login(LoginActivity.this, param, new DataInterface.ResponseCallback<ResponseData<ChauffeurInfo>>() {
            @Override
            public void onSuccess(ResponseData<ChauffeurInfo> response) {
                if (response.getResultCode().equals("S000")) {
                    MacaronApp.chauffeur = response.getData();
                    MacaronApp.chauffeur.accessToken = response.getData().accessToken;

                    // 영광, 무안 지역 저장
                    if(MacaronApp.chauffeur.companyVo != null) {
                        PrefUtil.setArea(LoginActivity.this, MacaronApp.chauffeur.companyVo.getSignupAreaDescriptionName());
                    }

                    AnalyticsHelper.getInstance(LoginActivity.this).sendEvent("로그인", "로그인 성공", "", FirebaseAnalytics.Event.LOGIN);

                    MacaronApp.workStatus = AppDef.ActivityStatus.LOGIN;

                    saveLoginInfo();
                    startMainActivity();

                    // 쇼퍼 로그인 상태
                    MacaronApp.clientChauffeurStatus = AppDef.ChauffeurStatus.CONNECT;

                } else {
                    AnalyticsHelper.getInstance(LoginActivity.this).sendEvent("로그인", "로그인 실패", "", FirebaseAnalytics.Event.LOGIN);
                    cancelLoadingViewAnimation();
                }
            }

            @Override
            public void onError(ResponseData<ChauffeurInfo> response) {
                AnalyticsHelper.getInstance(LoginActivity.this).sendEvent("로그인", "로그인 실패", "", FirebaseAnalytics.Event.LOGIN);
                cancelLoadingViewAnimation();
            }

            @Override
            public void onFailure(Throwable t) {
                AnalyticsHelper.getInstance(LoginActivity.this).sendEvent("로그인", "로그인 실패", Util.getExceptionError(t), FirebaseAnalytics.Event.LOGIN);
                cancelLoadingViewAnimation();
            }
        });
    }

    /**
     * 로그인 정보 저장
     */
    private void saveLoginInfo() {
        PrefUtil.setLoginId(LoginActivity.this, getBind().id.getText().toString());
        PrefUtil.setLoginPhoneNo(LoginActivity.this, getBind().phoneNo.getText().toString().replaceAll("-", ""));
        PrefUtil.setLoginPwd(LoginActivity.this, getBind().pw.getText().toString());
    }

    @Override
    public void onBackPressed() {
        if(toast != null) toast.cancel();
        backPressCloseHandler.onBackPressed();
    }

    /**
     * 메인액티비티로 이동
     */
    private void startMainActivity() {
        loginActivityExitFlag = true;

        try {
            mLocationManager.removeUpdates(gpsListener);
            mLocationManager.removeUpdates(netListener);

        } catch (Exception ignore) { }

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);

        if(alloc_idx > 0 && !TextUtils.isEmpty(a_title)) {
            intent.putExtra("a_detail", alloc_idx);
            intent.putExtra("a_title", a_title);
            intent.putExtra("a_flags", a_flags);
        }
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    /**
     * 핸드폰 다이얼 띄움
     */
    private void callCompany() {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Global.COMPANY_PHONENO));
        startActivity(intent);
    }

    /**
     * 비밀번호 찾기 팝업
     */
    private void forgetPassword() {
        showLoginDialog("비밀번호 찾기", getResources().getString(R.string.call_company), "전화하기",
                new View.OnClickListener() {
                    public void onClick(View v) {
                        callCompany();
                        dialog.dismiss();
                    }
                }, true, true);
    }

    /**
     * 로그인 화면 공통 다이얼로그 호출함수
     */
    private void showLoginDialog(String title, String content, String btnTitle, View.OnClickListener clickListener, boolean isThemePink, boolean isCloseBtn) {
        if(dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        dialog = new MacaronCustomDialog(LoginActivity.this, title, content, btnTitle, clickListener, isThemePink, isCloseBtn);
        dialog.show();
    }



    // ===========================================================================================
    // 히든기능
    // ===========================================================================================


    private String testPW = "adaadad";
    private String testTmp = "";

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if(BuildConfig.DEBUG) {
//            switch (keyCode) {
//                case KeyEvent.KEYCODE_VOLUME_UP:
//                    testTmp = testTmp + "a";
//                    return hidden();
//                case KeyEvent.KEYCODE_VOLUME_DOWN:
//                    testTmp = testTmp + "d";
//                    return hidden();
//            }
//        }
//
//        return super.onKeyDown(keyCode, event);
//    }

    /**
     * 볼륨키 히든기능
     */
    private boolean hidden() {
        if(!TextUtils.isEmpty(testTmp)) {
            if(testTmp.length() > 7) {
                testTmp = "";
                Toast.makeText(LoginActivity.this, "초기화", Toast.LENGTH_SHORT).show();
            } else {
                if(testTmp.equalsIgnoreCase(testPW)) {
                    testTmp = "";
                    getBind().id.setText("hdpark");
                    getBind().phoneNo.setText("");
                    getBind().phoneNo.setText("01023645325");
                    getBind().pw.setText("mmmm");
                }
            }
        }
        return true;
    }


    // ===========================================================================================
    // GPS
    // ===========================================================================================


    /**
     * GPS 초기화
     */
    @SuppressLint("MissingPermission")
    private void initGPS() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (mLocationManager != null) {
            Logger.d("## 위치정보 요청 중... ");

//            Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            if(location == null || location.getLongitude() <= 0 || location.getLatitude() <= 0) {
//                location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//            }
//
//            if(location != null && location.getLongitude() > 0 && location.getLatitude() > 0) {
//                MacaronApp.lastLocation = location;
//                Util.getLocationInfomationParams(LoginActivity.this, reverseGeocodingInterface, null, null);
//            }

            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0.0f, gpsListener);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0.0f, netListener);
        }
    }

    /**
     * GPS 정보가 들어오면 gps,network 전부 제거하고 loginParams에 GPS 정보 입력
     * @param location 수신받은 GSP 정보
     * @throws NullPointerException GPS 없을 때
     */
    @SuppressLint("MissingPermission")
    public void updateWithNewLocation(Location location) throws NullPointerException {
        if(location != null) {
            // ggomzzin
            MacaronApp.lastLocation = location;
            Util.getLocationInfomationParams(LoginActivity.this, reverseGeocodingInterface, null, null);

            mLocationManager.removeUpdates(gpsListener);
            mLocationManager.removeUpdates(netListener);
        }
    }

    /**
     * Tmap에서 위치정보 받아오는 콜백리스너
     */
    private ReverseGeocodingInterface reverseGeocodingInterface = new ReverseGeocodingInterface() {
        @Override
        public void onSuccess(final HashMap<String, Object> params, ChangeStatusInterface changeStatusInterface) {
            Logger.d("## Login Geocoding onSuccess()");
            loginParams.putAll(params);
            gpsCheck = true;
        }

        @Override
        public void onError(HashMap<String, Object> params, ChangeStatusInterface changeStatusInterface, final String errorMsg) {
            Logger.d("## Login Geocoding onError()");
            UIThread.executeInUIThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            });

            loginParams.putAll(params);
            gpsCheck = true;
        }

        @Override
        public void onGpsError(HashMap<String, Object> params, ChangeStatusInterface changeStatusInterface) {
            Logger.d("## Login Geocoding onGpsError()");
            loginParams.putAll(params);
            gpsCheck = true;
        }
    };

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
                updateWithNewLocation(location);
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
                updateWithNewLocation(location);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    };

}
