package kst.ksti.chauffeur.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.utility.OnSingleClickListener;

public class SelectPhotoDialog extends Dialog {
    private ImageButton mClosButton;
    private LinearLayout mLayoutCamera;
    private LinearLayout mLayoutAlbum;
    private SelectPhotoDialog dialog = this;

    private SelectPhotoDialog.OnSelectPhotoDialogListener mSelectPhotoDialogListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 다이얼로그 외부 화면 흐리게 표현
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.select_photo_dialog);

        setCancelable(false);

        mClosButton = findViewById(R.id.btn_dlg_close);
        mLayoutCamera = findViewById(R.id.layout_camera);
        mLayoutAlbum = findViewById(R.id.layout_album);

        // 제목과 내용을 생성자에서 셋팅한다.
        mClosButton.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                dialog.dismiss();
            }
        });
        mLayoutCamera.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                dialog.dismiss();

                if (mSelectPhotoDialogListener != null) {
                    mSelectPhotoDialogListener.onCameraClick();
                }
            }
        });
        mLayoutAlbum.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                dialog.dismiss();

                if (mSelectPhotoDialogListener != null) {
                    mSelectPhotoDialogListener.onGalleryClick();
                }
            }
        });
    }

    public SelectPhotoDialog(Context context, SelectPhotoDialog.OnSelectPhotoDialogListener listener) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        mSelectPhotoDialogListener = listener;
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

    public interface OnSelectPhotoDialogListener {
        void onCameraClick();
        void onGalleryClick();
    }
}
