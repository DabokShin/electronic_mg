package kst.ksti.chauffeur.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.common.AnalyticsHelper;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.databinding.FrOnlinepayCompleteBinding;
import kst.ksti.chauffeur.listner.OnTitleListener;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.OnSingleClickListener;
import kst.ksti.chauffeur.utility.Util;

/**
 * 앱결제 완료화면
 */
public class OnLinePayCompleteFragment extends NativeFragment implements OnTitleListener {

    private FrOnlinepayCompleteBinding mBind;
//    private String fareCat;
    private String fare;
    private String serviceCharge;
    private String reqFareCat;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nativeBaseActivity.cancelLoadingViewAnimation();

        Bundle bundle = getArguments();
        if (bundle != null) {
//            fareCat = bundle.getString("fareCat", "");
            fare = bundle.getString("fare", "0");
            serviceCharge = bundle.getString("serviceCharge", "0");
            reqFareCat = bundle.getString("reqFareCat", "0");
        }

        MacaronApp.nearByDrivingStatusCheck = false;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fr_onlinepay_complete, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Logger.d(getResources().getString(R.string.begin_fragment));    // 프래그먼트 호출 로그
        MacaronApp.allocStatus = AppDef.AllocationStatus.ARRIVAL;

        SetTitle("결제 완료");
        SetDividerVisibility(true);
        SetTitleListener(this);
        setDrawerLayoutEnable(false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AnalyticsHelper.getInstance(getContext()).sendScreenFromJson(nativeMainActivity, getClass().getSimpleName());

        mBind = FrOnlinepayCompleteBinding.bind(getView());

        initUI();
        initEventListener();
    }

    /**
     * UI 초기화
     */
    private void initUI() {
        mBind.title.btnDrawerOpen.setVisibility(View.GONE);
        mBind.title.btnTitleBack.setVisibility(View.GONE);

//        mBind.fareCat.setText(switchToPayment(fareCat));

        mBind.completeFare.setText(Util.makeStringComma(fare));
        mBind.completeServiceCharge.setText(Util.makeStringComma(serviceCharge));
        mBind.completeReqFareCat.setText(Util.makeStringComma(reqFareCat));
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

        //앱결제 confirm 버튼
        mBind.btnOnlineConfirm.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                //운행중으로 전환
                GoNativeScreenReplaceAdd(new DrivingFragment(), null, 1);
            }
        });
    }

    /**
     * 결제방법 텍스트 반환

    private String switchToPayment(String fareCat) {
        switch (fareCat) {
            case "APPCARD":
                return "앱 결제";

            case "OFFLINE":
                return "직접 결제";

            default:
                return "직접 결제";
        }
    }
     */

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
