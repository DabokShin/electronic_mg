package kst.ksti.chauffeur.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.common.AnalyticsHelper;
import kst.ksti.chauffeur.databinding.FrOnlinepayFailedBinding;
import kst.ksti.chauffeur.listner.OnTitleListener;
import kst.ksti.chauffeur.net.DataInterface;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.MacaronCustomDialog;
import kst.ksti.chauffeur.utility.OnSingleClickListener;
import kst.ksti.chauffeur.utility.Util;

/**
 * 앱결제 실패화면
 */
public class OnlinePayFailedFragment extends NativeFragment implements OnTitleListener {

    private FrOnlinepayFailedBinding mBind;
    private String fare;
    private String serviceCharge;
    private String reqFareCat;
    private String errorMessage;
    private MacaronCustomDialog macaronCustomDialog;

    public OnlinePayFailedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nativeBaseActivity.cancelLoadingViewAnimation();
        MacaronApp.nearByDrivingStatusCheck = false;

        Bundle bundle = getArguments();
        if (bundle != null) {
            fare = bundle.getString("fare", "0");
            serviceCharge = bundle.getString("serviceCharge", "0");
            reqFareCat = bundle.getString("reqFareCat", "0");
            errorMessage = bundle.getString("errorMessage", "");
        }

        if(!TextUtils.isEmpty(errorMessage)) {
//            Toast.makeText(nativeMainActivity, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fr_onlinepay_failed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Logger.d(getResources().getString(R.string.begin_fragment));    // 프래그먼트 호출 로그

        SetTitle("결제 실패");

        SetDividerVisibility(true);
        SetTitleListener(this);
        setDrawerLayoutEnable(false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AnalyticsHelper.getInstance(getContext()).sendScreenFromJson(nativeMainActivity, getClass().getSimpleName());

        mBind = FrOnlinepayFailedBinding.bind(getView());

        initUI();
        initEventListener();
    }

    /**
     * UI 초기화
     */
    private void initUI() {
        mBind.title.btnDrawerOpen.setVisibility(View.GONE);
        mBind.title.btnTitleBack.setVisibility(View.GONE);

        mBind.completeFare.setText(Util.makeStringComma(fare));
        mBind.completeServiceCharge.setText(Util.makeStringComma(serviceCharge));
        mBind.cost.setText(Util.makeStringComma(reqFareCat));
    }

    /**
     * 이벤트 리스너 관리
     */
    private void initEventListener() {
        mBind.title.btnDrawerOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventForTitleView(v);
            }
        });

        mBind.title.btnTitleBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventForTitleView(v);
            }
        });

        //현장결제 confirm 버튼
        mBind.btnOffReq.setOnClickListener(onSingleClickListener);
        // 앱결제 재승인 요청
        mBind.btnAppayAgain.setOnClickListener(onSingleClickListener);
    }

    private OnSingleClickListener onSingleClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            switch (v.getId()) {
                case R.id.btnOffReq:
                    showChangeOfflinePayDialog();
                    break;

                case R.id.btnAppayAgain:
                    Bundle bundle = new Bundle();
                    bundle.putInt("fare", Integer.parseInt(fare));
                    bundle.putInt("serviceCharge", Integer.parseInt(serviceCharge));
                    bundle.putInt("reqFareCat", Integer.parseInt(reqFareCat));
                    bundle.putBoolean("isRetry", true);
                    bundle.putString("title", "앱 결제 요금 입력");
                    GoNativeScreen(new InputPayFragment(), bundle, 2);
                    break;
            }
        }
    };

    /**
     * 직접결제 전환 확인팝업
     */
    private void showChangeOfflinePayDialog() {
        macaronCustomDialog = getMacaronCustomDialog();
        macaronCustomDialog.show();
    }

    /**
     * 직접결제 전환 확인 팝업
     */
    private MacaronCustomDialog getMacaronCustomDialog() {
        String message = "아래 요금을 직접 결제 받으시겠습니까?\n" + mBind.cost.getText().toString() + "원";
        return new MacaronCustomDialog(nativeMainActivity, "요금 직접결제 받기", message, "네", "아니오",
                leftClickListener, rightClickListener, false);
    }

    /**
     * 직접결제 전환 확인팝업 오른쪽 버튼 클릭리스너
     */
    private View.OnClickListener rightClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            macaronCustomDialog.dismiss();
        }
    };

    /**
     * 직접결제 전환 확인팝업 왼쪽 버튼 클릭리스너
     */
    private View.OnClickListener leftClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            macaronCustomDialog.dismiss();
            changeFareCatOffline(MacaronApp.currAllocation.allocationIdx);
        }
    };

    /**
     * 직접결제 전환요청 API
     */
    private void changeFareCatOffline(long allocationIdx) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("allocationIdx", allocationIdx);
        DataInterface.getInstance().changeFareCatOffline(nativeMainActivity, params, new DataInterface.ResponseCallback<ResponseData<Object>>() {
            @Override
            public void onSuccess(ResponseData<Object> response) {
                if (response.getResultCode().equals("S000")) {
                    Logger.d("직접 결제로 전환 완료");

                    // 쇼퍼상태 변화 없음
                    // 앱결제실패시 현장결제로 요청, 현장결제 화면으로 이동
                    Bundle bundle = new Bundle();
                    bundle.putString("fare", fare);
                    bundle.putString("serviceCharge", serviceCharge);
                    bundle.putString("reqFareCat", reqFareCat);
                    GoNativeScreen(new OffLinePayFragment(), bundle, 1);
                }
            }

            @Override
            public void onError(ResponseData<Object> response) {

            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
    }

    @Override
    public void onTitleBackPress() {

    }

    @Override
    public void onTitleClosePress() {

    }

    @Override
    public void onSidelistClicked() {

    }

    @Override
    public void doBack() {

    }

}
