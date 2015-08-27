package in.ureport;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.activeandroid.ActiveAndroid;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import in.ureport.managers.AmazonServicesManager;
import in.ureport.managers.UserManager;
import in.ureport.managers.FirebaseManager;
import io.fabric.sdk.android.Fabric;

/**
 * Created by johncordeiro on 7/9/15.
 */
public class UreportApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i("FirebaseManager", "init " + getString(R.string.firebase_app));

        FirebaseManager.init(this);
        initializeFabric();
        ActiveAndroid.initialize(this);
        AmazonServicesManager.init(this);
    }

    private void initializeFabric() {
        TwitterAuthConfig authConfig = new TwitterAuthConfig(getString(R.string.twitter_key), getString(R.string.twitter_secret));
        Fabric.with(this, new Twitter(authConfig));
    }

    public static boolean validateUserLogin(Context context) {
        return UserManager.validateKeyAction(context);
    }
}
