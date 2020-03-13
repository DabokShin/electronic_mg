package kst.ksti.chauffeur.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.jakewharton.rxbinding3.widget.RxTextView;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Pattern;
import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.common.AnalyticsHelper;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.databinding.ActivityProfileBinding;
import kst.ksti.chauffeur.model.ChauffeurInfo;
import kst.ksti.chauffeur.net.DataInterface;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.PrefUtil;
import kst.ksti.chauffeur.utility.Util;

public class ProfileActivity extends BaseActivity<ActivityProfileBinding> {

    private boolean isExpanded = false;
    private ChauffeurInfo chauffeurInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.d(getResources().getString(R.string.begin_activity));    // 액티비티 호출 로그

        setBind(R.layout.activity_profile);

        Thread.setDefaultUncaughtExceptionHandler(((MacaronApp)getApplication()).getUncaughtExceptionHandler(ProfileActivity.this));

        initView();
        setViewEventBind();
        setRxEventBind();

        getChauffeurInfo();
    }

    private void initView() {
        setLoadingBarLayout(getBind().loadingBarLayout);
        setProgressBar(getBind().progressBar);
        getBind().toolbar.btnDrawerOpen.setVisibility(View.GONE);
        getBind().toolbar.tvTitle.setText(getResources().getString(R.string.profile_title));
    }

    private void bindData() {
        if(chauffeurInfo == null) return;

        Fade fade = new Fade();
        fade.setStartDelay(0L);

        TransitionManager.beginDelayedTransition(getBind().viewRoot, fade);
        getBind().viewRoot.setVisibility(View.VISIBLE);

        getBind().textviewName.setText(chauffeurInfo.name);
        getBind().accountText.setText(chauffeurInfo.id);
        getBind().edittextPassword.setText(PrefUtil.getLoginPwd(this));
        getBind().phoneNumberText.setText(chauffeurInfo.mobileNo);

        if(chauffeurInfo.carVo != null) {
            getBind().carNumText.setText(chauffeurInfo.carVo.carNo);
            getBind().carTypeText.setText(AppDef.ResvCarCat.getSizeByValue(chauffeurInfo.carVo.carCat));

            StringBuilder builder = new StringBuilder();
            if(chauffeurInfo.carVo.company != null) builder.append(chauffeurInfo.carVo.company).append(" / ");
            if(chauffeurInfo.carVo.modelName != null) builder.append(chauffeurInfo.carVo.modelName);

            getBind().carModelText.setText(builder.toString());
        }
        else showCarLimitView();

        Glide.with(this)
                .load(chauffeurInfo.imgUrl)
                .apply(new RequestOptions().circleCrop())
                .into(getBind().btnProfile);

        if(Objects.equals(chauffeurInfo.companyType, "CORPORATE")) bindCorporateData();
        else bindIndividualData();
    }

    private void bindIndividualData() {
        getBind().viewBank.setVisibility(View.VISIBLE);
        getBind().viewLegal.setVisibility(View.GONE);

        getBind().bankText.setText(chauffeurInfo.bankName == null ? "" : chauffeurInfo.bankName);
        getBind().bankNumberText.setText(chauffeurInfo.acntNo == null ? "" : chauffeurInfo.acntNo);
        getBind().bankNameText.setText(chauffeurInfo.depositor == null ? "" : chauffeurInfo.depositor);
        getBind().carInfoGuide.setText(getResources().getString(R.string.profile_car_info_individual_warning));
    }

    private void bindCorporateData() {
        getBind().viewLegal.setVisibility(View.VISIBLE);
        getBind().viewBank.setVisibility(View.GONE);

        getBind().legalText.setText(chauffeurInfo.companyName == null ? "" : chauffeurInfo.companyName);
        getBind().legalNumberText.setText(chauffeurInfo.companyPhoneNo == null ? "" : chauffeurInfo.companyPhoneNo);
    }

    private void setViewEventBind() {
        getBind().toolbar.btnTitleBack.setOnClickListener(v -> finish());
        getBind().btnModify.setOnClickListener(v -> {
            if(!checkPasswordValidate()) return;

            //request password update
            requestPasswordUpdate();
        });

        getBind().btnPasswordChange.setOnClickListener(v -> expandPasswordView(!isExpanded));
    }

    private void setRxEventBind() {
        baseDisposable.add(RxTextView.textChanges(getBind().edittextNewPassword)
                .map(it -> isValidPassword(it.toString()))
                .subscribe(selected -> {
                    if(selected == null) return;

                    getBind().edittextNewPassword.setSelected(selected);
                }, Throwable::printStackTrace));

        baseDisposable.add(RxTextView.textChanges(getBind().edittextNewPasswordConfirm)
                .map(it -> it.toString().equals(getBind().edittextNewPassword.getText().toString()) && isValidPassword(it.toString()))
                .subscribe(selected -> {
                    if(selected == null) return;

                    getBind().edittextNewPasswordConfirm.setSelected(selected);
                }, Throwable::printStackTrace));
    }

    private void showCarLimitView() {
        getBind().invisibleGroup.setVisibility(View.VISIBLE);
        getBind().carNum.setTextColor(Color.parseColor("#cccccc"));
        getBind().carType.setTextColor(Color.parseColor("#cccccc"));
        getBind().carModel.setTextColor(Color.parseColor("#cccccc"));
    }

    private void expandPasswordView(boolean isExpanded) {
        if(this.isExpanded == isExpanded) return;

        this.isExpanded = isExpanded;

        getBind().toolbar.tvTitle.setText(isExpanded ? getResources().getString(R.string.profile_title_modify) : getResources().getString(R.string.profile_title));
        getBind().edittextPassword.setFocusable(isExpanded);
        getBind().edittextPassword.setFocusableInTouchMode(isExpanded);

        TransitionManager.beginDelayedTransition(getBind().viewRoot);
        getBind().viewExpanded.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        getBind().btnModify.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        if(isExpanded) {
            getBind().edittextPassword.setText("");
            getBind().edittextPassword.requestFocus();
            getBind().btnPasswordChange.setText(getResources().getString(R.string.txt_cancel));
            return;
        }

        getBind().edittextPassword.setText(PrefUtil.getLoginPwd(this));
        getBind().edittextNewPassword.setText("");
        getBind().edittextNewPasswordConfirm.setText("");
        getBind().btnPasswordChange.setText(getResources().getString(R.string.txt_change));

        new Handler().postDelayed(() -> Util.hideKeyboard(this, getBind().edittextPassword), 300);
    }

    private boolean checkPasswordValidate() {
        if(getBind().edittextPassword.getText().toString().isEmpty()) {
            Toast.makeText(this, getResources().getString(R.string.profile_password_empty), Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!PrefUtil.getLoginPwd(this).equals(getBind().edittextPassword.getText().toString())) {
            Toast.makeText(this, getResources().getString(R.string.profile_password_not_match), Toast.LENGTH_SHORT).show();
            return false;
        }

        if(getBind().edittextNewPassword.getText().toString().isEmpty()) {
            Toast.makeText(this, getResources().getString(R.string.profile_new_password_empty), Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!getBind().edittextNewPassword.isSelected() || !getBind().edittextNewPasswordConfirm.isSelected()) {
            Toast.makeText(this, getResources().getString(R.string.profile_password_warning), Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!getBind().edittextNewPassword.getText().toString().equals(getBind().edittextNewPasswordConfirm.getText().toString())) {
            Toast.makeText(this, getResources().getString(R.string.profile_new_password_not_match), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean isValidPassword(String pwd) {
        String regex = "^[a-zA-Z0-9]+$";

        boolean c = Pattern.compile(regex).matcher(pwd).matches();
        boolean l = Pattern.compile(".{4,16}").matcher(pwd).matches();

        return c && l;
    }

    private void getChauffeurInfo() {
        playLoadingViewAnimation();

        DataInterface.getInstance().getChauffeurInfo(this, new HashMap<>(), new DataInterface.ResponseCallback<ResponseData<ChauffeurInfo>>() {
            @Override
            public void onSuccess(ResponseData<ChauffeurInfo> response) {
                cancelLoadingViewAnimation();

                if(response == null || isFinishing()) return;
                if(!response.getResultCode().equals("S000")) return;

                chauffeurInfo = response.getData();
                bindData();
            }

            @Override
            public void onError(ResponseData<ChauffeurInfo> response) { cancelLoadingViewAnimation(); }

            @Override
            public void onFailure(Throwable t) { cancelLoadingViewAnimation(); }
        });
    }

    private void requestPasswordUpdate() {
        playLoadingViewAnimation();

        HashMap<String, Object> params = new HashMap<>();
        params.put("orgPwd", getBind().edittextPassword.getText().toString());
        params.put("pwd", getBind().edittextNewPassword.getText().toString());

        DataInterface.getInstance().updateUserInfo(this, params, new DataInterface.ResponseCallback<ResponseData<Object>>() {
            @Override
            public void onSuccess(ResponseData<Object> response) {
                cancelLoadingViewAnimation();

                if(response == null || isFinishing()) return;

                if(!response.getResultCode().equals("S000")) {
                    Toast.makeText(getApplicationContext(), response.getError(), Toast.LENGTH_SHORT).show();
                    return;
                }

                PrefUtil.setLoginPwd(ProfileActivity.this, (String) params.get("pwd"));
                Toast.makeText(ProfileActivity.this, getResources().getString(R.string.profile_password_change_complete), Toast.LENGTH_SHORT).show();
                expandPasswordView(false);
            }

            @Override
            public void onError(ResponseData<Object> response) { cancelLoadingViewAnimation(); }

            @Override
            public void onFailure(Throwable t) { cancelLoadingViewAnimation(); }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        AnalyticsHelper.getInstance(this).sendScreenFromJson(this, ProfileActivity.class.getSimpleName());
    }
}
