package kst.ksti.chauffeur.activity;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.disposables.CompositeDisposable;
import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.activity.signup.CorporationRegisterActivity;
import kst.ksti.chauffeur.activity.signup.SignupAdditionalInformation;
import kst.ksti.chauffeur.activity.signup.SignupBasicInfomation;
import kst.ksti.chauffeur.activity.signup.SignupFailInformation;
import kst.ksti.chauffeur.activity.signup.SignupPictureRegistration;
import kst.ksti.chauffeur.activity.signup.SignupRequestInformation;
import kst.ksti.chauffeur.activity.signup.TermsAgreeActivity;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.common.Global;
import kst.ksti.chauffeur.common.UIThread;
import kst.ksti.chauffeur.fragment.DrivingFragment;
import kst.ksti.chauffeur.fragment.NativeFragment;
import kst.ksti.chauffeur.listner.ChangeStatusInterface;
import kst.ksti.chauffeur.listner.ResultInoffice;
import kst.ksti.chauffeur.listner.ReverseGeocodingInterface;
import kst.ksti.chauffeur.listner.ReverseGeocodingInterfaceInoffice;
import kst.ksti.chauffeur.model.CompanyStatusVO;
import kst.ksti.chauffeur.model.Inoffice;
import kst.ksti.chauffeur.model.signup.ChauffeurStatusVO;
import kst.ksti.chauffeur.model.signup.RegistChauffeurInfoVO;
import kst.ksti.chauffeur.net.DataInterface;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.service.MyLocationService;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.MacaronCustomDialog;
import kst.ksti.chauffeur.utility.OnSingleClickListener;
import kst.ksti.chauffeur.utility.PrefUtil;
import kst.ksti.chauffeur.utility.Util;

public class BaseActivity <T extends ViewDataBinding> extends AppCompatActivity {

    public static final int REQUEST_GPS = 6655;

    private T mVd;
    protected NativeFragment mNativeFragment;
    protected DrawerLayout mDlHomeView ;
    protected String TAG = getClass().getSimpleName();

    protected DrawerLayout drawer;
    private ProgressBar progressBar;
    private RelativeLayout loadingBarLayout;

    private MacaronCustomDialog baseGpsDialog;
    private MacaronCustomDialog tmapNotInstallDialog;
    private MacaronCustomDialog gpsErrorDialog;

    protected CompositeDisposable baseDisposable = new CompositeDisposable();

    public interface StatusCallback<T> {
        void onStatusSuccess(T companyVO);
        void onStatusNotExist();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 앱 실행 하는 동안 화면 안꺼지게 하기
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!(this instanceof AllocSelectActivity) && !(this instanceof DriveScheduleActivity) && MacaronApp.topActivity.equals(Global.TOP_SCREEN.ALLOCSELECT)) {
            Intent intent = new Intent(this, AllocSelectActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        baseDisposable.clear();
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
        this.progressBar.setIndeterminate(true);
        this.progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#02d1d9"), PorterDuff.Mode.SRC_IN);
    }

    public ProgressBar getProgressBar() {
        if(progressBar != null)
            return progressBar;
        else
            return null;
    }

    public void playLoadingViewAnimation() {
        if(getProgressBar() != null && getLoadingBarLayout() != null) {
            getLoadingBarLayout().setVisibility(View.VISIBLE);
            Util.lockViewTouch(this);
        }
    }

    public void cancelLoadingViewAnimation() {
        if(getProgressBar() != null && getLoadingBarLayout() != null) {
            getLoadingBarLayout().setVisibility(View.GONE);
            Util.unLockViewTouch(this);
        }
    }

    public void setLoadingBarLayout(RelativeLayout loadingBarLayout) {
        this.loadingBarLayout = loadingBarLayout;
    }

    public RelativeLayout getLoadingBarLayout() {
        if(loadingBarLayout != null)
            return loadingBarLayout;
        else
            return null;
    }

    protected void setBind(@LayoutRes int layId){
        if(mVd == null){
            mVd = DataBindingUtil.setContentView(this, layId);
        }
    }

    public T getBind() {
        return mVd;
    }


    public DrawerLayout getDrawerLayout() {
        if(drawer != null) return drawer;
        else return null;
    }

    /**
     * 변경할 프레그먼트 호출.
     * 백스택에 저장하지 않는다.
     * @param fragment 호출할 프레그먼트
     * @param bundle 넘겨줄 정보
     */
    public void GoNativeScreenReplace(NativeFragment fragment, Bundle bundle, int direction){
        if (fragment == null) {
            return;
        }

        mNativeFragment = fragment;
        if (bundle != null) {
            mNativeFragment.setArguments(bundle);
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        switch (direction) {
            case 0:
                transaction.setCustomAnimations(0, 0, R.anim.pull_in_left, R.anim.push_out_right);
                break;
            case 1:
                transaction.setCustomAnimations(R.anim.pull_in_right, R.anim.push_out_left, R.anim.pull_in_left, R.anim.push_out_right);
                break;
            case 2:
                transaction.setCustomAnimations(R.anim.pull_in_left, R.anim.push_out_right, R.anim.pull_in_right, R.anim.push_out_left);
                break;
            default:
                break;
        }

        transaction.replace(R.id.vw_NativeContent, mNativeFragment).commitAllowingStateLoss();
    }

    /**
     * 변경할 프레그먼트 호출.
     * 백스택에 저장한다.
     * @param fragment 호출할 프레그먼트
     * @param bundle 넘겨줄 정보
     * @param direction 1: 오른쪽 / 2: 왼쪽 / default: 동작없음
     */
    public void GoNativeScreenReplaceAdd(NativeFragment fragment, Bundle bundle, int direction) {
        if (fragment == null) {
            return;
        }

        mNativeFragment = fragment;
        if (bundle != null) {
            mNativeFragment.setArguments(bundle);
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        switch (direction) {
            case 0:
                transaction.setCustomAnimations(0, 0, R.anim.pull_in_left, R.anim.push_out_right);
                break;
            case 1:
                transaction.setCustomAnimations(R.anim.pull_in_right, R.anim.push_out_left, R.anim.pull_in_left, R.anim.push_out_right);
                break;
            case 2:
                transaction.setCustomAnimations(R.anim.pull_in_left, R.anim.push_out_right, R.anim.pull_in_right, R.anim.push_out_left);
                break;
            default:
                break;
        }
        transaction.replace(R.id.vw_NativeContent, mNativeFragment).addToBackStack(null).commitAllowingStateLoss();
    }


    /**
     * 프레그먼트 백스택 관리
     */
    public void GoNativeBackStack() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (fragmentManager.getBackStackEntryCount() != 0) {
            fragmentManager.popBackStack();

        } else {
            GoNativeScreenReplaceAdd(new DrivingFragment(), null, 2);
        }
    }

    /**
     * 전화하기
     */
    public void Call(String callNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + callNumber));
        startActivity(intent);
    }

    /**
     * 메뉴 View 초기화
     */
    protected void SetMenuView() {
        mDlHomeView = findViewById(R.id.drawer_layout);

        mDlHomeView.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mDlHomeView.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if (mNativeFragment != null) {
                  //  mNativeFragment.checkMyMenuPosition();
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                mDlHomeView.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                mDlHomeView.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
    }


    //============================================================================================
    /**
     * 쇼퍼상태 변경
     * @param changeStatusInterface 콜백
     */
    public void sendChauffeurStatusAndGoNextScreen(final HashMap<String, Object> params, final ChangeStatusInterface changeStatusInterface) {
        Util.getLocationInfomationParams(BaseActivity.this, reverseGeocodingInterfaceChauffeur, params, changeStatusInterface);
    }

    /**
     * Tmap에서 위치정보 받아오는 콜백리스너
     */
    private ReverseGeocodingInterface reverseGeocodingInterfaceChauffeur = new ReverseGeocodingInterface() {
        @Override
        public void onSuccess(HashMap<String, Object> params, ChangeStatusInterface changeStatusInterface) {
            sendChauffeurStatus(params, changeStatusInterface);
        }

        @Override
        public void onError(HashMap<String, Object> params, ChangeStatusInterface changeStatusInterface, final String errorMsg) {
            UIThread.executeInUIThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(BaseActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            });

            sendChauffeurStatus(params, changeStatusInterface);
        }

        @Override
        public void onGpsError(final HashMap<String, Object> params, final ChangeStatusInterface changeStatusInterface) {
            String status = String.valueOf(params.get("chauffeurStatusCat"));

            if(!TextUtils.isEmpty(status) && status.equals("EXIT")) {
                sendChauffeurStatus(params, changeStatusInterface);

            } else {
                gpsErrorDialog = new MacaronCustomDialog(BaseActivity.this, null, getString(R.string.gps_error_message), getString(R.string.txt_confirm),
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                gpsErrorDialog.dismiss();
                                sendChauffeurStatus(params, changeStatusInterface);
                            }
                        }, true, false);
                gpsErrorDialog.show();
            }
        }
    };

    /**
     * 받아온 위치정보로 쇼퍼상태 변경 서버통신
     */
    private void sendChauffeurStatus(HashMap<String, Object> params, final ChangeStatusInterface changeStatusInterface) {
        DataInterface.getInstance().sendChauffeurStatus(BaseActivity.this, params, new DataInterface.ResponseCallback<ResponseData<Object>>() {
            @Override
            public void onSuccess(ResponseData<Object> response) {
                if ("S000".equals(response.getResultCode())) {
                    if(changeStatusInterface != null) changeStatusInterface.onSuccess(response);
                } else {
                    if(changeStatusInterface != null) changeStatusInterface.onErrorCode(response);
                }
            }

            @Override
            public void onError(ResponseData<Object> response) {
                if(changeStatusInterface != null) changeStatusInterface.onError();
            }

            @Override
            public void onFailure(Throwable t) {
                if(changeStatusInterface != null) changeStatusInterface.onFailed(t);
            }
        });
    }




    //============================================================================================
    /**
     * 배차상태 변경
     * @param allocationIdx 해당예약 idx
     * @param allocStatus 변경할 상태정보
     * @param changeStatusInterface 콜백
     */
    public  void sendAllocStatusAndGoNextScreen(long allocationIdx, final AppDef.AllocationStatus allocStatus, final ChangeStatusInterface changeStatusInterface) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("allocationIdx", allocationIdx);
        params.put("allocationStatus", allocStatus.toString());

        Util.getLocationInfomationParams(BaseActivity.this, reverseGeocodingInterfaceCar, params, changeStatusInterface);
    }

    /**
     * Tmap에서 위치정보 받아오는 콜백리스너
     */
    private ReverseGeocodingInterface reverseGeocodingInterfaceCar = new ReverseGeocodingInterface() {
        @Override
        public void onSuccess(HashMap<String, Object> params, ChangeStatusInterface changeStatusInterface) {
            changeAllocationStatus(params, changeStatusInterface);
        }

        @Override
        public void onError(HashMap<String, Object> params, ChangeStatusInterface changeStatusInterface, final String errorMsg) {
            UIThread.executeInUIThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(BaseActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            });

            changeAllocationStatus(params, changeStatusInterface);
        }

        @Override
        public void onGpsError(final HashMap<String, Object> params, final ChangeStatusInterface changeStatusInterface) {
            gpsErrorDialog = new MacaronCustomDialog(BaseActivity.this, null, getString(R.string.gps_error_message), getString(R.string.txt_confirm),
                    new View.OnClickListener() {
                        public void onClick(View v) {
                            gpsErrorDialog.dismiss();
                            changeAllocationStatus(params, changeStatusInterface);
                        }
                    }, true, false);
            gpsErrorDialog.show();
        }
    };

    /**
     * 배차상태 변경 API
     * @param params params
     * @param changeStatusInterface changeStatusInterface
     */
    private void changeAllocationStatus(HashMap<String, Object> params, final ChangeStatusInterface changeStatusInterface) {
        DataInterface.getInstance().changeAllocationStatus(BaseActivity.this, params, new DataInterface.ResponseCallback<ResponseData<Object>>() {
            @Override
            public void onSuccess(ResponseData<Object> response) {
                if ("S000".equals(response.getResultCode())) {
                    if(changeStatusInterface != null) changeStatusInterface.onSuccess(response);
                } else {
                    if(changeStatusInterface != null) changeStatusInterface.onErrorCode(response);
                }
            }

            @Override
            public void onError(ResponseData<Object> response) {
                if(changeStatusInterface != null) changeStatusInterface.onError();
            }

            @Override
            public void onFailure(Throwable t) {
                if(changeStatusInterface != null) changeStatusInterface.onFailed(t);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_GPS) {
            startLocationService();
        }

        if(resultCode != RESULT_OK) return;
    }

    /**
     * GPS 위성설정 화면으로 이동시켜주는 팝업
     * @return baseGpsDialog
     */
    public MacaronCustomDialog getBaseGpsDialog() {
        if(baseGpsDialog == null) {
            baseGpsDialog = new MacaronCustomDialog(BaseActivity.this,
                    "위치 서비스 설정",
                    "무선 네트워크 사용, GPS 위성 사용을 모두 체크하셔야 정확한 위치 서비스가 가능합니다.",
                    "이동",
                    new OnSingleClickListener() {
                        @Override
                        public void onSingleClick(View v) {
                            baseGpsDialog.dismiss();
                            // GPS설정 화면으로 이동
                            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, REQUEST_GPS);
                        }
                    },
                    true);
        }

        return baseGpsDialog;
    }

    /**
     * 티맵 미설치된 단말일 경우 보여주는 안내팝업
     */
    public void showTmapNotInstallDialog(final ArrayList result) {
        if(tmapNotInstallDialog != null && tmapNotInstallDialog.isShowing()) {
            tmapNotInstallDialog.dismiss();
        }

        tmapNotInstallDialog = new MacaronCustomDialog(
                this,
                null,
                getString(R.string.tmap_not_install),
                "네",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tmapNotInstallDialog.dismiss();
                        Intent i = new Intent(android.content.Intent.ACTION_VIEW);
                        i.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.skt.tmap.ku"));
                        startActivity(i);
                    }
                },
                true);
        tmapNotInstallDialog.show();
    }

    /**
     * 서비스 실행
     */
    public void startLocationService() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(getApplicationContext(), MyLocationService.class));
        } else {
            startService(new Intent(getApplicationContext(), MyLocationService.class));
        }

//        bindService(new Intent(getApplicationContext(), MyLocationService.class), MacaronApp.mConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 서비스가 실행중인지 체크
     * @param serviceClass 서비스 이름
     * @return 실행중 true / 실행중아님  false
     */
    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    //============================================================================================
    /**
     * 출근 요청
     * @param resultInoffice 콜백
     */
    public void sendInoffice(final HashMap<String, Object> params, final ResultInoffice resultInoffice) {
        Util.getLocationInfomationParams(BaseActivity.this, reverseGeocodingInterfaceChauffeurInoffice, params, resultInoffice);
    }

    /**
     * Tmap에서 위치정보 받아오는 콜백리스너
     */
    private ReverseGeocodingInterfaceInoffice reverseGeocodingInterfaceChauffeurInoffice = new ReverseGeocodingInterfaceInoffice() {
        @Override
        public void onSuccess(HashMap<String, Object> params, ResultInoffice resultInoffice) {
            // 출근 api 호출
            sendChauffeurInoffice(params, resultInoffice);
        }

        @Override
        public void onError(HashMap<String, Object> params, ResultInoffice resultInoffice, final String errorMsg) {
            UIThread.executeInUIThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(BaseActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            });

            // 출근 api 호출
            sendChauffeurInoffice(params, resultInoffice);
        }

        @Override
        public void onGpsError(final HashMap<String, Object> params, final ResultInoffice resultInoffice) {
            String status = String.valueOf(params.get("chauffeurStatusCat"));

            if(!TextUtils.isEmpty(status) && status.equals("EXIT")) {
                // 출근 api 호출
                sendChauffeurInoffice(params, resultInoffice);

            } else {
                gpsErrorDialog = new MacaronCustomDialog(BaseActivity.this, null, getString(R.string.gps_error_message), getString(R.string.txt_confirm),
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                gpsErrorDialog.dismiss();
                                // 출근 api 호출
                                sendChauffeurInoffice(params, resultInoffice);
                            }
                        }, true, false);
                gpsErrorDialog.show();
            }
        }
    };

    /**
     * 받아온 위치정보로 쇼퍼 출근상태 변경 서버통신
     */
    private void sendChauffeurInoffice(HashMap<String, Object> params, final ResultInoffice resultInoffice) {
        DataInterface.getInstance().sendInoffice(BaseActivity.this, params, new DataInterface.ResponseCallback<ResponseData<Inoffice>>() {
            @Override
            public void onSuccess(ResponseData<Inoffice> response) {
                if ("S000".equals(response.getResultCode())) {
                    if(resultInoffice != null) resultInoffice.onSuccess(response);
                } else {
                    if(resultInoffice != null) resultInoffice.onErrorCode(response);
                }
            }

            @Override
            public void onError(ResponseData<Inoffice> response) {
                if(resultInoffice != null) resultInoffice.onError();
            }

            @Override
            public void onFailure(Throwable t) {
                if(resultInoffice != null) resultInoffice.onFailed(t);
            }
        });
    }

    /**
     * 법인 가입 상태에 따른 화면 이동
     */
    public void getRegistCompanyStatusSignUp(Context context) {
        playLoadingViewAnimation();

        HashMap<String, Object> params = new HashMap<>();
        params.put("regPhoneno", PrefUtil.getRegPhoneNo(BaseActivity.this));

        DataInterface.getInstance().getRegistCompanyStatus(BaseActivity.this, params, new DataInterface.ResponseCallback<ResponseData<CompanyStatusVO>>() {
            @Override
            public void onSuccess(ResponseData<CompanyStatusVO> response) {
                cancelLoadingViewAnimation();

                if(response == null || isFinishing()) return;
                if(!response.getResultCode().equals("S000")) {

                    return;
                }

                Intent intent = new Intent();

                if(response.getData() == null) {
                    intent.putExtra("phoneNum", PrefUtil.getRegPhoneNo(BaseActivity.this));
                    intent.setClass(context, CorporationRegisterActivity.class);
                }
                else {
                    switch (response.getData().getSvcStatus()) {
                        case Global.SVC_STATUS.REQUEST:     // 승인요청
                        case Global.SVC_STATUS.REREQUEST:   // 재승인요청
                        case Global.SVC_STATUS.APPROVED:    // 승인완료
                        {
                            intent.setClass(context, SignupRequestInformation.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            intent.putExtra("joinType", Global.JOIN_TYPE.COMPANY);
                            intent.putExtra("companyStatusVO", response.getData());
                            intent.putExtra("company", "company");
                            intent.putExtra("isForceFinish", true);
                            startActivity(intent);
                            break;
                        }
                        case Global.SVC_STATUS.AVAILABLE:    // 이용가능
                        {
                            intent.setClass(context, LoginActivity.class);
                            break;
                        }
                        case Global.SVC_STATUS.INFORJCT:    // 정보승인보류
                        case Global.SVC_STATUS.CONTRJCT:    // 계약승인보류
                        {
                            intent.setClass(context, SignupFailInformation.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            intent.putExtra("joinType", Global.JOIN_TYPE.COMPANY);
                            intent.putExtra("companyStatusVO", response.getData());

                            // 승인보류 사유 (어드민에서 직접 작성)
                            String incorrectinfoCat = "";
                            if(response.getData().getReason() != null) {
                                incorrectinfoCat = "※ 기타사유\n- " + response.getData().getReason();
                            }

                            intent.putExtra("chauffeurIncorrectinfoCat", incorrectinfoCat);

                            startActivity(intent);
                            break;
                        }
                        default: {
                            intent.setClass(context, LoginActivity.class);
                            break;
                        }
                    }
                }

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
            }

            @Override
            public void onError(ResponseData<CompanyStatusVO> response) {
                cancelLoadingViewAnimation();
                showCustomDialog(response.getError());
            }

            @Override
            public void onFailure(Throwable t) {
                cancelLoadingViewAnimation();
                Logger.d("LOG1 : " + t.getMessage());
            }
        });
    }

    /**
     * 쇼퍼 가입 상태에 따른 화면 이동
     */
    public void getRegistChauffeurStatus(Context context) {
        playLoadingViewAnimation();

        HashMap<String, Object> params = new HashMap<>();
        params.put("mobileNo", PrefUtil.getRegPhoneNo(BaseActivity.this));

        DataInterface.getInstance().getRegistChauffeurStatus(BaseActivity.this, params, new DataInterface.ResponseCallback<ResponseData<ChauffeurStatusVO>>() {
            @Override
            public void onSuccess(ResponseData<ChauffeurStatusVO> response) {
                cancelLoadingViewAnimation();

                if(response == null || isFinishing()) return;
                if(!response.getResultCode().equals("S000")) {

                    return;
                }

                Intent intent = new Intent();

                if(response.getData() == null) {
                    intent.setClass(context, TermsAgreeActivity.class);
                }
                else {
                    switch (response.getData().getSvcStatus()) {
                        case Global.SVC_STATUS.APPRWAIT:    // 승인대기
                            if(response.getData().getChauffeurRegInfoCat() == null) {
                                intent.setClass(context, TermsAgreeActivity.class);
                            }
                            else {
                                // 추가검색 후 화면 전환을 해줘야 함으로 통신을 한 후 return을 해준다.
                                getRegistChauffeurInfo(context, response.getData().getChauffeurRegInfoCat(), Global.JOIN_TYPE.CHAUFFEUR, response.getData(), response.getData().getSvcStatus(),false);
                                return;
                            }
                            break;
                        case Global.SVC_STATUS.REQUEST:     // 승인요청
                        case Global.SVC_STATUS.REREQUEST:   // 재승인요청
                        case Global.SVC_STATUS.APPROVED:    // 승인완료
                        {
                            intent.setClass(context, SignupRequestInformation.class);
                            intent.putExtra("information_finish", true);
                            intent.putExtra("joinType", Global.JOIN_TYPE.CHAUFFEUR);
                            intent.putExtra("svcStatus", response.getData().getSvcStatus());
                            break;
                        }
                        case Global.SVC_STATUS.AVAILABLE:    // 이용가능
                        {
                            intent.setClass(context, SignupFailInformation.class);
                            intent.putExtra("joinType", Global.JOIN_TYPE.CHAUFFEUR);
                            intent.putExtra("svcStatus", response.getData().getSvcStatus());
                            intent.putExtra("id", response.getData().getId());
                            break;
                        }
                        case Global.SVC_STATUS.INFORJCT:    // 정보승인보류
                        case Global.SVC_STATUS.CONTRJCT:    // 계약승인보류
                        {
                            intent.setClass(context, SignupFailInformation.class);
                            intent.putExtra("joinType", Global.JOIN_TYPE.CHAUFFEUR);
                            intent.putExtra("chauffeurStatusVO", response.getData());
                            intent.putExtra("svcStatus", response.getData().getSvcStatus());
                            if(response.getData().getChauffeurRegInfoCat() != null)
                                intent.putExtra("chauffeurRegInfoCat", response.getData().getChauffeurRegInfoCat());

                            // 승인보류 사유 (어드민에서 직접 작성)
                            String incorrectinfoCat = "";
                            if(response.getData().getReason() != null) {
                                incorrectinfoCat = "※ 기타사유\n- " + response.getData().getReason();
                            }

                            intent.putExtra("chauffeurIncorrectinfoCat", incorrectinfoCat);
                            break;
                        }
                        case Global.SVC_STATUS.BYE:    // 탈퇴
                            Toast.makeText(BaseActivity.this, "탈퇴한 기사입니다.", Toast.LENGTH_SHORT).show();
                            intent.setClass(context, LoginActivity.class);
                            break;
                        default: {
                            intent.setClass(context, LoginActivity.class);
                            break;
                        }
                    }
                }

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
            }

            @Override
            public void onError(ResponseData<ChauffeurStatusVO> response) {
                cancelLoadingViewAnimation();
                showCustomDialog(response.getError());
            }

            @Override
            public void onFailure(Throwable t) {
                cancelLoadingViewAnimation();
                Logger.d("LOG1 : " + t.getMessage());
            }
        });
    }

    public void getRegistChauffeurInfo(Context context, String reginfoCat, String joinType, ChauffeurStatusVO chauffeurStatusVO, String svcStatus, boolean isClickBackBtn) {
        playLoadingViewAnimation();

        HashMap<String, Object> params = new HashMap<>();
        params.put("mobileNo", PrefUtil.getRegPhoneNo(this));
        params.put("chauffeurRegInfoCat", reginfoCat);

        DataInterface.getInstance().getRegistChauffeurInfo(this, params, new DataInterface.ResponseCallback<ResponseData<RegistChauffeurInfoVO>>() {
            @Override
            public void onSuccess(ResponseData<RegistChauffeurInfoVO> response) {
                cancelLoadingViewAnimation();

                if(response == null || isFinishing()) return;
                if(!response.getResultCode().equals("S000")) {
                    //showCommonDialog(response.getError());
                    return;
                }

                Intent intent = new Intent();
                if(response.getData().getChauffeurRegInfoCat().equals(AppDef.ChauffeurRegInfoCat.MEMBER.toString())) {
                    intent.setClass(context, SignupBasicInfomation.class);

                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    // 기본정보 데이터가 없다
                    if(response.getData().getRcbi() != null)
                        intent.putExtra("registChauffeurInfoVO", response.getData());

                    intent.putExtra("joinType", joinType);
                    intent.putExtra("chauffeurStatusVO", chauffeurStatusVO);
                    intent.putExtra("svcStatus", svcStatus);
                    if(chauffeurStatusVO != null && chauffeurStatusVO.getChauffeurRegInfoCat() != null)
                        intent.putExtra("chauffeurRegInfoCat", chauffeurStatusVO.getChauffeurRegInfoCat());

                    startActivity(intent);
                    finish();
                    if(isClickBackBtn)
                        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                    else
                        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                }
                else if(response.getData().getChauffeurRegInfoCat().equals(AppDef.ChauffeurRegInfoCat.PROFILE.toString())) {
                    intent.setClass(context, SignupPictureRegistration.class);

                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    // 사진 데이터가 있다.
                    if(response.getData().getRcpi() != null && response.getData().getRcpi2() != null)
                        intent.putExtra("registChauffeurInfoVO", response.getData());

                    intent.putExtra("joinType", joinType);
                    intent.putExtra("chauffeurStatusVO", chauffeurStatusVO);
                    intent.putExtra("svcStatus", svcStatus);
                    if(chauffeurStatusVO != null && chauffeurStatusVO.getChauffeurRegInfoCat() != null)
                        intent.putExtra("chauffeurRegInfoCat", chauffeurStatusVO.getChauffeurRegInfoCat());

                    startActivity(intent);
                    finish();
                    if(isClickBackBtn)
                        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                    else
                        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                }
                else if(response.getData().getChauffeurRegInfoCat().equals(AppDef.ChauffeurRegInfoCat.ETC.toString())) {
                    intent.setClass(context, SignupAdditionalInformation.class);

                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    // 기본정보 데이터가 없다
                    if(response.getData().getRcai() != null)
                        intent.putExtra("registChauffeurInfoVO", response.getData());

                    intent.putExtra("joinType", joinType);
                    intent.putExtra("chauffeurStatusVO", chauffeurStatusVO);
                    intent.putExtra("svcStatus", svcStatus);
                    if(chauffeurStatusVO != null && chauffeurStatusVO.getChauffeurRegInfoCat() != null)
                        intent.putExtra("chauffeurRegInfoCat", chauffeurStatusVO.getChauffeurRegInfoCat());

                    startActivity(intent);
                    finish();
                    if(isClickBackBtn)
                        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                    else
                        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                }
            }

            @Override
            public void onError(ResponseData<RegistChauffeurInfoVO> response) { cancelLoadingViewAnimation(); }

            @Override
            public void onFailure(Throwable t) { cancelLoadingViewAnimation(); }
        });
    }

    private void showCustomDialog(String msg) {
        final Dialog dialog = new MacaronCustomDialog(this, getResources().getString(R.string.app_name), msg, getString(R.string.confirm)
                , view -> {}, false, true);

        if(!isFinishing()) dialog.show();
    }
}
