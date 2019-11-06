package retrofit.Utils;

import android.content.Context;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit.Constans;
import retrofit.FileUtils;
import retrofit.NetworkUtils;

/**
 * @作者JTL.
 * @日期2018/3/15.
 * @说明：OkHttpClient帮助类
 */

public class OkHttpClientHelper {
    private static final String TAG = OkHttpClientHelper.class.getName();
    private OkHttpClient okHttpClient;
    private static Context sContext;

    private OkHttpClientHelper() {
        init();
    }

    public static OkHttpClientHelper getInstance(Context context) {
        sContext = context;
        return OkHttpClientHolder.instance;
    }

    /**
     * OkHttpClient 基本设置
     */
    private void init() {

        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        //添加HttpLogging拦截器，方便观察，上传和返回json
//        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//        builder.addInterceptor(loggingInterceptor);
        builder.addInterceptor(new LogInterceptor());
//        CookieJarImpl cookieJar=new CookieJarImpl(new PersistentCookieStore(AppContext.getContext()));
//        List<Cookie> cookies = cookieJar.getCookieStore().getCookies();
//        List<Cookie> cookies1 = cookieJar.getCookieStore().get(HttpUrl.parse(ConstansLogin.LoginUrl));
//        Log.i("getcookies", ConstansLogin.LoginUrl + "," + cookies != null ? cookies.toString() : "cookies为空"+","+cookies1 != null ? cookies1.toString() : "cookies1为空");

        CookieJar cookieJar = new CookieJar() {
            private final HashMap<HttpUrl, List<Cookie>> cookieStore = new HashMap<>(16);

            @Override
            public void saveFromResponse(HttpUrl url, @NotNull List<Cookie> cookies) {
                cookieStore.put(HttpUrl.parse(url.host()), cookies);
                Log.i("savecookies", url.host() + "," + cookies.toString());
            }

            @NotNull
            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
//                List<Cookie> cookies = cookieStore.get(url.host());
                List<Cookie> cookies = cookieStore.get(HttpUrl.parse(url.host()));
                Log.i("getcookies", url.host() != null ? url.host() : "url.host()为空" + "," + cookies != null ? cookies.toString() : "cookies为空");
                return cookies != null ? cookies : new ArrayList<Cookie>();
            }
        };

        //基本设置
        builder.readTimeout(Constans.READ_TIME, TimeUnit.SECONDS)
                .writeTimeout(Constans.WRITE_TIME, TimeUnit.SECONDS)
                .connectTimeout(Constans.CONNECT_TIME, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)//设置出现错误进行重新连接。
                .cookieJar(cookieJar);


        //添加Cache拦截器，有网时添加到缓存中，无网时取出缓存
        Interceptor cacheInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                if (!NetworkUtils.isNetwork(sContext)) {
                    request = request.newBuilder()
                            .cacheControl(CacheControl.FORCE_CACHE)
                            .build();
                }
                Response originalResponse = chain.proceed(request);
                if (NetworkUtils.isNetwork(sContext)) {
                    int maxAge = 60 * 60; // read from cache for 1 minute
                    return originalResponse.newBuilder()
                            .header("Cache-Control", "public, max-age=" + maxAge)
                            .removeHeader("Pragma")
                            .build();
                } else {
                    int maxStale = 60 * 60 * 24 * 28; // tolerate 4-weeks stale
                    return originalResponse.newBuilder()
                            .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                            .removeHeader("Pragma")
                            .build();
                }
            }
        };
        File file = FileUtils.getInstance().getCacheFolder();
        Cache cache = new Cache(file, 1024 * 1024 * 100);
        builder.cache(cache)
//                .addInterceptor(cacheInterceptor)
//                .addNetworkInterceptor(cacheInterceptor);
                .addInterceptor(getInterceptor())
                .addNetworkInterceptor(getNetWorkInterceptor());
        builder.addInterceptor(new RetryInterceptor(2));
        okHttpClient = builder.build();
    }

    private static class OkHttpClientHolder {
        private static OkHttpClientHelper instance = new OkHttpClientHelper();
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    /**
     * 设置返回数据的  Interceptor  判断网络   没网读取缓存
     */
    public Interceptor getInterceptor() {
        return new Interceptor() {
            @NotNull
            @Override
            public Response intercept(@NotNull Chain chain) throws IOException {
                Request request = chain.request();
                if (!NetworkUtils.isNetwork(sContext)) {
                    request = request.newBuilder()
                            .cacheControl(CacheControl.FORCE_CACHE)
                            .build();
                }
                return chain.proceed(request);
            }
        };
    }

    /**
     * 设置连接器  设置缓存
     */
    public Interceptor getNetWorkInterceptor() {
        return new Interceptor() {
            @NotNull
            @Override
            public Response intercept(@NotNull Chain chain) throws IOException {
                Request request = chain.request();
                Response response = chain.proceed(request);
                if (NetworkUtils.isNetwork(sContext)) {
//                    int maxAge = 60 * 60;
//                    int maxAge = 0 * 60;
                    int maxAge = 3;
                    // 有网络时 设置缓存超时时间0个小时
                    response.newBuilder()
                            .header("Cache-Control", "public, max-age=" + maxAge)
                            .removeHeader("Pragma")
                            .build();
                } else {
                    // 无网络时，设置超时为1周
                    int maxStale = 60 * 60 * 24 * 7;
                    response.newBuilder()
                            .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                            .removeHeader("Pragma")
                            .build();
                }
                return response;
            }
        };
    }
}
