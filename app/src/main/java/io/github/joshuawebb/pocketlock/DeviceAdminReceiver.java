package io.github.joshuawebb.pocketlock;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class DeviceAdminReceiver extends android.app.admin.DeviceAdminReceiver {
	@Override
	public void onEnabled(Context context, Intent intent) {
		onChange(context, context.getString(R.string.admin_receiver_status_enabled));
	}

	@Override
	public void onDisabled(Context context, Intent intent) {
		onChange(context, context.getString(R.string.admin_receiver_status_disabled));
	}

	private void onChange(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
}
