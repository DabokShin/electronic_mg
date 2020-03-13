package kst.ksti.chauffeur.net;


import java.io.IOException;

import kst.ksti.chauffeur.common.Global;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BasicDataInterface {

	private HttpService httpService;

	BasicDataInterface() {
		setService(Global.getBaseUrl());
	}

	BasicDataInterface(String url) {
		setService(url);
	}

	public HttpService getService() {
		return httpService;
	}

	public void setService(HttpService httpService) {
		this.httpService = httpService;
	}

	private void setService(String url) {
		HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
		interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);  //상용으로 올릴때는 false로

		Interceptor interceptor1 = new Interceptor() {
			@Override
			public Response intercept(Chain chain) throws IOException {
				Request request = chain.request().newBuilder()
						.addHeader("Connection", "close")
						.addHeader("accept", "application/json")
						.build();
				return chain.proceed(request);
			}
		};

		OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).addInterceptor(interceptor1).build();
		Retrofit retrofit = new Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create()).client(client).build();

		setService(retrofit.create(HttpService.class));
	}


//	public BasicDataInterface(okhttp3.OkHttpClient a) {
//
//		Retrofit retrofit = new Retrofit.Builder()
//				.baseUrl(Global.HOST_ADDRESS_DEV)
//				.addConverterFactory(GsonConverterFactory.create())
//				//	.client(getHeader("Bearer " + MacaronApp.chauffeur.accessToken))
//				.client(a)
//				.build();
//		service = retrofit.create(HttpService.class);
//	}


//	public OkHttpClient getHeader( ) {
//
//		final String token = "Bearer " + MacaronApp.chauffeur.accessToken;
//		HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
//		interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//		OkHttpClient okClient = new OkHttpClient.Builder()
//				.addInterceptor(interceptor)
//				.addNetworkInterceptor(
//						new Interceptor() {
//							@Override
//							public Response intercept(Interceptor.Chain chain) throws IOException {
//								Request request = null;
//								if (token != null) {
//									Log.d("--Authorization-- ", token);
//
//									Request original = chain.request();
//									// Request customization: add request headers
//									Request.Builder requestBuilder = original.newBuilder()
//											.addHeader("Authorization", token);
//
//									request = requestBuilder.build();
//								}
//								return chain.proceed(request);
//							}
//						})
//				.build();
//		return okClient;
//
//	}

}
