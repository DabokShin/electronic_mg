package kst.ksti.chauffeur.activity.signup.auth;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.jakewharton.rxbinding3.widget.RxTextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import kst.ksti.chauffeur.common.Global;
import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.activity.BaseActivity;
import kst.ksti.chauffeur.activity.LoginActivity;
import kst.ksti.chauffeur.activity.signup.CorporationRegisterActivity;
import kst.ksti.chauffeur.activity.signup.SignupFailInformation;
import kst.ksti.chauffeur.activity.signup.SignupRequestInformation;
import kst.ksti.chauffeur.common.AnalyticsHelper;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.databinding.ActivityPhoneAuthConfirmBinding;
import kst.ksti.chauffeur.model.CompanyStatusVO;
import kst.ksti.chauffeur.model.PhoneAuthVO;
import kst.ksti.chauffeur.net.DataInterface;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.service.HookSmsReceiver;
import kst.ksti.chauffeur.utility.AppSignatureHelper;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.MacaronCustomDialog;
import kst.ksti.chauffeur.utility.PrefUtil;

public class PhoneAuthConfirmActivity extends BaseActivity<ActivityPhoneAuthConfirmBinding> {

    private final static Long maxCountValue = 301L;
    private String authId;
    private String phoneNum;

    @Nullable
    private AppDef.AuthType authType = null;
    private boolean isTimeOut = false;

    private interface StatusCallback<T> {
        void onStatusSuccess(T companyVO);
        void onStatusNotExist();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBind(R.layout.activity_phone_auth_confirm);

        Thread.setDefaultUncaughtExceptionHandler(((MacaronApp)getApplication()).getUncaughtExceptionHandler(PhoneAuthConfirmActivity.this));

        initView();
        setData();
        setViewEventBind();
        setSmsEventBind();
        startCountObserver();
    }

    private void initView() {
        setLoadingBarLayout(getBind().loadingBarLayout);
        setProgressBar(getBind().progressBar);
        getBind().toolbar.btnDrawerOpen.setVisibility(View.GONE);
        getBind().toolbar.tvTitle.setText(getResources().getString(R.string.phone_auth_title));
    }

    private void setData() {
        if(getIntent() != null && getIntent().getExtras() != null) {
            this.authId = getIntent().getExtras().getString("authId", "");
            this.phoneNum = getIntent().getExtras().getString("phoneNum", "");
            authType = (AppDef.AuthType) getIntent().getSerializableExtra("authType");
        }
    }

    private void setViewEventBind() {
        getBind().btnConfirm.setOnClickListener(v -> {
            // request phone auth confirm
            checkPhoneAuth();
        });

        getBind().btnRetry.setOnClickListener(v -> {
            baseDisposable.clear();
            setSmsEventBind();
            requestPhoneAuth();
        });

        getBind().toolbar.btnTitleBack.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) finishAfterTransition();
            else finish();
        });
    }

    private void setSmsEventBind() {
        baseDisposable.add(HookSmsReceiver.mSubject
                .subscribe(next -> {
                    if(next == null) return;

                    getBind().edittextAuthNumber.setText(next);
                }, Throwable::printStackTrace));
    }

    private void startCountObserver() {
        isTimeOut = false;

        baseDisposable.add(Observable.intervalRange(1, maxCountValue, 0, 1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map(time -> maxCountValue - time)
                .subscribe(count -> {
                    if(count == null) return;

                    Logger.d(count.toString());
                    getBind().countDown.setText(secondToMMSS(count.intValue()));
                }, Throwable::printStackTrace, () -> {
                    getBind().countDown.setText(getResources().getString(R.string.phone_auth_time_out));
                    getBind().btnConfirm.setEnabled(false);
                    isTimeOut = true;
                }));

        baseDisposable.add(RxTextView.textChanges(getBind().edittextAuthNumber)
                .filter(v-> !isTimeOut)
                .map(it -> it.length() == 6)
                .subscribe(enabled -> {
                    if(enabled == null) return;

                    getBind().btnConfirm.setEnabled(enabled);
                }, Throwable::printStackTrace));
    }

    private void checkPhoneAuth() {
        playLoadingViewAnimation();

        HashMap<String, Object> params = new HashMap<>();
        params.put("macaronCdIdx", authId);
        params.put("cd", getBind().edittextAuthNumber.getText().toString());
        params.put("receivePhoneNo", phoneNum);

        DataInterface.getInstance().checkPhoneAuth(this, params, authType, new DataInterface.ResponseCallback<ResponseData<PhoneAuthVO>>() {
            @Override
            public void onSuccess(ResponseData<PhoneAuthVO> response) {
                cancelLoadingViewAnimation();

                if(response == null || isFinishing()) return;

                if(response.getResultCode().equals("S000")) {
                    startNextPage();
                    return;
                }

                if(response.getResultCode().equals("EC101") || response.getResultCode().equals("EC111")) {
//                    Spanned html = Html.fromHtml("<strong><font color='#ff1c74'><big><b>010-2955-2472</b></big></font></strong><br><br><font color='#555555'>해당 번호로 가입된 정보가 있습니다.<br>로그인하시겠습니까?</font><br><br><font color='#a5a5a5'><small>*가입한 적이 없는 경우<br>고객센타(1811-7994)로 문의바랍니다.</small></font>");
                    Dialog dialog = new MacaronCustomDialog(PhoneAuthConfirmActivity.this, getResources().getString(R.string.app_name), response.getError(), "로그인"
                            , view -> {
                        Intent intent = new Intent(PhoneAuthConfirmActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                    }, false, true);

                    if(!isFinishing()) dialog.show();

                    return;
                }

                Toast.makeText(getApplicationContext(), response.getError(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(ResponseData<PhoneAuthVO> response) { cancelLoadingViewAnimation(); }

            @Override
            public void onFailure(Throwable t) { cancelLoadingViewAnimation(); }
        });
    }

    private void requestPhoneAuth() {
        playLoadingViewAnimation();

        SmsRetrieverClient client = SmsRetriever.getClient(this);
        client.startSmsRetriever();

        HashMap<String, Object> params = new HashMap<>();
        params.put("receivePhoneNo", phoneNum);

        ArrayList<String> hash = new AppSignatureHelper(this).getAppSignatures();
        if(hash != null && hash.size() > 0) params.put("hash", hash.get(0));

        DataInterface.getInstance().requestPhoneAuth(this, params, authType, new DataInterface.ResponseCallback<ResponseData<PhoneAuthVO>>() {
            @Override
            public void onSuccess(ResponseData<PhoneAuthVO> response) {
                cancelLoadingViewAnimation();

                if(response == null || isFinishing()) return;

                if(!response.getResultCode().equals("S000")) {
                    Toast.makeText(PhoneAuthConfirmActivity.this, response.getError(), Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(PhoneAuthConfirmActivity.this, getResources().getString(R.string.phone_auth_send_complete), Toast.LENGTH_SHORT).show();
                authId = String.valueOf(response.getData().getMacaronCdIdx());

                startCountObserver();
            }

            @Override
            public void onError(ResponseData<PhoneAuthVO> response) { cancelLoadingViewAnimation(); }

            @Override
            public void onFailure(Throwable t) { cancelLoadingViewAnimation(); }
        });
    }

    private void getRegistCompanyStatus(StatusCallback callback) {
        playLoadingViewAnimation();

        HashMap<String, Object> params = new HashMap<>();
        params.put("regPhoneno", phoneNum);

        DataInterface.getInstance().getRegistCompanyStatus(this, params, new DataInterface.ResponseCallback<ResponseData<CompanyStatusVO>>() {
            @Override
            public void onSuccess(ResponseData<CompanyStatusVO> response) {
                cancelLoadingViewAnimation();

                if(response == null || isFinishing()) return;

                if(response.getResultCode().equals("S000")) {
                    callback.onStatusSuccess(response.getData());
                    return;
                }

                if(response.getResultCode().equals("EC302")) {
                    callback.onStatusNotExist();
                    return;
                }

                Toast.makeText(PhoneAuthConfirmActivity.this, response.getError(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(ResponseData<CompanyStatusVO> response) { cancelLoadingViewAnimation(); }

            @Override
            public void onFailure(Throwable t) { cancelLoadingViewAnimation(); }
        });
    }

    private String secondToMMSS(int from) {
        int minute = from / 60;
        int second = from % 60;

        return String.format(Locale.getDefault(),"%d:%02d", minute, second);
    }

    private void startNextPage() {
        baseDisposable.clear();

        PrefUtil.setRegPhoneNo(this, phoneNum);

        // 쇼퍼 가입 상태 조회 후 랜딩
        if(authType == AppDef.AuthType.PRIVATE) {
            PrefUtil.setRegJoinType(this, Global.JOIN_TYPE.CHAUFFEUR);
            startChauffeurProcess();
            return;
        }

        // 법인 등록 상태 조회 후 랜딩
        PrefUtil.setRegJoinType(this, Global.JOIN_TYPE.COMPANY);
        startCompanyProcess();
    }

    private void startChauffeurProcess() {
        playLoadingViewAnimation();
        getRegistChauffeurStatus(PhoneAuthConfirmActivity.this);
    }

    private void startCompanyProcess() {
        getRegistCompanyStatus(new StatusCallback<CompanyStatusVO>() {
            // 승인요청중인 법인등록건이 있을 경우
            @Override
            public void onStatusSuccess(CompanyStatusVO companyStatusVO) {

                if(companyStatusVO == null) {
                    Intent intent = new Intent(PhoneAuthConfirmActivity.this, CorporationRegisterActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("phoneNum", phoneNum);
                    startActivity(intent);
                }
                else {
                    switch (AppDef.CompanySvcStatus.getValue(companyStatusVO.getSvcStatus())) {
                        case REQUEST:
                        case REREQUEST: {
                            Intent intent = new Intent(PhoneAuthConfirmActivity.this, SignupRequestInformation.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            intent.putExtra("companyStatusVO", companyStatusVO);
                            intent.putExtra("isForceFinish", true);
                            startActivity(intent);
                            finish();
                            break;
                        }

                        case INFORJCT:
                        case CONTRJCT: {
                            Intent intent = new Intent(PhoneAuthConfirmActivity.this, SignupFailInformation.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            intent.putExtra("joinType", Global.JOIN_TYPE.COMPANY);
                            intent.putExtra("companyStatusVO", companyStatusVO);
                            startActivity(intent);
                            finish();
                            break;
                        }

                        default: {
                            break;
                        }
                    }
                }
            }

            // 없을 경우
            @Override
            public void onStatusNotExist() {
                Intent intent = new Intent(PhoneAuthConfirmActivity.this, CorporationRegisterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("phoneNum", phoneNum);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        AnalyticsHelper.getInstance(this).sendScreenFromJson(this, PhoneAuthConfirmActivity.class.getSimpleName());
    }
}
