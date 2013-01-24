package edu.elfak.chasegame;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.android.gcm.GCMRegistrar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener {

	Handler guiThread;
	Context context;
	ProgressDialog progressDialog;
	static boolean loginFlag;
	private String registrationId;
	private String playerName;
	private Intent messageIntent;
	private GcmRegisterReceiver gcmRegisterReceiver;
	private Button loginButton, signupButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login);

		// register device for Google Cloud Messaging
		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);

		registrationId = GCMRegistrar.getRegistrationId(this);

		if (registrationId.equals("")) {
			GCMRegistrar.register(this, "472939073721");
		}

		loginButton = (Button) findViewById(R.id.login_button);
		loginButton.setOnClickListener(this);
		signupButton = (Button) findViewById(R.id.signup_button);
		signupButton.setOnClickListener(this);

		// just for testing phase!
		EditText etName = (EditText) findViewById(R.id.login_name_edit);
		etName.setText("filip");
		EditText etPassword = (EditText) findViewById(R.id.login_password_edit);
		etPassword.setText("asdfgh");
		// just for testing phase!

		if (playerName != "" && playerName != null)
			loginButton.setClickable(false);

		guiThread = new Handler();
		context = this;
		progressDialog = new ProgressDialog(this);
		loginFlag = false;
	}

	public void onResume() {
		registrationId = GCMRegistrar.getRegistrationId(this);
		if (registrationId.equals("")) {
			if (gcmRegisterReceiver == null)
				gcmRegisterReceiver = new GcmRegisterReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction("REGISTRATION_RECEIVED");
			registerReceiver(gcmRegisterReceiver, intentFilter);
			loginButton.setEnabled(false);
			GCMRegistrar.register(this, "472939073721");
		}
		super.onResume();
	}

	private class GcmRegisterReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			registrationId = intent.getExtras().getString("registrationId");
			loginButton.setEnabled(true);
		}

	}

	public void OnDestroy() {
		GCMRegistrar.onDestroy(this);
		unregisterReceiver(gcmRegisterReceiver);
	}

	@Override
	public void onClick(View v) {
		final String method;

		EditText etName = (EditText) findViewById(R.id.login_name_edit);
		final String name = etName.getText().toString();
		EditText etPassword = (EditText) findViewById(R.id.login_password_edit);
		final String password = etPassword.getText().toString();

		if (v.getId() == R.id.login_button)
			method = "login";
		else
			method = "signup";

		if (isValid(password) && isValid(name)) {
			final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

			if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				showGPSDisabledAlertToUser();
			} else if(!isOnline()){
				showIternetDisabledAlertToUser();
			}
			else
			{
				ExecutorService transThread = Executors.newSingleThreadExecutor();
				transThread.submit(new Runnable() {
					@Override
					public void run() {
						try {
							if (method == "login")
								guiProgressDialog(true,
										"Prijavljivanje u toku ...");
							else
								guiProgressDialog(true,
										"Kreiranje novog naloga u toku ...");

							final String result = HttpHelper
									.parseResult(HttpHelper
											.sendRegistrationToServer(name,
													password, registrationId,
													method,
													HttpHelper.LOGIN_URL));
							guiProgressDialog(false, "");

							if (!result.startsWith("Error:")) {
								playerName = result;
								successfulLogin();
							} else {
								playerName = "";
								guiNotifyUser(result);
							}

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
		} else
			Toast.makeText(
					this,
					"Korisnicko ime i lozinka moraju "
							+ "imati vise od 6 slova, i ne smeju"
							+ " sadrzati specijalne karaktere!",
					Toast.LENGTH_LONG).show();
	}

	static boolean isValid(String s) {
		int length = s.length();
		if (length < 2 || length > 15)
			return false;
		if (!s.matches("[a-zA-Z0-9]*"))
			return false;
		if (s.matches("[a-zA-Z]*99"))
			return false;
		return true;
	}

	public void guiNotifyUser(final String message) {
		guiThread.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(context, message, Toast.LENGTH_LONG).show();
			}
		});
	}

	private void guiProgressDialog(final boolean start, final String message) {
		guiThread.post(new Runnable() {
			@Override
			public void run() {
				if (start) {
					progressDialog.setMessage(message);
					progressDialog.show();
				} else
					progressDialog.dismiss();

			}
		});
	}

	private void successfulLogin() {
		Intent i = new Intent(this, HomeActivity.class);
		Bundle dataBundle = new Bundle();
		dataBundle.putString("registrationId", registrationId);
		dataBundle.putString("playerName", playerName);
		i.putExtra("dataBundle", dataBundle);
		finish();
		startActivity(i);
	}

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

	private void showGPSDisabledAlertToUser() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder
				.setMessage(
						"Gps nije omogucen na ovom uredjaju, a neophodan je za pokretanje igrice. Da li zelite da ga omogucite?")
				.setCancelable(false)
				.setPositiveButton("Settings",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Intent callGPSSettingIntent = new Intent(
										android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								startActivity(callGPSSettingIntent);
							}
						});
		alertDialogBuilder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = alertDialogBuilder.create();
		alert.show();
	}
	
	private void showIternetDisabledAlertToUser() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder
				.setMessage(
						"Internet nije omogucen na ovom uredjaju, a neophodan je za pokretanje igrice. Da li zelite da ga omogucite?")
				.setCancelable(false)
				.setPositiveButton("Settings",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Intent callInternetSettingIntent = new Intent(
										android.provider.Settings.ACTION_WIRELESS_SETTINGS);
								startActivity(callInternetSettingIntent);
							}
						});
		alertDialogBuilder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = alertDialogBuilder.create();
		alert.show();
	}

}
