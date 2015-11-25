package pluggesture.fr1sk.com.myapplication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class BleWrapper {
	private static final String TAG = "RTRK_BleWrapper";

	/* Local variables for services */
	private BluetoothGattService mPowerService = null;
	private BluetoothGattService mControlService = null;
	private BluetoothGattService mInfoService = null;
	/* Local variables for characteristic */
	private BluetoothGattCharacteristic mActivePowerCharacteristic = null;
	private BluetoothGattCharacteristic mReactivePowerCharacteristic = null;
	private BluetoothGattCharacteristic mCurrentCharacteristic = null;
	private BluetoothGattCharacteristic mVoltageCharacteristic = null;
	private BluetoothGattCharacteristic mPowerFactorCharacteristic = null;
	private BluetoothGattCharacteristic mControlStateCharacteristic = null;
	private BluetoothGattCharacteristic mSetNotificationCharacteristic = null;
	private BluetoothGattCharacteristic mFirmwareVersionCharacteristic = null;
    private BluetoothGattCharacteristic mRGBCharacteristic = null;
    private BluetoothGattCharacteristic mTimerStatus = null;
    private BluetoothGattCharacteristic mTimerValue = null;
    private BluetoothGattCharacteristic mSamplingRateCharacteristic = null;
    private BluetoothGattCharacteristic mDeviceNameCharacteristic = null;
    private BluetoothGattCharacteristic mPowerLimitCharacteristic = null;
	/*  */
	private BluetoothManager mBluetoothManager = null;
	private BluetoothAdapter mBluetoothAdapter = null;
	private Activity mParent = null;
	private String mDeviceAddress;
	private BluetoothGatt mBluetoothGatt = null;
	private BluetoothDevice mBluetoothDevice = null;
	private BluetoothGattService mBluetoothSelectedService = null;
	private List<BluetoothGattService> mBluetoothGattServices = null;
	private boolean mConnected = false;
    private byte[] mTimerValueMS;
	/* callback object through which we are returning results to the caller */
	private BleWrapperCallbacks mCallback = null;
	/* define NULL object for UI callbacks */
	private static final BleWrapperCallbacks NULL_CALLBACK = new BleWrapperCallbacks.NullAdapter();

	public BleWrapper(Activity parent, BleWrapperCallbacks callback) {
		// TODO Auto-generated constructor stub
		this.mParent = parent;
		mCallback = callback;
		if (mCallback == null)
			mCallback = NULL_CALLBACK;
	}

	public BluetoothManager getManager() {
		return mBluetoothManager;
	}

	public BluetoothAdapter getAdapter() {
		return mBluetoothAdapter;
	}

	public BluetoothDevice getDevice() {
		return mBluetoothDevice;
	}

	public BluetoothGatt getGatt() {
		return mBluetoothGatt;
	}

	public BluetoothGattService getCachedService() {
		return mBluetoothSelectedService;
	}

	public List<BluetoothGattService> getCachedServices() {
		return mBluetoothGattServices;
	}

	public boolean isConnected() {
		return mConnected;
	}

	/* run test and check if this device has BT and BLE hardware available */
	public boolean checkAvailableHardware() {
		// First check general Bluetooth Hardware:
		// get BluetoothManager...
		final BluetoothManager manager = (BluetoothManager) mParent
				.getSystemService(Context.BLUETOOTH_SERVICE);
		if (manager == null)
			return false;

		final BluetoothAdapter adapter = manager.getAdapter();
		if (adapter == null)
			return false;

		// and then check if BT LE is also available
        return mParent.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE);

	}

	/*
	 * before any action check if BT is turned ON and enabled for us call this
	 * in onResume to be always sure that BT is ON when Your application is put
	 * into the foreground
	 */

	public boolean isBtEnabled() {
		final BluetoothManager manager = (BluetoothManager) mParent
				.getSystemService(Context.BLUETOOTH_SERVICE);
		if (manager == null)
			return false;

		final BluetoothAdapter adapter = manager.getAdapter();

		return adapter != null && adapter.isEnabled();
	}

	/* start scanning for BT LE devices around */
	public void startScanning() {
		if(!mBluetoothAdapter.startLeScan(mDeviceFoundCallback)){
            Log.d(TAG, "Scan didn't started");
        }
    }

	/* stops current scanning */
	public void stopScanning() {
		mBluetoothAdapter.stopLeScan(mDeviceFoundCallback);
	}

	/* initialize BLE and get BT Manager & Adapter */
	public boolean initialize() {
		Log.d(TAG, "initialize");
		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) mParent
					.getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
				return false;
			}
		}

		if (mBluetoothAdapter == null) {
			mBluetoothAdapter = mBluetoothManager.getAdapter();
			if (mBluetoothAdapter == null) {
				return false;
			}
		}

		return true;
	}

    /* connect to the device with specified address */
    public boolean connect(final String deviceAddress) {
        Log.d(TAG, "connect");
        if (mBluetoothAdapter == null || deviceAddress == null)
            return false;
        mDeviceAddress = deviceAddress;

        // check if we need to connect from scratch or just reconnect to
        // previous device
        if (mBluetoothGatt != null
                && mBluetoothGatt.getDevice().getAddress()
                .equals(deviceAddress)) {
            // just reconnect
            mConnected = mBluetoothGatt.connect();
            return mConnected;
        } else {
            // connect from scratch
            // get BluetoothDevice object for specified address
            mBluetoothDevice = mBluetoothAdapter
                    .getRemoteDevice(mDeviceAddress);
            if (mBluetoothDevice == null) {
                // we got wrong address - that device is not available!
                return false;
            }
            // connect with remote device, connect state will be in callbak
            mBluetoothGatt = mBluetoothDevice.connectGatt(mParent, false,
                    mBleCallback);
        }
        return true;
    }

	public void disconnect() {
		Log.d(TAG, "disconnect");
		if (mBluetoothGatt != null) {
			mBluetoothGatt.disconnect();
			mConnected = false;
		}
		mCallback.uiDeviceDisconnected();
	}

	/* close GATT client completely */
	public void close() {
		Log.d(TAG, "close");
		if (mBluetoothGatt != null){
			mBluetoothGatt.close();
		}
		mBluetoothGatt = null;
	}

	/*
	 * request to discover all services available on the remote devices results
	 * are delivered through callback object
	 */
	public void startServicesDiscovery() {
		if (mBluetoothGatt != null && mConnected)
			Log.i(TAG, "service discovery");
			mBluetoothGatt.discoverServices();
	}

	/*
	 * gets services and calls UI callback to handle them before calling
	 * getServices() make sure service discovery is finished!
	 */
	public void getSupportedServices() {
		boolean succes;

        mPowerService   = mBluetoothGatt.getService(BLeDefinedUUID.Service.mPowerServiceUUID);
        mControlService = mBluetoothGatt.getService(BLeDefinedUUID.Service.mControlServiceUUID);
        mInfoService    = mBluetoothGatt.getService(BLeDefinedUUID.Service.mInfoServiceUUID);
		if(mPowerService == null || mControlService == null || mInfoService == null){
			succes = false;
            Log.d(TAG, "Services not found error!");
		}
		else{
			succes = true;
		}

		mCallback.uiAvailableServices(succes);

	}

	/*
	 * get all characteristic for all services and save them to local variables,
	 * then call the UI callback
	 */
	public void getCharacteristicsForServices() {
		boolean success = true;
		// / Get list of power characteristics, and each one of them save
		// locally
        mActivePowerCharacteristic      = mPowerService.getCharacteristic(BLeDefinedUUID.Characteristic.mActivePowerUUID);
        mReactivePowerCharacteristic    = mPowerService.getCharacteristic(BLeDefinedUUID.Characteristic.mReactivePowerUUID);
        mCurrentCharacteristic          = mPowerService.getCharacteristic(BLeDefinedUUID.Characteristic.mCurrentUUID);
        mVoltageCharacteristic          = mPowerService.getCharacteristic(BLeDefinedUUID.Characteristic.mVoltageUUID);
        mPowerFactorCharacteristic      = mPowerService.getCharacteristic(BLeDefinedUUID.Characteristic.mPowerFactorUUID);
        mControlStateCharacteristic     = mControlService.getCharacteristic(BLeDefinedUUID.Characteristic.mControlStateUUID);
        mFirmwareVersionCharacteristic  = mInfoService.getCharacteristic(BLeDefinedUUID.Characteristic.mFirmwareVersionUUID);
        mSetNotificationCharacteristic  = mPowerService.getCharacteristic(BLeDefinedUUID.Characteristic.mSetNotificationUUID);
		mRGBCharacteristic              = mControlService.getCharacteristic(BLeDefinedUUID.Characteristic.mRGBIndication);
        mTimerStatus                    = mControlService.getCharacteristic(BLeDefinedUUID.Characteristic.mTimerStatusUUID);
        mTimerValue                     = mControlService.getCharacteristic(BLeDefinedUUID.Characteristic.mTimerValueUUID);
		mDeviceNameCharacteristic       = mInfoService.getCharacteristic(BLeDefinedUUID.Characteristic.mDeviceNameUUID);
		mSamplingRateCharacteristic     = mPowerService.getCharacteristic(BLeDefinedUUID.Characteristic.mSamplingRateUUID);
		mPowerLimitCharacteristic       = mPowerService.getCharacteristic(BLeDefinedUUID.Characteristic.mPowerLimitUUID);
		// return to callback
        if(mActivePowerCharacteristic == null || mReactivePowerCharacteristic == null ||
                mCurrentCharacteristic == null ||
                mVoltageCharacteristic == null ||
                mPowerFactorCharacteristic == null ||
                mControlStateCharacteristic == null ||
                mFirmwareVersionCharacteristic == null)
        {
            success = false;
        }

		mCallback.uiCharacteristicsForServices(success);
	}

	/*
	 * request to fetch newest value stored on the remote device for particular
	 * characteristic
	 */
	public void requestCharacteristicValue(BluetoothGattCharacteristic ch) {
		Log.d(TAG, "requestCharacteristicValue");
		if (mBluetoothAdapter == null || mBluetoothGatt == null)
			return;

		mBluetoothGatt.readCharacteristic(ch);
		// new value available will be notified in Callback Object
	}

	/*
	 * get characteristic's value (and parse it for some types of
	 * characteristics) before calling this You should always update the value
	 * by calling requestCharacteristicValue()
	 */
	public void getCharacteristicValue(BluetoothGattCharacteristic ch) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null || ch == null)
			return;

		UUID charUUID = ch.getUuid();
		int intValue = 0;
		byte[] rawValue;
        String stringValue = "";

		if (charUUID.equals(BLeDefinedUUID.Characteristic.mActivePowerUUID)) {
			intValue = ch.getIntValue(
					BluetoothGattCharacteristic.FORMAT_SINT16, 0);
		} else if (charUUID
				.equals(BLeDefinedUUID.Characteristic.mControlStateUUID)) {
			intValue = ch.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8,
					0);
		} else if (charUUID
				.equals(BLeDefinedUUID.Characteristic.mReactivePowerUUID)) {
			intValue = ch.getIntValue(
					BluetoothGattCharacteristic.FORMAT_SINT16, 0);
		} else if (charUUID.equals(BLeDefinedUUID.Characteristic.mCurrentUUID)) {
			intValue = ch.getIntValue(
					BluetoothGattCharacteristic.FORMAT_SINT16, 0);
		} else if (charUUID.equals(BLeDefinedUUID.Characteristic.mVoltageUUID)) {
			intValue = ch.getIntValue(
					BluetoothGattCharacteristic.FORMAT_SINT16, 0);
		} else if (charUUID
				.equals(BLeDefinedUUID.Characteristic.mPowerFactorUUID)) {
			intValue = ch.getIntValue(
					BluetoothGattCharacteristic.FORMAT_SINT16, 0);
		} else if (charUUID
				.equals(BLeDefinedUUID.Characteristic.mFirmwareVersionUUID)) {
			intValue = ch.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
			rawValue = ch.getValue();
			stringValue = ch.getStringValue(0);
			//Log.d(TAG, "Firmware value: ");
		}else if(charUUID.equals(BLeDefinedUUID.Characteristic.mRGBIndication)){
            Log.d(TAG, "GetCharValue - RGB");
            intValue = ch.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        }else if(charUUID.equals(BLeDefinedUUID.Characteristic.mTimerStatusUUID)){
            Log.d(TAG, "Timer status read char...");
            intValue = ch.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        }else if(charUUID.equals((BLeDefinedUUID.Characteristic.mTimerValueUUID))){
            Log.d(TAG, "Timer status read value...");
            intValue = ch.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);
        }else if(charUUID.equals(BLeDefinedUUID.Characteristic.mDeviceNameUUID)){
            stringValue = ch.getStringValue(0);
        }else if(charUUID.equals((BLeDefinedUUID.Characteristic.mSamplingRateUUID))){
            intValue = ch.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16,0);
        }else if(charUUID.equals(BLeDefinedUUID.Characteristic.mPowerLimitUUID)){
            intValue = ch.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16,0);
        }

		rawValue = ch.getValue();
		

		String timestamp = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS")
				.format(new Date());
		mCallback.uiNewValueForCharacteristic(mBluetoothGatt, mBluetoothDevice,
				mBluetoothSelectedService, ch, stringValue, intValue, rawValue,
				timestamp);
	}

	/* set new value for particular characteristic */
	public boolean writeDataToCharacteristic(final UUID serviceUUID,
			final UUID charUUID, final byte[] dataToWrite) {

		BluetoothGattCharacteristic ch;
		
		if (mBluetoothAdapter == null || mBluetoothGatt == null)
			return false;

        ch = mBluetoothGatt.getService(serviceUUID).getCharacteristic(charUUID);
        ch.setValue(dataToWrite);

        if(ch == null)
            return false;

		return mBluetoothGatt.writeCharacteristic(ch);
	}

	/* enables notifications for characteristics */
	public boolean setNotificationForCharacteristic() {
		if (mBluetoothAdapter == null || mBluetoothGatt == null)
			return false;
		if(mSetNotificationCharacteristic == null){
			Log.d(TAG, "mSetNotification is NULL");
		}
		// set main characteristic notification, then write descriptor
		BluetoothGattDescriptor descriptor = mSetNotificationCharacteristic
				.getDescriptor(BLeDefinedUUID.Characteristic.mDescriptorUUID);
		descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		if(!mBluetoothGatt.writeDescriptor(descriptor)){
			Log.d(TAG, "Write descriptor fail");
			return false;
		}
		mBluetoothGatt.setCharacteristicNotification(
				mSetNotificationCharacteristic, true);

//		// then set other characteristics
		if(!mBluetoothGatt.setCharacteristicNotification(mControlStateCharacteristic, true)){
			Log.d(TAG, "Set control state char notification failed");
			return false;
		}

		return true;
	}

    public boolean setNotification(){
        boolean success = true;



        return success;
    }

	public void getActivePower() {
        requestCharacteristicValue(mActivePowerCharacteristic);
	}

	public void getState() {
		if(mControlStateCharacteristic != null)
		    requestCharacteristicValue(mControlStateCharacteristic);
	}

	public void getFirmwareVersion() {
		if(mFirmwareVersionCharacteristic != null)
		    requestCharacteristicValue(mFirmwareVersionCharacteristic);
	}

	public void getReactivePower() {
		if(mReactivePowerCharacteristic != null)
		    requestCharacteristicValue(mReactivePowerCharacteristic);
	}

	public void getCurrent() {
		if(mCurrentCharacteristic != null)
		    requestCharacteristicValue(mCurrentCharacteristic);
	}

	public void getVoltage() {
		if(mVoltageCharacteristic != null)
		    requestCharacteristicValue(mVoltageCharacteristic);
	}

	public void getPowerFactor() {
		if(mPowerFactorCharacteristic != null)
		    requestCharacteristicValue(mPowerFactorCharacteristic);
	}
	/* set Timer */
    /*  Off timer, On timer or no timer */
    public void getTimerStatus(){
        if(mTimerStatus != null)
            requestCharacteristicValue(mTimerStatus);
    }
    public void getTimerValueInMS(){
        if(mTimerValue != null)
            requestCharacteristicValue(mTimerValue);
    }
    public void getRGB(){
        if(mRGBCharacteristic != null)
            requestCharacteristicValue(mRGBCharacteristic);
    }

    public void getDeviceName(){
        if(mDeviceNameCharacteristic != null)
            requestCharacteristicValue(mDeviceNameCharacteristic);
    }

    public void getSamplingRate(){
        if(mSamplingRateCharacteristic != null)
            requestCharacteristicValue(mSamplingRateCharacteristic);
    }

    public void getLimit(){
        if(mPowerLimitCharacteristic != null)
            requestCharacteristicValue(mPowerLimitCharacteristic);
    }

    public boolean setTimer(Boolean action, byte[] valueInMS){
        if (mBluetoothAdapter == null || mBluetoothGatt == null)
            return false;

        String state;
        if(action){
            state = "0x01";

        }else{
            state = "0x02";
        }

        mTimerValueMS = valueInMS;

        byte[] rawStateValue = parseHexStringToBytes(state);

        return writeDataToCharacteristic(BLeDefinedUUID.Service.mControlServiceUUID,
                BLeDefinedUUID.Characteristic.mTimerStatusUUID, rawStateValue);
    }
	/* defines callback for scanning results */
	private BluetoothAdapter.LeScanCallback mDeviceFoundCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			mCallback.uiDeviceFound(device, rssi, scanRecord);
		}
	};

    public boolean setRGB(boolean state){
        if (mBluetoothAdapter == null || mBluetoothGatt == null)
            return false;

        String s;
        if(state){
            s = "0x01";

        }else{
            s = "0x00";
        }
        byte[] rawStateValue = parseHexStringToBytes(s);

        return writeDataToCharacteristic(BLeDefinedUUID.Service.mControlServiceUUID,
                BLeDefinedUUID.Characteristic.mRGBIndication, rawStateValue);
    }

    public void setDeviceName(String name){
        if (mBluetoothAdapter == null || mBluetoothGatt == null)
            return;
        byte[] byteName = name.getBytes(Charset.forName("UTF-8"));
        writeDataToCharacteristic(BLeDefinedUUID.Service.mInfoServiceUUID,
                BLeDefinedUUID.Characteristic.mDeviceNameUUID, byteName);
    }

    public void setSamplingRate(int ms){

        if(mSamplingRateCharacteristic != null){
            byte[] value = intToByteArray(ms);
            writeDataToCharacteristic(BLeDefinedUUID.Service.mPowerServiceUUID,
                    BLeDefinedUUID.Characteristic.mSamplingRateUUID, value);
        }
    }

	private final BluetoothGattCallback mBleCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {

			if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION
					|| status == BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION) {
				Log.d(TAG,
						"GATT_INSUFFICIENT_AUTHENTICATION in onConnectionStateChange");
			}

			if (newState == BluetoothProfile.STATE_CONNECTED) {
				mConnected = true;
				mCallback.uiDeviceConnected();
				// now we can start talking with the device, e.g.
				mBluetoothGatt.readRemoteRssi();
				// response will be delivered to callback object!
				startServicesDiscovery();
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				mConnected = false;
				mCallback.uiDeviceDisconnected();
			}

		}

		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				// now, when services discovery is finished, we can call
				// getServices() for Gatt
				getSupportedServices();
			} else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION
					|| status == BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION) {
				Log.d(TAG, "GATT_INSUFFICIENT_AUTHENTICATION in onServicesDiscovered");
				/*
				 * failed to complete the operation because of encryption
				 * issues, this means we need to bond with the device
				 */
				/*
				 * registering Bluetooth BroadcastReceiver to be notified for
				 * any bonding messages
				 */
			} else {
				/*
				 * Operation failed for some other reason
				 */
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			// we got response regarding our request to fetch characteristic
			// value
			if (status == BluetoothGatt.GATT_SUCCESS) {
				// and it success, so we can get the value
                if(characteristic.getUuid().equals(BLeDefinedUUID.Characteristic.mFirmwareVersionUUID)){
                    getSamplingRate();
                }else if(characteristic.getUuid().equals(BLeDefinedUUID.Characteristic.mSamplingRateUUID)){
                    getState();
                }else if(characteristic.getUuid().equals(BLeDefinedUUID.Characteristic.mControlStateUUID)){
                    getRGB();
                }else if(characteristic.getUuid().equals(BLeDefinedUUID.Characteristic.mRGBIndication)){
                    getLimit();
                }else if(characteristic.getUuid().equals(BLeDefinedUUID.Characteristic.mPowerLimitUUID)){
                    getTimerStatus();
                }
				getCharacteristicValue(characteristic);
			} else
				Log.d(TAG, "Failed to read characteristic: "
						+ characteristic.getUuid().toString());

		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			// characteristic's value was updated due to enabled notification,
			// lets get this value
			// the value itself will be reported to the UI inside
			getCharacteristicValue(characteristic);
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			// we got response regarding our request to write new value to the
			// characteristic
			// let see if it failed or not
            if(characteristic.getUuid().equals(mTimerStatus.getUuid())){
                writeDataToCharacteristic(BLeDefinedUUID.Service.mControlServiceUUID,
                        BLeDefinedUUID.Characteristic.mTimerValueUUID,mTimerValueMS);
            }else if(characteristic.getUuid().equals(BLeDefinedUUID.Characteristic.mDeviceNameUUID)){
                getDeviceName();
            }else if(characteristic.getUuid().equals(BLeDefinedUUID.Characteristic.mSamplingRateUUID)){
                String sValue = characteristic.getStringValue(0);
                int iValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16,0);
                byte[] rValue = characteristic.getValue();
                mCallback.uiNewValueForCharacteristic(gatt, mBluetoothDevice, characteristic.getService(),
                        characteristic, sValue, iValue, rValue, "");
            }
		}

		@Override
		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
/*			if (status == BluetoothGatt.GATT_SUCCESS) {
				// we got new value of RSSI of the connection, pass it to the UI
			}*/
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {
			Log.d(TAG, "Descriptor has been writen.");
			mCallback.uiNotificationsSet(true);
			super.onDescriptorWrite(gatt, descriptor, status);
		}

	};
    public static byte[] parseHexStringToBytes(final String hex) {
        String tmp = hex.substring(2).replaceAll("[^[0-9][a-f]]", "");
        byte[] bytes = new byte[tmp.length() / 2]; // every two letters in the
        // string are one byte
        // finally

        String part = "";

        for (int i = 0; i < bytes.length; ++i) {
            part = "0x" + tmp.substring(i * 2, i * 2 + 2);
            bytes[i] = Long.decode(part).byteValue();
        }

        return bytes;
    }
    public static byte[] intToByteArray(int value){
        byte[] returnValue = new byte[4];

        returnValue[0] = (byte)(value);
        returnValue[1] = (byte)(value >> 8);
        returnValue[2] = (byte)(value >> 16);
        returnValue[3] = (byte)(value >> 24);

        return returnValue;
    }
}
