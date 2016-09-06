package io.github.joshuawebb.pocketlock;

import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneStateListener extends android.telephony.PhoneStateListener {
	public interface Delegate {
		/**
		 * called when the phone starts ringing.
		 */
		void onPhoneRinging();

		/**
		 * called when the phone was ringing and then was answered.
		 */
		void onCallAnswered();

		/**
		 * called when a phone call rang out
		 * called was rejected by the user
		 */
		void onCallMissedOrRejected();

		/**
		 * called after an answered phone call ends
		 * called when a listener is registered and the phone is not currently ringing
		 */
		void onPhoneStateIdle();
	}

	private static final String TAG = PhoneStateListener.class.getName();

	Delegate mDelegate;
	public PhoneStateListener(Delegate delegate) {
		mDelegate = delegate;
	}

	public static boolean wasRinging;

	@Override
	// incomingNumber is always empty because we don't have the READ_PHONE_STATE permission.
	public void onCallStateChanged(int state, String incomingNumber) {
		switch (state) {
		case TelephonyManager.CALL_STATE_RINGING:
			Log.i(TAG, "RINGING");
			wasRinging = true;
			mDelegate.onPhoneRinging();
			break;

		case TelephonyManager.CALL_STATE_OFFHOOK:
			Log.i(TAG, "OFFHOOK");

			if (wasRinging) {
				mDelegate.onCallAnswered();
			}
			else {
				// we are dialing out... we don't care about this...
			}

			wasRinging = false;
			break;
		case TelephonyManager.CALL_STATE_IDLE:
			Log.i(TAG, "IDLE");

			if (wasRinging) {
				mDelegate.onCallMissedOrRejected();
			}
			else {
				mDelegate.onPhoneStateIdle();
			}

			wasRinging = false;
			break;
		}
	}
}
