package kst.ksti.chauffeur.activity.signup;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.soundcloud.android.crop.Crop;

import java.util.HashMap;

import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.activity.BaseActivity;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.databinding.ActivitySignupPictureRegistrationBinding;
import kst.ksti.chauffeur.model.RegistPictureVO;
import kst.ksti.chauffeur.model.signup.ChauffeurStatusVO;
import kst.ksti.chauffeur.model.signup.RegistChauffeurInfoVO;
import kst.ksti.chauffeur.net.DataInterface;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.ui.dialog.FacePhotoInfoDialog;
import kst.ksti.chauffeur.ui.dialog.LicenseInfoDialog;
import kst.ksti.chauffeur.ui.dialog.SelectPhotoDialog;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.MacaronCustomDialog;
import kst.ksti.chauffeur.utility.OnSingleClickListener;
import kst.ksti.chauffeur.utility.PrefUtil;
import kst.ksti.chauffeur.utility.Util;

/**
 * 사진/자격 정보
 */
public class SignupPictureRegistration extends BaseActivity<ActivitySignupPictureRegistrationBinding> {

    private String srcUri;
    private String destUri;
    private int type;
    private MacaronCustomDialog macaronCustomDialog;
    private RegistChauffeurInfoVO registChauffeurInfoVO = null;
    private ChauffeurStatusVO chauffeurStatusVO = null;
    private String svcStatus = null;
    private String chauffeurRegInfoCat = null;
    private String joinType = null;

    private boolean isChauffeur = false;
    private boolean isLicense = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.d(getResources().getString(R.string.begin_activity));    // 액티비티 호출 로그

        Thread.setDefaultUncaughtExceptionHandler(((MacaronApp)getApplication()).getUncaughtExceptionHandler(SignupPictureRegistration.this));

        setBind(R.layout.activity_signup_picture_registration);
        setLoadingBarLayout(getBind().loadingBarLayout);
        setProgressBar(getBind().progressBar);

        Intent intent = getIntent();
        if (intent != null) {
            registChauffeurInfoVO = (RegistChauffeurInfoVO)getIntent().getSerializableExtra("registChauffeurInfoVO");
            chauffeurStatusVO = (ChauffeurStatusVO)intent.getSerializableExtra("chauffeurStatusVO");
            svcStatus = intent.getStringExtra("svcStatus");
            chauffeurRegInfoCat = intent.getStringExtra("chauffeurRegInfoCat");
            joinType = intent.getStringExtra("joinType");

            if(registChauffeurInfoVO != null) {
                if(registChauffeurInfoVO.getRcpi() != null) {
                    Glide.with(SignupPictureRegistration.this)
                            .load(registChauffeurInfoVO.getRcpi().getImgUrl())
                            .into("CHAUFFEUR".equals("CHAUFFEUR") ? getBind().imageFace : getBind().imageLicense);

                    isChauffeur = true;
                }

                if(registChauffeurInfoVO.getRcpi2() != null) {
                    Glide.with(SignupPictureRegistration.this)
                            .load(registChauffeurInfoVO.getRcpi2().getImgUrl())
                            .into("CHAUFFEUR".equals("LICENSE") ? getBind().imageFace : getBind().imageLicense);

                    isLicense = true;
                }

                validNextCheck();
            }
        }

        initViews();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == Crop.REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                if (type == 1) {
                    uploadImage("CHAUFFEUR", destUri);
                } else {
                    uploadImage("LICENSE", destUri);
                }
            }
        } else if (requestCode == Util.REQUEST_CAMERA) {
            if (resultCode == RESULT_OK) {
                srcUri = Util.editImage(SignupPictureRegistration.this, srcUri);

                if (type == 1) {
                    destUri = Util.getOutputMediaFileName(SignupPictureRegistration.this);
                    Crop.of(Util.fromFile(this, srcUri), Util.fromFile(this, destUri)).asSquare().start(this);
                } else {
                    destUri = Util.getOutputMediaFileName(SignupPictureRegistration.this);
                    Crop.of(Util.fromFile(this, srcUri), Util.fromFile(this, destUri)).start(this);
                }
            }
        } else if (requestCode == Util.REQUEST_GALLERY) {
            if (resultCode == RESULT_OK) {
                Uri uri = result.getData();

                srcUri = Util.getPath(SignupPictureRegistration.this, uri);
                srcUri = Util.editImage(SignupPictureRegistration.this, srcUri);

                if (type == 1) {
                    // 얼굴 사진 업로드
                    destUri = Util.getOutputMediaFileName(SignupPictureRegistration.this);
                    //Crop.of(uri, Util.fromFile(this, destUri)).asSquare().start(this);
                    Crop.of(Util.fromFile(this, srcUri), Util.fromFile(this, destUri)).start(this);
                } else {
                    // 면허증 사진 업로드
                    destUri = Util.getOutputMediaFileName(SignupPictureRegistration.this);
                    //Crop.of(uri, Util.fromFile(this, destUri)).start(this);
                    Crop.of(Util.fromFile(this, srcUri), Util.fromFile(this, destUri)).start(this);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, result);
        }
    }

    private void initViews() {
        String companyType = null;

        getBind().title.btnTitleBack.setVisibility(View.GONE);
        getBind().title.btnDrawerOpen.setVisibility(View.GONE);
        getBind().title.tvTitle.setText(getResources().getString(R.string.signup_picture_registration_title));
        getBind().btnFaceCamera.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                new FacePhotoInfoDialog(SignupPictureRegistration.this).show();
            }
        });
        getBind().btnFacePictureUpload.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                type = 1;
                showSelectPhotoDialog();
            }
        });
        getBind().btnCertificateCamera.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                new LicenseInfoDialog(SignupPictureRegistration.this).show();
            }
        });
        getBind().btnCertificatePictureUpload.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                type = 2;
                showSelectPhotoDialog();
            }
        });

        // 보류 내용 작성
        if(chauffeurStatusVO != null && chauffeurStatusVO.getCitList() != null ) {
            TableRow subTableRow = new TableRow(this);
            TextView tvSubTitle1 = new TextView(this);
            TextView tvSubTitle2 = new TextView(this);

            tvSubTitle1.setText(getResources().getString(R.string.signup_complete_reason_subtitle1));
            tvSubTitle1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            tvSubTitle1.setTextColor(Color.parseColor("#fff97dad"));
            tvSubTitle2.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            tvSubTitle2.setText(getResources().getString(R.string.signup_complete_reason_subtitle2));
            tvSubTitle2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            tvSubTitle2.setTextColor(Color.parseColor("#fff97dad"));
            tvSubTitle2.setPadding(20, 0, 0, 0);
            subTableRow.setPadding(20, 0, 0, 0);
            subTableRow.addView(tvSubTitle1);
            subTableRow.addView(tvSubTitle2);

            getBind().tlReason.addView(subTableRow);

            for(ChauffeurStatusVO.CitList tmp : chauffeurStatusVO.getCitList()) {
                if(tmp.getChauffeurIncorrectinfoCat().contains(AppDef.ChauffeurRegInfoCat.PROFILE.toString())) {
                    TableRow tableRow = new TableRow(this);
                    TextView tv1 = new TextView(this);
                    TextView tv2 = new TextView(this);
                    tv1.setText("  -");
                    tv1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                    tv1.setTextColor(Color.parseColor("#fff97dad"));
                    tv2.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    tv2.setText(tmp.getChauffeurIncorrectinfoText());
                    tv2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                    tv2.setTextColor(Color.parseColor("#fff97dad"));
                    tv2.setPadding(20, 0, 0, 0);
                    tableRow.setPadding(20, 0, 0, 0);
                    tableRow.addView(tv1);
                    tableRow.addView(tv2);

                    getBind().tlReason.addView(tableRow);
                }
            }

            getBind().tlReason.setVisibility(View.VISIBLE);
        }
        else {
            getBind().tlReason.setVisibility(View.GONE);
        }

        getBind().btnBefore.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                onBackPressed();
            }
        });

        companyType = PrefUtil.getSignupCompanyType(SignupPictureRegistration.this);

        // 저장되어 있는 값이 없다면 사진 업로드까지 진행은 했다가
        // 앱을 지우고 다시 설치 한 것으로 간주, 데이터를 다시 넣어준다.
        if(companyType == null || companyType.equals("")) {
            if(registChauffeurInfoVO != null)
            {
                if(registChauffeurInfoVO.getCompanyType().equals("INDIVIDUAL")) {
                    PrefUtil.setSignupCompanyType(SignupPictureRegistration.this, "individual");
                }
                else if(registChauffeurInfoVO.getCompanyType().equals("CORPORATE")) {
                    PrefUtil.setSignupCompanyType(SignupPictureRegistration.this, "company");
                }
            }
        }

        if(PrefUtil.getSignupCompanyType(SignupPictureRegistration.this).equals("individual")) {
            getBind().btnConfirm.setText(getString(R.string.signup_basic_information_request));

            if(chauffeurStatusVO != null && chauffeurStatusVO.getChauffeurRjctInfoList() != null) {
                for(String tmp : chauffeurStatusVO.getChauffeurRjctInfoList()) {
                    // 승인 보류중 부가정보 입력도 보류 되었으면 버튼으르 다음으로 변경해준다.
                    if(tmp.equals(AppDef.ChauffeurRegInfoCat.ETC.toString())) {
                        getBind().btnConfirm.setText(getString(R.string.signup_basic_information));
                        break;
                    }
                }
            }
            else {
                getBind().btnConfirm.setText(getString(R.string.signup_basic_information));
            }
        }
        else {
            getBind().btnConfirm.setText(getString(R.string.signup_basic_information_request));
        }

        getBind().btnConfirm.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                playLoadingViewAnimation();

                HashMap<String, Object> params = new HashMap<>();
                params.put("appToken", PrefUtil.getPushKey(SignupPictureRegistration.this));               // 앱토큰
                params.put("mobileNo", PrefUtil.getRegPhoneNo(SignupPictureRegistration.this));

                DataInterface.getInstance().registChauffeurStatus(SignupPictureRegistration.this, params, new DataInterface.ResponseCallback<ResponseData<Object>>() {
                    @Override
                    public void onSuccess(ResponseData<Object> response) {
                        cancelLoadingViewAnimation();

                        if ("S000".equals(response.getResultCode())) {
                            // 개인일 경우만 부가정보 페이지로 이동한다.
                            if(PrefUtil.getSignupCompanyType(SignupPictureRegistration.this).equals("individual")) {
                                if(chauffeurStatusVO != null && chauffeurStatusVO.getChauffeurRjctInfoList() != null) {
                                    for(String tmp : chauffeurStatusVO.getChauffeurRjctInfoList()) {
                                        // 승인 보류중 부가정보 입력도 보류 되었으면 부가정보 페이지로 보내준다.
                                        if(tmp.equals(AppDef.ChauffeurRegInfoCat.ETC.toString())) {
                                            getRegistChauffeurInfo(SignupPictureRegistration.this, AppDef.ChauffeurRegInfoCat.ETC.toString(), joinType, chauffeurStatusVO, svcStatus,false);
                                            return;
                                        }
                                    }

                                    signupCompletePage();
                                }

                                getRegistChauffeurInfo(SignupPictureRegistration.this, AppDef.ChauffeurRegInfoCat.ETC.toString(), joinType, chauffeurStatusVO, svcStatus,false);
                            }
                            else {
                                signupCompletePage();
                            }
                        }
                    }

                    @Override
                    public void onError(ResponseData<Object> response) {
                        cancelLoadingViewAnimation();
                        showDialog(SignupPictureRegistration.this, null, "네트웍상태를 확인해 주세요.");
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        cancelLoadingViewAnimation();
                        showDialog(SignupPictureRegistration.this, null, "네트웍상태를 확인해 주세요.");
                    }
                });
            }
        });

        if ("Y".equals(getIntent().getStringExtra("oneButton"))) {
            getBind().btnBefore.setVisibility(View.GONE);
        }
    }

    private void signupCompletePage() {
        Intent intent = new Intent(SignupPictureRegistration.this, SignupRequestInformation.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("information_finish", true);
        intent.putExtra("svcStatus", getResources().getString(R.string.signup_complete_informaiton_chauffeur));

        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    private void validNextCheck() {
        getBind().btnConfirm.setEnabled(isChauffeur && isLicense);
    }

    private void showSelectPhotoDialog() {
        SelectPhotoDialog dialog = new SelectPhotoDialog(this, new SelectPhotoDialog.OnSelectPhotoDialogListener() {
            @Override
            public void onCameraClick() {
                srcUri = Util.getOutputMediaFileName(SignupPictureRegistration.this);
                Util.showCamera(SignupPictureRegistration.this, Util.fromFile(SignupPictureRegistration.this, srcUri));
            }

            @Override
            public void onGalleryClick() {
                Util.showGallery(SignupPictureRegistration.this);
            }
        });
        dialog.show();
    }

    private void showDialog(Context context, String title, String msg) {
        macaronCustomDialog = new MacaronCustomDialog(context, title, msg, "확인", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                macaronCustomDialog.dismiss();
                finish();
            }
        });

        try {
            macaronCustomDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadImage(String fileCat, String uri) {
        HashMap<String, Object> map = new HashMap<>();

        map.put("mobileNo", PrefUtil.getRegPhoneNo(this));
        map.put("fileCat", fileCat);

        playLoadingViewAnimation();

        DataInterface.getInstance().uploadImage(this, map, uri, new DataInterface.ResponseCallback<ResponseData<RegistPictureVO>>() {
            @Override
            public void onSuccess(ResponseData<RegistPictureVO> response) {
                cancelLoadingViewAnimation();
                if ("S000".equals(response.getResultCode())) {
                    Logger.d("uploadImage success. imgUrl = " + response.getData().getImgUrl());
                    Glide.with(SignupPictureRegistration.this)
                            .load(response.getData().getImgUrl())
                            .into("CHAUFFEUR".equals(fileCat) ? getBind().imageFace : getBind().imageLicense);

                    if("CHAUFFEUR".equals(fileCat)) {
                        isChauffeur = true;
                    }
                    else {
                        isLicense = true;
                    }

                    validNextCheck();
                }
            }

            @Override
            public void onError(ResponseData<RegistPictureVO> response) {
                cancelLoadingViewAnimation();
                showDialog(SignupPictureRegistration.this, null, "네트웍상태를 확인해 주세요.");
            }

            @Override
            public void onFailure(Throwable t) {
                cancelLoadingViewAnimation();
                showDialog(SignupPictureRegistration.this, null, "네트웍상태를 확인해 주세요.");
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(chauffeurStatusVO != null && chauffeurStatusVO.getChauffeurRjctInfoList() != null) {
            if(chauffeurStatusVO.getChauffeurRjctInfoList().get(0).equals("PROFILE")) {
                //finishAffinity();
                Intent intent = new Intent(this, SignupFailInformation.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                intent.putExtra("joinType", joinType);
                intent.putExtra("chauffeurStatusVO", chauffeurStatusVO);
                intent.putExtra("svcStatus", svcStatus);
                if(chauffeurRegInfoCat != null)
                    intent.putExtra("chauffeurRegInfoCat", chauffeurRegInfoCat);

                startActivity(intent);
                overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                finish();
                return;
            }
        }

        getRegistChauffeurInfo(SignupPictureRegistration.this, AppDef.ChauffeurRegInfoCat.MEMBER.toString(), joinType, chauffeurStatusVO, svcStatus,true);
    }
}
