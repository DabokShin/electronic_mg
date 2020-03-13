package kst.ksti.chauffeur.activity.signup;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import com.jakewharton.rxbinding3.widget.RxTextView;
import com.jakewharton.rxbinding3.widget.TextViewAfterTextChangeEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import kst.ksti.chauffeur.common.Global;
import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.activity.BaseActivity;
import kst.ksti.chauffeur.activity.LoginActivity;
import kst.ksti.chauffeur.common.AnalyticsHelper;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.databinding.ActivityCorporationRegisterBinding;
import kst.ksti.chauffeur.model.CompanyStatusVO;
import kst.ksti.chauffeur.model.CompanyVO;
import kst.ksti.chauffeur.model.signup.SignupCodeList;
import kst.ksti.chauffeur.model.signup.SignupCodeListVO;
import kst.ksti.chauffeur.net.DataInterface;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.ui.dialog.SelectItemDialog;
import kst.ksti.chauffeur.utility.MacaronCustomDialog;
import kst.ksti.chauffeur.utility.PrefUtil;
import kst.ksti.chauffeur.utility.Util;

public class CorporationRegisterActivity extends BaseActivity<ActivityCorporationRegisterBinding> {

    private boolean isGunguAll = false;
    private boolean mSelfChange = false;
    private Dialog dialog = null;

    private CompanyStatusVO companyStatusVO = null;
    private CompanyVO companyVO = null;

    private String mSidoCd = null;
    private String mGunguCd = null;
    private String mCarCd = null;
    private String mBankCd = null;
    private String mPhoneNum = null;

    private List<SignupCodeList> sidoList = null;
    private List<SignupCodeList> carCatList = null;
    private List<SignupCodeList> bankList = null;

    private Disposable disposable = null;

    private interface CommonCallback {
        void onResult(List<SignupCodeList> list);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AnalyticsHelper.getInstance(this).sendScreenFromJson(this, CorporationRegisterActivity.class.getSimpleName());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setBind(R.layout.activity_corporation_register);

        Thread.setDefaultUncaughtExceptionHandler(((MacaronApp)getApplication()).getUncaughtExceptionHandler(CorporationRegisterActivity.this));

        getCommonCodes();
        initData();
        initView();
        setViewEventBind();
        setRxEventBind();

        if(companyVO != null)
            setData();
    }

    private void initData() {
        if(getIntent() != null && getIntent().getExtras() != null) {
            this.mPhoneNum = getIntent().getExtras().getString("phoneNum", "");

            this.companyStatusVO = (CompanyStatusVO)getIntent().getSerializableExtra("companyStatusVO");
            this.companyVO = (CompanyVO)getIntent().getSerializableExtra("companyVO");
        }
    }

    private void initView() {
        setLoadingBarLayout(getBind().loadingBarLayout);
        setProgressBar(getBind().progressBar);
        getBind().toolbar.btnDrawerOpen.setVisibility(View.GONE);
        getBind().toolbar.btnTitleBack.setVisibility(View.GONE);
        getBind().toolbar.tvTitle.setText(getResources().getString(R.string.corporation_register_title));
        getBind().tvJoinCourseInfo.setText(Html.fromHtml(getResources().getString(R.string.join_course_info)));
    }

    private void setViewEventBind() {
        // 시/도 선택 스피너
        getBind().spinnerSido.setOnClickListener(v-> {
            if(sidoList == null || sidoList.isEmpty()) return;

            generateListAndShowDialog(getString(R.string.corporation_register_spinner_sido_title), sidoList, (TextView) v, 2, getBind().spinnerSido.getText().toString());
        });

        // 시/군 선택 스피너
        getBind().spinnerGungu.setOnClickListener(v-> {
            if(mSidoCd == null) {
                Toast.makeText(this, getString(R.string.corporation_register_sido_empty), Toast.LENGTH_SHORT).show();
                return;
            }

            if(isGunguAll) {
                Toast.makeText(this, getString(R.string.corporation_register_not_allow_gungu), Toast.LENGTH_SHORT).show();
                return;
            }

            getCommonSubCodes(mSidoCd, list -> {
                if(list == null || list.isEmpty()) return;

                generateListAndShowDialog(getString(R.string.corporation_register_spinner_gungu_title), list, (TextView) v, 2, getBind().spinnerGungu.getText().toString());
            });
        });

        // 택시 유형 선택 스피너
        getBind().spinnerTaxiType.setOnClickListener(v-> {
            if(carCatList == null || carCatList.isEmpty()) return;

            generateListAndShowDialog(getString(R.string.corporation_register_spinner_taxi_title), carCatList, (TextView) v, 2, getBind().spinnerTaxiType.getText().toString());
        });

        // 은행 선택 스피너
        getBind().spinnerBank.setOnClickListener(v-> {
            if(bankList == null || bankList.isEmpty()) return;

            generateListAndShowDialog(getString(R.string.corporation_register_spinner_bank_title), bankList, (TextView) v, 2, getBind().spinnerBank.getText().toString());
        });

        // 사업자등록번호 유효성 체크
        getBind().btnExistCheck.setOnClickListener(v -> {
            if(getBind().edittextCorporationNumber.getText().length() != 12) {
                Toast.makeText(this, getString(R.string.corporation_register_bis_number_empty), Toast.LENGTH_SHORT).show();
                return;
            }

            if(getBind().edittextCorporationNumber.getText().toString().isEmpty()) {
                Toast.makeText(this, getString(R.string.corporation_register_bis_number_empty), Toast.LENGTH_SHORT).show();
                return;
            }

            checkBisNumber();
        });

        // 계좌번호 유효성 체크
        getBind().btnNumberAuth.setOnClickListener(v-> {
            if(mBankCd == null) {
                Toast.makeText(this, getString(R.string.corporation_register_bank_name_empty), Toast.LENGTH_SHORT).show();
                return;
            }

            if(getBind().edittextName.getText().toString().isEmpty()) {
                Toast.makeText(this, getString(R.string.corporation_register_bank_user_empty), Toast.LENGTH_SHORT).show();
                return;
            }
            else if(getBind().edittextBankNumber.getText().toString().isEmpty()) {
                Toast.makeText(this, getString(R.string.corporation_register_bank_number_empty), Toast.LENGTH_SHORT).show();
                return;
            }

            checkBankAccount();
        });

        // 법인 등록 신청
        getBind().btnRequest.setOnClickListener(v-> {
            // request register
            requestCorporationRegister();
        });

        // 취소
        getBind().btnCancel.setOnClickListener(v-> {
            if(Util.keyboardCheckAndHide(CorporationRegisterActivity.this)) {
                return;
            }

            backProcess();
        });

        // 보류 내용 작성
        if(companyStatusVO != null && companyStatusVO.getCitList() != null ) {
            TableRow subTableRow = new TableRow(this);
            TextView tvSubTitle1 = new TextView(this);
            TextView tvSubTitle2 = new TextView(this);

            tvSubTitle1.setText(getResources().getString(R.string.signup_complete_reason_subtitle1));
            tvSubTitle1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            tvSubTitle1.setTextColor(Color.parseColor("#fff97dad"));
            tvSubTitle2.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            tvSubTitle2.setText(getResources().getString(R.string.signup_complete_reason_subtitle2));
            tvSubTitle2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            tvSubTitle2.setTextColor(Color.parseColor("#fff97dad"));
            tvSubTitle2.setPadding(20, 0, 0, 0);
            subTableRow.setPadding(20, 0, 0, 0);
            subTableRow.addView(tvSubTitle1);
            subTableRow.addView(tvSubTitle2);

            getBind().tlReason.addView(subTableRow);

            for(CompanyStatusVO.CitList tmp : companyStatusVO.getCitList()) {
                TableRow tableRow = new TableRow(this);
                TextView tv1 = new TextView(this);
                TextView tv2 = new TextView(this);
                tv1.setText("  -");
                tv1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                tv1.setTextColor(Color.parseColor("#fff97dad"));
                tv2.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                tv2.setText(tmp.getCompanyIncorrectinfoText());
                tv2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                tv2.setTextColor(Color.parseColor("#fff97dad"));
                tv2.setPadding(20, 0, 0, 0);
                tableRow.setPadding(20, 0, 0, 0);
                tableRow.addView(tv1);
                tableRow.addView(tv2);

                getBind().tlReason.addView(tableRow);
            }

            getBind().tlReason.setVisibility(View.VISIBLE);
        }
        else {
            getBind().tlReason.setVisibility(View.GONE);
        }
    }

    private void setRxEventBind() {
        // editText text change event
        baseDisposable.add(Observable.merge(RxTextView.textChanges(getBind().edittextCorporationName).skipInitialValue(),
                                            RxTextView.textChanges(getBind().edittextCertificate).skipInitialValue(),
                                            RxTextView.textChanges(getBind().etJoinCourse).skipInitialValue())
                .subscribe( it -> {
                    if(it == null) return;

                    validNextCheck();
                }, Throwable::printStackTrace));

        baseDisposable.add(RxTextView.afterTextChangeEvents(getBind().edittextCorporationNumber)
                .skipInitialValue()
                .filter(v-> !mSelfChange)
                .map(TextViewAfterTextChangeEvent::getEditable)
                .subscribe(editable -> {
                    if(editable == null) return;

                    getBind().btnExistCheck.setSelected(false);

                    String number = Util.convertHyphenBisNumber(editable.toString());

                    if(number == null) return;

                    mSelfChange = true;
                    editable.replace(0, editable.length(), number, 0, number.length());
                    mSelfChange = false;

                    validNextCheck();
                }, Throwable::printStackTrace));

        baseDisposable.add(RxTextView.afterTextChangeEvents(getBind().edittextPhoneNumber)
                .skipInitialValue()
                .filter(v-> !mSelfChange)
                .map(TextViewAfterTextChangeEvent::getEditable)
                .subscribe(editable -> {
                    if(editable == null) return;

                    String number = Util.convertHyphenPhoneNumber(editable.toString());

                    if(number == null) return;

                    mSelfChange = true;
                    editable.replace(0, editable.length(), number, 0, number.length());
                    mSelfChange = false;

                    validNextCheck();
                }, Throwable::printStackTrace));

        baseDisposable.add(RxTextView.afterTextChangeEvents(getBind().etJoinCourse)
                .skipInitialValue()
                .filter(v-> !mSelfChange)
                .map(TextViewAfterTextChangeEvent::getEditable)
                .subscribe(editable -> {
                    if(editable == null) return;

                    validNextCheck();
                }, Throwable::printStackTrace));

        baseDisposable.add(Observable.merge(RxTextView.textChanges(getBind().edittextBankNumber).skipInitialValue(),
                                            RxTextView.textChanges(getBind().edittextName).skipInitialValue())
                .subscribe( it -> {
                    if(it == null) return;

                    getBind().btnNumberAuth.setSelected(false);
                    validNextCheck();
                }, Throwable::printStackTrace));
    }

    private void setData()
    {
        // 법인명
        getBind().edittextCorporationName.setText(companyVO.getName());

        // 사업자번호
        getBind().edittextCorporationNumber.setText(companyVO.getBusinessNo());
        getBind().btnExistCheck.setSelected(true);  // 사업자 인증 받은 것으로 처리

        // 전화번호
        getBind().edittextPhoneNumber.setText(companyVO.getRegPhoneno());

        // 지역(시도)
        mSidoCd = companyVO.getSignupAreaCd();
        getBind().spinnerSido.setText(companyVO.getSignupAreaName());

        // 지역(시군구)
        mGunguCd = companyVO.getSignupAreaDescriptionCd();
        if(mGunguCd == null) {
            isGunguAll = true;
            getBind().spinnerGungu.setText("전체");
        }
        else {
            getBind().spinnerGungu.setText(companyVO.getSignupAreaDescriptionName());
        }

        // 택시종류
        mCarCd = companyVO.getCarCat();
        getBind().spinnerTaxiType.setText(companyVO.getCarCatName());

        // 면허대수
        getBind().edittextCertificate.setText(companyVO.getLicenseRetentionQuanty());

        // 은행명
        mBankCd = companyVO.getBankCd();
        getBind().spinnerBank.setText(companyVO.getBankName());

        // 예금주
        getBind().edittextName.setText(companyVO.getDepositor());

        // 계좌번호
        getBind().edittextBankNumber.setText(companyVO.getAcntNo());
        getBind().btnNumberAuth.setSelected(true);  // 계좌 인증 받은 것으로 처리

        // 가입경로
        getBind().etJoinCourse.setText(companyVO.getSignupRecmdEtcContents());

        validNextCheck();
    }

    private void generateListAndShowDialog(String title, List<SignupCodeList> items, TextView targetView, int column, String selectStr) {
        if(disposable != null && !disposable.isDisposed()) disposable.dispose();
        if(dialog != null && dialog.isShowing()) return;

        disposable = Observable.fromIterable(items)
                .observeOn(AndroidSchedulers.mainThread())
                .map(SignupCodeList::getCdName)
                .toList()
                .subscribe(it -> {
                    dialog = new SelectItemDialog(this, title, it.toArray(new String[0]), index -> {
                        if(index < 0 || index > items.size() - 1) return;

                        SignupCodeList code = items.get(index);

                        switch (targetView.getId()) {
                            case R.id.spinner_sido: {
                                isGunguAll = false;

                                // 선택된 시/도가 이전 값과 다를 경우 시/군 초기화
                                if(mSidoCd != null && !mSidoCd.equals(code.getCd())) {
                                    getBind().spinnerGungu.setText("");
                                    mGunguCd = null;
                                }

                                // 선택된 시/도가 하위코드가 없을 경우 시/군 전체
                                if(code.getExtCd1() != null && code.getExtCd1().equals("END")) {
                                    isGunguAll = true;
                                    getBind().spinnerGungu.setText("전체");
                                    mGunguCd = null;
                                }

                                mSidoCd = code.getCd();
                                break;
                            }

                            case R.id.spinner_gungu: {
                                mGunguCd = code.getCd();
                                break;
                            }

                            case R.id.spinner_bank: {
                                // 선택된 은행이 이전 값과 다를 경우 인증 버튼 비활성화
                                if(mBankCd != null && !mBankCd.equals(code.getCd())) getBind().btnNumberAuth.setSelected(false);

                                mBankCd = code.getCd();
                                break;
                            }

                            case R.id.spinner_taxi_type: {
                                mCarCd = code.getCd();
                                break;
                            }
                        }

                        targetView.setText(code.getCdName());
                        validNextCheck();
                    }, 2, selectStr);

                    if(!isFinishing()) dialog.show();
                }, Throwable::printStackTrace);
    }

    private void showCommonDialog(String msg) {
        final Dialog dialog = new MacaronCustomDialog(this, getResources().getString(R.string.app_name), msg, getString(R.string.confirm)
                , view -> {}, false, true);

        if(!isFinishing()) dialog.show();
    }

    private void validNextCheck() {
        getBind().btnRequest.setEnabled(getBind().btnExistCheck.isSelected() &&
                                        !getBind().edittextCorporationName.getText().toString().isEmpty() &&
                                        !getBind().edittextPhoneNumber.getText().toString().isEmpty() &&
                                        mSidoCd != null &&
                                        (mGunguCd != null || isGunguAll) &&
                                        mCarCd != null &&
                                        !getBind().edittextCertificate.getText().toString().isEmpty() &&
                                        !getBind().etJoinCourse.getText().toString().isEmpty() &&
                                        getBind().btnNumberAuth.isSelected());
    }

    private void getCommonCodes() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("groupCds", Arrays.asList("SIDO_CD", "BANK_CD", "CAR_CAT"));

        playLoadingViewAnimation();

        DataInterface.getInstance().getCommonCodes(this, params, new DataInterface.ResponseCallback<ResponseData<SignupCodeListVO>>() {
            @Override
            public void onSuccess(ResponseData<SignupCodeListVO> response) {
                cancelLoadingViewAnimation();

                if(response == null || isFinishing()) return;

                if(!response.getResultCode().equals("S000")) {
                    Toast.makeText(CorporationRegisterActivity.this, response.getError(), Toast.LENGTH_SHORT).show();
                    return;
                }

                for(SignupCodeListVO code : response.getList()) {
                    if(code.getGroupCd().equals("SIDO_CD")) sidoList = code.getCodeList();
                    if(code.getGroupCd().equals("BANK_CD")) bankList = code.getCodeList();
                    if(code.getGroupCd().equals("CAR_CAT")) carCatList = code.getCodeList();
                }
            }

            @Override
            public void onError(ResponseData<SignupCodeListVO> response) { cancelLoadingViewAnimation(); }

            @Override
            public void onFailure(Throwable t) { cancelLoadingViewAnimation(); }
        });
    }

    private void getCommonSubCodes(String cd, CommonCallback callback) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("groupCd", "SIGUNGU_CD");
        params.put("cd", cd);

        playLoadingViewAnimation();

        DataInterface.getInstance().getCommonSubCodes(this, params, new DataInterface.ResponseCallback<ResponseData<SignupCodeList>>() {
            @Override
            public void onSuccess(ResponseData<SignupCodeList> response) {
                cancelLoadingViewAnimation();

                if(response == null || isFinishing()) return;

                if(!response.getResultCode().equals("S000")) {
                    Toast.makeText(CorporationRegisterActivity.this, response.getError(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if(callback != null) callback.onResult(response.getList());
            }

            @Override
            public void onError(ResponseData<SignupCodeList> response) { cancelLoadingViewAnimation(); }

            @Override
            public void onFailure(Throwable t) { cancelLoadingViewAnimation(); }
        });
    }

    private void checkBisNumber() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("regPhoneno",  PrefUtil.getRegPhoneNo(this));     // 등록전화번호
        params.put("businessNo", getBind().edittextCorporationNumber.getText().toString().replaceAll("-", ""));

        playLoadingViewAnimation();

        DataInterface.getInstance().checkBisNumber(this, params, new DataInterface.ResponseCallback<ResponseData<Object>>() {
            @Override
            public void onSuccess(ResponseData<Object> response) {
                cancelLoadingViewAnimation();

                if(response == null || isFinishing()) return;

                if(!response.getResultCode().equals("S000")) {
                    getBind().btnExistCheck.setSelected(false);
                    validNextCheck();
                    showCommonDialog(response.getError());
                    return;
                }

                getBind().btnExistCheck.setSelected(true);
                validNextCheck();
                showCommonDialog(getString(R.string.corporation_register_possible));
            }

            @Override
            public void onError(ResponseData<Object> response) { cancelLoadingViewAnimation(); }

            @Override
            public void onFailure(Throwable t) { cancelLoadingViewAnimation(); }
        });
    }

    private void checkBankAccount() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("depositor", getBind().edittextName.getText().toString());   // 예금주명
        params.put("bankCd", mBankCd);
        params.put("acntNo", getBind().edittextBankNumber.getText().toString());

        playLoadingViewAnimation();

        DataInterface.getInstance().checkBankAccount(this, params, new DataInterface.ResponseCallback<ResponseData<Object>>() {
            @Override
            public void onSuccess(ResponseData<Object> response) {
                cancelLoadingViewAnimation();

                if(response == null || isFinishing()) return;

                if(!response.getResultCode().equals("S000")) {
                    getBind().btnNumberAuth.setSelected(false);
                    validNextCheck();
                    showCommonDialog(response.getError());
                    return;
                }

                getBind().btnNumberAuth.setSelected(true);
                validNextCheck();
                showCommonDialog(getString(R.string.corporation_register_valid_bank_msg));
            }

            @Override
            public void onError(ResponseData<Object> response) { cancelLoadingViewAnimation(); }

            @Override
            public void onFailure(Throwable t) { cancelLoadingViewAnimation(); }
        });
    }

    private void requestCorporationRegister() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("appToken", PrefUtil.getPushKey(this));               // 앱토큰
        params.put("companyType", AppDef.AuthType.CORPORATE.name());
        params.put("name", getBind().edittextCorporationName.getText().toString());
        params.put("businessNo", getBind().edittextCorporationNumber.getText().toString());
        params.put("phoneNo", getBind().edittextPhoneNumber.getText().toString());
        params.put("signupAreaCd", mSidoCd);
        params.put("signupAreaDescriptionCd", mGunguCd);
        params.put("svcType", Global.ChauffeurMemberType.PARTNER);
        params.put("carCat", mCarCd);
        params.put("licenseRetentionQuanty", getBind().edittextCertificate.getText().toString());
        params.put("regPhoneno", mPhoneNum);
        params.put("depositor", getBind().edittextName.getText().toString());
        params.put("acntNo", getBind().edittextBankNumber.getText().toString());
        params.put("bankCd", mBankCd);
        params.put("signupPathCat", "ETC");                                             // 가입 경로 카테고리(HOMEPAGE:홈페이지 광고,AGENCY:대리점추천,CHAUFFEUR:쇼퍼추천,ETC:기타)
        params.put("signupRecmdAreaCd", null);                                          // 가입 추천 지역 코드(공통코드의 시도코드 사용, 미사용시 NULL)
        params.put("signupRecmdEtcContents", getBind().etJoinCourse.getText().toString());  // 가입 추천 기타 내용

        playLoadingViewAnimation();

        DataInterface.getInstance().requestCorporationRegister(this, params, new DataInterface.ResponseCallback<ResponseData<Object>>() {
            @Override
            public void onSuccess(ResponseData<Object> response) {
                cancelLoadingViewAnimation();

                if(response == null || isFinishing()) return;

                if(!response.getResultCode().equals("S000")) {
                    Toast.makeText(CorporationRegisterActivity.this, response.getError(), Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(CorporationRegisterActivity.this, SignupRequestInformation.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("joinType", Global.JOIN_TYPE.COMPANY);
                intent.putExtra("isForceFinish", true);
                intent.putExtra("companyRequestComplete", true);
                startActivity(intent);
            }

            @Override
            public void onError(ResponseData<Object> response) { cancelLoadingViewAnimation(); }

            @Override
            public void onFailure(Throwable t) { cancelLoadingViewAnimation(); }
        });
    }

    private void backProcess() {
        if(companyVO != null) {
            //finishAffinity();

            Intent intent = new Intent(this, SignupFailInformation.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            intent.putExtra("joinType", Global.JOIN_TYPE.COMPANY);
            intent.putExtra("companyStatusVO", companyStatusVO);

            startActivity(intent);
            overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
            finish();

            return;
        }

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        backProcess();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(disposable != null && !disposable.isDisposed()) disposable.dispose();
    }
}
