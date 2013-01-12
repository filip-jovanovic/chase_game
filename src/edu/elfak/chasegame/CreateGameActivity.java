<<<<<<< HEAD
package edu.elfak.chasegame;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.EditText;

public class CreateGameActivity extends Activity implements OnClickListener {
	EditText gameName;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_game);
		
		ArrayList<String> mape = HTTPHelper.getMapList();
		
		Spinner lista = (Spinner) findViewById(R.id.mapList);
		
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
						(this,android.R.layout.simple_spinner_dropdown_item, mape);
		lista.setAdapter(arrayAdapter);
		View button = findViewById(R.id.startGame);
		button.setOnClickListener(this);
		gameName = (EditText) findViewById(R.id.gameName);
	}

	public void onClick(View arg0) {
		//Toast.makeText(getBaseContext(), "Proba", Toast.LENGTH_SHORT).show();
	    if(gameName.getText().toString().compareTo("")==0)
	    	Toast.makeText(getBaseContext(), "Unesi tekst", Toast.LENGTH_SHORT).show();
	    else{
	    	Intent i = new Intent(this, MapActivity.class);
	    	startActivity(i);	
	    }
	}
}
=======
package edu.elfak.chasegame;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CreateGameActivity extends Activity implements OnItemClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_game);
		
		ArrayList<String> mape = HTTPHelper.getMapList();
		
		ListView lista = (ListView) findViewById(R.id.mapList);
		
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
						(this,android.R.layout.simple_list_item_1, mape);
		lista.setAdapter(arrayAdapter);
		
		lista.setOnItemClickListener(this);
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		String item = ((TextView)arg1).getText().toString();
        
        Toast.makeText(getBaseContext(), item, Toast.LENGTH_SHORT).show();
       
        Intent i = new Intent(this, MapActivity.class);
		startActivity(i);		
	}
}
>>>>>>> services added
