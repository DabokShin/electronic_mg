package kst.ksti.chauffeur.ui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.activity.WebViewActivity;
import kst.ksti.chauffeur.utility.Logger;

public class WebViewView {
	private Context base;
	public WebView mView, mWebviewPop;
	public String curUrl;
	public FrameLayout mContainer;
	public Map<String, String> titleArr = new HashMap<>();
	private WebViewActivity.titleCallback callback;

	public WebViewView(Context baseActivity, View v) {
		base = baseActivity;

		mView = v.findViewById(R.id.webview);
		mContainer = v.findViewById(R.id.webview_frame);

		WebSettings settings = mView.getSettings();

		settings.setJavaScriptEnabled(true);
		settings.setSupportMultipleWindows(true);
		settings.setLoadWithOverviewMode(true);
		settings.setUseWideViewPort(true);
		settings.setSupportZoom(true);
		settings.setBuiltInZoomControls(false);
		settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
		settings.setDomStorageEnabled(true);
		mView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		mView.setScrollbarFadingEnabled(true);

		if (18 < Build.VERSION.SDK_INT) {
			mView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			mView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
			CookieManager cookieManager = CookieManager.getInstance();
			cookieManager.setAcceptCookie(true);
			cookieManager.setAcceptThirdPartyCookies(mView, true);
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			mView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		} else {
			mView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}

		mView.setWebViewClient(new MyCustomWebViewClient());
		mView.setWebChromeClient(new MyCustomWebChromeClient());
	}

	private class MyCustomWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Logger.d("webviewview load : " + url);

			if (url.startsWith("intent")) {
				Intent intent = null;
				try {
					intent = Intent.parseUri(url, 0);
					base.startActivity(intent);
					return true;
				} catch (ActivityNotFoundException ex) {
					ex.printStackTrace();
					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + intent.getPackage()));
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					base.startActivity(i);

					return true;
				} catch (URISyntaxException e) {
					e.printStackTrace();
					return false;
				}
			}

			curUrl = url;
			return false;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			CookieSyncManager.getInstance().sync();

			if(titleArr.get(url) != null && !titleArr.get(url).equals("")) {
				setWebViewTitle(titleArr.get(url));
			}

			super.onPageFinished(view, url);
		}
	}

	private class MyCustomWebChromeClient extends WebChromeClient {
		@Override
		public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
			mWebviewPop = new WebView(view.getContext());
			mWebviewPop.setFocusable(true);
			mWebviewPop.setVerticalScrollbarOverlay(true);
			mWebviewPop.getSettings().setSupportZoom(true);
			mWebviewPop.getSettings().setJavaScriptEnabled(true);
			mWebviewPop.setVerticalScrollBarEnabled(true);
			mWebviewPop.getSettings().setSupportMultipleWindows(true);

			mWebviewPop.setWebViewClient(new MyCustomWebViewClient());
			mWebviewPop.setWebChromeClient(new MyCustomWebChromeClient());
			mWebviewPop.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
			mContainer.addView(mWebviewPop);
			WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
			transport.setWebView(mWebviewPop);
			resultMsg.sendToTarget();

			return true;
		}

		@Override
		public void onCloseWindow(WebView window) {
			super.onCloseWindow(window);
			if(mWebviewPop != null)
			{
				mWebviewPop.setVisibility(View.GONE);
				mContainer.removeView(mWebviewPop);
				mWebviewPop = null;
			}
		}

		@Override
		public void onReceivedTitle(WebView view, String title) {
			if(!titleArr.containsKey(view.getUrl())) {
			    titleArr.put(view.getUrl(), title);
            }

			super.onReceivedTitle(view, title);
		}

		@Override
		public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {

			AlertDialog.Builder dialog = new AlertDialog.Builder(base);
			dialog.setTitle(R.string.app_name).setMessage(message).setCancelable(false).setPositiveButton("확인", (dialog1, which) -> result.cancel()).create().setCanceledOnTouchOutside(false);

			if(base instanceof Activity && !((Activity)base).isFinishing()) {
				dialog.show();
			}

			return true;
		}

		@Override
		public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(base);
			dialog.setTitle(R.string.app_name).setMessage(message).setCancelable(false).setPositiveButton("예", (dialog1, which) -> result.confirm()).setNegativeButton("아니오", (dialog12, which) -> result.cancel()).create().setCanceledOnTouchOutside(false);

			if(base instanceof Activity && !((Activity)base).isFinishing()) {
				dialog.show();
			}

			return true;
		}
	}

	private void setWebViewTitle(String title) {
		if(callback != null) callback.onReceive(title);
	}

	public void setWebTitleCallback(WebViewActivity.titleCallback listener)
	{
		callback = listener;
	}

	public void initContentView(String link) {
//		String targetUrl = WebViewHelper.getTargetUrlWithParam(base, link);
		curUrl = link;
		mView.loadUrl(link);
	}

	public void destroy()
	{
		if(mContainer == null) return;
		if(mView == null) return;

		if(callback != null) callback= null;
		mContainer.removeView(mView);
		mView.removeAllViews();
		mView.destroy();
		mView = null;
	}
}
