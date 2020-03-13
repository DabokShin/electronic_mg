package kst.ksti.chauffeur.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

import kst.ksti.chauffeur.common.Global;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.common.AnalyticsHelper;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.common.UIThread;
import kst.ksti.chauffeur.databinding.FrBreakBinding;
import kst.ksti.chauffeur.listner.ChangeStatusInterface;
import kst.ksti.chauffeur.listner.OnTitleListener;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.OnSingleClickListener;

/**
 * 휴식화면
 */
public class BreakTimeFragment extends NativeFragment implements OnTitleListener {

    private FrBreakBinding mBind;

    private long startBreakTime = 0;
    private long breakTime = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            breakTime = bundle.getLong("breakTime", 0);
            Logger.e("## breakTime = "+ breakTime);
        }

        startBreakTime = SystemClock.elapsedRealtime() - (breakTime * 1000);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fr_break, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Logger.d(getResources().getString(R.string.begin_fragment));    // 프래그먼트 호출 로그

        SetTitle("");
        SetDividerVisibility(false);
        SetTitleListener(this);
        setDrawerLayoutEnable(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AnalyticsHelper.getInstance(getContext()).sendScreenFromJson(getActivity(), getClass().getSimpleName());

        mBind = FrBreakBinding.bind(getView());

        mBind.title.btnDrawerOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventForTitleView(v);
            }
        });

        nativeBaseActivity.cancelLoadingViewAnimation();
        UIThread.executeInUIThread(mTickExecutor, 100);

        initEventListener();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        UIThread.removeUIThread(mTickExecutor);
    }

    /**
     * 휴식시간 타이머 쓰레드
     */
    private Runnable mTickExecutor = new Runnable() {
        @Override
        public void run() {
            setBreakTime();
            UIThread.executeInUIThread(mTickExecutor, 100);
        }
    };

    /**
     * 휴식시간 계산하여 화면에 노출
     */
    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void setBreakTime() {
        long time   = (startBreakTime < 0) ? 0 : (SystemClock.elapsedRealtime() - startBreakTime);
        int hour    = (int) (time / (1000*60*60)) % 24;
        int minutes = (int) (time / (1000*60)) % 60;
        int seconds = (int) (time / 1000) % 60;

        String hourText = (hour>0) ? hour+":" : "";
        String minutesText = String.format("%02d", minutes) + ":";
        String secondsText = String.format("%02d", seconds);

        mBind.duration.setText(hourText + minutesText + secondsText);
    }

    /**
     * 이벤트 리스너 관리
     */
    private void initEventListener() {
        mBind.btnDriving.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                backProcess();
            }
        });
    }

    /**
     * 뒤로가기 버튼 클릭시
     */
    public void backProcess() {
        nativeBaseActivity.playLoadingViewAnimation();

        HashMap<String, Object> params = new HashMap<>();
        params.put("chauffeurStatusCat", AppDef.ChauffeurStatus.WORK.toString());
        changeChauffeurStatusAndGoNextScreen(params, changeStatusInterface);
    }

    /**
     * 쇼퍼 상태변경 콜백
     */
    private ChangeStatusInterface changeStatusInterface = new ChangeStatusInterface() {
        @Override
        public void onSuccess(ResponseData<Object> response) {
            GoNativeScreenReplaceAdd(new DrivingFragment(), null, 2);
        }

        @Override
        public void onErrorCode(ResponseData<Object> response) {
            nativeBaseActivity.cancelLoadingViewAnimation();
            switch (response.getResultCode()) {
                case Global.ErrorCode.EC901:
                    showErrorCode901_Dialog(getActivity(), response.getError());
                    break;
                case Global.ErrorCode.EC902:
                    showErrorCode902_Dialog(getActivity(), response.getError());
                    break;
                default:
                    showErrorCodeEtcDialog(nativeMainActivity, response.getError());
                    break;
            }
        }

        @Override
        public void onError() {
            nativeBaseActivity.cancelLoadingViewAnimation();
        }

        @Override
        public void onFailed(Throwable t) {
            nativeBaseActivity.cancelLoadingViewAnimation();
        }
    };

    @Override
    public void onTitleBackPress() {

    }

    @Override
    public void onTitleClosePress() {
        // 전체 화면 팝업 닫기 버튼 선택
    }

    @Override
    public void onSidelistClicked() {

    }

    @Override
    public void doBack() {

    }

}
