package rs.direktnoizbaste.dizb;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.json.JSONObject;

import rs.direktnoizbaste.dizb.app.SessionManager;
import rs.direktnoizbaste.dizb.array_adapters.SensorListAdapter;
import rs.direktnoizbaste.dizb.callback_interfaces.WebRequestCallbackInterface;
import rs.direktnoizbaste.dizb.dialogs.SensorDeleteConfirmationDialog;
import rs.direktnoizbaste.dizb.dialogs.SensorScanConfirmationDialog;
import rs.direktnoizbaste.dizb.web_requests.AddSensorRequest;
import rs.direktnoizbaste.dizb.web_requests.DeleteSensorRequest;
import rs.direktnoizbaste.dizb.web_requests.PullSensorListRequest;
import rs.direktnoizbaste.dizb.wifi.SensorAPActivity;

public class DrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener, AbsListView.MultiChoiceModeListener {

    private ListView listView;
    private SensorListAdapter customArrayAdapter;
    private Toolbar toolbar;
    private FloatingActionButton fab;

    private ActionMode actionBarReference;
    private SessionManager session;

    private PullSensorListRequest psl;
    private AddSensorRequest asr;
    private DeleteSensorRequest dsr;

    private String uid;

    private int sensor_count = 0; // For debugging only!!! Should be deleted when finished.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        setSupportActionBar(toolbar);
        session = new SessionManager(getApplicationContext());
        uid = session.getUID();

        asr = new AddSensorRequest(DrawerActivity.this);
        asr.setCallbackListener(new WebRequestCallbackInterface() {
            @Override
            public void webRequestSuccess(boolean success, JSONObject[] jsonObjects) {
                if (success) {
                    showSnack("Senzor uspešno dodat.");
                    psl.pullSensorList(uid); // refresh sensor list
                } else {
                    showSnack("Nije uspelo dodavanje senzora!");
                }
            }

            @Override
            public void webRequestError(String error) {
                showSnack(error);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* TODO implement sensor adding action */

//                // Dummy code
//                sensor_count++;
//                //Add new sensor
//                if (sensor_count ==1)
//                    asr.addSensor(uid, "5CCF7F747A7", "4");
//                if (sensor_count ==2)
//                    asr.addSensor(uid, "5CCF7F752BA", "4");
//                if (sensor_count ==3)
//                    asr.addSensor(uid, "18FE349DB7E6", "4");
//                //end dummy code
                final SensorScanConfirmationDialog sensorScanConfirmationDialog = new SensorScanConfirmationDialog(DrawerActivity.this);
                sensorScanConfirmationDialog.setPositiveButtonListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(DrawerActivity.this, SensorAPActivity.class);
                        startActivity(intent);
                    }
                });
                sensorScanConfirmationDialog.setNegativeButtonListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                });
                sensorScanConfirmationDialog.create().show();


            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(this);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(this);

        session = new SessionManager(getApplicationContext());


        psl = new PullSensorListRequest(this);
        psl.setCallbackListener(new WebRequestCallbackInterface() {
            @Override
            public void webRequestSuccess(boolean success, JSONObject[] jsonObjects) {
                // reload sensor list

                if (success) {
                    //successfully loaded sensor list
                    customArrayAdapter = new SensorListAdapter(DrawerActivity.this, jsonObjects);
                    listView.setAdapter(customArrayAdapter);

                } else {
                    showSnack("Nema dodatih senzora.\nKlikni na + da dodaš senzor.");
                    customArrayAdapter = new SensorListAdapter(DrawerActivity.this, new JSONObject[0]);
                    listView.setAdapter(customArrayAdapter);
                }
            }

            @Override
            public void webRequestError(String error) {
                showSnack(error);
            }
        });

        dsr = new DeleteSensorRequest(this);
        dsr.setCallbackListener(new WebRequestCallbackInterface() {
            @Override
            public void webRequestSuccess(boolean success, JSONObject[] jsonObjects) {
                /*TODO create one final success callback, not after each individual sensor delete*/
                if (success) {
                    // refresh sensor list
                    psl.pullSensorList(uid);
                } else {
                    showSnack("Brisanje nije uspelo.");
                    psl.pullSensorList(uid);
                }
            }

            @Override
            public void webRequestError(String error) {
                // refresh sensor list
                showSnack(error);
                psl.pullSensorList(uid);
            }
        });

        psl.pullSensorList(uid);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {

            //If the session is logged in log out
            if (session.isLoggedIn()) {
                /*TODO make actual log out from the server */
                session.setLogin(false);
                Intent intent = new Intent(DrawerActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }

        } else if (id == R.id.nav_add) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(DrawerActivity.this, GraphActivity.class);
        ListAdapter la = listView.getAdapter();
        SensorListAdapter sla = (SensorListAdapter) la;
        intent.putExtra("SensorMAC", sla.getSensorMAC(position));
        startActivity(intent);
    }

    //Multichoice mode listener methods for list view

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        // Here you can do something when items are selected/de-selected,
        // such as update the title in the CAB
        ListAdapter la = listView.getAdapter();
        SensorListAdapter sla = (SensorListAdapter) la;
        sla.selectView(position, checked);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        // Inflate the menu for the CAB
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_sensor_list_cab, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        // Here you can perform updates to the CAB due to
        // an invalidate() request
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        // Respond to clicks on the actions in the CAB
        switch (item.getItemId()) {
            case R.id.action_delete:
                //deleteSelectedItems();
                // create Alert dialog to confirm sensor delete
                SensorDeleteConfirmationDialog sensorDeleteConfirmationDialog = new SensorDeleteConfirmationDialog(DrawerActivity.this);
                sensorDeleteConfirmationDialog.setPositiveButtonListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dsr.deleteSensorListRequest(uid);

                        if (actionBarReference != null)// Action picked, so close the CAB
                            actionBarReference.finish();
                    }
                });
                sensorDeleteConfirmationDialog.setNegativeButtonListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                });
                sensorDeleteConfirmationDialog.create().show();
                actionBarReference = mode;
                //mode.finish(); // Action picked, so close the CAB
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        // Here you can make any necessary updates to the activity when
        // the CAB is removed. By default, selected items are deselected/unchecked.
        // customArrayAdapter.removeSelection();
        ListAdapter la = listView.getAdapter();
        SensorListAdapter sla = (SensorListAdapter) la;
        sla.removeSelection();
    }

    private void showSnack(String msg) {
        Snackbar.make(fab, msg, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

}
