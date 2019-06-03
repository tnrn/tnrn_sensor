package io.rnkit.sensor;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Base64;

import com.facebook.react.bridge.Callback;
import com.yanzhenjie.kalle.Canceller;
import com.yanzhenjie.kalle.Kalle;
import com.yanzhenjie.kalle.RequestBody;
import com.yanzhenjie.kalle.StringBody;
import com.yanzhenjie.kalle.simple.SimpleCallback;
import com.yanzhenjie.kalle.simple.SimpleResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.internal.Util;

/**
 * Created by carlos on 2017/8/17.
 * 网络请求方法
 */

class StaticUtil {

    //    static ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

    /**
     * 一次从数据库中取的最大数量
     */
    static int MAX_VOLUME = 20;
    /**
     * 是否打印日志
     */
    static boolean isSensorLog = false;
    /**
     * 服务器分配的appKey
     */
    static String appKey = "";

    static int REPEAT_TIMES = 3;

    static String deviceId = "";

    static final String logEvent = "evnt_ckapp_log_collect";

    static final String KEY_FAIL_TIMES = "failTimes";


    private static final int CORE_POOL_SIZE = 3;
    private static final int MAX_POOL_SIZE = 5;
    private static final int KEEP_ALIVE_TIME = 120;
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;
    private static final BlockingQueue<Runnable> WORK_QUEUE = new LinkedBlockingQueue<Runnable>();
    private static ExecutorService executorService;

    static synchronized ExecutorService createExecutorService() {
        if (executorService == null) {
            executorService = new ThreadPoolExecutor(
                    CORE_POOL_SIZE,
                    MAX_POOL_SIZE,
                    KEEP_ALIVE_TIME,
                    TIME_UNIT,
                    WORK_QUEUE,
                    Util.threadFactory("StaticUtil Dispatcher", false));
        }

        return executorService;
    }


    static Canceller sendOkHttp(String url, final String hashString, long timeStamp, SimpleCallback<Object> callback) {
        return Kalle.post(url)
                .addHeader("content-md5", getMD5(hashString + appKey + timeStamp).toLowerCase())
                .addHeader("content-timestamp", String.valueOf(timeStamp))
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept-Encoding", "gzip")
                .body(new StringBody(gzip(hashString)))
                .perform(callback);
    }

    public static String gzip(String primStr) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = null;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(primStr.getBytes("UTF-8"));
        } catch (IOException e) {
        } finally {
            if (gzip != null) {
                try {
                    gzip.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);
    }

    /**
     * 检测当的网络（WLAN、3G/2G）状态
     *
     * @param context Context
     * @return true 表示网络可用
     */
    static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }

    private static String getMD5(String message) {
        String md5str = "";
        try {
            // 1 创建一个提供信息摘要算法的对象，初始化为md5算法对象
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 2 将消息变成byte数组
            byte[] input = message.getBytes();

            // 3 计算后获得字节数组,这就是那128位了
            byte[] buff = md.digest(input);

            // 4 把数组每一字节（一个字节占八位）换成16进制连成md5字符串
            md5str = bytesToHex(buff);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return md5str;
    }

    /**
     * 二进制转十六进制
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder md5str = new StringBuilder();
        // 把数组每一字节换成16进制连成md5字符串
        int digital;
        for (byte aByte : bytes) {
            digital = aByte;

            if (digital < 0) {
                digital += 256;
            }
            if (digital < 16) {
                md5str.append("0");
            }
            md5str.append(Integer.toHexString(digital));
        }
        return md5str.toString().toUpperCase();
    }

    static String getDeviceId(Context context) {
        String deviceId = "";
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            deviceId = tm.getDeviceId();
        } catch (Exception e) {
        }
        return deviceId;
    }

    /**
     * 返回日志事件的字符串
     */
    public static String addLog(String eventString, String log) {
        try {
            JSONObject jsonObject = new JSONObject(eventString);
            String eventType = jsonObject.optString("event_type");
            long eventTime = jsonObject.optLong("seq_tns");
            String eventPhone = jsonObject.optJSONObject("fields").optString("phone_no");
            JSONObject logJSON = new JSONObject();
            logJSON.put("event_type", logEvent);
            logJSON.put("seq_tns", System.currentTimeMillis());
            JSONObject logContent = new JSONObject();
            logContent.put("phone_no", eventPhone);
            logContent.put("eventType", eventType);
            logContent.put("eventTime", eventTime);
            logContent.put("log", log + "--" + eventType + "--" + eventTime + "--" + eventPhone);
            logJSON.put("fields", logContent);
            return logJSON.toString();
        } catch (JSONException ignored) {
        }
        return null;
    }

    /**
     * @return 返回事件的名称
     */
    public static String getEventType(String eventString) {
        try {
            JSONObject jsonObject = new JSONObject(eventString);
            return jsonObject.optString("event_type");
        } catch (JSONException ignored) {
        }
        return null;
    }
}
