package pe.com.android.femtaxi.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {


    private static PreferencesManager mInstance;

    public SharedPreferences mPreferences;
    public SharedPreferences.Editor mEditor;

    public PreferencesManager(Context context) {
        mPreferences = context.getSharedPreferences(Constants.Preferences.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mEditor = mPreferences.edit();
    }

    public static synchronized PreferencesManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new PreferencesManager(context);
        }
        return mInstance;
    }

    public void setIsClient(boolean isClient) {
        mEditor.putBoolean(Constants.Preferences.PREF_IS_CLIENT, isClient);
        mEditor.apply();
    }

    public boolean getIsClient() {
        return mPreferences.getBoolean(Constants.Preferences.PREF_IS_CLIENT, false);
    }

    public void setIsDriver(boolean isDriver) {
        mEditor.putBoolean(Constants.Preferences.PREF_IS_DRIVER, isDriver);
        mEditor.apply();
    }

    public boolean getIsDriver() {
        return mPreferences.getBoolean(Constants.Preferences.PREF_IS_DRIVER, false);
    }

    public void setToken(String token) {
        //LogUtils.e(TAG, "setToken token: " + token);
        mEditor.putString(Constants.Preferences.PREF_TOKEN, token).commit();
    }

    public String getToken() {
        //LogUtils.e(TAG, "getToken : " + mPreferences.getString(Constants.Preferences.PREF_TOKEN, null));
        return mPreferences.getString(Constants.Preferences.PREF_TOKEN, null);
    }
}
