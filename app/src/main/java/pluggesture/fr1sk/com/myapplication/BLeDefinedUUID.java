package pluggesture.fr1sk.com.myapplication;

import java.util.UUID;

public class BLeDefinedUUID {
	public static class Service{
		/*	UUID's for services	*/
		final static public UUID mPowerServiceUUID 		= UUID.fromString("0000aa10-0000-1000-8000-00805f9b34fb");
		final static public UUID mControlServiceUUID 	= UUID.fromString("0000aa20-0000-1000-8000-00805f9b34fb");
		final static public UUID mInfoServiceUUID 		= UUID.fromString("0000aa40-0000-1000-8000-00805f9b34fb");
	}
	public static class Characteristic{
		/*	UUID's for characteristics	*/
		final static public UUID mActivePowerUUID 		= UUID.fromString("0000AA11-0000-1000-8000-00805f9b34fb");
		final static public UUID mReactivePowerUUID 	= UUID.fromString("0000AA12-0000-1000-8000-00805f9b34fb");
		final static public UUID mCurrentUUID	 		= UUID.fromString("0000AA13-0000-1000-8000-00805f9b34fb");
		final static public UUID mVoltageUUID 			= UUID.fromString("0000AA14-0000-1000-8000-00805f9b34fb");
		final static public UUID mPowerFactorUUID 		= UUID.fromString("0000AA15-0000-1000-8000-00805f9b34fb");
		final static public UUID mPowerLimitUUID	    = UUID.fromString("0000AA16-0000-1000-8000-00805f9b34fb");
		final static public UUID mSetNotificationUUID	= UUID.fromString("0000AA17-0000-1000-8000-00805f9b34fb");
		final static public UUID mSamplingRateUUID      = UUID.fromString("0000AA17-0000-1000-8000-00805f9b34fb");
		/////////////////////////////////////////////////////////////////////////////////////////////////////////

		final static public UUID mControlStateUUID		= UUID.fromString("0000AA23-0000-1000-8000-00805f9b34fb");
		final static public UUID mRGBIndication         = UUID.fromString("0000AA24-0000-1000-8000-00805f9b34fb");
        final static public UUID mTimerStatusUUID       = UUID.fromString("0000AA22-0000-1000-8000-00805f9b34fb");
        final static public UUID mTimerValueUUID        = UUID.fromString("0000AA21-0000-1000-8000-00805f9b34fb");

        final static public UUID mFirmwareVersionUUID	= UUID.fromString("0000AA41-0000-1000-8000-00805f9b34fb");
        final static public UUID mDeviceNameUUID        = UUID.fromString("0000AA43-0000-1000-8000-00805f9b34fb");
		final static public UUID mDescriptorUUID		= UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
	}
}
