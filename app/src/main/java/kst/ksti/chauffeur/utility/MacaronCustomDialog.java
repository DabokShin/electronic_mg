package kst.ksti.chauffeur.utility;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Spanned;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import kst.ksti.chauffeur.R;


public class MacaronCustomDialog extends Dialog {

    private TextView mTitleView;
    private TextView mContentView;
    private Button mLeftButton;
    private Button mRightButton;
    private ImageButton mClosButton;
    private Button mSingleButton;
    private LinearLayout mLayoutButtons;
    private String mTitle;
    private String mContent;
    private String mSingleTitle;
    private String mLeftTitle;
    private String mRightTitle;
    private Spanned mSpannedContent;
    private boolean isThemePink = false;
    private boolean isCloseBtn = false;
    private MacaronCustomDialog dialog = this;

    private View.OnClickListener mLeftClickListener;
    private View.OnClickListener mRightClickListener;
    private View.OnClickListener mSingleClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 다이얼로그 외부 화면 흐리게 표현
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.macaron_custom_dlg);

        setCancelable(false);

        mTitleView = findViewById(R.id.dlg_title);
        mContentView = findViewById(R.id.dlg_msg);
        mLeftButton = findViewById(R.id.btnLeft);
        mRightButton = findViewById(R.id.btnRight);
        mSingleButton = findViewById(R.id.btnSingle);
        mLayoutButtons = findViewById(R.id.layoutButtons);
        mClosButton = findViewById(R.id.btn_dlg_close);

        // 제목과 내용을 생성자에서 셋팅한다.
        mTitleView.setText(mTitle);

        if (mSpannedContent != null) {
            mContentView.setText(mSpannedContent);
        } else {
            mContentView.setText(mContent);
        }

        if (isThemePink) {
            mTitleView.setTextColor(getContext().getResources().getColor(R.color.txtPink));
            mSingleButton.setBackgroundResource(R.drawable.ripple_single_round_false_effect);
        }

        // 클릭 이벤트 셋팅
        if (mLeftClickListener != null && mRightClickListener != null) {
            mTitleView.setTextColor(getContext().getResources().getColor(R.color.txtPink));
            mLayoutButtons.setVisibility(View.VISIBLE);
            mSingleButton.setVisibility(View.GONE);
            mLeftButton.setOnClickListener(mLeftClickListener);
            mLeftButton.setText(mLeftTitle);
            mRightButton.setOnClickListener(mRightClickListener);
            mRightButton.setText(mRightTitle);

            mClosButton.setOnClickListener(mRightClickListener);

        } else if (mSingleClickListener != null) {
            mLayoutButtons.setVisibility(View.GONE);
            mSingleButton.setVisibility(View.VISIBLE);
            mSingleButton.setOnClickListener(v -> {
                this.dismiss();
                mSingleClickListener.onClick(v);
            });
            mSingleButton.setText(mSingleTitle);

            if(isCloseBtn) {
                mClosButton.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        dialog.dismiss();
                    }
                });
            } else {
                mClosButton.setOnClickListener(mSingleClickListener);
            }
        }
    }

    // 클릭버튼이 하나일때 생성자 함수로 클릭이벤트를 받는다.
    public MacaronCustomDialog(Context context, String title, String content, String btnTitle,
                               View.OnClickListener singleListener, boolean isThemePink) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);

        if (title != null) {
            this.mTitle = title;
        } else {
            this.mTitle = context.getResources().getString(R.string.app_name);
        }

        this.mContent = content;
        this.mSingleClickListener = singleListener;
        this.mSingleTitle = btnTitle;
        this.isThemePink = isThemePink;
    }

    // 클릭버튼이 하나일때 생성자 함수로 클릭이벤트를 받는다.
    public MacaronCustomDialog(Context context, String title, String content, String btnTitle,
                               View.OnClickListener singleListener, boolean isThemePink, boolean isCloseBtn) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);

        if (title != null) {
            this.mTitle = title;
        } else {
            this.mTitle = context.getResources().getString(R.string.app_name);
        }

        this.mContent = content;
        this.mSingleClickListener = singleListener;
        this.mSingleTitle = btnTitle;
        this.isThemePink = isThemePink;
        this.isCloseBtn = isCloseBtn;
    }


    // 클릭버튼이 하나일때 생성자 함수로 클릭이벤트를 받는다.
    public MacaronCustomDialog(Context context, String title, String content, String btnTitle,
                               View.OnClickListener singleListener) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);

        if (title != null) {
            this.mTitle = title;
        } else {
            this.mTitle = context.getResources().getString(R.string.app_name);
        }

        this.mContent = content;
        this.mSingleClickListener = singleListener;
        this.mSingleTitle = btnTitle;
    }

    public MacaronCustomDialog(Context context, String title, Spanned content, String btnTitle,
                               View.OnClickListener singleListener, boolean isCancelBtn) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);

        if (title != null) {
            this.mTitle = title;
        } else {
            this.mTitle = context.getResources().getString(R.string.app_name);
        }

        this.mSpannedContent = content;
        this.mSingleClickListener = singleListener;
        this.mSingleTitle = btnTitle;
    }

    // 클릭버튼이 확인과 취소 두개일때 생성자 함수로 이벤트를 받는다
    public MacaronCustomDialog(Context context, String title, String content, String leftBtnTitle, String rightBtnTitle,
                               View.OnClickListener leftListener,
                               View.OnClickListener rightListener, boolean isThemePink) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);

        if (title != null) {
            this.mTitle = title;
        } else {
            this.mTitle = context.getResources().getString(R.string.app_name);
        }

        this.mContent = content;
        this.mLeftClickListener = leftListener;
        this.mRightClickListener = rightListener;
        this.mLeftTitle = leftBtnTitle;
        this.mRightTitle = rightBtnTitle;
        this.isThemePink = isThemePink;
    }

    // 클릭버튼이 확인과 취소 두개일때 생성자 함수로 이벤트를 받는다
    public MacaronCustomDialog(Context context, String title, String content, String leftBtnTitle, String rightBtnTitle,
                               View.OnClickListener leftListener,
                               View.OnClickListener rightListener) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);

        if (title != null) {
            this.mTitle = title;
        } else {
            this.mTitle = context.getResources().getString(R.string.app_name);
        }

        this.mContent = content;
        this.mLeftClickListener = leftListener;
        this.mRightClickListener = rightListener;
        this.mLeftTitle = leftBtnTitle;
        this.mRightTitle = rightBtnTitle;
    }

    // 클릭버튼이 확인과 취소 두개일때 생성자 함수로 이벤트를 받는다
    public MacaronCustomDialog(Context context, String title, Spanned content, String leftBtnTitle, String rightBtnTitle,
                               View.OnClickListener leftListener,
                               View.OnClickListener rightListener, boolean isThemePink) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);

        if (title != null) {
            this.mTitle = title;
        } else {
            this.mTitle = context.getResources().getString(R.string.app_name);
        }

        this.mSpannedContent = content;
        this.mLeftClickListener = leftListener;
        this.mRightClickListener = rightListener;
        this.mLeftTitle = leftBtnTitle;
        this.mRightTitle = rightBtnTitle;
        this.isThemePink = isThemePink;
    }

    // 클릭버튼이 확인과 취소 두개일때 생성자 함수로 이벤트를 받는다
    public MacaronCustomDialog(Context context, String title, Spanned content, String leftBtnTitle, String rightBtnTitle,
                               View.OnClickListener leftListener,
                               View.OnClickListener rightListener) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);

        if (title != null) {
            this.mTitle = title;
        } else {
            this.mTitle = context.getResources().getString(R.string.app_name);
        }

        this.mSpannedContent = content;
        this.mLeftClickListener = leftListener;
        this.mRightClickListener = rightListener;
        this.mLeftTitle = leftBtnTitle;
        this.mRightTitle = rightBtnTitle;
    }

    @Override
    public void show() {
        try {
            super.show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decor = getWindow().getDecorView();
                decor.setSystemUiVisibility(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        dismiss();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                View decor = getWindow().getDecorView();
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
