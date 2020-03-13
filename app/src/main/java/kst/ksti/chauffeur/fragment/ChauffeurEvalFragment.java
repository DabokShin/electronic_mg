package kst.ksti.chauffeur.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;

import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.common.AnalyticsHelper;
import kst.ksti.chauffeur.databinding.FrEvaluateListBinding;
import kst.ksti.chauffeur.listner.OnTitleListener;
import kst.ksti.chauffeur.model.ChauffeurEval;
import kst.ksti.chauffeur.net.DataInterface;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.MacaronCustomDialog;
import kst.ksti.chauffeur.utility.PrefUtil;

/**
 * 좋아요, 싫어요 리스트 화면
 */
public class ChauffeurEvalFragment extends NativeFragment implements OnTitleListener {

    private FrEvaluateListBinding mBind;
    private ArrayList<ChauffeurEval> chauffeurEvalsGood;
    private ArrayList<ChauffeurEval> chauffeurEvalsBad;
    private String likeYn;

    private MacaronCustomDialog dialog;

    private boolean isBackKeyCheck = false;

    public ChauffeurEvalFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();

        if (bundle != null) {
            likeYn = bundle.getString("likeYn", "Y");
        }

        chauffeurEvalsGood = new ArrayList<>();
        chauffeurEvalsBad = new ArrayList<>();

        if(!PrefUtil.getBackKeyCheck(nativeMainActivity)) {
            isBackKeyCheck = true;
            PrefUtil.setBackKeyCheck(nativeMainActivity, true);
        } else {
            isBackKeyCheck = false;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fr_evaluate_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Logger.d(getResources().getString(R.string.begin_fragment));    // 프래그먼트 호출 로그

        if (likeYn.equals("Y")) SetTitle("좋아요 리스트");
        else SetTitle("싫어요 리스트");

        SetDividerVisibility(true);
        SetTitleListener(this);
        setDrawerLayoutEnable(false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AnalyticsHelper.getInstance(getContext()).sendScreenFromJson(nativeMainActivity, getClass().getSimpleName());

        mBind = FrEvaluateListBinding.bind(getView());

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

        if(MacaronApp.chauffeur != null) {
            mBind.name.setText(MacaronApp.chauffeur.name + " 기사");
            receiveChauffeurEvaluation();

        } else {
            mBind.name.setText("기사");

            dialog = new MacaronCustomDialog(nativeMainActivity,
                    null,
                    "쇼퍼님의 정보가 없습니다.\n앱을 다시 실행해 주세요.",
                    "확인",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!nativeMainActivity.isFinishing()) {
                                dialog.dismiss();
                                nativeMainActivity.finish();
                            }
                        }
                    });
            dialog.show();
        }
    }

    @Override
    public void onDetach() {
        if(isBackKeyCheck) {
            PrefUtil.setBackKeyCheck(nativeMainActivity, false);
        }

        super.onDetach();
    }

    /**
     * 좋아요, 싫어요 리스트 호출
     */
    private void receiveChauffeurEvaluation() {
        HashMap<String, Object> params = new HashMap<>();

        DataInterface.getInstance().receiveEvaluation(nativeMainActivity, params, new DataInterface.ResponseCallback<ResponseData<ChauffeurEval>>() {
            @Override
            public void onSuccess(ResponseData<ChauffeurEval> response) {
                if ("S000".equals(response.getResultCode())) {
                    initUI((ArrayList<ChauffeurEval>) response.getList());
                }

                nativeBaseActivity.cancelLoadingViewAnimation();
            }

            @Override
            public void onError(ResponseData<ChauffeurEval> response) {
                nativeBaseActivity.cancelLoadingViewAnimation();
            }

            @Override
            public void onFailure(Throwable t) {
                nativeBaseActivity.cancelLoadingViewAnimation();
            }
        });
    }

    /**
     * UI 초기화
     */
    private void initUI(ArrayList<ChauffeurEval> chauffeurEvals) {
        try {
            for (ChauffeurEval tmpList : chauffeurEvals) {
                if (tmpList.likeYn.equalsIgnoreCase("Y")) {
                    chauffeurEvalsGood.add(tmpList);
                } else {
                    chauffeurEvalsBad.add(tmpList);
                }
            }

            if (likeYn.equals("Y")) {
                if(chauffeurEvalsGood.size() > 0) setGoodEval();
            } else {
                if(chauffeurEvalsBad.size() > 0) setBadEval();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 좋아요 리스트 초기화
     */
    private void setGoodEval(){
        mBind.contLayout1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.background_evaluate_good));
        mBind.contLayout2.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.background_evaluate_good));
        mBind.contLayout3.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.background_evaluate_good));
        mBind.contLayout4.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.background_evaluate_good));
        mBind.contLayout5.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.background_evaluate_good));

        switch (chauffeurEvalsGood.size()) {
            case 5:
                mBind.cont5.setText(chauffeurEvalsGood.get(4).cdName);
                mBind.cont5Cnt.setText(chauffeurEvalsGood.get(4).cdCount);
            case 4:
                mBind.cont4.setText(chauffeurEvalsGood.get(3).cdName);
                mBind.cont4Cnt.setText(chauffeurEvalsGood.get(3).cdCount);
            case 3:
                mBind.cont3.setText(chauffeurEvalsGood.get(2).cdName);
                mBind.cont3Cnt.setText(chauffeurEvalsGood.get(2).cdCount);
            case 2:
                mBind.cont2.setText(chauffeurEvalsGood.get(1).cdName);
                mBind.cont2Cnt.setText(chauffeurEvalsGood.get(1).cdCount);
            case 1:
                mBind.cont1.setText(chauffeurEvalsGood.get(0).cdName);
                mBind.cont1Cnt.setText(chauffeurEvalsGood.get(0).cdCount);
                break;
            default:
                break;
        }
    }

    /**
     * 싫어요 리스트 초기화
     */
    private void setBadEval(){
        mBind.contLayout1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.background_evaluate_bad));
        mBind.contLayout2.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.background_evaluate_bad));
        mBind.contLayout3.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.background_evaluate_bad));
        mBind.contLayout4.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.background_evaluate_bad));
        mBind.contLayout5.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.background_evaluate_bad));

        switch (chauffeurEvalsBad.size()) {
            case 5:
                mBind.cont5.setText(chauffeurEvalsBad.get(4).cdName);
                mBind.cont5Cnt.setText(chauffeurEvalsBad.get(4).cdCount);
            case 4:
                mBind.cont4.setText(chauffeurEvalsBad.get(3).cdName);
                mBind.cont4Cnt.setText(chauffeurEvalsBad.get(3).cdCount);
            case 3:
                mBind.cont3.setText(chauffeurEvalsBad.get(2).cdName);
                mBind.cont3Cnt.setText(chauffeurEvalsBad.get(2).cdCount);
            case 2:
                mBind.cont2.setText(chauffeurEvalsBad.get(1).cdName);
                mBind.cont2Cnt.setText(chauffeurEvalsBad.get(1).cdCount);
            case 1:
                mBind.cont1.setText(chauffeurEvalsBad.get(0).cdName);
                mBind.cont1Cnt.setText(chauffeurEvalsBad.get(0).cdCount);
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


}
