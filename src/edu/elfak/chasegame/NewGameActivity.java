package edu.elfak.chasegame;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class NewGameActivity extends Activity implements OnClickListener, OnItemClickListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_game);
		
		ArrayList<String> mape = HTTPHelper.getGameList();
		
		ListView lista = (ListView) findViewById(R.id.gameList);
		
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
						(this,android.R.layout.simple_list_item_1, mape);
		lista.setAdapter(arrayAdapter);
		lista.setOnItemClickListener(this);/*new OnItemClickListener() {
			
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            	
                String item = ((TextView)v).getText().toString();
                
                Toast.makeText(getBaseContext(), item, Toast.LENGTH_SHORT).show();
            }
        });*/
		View but = findViewById(R.id.createGameButton);
        but.setOnClickListener(this);
	}

	public void onClick(View arg0) {
		Intent i = new Intent(this, CreateGameActivity.class);
		startActivity(i);		
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		String item = ((TextView)arg1).getText().toString();
        
        Toast.makeText(getBaseContext(), item, Toast.LENGTH_SHORT).show();
       
        Intent i = new Intent(this, MapActivity.class);
		startActivity(i);	
	}
