package kst.ksti.chauffeur.activity.signup;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;

import kst.ksti.chauffeur.common.Global;
import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.activity.BaseActivity;
import kst.ksti.chauffeur.activity.LoginActivity;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.databinding.ActivitySignupRequestInformationBinding;
import kst.ksti.chauffeur.model.CompanyStatusVO;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.OnSingleClickListener;

/**
 *  가입신청 완료 화면
 */
public class SignupRequestInformation extends BaseActivity<ActivitySignupRequestInformationBinding> {

    private CompanyStatusVO companyStatusVO = null;
    private boolean companyRequestComplete = false; // 회사 신청 완료 (처음 신청 버튼 누르 후)
    private String joinType = null;
    private boolean isForceFinish = false;
    private String svcStatus = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.d(getResources().getString(R.string.begin_activity));    // 액티비티 호출 로그

        Thread.setDefaultUncaughtExceptionHandler(((MacaronApp)getApplication()).getUncaughtExceptionHandler(SignupRequestInformation.this));

        setBind(R.layout.activity_signup_request_information);

        initData();
        initView();
        setViewEventBind();
    }

    private void initData() {
        if(getIntent() != null && getIntent().getExtras() != null) {
            companyStatusVO = (CompanyStatusVO) getIntent().getSerializableExtra("companyStatusVO");
            joinType = getIntent().getStringExtra("joinType");
            isForceFinish = getIntent().getBooleanExtra("isForceFinish", false);
            companyRequestComplete = getIntent().getBooleanExtra("companyRequestComplete", false);
        }

        if(companyStatusVO != null || (joinType != null && joinType.equals(Global.JOIN_TYPE.COMPANY))) {
            bindCompanyData();
            return;
        }

        // 쇼퍼 가입 상태에 따른 데이터 바인딩
        if(getIntent() != null && getIntent().getExtras() != null) {
            bindChauffeurData();
            return;
        }

        getBind().title.tvTitle.setText("신청 확인 안내");
        getBind().tvInformation.setText(AppDef.ChauffeurSvcStatus.getMessageByStatus(AppDef.ChauffeurSvcStatus.AVAILABLE.toString()));
        getBind().tvInformation2.setText(Html.fromHtml(getString(R.string.approval_contents_chauffeur)));
    }

    private void initView() {
        setLoadingBarLayout(getBind().loadingBarLayout);
        setProgressBar(getBind().progressBar);

        getBind().title.btnTitleBack.setVisibility(View.GONE);
        getBind().title.btnDrawerOpen.setVisibility(View.GONE);
    }

    private void setViewEventBind() {

        // 회사로 전화
        getBind().callCompany.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Global.COMPANY_PHONENO));
                startActivity(intent);
            }
        });

        getBind().callCompany.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);

        getBind().btnConfirm.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if(checkReRequest()) return;

                if(isForceFinish) {
                    finishAffinity();
                    return;
                }

                Intent intent = new Intent(SignupRequestInformation.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    private void bindCompanyData() {
        getBind().title.tvTitle.setText("신청 확인 안내");
        if(companyStatusVO != null)
            getBind().tvInformation.setText(AppDef.CompanySvcStatus.getMessageByStatus(companyStatusVO.getSvcStatus()));
        else if(companyRequestComplete)
            getBind().tvInformation.setText(getResources().getString(R.string.signup_complete_informaiton_company));
        else
            getBind().tvInformation.setText(AppDef.CompanySvcStatus.getMessageByStatus(Global.SVC_STATUS.REQUEST));
        getBind().tvInformation2.setText(Html.fromHtml(getString(R.string.approval_contents)));
    }

    private void bindChauffeurData() {
        isForceFinish = getIntent().getExtras().getBoolean("information_finish", false);
        svcStatus = getIntent().getExtras().getString("svcStatus", Global.SVC_STATUS.REQUEST);

        getBind().title.tvTitle.setText("신청 확인 안내");

        switch(svcStatus) {
            case Global.SVC_STATUS.REQUEST:     // 승인요청
            case Global.SVC_STATUS.REREQUEST:   // 재승인요청
            case Global.SVC_STATUS.APPROVED:    // 승인완료
            {
                getBind().tvInformation.setText(AppDef.ChauffeurSvcStatus.getMessageByStatus(AppDef.ChauffeurSvcStatus.REQUEST.toString()));
                break;
            }
            case Global.SVC_STATUS.AVAILABLE:   // 이용가능
            {
                getBind().tvInformation.setText(AppDef.ChauffeurSvcStatus.getMessageByStatus(AppDef.ChauffeurSvcStatus.AVAILABLE.toString()));
                break;
            }
            default:
                getBind().tvInformation.setText(getResources().getString(R.string.signup_complete_informaiton_chauffeur));
                break;
        }

        getBind().tvInformation2.setText(Html.fromHtml(getString(R.string.approval_contents_chauffeur)));
    }

    private boolean checkReRequest() {
        // 법인등록 일 경우 법인 등록 화면 이동
        if(companyStatusVO != null) {
            AppDef.CompanySvcStatus status = AppDef.CompanySvcStatus.getValue(companyStatusVO.getSvcStatus());

            switch (status) {
                case INFORJCT:
                case CONTRJCT:{
                    Intent intent = new Intent(SignupRequestInformation.this, CorporationRegisterActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("companyStatusVO", companyStatusVO);
                    startActivity(intent);
                    return true;
                }
            }
        }

        // 쇼퍼 회원가입일 경우 체크


        return false;
    }

    @Override
    public void onBackPressed() {
        getBind().btnConfirm.performClick();
    }
}
