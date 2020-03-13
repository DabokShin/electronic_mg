package kst.ksti.chauffeur.activity;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.skt.Tmap.TMapTapi;
import com.skt.Tmap.TMapView;

import java.util.HashMap;

import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.common.Global;
import kst.ksti.chauffeur.common.UIThread;
import kst.ksti.chauffeur.databinding.ActivityMainBinding;
import kst.ksti.chauffeur.fragment.BreakTimeFragment;
import kst.ksti.chauffeur.fragment.ChauffeurEvalFragment;
import kst.ksti.chauffeur.fragment.CustomerLoadFragment;
import kst.ksti.chauffeur.fragment.DestArrivedFragment;
import kst.ksti.chauffeur.fragment.DestSearchFragment;
import kst.ksti.chauffeur.fragment.DriveHistoryFragment;
import kst.ksti.chauffeur.fragment.DriveScheduleFragment;
import kst.ksti.chauffeur.fragment.DrivingFragment;
import kst.ksti.chauffeur.fragment.MainWorkFragment;
import kst.ksti.chauffeur.fragment.MoveGarageFragment;
import kst.ksti.chauffeur.fragment.NearByDriveFragment;
import kst.ksti.chauffeur.fragment.OrgArrivedFragment;
import kst.ksti.chauffeur.fragment.RoadsaleCompleteFragment;
import kst.ksti.chauffeur.fragment.RoadsaleStartFragment;
import kst.ksti.chauffeur.fragment.StatisticsFragment;
import kst.ksti.chauffeur.fragment.WebViewFragment;
import kst.ksti.chauffeur.listner.ChangeStatusInterface;
import kst.ksti.chauffeur.listner.ReverseGeocodingInterface;
import kst.ksti.chauffeur.model.EvaluationSum;
import kst.ksti.chauffeur.net.DataInterface;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.service.MyLocationService;
import kst.ksti.chauffeur.utility.BackPressCloseHandler;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.MacaronCustomDialog;
import kst.ksti.chauffeur.utility.OnSingleClickListener;
import kst.ksti.chauffeur.utility.PrefUtil;
import kst.ksti.chauffeur.utility.Util;


public class MainActivity extends BaseActivity<ActivityMainBinding> implements NavigationView.OnNavigationItemSelectedListener {

    private MacaronCustomDialog dialog;
    private BroadcastReceiver mReceiver = null;
    private BackPressCloseHandler backPressCloseHandler;

    private NavigationView navigationView;

    private boolean isCarAccident = false;  // 사고접수시 로그아웃처리할 때 로그인화면으로 이동못하게 막음.
    private boolean isNewIntentAllocDetailFlag = false;  // 예약상세 화면에서 출발버튼 클릭시 onNewIntent()에서 true로 변경하여 메인화면의 onResume()안에서 로딩바를 못없애게 막음.

    private long alloc_idx = -1;
    private String a_title;
    private boolean a_flags;

    private TMapView mMapView;
    public TMapTapi tMapTapi;

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

        setBind(R.layout.activity_main);

        // 앱이 크래시 나서 죽을때 해당 크래시쓰레드 체크
        Thread.setDefaultUncaughtExceptionHandler(((MacaronApp)getApplication()).getUncaughtExceptionHandler(MainActivity.this));

        // 백버튼 세팅
        backPressCloseHandler = new BackPressCloseHandler(this);

        // 탑 화면 세팅
        MacaronApp.topActivity = Global.TOP_SCREEN.MAIN;

        // 쇼퍼정보 체크
        if(MacaronApp.chauffeur != null) {
            initMainAction();

        } else {
            Toast.makeText(MainActivity.this, "기사정보가 없습니다. 앱을 다시 실행해 주세요.", Toast.LENGTH_LONG).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 1500);
        }
    }

    /**
     * 메인화면 initialize
     */
    private void initMainAction() {
        drawer = getBind().drawerLayout;
        drawer.addDrawerListener(new ActionBarDrawerToggle(MainActivity.this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getEvaluationSum();
            }
        });

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initNaviDrawerHeader();

        setLoadingBarLayout(getBind().loadingBarLayout);
        setProgressBar(getBind().progressBar);

        Bundle bundle = null;
        if(alloc_idx > 0 && !TextUtils.isEmpty(a_title)) {
            bundle = new Bundle();
            bundle.putLong("a_detail", alloc_idx);
            bundle.putString("a_title", a_title);
            bundle.putBoolean("a_flags", a_flags);
        }

        GoNativeScreenReplace(new MainWorkFragment(), bundle, 0);
        startLocationService();

        initTmapTapi();

        // 수락배차 리스트 초기화
        MacaronApp.allocSelectList.clear();
    }

    public void initTmapTapi() {
        mMapView = new TMapView(this);
        mMapView.setSKTMapApiKey(Global.TMAP_APIKEY);

        tMapTapi = new TMapTapi(this);
        tMapTapi.setSKTMapAuthentication(Global.TMAP_APIKEY);
    }

    /**
     * 네비게이션뷰 init (햄버거 메뉴, 좌측 메뉴)
     */
    private void initNaviDrawerHeader() {
        try {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) getBind().navView.getLayoutParams();
            params.width = metrics.widthPixels;
            getBind().navView.setLayoutParams(params);

            TextView name = getBind().navView.findViewById(R.id.name);
            name.setText(MacaronApp.chauffeur.name);

            TextView phoneNo = getBind().navView.findViewById(R.id.phoneNo);
            phoneNo.setText(MacaronApp.chauffeur.mobileNo);

            if (MacaronApp.chauffeur.imgUrl != null) {
                int imageSize = (int) Util.convertDpToPixel(108.3f, MainActivity.this);

                ImageView imgProfile = getBind().navView.findViewById(R.id.img_profile);
                Drawable photo;

                if(android.os.Build.VERSION.SDK_INT >= 21){
                    photo = getResources().getDrawable(R.drawable.photo_default, getTheme());
                } else {
                    photo = getResources().getDrawable(R.drawable.photo_default);
                }

                Glide.with(getApplicationContext())
                        .load(MacaronApp.chauffeur.imgUrl)
                        .apply(new RequestOptions().override(imageSize, imageSize).circleCrop().placeholder(photo).error(photo))
                        .into(imgProfile);
            }

            getBind().contents.imgProfile.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });

            LinearLayout coverLike = getBind().navView.findViewById(R.id.cover_like);
            coverLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playLoadingViewAnimation();
                    Bundle bundle = new Bundle();
                    bundle.putString("likeYn", "Y");
                    bundle.putString("arrowBack", "y");
                    GoNativeScreenReplaceAdd(new ChauffeurEvalFragment(), bundle, 0);
                    drawer.closeDrawer(GravityCompat.START);
                }
            });

            LinearLayout coverDislike = getBind().navView.findViewById(R.id.cover_dislike);
            coverDislike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playLoadingViewAnimation();
                    Bundle bundle = new Bundle();
                    bundle.putString("likeYn", "N");
                    bundle.putString("arrowBack", "y");
                    GoNativeScreenReplaceAdd(new ChauffeurEvalFragment(), bundle, 0);
                    drawer.closeDrawer(GravityCompat.START);
                }
            });

            LinearLayout leftResvDateTime = getBind().navView.findViewById(R.id.leftResvDateTime);
            leftResvDateTime.setOnClickListener(leftSingleClickListener);

            LinearLayout leftDriveHistory = getBind().navView.findViewById(R.id.leftDriveHistory);
            leftDriveHistory.setOnClickListener(leftSingleClickListener);

            // 정산내역 버튼
            LinearLayout leftBalanceAccounts = getBind().navView.findViewById(R.id.leftBalanceAccounts);
            leftBalanceAccounts.setOnClickListener(leftSingleClickListener);

            // 사고접수 버튼
            LinearLayout leftAccident = getBind().navView.findViewById(R.id.leftAccident);
            leftAccident.setOnClickListener(leftSingleClickListener);

            // 차고지이동 버튼
            LinearLayout leftRetire = getBind().navView.findViewById(R.id.leftRetire);
            leftRetire.setOnClickListener(leftSingleClickListener);

            // 좌측메뉴 퇴근하기 버튼
            LinearLayout logout = getBind().navView.findViewById(R.id.leftLogout);
            logout.setOnClickListener(leftSingleClickListener);

            ImageView leftClose = getBind().navView.findViewById(R.id.leftClose);
            leftClose.setOnClickListener(leftSingleClickListener);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 좌측메뉴 클릭리스너
     */
    private OnSingleClickListener leftSingleClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            switch (v.getId()) {
                case R.id.leftResvDateTime: {
                    playLoadingViewAnimation();
                    Bundle bundle = new Bundle();
                    bundle.putString("arrowBack", "y");
                    GoNativeScreenReplaceAdd(new DriveScheduleFragment(), bundle, 0);
                    break;
                }

                case R.id.leftDriveHistory: {
                    playLoadingViewAnimation();
                    Bundle bundle = new Bundle();
                    bundle.putString("arrowBack", "y");
                    GoNativeScreenReplaceAdd(new DriveHistoryFragment(), bundle, 0);
                    break;
                }

                case R.id.leftBalanceAccounts: {
                    String url = Global.getBALANCEACCOUNTSUrl() + MacaronApp.chauffeur.accessToken + Global.getBALANCE_ACCOUNTS_PARAM() + MacaronApp.chauffeur.chauffeurIdx;
                    Bundle bundle = new Bundle();
                    bundle.putString("title", "정산내역");
                    bundle.putString("url", url);
                    GoNativeScreenReplaceAdd(new WebViewFragment(), bundle, 0);
                    break;
                }

                case R.id.leftAccident:     // 사고접수 버튼
                    showCarAccidentDialog();
                    break;

                case R.id.leftRetire:       // 차고지로 이동 버튼 v1.0.8
                    if(dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }

                    dialog = new MacaronCustomDialog(MainActivity.this, getString(R.string.txt_move_to_garage), getString(R.string.popup_retire), "네", "아니오", leftGarageListener, rightListener, true);
                    dialog.show();

                    break;

                case R.id.leftLogout:
                    confirmLeave();
                    break;

                case R.id.leftClose:
                    drawer.closeDrawer(GravityCompat.START);
                    break;
            }
        }
    };

    /**
     * 좌측메뉴 차고지이동 다이얼로그 왼쪽 클릭리스너
     */
    private View.OnClickListener leftGarageListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dialog.dismiss();

            Fragment fragment = getCurrentFragment();
            if (fragment instanceof DrivingFragment) {
                drawer.closeDrawer(GravityCompat.START);

                chauffeurStatus = AppDef.ChauffeurStatus.RETIRE;
                HashMap<String, Object> params = new HashMap<>();
                params.put("chauffeurStatusCat", AppDef.ChauffeurStatus.RETIRE.toString());
                sendChauffeurStatusAndGoNextScreen(params, changeStatusGarageInterface);
            }
        }
    };

    /**
     * 쇼퍼 상태변경 콜백
     */
    private ChangeStatusInterface changeStatusGarageInterface = new ChangeStatusInterface() {
        @Override
        public void onSuccess(ResponseData<Object> response) {
            switch (chauffeurStatus) {
                case RETIRE:
                    GoNativeScreenReplaceAdd(new MoveGarageFragment(), null, 1);
                    break;
                default:
                    cancelLoadingViewAnimation();
            }
        }

        @Override
        public void onErrorCode(ResponseData<Object> response) {
            dialog = new MacaronCustomDialog(MainActivity.this, null, response.getError(), "확인", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    GoNativeScreenReplaceAdd(new DrivingFragment(), null, 2);
                }
            });

            try {
                dialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }

            cancelLoadingViewAnimation();
        }

        @Override
        public void onError() {
            cancelLoadingViewAnimation();
        }

        @Override
        public void onFailed(Throwable t) {
            cancelLoadingViewAnimation();
        }
    };


    /**
     * 좌측메뉴 퇴근버튼 클릭시 액션
     */
    private void confirmLeave() {
        String title, message;
        boolean check = true;  // 출근

        Fragment fragment = getCurrentFragment();
        if (fragment instanceof MainWorkFragment) {
            check = false;
        }

        if(!check) {
            title = "종료 안내";
            message = "로그아웃 하시겠습니까?";

        } else {
            title = "퇴근 안내";

            if(MacaronApp.scheduleCount > 0) {
                // 예약이 있을때
                message = "<font color='#ff1c74'>잔여 예약</font>이 남아있습니다.<br>정말 퇴근하시겠습니까?";
            } else {
                // 예약이 없을때
                message = "퇴근하시겠습니까?<br>오늘도 수고하셨습니다.";
            }
        }


        dialog = new MacaronCustomDialog(
                this, title, Html.fromHtml(message), "네", "아니오", leftListener, rightListener, true);

        dialog.show();
    }

    /**
     * 좌측메뉴 퇴근버튼 다이얼로그 오른쪽버튼 클릭리스너
     */
    private View.OnClickListener rightListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dialog.dismiss();
        }
    };

    /**
     * 좌측메뉴 퇴근버튼 다이얼로그 왼쪽 클릭리스너
     */
    private View.OnClickListener leftListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            drawer.closeDrawer(GravityCompat.START);
            dialog.dismiss();
            playLoadingViewAnimation();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
//                    setLogout();
                    Fragment fragment = getCurrentFragment();
                    if (fragment instanceof MainWorkFragment) {
                        // 새로운 상태
//                        Toast.makeText(MainActivity.this, "새로운 상태", Toast.LENGTH_SHORT).show();
                        setEXIT(true, AppDef.ChauffeurStatus.DISCONNECT);

                    } else {
                        setLogout();
                    }
                }
            }, 300);
        }
    };

    /**
     * 좋아요, 싫어요 카운트 서버통신
     */
    private void getEvaluationSum() {
        DataInterface.getInstance().getEvaluationSum(MainActivity.this, new DataInterface.ResponseCallback<ResponseData<EvaluationSum>>() {
            @Override
            public void onSuccess(ResponseData<EvaluationSum> response) {
                if ("S000".equals(response.getResultCode())) {
                    Logger.d("좋아요,싫어요 카운트 받기 성공");

                    try {
                        for (int i = 0; i < response.getList().size(); i++) {
                            if (response.getList().get(i).likeYn.equals("likeY")) {
                                TextView txtLike = getBind().navView.findViewById(R.id.txt_like);
                                txtLike.setText(String.valueOf(response.getList().get(i).likeYnCount));
                            } else {
                                TextView txtDislike = getBind().navView.findViewById(R.id.txt_dislike);
                                txtDislike.setText(String.valueOf(response.getList().get(i).likeYnCount));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(ResponseData<EvaluationSum> response) {
            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        drawer = findViewById(R.id.drawer_layout);

        Fragment fragment = getCurrentFragment();

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (fragment instanceof DrivingFragment) {           // 메인화면에서 뒤로가기 버튼 누른 경우
            confirmLeave();
        } else if (fragment instanceof WebViewFragment) {           // 웹뷰 화면에서 뒤로가기 버튼 누른 경우
            WebViewFragment webView = (WebViewFragment)getCurrentFragment();
            boolean goback = webView.getWebView().canGoBack();
            if (goback) {
                webView.getWebView().goBack();
            }
            else {
                webView.BackProcess();
            }
        } else if (fragment instanceof BreakTimeFragment) { // 휴식중 화면에서 뒤로가기 버튼 누른 경우
            dialog = new MacaronCustomDialog(MainActivity.this, getString(R.string.txt_breaktime), "휴식을 종료하시겠습니까?", getString(R.string.txt_confirm),
                    new View.OnClickListener() {
                        public void onClick(View v) {
                            dialog.dismiss();
                            BreakTimeFragment breakTimeFr = (BreakTimeFragment)getCurrentFragment();
                            breakTimeFr.backProcess();
                        }
                    },
                    true,
                    true);

            dialog.show();
        } else if (fragment instanceof ChauffeurEvalFragment) {     // 좋아요/싫어요 화면에서 뒤로가기 버튼 누른 경우
            ChauffeurEvalFragment fr = (ChauffeurEvalFragment)getCurrentFragment();
            fr.BackProcess();
        } else if (fragment instanceof DestSearchFragment) {        // 일반운행 목적지 선택 화면에서 뒤로가기 버튼 누른 경우
            DestSearchFragment fr = (DestSearchFragment)getCurrentFragment();
            fr.backProcess();
        } else if (fragment instanceof RoadsaleStartFragment) {     // 일반운행 시작 화면에서 뒤로가기 버튼 누른 경우
            RoadsaleStartFragment fr = (RoadsaleStartFragment)getCurrentFragment();
            fr.BackProcess();
        } else if (fragment instanceof DriveScheduleFragment) {     // 예약목록 화면에서 뒤로가기 버튼 누른 경우
            DriveScheduleFragment fr = (DriveScheduleFragment)getCurrentFragment();
            fr.BackProcess();
        } else if (fragment instanceof DriveHistoryFragment) {      // 운행이력 화면에서 뒤로가기 버튼 누른 경우
            DriveHistoryFragment fr = (DriveHistoryFragment)getCurrentFragment();
            fr.BackProcess();
        } else if (fragment instanceof MoveGarageFragment) {         // 차고지 이동 화면에서 뒤로가기 버튼 누른 경우
            MoveGarageFragment fr = (MoveGarageFragment)getCurrentFragment();
            fr.backProcess();
        } else if (fragment instanceof StatisticsFragment) {         // 운행통계 화면에서 뒤로가기 버튼 누른 경우
            StatisticsFragment fr = (StatisticsFragment)getCurrentFragment();
            fr.BackProcess();
        } else {
            if(! PrefUtil.getBackKeyCheck(MainActivity.this)) {
                backPressCloseHandler.onBackPressed();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(getCurrentFragment() instanceof StatisticsFragment)
        {
            WebView webView = (WebView) findViewById(R.id.webView);

            if(webView != null) {
                if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
                    webView.goBack();
                    return true;
                }
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_schedule_list:
                break;
            case R.id.nav_accident_reg:
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * 사고접수 다이얼로그
     */
    private void showCarAccidentDialog() {
        String msg = getResources().getString(R.string.emergency_call);
        dialog = new MacaronCustomDialog(MainActivity.this, "사고접수", msg, "전화하기",
                new View.OnClickListener() {
                    public void onClick(View v) {
                        isCarAccident = true;
                        informCarAccidentStatus();
                        Util.callCompany(MainActivity.this);
                        dialog.dismiss();
                    }
                },
                true,
                true);

        dialog.show();
    }

    /**
     * 사고접수 서버통신
     */
    private void informCarAccidentStatus() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("carStatusCat", AppDef.ChauffeurStatus.ACCIDENT.toString());

        DataInterface.getInstance().informCarAccident(MainActivity.this, params, new DataInterface.ResponseCallback<ResponseData<Object>>() {
            @Override
            public void onSuccess(ResponseData<Object> response) {
                if ("S000".equals(response.getResultCode())) {
                    UIThread.executeInUIThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "자동차 사고 신고접수 성공", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onError(ResponseData<Object> response) {
                Logger.d("에러발생:" + response.getError());
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        });
    }

    /**
     * 메뉴 열기 (햄버거 버튼 클릭)
     */
    public void OpenMenuMap() {
        if (mNativeFragment != null) {
            UIThread.executeInUIThread(new Runnable() {
                @Override
                public void run() {
                    drawer.openDrawer(Gravity.LEFT);
                }
            });
        } else {
            return;
        }
    }

    /**
     * 메뉴 닫기
     *
     * @return true: 닫기 성공, false: 실패 (or 무시)
     */
    protected boolean HideMenuMap() {
        if (mNativeFragment != null) {
            UIThread.executeInUIThread(new Runnable() {
                @Override
                public void run() {
                    drawer.closeDrawer(GravityCompat.END);
                }
            });
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_OK) return;

//        switch (requestCode) {
//            case ORG_ARRIVED:
//                GoNativeScreenReplaceAdd(new OrgArrivedFragment(), null, 0);
//                break;
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        onResumeAction();

        PrefUtil.setActivityStatus(getApplicationContext(), AppDef.ActivityStatus.MAIN.toString());
    }

    /**
     * 현재 보여지고있는 프레그먼트 반환
     * @return current fragment
     */
    public Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.vw_NativeContent);
    }

    /**
     * 메인액티비티 onResume() 액션
     */
    private void onResumeAction() {
        try {
            Fragment f = getCurrentFragment();
            if (f instanceof DrivingFragment) {
                ((DrivingFragment)f).refreshList(Global.FIRST_PAGE);
                ((DrivingFragment)f).setScheduleTimer();

            } else if (f instanceof OrgArrivedFragment) {
                if(!isNewIntentAllocDetailFlag) {
                    cancelLoadingViewAnimation();
                }
                isNewIntentAllocDetailFlag = false;

            } else if (f instanceof CustomerLoadFragment) {
                cancelLoadingViewAnimation();
            } else if (f instanceof DestArrivedFragment) {
                cancelLoadingViewAnimation();
            } else if (f instanceof DriveScheduleFragment) {
                cancelLoadingViewAnimation();
            } else if (f instanceof RoadsaleStartFragment) {        // 일반영업 시작 화면
                cancelLoadingViewAnimation();
            } else if (f instanceof RoadsaleCompleteFragment) {     // 일반영업 종료 화면
                cancelLoadingViewAnimation();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        onPauseAction();
    }

    /**
     * 메인액티비티 onPause() 액션
     */
    private void onPauseAction() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.vw_NativeContent);
        if (f instanceof DrivingFragment) {
            ((DrivingFragment)f).cancelScheduleTimer();

        }
    }

    @Override
    protected void onDestroy() {
        if(MacaronApp.chauffeur != null) {
            setEXIT(true, AppDef.ChauffeurStatus.EXIT);
        }

//        try {
//            unregisterReceiver(mReceiver);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        stopService(new Intent(getApplicationContext(), MyLocationService.class));

        Logger.d("## Main onDestroy()");

        super.onDestroy();
    }

    private boolean showLogoutToastMsgFlag = true;  // 퇴근시 로그인화면에서 "퇴근하셨습니다" 토스트 보여줄지 말지
    private AppDef.ChauffeurStatus chauffeurStatus;

    /**
     * 강제종료(EXIT) 호출시작
     */
    public void setEXIT(boolean logout, AppDef.ChauffeurStatus status) {
        showLogoutToastMsgFlag = logout;
        chauffeurStatus = status;
        // 강제종료
        HashMap<String, Object> params = new HashMap<>();
        params.put("chauffeurStatusCat", status.toString());
        sendChauffeurStatusAndGoNextScreen(params, changeStatusInterface);
    }

    /**
     * 강제종료(EXIT) 호출 콜백리스너
     */
    private ChangeStatusInterface changeStatusInterface = new ChangeStatusInterface() {
        @Override
        public void onSuccess(ResponseData<Object> response) {
            MacaronApp.chauffeur = null;
            MacaronApp.nearByDriveStartCheck = false;
            PrefUtil.setBackKeyCheck(MainActivity.this, false);

            cancelLoadingViewAnimation();

            String msg;
            switch (chauffeurStatus) {
                case EXIT:
                    if(MacaronApp.workStatus == AppDef.ActivityStatus.LOGIN)
                        msg = "로그아웃되었습니다.";
                    else
                        msg = "퇴근하셨습니다.";
                    break;
                default:
                    msg = "로그아웃되었습니다.";
                    break;
            }

            logoutNextAction(showLogoutToastMsgFlag, msg);
            PrefUtil.setExitStatus(MainActivity.this, "");
        }

        @Override
        public void onErrorCode(ResponseData<Object> response) {
            cancelLoadingViewAnimation();
        }

        @Override
        public void onError() {
            cancelLoadingViewAnimation();
        }

        @Override
        public void onFailed(Throwable t) {
            cancelLoadingViewAnimation();
        }
    };

    private boolean isLogoutBtnClick = false;

    /**
     * 퇴근하기 호출시작
     */
    public void setLogout() {
        if(!isLogoutBtnClick) {
            isLogoutBtnClick = true;

            Util.getLocationInfomationParams(this, reverseGeocodingInterface, null, null);
        }
    }

    /**
     * Tmap에서 위치정보 받아오는 콜백리스너
     */
    private ReverseGeocodingInterface reverseGeocodingInterface = new ReverseGeocodingInterface() {
        @Override
        public void onSuccess(HashMap<String, Object> params, ChangeStatusInterface changeStatusInterface) {
            callLogout(params);
        }

        @Override
        public void onError(HashMap<String, Object> params, ChangeStatusInterface changeStatusInterface, final String errorMsg) {
            callLogout(params);
        }

        @Override
        public void onGpsError(final HashMap<String, Object> params, ChangeStatusInterface changeStatusInterface) {
            dialog = new MacaronCustomDialog(MainActivity.this, null, getString(R.string.gps_error_message), getString(R.string.txt_confirm),
                    new View.OnClickListener() {
                        public void onClick(View v) {
                            dialog.dismiss();
                            callLogout(params);
                        }
                    }, true, false);
            dialog.show();
        }
    };

    /**
     * 위치정보 받아오고나서 서버로 퇴근하기 호출
     * @param params 서버로 올릴 파라미터
     */
    private void callLogout(HashMap<String, Object> params) {
        DataInterface.getInstance().logout(MainActivity.this, params, new DataInterface.ResponseCallback<ResponseData<Object>>() {
            @Override
            public void onSuccess(ResponseData<Object> response) {
                if (response.getResultCode().equals("S000")) {
                    Logger.d("로그아웃 성공");
                    MacaronApp.chauffeur = null;
                    MacaronApp.nearByDriveStartCheck = false;
                    PrefUtil.setBackKeyCheck(MainActivity.this, false);
                    PrefUtil.setStartTime(MainActivity.this, 0);

                    logoutNextAction(true, "퇴근하셨습니다.");
                }
                cancelLoadingViewAnimation();
            }

            @Override
            public void onError(ResponseData<Object> response) {
                cancelLoadingViewAnimation();
                isLogoutBtnClick = false;
            }

            @Override
            public void onFailure(Throwable t) {
                cancelLoadingViewAnimation();
                isLogoutBtnClick = false;
            }
        });
    }

    /**
     * 로그아웃 성공후 다음 액션
     * @param logout 로그인화면에서 Toast 보여줄지 말지
     */
    private void logoutNextAction(boolean logout, String msg) {
        PrefUtil.setActivityStatus(getApplicationContext(), AppDef.ActivityStatus.NONE.toString());

        if(!isCarAccident) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.putExtra(Global.INTENT_EXTRA_NAME.LOG_OUT, logout);
            intent.putExtra(Global.INTENT_EXTRA_NAME.LOG_OUT_MSG, msg);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);

        } else {
            finish();
            overridePendingTransition(0, 0);
        }
    }

    /**
     * 운행임박 폴링시 화면이동시키는 리시브함수
     */
//    private void registerReceiver() {
//        /** 1. intent filter를 만든다
//         *  2. intent filter에 action을 추가한다.
//         *  3. BroadCastReceiver를 익명클래스로 구현한다.
//         *  4. intent filter와 BroadCastReceiver를 등록한다.
//         * */
//        if (mReceiver != null) return;
//
//        final IntentFilter theFilter = new IntentFilter();
//        theFilter.addAction(Global.DEEP_LINK.TICKET);
//        theFilter.addAction(Global.DEEP_LINK.NEARBY);
//
//        this.mReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                if (intent.getAction().equals(Global.DEEP_LINK.NEARBY)) {
//                   // Toast.makeText(context, "recevied Data : " + receviedData, Toast.LENGTH_SHORT).show();
//                  //  Intent intent1 = getIntent();
//                  if(intent != null) {
//                      Bundle bundle = new Bundle();
//                      bundle.putString("idx", intent.getStringExtra("idx"));
//                      GoNativeScreenReplaceAdd(new NearByDriveFragment(), bundle, 0);
//                  }
//                }else if(intent.getAction().equals(Global.DEEP_LINK.TICKET)){
//                    Bundle bundle = new Bundle();
//                    bundle.putString("idx", intent.getStringExtra("reservationId"));
//
//                    Intent intent_detail = new Intent(MainActivity.this, AllocDetailActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    intent.putExtra("allocActivityType", AllocDetailActivity.ACTIVITY_TYPE_DEFAULT);
//                    startActivity(intent_detail);
//                }
//            }
//        };
//        this.registerReceiver(this.mReceiver, theFilter);
//    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String type = intent.getStringExtra("a_type");

        if(!TextUtils.isEmpty(type)) {
            Logger.i("## type = " + type);

            switch(type) {
                case "nearby":
                    long idx = intent.getLongExtra("allocationIdx", -1);
                    int count = intent.getIntExtra("scheduleCount", -1);
                    long a_date = intent.getLongExtra("a_date", -1);

                    if (idx >= 0) {
                        // 운행임박으로 이동
                        Bundle bundle = new Bundle();
                        bundle.putString("idx", idx + "");
                        bundle.putString("scheduleCount", count + "");
                        bundle.putLong("a_date", a_date);
                        GoNativeScreenReplaceAdd(new NearByDriveFragment(), bundle, 0);
                    }
                    break;

                case "allocDetail":
                    isNewIntentAllocDetailFlag = true;
                    playLoadingViewAnimation();
                    GoNativeScreenReplaceAdd(new OrgArrivedFragment(), null, 0);
                    break;
            }
        }

    }

}
