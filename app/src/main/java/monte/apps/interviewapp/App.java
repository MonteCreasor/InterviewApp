package monte.apps.interviewapp;

import android.app.Application;
import android.content.Context;

import com.facebook.stetho.Stetho;

/**
 * Created by monte on 2016-07-19.
 */

public class App extends Application {
    private static App sInstance;

    public App() {
        sInstance = this;
    }

    public static App getApp() {
        return sInstance;
    }

    public static Context getContext() {
        return sInstance.getApplicationContext();
    }

    /**
     * Called when the application is starting, before any activity, service, or
     * receiver objects (excluding content providers) have been created.
     * Implementations should be as quick as possible (for example using lazy
     * initialization of state) since the time spent in this function directly
     * impacts the performance of starting the first activity, service, or
     * receiver in a process. If you override this method, be sure to call
     * super.onCreate().
     */
    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
        }
    }
}
