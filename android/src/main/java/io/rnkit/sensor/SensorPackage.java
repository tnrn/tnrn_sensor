package io.rnkit.sensor;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.Collections;
import java.util.List;

/**
 * Created by carlos on 2017/8/21.
 */

public class SensorPackage implements ReactPackage {
    private SensorEventCallback mSensorEventCallback;
    public SensorPackage() {
    }

    public SensorPackage(SensorEventCallback sensorEventCallback) {
        this.mSensorEventCallback = sensorEventCallback;
    }

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        return Collections.<NativeModule>singletonList(new DBJsModule(reactContext, mSensorEventCallback));
    }

    @Override
    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Collections.emptyList();
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }

    public interface SensorEventCallback {
        void onError(String url, String jsonBody, Exception e);
        void onComplete(String url, String jsonBody);
    }
}
