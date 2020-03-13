package kst.ksti.chauffeur.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.airbnb.lottie.LottieDrawable;
import com.crashlytics.android.Crashlytics;
import com.jakewharton.rxbinding3.widget.RxTextView;
import com.jakewharton.rxbinding3.widget.TextViewTextChangeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import kst.ksti.chauffeur.common.Global;
import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.adapter.RecyclerViewAdapter;
import kst.ksti.chauffeur.common.AnalyticsHelper;
import kst.ksti.chauffeur.common.RecyclerViewAdapterCallback;
import kst.ksti.chauffeur.common.SearchEntity;
import kst.ksti.chauffeur.common.TmapTimeMachineParse;
import kst.ksti.chauffeur.common.UIThread;
import kst.ksti.chauffeur.databinding.FrDestSearchBinding;
import kst.ksti.chauffeur.listner.ChangeStatusInterface;
import kst.ksti.chauffeur.listner.ReverseGeocodingInterface;
import kst.ksti.chauffeur.listner.TMapTimeMachineAsyncTaskCallback;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.OnSingleClickListener;
import kst.ksti.chauffeur.utility.Util;

public class DestSearchFragment extends NativeFragment implements RecyclerViewAdapterCallback {

    private FrDestSearchBinding mBind;

    private RecyclerViewAdapter adapter;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable workRunnable;

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;

    private final int PERMISSIONS_REQUEST_ACCOUNTS = 100;
    private final int TMAP_DESTINATION_LIST_PAGE = 1;       // T맵 목적지 리스트 기본 페이지

    private boolean isInputType = true; // true : 텍스트, false : 보이스
    private int tMapListPage = TMAP_DESTINATION_LIST_PAGE;

    private CompositeDisposable mDisposable = new CompositeDisposable();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SetTitle("목적지 검색");
        SetDividerVisibility(true);
        setDrawerLayoutEnable(false);   // 네비게이션 상태 유무
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fr_dest_search, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AnalyticsHelper.getInstance(getContext()).sendScreenFromJson(nativeMainActivity, getClass().getSimpleName());

        mBind = FrDestSearchBinding.bind(getView());

        mBind.title.btnTitleBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backProcess();
            }
        });

        nativeMainActivity.cancelLoadingViewAnimation();

        mBind.animationView.setAnimation("lottie_voice_1.json");
        mBind.animationView.setRepeatCount(LottieDrawable.INFINITE);

        initEventListener();

        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, nativeMainActivity.getPackageName());
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(nativeMainActivity);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.e("<PHD>", "## params = " + params);
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                Log.e("<PHD>", "## buffer = " + Arrays.toString(buffer));
            }

            @Override
            public void onEndOfSpeech() {
                Log.e("<PHD>", "## onEndOfSpeech()");
            }

            @Override
            public void onError(int error) {
                // 음성입력 이미지 숨기고 다시 시작 버튼 이미지로 변경
                if(!isInputType)
                {
                    mBind.popupDestinationVoice.setVisibility(View.GONE);
                    mBind.popupDestinationVoiceRetry.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onResults(Bundle results) {
                Log.e("<PHD>", "## results = " + results);

                String key = SpeechRecognizer.RESULTS_RECOGNITION;

                ArrayList<String> arrayList = results.getStringArrayList(key);
                String rs[] = new String[arrayList.size()];
                arrayList.toArray(rs);

                if(isInputType)     // 상단 Hint 변경 : 텍스트
                {
                    setEditBoxUI(true);

                    mBind.editDestination.setText(rs[0]);
                    mBind.editDestination.setSelection(mBind.editDestination.getText().toString().length());
                }
                else                // 상단 Hint 변경 : 보이스
                {
                    setEditBoxUI(false);

                    mBind.editDestination.setText(rs[0]);
                    mBind.editDestination.setSelection(mBind.editDestination.getText().toString().length());

                    mBind.editDestinationVoice.setText(rs[0]);
                    mBind.editDestinationVoice.setSelection(mBind.editDestinationVoice.getText().toString().length());
                    mBind.editDestinationVoice.requestFocus();
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                Log.e("<PHD>", "## partialResults = " + partialResults);
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                Log.e("<PHD>", "## eventType = " + eventType + " / params = " + params);
            }
        });
    }

    /**
     * 이벤트 리스너 관리
     */
    private void initEventListener() {
        // 메인
        mBind.btnDestinationCancel.setOnClickListener(onSingleClickListener);           // 텍스트입력 검색시 취소 버튼
        mBind.btnDestinationCancelVoice.setOnClickListener(onSingleClickListener);      // 음성입력 검색시 취소 버튼
        mBind.btnDestinationVoice.setOnClickListener(onSingleClickListener);            // 음성입력 버튼

        // 음성입력
        mBind.btnDestinationVoiceRetry.setOnClickListener(onSingleClickListener);       // 음성입력 실패시 다시시도 버튼
        mBind.btnDestinationVoiceCancel.setOnClickListener(onSingleClickListener);      // 음성입력 팝업 취소
        mBind.btnDestinationVoiceCancelRetry.setOnClickListener(onSingleClickListener); // 음성입력 실패시 다시시도 팝업 취소

        // 목적지 입력 변경 리스너
        mBind.editDestination.setOnClickListener(onClickListener);                // 텍스트 입력 EditText 클릭시
        mBind.editDestination.requestFocus();
        mBind.editDestinationVoice.setOnClickListener(onClickListener);           // 음성입력 EditText 클릭시

        // EditText 입력
        mDisposable.add(
                RxTextView.textChangeEvents(mBind.editDestination)
                .debounce(400, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(searchQuery()));

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(nativeMainActivity);
        adapter = new RecyclerViewAdapter();
        mBind.recyclerView.setLayoutManager(layoutManager);
        mBind.recyclerView.setAdapter(adapter);

        mBind.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                int itemTotalCount = recyclerView.getAdapter().getItemCount();

                if (lastVisibleItemPosition == (itemTotalCount - 1) &&
                        itemTotalCount > 0) {
                    // 리스트 마지막(바닥) 도착!!!!! 다음 페이지 데이터 로드!!
                    //Logger.d("LOG2## T맵 목적지 리스트 : 마지막 리스트 id(개수 - 1) = " + lastVisibleItemPosition + ", 리스트 총 개수 = " + itemTotalCount);

                    int page = itemTotalCount / Global.TMAP_PAGE_LIST_COUNT;

                    // 요청한 페이지가 현재 리스트 셋팅 되어 있는 페이지보다 같거나 작으면 다음 페이지를 요청 한다.
                    if(tMapListPage <= page)
                    {
                        tMapListPage++;

                        final String word = mBind.editDestination.getText().toString();

                        if (mBind.editDestination.getText().length() > 0) {
                            handler.removeCallbacks(workRunnable);
                            workRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    Log.e("<PHD>", "#######################################  keyword = " + word);
                                    // TMap 목적지 검색
                                    adapter.filter(word, tMapListPage);
                                }
                            };
                            handler.post(workRunnable);
                        }
                    }
                    else
                        Toast.makeText(nativeMainActivity, getResources().getString(R.string.toast_tmap_last_destination), Toast.LENGTH_SHORT).show();
                }
            }
        });

        adapter.setCallback(this);
    }

    public void backProcess() {
        String editText = mBind.editDestination.getText().toString();

        // 글자 입력란이 "" 일경우는 뒤로 가기
        if(editText.equals(""))
        {
            // 음성 취소
            speechRecognizer.stopListening();
            speechRecognizer.cancel();

            nativeBaseActivity.GoNativeBackStack();
            Util.hideKeyboard(nativeMainActivity);
        }
        else
        {
            // 글자 입력란에 글자가 있을 경우 뒤로가기 누르면
            cancelWork();
            mBind.editDestination.requestFocus();

            isInputType = true;
            setEditBoxUI(true);
        }
    }

    private DisposableObserver<TextViewTextChangeEvent> searchQuery() {
        return new DisposableObserver<TextViewTextChangeEvent>() {
            @Override
            public void onNext(TextViewTextChangeEvent str) {

                // 음성검색중 다른 앱이 떠서 Context가 없어지는 경우
                if(getContext() == null)
                {
                    Logger.e("LOG1 : Context가 없는 경우 음성검색을 하면 안된다.");
                    return;
                }

                if(isInputType)     // 상단 Hint 변경 : 텍스트
                    setEditBoxUI(true);
                else                // 상단 Hint 변경 : 보이스
                    setEditBoxUI(false);

                final String word = str.getText().toString();

                if (str.getText().length() > 0) {
                    adapter.clear();

                    handler.removeCallbacks(workRunnable);
                    workRunnable = new Runnable() {
                        @Override
                        public void run() {
                            Log.e("<PHD>", "#######################################  keyword = " + word);
                            // TMap 목적지 검색
                            tMapListPage = TMAP_DESTINATION_LIST_PAGE;  // 첫 검색이기 때문에 무조건 1페이지만 불러온다.
                            adapter.filter(word, tMapListPage);
                        }
                    };
                    handler.post(workRunnable);

                    mBind.btnDestinationCancel.setVisibility(View.VISIBLE);
                    mBind.recyclerView.setVisibility(View.VISIBLE);
                    mBind.tvSearchVoice.setVisibility(View.GONE);
                    mBind.btnVoiceBox.setVisibility(View.GONE);
                    mBind.popupDestinationVoice.setVisibility(View.GONE);
                    mBind.popupDestinationVoiceRetry.setVisibility(View.GONE);
                } else {
                    mBind.btnDestinationCancel.setVisibility(View.GONE);
                    mBind.recyclerView.setVisibility(View.GONE);
                    mBind.tvSearchVoice.setVisibility(View.VISIBLE);
                    mBind.btnVoiceBox.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(Throwable e) {
                Logger.d("LOG1 : 배달 목적지 입력 실패");
            }

            @Override
            public void onComplete() {

            }
        };
    }

    /**
     * 검색창 UI 변경
     */
    private void setEditBoxUI(boolean isType)
    {
        if(isType)  // 상단 Hint 변경 : 텍스트
        {
            mBind.tvSearchHint.setText(getContext().getString(R.string.txt_text_input_search));
            mBind.searchInputBoxText.setVisibility(View.VISIBLE);   // 텍스트
            mBind.searchInputBoxVoice.setVisibility(View.GONE);     // 보이스
        }
        else        // 상단 Hint 변경 : 보이스
        {
            mBind.tvSearchHint.setText(getContext().getString(R.string.txt_voice_input_search));
            mBind.searchInputBoxText.setVisibility(View.GONE);      // 텍스트
            mBind.searchInputBoxVoice.setVisibility(View.VISIBLE);  // 보이스
        }
    }

    /**
     * 검색 취소시 해줘야 하는 것들
     */
    private void cancelWork()
    {
        // 음성 취소
        speechRecognizer.stopListening();
        speechRecognizer.cancel();

        //  EditBox 초기화
        mBind.editDestination.setText("");
        mBind.editDestinationVoice.setText("");
    }

    /**
     * 검색된 POI 클릭
     */
    @Override
    public void onItemClick(int position, SearchEntity searchEntity) {
        Logger.d("LOG1 : 배달 검색 리스트에서 " + position + "번째 클릭");

        nativeMainActivity.playLoadingViewAnimation();

        try
        {
            // 일반운행 도착지 정보 셋팅
            MacaronApp.currStartRoadsale.resvDstLon = Double.parseDouble(searchEntity.getPoi().getNoorLon());
            MacaronApp.currStartRoadsale.resvDstLat = Double.parseDouble(searchEntity.getPoi().getNoorLat());
            MacaronApp.currStartRoadsale.resvDstPoi = searchEntity.getPoi().getName();
            MacaronApp.currStartRoadsale.resvDstAddress = searchEntity.getAddress();

            Log.e("<LOG1#>", searchEntity.getTitle() + "/" + searchEntity.getAddress());

            Util.hideKeyboard(nativeMainActivity, mBind.tvSearchHint);

            // 내 현재 위치를 Tmap에서 받아온다.
            Util.getLocationInfomationParams(nativeMainActivity, reverseGeocodingInterfaceTmapTimeMachine, null, null);
        }
        catch(Exception e)
        {
            Logger.e("LOG E : " + e.getMessage());
            e.printStackTrace();

            // fabric crash report 내부/상용 서버 배포
            if(Global.getCrashReport())
                Crashlytics.logException(e);

            nativeMainActivity.cancelLoadingViewAnimation();
            Toast.makeText(nativeMainActivity, "목적지 리스트 오류. 다시 시도해주세요", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Tmap에서 위치정보 받아오는 콜백리스너
     */
    private ReverseGeocodingInterface reverseGeocodingInterfaceTmapTimeMachine = new ReverseGeocodingInterface() {
        @Override
        public void onSuccess(final HashMap<String, Object> params, ChangeStatusInterface changeStatusInterface) {
            Logger.d("## TMap My Position : Geocoding onSuccess()");

            try
            {
                // 티맵 타임머신 api 호출
                TmapTimeMachineParse timeMachineParse = new TmapTimeMachineParse(params, new TMapTimeMachineAsyncTaskCallback() {
                    @Override
                    public void onSuccess() {
                        nativeMainActivity.cancelLoadingViewAnimation();
                        GoNativeScreenReplaceAdd(new RoadsaleStartFragment(), null, 1);
                    }

                    @Override
                    public void onFailure() {
                        nativeMainActivity.cancelLoadingViewAnimation();
                    }
                });
                timeMachineParse.execute();
            }
            catch(Exception e) {
                Toast.makeText(nativeMainActivity, "T맵 통신 오류", Toast.LENGTH_SHORT).show();
                nativeMainActivity.cancelLoadingViewAnimation();
            }
        }

        @Override
        public void onError(HashMap<String, Object> params, ChangeStatusInterface changeStatusInterface, final String errorMsg) {
            Logger.d("## TMap My Position : Geocoding onError()");
            UIThread.executeInUIThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(nativeMainActivity, errorMsg, Toast.LENGTH_SHORT).show();
                    nativeMainActivity.cancelLoadingViewAnimation();
                }
            });
        }

        @Override
        public void onGpsError(HashMap<String, Object> params, ChangeStatusInterface changeStatusInterface) {
            Logger.d("## TMap My Position : Geocoding onGpsError()");
            nativeMainActivity.cancelLoadingViewAnimation();
        }
    };

    private OnSingleClickListener onSingleClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            switch (v.getId()) {
                case R.id.btnDestinationCancel:         // 목적지 입력 취소 버튼
                case R.id.btnDestinationCancelVoice:    // 목적지 입력 취소 버튼
                    mBind.editDestination.setText("");
                    mBind.editDestinationVoice.setText("");
                    mBind.editDestination.requestFocus();

                    isInputType = true;
                    setEditBoxUI(true);

                    cancelWork();
                    break;

                case R.id.btnDestinationVoice:          // 음성입력 버튼
                case R.id.btnDestinationVoiceRetry:     // 음성입력 다시 시도 버튼

                    // 음성입력 버튼 일때 검색할 텍스트가 남아 있으면 버튼이 안눌린다.
                    if(v.getId() == R.id.btnDestinationVoice)
                    {
                        String editDestination = mBind.editDestination.getText().toString();
                        if(!editDestination.equals(""))
                        {
                            Toast.makeText(nativeMainActivity, "목적지 검색중 입니다.", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }

                    Util.hideKeyboard(nativeMainActivity);

                    mBind.animationView.playAnimation();

                    // 음성입력 권한 체크
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (nativeMainActivity.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_ACCOUNTS);
                            break;
                        }
                    }

                    mBind.popupDestinationVoice.setVisibility(View.VISIBLE);
                    mBind.popupDestinationVoiceRetry.setVisibility(View.GONE);

                    // 음성입력 시작
                    try
                    {
                        speechRecognizer.startListening(speechRecognizerIntent);
                    }
                    catch(Exception e)
                    {
                        Log.e("LOG1 Exception", e.getMessage());
                        e.printStackTrace();
                    }


                    isInputType = false;
                    break;

                case R.id.btnDestinationVoiceCancel:        // 음성입력 취소
                case R.id.btnDestinationVoiceCancelRetry:   // 음성입력 다시하기 팝업 취소
                    mBind.popupDestinationVoice.setVisibility(View.GONE);
                    mBind.popupDestinationVoiceRetry.setVisibility(View.GONE);

                    isInputType = true;
                    setEditBoxUI(true);
                    break;
                case R.id.editDestination:
                case R.id.editDestinationVoice:
                    isInputType = true;
                    setEditBoxUI(true);
                    break;
            }
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.editDestination:
                case R.id.editDestinationVoice:
                    isInputType = true;
                    setEditBoxUI(true);
                    break;
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode)
        {
            case PERMISSIONS_REQUEST_ACCOUNTS:
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    mBind.popupDestinationVoice.setVisibility(View.VISIBLE);
                    mBind.popupDestinationVoiceRetry.setVisibility(View.GONE);

                    // 음성입력 시작
                    try
                    {
                        speechRecognizer.startListening(speechRecognizerIntent);
                    }
                    catch(Exception e)
                    {
                        Log.e("LOG1 Exception", e.getMessage());
                        e.printStackTrace();
                    }

                    isInputType = false;
                }
                break;
        }
    }
}
