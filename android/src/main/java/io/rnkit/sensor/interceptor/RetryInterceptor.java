package io.rnkit.sensor.interceptor;


import com.yanzhenjie.kalle.Response;
import com.yanzhenjie.kalle.connect.Interceptor;
import com.yanzhenjie.kalle.connect.http.Chain;

import java.io.IOException;

/**
 * 重连拦截器
 * Created on 2019-06-22.
 *
 * @author 老潘
 */
public class RetryInterceptor implements Interceptor {
    /**
     * 默认重连3次
     */
    private int mRetryCount = 3;

    public RetryInterceptor() {
        this(3);
    }

    public RetryInterceptor(int retryCount) {
        this.mRetryCount = retryCount;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        try {
            return chain.proceed(chain.request());
        } catch (IOException e) {
            if (mRetryCount > 0) {
                mRetryCount--;
                return intercept(chain);
            }
            throw e;
        }
    }
}
