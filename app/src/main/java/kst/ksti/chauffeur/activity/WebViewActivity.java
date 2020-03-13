package kst.ksti.chauffeur.activity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import kst.ksti.chauffeur.BuildConfig;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.databinding.ActivityWebviewBinding;
import kst.ksti.chauffeur.ui.WebViewView;
import kst.ksti.chauffeur.utility.Logger;

public class WebViewActivity extends BaseActivity<ActivityWebviewBinding> {
	public WebViewView webview;
	private Listener listener = new Listener();

	public interface titleCallback{
		void onReceive(String title);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(webview == null) return;

		webview.destroy();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Logger.d(getResources().getString(R.string.begin_activity));    // 액티비티 호출 로그

		setBind(R.layout.activity_webview);

		init();
	}

	private void init() {
		getBind().toolbar.btnDrawerOpen.setVisibility(View.GONE);
		getBind().toolbar.btnTitleBack.setOnClickListener(listener);

		webview = new WebViewView(this, this.findViewById(android.R.id.content).getRootView());
		webview.setWebTitleCallback(this::setTitle);

		if(getIntent() != null) {
			String url = getIntent().getStringExtra("targetUrl");
			webview.initContentView(url);
		}

		if(BuildConfig.DEBUG) {
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				WebView.setWebContentsDebuggingEnabled(true);
			}
		}
	}

	public void setTitle(String title) {
		if (title == null) {
			return;
		}

		getBind().toolbar.tvTitle.setText(title);
	}

	private class Listener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.btnTitleBack) {
				finish();
			}
		}
	}

	@Override
	public void onBackPressed() {
		if (webview.mWebviewPop != null) {
			boolean goback = webview.mWebviewPop.canGoBack();
			if (goback) {
				webview.mWebviewPop.goBack();
			} else {
				webview.mWebviewPop.setVisibility(View.GONE);
				webview.mContainer.removeView(webview.mWebviewPop);
				webview.mWebviewPop = null;
				setTitle(webview.titleArr.get(webview.mView.getOriginalUrl()));
			}

			return;

		} else {
			boolean goback = webview.mView.canGoBack();
			if (goback) {
				webview.mView.goBack();
				return;
			}
		}

		super.onBackPressed();
	}
}
