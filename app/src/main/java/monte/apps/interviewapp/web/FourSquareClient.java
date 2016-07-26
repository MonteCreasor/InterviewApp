package monte.apps.interviewapp.web;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by monte on 2016-07-17.
 */

public class FourSquareClient {
    /** Logging tag. */
    private static final String TAG = "FourSquareClient";

    public static final String VERSION = "20160718";
    public static final int ICON_SIZE = 64;

    /**
     * Base URL for FourSquare query service.
     */
    private static final String BASE_URL =
            "https://api.foursquare.com/v2/";
    private static final String ClientID =
            "K1Z2FUTK0SNVMIPGWW1W3V3LTTSURYMPBCUSPURCI315M1Q2";
    private static final String CLIENT_ID_PARAM = "client_id";
    private static final String CLIENT_SECRET_PARAM = "client_secret";
    private static final String ClientSecret =
            "5BTQVHFWLCQGMQPPZXTSAI4JT0AADCPBHCBC0RCKTP1DLLJX";
    private static final String VERSION_PARAM = "v";


    private static FourSquareApi sFourSquareApi;

    /**
     * Rest adapter for FourSquare server. The API key is automatically added to
     * each query.
     *
     * @return
     */
    public static synchronized FourSquareApi buildRestAdapter() {

        if (sFourSquareApi != null) {
            return sFourSquareApi;
        }

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(chain -> {
            Request original = chain.request();
            HttpUrl originalHttpUrl = original.url();

            HttpUrl url = originalHttpUrl.newBuilder()
                    .addQueryParameter(CLIENT_ID_PARAM, ClientID)
                    .addQueryParameter(CLIENT_SECRET_PARAM, ClientSecret)
                    .addQueryParameter(VERSION_PARAM, VERSION)
                    .build();

            // Request customization: add api key and secret.
            Request.Builder requestBuilder = original.newBuilder().url(url);
            Request request = requestBuilder.build();
            Log.d(TAG, "Get URL: " + url);
            return chain.proceed(request);
        });

        /*
                client.interceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response response = chain.proceed(chain.request());

                Request original = chain.request();
                HttpUrl originalHttpUrl = original.url();

                HttpUrl url = originalHttpUrl.newBuilder()
                        .addQueryParameter(CLIENT_ID_PARAM, ClientID)
                        .addQueryParameter(CLIENT_SECRET_PARAM, ClientSecret)
                        .build();

                // Request customization: add api key and secret.
                Request.Builder requestBuilder = original.newBuilder().url(url);
                Request request = requestBuilder.build();

                return response;
            }
        });
         */

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        sFourSquareApi = retrofit.create(FourSquareApi.class);

        return sFourSquareApi;
    }
}
