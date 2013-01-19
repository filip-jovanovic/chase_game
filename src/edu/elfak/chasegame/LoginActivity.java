package edu.elfak.chasegame;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.android.gcm.GCMRegistrar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener {

	Handler guiThread;
	Context context;
	ProgressDialog progressDialog;
	static boolean loginFlag;
	private String registrationId;
	private String playerName;
	Intent messageIntent;
	

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		// register device for Google Cloud Messaging
		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);
		registrationId = GCMRegistrar.getRegistrationId(this);
		
		if (registrationId.equals("")) {
			GCMRegistrar.register(this, "472939073721");
			registrationId = GCMRegistrar.getRegistrationId(this);
		} else {
			Log.v("Notice:", "Already registered");
		}

		View loginButton = findViewById(R.id.login_button);
		loginButton.setOnClickListener(this);
		View signupButton = findViewById(R.id.signup_button);
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
	
	public void OnDestroy(){
		GCMRegistrar.onDestroy(this);
	}

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

			ExecutorService transThread = Executors.newSingleThreadExecutor();
			transThread.submit(new Runnable() {
				public void run() {
					try {
						if (method == "login")
							guiProgressDialog(true, "Prijavljivanje u toku ...");
						else
							guiProgressDialog(true,
									"Kreiranje novog naloga u toku ...");

						final String result = HTTPHelper.parseResult(HTTPHelper
								.sendRegistrationToServer(name, password,
										registrationId, method,
										HTTPHelper.LOGIN_URL));
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
			public void run() {
				Toast.makeText(context, message, Toast.LENGTH_LONG).show();
			}
		});
	}

	private void successfulLogin() {
		Intent i = new Intent(this, HomeActivity.class);
		Bundle dataBundle = new Bundle();
		dataBundle.putString("registrationId", registrationId);
		dataBundle.putString("playerName",playerName);
		i.putExtra("dataBundle",dataBundle);
		finish();
		startActivity(i);
	}

	private void guiProgressDialog(final boolean start, final String message) {
		guiThread.post(new Runnable() {
			public void run() {
				if (start) {
					progressDialog.setMessage(message);
					progressDialog.show();
				} else
					progressDialog.dismiss();

			}
		});
	}

}
