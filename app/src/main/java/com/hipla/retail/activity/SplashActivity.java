package com.hipla.retail.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.databinding.BindingAdapter;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.hipla.retail.R;
import com.hipla.retail.application.MainApplication;
import com.hipla.retail.db.Db_helper;
import com.hipla.retail.helper.MarshmallowPermissionHelper;
import com.hipla.retail.model.Login_model;
import com.hipla.retail.model.ZoneInfo;
import com.hipla.retail.networking.NetworkUtility;
import com.navigine.naviginesdk.NavigineSDK;

import io.paperdb.Paper;

public class SplashActivity extends AppCompatActivity {

    private static final int REQUEST_ALL_PERMISSION = 1000;
    private Login_model login_model;
    private String TAG="Retail";
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        login_model = new Login_model();

        initView();
    }

    private void initView() {

        setUpZoneData();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (haveNetworkConnection()) {
                    checkNavinginePermissions();
                }else{
                    showNetConnectionDialog();
                }
            }
        }, 3000);

    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    private void showNetConnectionDialog() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(SplashActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(SplashActivity.this);
        }
        builder.setTitle("No Internet Connection")
                .setMessage("Please enable your network connection to proceed")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        if (haveNetworkConnection()) {
                            checkNavinginePermissions();
                        }else{
                            showNetConnectionDialog();
                        }
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void checkNavinginePermissions() {
        if (Build.VERSION.SDK_INT > 22) {
            if (MarshmallowPermissionHelper.getAllNaviginePermission(null
                    , this, REQUEST_ALL_PERMISSION)) {
                if(!MainApplication.isNavigineInitialized) {
                    (new InitTask(this)).execute();
                }else{
                    if(Paper.book().read(NetworkUtility.USER_INFO,null)!=null){
                        startActivity(new Intent(SplashActivity.this, ScaningActivity.class));
                        overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
                        supportFinishAfterTransition();
                    }else{
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
                        supportFinishAfterTransition();
                    }
                }
            }
        } else {
            if(!MainApplication.isNavigineInitialized) {
                (new InitTask(this)).execute();
            }else{
                if(Paper.book().read(NetworkUtility.USER_INFO,null)!=null){
                    startActivity(new Intent(SplashActivity.this, ScaningActivity.class));
                    overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
                    supportFinishAfterTransition();
                }else{
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
                    supportFinishAfterTransition();
                }
            }
        }
    }

    @BindingAdapter("app:font_lite")
    public static  void setFont_lite(TextView tv , Typeface typeface){
        AssetManager assetManager = tv.getContext().getAssets();
        typeface = Typeface.createFromAsset(assetManager, "fonts/helviticaneulight.ttf");

        tv.setTypeface(typeface);
    }

    @BindingAdapter("app:font_thin")
    public static  void setFont_thin(TextView tv , Typeface typeface){

        AssetManager assetManager = tv.getContext().getAssets();
        typeface = Typeface.createFromAsset(assetManager, "fonts/helviticaneuthin.ttf");

        tv.setTypeface(typeface);
    }

    class InitTask extends AsyncTask<Void, Void, Boolean> {
        private Context mContext = null;
        private String mErrorMsg = null;

        public InitTask(Context context) {
            mContext = context.getApplicationContext();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            if (!MainApplication.initialize(getApplicationContext())) {
                mErrorMsg = "Error downloading location information! Please, try again later or contact technical support";
                return Boolean.FALSE;
            }
            Log.d(TAG, "Initialized!");
            if (!NavigineSDK.loadLocation(MainApplication.LOCATION_ID, 60)) {
                mErrorMsg = "Error downloading location information! Please, try again later or contact technical support";
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            if (result.booleanValue()) {
                // Starting main activity
                if(Paper.book().read(NetworkUtility.USER_INFO,null)!=null){
                    startActivity(new Intent(SplashActivity.this, ScaningActivity.class));
                    overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
                    supportFinishAfterTransition();
                }else{
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
                    supportFinishAfterTransition();
                }
            } else {
                Toast.makeText(mContext, mErrorMsg, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }
    }

    // Callback with the request from calling requestPermissions(...)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        switch (requestCode) {
            case REQUEST_ALL_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[3] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[4] == PackageManager.PERMISSION_GRANTED) {
                    if(!MainApplication.isNavigineInitialized) {
                        (new InitTask(this)).execute();
                    }
                }
                return;
            }

            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    private void setUpZoneData(){

        Db_helper db_helper = new Db_helper(getApplicationContext());

        db_helper.insert_zone(new ZoneInfo(1,"15.78,39.44","14.37,41.94","19.24,40.17","17.20,36.32","12.21,38.30"));
        db_helper.insert_zone(new ZoneInfo(2,"14.38,33.31","14.17,35.90","17.77,34.94","18.03,31.22","14.48,31.19"));
        db_helper.insert_zone(new ZoneInfo(3,"19.02,28.62","17.10,30.60","20.94,30.53","21.27,24.78","16.91,24.65"));
        db_helper.insert_zone(new ZoneInfo(4,"22.76,19.14","20.79,23.08","25.84,22.56","25.90,13.13","19.12,15.92"));
        db_helper.insert_zone(new ZoneInfo(5,"15.44,21.66","14.19,23.61","18.74,23.16","18.23,19.60","14.15,19.53"));
        db_helper.insert_zone(new ZoneInfo(6,"16.34,13.41","12.74,14.22","16.70,14.13","16.55,8.03","12.53,8.29"));
        db_helper.insert_zone(new ZoneInfo(7,"6.73,41.08","4.32,45.01","8.55,44.91","9.13,36.06","3.87,36.16"));

    }

}
