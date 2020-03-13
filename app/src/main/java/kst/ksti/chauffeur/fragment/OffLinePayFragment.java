package kst.ksti.chauffeur.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.HashMap;

import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.common.AnalyticsHelper;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.databinding.FrOfflinePayBinding;
import kst.ksti.chauffeur.listner.OnTitleListener;
import kst.ksti.chauffeur.net.DataInterface;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.OnSingleClickListener;
import kst.ksti.chauffeur.utility.Util;

/**
 * 직접결제 화면
 */
public class OffLinePayFragment extends NativeFragment implements OnTitleListener {

    private FrOfflinePayBinding mBind;
    private String fare;
    private String serviceCharge;
    private String reqFareCat;

    public OffLinePayFragment() {
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
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fr_offline_pay, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Logger.d(getResources().getString(R.string.begin_fragment));    // 프래그먼트 호출 로그
        MacaronApp.allocStatus = AppDef.AllocationStatus.ARRIVAL;

        SetTitle("요금 받기");
        SetDividerVisibility(true);
        SetTitleListener(this);
        setDrawerLayoutEnable(false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AnalyticsHelper.getInstance(getContext()).sendScreenFromJson(nativeMainActivity, getClass().getSimpleName());

        mBind = FrOfflinePayBinding.bind(getView());

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
        mBind.amount.setText(Util.makeStringComma(reqFareCat));

        mBind.amount.setText(Util.makeStringComma(reqFareCat));
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
        mBind.btnOffAccept.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                //쇼퍼상태 변화 없음
                int realTaxiToll = 0;   // 통행요금
                try
                {
                    realTaxiToll = Integer.parseInt(serviceCharge);
                }
                catch (Exception e)
                {
                    Logger.e(e.getMessage());
                    Logger.e("LOG1 : 통행요금 String :  " + serviceCharge);
                }

                confirmOfflinePay(MacaronApp.currAllocation.allocationIdx, Integer.parseInt(reqFareCat), realTaxiToll);
            }
        });
    }

    /**
     * 직접결제요청 API 호출
     *
     * @param allocationIdx 해당 예약의 idz
     * @param realTaxiFare 결제금액
     */
    private void confirmOfflinePay(long allocationIdx, int realTaxiFare, int realTaxiToll) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("allocationIdx", allocationIdx);
        params.put("realTaxiFare", realTaxiFare);
        params.put("realTaxiToll", realTaxiToll);   // 통행요금

        DataInterface.getInstance().confirmOfflinePay(nativeMainActivity, params, new DataInterface.ResponseCallback<ResponseData<Object>>() {
            @Override
            public void onSuccess(ResponseData<Object> response) {
                if (response.getResultCode().equals("S000")) {
//                    Bundle bundle = new Bundle();
//                    bundle.putString("arrowBack", "y");
//                    bundle.putString("fareCat", "OFFLINE");
//                    GoNativeScreen(new OnLinePayCompleteFragment(), bundle, 1);

                    Toast.makeText(nativeMainActivity, "직접 결제를 완료하였습니다.", Toast.LENGTH_SHORT).show();
                    GoNativeScreenReplaceAdd(new DrivingFragment(), null, 1);
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
