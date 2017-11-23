package com.apitest.newapi;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.apitest.oval.R;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button newBtn, existingBtn;
    private ProgressDialog dialog;

    private int click = 0;

    private String API_URL = "http://fm-ogw.meetoval.com/api/app-svc/clients/get_app_url?token=";
    private String NEW_TOKEN = "8819YRGEK3UQ9SZU4YUS5JS7E02PXOSL";
    private String EXISTING_TOKEN = "KBX9IL945LXP023L23U1FGO3PIDDO0G1";
    private String webUrl = "";

    private Toolbar toolbar;

    private CustomTabsClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        if(getSupportActionBar() != null)
        {
            setSupportActionBar(toolbar);
        }

        newBtn = findViewById(R.id.btn_new);
        existingBtn = findViewById(R.id.btn_existing);

        newBtn.setOnClickListener(this);
        existingBtn.setOnClickListener(this);

        CustomTabsServiceConnection mConnection = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient customTabsClient) {
                mClient = customTabsClient;
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mClient = null;
            }
        };

        String packageName = "com.android.chrome";
        CustomTabsClient.bindCustomTabsService(this, packageName, mConnection);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn_new:
                click = 0;
                new APIcalling().execute();
                Log.d("result: ", "API Called");
                break;
            case R.id.btn_existing:
                click = 1;
                new APIcalling().execute();
                Log.d("result: ", "API Called");
                break;
        }
    }

    private class APIcalling extends AsyncTask<String, String, String>
    {
        String response = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage("Please Wait");
            dialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {

            try {
                URL url = null;
                if (click == 0)
                {
                    url = new URL(API_URL + NEW_TOKEN);
                }
                else if(click == 1)
                {
                    url = new URL(API_URL + EXISTING_TOKEN);
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Error: URL not found", Toast.LENGTH_SHORT).show();
                }

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    response = stringBuilder.toString();
                    return response;
                }
                finally{
                    urlConnection.disconnect();
                }
            }
            catch(Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);

            if(response == null) {
                response = "THERE WAS AN ERROR";
                Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
            }

            System.out.println("response>> "+response);

            try {
                JSONObject jsonObj = new JSONObject(response);
                String params = jsonObj.getString("params");
                JSONObject jsonObjParam = new JSONObject(params);
                webUrl = jsonObjParam.getString("web_url");

                dialog.dismiss();
                loadCustomTabs();
                Log.d("INFO: ", response);

                System.out.println("web_url: "+ webUrl);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private CustomTabsSession getSession() {
        return mClient.newSession(new CustomTabsCallback() {
            @Override
            public void onNavigationEvent(int navigationEvent, Bundle extras) {
                super.onNavigationEvent(navigationEvent, extras);
            }
        });
    }

    public void loadCustomTabs() {
        CustomTabsIntent.Builder mBuilder = new CustomTabsIntent.Builder(getSession());
        mBuilder.setShowTitle(true);
        mBuilder.setToolbarColor(ContextCompat.getColor(this, R.color.indigo_500));
        mBuilder.setCloseButtonIcon(BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_arrow_back_white_24dp));
        mBuilder.setStartAnimations(this, R.anim.slide_in_right, R.anim.slide_out_left);
        mBuilder.setExitAnimations(this, R.anim.slide_in_left, R.anim.slide_out_right);
        CustomTabsIntent mIntent = mBuilder.build();
        System.out.println("openURL: " + webUrl);
        mIntent.launchUrl(this, Uri.parse(webUrl));
    }
}
