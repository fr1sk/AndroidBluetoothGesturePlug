package pluggesture.fr1sk.com.myapplication;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

public class DeviceListAdapter extends BaseAdapter implements Filterable{
	private static final String TAG = "DeviceListAdapter";
    private CharSequence typedText="";
	private ArrayList<BluetoothDevice> mDevices;
	private ArrayList<BluetoothDevice> originList;
	private ArrayList<Integer> mRSSI;
    private ArrayList<Integer> originRSSI;
    private ArrayList<byte[]> orginRecords;
	private LayoutInflater mInflater;
	private ArrayList<byte[]> mRecords;
	private Filter deviceFilter;
	
	public DeviceListAdapter(Activity parent) {
		super();
		mDevices = new ArrayList<BluetoothDevice>();
        originList=new ArrayList<BluetoothDevice>();


		mRSSI = new ArrayList<Integer>();
        originRSSI=new ArrayList<Integer>();

        mRecords = new ArrayList<byte[]>();
        orginRecords = new ArrayList<byte[]>();
        mInflater = parent.getLayoutInflater();
	}
	public void addDevice(BluetoothDevice device, int rssi, byte[] scanRecord){
		//&& device.getName().contains("RTRK")
		if(mDevices.contains(device) == false && mDevices != null && device.getName().contains("RTRK")){
			mDevices.add(device);
			mRSSI.add(rssi);
			mRecords.add(scanRecord);
			originList = (ArrayList<BluetoothDevice>) mDevices.clone();
            orginRecords= (ArrayList<byte[]>) mRecords.clone();
            originRSSI = (ArrayList<Integer>) mRSSI.clone();

		}
		else{
			Log.d(TAG, "Device is null");
		}
	}
	public BluetoothDevice getDevice(int index){
		return mDevices.get(index);
	}
	
	public int getRssi(int index){
		return mRSSI.get(index);
	}
	
	public void clearList(){
		mDevices.clear();
		mRSSI.clear();
		mRecords.clear();
	}
	
	public void resetList(){
		mDevices = originList;
	}

    public void refreshList(ArrayList<BluetoothDevice> newList ){
        mDevices = newList;
    }

	
	@Override
	public int getCount() {
		return mDevices.size();
	}

	@Override
	public Object getItem(int position) {
		return getDevice(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		// get already available view or create new if necessary
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.activity_scanning_item, parent, false);
			holder = new ViewHolder();
			holder.deviceName 		= (TextView) convertView.findViewById(R.id.deviceName);
			holder.deviceRSSI 		= (TextView) convertView.findViewById(R.id.deviceRSSI);
			holder.deviceDistance	= (TextView) convertView.findViewById(R.id.deviceDistance);
			holder.devicePaired 	= (TextView) convertView.findViewById(R.id.devicePaired);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		// set proper values into the view
		BluetoothDevice device = mDevices.get(position);
        String rssiString;
        if(!mRSSI.isEmpty()){
		    int rssi = mRSSI.get(position);
            rssiString = (rssi == 0) ? "N/A" : rssi + " db";
        }else{
            rssiString = "N/A";
        }

		String name = device.getName();
		if( name == null || name.length() <= 0){
			name = "Unknown device";
        }else{
            if(name.contains("RTRKSP-")){
                name = name.substring(7);
            }
        }
		String isPaired;
		int paired = device.getBondState();
		if(paired == BluetoothDevice.BOND_BONDED){
			isPaired = "Paired";
		}else{
			isPaired = "New";
		}

        Spannable WordtoSpan = new SpannableString(name);
        int start=0;
        for (int i = 0; i< name.length();i++)
        {
             if (name.indexOf(typedText.toString(), i) != -1) {
                start = i;
             }
        }
        int end = start + typedText.length();
        WordtoSpan.setSpan(new ForegroundColorSpan(Color.GREEN), start , end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        holder.deviceName.setText(WordtoSpan);
		holder.deviceRSSI.setText(rssiString);
		holder.deviceDistance.setText(rssiString);
		holder.devicePaired.setText(isPaired);
		
		return convertView;
	}
	//create a holder class to contain inflated xml file elements
	private static class ViewHolder{
		TextView deviceName;
		TextView deviceRSSI;
		TextView devicePaired;
		TextView deviceDistance;
	}
	/*
	 * We create our filter	
	 */
	@Override
	public Filter getFilter() {
		if(deviceFilter ==null)
			deviceFilter = new DeviceFilter();
		return deviceFilter;
	}
	
	public class DeviceFilter extends Filter{
		FilterResults results = new FilterResults();
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
            BluetoothDevice ble= null;
			/*if (constraint == null || constraint.length() == 0){
				//No filter implemented we returned all the list
               *//* mDevices = (ArrayList<BluetoothDevice>) originList.clone();
                mRecords= (ArrayList<byte[]>) orginRecords.clone();
                mRSSI = (ArrayList<Integer>) originRSSI.clone();*//*
                Log.w(TAG,"ALL");

			}else {*/

                mDevices = (ArrayList<BluetoothDevice>) originList.clone();
                mRecords= (ArrayList<byte[]>) orginRecords.clone();
                mRSSI = (ArrayList<Integer>) originRSSI.clone();
				ArrayList<BluetoothDevice> returnList = new ArrayList<BluetoothDevice>();
                ArrayList<BluetoothDevice> list= originList;

                typedText=constraint.toString().toUpperCase();
                for (int i = 0; i < list.size(); i++) {
				/*for(BluetoothDevice bd : mDevices){*/

                    BluetoothDevice bd = list.get(i);

                    Log.w(TAG,bd.getName().toUpperCase());
                    Log.w(TAG,constraint.toString().toUpperCase());

                    if(bd.getName().toUpperCase().contains(constraint.toString().toUpperCase())) {

                        Log.w(TAG,"nasao"+bd.getName());

                        returnList.add(bd);
                       // notifyDataSetChanged();
					}
				}
                //mDevices=returnList;
				//refreshList(returnList);
				results.values = returnList;
				results.count = returnList.size();
			//}
			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {

           // ArrayList<BluetoothDevice> ble =
            //if(ble!=null)
            mDevices=  (ArrayList<BluetoothDevice> ) results.values;
            notifyDataSetChanged();
		}
		
	}

}
