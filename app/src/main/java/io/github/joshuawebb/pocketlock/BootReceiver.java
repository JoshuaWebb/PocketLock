package io.github.joshuawebb.pocketlock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
	private static final String TAG = BootReceiver.class.getName();
	private static final int requestCode = 1;

	public BootReceiver() {}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "onReceive()");
		Intent serviceIntent = new Intent(context, ScreenActivityIntentRegistrationService.class);

		// Wait a couple of minutes before launching service in case it goes rogue
		// this should hopefully buy enough time to perform some kind of recovery.
		PendingIntent delayedStart = PendingIntent.getService(context, requestCode, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000 * 120, delayedStart);
	}
}
