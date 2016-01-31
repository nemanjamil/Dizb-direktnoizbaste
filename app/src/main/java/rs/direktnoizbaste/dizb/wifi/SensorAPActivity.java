package rs.direktnoizbaste.dizb.wifi;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import rs.direktnoizbaste.dizb.R;
import rs.direktnoizbaste.dizb.array_adapters.SensorAPListAdapter;


public class SensorAPActivity extends AppCompatActivity {
    WifiManager wifiManager;
    boolean wasWiFiEnabled;

    ListView listView;
    Toolbar toolbar;
    WifiScanReceiver wifiReciever = new WifiScanReceiver();

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_ap);
        listView = (ListView) findViewById(R.id.lv_access_points);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        // get Wifi service
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wasWiFiEnabled = wifiManager.isWifiEnabled();

        //setting progressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        setSupportActionBar(toolbar);
        // enabling wifi
        wifiManager.setWifiEnabled(true);
    }

    protected void onPause() {
        unregisterReceiver(wifiReciever);
        super.onPause();
    }

    protected void onResume() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wifiReciever, intentFilter);
        Log.i("SnesorScan", "onResume");
        super.onResume();
    }

    /*
    function to show dialog
    */
    private void showDialog(String msg) {
        if (!progressDialog.isShowing()) {
            progressDialog.setMessage(msg);
            progressDialog.show();
        }

    }

    /*
    function to hide dialog
     */
    private void hideDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

    private class WifiScanReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            /*TODO Check for intent action*/
            String intentAction = intent.getAction();
            if (wifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intentAction)) {
                List<ScanResult> wifiScanList = wifiManager.getScanResults();
                List<ScanResult> wifiSensorAPList = new ArrayList<ScanResult>();

                for (int i = 0; i < wifiScanList.size(); i++) {
                    if (wifiScanList.get(i).SSID.startsWith("SENZOR"))
                        wifiSensorAPList.add(wifiScanList.get(i));
                }

                if (wifiSensorAPList.size() == 0) showSnack("Nije pronađen ni jedan senzor.\nDa li ste prebacili senzor u mod za podešavanje?");

                listView.setAdapter(new SensorAPListAdapter(getApplicationContext(), wifiScanList));
                hideDialog();
            } else if (wifiManager.WIFI_STATE_CHANGED_ACTION.equals(intentAction)) {
                int wifi_state = intent.getIntExtra(wifiManager.EXTRA_WIFI_STATE, wifiManager.WIFI_STATE_UNKNOWN);
                if (wifi_state == wifiManager.WIFI_STATE_ENABLING) {
                    // show progress dialog
                    showDialog("Uključujem WiFi...");
                } else if (wifi_state == wifiManager.WIFI_STATE_ENABLED) {
                    // hide progress dialog
                    hideDialog();
                    wifiManager.startScan();
                    showDialog("Tražim senzore...");
                } else if (wifi_state == wifiManager.WIFI_STATE_DISABLED) {
                    // hide progress dialog
                    hideDialog();
                }
            }
        }
    }

    private void showSnack(String msg) {
        Snackbar.make(listView, msg, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

}
