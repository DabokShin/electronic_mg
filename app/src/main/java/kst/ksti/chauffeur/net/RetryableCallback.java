package kst.ksti.chauffeur.net;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import kst.ksti.chauffeur.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public abstract class RetryableCallback<T> implements Callback<T> {
    private final Call<T> call;
    private Context context;

    public RetryableCallback(Call<T> call, Context context) {
        this.call = call;
        this.context = context;
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (!DataInterface.isCallSuccess(response) && (context instanceof Activity))
        {
            onFinalResponse(call,response);
        }
        else onFinalResponse(call,response);
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        onFinalFailure(call, t);
    }

    public void onFinalResponse(Call<T> call, Response<T> response) {

    }

    public void onFinalFailure(Call<T> call, Throwable t) {
    }

    private void retry() {
        call.clone().enqueue(this);
    }
}
