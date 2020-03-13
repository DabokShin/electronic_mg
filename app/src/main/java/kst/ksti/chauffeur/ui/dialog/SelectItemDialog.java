package kst.ksti.chauffeur.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.utility.OnSingleClickListener;
import kst.ksti.chauffeur.utility.Util;

public class SelectItemDialog extends Dialog implements View.OnClickListener {

    private TextView mTitleView;
    private ImageButton mClosButton;
    private Button mSingleButton;
    private String mTitle;
    private LinearLayout mLayoutSelect;
    private SelectItemDialog dialog = this;

    private OnSelectItemDialogListener mSelectItemDialogListener;

    private String[] mContents;
    private int selectIndex = -1;
    private int columnCount = 2;
    private String selectStr = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 다이얼로그 외부 화면 흐리게 표현
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.select_item_dialog);

        setCancelable(false);

        mTitleView = findViewById(R.id.dlg_title);
        mSingleButton = findViewById(R.id.btnSingle);
        mClosButton = findViewById(R.id.btn_dlg_close);
        mLayoutSelect = findViewById(R.id.layout_select);

        // 제목과 내용을 생성자에서 셋팅한다.
        mTitleView.setText(mTitle);
        mClosButton.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                dialog.dismiss();
            }
        });
        mSingleButton.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                dialog.dismiss();

                if (mSelectItemDialogListener != null) {
                    mSelectItemDialogListener.onClick(selectIndex);
                }
            }
        });

        if (mContents != null) {
            if (columnCount == 2) {
                for (int i = 0; i < mContents.length; i += 2) {
                    LinearLayout layout = new LinearLayout(getContext());
                    Button b1 = new Button(getContext());
                    LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
                    LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);

                    params1.weight = 1;
                    params1.leftMargin = (int) Util.convertDpToPixel(19.7f, getContext());
                    params1.rightMargin = (int) Util.convertDpToPixel(18.6f, getContext());
                    params2.weight = 1;
                    params2.rightMargin = (int) Util.convertDpToPixel(19.7f, getContext());

                    b1.setMinHeight((int)Util.convertDpToPixel(50, getContext()));
                    b1.setText(mContents[i]);
                    b1.setBackgroundResource(R.drawable.select_item_dialog_button_state_back);
                    b1.setTag(i);
                    b1.setTextColor(Color.parseColor("#1e1e1e"));
                    b1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                    b1.setOnClickListener(this);
                    if(selectStr != null) {
                        if(selectStr.equals(mContents[i])) {
                            selectIndex = (int)b1.getTag();
                            b1.setSelected(true);
                            b1.setTextColor(Color.parseColor("#ffffff"));
                            b1.setTypeface(Typeface.DEFAULT_BOLD);
                        }
                    }
                    layout.addView(b1, params1);

                    if (i + 1 < mContents.length) {
                        Button b2 = new Button(getContext());

                        b2.setMinHeight((int)Util.convertDpToPixel(50, getContext()));
                        b2.setText(mContents[i + 1]);
                        b2.setBackgroundResource(R.drawable.select_item_dialog_button_state_back);
                        b2.setTag(i + 1);
                        b2.setTextColor(Color.parseColor("#1e1e1e"));
                        b2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                        b2.setOnClickListener(this);
                        if(selectStr != null) {
                            if(selectStr.equals(mContents[i + 1])) {
                                selectIndex = (int)b2.getTag();
                                b2.setSelected(true);
                                b2.setTextColor(Color.parseColor("#ffffff"));
                                b2.setTypeface(Typeface.DEFAULT_BOLD);
                            }
                        }
                        layout.addView(b2, params2);
                    } else {
                        View v2 = new View(getContext());

                        layout.addView(v2, params2);
                    }

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.topMargin = (int) Util.convertDpToPixel(7.3f, getContext());
                    layout.setGravity(Gravity.CENTER);
                    mLayoutSelect.addView(layout, params);
                }
            } else {
                for (int i = 0; i < mContents.length; i++) {
                    Button b1 = new Button(getContext());
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) Util.convertDpToPixel(130, getContext()), ViewGroup.LayoutParams.WRAP_CONTENT);

                    params.topMargin = (int) Util.convertDpToPixel(7.3f, getContext());
                    params.gravity = Gravity.CENTER;
                    b1.setMinHeight((int)Util.convertDpToPixel(50, getContext()));
                    b1.setText(mContents[i]);
                    b1.setBackgroundResource(R.drawable.select_item_dialog_button_state_back);
                    b1.setTag(i);
                    b1.setTextColor(Color.parseColor("#1e1e1e"));
                    b1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                    b1.setOnClickListener(this);
                    mLayoutSelect.addView(b1, params);
                }
            }
        }
    }

    public SelectItemDialog(Context context, String title, String[] contents, OnSelectItemDialogListener listener) {
        this(context, title, contents, listener, 2);
    }

    public SelectItemDialog(Context context, String title, String[] contents, OnSelectItemDialogListener listener, int column) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);

        if (title != null) {
            mTitle = title;
        } else {
            mTitle = "";
        }

        mSelectItemDialogListener = listener;
        mContents = contents;
        columnCount = column > 1 ? 2 : 1;
    }

    public SelectItemDialog(Context context, String title, String[] contents, OnSelectItemDialogListener listener, int column, String str) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);

        if (title != null) {
            mTitle = title;
        } else {
            mTitle = "";
        }

        mSelectItemDialogListener = listener;
        mContents = contents;
        columnCount = column > 1 ? 2 : 1;
        selectStr = str;
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

    @Override
    public boolean isShowing()
    {
        return super.isShowing();
    }

    @Override
    public void onClick(View v) {
        if (selectIndex >= 0) {
            Button b;

            if (columnCount == 2) {
                LinearLayout l = (LinearLayout) mLayoutSelect.getChildAt(selectIndex / 2);

                b = (Button) l.getChildAt(selectIndex % 2);
            } else {
                b = (Button) mLayoutSelect.getChildAt(selectIndex);
            }

            b.setSelected(false);
            b.setTextColor(Color.parseColor("#1e1e1e"));
            b.setTypeface(Typeface.DEFAULT);
        }

        Button bb = (Button)v;
        selectIndex = (int)bb.getTag();
        bb.setSelected(true);
        bb.setTextColor(Color.parseColor("#ffffff"));
        bb.setTypeface(Typeface.DEFAULT_BOLD);
    }

    public interface OnSelectItemDialogListener {
        void onClick(int index);
    }
}
