package pluggesture.fr1sk.com.myapplication;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

public interface BleWrapperCallbacks {
	
	public void uiDeviceFound(final BluetoothDevice device, int rssi, byte[] record);
	
	public void uiDeviceConnected();
	
	public void uiDeviceDisconnected();
	
	public void uiAvailableServices(boolean success);
	
	public void uiCharacteristicsForServices(boolean success);
	
	public void uiNotificationsSet(boolean success);
	
	public void uiNewValueForCharacteristic(final BluetoothGatt gatt,
                                            final BluetoothDevice device,
                                            final BluetoothGattService service,
                                            final BluetoothGattCharacteristic ch,
                                            final String strValue,
                                            final int intValue,
                                            final byte[] rawValue,
                                            final String timestamp);
	
	public void uiGotNotification(final BluetoothGatt gatt,
                                  final BluetoothDevice device,
                                  final BluetoothGattService service,
                                  final BluetoothGattCharacteristic characteristic);

	public void uiSuccessfulWrite(final BluetoothGatt gatt,
                                  final BluetoothDevice device,
                                  final BluetoothGattService service,
                                  final BluetoothGattCharacteristic ch,
                                  final String description);

	public void uiFailedWrite(final BluetoothGatt gatt,
                              final BluetoothDevice device,
                              final BluetoothGattService service,
                              final BluetoothGattCharacteristic ch,
                              final String description);
	
	public static class NullAdapter implements BleWrapperCallbacks{

		@Override
		public void uiDeviceFound(BluetoothDevice device, int rssi, byte[] record) {}

		@Override
		public void uiDeviceConnected() {}

		@Override
		public void uiDeviceDisconnected() {}

		@Override
		public void uiAvailableServices(boolean success) {}

		@Override
		public void uiCharacteristicsForServices(boolean success) {
		}

		@Override
		public void uiNewValueForCharacteristic(BluetoothGatt gatt,
				BluetoothDevice device, BluetoothGattService service,
				BluetoothGattCharacteristic ch, String strValue, int intValue,
				byte[] rawValue, String timestamp) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void uiGotNotification(BluetoothGatt gatt,
				BluetoothDevice device, BluetoothGattService service,
				BluetoothGattCharacteristic characteristic) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void uiSuccessfulWrite(BluetoothGatt gatt,
				BluetoothDevice device, BluetoothGattService service,
				BluetoothGattCharacteristic ch, String description) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void uiFailedWrite(BluetoothGatt gatt, BluetoothDevice device,
				BluetoothGattService service, BluetoothGattCharacteristic ch,
				String description) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void uiNotificationsSet(boolean success) {
			// TODO Auto-generated method stub
			
		}
		
	}



}
