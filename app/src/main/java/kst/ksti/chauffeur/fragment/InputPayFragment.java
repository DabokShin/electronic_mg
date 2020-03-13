package kst.ksti.chauffeur.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import kst.ksti.chauffeur.common.Global;
import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.common.AnalyticsHelper;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.common.NumberTextWatcher;
import kst.ksti.chauffeur.databinding.FrInputPayBinding;
import kst.ksti.chauffeur.listner.OnTitleListener;
import kst.ksti.chauffeur.net.DataInterface;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.MacaronCustomDialog;
import kst.ksti.chauffeur.utility.OnSingleClickListener;
import kst.ksti.chauffeur.utility.Util;

/**
 * 결제화면
 */
public class InputPayFragment extends NativeFragment implements OnTitleListener {

    private FrInputPayBinding mBind;
    private int fare = 0;
    private int serviceCharge = 0;
    private int total = 0;
    private boolean isRetry = false;
    private String title;
    private MacaronCustomDialog macaronCustomDialog;

    public InputPayFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nativeBaseActivity.cancelLoadingViewAnimation();
        MacaronApp.nearByDrivingStatusCheck = false;

        Bundle bundle = getArguments();
        if (bundle != null) {
            fare = bundle.getInt("fare", 0);
            serviceCharge = bundle.getInt("serviceCharge", 0);
            total = bundle.getInt("reqFareCat", 0);
            isRetry = bundle.getBoolean("isRetry", false);
            title = bundle.getString("title", "요금입력");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fr_input_pay, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Logger.d(getResources().getString(R.string.begin_fragment));    // 프래그먼트 호출 로그
        MacaronApp.allocStatus = AppDef.AllocationStatus.ARRIVAL;

        SetTitle(title);
        SetDividerVisibility(true);
        SetTitleListener(this);
        setDrawerLayoutEnable(false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AnalyticsHelper.getInstance(getContext()).sendScreenFromJson(nativeMainActivity, getClass().getSimpleName());

        mBind = FrInputPayBinding.bind(getView());

        initUI();
        setPaymentInfo();
        initEventListener();

        if(isRetry) {
            mBind.btnReqPay.setEnabled(true);
            mBind.btnReqPay.performClick();
        }
    }

    /**
     * UI 초기화
     */
    private void initUI() {
        mBind.title.btnDrawerOpen.setVisibility(View.GONE);
        mBind.title.btnTitleBack.setVisibility(View.GONE);

        mBind.cost.setText("0");
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

        mBind.fareAmount.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mBind.fareAmount.setFocusable(true);
                mBind.fareAmount.setFocusableInTouchMode(true);
                mBind.etServiceCharge.setFocusable(true);
                mBind.etServiceCharge.setFocusableInTouchMode(true);
                mBind.fareAmount.requestFocus();
                return false;
            }
        });

        mBind.fareAmount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean gainFocus) {
                //포커스가 주어졌을 때
                if (gainFocus) {
                    mBind.fareAmount.setHint("");
                }
                //포커스를 잃었을 때
                else {
                    mBind.fareAmount.setHint("0");
                }
            }
        });

        mBind.etServiceCharge.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mBind.etServiceCharge.setFocusable(true);
                mBind.etServiceCharge.setFocusableInTouchMode(true);
                mBind.etServiceCharge.requestFocus();
                return false;
            }
        });

        mBind.etServiceCharge.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean gainFocus) {
                //포커스가 주어졌을 때
                if (gainFocus) {
                    mBind.etServiceCharge.setHint("");
                }
                //포커스를 잃었을 때
                else {
                    mBind.etServiceCharge.setHint("0");
                }
            }
        });

        mBind.inputPayLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mBind.fareAmount.setFocusable(false);
                mBind.fareAmount.setFocusableInTouchMode(false);
                mBind.etServiceCharge.setFocusable(false);
                mBind.etServiceCharge.setFocusableInTouchMode(false);
                Util.hideKeyboard(nativeMainActivity, mBind.fareAmount);
                Util.hideKeyboard(nativeMainActivity, mBind.etServiceCharge);
                return false;
            }
        });

        mBind.fareAmount.setOnEditorActionListener(onEditorActionListener);
        mBind.etServiceCharge.setOnEditorActionListener(onEditorActionListener);

        // 결제 방법에 따라 결제 전환 버튼 활성화/비활성화
        switch (MacaronApp.currAllocation.fareCat) {
            case "APPCARD":
                mBind.btnOfflinePaymentChange.setVisibility(View.VISIBLE);
                mBind.btnReqPay.setText(R.string.txt_request_payment_app);
                break;
            case "OFFLINE":
                mBind.btnOfflinePaymentChange.setVisibility(View.GONE);
                mBind.btnReqPay.setText(R.string.txt_request_payment_offline);
                break;
            default:
                break;
        }

        // 결제 방식 변경 팝업 버튼
        mBind.btnOfflinePaymentChange.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Util.hideKeyboard(nativeMainActivity);

                macaronCustomDialog = getOfflinePaymentChangeDialog();
                macaronCustomDialog.show();
            }
        });

        //결제요청 버튼
        mBind.btnReqPay.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Util.hideKeyboard(nativeMainActivity);

                macaronCustomDialog = getMacaronCustomDialog();
                macaronCustomDialog.show();
            }
        });

        mBind.fareAmount.addTextChangedListener(new TotalAmountTextWatcher(mBind.fareAmount));
        mBind.etServiceCharge.addTextChangedListener(new TotalAmountTextWatcher(mBind.etServiceCharge));
    }

    private EditText.OnEditorActionListener onEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            switch (actionId) {
                case EditorInfo.IME_ACTION_DONE:
                    Util.hideKeyboard(nativeMainActivity);
                    mBind.fareAmount.setFocusable(false);
                    mBind.fareAmount.setFocusableInTouchMode(false);
                    mBind.etServiceCharge.setFocusable(false);
                    mBind.etServiceCharge.setFocusableInTouchMode(false);
                    break;

                case EditorInfo.IME_ACTION_NEXT:
                    break;
            }
            return false;
        }
    };

    private void setPaymentInfo() {
        if(isRetry) {
            mBind.fareAmount.setText(Util.makeStringComma(String.valueOf(fare)));
            mBind.etServiceCharge.setText(Util.makeStringComma(String.valueOf(serviceCharge)));
            mBind.cost.setText(Util.makeStringComma(String.valueOf(total)));

            mBind.fareAmount.setSelection(mBind.fareAmount.getText().toString().length());
        }
    }

    /**
     * 결제금액 확인 팝업
     */
    private MacaronCustomDialog getOfflinePaymentChangeDialog() {
        String message = "고객 요청에 의해\n직접 결제로\n변경하시겠습니까?\n(쿠폰 사용 및 재변경 불가)";
        return new MacaronCustomDialog(nativeMainActivity, "결제 방식 변경", message, "확인", "취소", offlinePaymentChangeClickListener, rightClickListener, true);
    }

    /**
     * 결제금액 확인 팝업
     */
    private MacaronCustomDialog getMacaronCustomDialog() {
        String message = "아래 결제금액이 맞는지\n확인해 주세요.\n" + mBind.cost.getText().toString() + "원";
        return new MacaronCustomDialog(nativeMainActivity, "결제요청", message, "확인", "취소", leftClickListener, rightClickListener, false);
    }

    /**
     * 결제 방식 변경 확인팝업 왼쪽 버튼 클릭리스너
     */
    private View.OnClickListener offlinePaymentChangeClickListener = new View.OnClickListener() {
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

                    // 직접 결제 요금 입력으로 세팅해준다.
                    SetTitle("직접 결제 요금 입력");
                    mBind.btnOfflinePaymentChange.setVisibility(View.GONE);

                    MacaronApp.currAllocation.fareCat = "OFFLINE";
                    mBind.btnReqPay.setText(R.string.txt_request_payment_offline);
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

    /**
     * 결제금액 확인팝업 오른쪽 버튼 클릭리스너
     */
    private View.OnClickListener rightClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            macaronCustomDialog.dismiss();
        }
    };

    /**
     * 결제금액 확인팝업 왼쪽 버튼 클릭리스너
     */
    private View.OnClickListener leftClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            macaronCustomDialog.dismiss();

            if(total > 0) {
                switchToPayment(MacaronApp.currAllocation.fareCat);
            } else {
                Toast.makeText(nativeMainActivity, "결제금액을 확인해 주세요.", Toast.LENGTH_SHORT).show();
            }
        }
    };

    /**
     * 결제요청 서버통신
     */
    private void requestCardPay() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("allocationIdx", MacaronApp.currAllocation.allocationIdx);
        params.put("realTaxiFare", total);
        params.put("realTaxiToll", serviceCharge);  // 통행요금

        DataInterface.getInstance().requestCardPay(nativeMainActivity, params, new DataInterface.ResponseCallback<ResponseData<Object>>() {
            @Override
            public void onSuccess(ResponseData<Object> response) {
                if (response.getResultCode().equals("S000")) {
//                    Bundle bundle = new Bundle();
//                    bundle.putString("arrowBack", "y");
//                    bundle.putString("fareCat", "APPCARD");

                    GoNativeScreen(new OnLinePayCompleteFragment(), getPaymentBundle(true), 1);
                    AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("결제요청", "결제요청 성공", "", Global.FA_EVENT_NAME.PAYMENT_REQUEST);

                } else {
                    Bundle bundle = getPaymentBundle(true);
//                    bundle.putString("errorMessage", response.getError());

                    GoNativeScreen(new OnlinePayFailedFragment(), bundle, 1);
                    AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("결제요청", "결제요청 실패", "", Global.FA_EVENT_NAME.PAYMENT_REQUEST);
                }
            }

            @Override
            public void onError(ResponseData<Object> response) {
                GoNativeScreen(new OnlinePayFailedFragment(), getPaymentBundle(true), 1);
                AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("결제요청", "결제요청 실패", "", Global.FA_EVENT_NAME.PAYMENT_REQUEST);
            }

            @Override
            public void onFailure(Throwable t) {
                GoNativeScreen(new OnlinePayFailedFragment(), getPaymentBundle(true), 1);
                AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("결제요청", "결제요청 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.PAYMENT_REQUEST);
            }
        });
    }

    /**
     * Bundle 세팅
     * @param arrow arrowBack 허용여부
     */
    private Bundle getPaymentBundle(boolean arrow) {
        Bundle bundle = new Bundle();

        try {
            if(arrow) {
                bundle.putString("arrowBack", "y");
            }
            bundle.putString("fare", String.valueOf(fare));
            bundle.putString("serviceCharge", String.valueOf(serviceCharge));
            bundle.putString("reqFareCat", String.valueOf(total));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return bundle;
    }

    /**
     * 결제방법 구분
     * @param fareCat 결제방법
     */
    private void switchToPayment(String fareCat) {
        if (fareCat == null) {
            Toast.makeText(nativeMainActivity, "결제 방법이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        nativeBaseActivity.playLoadingViewAnimation();

        switch (fareCat) {
            case "APPCARD":
                requestCardPay();
                break;
            case "OFFLINE":
                GoNativeScreen(new OffLinePayFragment(), getPaymentBundle(false), 1);
                break;
            default:
                break;
        }
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

    /**
     * 총 결제금액 계산하는 내부클래스
     */
    private class TotalAmountTextWatcher extends NumberTextWatcher {
        String fareStr;
        String serviceChargeStr;
        EditText et;

        TotalAmountTextWatcher(EditText et) {
            super(et);
            this.et = et;
        }

        @Override
        public void afterTextChanged(Editable s) {
            super.afterTextChanged(s);
//            if (mBind.fareAmount.getText().toString().trim().equals("")) {
//                mBind.fareAmount.setText("0");
//                mBind.fareAmount.setSelection(1);
//                return;
//            }
//            if (mBind.etServiceCharge.getText().toString().trim().equals("")) {
//                mBind.etServiceCharge.setText("0");
//                mBind.etServiceCharge.setSelection(1);
//                return;
//            }

            try
            {
                fareStr = getFare(mBind.fareAmount.getText().toString().trim());
                fare = Integer.valueOf(fareStr);
            }
            catch (Exception e) {
                fare = 0;
            }

            try
            {
                serviceChargeStr = getFare(mBind.etServiceCharge.getText().toString().trim());
                serviceCharge = Integer.valueOf(serviceChargeStr);
            }
            catch (Exception e) {
                serviceCharge = 0;
            }
            total = fare + serviceCharge;
            mBind.cost.setText(Util.makeStringComma(String.valueOf(total)));
            setButtonActive();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            super.beforeTextChanged(s, start, count, after);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            super.onTextChanged(s, start, before, count);
        }

        /**
         * 숫자 이외의 텍스트 제거
         */
        private String getFare(String fare) {
            String tmp = fare.replaceAll("[^0-9]", "");

            if (!Util.isNumber(tmp)) {
                tmp = tmp.replaceAll("[^0-9]", "");
            }

            return tmp;
        }

        /**
         * 버튼 활성화여부
         */
        private void setButtonActive() {
            mBind.btnReqPay.setEnabled(!mBind.fareAmount.getText().toString().isEmpty() &&  total > 0);
        }
    }

}
