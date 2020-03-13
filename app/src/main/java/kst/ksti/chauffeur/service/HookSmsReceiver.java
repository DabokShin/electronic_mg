package kst.ksti.chauffeur.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.subjects.PublishSubject;

public class HookSmsReceiver extends BroadcastReceiver {

    public static PublishSubject<String> mSubject = PublishSubject.create();

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent == null) return;

        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();

            if(extras == null) return;

            Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);

            switch (status.getStatusCode()) {
                case CommonStatusCodes.SUCCESS: {
                    String otp = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                    String authCode = parseSmsMessage(otp);

                    if(authCode == null) return;
                    if(!authCode.isEmpty()) mSubject.onNext(authCode);
                }

                case CommonStatusCodes.TIMEOUT: {

                }
            }
        }
    }

    @Nullable
    private String parseSmsMessage(String msg) {
        Matcher p = Pattern.compile("\\d{6}").matcher(msg);

        if (p.find() && msg.contains("배달")) {
            return p.group(0);
        }

        return "";
    }
}
