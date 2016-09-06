package io.github.joshuawebb.pocketlock;

import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Do the actual work
 */
public class MainService
		extends Service
		implements SensorEventListener, PhoneStateListener.Delegate
{
	private static final String TAG = MainService.class.getName();
	private static final String ACTION_SCREEN_ON = "org.webb.joshua.turnitoff.action.SCREEN_ON";
	private static final String ACTION_SCREEN_OFF = "org.webb.joshua.turnitoff.action.SCREEN_OFF";

	private SensorManager mSensorManager;
	private Sensor mProximitySensor;

	private PhoneStateListener mPhoneStateListener;
	private TelephonyManager mTelephonyManager;

	private ScheduledExecutorService mScheduledExecutorService;

	private Runnable mRunnable = null;
	private ScheduledFuture future;

	private boolean near;

	public static void startActionScreenOn(Context context) {
		Intent intent = new Intent(context, MainService.class);
		intent.setAction(ACTION_SCREEN_ON);
		context.startService(intent);
	}

	public static void startActionScreenOff(Context context) {
		Intent intent = new Intent(context, MainService.class);
		intent.setAction(ACTION_SCREEN_OFF);
		context.startService(intent);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		mTelephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		mPhoneStateListener = new PhoneStateListener(this);
		mScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			final String action = intent.getAction();
			switch (action) {
			case ACTION_SCREEN_ON:
				handleActionScreenOn();
				break;
			case ACTION_SCREEN_OFF:
				handleActionScreenOff();
				break;
			}
		}

		return START_NOT_STICKY;
	}

	private void handleActionScreenOn() {
		Log.i(TAG, "handleActionScreenOn()");

		// if the phone is not ringing, onPhoneStateIdle() will be called
		enablePhoneStateListener();
	}

	private void handleActionScreenOff() {
		Log.i(TAG, "handleActionScreenOff()");
		stopSelf();
	}

	@Override
	public void onDestroy() {
		tearDown();
		super.onDestroy();
	}

	private void tearDown() {
		mScheduledExecutorService.shutdownNow();
		disablePhoneStateListener();
		disableProximitySensor();
	}

	private void lockScreen() {
		DevicePolicyManager policyManager =
				(DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		ComponentName adminReceiver = new ComponentName(this, DeviceAdminReceiver.class);
		boolean admin = policyManager.isAdminActive(adminReceiver);
		if (admin) {
			Log.i(TAG, "Going to sleep.");
			policyManager.lockNow();
		}
		else {
			Log.w(TAG, "Not an admin");
			Toast.makeText(this, R.string.not_admin_prompt, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// if we are near for some amount of time, lock the screen
		// if we go far at any stage in that time, abort.
		near = event.values[0] < mProximitySensor.getMaximumRange();
		String proximity = near ? "near" : "far";
		Log.i(TAG, proximity);

		if (!near) {
			// make sure we cancel as soon as possible.
			cancelLockTimer();

			// not in pocket, stop checking.
			stopSelf();
		}

		if (mRunnable == null) {
			mRunnable = new Runnable() {
				@Override
				public void run() {
					if (near) {
						lockScreen();
					}
				}
			};

			// No particular reason for 2... it seems reasonable.
			future = mScheduledExecutorService.schedule(mRunnable, 2, TimeUnit.SECONDS);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO: do we care about this?
		// I don't think so...
		Log.i(TAG, "Accuracy changed:" + accuracy);
	}

	private void cancelLockTimer() {
		if (future != null) {
			future.cancel(true);
		}
	}

	private void enableProximitySensor() {
		mSensorManager.registerListener(this, mProximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
	}

	private void disableProximitySensor() {
		mSensorManager.unregisterListener(this);
	}

	private void enablePhoneStateListener() {
		mTelephonyManager.listen(mPhoneStateListener, android.telephony.PhoneStateListener.LISTEN_CALL_STATE);
	}

	private void disablePhoneStateListener() {
		mTelephonyManager.listen(mPhoneStateListener, android.telephony.PhoneStateListener.LISTEN_NONE);
	}

	@Override
	public void onPhoneRinging() {
		// TODO: if phone is on silent should we enable the proximity sensor
		//       to stop potential "butt answering"

		// Don't do anything yet... lets see how this pans out
		disableProximitySensor();
		cancelLockTimer();
	}

	@Override
	public void onCallAnswered() {
		// Probably don't need to do this, but just in case.
		disableProximitySensor();
		cancelLockTimer();
	}

	@Override
	public void onCallMissedOrRejected() {
		enableProximitySensor();
	}

	// This will be called after a phone call
	// Or if the screen was turned on but the phone wasn't ringing.
	// This should account for "Butt answering"
	@Override
	public void onPhoneStateIdle() {
		enableProximitySensor();
	}
}
