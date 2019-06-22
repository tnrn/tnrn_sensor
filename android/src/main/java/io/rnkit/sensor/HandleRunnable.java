package io.rnkit.sensor;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.react.bridge.Callback;
import com.yanzhenjie.kalle.simple.SimpleCallback;
import com.yanzhenjie.kalle.simple.SimpleResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by carlos on 2017/8/17.
 * 用来跑任务的Runnable
 */

class HandleRunnable  {

    private Context context;
    private SharedPreferences sharedPreferences;
    private SensorPackage.SensorEventCallback mSensorEventCallback;

    HandleRunnable(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(DBJsModule.class.getName(), Context.MODE_PRIVATE);
    }

    public HandleRunnable(Context context, SensorPackage.SensorEventCallback sensorEventCallback) {
        this(context);
        mSensorEventCallback = sensorEventCallback;
    }

    public synchronized void execute() {
        if (StaticUtil.appKey.equals("")) {
            return;
        }
        //如果没有网络，就终止循环
        if (!StaticUtil.isNetworkAvailable(context)) {
            return;
        }
        List<DBModel> dbModels = DBManager.getInstance(context).getUnSend();
        if (dbModels.size() > 0) {
            for (final DBModel dbModel : dbModels) {
                if (dbModel.times > StaticUtil.REPEAT_TIMES && dbModel.priority > 0) {
                    DBManager.getInstance(context).delete(dbModel.id);
                    // 上传失败删除
                    onError(dbModel, new Exception("事件因为失败次数过多而删除"));
                    //打印日志
                    if(StaticUtil.isSensorLog){
                        String eventType = StaticUtil.getEventType(dbModel.jsonBody);
                        if (eventType != null && !eventType.equals(StaticUtil.logEvent)) {
                            DBManager.getInstance(context).save(StaticUtil.addLog(dbModel.jsonBody,"事件因为失败次数过多而删除"), dbModel.requestUrl, 0);
                        }
                    }
                    int failTimes = sharedPreferences.getInt(StaticUtil.KEY_FAIL_TIMES, 0);
                    sharedPreferences.edit().putInt(StaticUtil.KEY_FAIL_TIMES, ++failTimes).apply();
                    continue;
                }
                //这里发送给后台
                try {
                    JSONObject jsonObject = new JSONObject();
                    long timeStamp = System.currentTimeMillis();
                    jsonObject.put("timestamp", timeStamp);
                    jsonObject.put("distinct_id", StaticUtil.deviceId);
                    jsonObject.put("bizType", "B005");
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.put(new JSONObject(dbModel.jsonBody));
                    jsonObject.put("events", jsonArray);

                    final String jsonBody = jsonObject.toString();
                    String url = TextUtils.isEmpty(StaticUtil.url) ? dbModel.requestUrl : StaticUtil.url;
                    StaticUtil.sendOkHttp(
                            url,
                            jsonObject.toString(),
                            timeStamp,
                            new SimpleCallback<Object>() {
                                @Override
                                public void onResponse(SimpleResponse<Object, String> response) {
                                    if (response.isSucceed()) {
                                        DBManager.getInstance(context).delete(dbModel.id);
                                        mSensorEventCallback.onComplete(dbModel.requestUrl, jsonBody);
                                        // 成功
                                        if (BuildConfig.DEBUG) {
                                            Log.e("TouNa", "上传埋点事件成功");
                                        }
                                    }
                                }

                                @Override
                                public void onException(Exception e) {
                                    super.onException(e);
                                    Log.e("TouNa", "onResponse error: " + e.toString(), e);
                                    mSensorEventCallback.onError(dbModel.requestUrl, jsonBody, e);
                                }
                            });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void onError(DBModel dbModel, Exception e) {
        if (this.mSensorEventCallback != null) {
            this.mSensorEventCallback.onError(dbModel.requestUrl, dbModel.jsonBody, e);
        }
    }

    private void onComplete(DBModel dbModel) {
        if (this.mSensorEventCallback != null) {
            this.mSensorEventCallback.onComplete(dbModel.requestUrl, dbModel.jsonBody);
        }
    }
}
