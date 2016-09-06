package io.github.joshuawebb.pocketlock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ScreenActivityActionReceiver extends BroadcastReceiver {
	private static final String TAG = ScreenActivityActionReceiver.class.getName();

	public ScreenActivityActionReceiver() {}

	@Override
	public void onReceive(Context context, Intent intent) {
		switch(intent.getAction()) {
		case Intent.ACTION_SCREEN_OFF: onScreenOff(context); break;
		case Intent.ACTION_SCREEN_ON: onScreenOn(context); break;
		default: Log.e(TAG, "unhandled intent: " + intent.getAction()); break;
		}
	}

	private void onScreenOn(Context context) {
		Log.i(TAG, "onScreenOn()");
		MainService.startActionScreenOn(context);
	}

	private void onScreenOff(Context context) {
		Log.i(TAG, "onScreenOff()");
		MainService.startActionScreenOff(context);
	}
}
