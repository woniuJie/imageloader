package com.culiu.core.imageloader;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.Version;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by xujianbo on 2017/6/21.
 */
public class OkHttpConfig {

    private static final String TAG = "ImageLoader";

    public static final int DEFAULT_CONNECT_TIMEOUT = 10;

    public static final int DEFAULT_READ_TIMEOUT = 15;

    public static final int DEFAULT_WRITE_TIMEOUT = 15;

    private OkHttpClient mOkHttpClient;

    public OkHttpClient getOkHttpClient() {
        if (mOkHttpClient == null) {
            SSLHelper.SSLParams sslParams = SSLHelper.getSslSocketFactory(null, null, null);
            mOkHttpClient = new OkHttpClient.Builder()
                    .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
                    .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(DEFAULT_READ_TIMEOUT * 2, TimeUnit.SECONDS)
                    .writeTimeout(DEFAULT_WRITE_TIMEOUT * 2, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .addInterceptor(getUserAgentInterceptor())
                    .addInterceptor(getHttpLoggingInterceptor())
                    .build();
        }
        return mOkHttpClient;
    }

    private HttpLoggingInterceptor getHttpLoggingInterceptor() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(
                new HttpLoggingInterceptor.Logger() {
                    @Override
                    public void log(String message) {
                        if (ImageLoader.getInstance().isDebuggable()) {
                            Log.i(TAG, message);
                        }
                    }
                });
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        return loggingInterceptor;
    }

    private Interceptor getUserAgentInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Request.Builder newBuilder = request.newBuilder();
                newBuilder.addHeader("User-Agent", getUserAgentInfo());
                return chain.proceed(newBuilder.build());
            }
        };
    }

    private String getUserAgentInfo() {
        String deviceInfo = getDefaultUserAgent() + " " + Version.userAgent();
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0, length = deviceInfo.length(); i < length; i++) {
            char c = deviceInfo.charAt(i);
            if ((c <= '\u001f' && c != '\u0009' /* htab */) || c >= '\u007f') {
                // 转为unicode码
                stringBuffer.append(String.format("\\u%04x", (int) c));
            } else {
                stringBuffer.append(c);
            }
        }
        return stringBuffer.toString();
    }

    private String getDefaultUserAgent() {
        return System.getProperty("http.agent");
    }

}
