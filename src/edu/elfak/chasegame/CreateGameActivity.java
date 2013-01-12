package edu.elfak.chasegame;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
		
		lista.setOnItemClickListener(new OnItemClickListener() {
			
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            	
                String item = ((TextView)v).getText().toString();
                
                Toast.makeText(getBaseContext(), item, Toast.LENGTH_SHORT).show();
            }
        });
	}

}
