package kst.ksti.chauffeur.activity.signup;

import android.app.DatePickerDialog;
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

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import kst.ksti.chauffeur.common.Global;
import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.activity.BaseActivity;
import kst.ksti.chauffeur.activity.LoginActivity;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.databinding.ActivitySignupBasicInfomationBinding;
import kst.ksti.chauffeur.model.CompanyVO;
import kst.ksti.chauffeur.model.signup.ChauffeurStatusVO;
import kst.ksti.chauffeur.model.signup.RcbiVO;
import kst.ksti.chauffeur.model.signup.RegistChauffeurInfoVO;
import kst.ksti.chauffeur.model.signup.SignupCodeList;
import kst.ksti.chauffeur.net.DataInterface;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.ui.dialog.SelectItemDialog;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.MacaronCustomDialog;
import kst.ksti.chauffeur.utility.OnSingleClickListener;
import kst.ksti.chauffeur.utility.PrefUtil;
import kst.ksti.chauffeur.utility.Util;

/**
 * 기본정보 등록
 */
public class SignupBasicInfomation extends BaseActivity<ActivitySignupBasicInfomationBinding> {

    private final String INDIVIDUAL = "individual";
    private final String COMPANY = "company";
    private final String END = "END";

    private boolean mSelfChange = false;

    private String radioBtnType = "";
    private long companyIdx = 0;
    private String companyName = null;
    private String city = null;         // 시 (ex: 서울, 경기 등)
    private String dong = null;         // 동 (ex: 한남동, 명동 등)
    private String cityCode = null;     // 영업지역 시도코드
    private String dongCode = null;     // 영업지역 시군구코드
    private RegistChauffeurInfoVO registChauffeurInfoVO = null;
    private ChauffeurStatusVO chauffeurStatusVO = null;
    private String svcStatus = null;
    private String chauffeurRegInfoCat = null;
    private String joinType = null;

    private SelectItemDialog selectDialog;

    private ArrayList<SignupCodeList> codeLists = new ArrayList<>();
    private ArrayList<SignupCodeList> codeSubList = new ArrayList<>();
    private SignupCodeList areaCode = null;

    private ArrayList<CompanyVO> companyList = new ArrayList<>();

    protected CompositeDisposable baseDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.d(getResources().getString(R.string.begin_activity));    // 액티비티 호출 로그

        Thread.setDefaultUncaughtExceptionHandler(((MacaronApp) getApplication()).getUncaughtExceptionHandler(SignupBasicInfomation.this));

        Intent intent = getIntent();
        if (intent != null) {
            registChauffeurInfoVO = (RegistChauffeurInfoVO)getIntent().getSerializableExtra("registChauffeurInfoVO");
            chauffeurStatusVO = (ChauffeurStatusVO)intent.getSerializableExtra("chauffeurStatusVO");
            svcStatus = intent.getStringExtra("svcStatus");
            chauffeurRegInfoCat = intent.getStringExtra("chauffeurRegInfoCat");
            joinType = intent.getStringExtra("joinType");
        }

        setBind(R.layout.activity_signup_basic_infomation);

        setLoadingBarLayout(getBind().loadingBarLayout);
        setProgressBar(getBind().progressBar);

        getBind().title.tvTitle.setText("기본정보");

        getBind().title.btnDrawerOpen.setVisibility(View.GONE);
        getBind().title.btnTitleBack.setVisibility(View.GONE);

        getBind().rbIndividual.setOnClickListener(mOnClickListener);
        getBind().rbCompany.setOnClickListener(mOnClickListener);

        getBind().tvBirthday.setOnClickListener(onSingleClickListener);
        getBind().tvBirthday2.setOnClickListener(onSingleClickListener);

        getBind().btnCity.setOnClickListener(onSingleClickListener);
        getBind().btnDong.setOnClickListener(onSingleClickListener);

        getBind().btnCancel.setOnClickListener(onSingleClickListener);
        getBind().btnRequest.setOnClickListener(onSingleClickListener);
        getBind().btnRequest.setText(getString(R.string.signup_basic_information_request));
        getBind().tvJoinCourseInfo.setText(Html.fromHtml(getResources().getString(R.string.join_course_info)));
        if(chauffeurStatusVO != null && chauffeurStatusVO.getChauffeurRjctInfoList() != null) {
            for(String tmp : chauffeurStatusVO.getChauffeurRjctInfoList()) {
                // 승인 보류중 부가정보 입력도 보류 되었으면 버튼으르 다음으로 변경해준다.
                if(tmp.equals(AppDef.ChauffeurRegInfoCat.PROFILE.toString()) || tmp.equals(AppDef.ChauffeurRegInfoCat.ETC.toString())) {
                    getBind().btnRequest.setText(getString(R.string.signup_basic_information));
                    break;
                }
            }

            // 보류 내용 작성
            if(chauffeurStatusVO != null && chauffeurStatusVO.getCitList() != null ) {
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

                for(ChauffeurStatusVO.CitList tmp : chauffeurStatusVO.getCitList()) {
                    if(tmp.getChauffeurIncorrectinfoCat().contains(AppDef.ChauffeurRegInfoCat.MEMBER.toString())) {
                        TableRow tableRow = new TableRow(this);
                        TextView tv1 = new TextView(this);
                        TextView tv2 = new TextView(this);
                        tv1.setText("  -");
                        tv1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                        tv1.setTextColor(Color.parseColor("#fff97dad"));
                        tv2.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        tv2.setText(tmp.getChauffeurIncorrectinfoText());
                        tv2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                        tv2.setTextColor(Color.parseColor("#fff97dad"));
                        tv2.setPadding(20, 0, 0, 0);
                        tableRow.setPadding(20, 0, 0, 0);
                        tableRow.addView(tv1);
                        tableRow.addView(tv2);

                        getBind().tlReason.addView(tableRow);
                    }
                }

                getBind().tlReason.setVisibility(View.VISIBLE);
            }
            else {
                getBind().tlReason.setVisibility(View.GONE);
            }
        }
        else {
            getBind().tlReason.setVisibility(View.GONE);
            getBind().btnRequest.setText(getString(R.string.signup_basic_information));
        }

        // 사업자등록번호 유효성 체크
        getBind().btnExistCheck.setOnClickListener(v -> {
            if(getBind().etRegistrationNumber.getText().length() != 12) {
                Toast.makeText(this, getString(R.string.corporation_register_bis_number_empty), Toast.LENGTH_SHORT).show();
                return;
            }

            if(getBind().etRegistrationNumber.getText().toString().isEmpty()) {
                Toast.makeText(this, getString(R.string.corporation_register_bis_number_empty), Toast.LENGTH_SHORT).show();
                return;
            }

            checkBisNumber();
        });

        // 소속 조회
        getBind().btnCompanySearch.setOnClickListener(v -> {
            if(getBind().etCompanyName.getText().toString().isEmpty()) {
                Toast.makeText(this, getString(R.string.signup_basic_information_company_name_empty), Toast.LENGTH_SHORT).show();
                return;
            }

            getCompanyList();
        });

        if(!getBind().rbIndividual.isChecked() && !getBind().rbCompany.isChecked())
            getBind().normal.setVisibility(View.VISIBLE);
        else
            getBind().normal.setVisibility(View.GONE);

        setRxEventBind();

        // 화면에 채워줄 데이터가 있다.
        if(registChauffeurInfoVO != null && registChauffeurInfoVO.getRcbi() != null)
            setData(registChauffeurInfoVO.getRcbi());
    }

    private void setData(final RcbiVO rcbi)
    {
        switch(rcbi.companyType) {
            case "INDIVIDUAL":  // 개인
                radioBtnType = INDIVIDUAL;

                getBind().rbIndividual.setChecked(true);
                getBind().normal.setVisibility(View.GONE);

                codeLists.clear();
                // 전국시/도를 요청한다.
                getCodeAreaList(AppDef.GroupCode.SIDO_CD.toString());

                getBind().individual.setVisibility(View.VISIBLE);
                getBind().company.setVisibility(View.GONE);

                getBind().etRegistrationNumber.setText(rcbi.businessNo);
                getBind().etName.setText(rcbi.name);
                getBind().tvBirthday.setText(rcbi.birth);

                // 사업자 인증 확인 된 것으로 처리한다.
                getBind().btnExistCheck.setSelected(true);
                break;
            case "CORPORATE":   // 법인
                radioBtnType = COMPANY;

                getBind().rbCompany.setChecked(true);
                getBind().normal.setVisibility(View.GONE);

                getBind().individual.setVisibility(View.GONE);
                getBind().company.setVisibility(View.VISIBLE);

                companyIdx = rcbi.companyIdx;
                companyName = rcbi.companyName;
                getBind().etCompanyName.setText(rcbi.companyName);
                getBind().etName2.setText(rcbi.name);
                getBind().tvBirthday2.setText(rcbi.birth);

                // 소속이 있는 것으로 처리한다.
                getBind().btnCompanySearch.setSelected(true);
                break;
        }

        // 가입 경로
        getBind().etJoinCourse.setText(rcbi.signupRecmdEtcContents);

        validNextCheck();
    }

    private void checkBisNumber() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("regPhoneno",  PrefUtil.getRegPhoneNo(this));     // 등록전화번호
        params.put("businessNo", getBind().etRegistrationNumber.getText().toString().replaceAll("-", ""));

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
                showCommonDialog(getString(R.string.signup_basic_information_possible));
            }

            @Override
            public void onError(ResponseData<Object> response) { cancelLoadingViewAnimation(); }

            @Override
            public void onFailure(Throwable t) { cancelLoadingViewAnimation(); }
        });
    }

    private void getCompanyList() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("name", getBind().etCompanyName.getText().toString());

        companyIdx = 0;
        companyName = null;
        companyList.clear();

        // 회사명 EditBox에 이름이 바뀌어 있을 수 때문에 다음 버튼을 클릭 안되게 해준다.
        getBind().btnCompanySearch.setSelected(false);
        validNextCheck();

        playLoadingViewAnimation();

        DataInterface.getInstance().getCompanyList(this, params, new DataInterface.ResponseCallback<ResponseData<CompanyVO>>() {
            @Override
            public void onSuccess(ResponseData<CompanyVO> response) {
                cancelLoadingViewAnimation();

                if(response == null || isFinishing()) return;

                if(!response.getResultCode().equals("S000")) {
                    getBind().btnCompanySearch.setSelected(false);
                    validNextCheck();
                    showCommonDialog(response.getError());
                    return;
                }

                if(selectDialog != null && selectDialog.isShowing())
                    selectDialog.dismiss();

                if(response.getList().size() > 0) {
                    companyList.addAll(response.getList());

                    String[] _companyName = new String[companyList.size()];
                    for(int i = 0; i < companyList.size(); i++) {
                        _companyName[i] = companyList.get(i).getName();
                    }

                    selectDialog = new SelectItemDialog(SignupBasicInfomation.this, "회사 선택", _companyName, new SelectItemDialog.OnSelectItemDialogListener()
                    {
                        @Override
                        public void onClick(int index) {
                            if(index < 0 || companyList.size() - 1 < index ) {
                                Toast.makeText(SignupBasicInfomation.this, "회사를 선택해주세요.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            selectDialog.dismiss();

                            companyIdx = companyList.get(index).getCompanyIdx();
                            companyName = companyList.get(index).getName();
                            getBind().etCompanyName.setText(companyName.toString());

                            getBind().btnCompanySearch.setSelected(true);
                            validNextCheck();
                        }
                    }, 2, getBind().etCompanyName.getText().toString());
                    selectDialog.show();
                }
                else {
                    showCommonDialog(getResources().getString(R.string.signup_basic_information_company_empty));
                }
            }

            @Override
            public void onError(ResponseData<CompanyVO> response) { cancelLoadingViewAnimation(); }

            @Override
            public void onFailure(Throwable t) { cancelLoadingViewAnimation(); }
        });
    }

    private void showCommonDialog(String msg) {
        final Dialog dialog = new MacaronCustomDialog(this, getResources().getString(R.string.app_name), msg, getString(R.string.confirm)
                , view -> {}, false, true);

        if(!isFinishing()) dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void setRxEventBind() {
        // editText text change event
        // 개인
        baseDisposable.add(Observable.merge(RxTextView.textChanges(getBind().etRegistrationNumber).skipInitialValue(),
                RxTextView.textChanges(getBind().etName).skipInitialValue())
                .subscribe( it -> {
                    if(it == null) return;

                    validNextCheck();
                }, Throwable::printStackTrace));

        baseDisposable.add(RxTextView.afterTextChangeEvents(getBind().etRegistrationNumber)
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

        baseDisposable.add(RxTextView.afterTextChangeEvents(getBind().etName)
                .skipInitialValue()
                .subscribe(editable -> {
                    if(editable == null) return;

                    validNextCheck();
                }, Throwable::printStackTrace));

        baseDisposable.add(RxTextView.afterTextChangeEvents(getBind().etJoinCourse)
                .skipInitialValue()
                .subscribe(editable -> {
                    if(editable == null) return;

                    validNextCheck();
                }, Throwable::printStackTrace));


        // editText text change event
        // 법인
        baseDisposable.add(Observable.merge(RxTextView.textChanges(getBind().etCompanyName).skipInitialValue(),
                RxTextView.textChanges(getBind().etName2).skipInitialValue())
                .subscribe( it -> {
                    if(it == null) return;

                    validNextCheck();
                }, Throwable::printStackTrace));

        baseDisposable.add(RxTextView.afterTextChangeEvents(getBind().etCompanyName)
                .skipInitialValue()
                .map(TextViewAfterTextChangeEvent::getEditable)
                .subscribe(editable -> {
                    if(editable == null) return;

                    getBind().btnCompanySearch.setSelected(false);

                    validNextCheck();
                }, Throwable::printStackTrace));

        baseDisposable.add(RxTextView.afterTextChangeEvents(getBind().etName2)
                .skipInitialValue()
                .subscribe(editable -> {
                    if(editable == null) return;

                    validNextCheck();
                }, Throwable::printStackTrace));
    }

    private void validNextCheck() {
        // 개인
        if(getBind().rbIndividual.isChecked()) {
            getBind().btnRequest.setEnabled(getBind().btnExistCheck.isSelected() &&
                    !getBind().etName.getText().toString().isEmpty() &&
                    !getBind().etJoinCourse.getText().toString().isEmpty() &&
                    !getBind().tvBirthday.getText().toString().isEmpty() &&
                    (city != null && !city.isEmpty()) &&
                    (dong != null && !dong.isEmpty()));
        }
        // 법인
        else if(getBind().rbCompany.isChecked()) {
            getBind().btnRequest.setEnabled(getBind().btnCompanySearch.isSelected() &&
                    !getBind().etCompanyName.getText().toString().isEmpty() &&
                    !getBind().etName2.getText().toString().isEmpty() &&
                    !getBind().tvBirthday2.getText().toString().isEmpty() &&
                    (companyName != null && !companyName.isEmpty()));
        }
    }

    private OnSingleClickListener onSingleClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            switch (v.getId()) {
                case R.id.btn_cancel:
                    if(Util.keyboardCheckAndHide(SignupBasicInfomation.this)) {
                        break;
                    }

                    onBackPressed();
                    break;
                case R.id.btn_request:  // 다음 버튼
                    String str = "";

                    // 개인
                    if(getBind().rbIndividual.isChecked()) {
                        PrefUtil.setSignupCompanyType(SignupBasicInfomation.this, INDIVIDUAL);
                    }
                    // 법인
                    else if(getBind().rbCompany.isChecked()) {
                        PrefUtil.setSignupCompanyType(SignupBasicInfomation.this, COMPANY);
                    }

                    registChauffeurBaseInfo();
                    break;
                case R.id.tvBirthday:
                case R.id.tvBirthday2:
                    // DatePickerDialog
                    DatePickerDialog datePickerDialog = new DatePickerDialog(SignupBasicInfomation.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, datePickListener, 1980, 0, 1);
                    if(datePickerDialog.getWindow() != null) {
                        datePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    }
                    datePickerDialog.show();
                    break;
                case R.id.btn_city: // 전국시/도
                    if(selectDialog != null && selectDialog.isShowing())
                        selectDialog.dismiss();

                    String[] cityName = new String[codeLists.size()];
                    for(int i = 0; i < codeLists.size(); i++) {
                        cityName[i] = codeLists.get(i).getCdName();
                    }

                    selectDialog = new SelectItemDialog(SignupBasicInfomation.this, "전국 시도 선택", cityName, new SelectItemDialog.OnSelectItemDialogListener()
                    {
                        @Override
                        public void onClick(int index) {
                            if(index < 0 || codeLists.size() - 1 < index ) {
                                Toast.makeText(SignupBasicInfomation.this, "시/도를 선택해주세요.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            selectDialog.dismiss();

                            areaCode = codeLists.get(index);

                            // 시/도 글자 세팅
                            city = areaCode.getCdName();
                            cityCode = areaCode.getCd();
                            getBind().btnCity.setText(city.toString());

                            // 시/군/구를 요청한다.
                            codeSubList.clear();
                            if(areaCode.getExtCd1() != null && areaCode.getExtCd1().equals(END)) {
                                dong = "전체";
                                dongCode = null;
                                getBind().btnDong.setText(dong);
                            }
                            else {
                                dong = null;
                                getBind().btnDong.setText("");
                                getCodeSubList(AppDef.GroupCode.SIGUNGU_CD.toString(), areaCode.getCd());
                            }

                            validNextCheck();
                        }
                    }, 2, getBind().btnCity.getText().toString());
                    selectDialog.show();
                    break;
                case R.id.btn_dong:    // 시/군
                    if(city.isEmpty() || areaCode == null)
                    {
                        Toast.makeText(SignupBasicInfomation.this, "전국 시/도를 먼저 선택해주세요.", Toast.LENGTH_SHORT).show();
                        break;
                    }

                    if(dong != null && dong.equals("전체"))
                        break;

                    if(selectDialog != null && selectDialog.isShowing())
                        selectDialog.dismiss();

                    String[] dongName = new String[codeSubList.size()];
                    for(int i = 0; i < codeSubList.size(); i++) {
                        dongName[i] = codeSubList.get(i).getCdName();
                    }

                    selectDialog = new SelectItemDialog(SignupBasicInfomation.this, "시군 선택", dongName, new SelectItemDialog.OnSelectItemDialogListener()
                    {
                        @Override
                        public void onClick(int index) {
                            if(index < 0) {
                                Toast.makeText(SignupBasicInfomation.this, "시군을 선택해주세요.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            selectDialog.dismiss();

                            dongCode = codeSubList.get(index).getCd();
                            dong = codeSubList.get(index).getCdName();
                            getBind().btnDong.setText(dong.toString());

                            validNextCheck();
                        }
                    }, 2, getBind().btnDong.getText().toString());
                    selectDialog.show();
                    break;
            }
        }
    };

    private DatePickerDialog.OnDateSetListener datePickListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(android.widget.DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            String birthDay;
            String month = "";
            String day = "";

            if(0 <= monthOfYear && monthOfYear < 10)
                month = "0" + (monthOfYear + 1);
            else
                month = "" + (monthOfYear + 1);

            if(0 < dayOfMonth && dayOfMonth < 10)
                day = "0" + dayOfMonth;
            else
                day = "" + dayOfMonth;

            birthDay = year + "-" + month + "-" + day;

            // 개인
            if(getBind().rbIndividual.isChecked()) {
                getBind().tvBirthday.setText(birthDay);
            }
            // 법인
            else if(getBind().rbCompany.isChecked()) {
                getBind().tvBirthday2.setText(birthDay);
            }

            validNextCheck();
        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(chauffeurStatusVO != null && !chauffeurStatusVO.getSvcStatus().equals(Global.SVC_STATUS.APPRWAIT)) {
                if(radioBtnType.equals(INDIVIDUAL))
                    getBind().rbCompany.setChecked(false);
                else
                    getBind().rbIndividual.setChecked(false);

                return;
            }
            switch (v.getId()) {
                case R.id.rbIndividual: // 개인
                    if(getBind().rbIndividual.isChecked())
                    {
                        getBind().rbCompany.setChecked(false);
                        getBind().normal.setVisibility(View.GONE);
                        validNextCheck();

                        if(!radioBtnType.equals(INDIVIDUAL))
                        {
                            radioBtnType = INDIVIDUAL;

                            if(codeLists.size() <= 0)
                            {
                                codeLists.clear();
                                // 전국시/도를 요청한다.
                                getCodeAreaList(AppDef.GroupCode.SIDO_CD.toString());
                            }
                            else
                            {
                                getBind().individual.setVisibility(View.VISIBLE);
                                getBind().company.setVisibility(View.GONE);

                                city = getBind().btnCity.getText().toString();
                                dong = getBind().btnDong.getText().toString();

                                // 시/군/구를 요청한다.
                                codeSubList.clear();
                                dongCode = null;
                                for(SignupCodeList tmp : codeLists)
                                {
                                    if(tmp.getCdName().equals(city) && tmp.getExtCd1() == null)
                                    {
                                        dongCode = tmp.getCd();
                                        getCodeSubList(AppDef.GroupCode.SIGUNGU_CD.toString(), dongCode);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    break;
                case R.id.rbCompany:    // 법인
                    if(getBind().rbCompany.isChecked())
                    {
                        getBind().rbIndividual.setChecked(false);
                        getBind().normal.setVisibility(View.GONE);
                        validNextCheck();

                        if(!radioBtnType.equals(COMPANY))
                        {
                            getBind().individual.setVisibility(View.GONE);
                            getBind().company.setVisibility(View.VISIBLE);

                            companyName = getBind().etCompanyName.getText().toString();

                            radioBtnType = COMPANY;
                        }
                    }
                    break;
            }
        }
    };


    private void getCodeAreaList(String groupCd) {
        playLoadingViewAnimation();

        HashMap<String, Object> params = new HashMap<>();
        params.put("groupCd", groupCd);

        DataInterface.getInstance().getCodeAreaList(this, params, new DataInterface.ResponseCallback<ResponseData<SignupCodeList>>() {
            @Override
            public void onSuccess(ResponseData<SignupCodeList> response) {
                cancelLoadingViewAnimation();

                if(response == null || isFinishing()) return;
                if(!response.getResultCode().equals("S000")) return;

                if(registChauffeurInfoVO != null) { // Activity 생성시 데이터가 있을 경우 지역 자동 설정
                    codeLists.addAll(response.getList());

                    getBind().individual.setVisibility(View.VISIBLE);
                    getBind().company.setVisibility(View.GONE);

                    for(SignupCodeList tmp : codeLists) {
                        if(tmp.getCd().equals(registChauffeurInfoVO.getRcbi().signupAreaCd)) {
                            areaCode = tmp;
                            cityCode = registChauffeurInfoVO.getRcbi().signupAreaCd;
                            city = tmp.getCdName();
                            getBind().btnCity.setText(city.toString());

                            // 시/군/구를 요청한다.
                            codeSubList.clear();
                            if(tmp.getExtCd1() != null && tmp.getExtCd1().equals(END)) {
                                dong = "전체";
                                dongCode = null;
                                getBind().btnDong.setText(dong);
                                validNextCheck();
                            }
                            else {
                                dong = null;
                                getBind().btnDong.setText("");
                                getCodeSubList(AppDef.GroupCode.SIGUNGU_CD.toString(), tmp.getCd());
                            }
                            break;
                        }
                    }
                }
                // 개인버튼 클릭시
                else if(radioBtnType.equals(INDIVIDUAL))
                {
                    codeLists.addAll(response.getList());

                    getBind().individual.setVisibility(View.VISIBLE);
                    getBind().company.setVisibility(View.GONE);

                    city = getBind().btnCity.getText().toString();
                    dong = getBind().btnDong.getText().toString();
                }
            }

            @Override
            public void onError(ResponseData<SignupCodeList> response) { cancelLoadingViewAnimation(); }

            @Override
            public void onFailure(Throwable t) { cancelLoadingViewAnimation(); }
        });
    }

    private void getCodeSubList(String groupCd, String cd) {
        playLoadingViewAnimation();

        HashMap<String, Object> params = new HashMap<>();
        params.put("groupCd", groupCd);
        params.put("cd", cd);

        DataInterface.getInstance().getCodeSubList(this, params, new DataInterface.ResponseCallback<ResponseData<SignupCodeList>>() {
            @Override
            public void onSuccess(ResponseData<SignupCodeList> response) {
                cancelLoadingViewAnimation();

                if(response == null || isFinishing()) return;
                if(!response.getResultCode().equals("S000")) return;

                codeSubList.addAll(response.getList());

                if(registChauffeurInfoVO != null && registChauffeurInfoVO.getRcbi() != null) { // Activity 생성시 데이터가 있을 경우 지역 자동 설정
                    for(SignupCodeList tmp : codeSubList) {
                        if (tmp.getCd().equals(registChauffeurInfoVO.getRcbi().signupAreaDescriptionCd)) {
                            dongCode = tmp.getCd();
                            dong = tmp.getCdName();
                            getBind().btnDong.setText(dong.toString());
                            validNextCheck();
                            break;
                        }
                    }
                }
            }

            @Override
            public void onError(ResponseData<SignupCodeList> response) { cancelLoadingViewAnimation(); }

            @Override
            public void onFailure(Throwable t) { cancelLoadingViewAnimation(); }
        });
    }

    // 다음으로 넘어가기 위한 통신
    private void registChauffeurBaseInfo() {
        playLoadingViewAnimation();

        HashMap<String, Object> params = new HashMap<>();

        params.put("appToken", PrefUtil.getPushKey(this));                 // 앱토큰

        // 개인
        if(getBind().rbIndividual.isChecked()) {
            params.put("mobileNo",  PrefUtil.getRegPhoneNo(this));                 // 등록전화번호
            params.put("companyType", "INDIVIDUAL");                                        // 회사타입(CORPORATE:법인, INDIVIDUAL:개인)
            params.put("businessNo", getBind().etRegistrationNumber.getText().toString().replaceAll("-", ""));  // 사업자등록번호
            params.put("name", getBind().etName.getText().toString());                      // 이름
            params.put("birth", getBind().tvBirthday.getText().toString());                 // 생년월일
            params.put("signupAreaCd", cityCode);                                           // 영업지역 시도코드
            params.put("signupAreaDescriptionCd", dongCode);                                // 영업지역 시군구코드
            params.put("signupPathCat", "ETC");                                             // 가입 경로 카테고리(HOMEPAGE:홈페이지 광고,AGENCY:대리점추천,CHAUFFEUR:쇼퍼추천,ETC:기타)
            params.put("signupRecmdAreaCd", null);                                          // 가입 추천 지역 코드(공통코드의 시도코드 사용, 미사용시 NULL)
            params.put("signupRecmdEtcContents", getBind().etJoinCourse.getText().toString());  // 가입 추천 기타 내용
        }
        // 법인
        else if(getBind().rbCompany.isChecked()) {
            params.put("mobileNo",  PrefUtil.getRegPhoneNo(this));     // 등록전화번호
            params.put("companyType", "CORPORATE");                             // 회사타입(CORPORATE:법인, INDIVIDUAL:개인)
            params.put("companyIdx", companyIdx);                               // 사업자등록번호
            params.put("name", getBind().etName2.getText().toString());         // 이름
            params.put("birth", getBind().tvBirthday2.getText().toString());    // 생년월일
        }

        DataInterface.getInstance().registChauffeurBaseInfo(this, params, new DataInterface.ResponseCallback<ResponseData<Object>>() {
            @Override
            public void onSuccess(ResponseData<Object> response) {
                cancelLoadingViewAnimation();

                if(response == null || isFinishing()) return;
                if(!response.getResultCode().equals("S000")) {
                    showCommonDialog(response.getError());
                    return;
                }

                if(chauffeurStatusVO != null && chauffeurStatusVO.getChauffeurRjctInfoList() != null) {
                    for(String tmp : chauffeurStatusVO.getChauffeurRjctInfoList()) {
                        if(tmp.equals(AppDef.ChauffeurRegInfoCat.PROFILE.toString())) {
                            getRegistChauffeurInfo(SignupBasicInfomation.this, AppDef.ChauffeurRegInfoCat.PROFILE.toString(), joinType, chauffeurStatusVO, svcStatus, false);
                            return;
                        }
                        else if(tmp.equals(AppDef.ChauffeurRegInfoCat.ETC.toString())) {
                            getRegistChauffeurInfo(SignupBasicInfomation.this, AppDef.ChauffeurRegInfoCat.ETC.toString(), joinType, chauffeurStatusVO, svcStatus, false);
                            return;
                        }
                    }

                    Intent intent = new Intent(SignupBasicInfomation.this, SignupRequestInformation.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("information_finish", true);
                    intent.putExtra("svcStatus", getResources().getString(R.string.signup_complete_informaiton_chauffeur));

                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                }
                else {
                    getRegistChauffeurInfo(SignupBasicInfomation.this, AppDef.ChauffeurRegInfoCat.PROFILE.toString(), joinType, chauffeurStatusVO, svcStatus,false);
                }
            }

            @Override
            public void onError(ResponseData<Object> response) { cancelLoadingViewAnimation(); }

            @Override
            public void onFailure(Throwable t) { cancelLoadingViewAnimation(); }
        });
    }

    @Override
    public void onBackPressed() {
        if(chauffeurStatusVO != null && !chauffeurStatusVO.getSvcStatus().equals(Global.SVC_STATUS.APPRWAIT)) {
            //finishAffinity();
            Intent intent = new Intent(this, SignupFailInformation.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            intent.putExtra("joinType", joinType);
            intent.putExtra("chauffeurStatusVO", chauffeurStatusVO);
            intent.putExtra("svcStatus", svcStatus);
            if(chauffeurRegInfoCat != null)
                intent.putExtra("chauffeurRegInfoCat", chauffeurRegInfoCat);

            startActivity(intent);
            overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
            finish();
            return;
        }

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }
}
