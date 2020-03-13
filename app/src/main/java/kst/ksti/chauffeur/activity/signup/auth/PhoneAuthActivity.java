package kst.ksti.chauffeur.activity.signup.auth;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.jakewharton.rxbinding3.widget.RxTextView;
import com.jakewharton.rxbinding3.widget.TextViewAfterTextChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.activity.BaseActivity;
import kst.ksti.chauffeur.common.AnalyticsHelper;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.databinding.ActivityPhoneAuthBinding;
import kst.ksti.chauffeur.model.PhoneAuthVO;
import kst.ksti.chauffeur.net.DataInterface;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.utility.AppSignatureHelper;
import kst.ksti.chauffeur.utility.Util;

public class PhoneAuthActivity extends BaseActivity<ActivityPhoneAuthBinding> {

    private boolean mSelfChange = false;

    @Nullable
    private AppDef.AuthType authType = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBind(R.layout.activity_phone_auth);

        Thread.setDefaultUncaughtExceptionHandler(((MacaronApp)getApplication()).getUncaughtExceptionHandler(PhoneAuthActivity.this));

        initData();
        initView();
        setViewEventBind();
        setRxEventBind();
    }

    private void initData() {
        if(getIntent() != null) {
            authType = (AppDef.AuthType) getIntent().getSerializableExtra("authType");
        }
    }

    private void initView() {
        setLoadingBarLayout(getBind().loadingBarLayout);
        setProgressBar(getBind().progressBar);
        getBind().toolbar.btnDrawerOpen.setVisibility(View.GONE);
        getBind().toolbar.tvTitle.setText(getResources().getString(R.string.phone_auth_title));
    }

    private void setViewEventBind() {
        getBind().btnSend.setOnClickListener(v -> {
            // request phone auth
            requestPhoneAuth();
        });

        getBind().btnClear.setOnClickListener(v -> getBind().edittextPhoneNumber.setText(""));
        getBind().toolbar.btnTitleBack.setOnClickListener(v -> finish());
    }

    private void setRxEventBind() {
        baseDisposable.add(RxTextView.afterTextChangeEvents(getBind().edittextPhoneNumber)
                .filter(v-> !mSelfChange)
                .map(TextViewAfterTextChangeEvent::getEditable)
                .subscribe(editable -> {
                    if(editable == null) return;

                    String number = Util.convertHyphenPhoneNumber(editable.toString());

                    if(number == null) return;

                    mSelfChange = true;
                    editable.replace(0, editable.length(), number, 0, number.length());
                    mSelfChange = false;

                    getBind().btnSend.setEnabled(Util.isValidPhoneNumber(number));
                    getBind().btnClear.setVisibility(editable.length() > 0 ? View.VISIBLE : View.GONE);

                }, Throwable::printStackTrace));
    }

    private void requestPhoneAuth() {
        playLoadingViewAnimation();

        SmsRetrieverClient client = SmsRetriever.getClient(this);
        client.startSmsRetriever();

        HashMap<String, Object> params = new HashMap<>();
        params.put("receivePhoneNo", getBind().edittextPhoneNumber.getText().toString());
        ArrayList<String> hash = new AppSignatureHelper(this).getAppSignatures();
        if(hash != null && hash.size() > 0) params.put("hash", hash.get(0));

        DataInterface.getInstance().requestPhoneAuth(this, params, authType, new DataInterface.ResponseCallback<ResponseData<PhoneAuthVO>>() {
            @Override
            public void onSuccess(ResponseData<PhoneAuthVO> response) {
                cancelLoadingViewAnimation();

                if(response == null || isFinishing()) return;

                if(!response.getResultCode().equals("S000")) {
                    Toast.makeText(PhoneAuthActivity.this, response.getError(), Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(PhoneAuthActivity.this, getResources().getString(R.string.phone_auth_send_complete), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(PhoneAuthActivity.this, PhoneAuthConfirmActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("authId", String.valueOf(response.getData().getMacaronCdIdx()));
                intent.putExtra("phoneNum", getBind().edittextPhoneNumber.getText().toString());
                intent.putExtra("authType", authType);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(PhoneAuthActivity.this).toBundle());
                else startActivity(intent);
            }

            @Override
            public void onError(ResponseData<PhoneAuthVO> response) { cancelLoadingViewAnimation(); }

            @Override
            public void onFailure(Throwable t) { cancelLoadingViewAnimation(); }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        AnalyticsHelper.getInstance(this).sendScreenFromJson(this, PhoneAuthActivity.class.getSimpleName());
    }
}
