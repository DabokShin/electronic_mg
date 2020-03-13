package kst.ksti.chauffeur.activity.signup;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.activity.BaseActivity;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.databinding.ActivitySignupAdditionalInformationBinding;
import kst.ksti.chauffeur.model.signup.ChauffeurStatusVO;
import kst.ksti.chauffeur.model.signup.RcaiVO;
import kst.ksti.chauffeur.model.signup.RegistChauffeurInfoVO;
import kst.ksti.chauffeur.model.signup.SignupCodeList;
import kst.ksti.chauffeur.model.signup.SignupCodeListVO;
import kst.ksti.chauffeur.net.DataInterface;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.ui.dialog.SelectItemDialog;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.MacaronCustomDialog;
import kst.ksti.chauffeur.utility.OnSingleClickListener;
import kst.ksti.chauffeur.utility.PrefUtil;
import kst.ksti.chauffeur.utility.Util;

/**
 * 개인택시 부가정보
 */
public class SignupAdditionalInformation extends BaseActivity<ActivitySignupAdditionalInformationBinding> {

    private ArrayList<SignupCodeListVO> codeLists = new ArrayList<>();
    private ArrayList<SignupCodeList> codeSubList = new ArrayList<>();

    private SignupCodeListVO codeListVO = null;
    private String groupCd;
    private String popupToastText;
    private SelectItemDialog selectDialog;

    private String bankCd = null;
    private String bank = null;
    private String carCompanyCd = null;
    private String carCompany = null;
    private String carModelCd = null;
    private String carModel = null;
    private String carNumberArea = null;
    private String carTypeCd = null;
    private String carType = null;

    private RegistChauffeurInfoVO registChauffeurInfoVO = null;
    private ChauffeurStatusVO chauffeurStatusVO = null;
    private String svcStatus = null;
    private String chauffeurRegInfoCat = null;
    private String joinType = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.d(getResources().getString(R.string.begin_activity));    // 액티비티 호출 로그

        Thread.setDefaultUncaughtExceptionHandler(((MacaronApp)getApplication()).getUncaughtExceptionHandler(SignupAdditionalInformation.this));

        setBind(R.layout.activity_signup_additional_information);

        setLoadingBarLayout(getBind().loadingBarLayout);
        setProgressBar(getBind().progressBar);

        getBind().title.tvTitle.setText("부가정보");

        getBind().title.btnDrawerOpen.setVisibility(View.GONE);
        getBind().title.btnTitleBack.setVisibility(View.GONE);

        getBind().btnBank.setOnClickListener(onSingleClickListener);
        getBind().btnCarCompany.setOnClickListener(onSingleClickListener);
        getBind().btnCarModel.setOnClickListener(onSingleClickListener);
        getBind().btnCarArea.setOnClickListener(onSingleClickListener);
        getBind().btnCarType.setOnClickListener(onSingleClickListener);

        // 계좌 인증
        getBind().btnBankAccountConfirm.setOnClickListener(v -> {
            if(getBind().etName.getText().toString().isEmpty()) {
                Toast.makeText(this, getString(R.string.corporation_register_bank_user_empty), Toast.LENGTH_SHORT).show();
                return;
            }
            else if(getBind().etBankAccountNumber.getText().toString().isEmpty()) {
                Toast.makeText(this, getString(R.string.corporation_register_bank_number_empty), Toast.LENGTH_SHORT).show();
                return;
            }

            checkBankAccount();
        });

        getBind().btnCancel.setOnClickListener(onSingleClickListener);
        getBind().btnRequest.setOnClickListener(onSingleClickListener);

        getCodeList();
        setRxEventBind();

        Intent intent = getIntent();
        if (intent != null) {
            registChauffeurInfoVO = (RegistChauffeurInfoVO)getIntent().getSerializableExtra("registChauffeurInfoVO");
            chauffeurStatusVO = (ChauffeurStatusVO)intent.getSerializableExtra("chauffeurStatusVO");
            svcStatus = intent.getStringExtra("svcStatus");
            chauffeurRegInfoCat = intent.getStringExtra("chauffeurRegInfoCat");
            joinType = intent.getStringExtra("joinType");

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
                    if(tmp.getChauffeurIncorrectinfoCat().contains(AppDef.ChauffeurRegInfoCat.ETC.toString())) {
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
    }

    private void setRxEventBind() {
        // editText text change event
        baseDisposable.add(Observable.merge(RxTextView.textChanges(getBind().etBankAccountNumber).skipInitialValue(),
                RxTextView.textChanges(getBind().etName).skipInitialValue(),
                RxTextView.textChanges(getBind().etCarNumber).skipInitialValue())
                .subscribe( it -> {
                    if(it == null) return;

                    validNextCheck();
                }, Throwable::printStackTrace));

        baseDisposable.add(RxTextView.afterTextChangeEvents(getBind().etBankAccountNumber)
                .skipInitialValue()
                .map(TextViewAfterTextChangeEvent::getEditable)
                .subscribe(editable -> {
                    if(editable == null) return;

                    getBind().btnBankAccountConfirm.setSelected(false);

                    String number = Util.convertHyphenBisNumber(editable.toString());

                    if(number == null) return;

                    validNextCheck();
                }, Throwable::printStackTrace));

        baseDisposable.add(RxTextView.afterTextChangeEvents(getBind().etName)
                .skipInitialValue()
                .subscribe(editable -> {
                    if(editable == null) return;

                    validNextCheck();
                }, Throwable::printStackTrace));

        baseDisposable.add(RxTextView.textChanges(getBind().etCarNumber)
                .skipInitialValue()
                .subscribe( it -> {
                    if(it == null) return;

                    validNextCheck();
                }, Throwable::printStackTrace));
    }

    private void validNextCheck() {
        getBind().btnRequest.setEnabled(getBind().btnBankAccountConfirm.isSelected() &&
                    !getBind().etName.getText().toString().isEmpty() &&
                    !getBind().etBankAccountNumber.getText().toString().isEmpty() &&
                    !getBind().etCarNumber.getText().toString().isEmpty() &&
                    (bank != null && !bank.isEmpty()) &&
                    (carCompany != null && !carCompany.isEmpty()) &&
                    (carModel != null && !carModel.isEmpty()) &&
                    (carNumberArea != null && !carNumberArea.isEmpty()) &&
                    (carType != null && !carType.isEmpty()));
    }

    private void checkBankAccount() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("depositor", getBind().etName.getText().toString());   // 예금주명
        params.put("bankCd", bankCd); // 은행코드
        params.put("acntNo", getBind().etBankAccountNumber.getText().toString());   // 계좌번호

        playLoadingViewAnimation();

        DataInterface.getInstance().checkBankAccount(this, params, new DataInterface.ResponseCallback<ResponseData<Object>>() {
            @Override
            public void onSuccess(ResponseData<Object> response) {
                cancelLoadingViewAnimation();

                if(response == null || isFinishing()) return;

                if(response.getResultCode().equals("S000")) {
                    getBind().btnBankAccountConfirm.setSelected(true);
                    validNextCheck();
                    showCommonDialog(getString(R.string.corporation_register_valid_bank_msg));
                    return;
                }
                else if(response.getResultCode().equals("EM221") || response.getResultCode().equals("EM225")) {
                    getBind().btnBankAccountConfirm.setSelected(false);
                    validNextCheck();
                    showCommonDialog(response.getError());
                    return;
                }
                else {
                    getBind().btnBankAccountConfirm.setSelected(false);
                    validNextCheck();
                    showCommonDialog(response.getError());
                }
            }

            @Override
            public void onError(ResponseData<Object> response) { cancelLoadingViewAnimation(); }

            @Override
            public void onFailure(Throwable t) { cancelLoadingViewAnimation(); }
        });
    }

    private OnSingleClickListener onSingleClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            switch (v.getId()) {
                case R.id.btn_cancel:
                    if(Util.keyboardCheckAndHide(SignupAdditionalInformation.this)) {
                        break;
                    }

                    onBackPressed();
                    break;
                case R.id.btn_request:
                    registChauffeurAddInfo();
                    break;
                case R.id.btnBank:  // 은행
                    SetCodeSelectPopup(AppDef.GroupCode.BANK_CD.toString(), "은행 선택", "은행을 선택해주세요.", bank);
                    break;
                case R.id.btnCarCompany:    // 차량 제조사
                    SetCodeSelectPopup(AppDef.GroupCode.AUTMAKE_CD.toString(), "제조사 선택", "차량 제조사를 선택해주세요.", carCompany);
                    break;
                case R.id.btnCarModel:      // 차량 모델
                    if(carCompany == null || carCompany.isEmpty()) {
                        showCommonDialog(getString(R.string.signup_additional_information_car_company_empty));
                        break;
                    }
                    getCodeSubList(AppDef.GroupCode.CARMODL_CD.toString(), carCompanyCd);
                    break;
                case R.id.btnCarArea:       // 차량번호 지역
                    SetCodeSelectPopup(AppDef.GroupCode.SIDO_CD.toString(), "차량번호 지역", "차량번호 지역을 선택해주세요.", carNumberArea);
                    break;
                case R.id.btnCarType:       // 택시 유형
                    SetCodeSelectPopup(AppDef.GroupCode.CAR_CAT.toString(), "택시유형(요금)", "택시유형을 선택해주세요.", carType);
                    break;

            }
        }
    };

    private void getCodeList() {
        playLoadingViewAnimation();

        codeLists.clear();

        HashMap<String, Object> params = new HashMap<>();
        params.put("groupCds", new String[]{"BANK_CD", "AUTMAKE_CD", "SIDO_CD", "CAR_CAT"});

        DataInterface.getInstance().getCodeList(this, params, new DataInterface.ResponseCallback<ResponseData<SignupCodeListVO>>() {
            @Override
            public void onSuccess(ResponseData<SignupCodeListVO> response) {
                cancelLoadingViewAnimation();

                if(response == null || isFinishing()) return;
                if(!response.getResultCode().equals("S000")) return;

                codeLists.addAll(response.getList());

                // 넘겨 받은 데이터가 있다면~
                if(registChauffeurInfoVO != null) {
                    SetData();
                }
            }

            @Override
            public void onError(ResponseData<SignupCodeListVO> response) { cancelLoadingViewAnimation(); }

            @Override
            public void onFailure(Throwable t) { cancelLoadingViewAnimation(); }
        });
    }

    private void getCodeSubList(String groupCd, String cd) {
        playLoadingViewAnimation();

        codeSubList.clear();

        HashMap<String, Object> params = new HashMap<>();
        params.put("groupCd", groupCd);
        params.put("cd", cd);

        DataInterface.getInstance().getCodeSubList(this, params, new DataInterface.ResponseCallback<ResponseData<SignupCodeList>>() {
            @Override
            public void onSuccess(ResponseData<SignupCodeList> response) {
                cancelLoadingViewAnimation();

                if (response == null || isFinishing()) return;
                if (!response.getResultCode().equals("S000")) return;

                codeSubList.addAll(response.getList());

                SetSubCodeSelectPopup("차량 모델 선택", "차량 모델을 선택해주세요.");
            }

            @Override
            public void onError(ResponseData<SignupCodeList> response) {
                cancelLoadingViewAnimation();
            }

            @Override
            public void onFailure(Throwable t) {
                cancelLoadingViewAnimation();
            }
        });
    }

    private void SetData() {
        if(registChauffeurInfoVO.getRcai() == null) {
            return;
        }

        final RcaiVO rcai = registChauffeurInfoVO.getRcai();

        // 은행
        bankCd = rcai.bankCd;
        bank = getCodeListVO_Name(AppDef.GroupCode.BANK_CD.toString(), bankCd);
        getBind().tvBank.setText(bank);

        // 예금주명
        getBind().etName.setText(rcai.depositor);

        // 계좌번호
        getBind().etBankAccountNumber.setText(rcai.acntNo);
        getBind().btnBankAccountConfirm.setSelected(true);    // 계좌 인증 받은 것으로 처리

        // 차량 제조사
        carCompanyCd = rcai.autmakeCd;
        carCompany = getCodeListVO_Name(AppDef.GroupCode.AUTMAKE_CD.toString(), carCompanyCd);
        getBind().tvCarCompany.setText(carCompany);

        // 차량 모델
        carModelCd = rcai.carmodlCd;
        carModel = rcai.carmodlName;
        getBind().tvCarModel.setText(carModel);

        // 차량번호 지역
        carNumberArea = rcai.carNo.substring(0, 2);
        getBind().tvCarNumberArea.setText(carNumberArea);
        getBind().etCarNumber.setText(rcai.carNo.substring(2, 9));

        // 택시유형
        carTypeCd = rcai.carCat;
        carType = getCodeListVO_Name(AppDef.GroupCode.CAR_CAT.toString(), carTypeCd);
        getBind().tvCarType.setText(carType);

        validNextCheck();
    }

    private String getCodeListVO_Name(String _groupCd, String cd) {
        groupCd = _groupCd;

        // 해당 코드 데이터를 가져온다.
        for(SignupCodeListVO list : codeLists)
        {
            if(list.getGroupCd().equals(groupCd))
            {
                codeListVO = list;
                break;
            }
        }

        for(SignupCodeList tmp : codeListVO.codeList) {
            if(tmp.getCd().equals(cd)) {
                return tmp.getCdName();
            }
        }

        return null;
    }

    private void SetCodeSelectPopup(String _groupCd, final String title, final String toastText, final String selectStr)
    {
        groupCd = _groupCd;
        popupToastText = toastText;

        // 해당 코드 데이터를 가져온다.
        for(SignupCodeListVO list : codeLists)
        {
            if(list.getGroupCd().equals(groupCd))
            {
                codeListVO = list;
                break;
            }
        }
        if(selectDialog != null && selectDialog.isShowing())
            selectDialog.dismiss();

        if(codeListVO == null) {
            Logger.d("LOG1 : codeList data null");
            return;
        }

        String[] name = new String[codeListVO.codeList.size()];
        for(int i = 0; i < codeListVO.codeList.size(); i++) {
            if(groupCd.equals(AppDef.GroupCode.SIDO_CD.toString())) {  // 차량번호 지역일때만 cd2를 넣어준다. (지역 글자수를 2글자로 맞춰주기 위함)
                name[i] = codeListVO.codeList.get(i).getExtCd2();
            }
            else {
                name[i] = codeListVO.codeList.get(i).getCdName();
            }
        }

        selectDialog = new SelectItemDialog(SignupAdditionalInformation.this, title, name, new SelectItemDialog.OnSelectItemDialogListener()
        {
            @Override
            public void onClick(int index) {
                if(index < 0) {
                    Toast.makeText(SignupAdditionalInformation.this, popupToastText, Toast.LENGTH_SHORT).show();
                    return;
                }
                selectDialog.dismiss();

                if(groupCd.equals(AppDef.GroupCode.BANK_CD.toString())) {   // 은행
                    bankCd = codeListVO.codeList.get(index).getCd();
                    bank = codeListVO.codeList.get(index).getCdName();
                    getBind().tvBank.setText(bank);
                }
                else if(groupCd.equals(AppDef.GroupCode.AUTMAKE_CD.toString())) {  // 차량 제조사
                    carCompanyCd = codeListVO.codeList.get(index).getCd();
                    carCompany = codeListVO.codeList.get(index).getCdName();
                    getBind().tvCarCompany.setText(carCompany);

                    carModel = null;
                    getBind().tvCarModel.setText("");
                }
                else if(groupCd.equals(AppDef.GroupCode.CARMODL_CD.toString())) {  // 차량 모델
                    carModel = codeListVO.codeList.get(index).getCdName();
                    getBind().tvCarModel.setText(carModel);
                }
                else if(groupCd.equals(AppDef.GroupCode.SIDO_CD.toString())) {  // 차량번호 지역
                    carNumberArea = codeListVO.codeList.get(index).getExtCd2();
                    getBind().tvCarNumberArea.setText(carNumberArea);
                }
                else if(groupCd.equals(AppDef.GroupCode.CAR_CAT.toString())) {  // 택시유형
                    carTypeCd = codeListVO.codeList.get(index).getCd();
                    carType = codeListVO.codeList.get(index).getCdName();
                    getBind().tvCarType.setText(carType);
                }

                validNextCheck();
            }
        }, 2, selectStr);
        selectDialog.show();
    }

    private void SetSubCodeSelectPopup(final String title, final String toastText)
    {
        popupToastText = toastText;

        if(selectDialog != null && selectDialog.isShowing())
            selectDialog.dismiss();

        if(codeSubList == null) {
            Logger.d("LOG1 : subCode data null");
            return;
        }

        String[] name = new String[codeSubList.size()];
        for(int i = 0; i < codeSubList.size(); i++) {
            name[i] = codeSubList.get(i).getCdName();
        }

        selectDialog = new SelectItemDialog(SignupAdditionalInformation.this, title, name, new SelectItemDialog.OnSelectItemDialogListener()
        {
            @Override
            public void onClick(int index) {
                if(index < 0) {
                    Toast.makeText(SignupAdditionalInformation.this, popupToastText, Toast.LENGTH_SHORT).show();
                    return;
                }
                selectDialog.dismiss();

                carModelCd = codeSubList.get(index).getCd();
                carModel = codeSubList.get(index).getCdName();
                getBind().tvCarModel.setText(carModel);

                validNextCheck();
            }
        }, 2, carModel);
        selectDialog.show();
    }

    private void showCommonDialog(String msg) {
        final Dialog dialog = new MacaronCustomDialog(this, getResources().getString(R.string.app_name), msg, getString(R.string.confirm)
                , view -> {}, false, true);

        if(!isFinishing()) dialog.show();
    }

    private void registChauffeurAddInfo() {
        playLoadingViewAnimation();

        HashMap<String, Object> params = new HashMap<>();
        params.put("appToken", PrefUtil.getPushKey(this));                // 앱토큰
        params.put("mobileNo", PrefUtil.getRegPhoneNo(this));             // 등록전화번호
        params.put("bankCd",  bankCd);                                             // 은행코드
        params.put("depositor", getBind().etName.getText().toString());            // 예금주명
        params.put("acntNo", getBind().etBankAccountNumber.getText().toString());  // 계좌번호
        params.put("autmakeCd", carCompanyCd);                                     // 차량제조사코드
        params.put("carmodlCd", carModelCd);                                       // 차량모델코드
        params.put("carNo", carNumberArea + getBind().etCarNumber.getText().toString());    // 차량번호
        params.put("carCat", carTypeCd);                                           // 차량유형 (MIDSIZE:중형, FULLSIZE:대형, MOBUM:모범, BLACK:블랙)

        DataInterface.getInstance().registChauffeurAddInfo(this, params, new DataInterface.ResponseCallback<ResponseData<Object>>() {
            @Override
            public void onSuccess(ResponseData<Object> response) {
                cancelLoadingViewAnimation();

                if(response == null || isFinishing()) return;
                if(!response.getResultCode().equals("S000")) {
                    showCustomDialog(response.getError());
                    return;
                }

                Intent intent = new Intent(SignupAdditionalInformation.this, SignupRequestInformation.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("information_finish", true);
                intent.putExtra("svcStatus", getResources().getString(R.string.signup_complete_informaiton_chauffeur));

                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
            }

            @Override
            public void onError(ResponseData<Object> response) { cancelLoadingViewAnimation(); }

            @Override
            public void onFailure(Throwable t) { cancelLoadingViewAnimation(); }
        });
    }

    private void showCustomDialog(String msg) {
        final Dialog dialog = new MacaronCustomDialog(this, getResources().getString(R.string.app_name), msg, getString(R.string.confirm)
                , view -> {}, false, true);

        if(!isFinishing()) dialog.show();
    }

    @Override
    public void onBackPressed() {
        if(chauffeurStatusVO != null && chauffeurStatusVO.getChauffeurRjctInfoList() != null) {
            // 보류항목에서 바로 이전 항목인 사진등록이 보류 되었으면 이전으로 보내준다.
            for(String tmp : chauffeurStatusVO.getChauffeurRjctInfoList()) {
                if(tmp.equals(AppDef.ChauffeurRegInfoCat.PROFILE.toString())) {
                    getRegistChauffeurInfo(SignupAdditionalInformation.this, AppDef.ChauffeurRegInfoCat.PROFILE.toString(), joinType, chauffeurStatusVO, svcStatus,true);
                    return;
                }
            }

            // 보류항목에서 처음 항목인 기본정보 등록이 보류 되었으면 기본정보 등록으로 보내준다.
            for(String tmp : chauffeurStatusVO.getChauffeurRjctInfoList()) {
                if(tmp.equals(AppDef.ChauffeurRegInfoCat.MEMBER.toString())) {
                    getRegistChauffeurInfo(SignupAdditionalInformation.this, AppDef.ChauffeurRegInfoCat.MEMBER.toString(), joinType, chauffeurStatusVO, svcStatus,true);
                    return;
                }
            }

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
        else {
            getRegistChauffeurInfo(SignupAdditionalInformation.this, AppDef.ChauffeurRegInfoCat.PROFILE.toString(), joinType, chauffeurStatusVO, svcStatus,true);
        }

    }
}
