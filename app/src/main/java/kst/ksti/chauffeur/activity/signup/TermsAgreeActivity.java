package kst.ksti.chauffeur.activity.signup;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import java.util.HashMap;

import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.activity.BaseActivity;
import kst.ksti.chauffeur.activity.LoginActivity;
import kst.ksti.chauffeur.activity.WebViewActivity;
import kst.ksti.chauffeur.common.AnalyticsHelper;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.databinding.ActivityTermsBinding;
import kst.ksti.chauffeur.model.signup.RegistChauffeurInfoVO;
import kst.ksti.chauffeur.net.DataInterface;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.MacaronCustomDialog;
import kst.ksti.chauffeur.utility.PrefUtil;

public class TermsAgreeActivity extends BaseActivity<ActivityTermsBinding> implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBind(R.layout.activity_terms);

        Thread.setDefaultUncaughtExceptionHandler(((MacaronApp)getApplication()).getUncaughtExceptionHandler(TermsAgreeActivity.this));

        initView();
        setViewEventBind();

        if(savedInstanceState != null) validateCheck();
    }

    private void initView() {
        getBind().toolbar.btnDrawerOpen.setVisibility(View.GONE);
        getBind().toolbar.tvTitle.setText(getResources().getString(R.string.terms_agree_title));
        getBind().textviewAllAgree.setText(Html.fromHtml(getResources().getString(R.string.terms_agree_all)));
    }

    private void setViewEventBind() {
        getBind().viewTermsPrivate.setOnClickListener(this);
        getBind().viewTermsAllocation.setOnClickListener(this);
        getBind().viewTermsPay.setOnClickListener(this);
        getBind().viewTermsLocation.setOnClickListener(this);
        getBind().viewTermsChauffeur.setOnClickListener(this);
        getBind().viewTermsCurrent.setOnClickListener(this);

        getBind().checkboxPrivate.setOnClickListener(this);
        getBind().checkboxPay.setOnClickListener(this);
        getBind().checkboxPay.setOnClickListener(this);
        getBind().checkboxLocation.setOnClickListener(this);
        getBind().checkboxChauffeur.setOnClickListener(this);
        getBind().checkboxCurrent.setOnClickListener(this);

        getBind().viewTermsAll.setOnClickListener(v -> getBind().checkboxAll.performClick());

        getBind().checkboxAll.setOnClickListener(v -> {
            CheckBox box = (CheckBox) v;

            getBind().checkboxPrivate.setChecked(box.isChecked());
            getBind().checkboxAllocation.setChecked(box.isChecked());
            getBind().checkboxPay.setChecked(box.isChecked());
            getBind().checkboxLocation.setChecked(box.isChecked());
            getBind().checkboxChauffeur.setChecked(box.isChecked());
            getBind().checkboxCurrent.setChecked(box.isChecked());

            validateCheck();
        });

        getBind().textviewTermsNext.setOnClickListener(v -> {
            getRegistChauffeurInfo(AppDef.ChauffeurRegInfoCat.MEMBER.toString());
        });

        getBind().toolbar.btnTitleBack.setOnClickListener(v -> backProcess());
    }

    private void validateCheck() {
        getBind().checkboxAll.setChecked(isAllChecked());
        getBind().textviewTermsNext.setEnabled(getBind().checkboxAll.isChecked());
    }

    private boolean isAllChecked() {
        return getBind().checkboxPrivate.isChecked()
                && getBind().checkboxAllocation.isChecked()
                && getBind().checkboxPay.isChecked()
                && getBind().checkboxLocation.isChecked()
                && getBind().checkboxChauffeur.isChecked()
                && getBind().checkboxCurrent.isChecked();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AnalyticsHelper.getInstance(this).sendScreenFromJson(this, TermsAgreeActivity.class.getSimpleName());
    }

    @Override
    public void onClick(View v) {
        if(v instanceof ViewGroup) {
            int ordinal = Integer.parseInt((String) v.getTag());

            Intent intent = new Intent(this, WebViewActivity.class);
            intent.putExtra("targetUrl", AppDef.TermsUrls.getUrlByOrdinal(ordinal));
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

            startActivity(intent);
            return;
        }

        validateCheck();
    }

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
                intent.setClass(TermsAgreeActivity.this, SignupBasicInfomation.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                // 기본정보 데이터가 있다.
                if(response.getData().getRcbi() != null)
                    intent.putExtra("registChauffeurInfoVO", response.getData());

                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
            }

            @Override
            public void onError(ResponseData<RegistChauffeurInfoVO> response) {
                cancelLoadingViewAnimation();
                showCommonDialog(response.getError());
            }

            @Override
            public void onFailure(Throwable t) {
                cancelLoadingViewAnimation();
                Logger.d("LOG1  : 이용약관 통신 오류");
            }
        });
    }

    private void showCommonDialog(String msg) {
        final Dialog dialog = new MacaronCustomDialog(this, getResources().getString(R.string.app_name), msg, getString(R.string.confirm)
                , view -> {}, false, true);

        if(!isFinishing()) dialog.show();
    }

    private void backProcess() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        backProcess();
    }
}
