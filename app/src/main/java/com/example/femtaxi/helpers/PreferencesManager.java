package com.example.femtaxi.helpers;

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

    /*public void guardarUsuario(User user) {
        Gson gson = new Gson();
        String json = gson.toJson(user);
        mEditor = mPreferences.edit();
        mEditor.putString(KEY_USER, json);
        mEditor.apply();
    }

    public User guardarUsuario() {
        Gson gson = new Gson();
        String json = mPreferences.getString(KEY_USER, "");
        if (json.equals("")) {
            guardarUsuario(null);
            json = mPreferences.getString(KEY_USER, null);
        }
        return gson.fromJson(json, User.class);
    }*/
}
