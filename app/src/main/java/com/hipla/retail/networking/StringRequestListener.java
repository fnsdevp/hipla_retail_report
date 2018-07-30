package com.hipla.retail.networking;


import org.json.JSONException;

public interface StringRequestListener {

    public void onSuccess(String result, String type) throws JSONException;

    public void onFailure(int responseCode, String responseMessage);

    public void onStarted();
}
