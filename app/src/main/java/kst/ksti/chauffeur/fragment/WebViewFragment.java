package kst.ksti.chauffeur.fragment;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import kst.ksti.chauffeur.BuildConfig;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.common.AnalyticsHelper;
import kst.ksti.chauffeur.databinding.FrWebViewBinding;
import kst.ksti.chauffeur.utility.Logger;

/**
 * 웹뷰 화면
 */

public class WebViewFragment extends NativeFragment  {

    private FrWebViewBinding mBind;

    public WebView getWebView() {
        return mWebView;
    }

    private WebView mWebView;

    private String title = null;
    private String url = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            title = bundle.getString("title", "웹뷰");
            url = bundle.getString("url", "https://www.google.com");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fr_web_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Logger.d(getResources().getString(R.string.begin_fragment));    // 프래그먼트 호출 로그

        SetTitle(title);
        SetDividerVisibility(true);
        setDrawerLayoutEnable(false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AnalyticsHelper.getInstance(getContext()).sendScreenFromJson(nativeMainActivity, getClass().getSimpleName());

        if(BuildConfig.DEBUG) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
        }

        mBind = FrWebViewBinding.bind(getView());

        mBind.title.btnTitleBack.setVisibility(View.VISIBLE);
        mBind.title.btnTitleBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mWebView.canGoBack()) {
                    mWebView.goBack();
                } else {
                    EventForTitleView(v);
                }
            }
        });

        mBind.title.btnDrawerOpen.setVisibility(View.GONE);

        // 웹뷰 세팅
        mWebView = mBind.webView;                               // xml 자바코드 연결
        mWebView.getSettings().setJavaScriptEnabled(true);      // 자바스크립트 허용
        mWebView.loadUrl(url);                                  // 웹뷰 실행
        mWebView.setWebChromeClient(new WebChromeClient());     // 웹뷰에 크롬 사용 허용//이 부분이 없으면 크롬에서 alert가 뜨지 않음
        mWebView.setWebViewClient(new WebViewClientClass());    // 새창열기 없이 웹뷰 내에서 다시 열기//페이지 이동 원활히 하기위해 사용
    }

    private class WebViewClientClass extends WebViewClient {//페이지 이동
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("LOG1## : check URL", url);
            view.loadUrl(url);
            view.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onReceivedTitle(WebView view, String title) {
                    SetTitle(title);
                }
            });

            return true;
        }
    }
}
