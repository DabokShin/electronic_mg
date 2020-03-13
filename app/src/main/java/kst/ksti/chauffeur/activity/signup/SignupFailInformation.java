package kst.ksti.chauffeur.activity.signup;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import kst.ksti.chauffeur.common.Global;
import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.activity.BaseActivity;
import kst.ksti.chauffeur.activity.LoginActivity;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.databinding.ActivitySignupFailInformationBinding;
import kst.ksti.chauffeur.model.CompanyStatusVO;
import kst.ksti.chauffeur.model.CompanyVO;
import kst.ksti.chauffeur.model.signup.ChauffeurStatusVO;
import kst.ksti.chauffeur.model.signup.RegistChauffeurInfoVO;
import kst.ksti.chauffeur.net.DataInterface;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.MacaronCustomDialog;
import kst.ksti.chauffeur.utility.OnSingleClickListener;
import kst.ksti.chauffeur.utility.PrefUtil;

/**
 * 가입신청 실패 화면
 */
public class SignupFailInformation extends BaseActivity<ActivitySignupFailInformationBinding> {

    private final int REASON_TEXT_SIZE = 17;
    private ChauffeurStatusVO chauffeurStatusVO = null;
    private CompanyStatusVO companyStatusVO = null;
    private String svcStatus = null;
    private String chauffeurRegInfoCat = null;
    private String joinType = null;
    private String id = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.d(getResources().getString(R.string.begin_activity));    // 액티비티 호출 로그

        Thread.setDefaultUncaughtExceptionHandler(((MacaronApp)getApplication()).getUncaughtExceptionHandler(SignupFailInformation.this));

        setBind(R.layout.activity_signup_fail_information);

        getBind().title.btnTitleBack.setVisibility(View.GONE);
        getBind().title.btnDrawerOpen.setVisibility(View.GONE);

        Intent intent = getIntent();
        if (intent != null) {
            getBind().title.tvTitle.setText("가입신청 확인");
            getBind().success.setVisibility(View.GONE);
            getBind().fail.setVisibility(View.GONE);

            joinType = intent.getStringExtra("joinType");

            // 쇼퍼
            if(joinType.equals(Global.JOIN_TYPE.CHAUFFEUR)) {
                chauffeurStatusVO = (ChauffeurStatusVO)intent.getSerializableExtra("chauffeurStatusVO");
                svcStatus = intent.getStringExtra("svcStatus");
                id = intent.getStringExtra("id");

                switch(svcStatus) {
                    case Global.SVC_STATUS.AVAILABLE:   // 이용가능
                        getBind().success.setVisibility(View.VISIBLE);
                        getBind().tvInformation.setText(getString(R.string.signup_complete_informaiton_success));
                        getBind().tvChauffeurID.setText("- 아이디 : " + id);
                        getBind().btnConfirm.setText("로그인");

                        // 로그인 아이디, 전화번호 저장
                        saveLoginData(id, PrefUtil.getRegPhoneNo(SignupFailInformation.this).replaceAll("-", ""));
                        break;
                    case Global.SVC_STATUS.INFORJCT:    // 정보승인보류
                    {
                        getBind().fail.setVisibility(View.VISIBLE);
                        getBind().tvInformation.setText(getString(R.string.signup_complete_informaiton_inforjct));
                        getBind().btnConfirm.setText("정보 수정하기");

                        chauffeurRegInfoCat = intent.getStringExtra("chauffeurRegInfoCat");

                        // 보류 내용 작성
                        if(chauffeurStatusVO.getCitList() != null)
                            setTableRowChauffeur(chauffeurStatusVO.getCitList());

                        if(chauffeurStatusVO.getReason() != null) {
                            setTableRowReason(chauffeurStatusVO.getReason());
                        }

                        break;
                    }
                    case Global.SVC_STATUS.CONTRJCT:    // 계약승인보류
                    {
                        getBind().fail.setVisibility(View.VISIBLE);
                        getBind().tvInformation.setText(getString(R.string.signup_complete_informaiton_inforjct));
                        getBind().btnConfirm.setText("확인");

                        // 보류 내용 작성
                        if(chauffeurStatusVO.getCitList() != null)
                            setTableRowChauffeur(chauffeurStatusVO.getCitList());

                        if(chauffeurStatusVO.getReason() != null) {
                            setTableRowReason(chauffeurStatusVO.getReason());
                        }

                        break;
                    }
                }
            }
            else if(joinType.equals(Global.JOIN_TYPE.COMPANY)) {
                companyStatusVO = (CompanyStatusVO) getIntent().getSerializableExtra("companyStatusVO");

                if(companyStatusVO != null) {
                    svcStatus = companyStatusVO.getSvcStatus();
                    switch(companyStatusVO.getSvcStatus())
                    {
                        case Global.SVC_STATUS.AVAILABLE:   // 이용가능
                            //getBind().success.setVisibility(View.VISIBLE);
                            getBind().tvInformation.setText(getString(R.string.signup_complete_informaiton_success_company));
                            getBind().tvChauffeurID.setText(intent.getStringExtra(""));
                            getBind().btnConfirm.setText("로그인");

                            // 로그인 아이디, 전화번호 저장
                            saveLoginData(getBind().tvChauffeurID.getText().toString(), PrefUtil.getRegPhoneNo(SignupFailInformation.this).replaceAll("-", ""));
                            break;
                        case Global.SVC_STATUS.INFORJCT:    // 정보승인보류
                        case Global.SVC_STATUS.CONTRJCT:    // 계약승인보류
                        {
                            getBind().fail.setVisibility(View.VISIBLE);
                            getBind().tvInformation.setText(getString(R.string.signup_complete_informaiton_inforjct_company));

                            if(companyStatusVO.getSvcStatus().equals(Global.SVC_STATUS.INFORJCT)) {
                                getBind().btnConfirm.setText("정보 수정하기");
                            }
                            else {
                                getBind().btnConfirm.setText("확인");
                            }

                            // 보류 내용 작성
                            if(companyStatusVO.getCitList() != null)
                                setTableRowCompany(companyStatusVO.getCitList());

                            if(companyStatusVO.getReason() != null) {
                                setTableRowReason(companyStatusVO.getReason());
                            }

                            break;
                        }
                    }
                }
            }
        }

        // 회사로 전화
        getBind().callCompany.setOnClickListener(onSingleClickListener);
        getBind().callCompany.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);

        getBind().btnAppFinish.setOnClickListener(onSingleClickListener);
        getBind().btnConfirm.setOnClickListener(onSingleClickListener);
    }

    private void saveLoginData(String id, String phoneNum) {
        // 로그인 아이디, 전화번호 저장
        PrefUtil.setLoginId(SignupFailInformation.this, id);
        PrefUtil.setLoginPhoneNo(SignupFailInformation.this, phoneNum);

        // 이용 가능 데이터를 받는 순간
        // 다음에 접속 했을 시 로그인 페이지로 가도록 해준다.
        PrefUtil.setRegPhoneNo(SignupFailInformation.this, "");
        PrefUtil.setRegJoinType(SignupFailInformation.this, "");
    }

    private void setTableRowChauffeur(List<ChauffeurStatusVO.CitList> list) {

        for(int i = 0; i < list.size(); i++) {

            TableRow tableRow = new TableRow(this);
            TextView tv1 = new TextView(this);
            TextView tv2 = new TextView(this);
            tv1.setText(" -");
            tv1.setTextColor(Color.parseColor("#ff656565"));
            tv1.setTextSize(REASON_TEXT_SIZE);
            tv1.setTypeface(null, Typeface.BOLD);
            tv2.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            tv2.setText(list.get(i).getChauffeurIncorrectinfoText());
            tv2.setTextColor(Color.parseColor("#ff656565"));
            tv2.setTextSize(REASON_TEXT_SIZE);
            tv2.setTypeface(null, Typeface.BOLD);
            tv2.setPadding(10, 0, 0, 0);
            tableRow.setPadding(0, 0, 0, 0);
            tableRow.addView(tv1);
            tableRow.addView(tv2);

            getBind().tlReason.addView(tableRow);
        }

        getBind().tlReason.setVisibility(View.VISIBLE);
    }

    private void setTableRowCompany(List<CompanyStatusVO.CitList> list) {

        for(int i = 0; i < list.size(); i++) {

            TableRow tableRow = new TableRow(this);
            TextView tv1 = new TextView(this);
            TextView tv2 = new TextView(this);
            tv1.setText(" -");
            tv1.setTextColor(Color.parseColor("#ff656565"));
            tv1.setTextSize(REASON_TEXT_SIZE);
            tv1.setTypeface(null, Typeface.BOLD);
            tv2.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            tv2.setText(list.get(i).getCompanyIncorrectinfoText());
            tv2.setTextColor(Color.parseColor("#ff656565"));
            tv2.setTextSize(REASON_TEXT_SIZE);
            tv2.setTypeface(null, Typeface.BOLD);
            tv2.setPadding(10, 0, 0, 0);
            tableRow.setPadding(0, 0, 0, 0);
            tableRow.addView(tv1);
            tableRow.addView(tv2);

            getBind().tlReason.addView(tableRow);
        }

        getBind().tlReason.setVisibility(View.VISIBLE);
    }

    private void setTableRowReason(String reason) {
        TableRow subTitleTableRow = new TableRow(this);
        TextView subTitleTv1 = new TextView(this);
        TextView subTitleTv2 = new TextView(this);

        subTitleTv1.setText("※");
        subTitleTv1.setTextColor(Color.parseColor("#ff656565"));
        subTitleTv1.setTextSize(REASON_TEXT_SIZE);
        subTitleTv1.setTypeface(null, Typeface.BOLD);
        subTitleTv2.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        subTitleTv2.setText("기타사유");
        subTitleTv2.setTextColor(Color.parseColor("#ff656565"));
        subTitleTv2.setTextSize(REASON_TEXT_SIZE);
        subTitleTv2.setTypeface(null, Typeface.BOLD);
        subTitleTv2.setPadding(10, 0, 0, 0);
        subTitleTableRow.setPadding(0, 35, 0, 0);
        subTitleTableRow.addView(subTitleTv1);
        subTitleTableRow.addView(subTitleTv2);

        getBind().tlReason.addView(subTitleTableRow);

        TableRow tableRow = new TableRow(this);
        TextView tv1 = new TextView(this);
        TextView tv2 = new TextView(this);

        tv1.setText(" -");
        tv1.setTextColor(Color.parseColor("#ff656565"));
        tv1.setTextSize(REASON_TEXT_SIZE);
        tv1.setTypeface(null, Typeface.BOLD);
        tv2.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tv2.setText(reason);
        tv2.setTextColor(Color.parseColor("#ff656565"));
        tv2.setTextSize(REASON_TEXT_SIZE);
        tv2.setTypeface(null, Typeface.BOLD);
        tv2.setPadding(10, 0, 0, 0);
        tableRow.setPadding(0, 0, 0, 0);
        tableRow.addView(tv1);
        tableRow.addView(tv2);

        getBind().tlReason.addView(tableRow);
    }

    private OnSingleClickListener onSingleClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            switch (v.getId()) {
                case R.id.callCompany: {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Global.COMPANY_PHONENO));
                    startActivity(intent);
                }
                    break;
                case R.id.btnAppFinish:
                    finishAffinity();
                    break;
                case R.id.btnConfirm:
                {
                    Intent intent = new Intent();
                    switch(svcStatus) {
                        case Global.SVC_STATUS.AVAILABLE:   // 이용가능
                            PrefUtil.setRegPhoneNo(SignupFailInformation.this, "");
                            PrefUtil.setRegJoinType(SignupFailInformation.this, "");
                            intent.setClass(SignupFailInformation.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                            finish();
                            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                            break;
                        case Global.SVC_STATUS.INFORJCT:    // 정보승인보류
                        {
                            if(joinType.equals(Global.JOIN_TYPE.CHAUFFEUR))
                                getRegistChauffeurInfo(chauffeurRegInfoCat);
                            else {
                                getCompany(companyStatusVO.getCompanyIdx());
                            }
                            break;
                        }
                        case Global.SVC_STATUS.CONTRJCT:    // 계약승인보류
                        {
                            finishAffinity();
                            break;
                        }
                    }
                    break;
                }
            }
        }
    };

    private void getRegistChauffeurInfo(String reginfoCat) {
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
                    showCommonDialog(response.getError());
                    return;
                }

                Intent intent = new Intent();
                if(response.getData().getChauffeurRegInfoCat().equals(AppDef.ChauffeurRegInfoCat.MEMBER.toString())) {
                    intent.setClass(SignupFailInformation.this, SignupBasicInfomation.class);

                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    // 기본정보 데이터가 없다
                    if(response.getData().getRcbi() != null)
                        intent.putExtra("registChauffeurInfoVO", response.getData());

                    intent.putExtra("joinType", joinType);
                    intent.putExtra("chauffeurStatusVO", chauffeurStatusVO);
                    intent.putExtra("svcStatus", svcStatus);
                    intent.putExtra("chauffeurRegInfoCat", chauffeurRegInfoCat);

                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                }
                else if(response.getData().getChauffeurRegInfoCat().equals(AppDef.ChauffeurRegInfoCat.PROFILE.toString())) {
                    intent.setClass(SignupFailInformation.this, SignupPictureRegistration.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    // 사진 데이터가 없다
                    if(response.getData().getRcpi() != null && response.getData().getRcpi2() != null)
                        intent.putExtra("registChauffeurInfoVO", response.getData());

                    intent.putExtra("joinType", joinType);
                    intent.putExtra("chauffeurStatusVO", chauffeurStatusVO);
                    intent.putExtra("svcStatus", svcStatus);
                    intent.putExtra("chauffeurRegInfoCat", chauffeurRegInfoCat);

                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                }
                else if(response.getData().getChauffeurRegInfoCat().equals(AppDef.ChauffeurRegInfoCat.ETC.toString())) {
                    intent.setClass(SignupFailInformation.this, SignupAdditionalInformation.class);

                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    // 기본정보 데이터가 없다
                    if(response.getData().getRcai() != null)
                        intent.putExtra("registChauffeurInfoVO", response.getData());

                    intent.putExtra("joinType", joinType);
                    intent.putExtra("chauffeurStatusVO", chauffeurStatusVO);
                    intent.putExtra("svcStatus", svcStatus);
                    intent.putExtra("chauffeurRegInfoCat", chauffeurRegInfoCat);

                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                }
            }

            @Override
            public void onError(ResponseData<RegistChauffeurInfoVO> response) { cancelLoadingViewAnimation(); }

            @Override
            public void onFailure(Throwable t) { cancelLoadingViewAnimation(); }
        });
    }

    private void getCompany(long companyIdx) {
        playLoadingViewAnimation();

        HashMap<String, Object> params = new HashMap<>();
        params.put("companyIdx", companyIdx);

        DataInterface.getInstance().getCompany(this, params, new DataInterface.ResponseCallback<ResponseData<CompanyVO>>() {
            @Override
            public void onSuccess(ResponseData<CompanyVO> response) {
                cancelLoadingViewAnimation();

                if(response == null || isFinishing()) return;
                if(!response.getResultCode().equals("S000")) {
                    showCommonDialog(response.getError());
                    return;
                }

                Intent intent = new Intent();
                intent.setClass(SignupFailInformation.this, CorporationRegisterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("phoneNum", PrefUtil.getRegPhoneNo(SignupFailInformation.this));
                intent.putExtra("companyStatusVO", companyStatusVO);
                intent.putExtra("companyVO", response.getData());
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
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
    public void onBackPressed() {
        finishAffinity();
    }
}
