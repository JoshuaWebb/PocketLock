package io.github.joshuawebb.pocketlock;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

/**
 * This service registers the broadcast listeners for screen activity
 * because for some reason you can't declare them in the manifest and
 * have to call them from a full-blown activity/service...
 */
public class ScreenActivityIntentRegistrationService extends Service {
   private static final String TAG = ScreenActivityIntentRegistrationService.class.getName();

   BroadcastReceiver mScreenStateReceiver;

   @Override
   public IBinder onBind(Intent unused) { return null; }

   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
      Log.i(TAG, "onStartCommand()");

      mScreenStateReceiver = new ScreenActivityActionReceiver();

      IntentFilter filter = new IntentFilter();
      filter.addAction(Intent.ACTION_SCREEN_OFF);
      filter.addAction(Intent.ACTION_SCREEN_ON);

      registerReceiver(mScreenStateReceiver, filter);

      return START_STICKY;
   }

   @Override
   public void onDestroy() {
      unregisterReceiver(mScreenStateReceiver);
      super.onDestroy();
   }
}
