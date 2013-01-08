package edu.elfak.chasegame;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class CreateGameActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_game);
		
		ArrayList<String> mape = HTTPHelper.getMapList();
		
		ListView lista = (ListView) findViewById(R.id.mapList);
		
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
						(this,android.R.layout.simple_list_item_1, mape);
		lista.setAdapter(arrayAdapter);
	}

}
