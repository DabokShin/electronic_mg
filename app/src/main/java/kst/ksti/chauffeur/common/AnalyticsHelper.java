package kst.ksti.chauffeur.common;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.InputStream;

import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.utility.Util;

public class AnalyticsHelper {
	private static AnalyticsHelper mInstance;
	private JsonElement element;
	private Context context;

	private FirebaseAnalytics firebaseAnalytics;

	public static AnalyticsHelper getInstance(Context context) {
		if (AnalyticsHelper.mInstance == null) {
			synchronized (AnalyticsHelper.class) {
				AnalyticsHelper.mInstance = new AnalyticsHelper(context.getApplicationContext());
			}
		}

		return AnalyticsHelper.mInstance;
	}



	public AnalyticsHelper(Context context)
	{
		this.context = context;

		InputStream inputStream = context.getResources().openRawResource(R.raw.ga_screen);
		String jsonString = Util.readJsonFile(inputStream);
		JsonParser parser = new JsonParser();
		element = parser.parse(jsonString).getAsJsonObject().get("screen");
		firebaseAnalytics = FirebaseAnalytics.getInstance(context);
	}

	/**
	 * Google Analytics start
	 *
     */
	public void sendScreenFromJson(Activity activity, String name)
	{
		JsonElement object = element.getAsJsonObject().get(name);

		if(object == null)
		{
			return;
		}

		String screen = object.getAsString();
		sendScreen(activity, screen);
	}

	public void sendScreen(Activity activity, String name)
	{
		firebaseAnalytics.setCurrentScreen(activity, name, null);
	}

	public void sendEvent(String cat, String action, String label, String event)
	{
		Bundle bundle = new Bundle();
		bundle.putString("cat", cat);
		bundle.putString("action", action);
		bundle.putString("label", label);
		firebaseAnalytics.logEvent(event, bundle);
	}
}
