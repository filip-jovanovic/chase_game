package edu.elfak.chasegame;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.android.gcm.GCMRegistrar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

public class ChaseGameLoginActivity extends Activity implements OnClickListener {
	
	Handler guiThread;
	Context context;
	ProgressDialog progressDialog;
	static boolean loginFlag;
	String regId;
	Intent messageIntent;
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
        setContentView(R.layout.login);   
        
        //register device for Google Cloud messaging
        GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);
        regId = GCMRegistrar.getRegistrationId(this);
        if (regId.equals("")) {
          GCMRegistrar.register(this, "472939073721");
        } else {
          Log.v("Notice:", "Already registered");
        }
        
        //Toast.makeText(this, "regId: "+ regId, Toast.LENGTH_LONG).show();
        
        View loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(this);
        View signupButton = findViewById(R.id.signup_button);
        signupButton.setOnClickListener(this);
        
        if(Player.player_id != "" && Player.player_id != null)
        	{
        	loginButton.setClickable(false);
        	}

        
        guiThread = new Handler();
		context = this;
		progressDialog = new ProgressDialog(this);
		loginFlag = false;
		
	}
	
	public void onClick(View v) {
		Intent i;
		final String method;
				
		EditText etName = (EditText) findViewById(R.id.login_name_edit);
		final String name = etName.getText().toString();	
		EditText etPassword = (EditText) findViewById(R.id.login_password_edit);
		final String password = etPassword.getText().toString(); 
		
		if(v.getId() == R.id.login_button) 
			method = "login";
		else
			method = "signup";
		
			if(isValid(password)&&isValid(name))
			{
				
				ExecutorService transThread = Executors.newSingleThreadExecutor();
				transThread.submit(new Runnable(){
					public void run(){
						try{
							if(method == "login")
								guiProgressDialog(true, "Prijavljivanje u toku ...");
							else
								guiProgressDialog(true, "Kreiranje novog naloga u toku ...");
							
							final String result = HTTPHelper.parseResult(HTTPHelper.
									sendRegistrationToServer(name, password, regId, method, HTTPHelper.LOGIN_URL));	
							guiProgressDialog(false,"");
	
							if(!result.startsWith("Error:")){
								Player.player_id = result;	
								successfulLogin();
							}														
							else
							{
								Player.player_id = "";
								guiNotifyUser(result);
							}
							
						} catch(Exception e){e.printStackTrace();}
					}					
				});								
			}
			else
				Toast.makeText(this, "Korisnicko ime i lozinka moraju " +
						"imati vise od 6 slova, i ne smeju" +
						" sadrzati specijalne karaktere!", Toast.LENGTH_LONG).show();
	}
	
	static boolean isValid(String s) {
	    int length = s.length();
	    if (length < 2 || length > 15) return false;
	    if (!s.matches("[a-zA-Z0-9]*")) return false;
	    if (s.matches("[a-zA-Z]*99")) return false;
	    return true;
	}
	
	public void guiNotifyUser(final String message){
		guiThread.post(new Runnable() {			
			public void run() {
				Toast.makeText(context, message, Toast.LENGTH_LONG).show();
			}
		});
	}
	
	private void successfulLogin(){
		View loginButton = findViewById(R.id.login_button);
    		Intent i = new Intent(this, ChooseGameActivity.class);
			startActivity(i);
	}
	
	private void guiProgressDialog(final boolean start, final String message){
		guiThread.post(new Runnable() {		
			public void run() {
				if(start)
				{	
					progressDialog.setMessage(message);
					progressDialog.show();
				}
				else
					progressDialog.dismiss();
				
			}
		});
	}
	
	// broadcast receiver that handles messages from GCM
	
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	String message = intent.getExtras().getString("message");
        	Log.v("GCM","Message received"+ message);
        	// Do sth with message
        	Toast.makeText(context, "Received: " + message, Toast.LENGTH_LONG).show();
        }
    };
    
    @Override
	public void onResume() {
		super.onResume();		
		registerReceiver(broadcastReceiver, new IntentFilter(GCMIntentService.TAG));
	}
 
	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(broadcastReceiver);
	}
}
