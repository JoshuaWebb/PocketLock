package io.github.joshuawebb.pocketlock;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * This must be launched at least once for RECEIVE_BOOT_COMPLETED to work.
 * This contains a button to launch the service instead of requiring a reboot.
 *
 * TODO: this should become a settings screen where you can configure the various
 *       timeouts and such. (Keep a launch service button though)
 */
public class EntryActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_entry);

		// Don't worry about boot shenanigans
		Button button = (Button) findViewById(R.id.button);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startService();
			}
		});
	}

	private void startService() {
		Intent serviceIntent = new Intent(this, ScreenActivityIntentRegistrationService.class);
		stopService(serviceIntent);
		startService(serviceIntent);
	}
}
