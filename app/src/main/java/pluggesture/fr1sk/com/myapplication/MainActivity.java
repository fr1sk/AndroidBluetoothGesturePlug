package pluggesture.fr1sk.com.myapplication;

import android.app.Activity;
import android.app.SearchManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.app.ListActivity;
import android.app.Activity;
import android.app.ListActivity;
import android.app.SearchManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;


public class MainActivity extends ListActivity {

    private static final String TAG = "MainActivity";
    private final static int REQUEST_ENABLE_BT = 1;
    private static final long SCANNING_TIMEOUT = 3 * 1000; /* 3 seconds */
    TextView rssi, name;
    int bt_rssi;
    String bt_name;

    private BleWrapper BleWrapper = null;
    private DeviceListAdapter mDeviceListAdapter = null;
    private Activity mActivity;
    private Handler mHandler = new Handler();
    SearchView mSearchView;
    SearchManager mSearchManager;
    Button nekibutton = null;

    private BluetoothDevice mDevice;

    private boolean mScanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rssi = (TextView) findViewById(R.id.rssi);
        name = (TextView) findViewById(R.id.name);
/*
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayHomeAsUpEnabled(false);
        getActionBar().setDisplayUseLogoEnabled(false);
        */
        BleWrapper = new BleWrapper(this, new BleWrapperCallbacks.NullAdapter() {
            public void uiDeviceFound(final BluetoothDevice device, final int rssi, final byte[] record) {
                handleFoundDevice(device, rssi, record);
            }
        });



    };





    @Override
    /*
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    */

    public void onResume()
    {
        super.onResume();
        Log.d(TAG, "OnResume");
        // on every Resume check if BT is enabled (user could turn it off while app was in background etc.)
        if (BleWrapper.isBtEnabled() == false) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            // see onActivityResult to check what is the status of our request
        }
        BleWrapper.initialize();
        //BleWrapper.stopScanning();
        if(mDeviceListAdapter != null){
            mDeviceListAdapter.clearList();
        }else{
            mDeviceListAdapter = new DeviceListAdapter(this);
            setListAdapter(mDeviceListAdapter);
        }
        // Automatically start scanning for devices
        mScanning = true;
        // remember to add timeout for scanning to not run it forever and drain the battery
        addScanningTimeout();
        BleWrapper.startScanning();
        invalidateOptionsMenu();
    }
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "OnPause");
        mScanning = false;
        BleWrapper.stopScanning();
        mDeviceListAdapter.clearList();
        mDeviceListAdapter.notifyDataSetChanged();
        invalidateOptionsMenu();
    }

    protected void onStop() {
        super.onStop();
        Log.d(TAG, "OnStop");
        mScanning = false;
        BleWrapper.stopScanning();
        mDeviceListAdapter.clearList();
        mDeviceListAdapter.notifyDataSetChanged();
        //unregisterReceiver(MyBTReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "OnStop");
        mScanning = false;
        BleWrapper.stopScanning();
        mDeviceListAdapter.clearList();
        ;
    }


    /*
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);



        if (mScanning) {
            menu.findItem(R.id.scanning_start).setVisible(false);
            menu.findItem(R.id.scanning_stop).setVisible(true);

        } else {
            menu.findItem(R.id.scanning_start).setVisible(true);
            menu.findItem(R.id.scanning_stop).setVisible(false);

        }

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;



        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(width - 100, FrameLayout.LayoutParams.WRAP_CONTENT);
        mSearchView.setLayoutParams(lp);
        mSearchView.setIconifiedByDefault(false);


        SearchView.OnQueryTextListener textChangeListener = new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String newText) {
                // this is your adapter that will be filtered
                mDeviceListAdapter.getFilter().filter(newText);
                Log.d("MAIN ACTIVITY", "on text chnge text: " + newText);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                // this is your adapter that will be filtered
                mDeviceListAdapter.getFilter().filter(query);
                Log.d("MAIN ACTIVITY", "on query submit: " + query);
                return true;
            }
        };

        mSearchView.setOnQueryTextListener(textChangeListener);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.scanning_start) {
            mScanning = true;
            BleWrapper.startScanning();
        } else if (id == R.id.scanning_stop) {
            mScanning = false;
            BleWrapper.stopScanning();
        }

        invalidateOptionsMenu();
        return true;
    }


    @Override
    protected void onListItemClick(ListView listeView, View view, int position, long id) {
        mDevice = mDeviceListAdapter.getDevice(position);
        if (mDevice == null)
            return;

        final Intent intent = new Intent(this, DeviceActivity.class);
        //intent.putExtra(DeviceActivity.EXTRAS_DEVICE_ADDRESS, mDevice.getAddress());
        //intent.putExtra(DeviceActivity.EXTRAS_DEVICE_NAME, mDevice.getName());
        startActivity(intent);


        if (mScanning) {
            mScanning = false;
            BleWrapper.stopScanning();
        }

    }

    /* check if user agreed to enable BT */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                btDisabled();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /* add device to the current list of devices */
    private void handleFoundDevice(final BluetoothDevice device, final int rssi, final byte[] record) {
        // adding to the UI have to happen in UI thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mDeviceListAdapter != null){
                    mDeviceListAdapter.addDevice(device, rssi, record);
                    mDeviceListAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void bleMissing() {
        Toast.makeText(this, "BLE not suported :/", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void btDisabled() {
        Toast.makeText(this, "BLE disabled", Toast.LENGTH_SHORT).show();
        finish();
    }

    /* make sure that potential scanning will take no longer
	 * than <SCANNING_TIMEOUT> seconds from now on */
    private void addScanningTimeout() {
        Runnable timeout = new Runnable() {
            @Override
            public void run() {
                if (BleWrapper == null) return;
                mScanning = false;
                BleWrapper.stopScanning();
                invalidateOptionsMenu();
            }
        };
        mHandler.postDelayed(timeout, SCANNING_TIMEOUT);
    }
}