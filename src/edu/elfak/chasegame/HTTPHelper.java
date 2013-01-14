package edu.elfak.chasegame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class HTTPHelper {

	public static final String LOGIN_URL = "http://android-test-rig.comuf.com/login.php";
	public static final String CREATE_GAME_URL = "http://android-test-rig.comuf.com/createNewGame.php";
	public static final String UPDATE_GAME_URL = "http://android-test-rig.comuf.com/getGame.php";
	public static String URL = "http://android-test-rig.comuf.com/";
	static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";
    
	public static String sendRegistrationToServer(String name, String password, String regId, String method, String url) {
    	
    	String retStr;
    	HttpClient client = new DefaultHttpClient();
    	HttpPost post = new HttpPost(url);
    	
    	try {
    		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
    		
    		nameValuePairs.add(new BasicNameValuePair("username", name));
    		nameValuePairs.add(new BasicNameValuePair("password", password));
    		nameValuePairs.add(new BasicNameValuePair("regId", regId));
    		nameValuePairs.add(new BasicNameValuePair("method", method));
    		
    		post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
    		HttpResponse response = client.execute(post);
    		retStr = inputStreamToString(response.getEntity().getContent()).toString();
    	} catch (IOException e) {
			e.printStackTrace();
			retStr = "Error during upload!";
		} 
    	
    	return retStr;
    }
	
	public static String sendValuesToUrl(ArrayList<String> parameters, ArrayList<String> values, String url) {
    	
    	String retStr;
    	HttpClient client = new DefaultHttpClient();
    	HttpPost post = new HttpPost(url);
    	
    	try {
    		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
    		for(int i = 0; i<parameters.size();i++){
    			Log.v("http",parameters.get(i) + " : " + values.get(i));
    			nameValuePairs.add(new BasicNameValuePair(parameters.get(i), values.get(i)));
    		}
    		post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
    		HttpResponse response = client.execute(post);
    		retStr = inputStreamToString(response.getEntity().getContent()).toString().split("<!--")[0];
    	} catch (IOException e) {
			e.printStackTrace();
			retStr = "Error during upload!";
		} 
    	
    	return retStr;
    }
	
	public static ArrayList<String> getMapList() {
		final String TAG_MAPS = "maps";
		final String TAG_ID = "id";
		final String TAG_NAME = "name";
		
		ArrayList<String> mapList = new ArrayList<String>();
		JSONArray contacts = null;
		 
		// getting JSON string from URL
		JSONObject json = getJSONFromUrl(URL+"mapList.php");
		 
		try {
		    // Getting Array of Contacts
		    contacts = json.getJSONArray(TAG_MAPS);
		 
		    // looping through All Contacts
		    for(int i = 0; i < contacts.length(); i++){
		        JSONObject c = contacts.getJSONObject(i);
		 
		        // Storing each json item in variable
		        String id = c.getString(TAG_ID);
		        String name = c.getString(TAG_NAME);
 
                // adding each child node to HashMap key => value
                //contactList.add(id);
                mapList.add(id+". "+name);
		    }
		} catch (JSONException e) {
		    e.printStackTrace();
		}    	
    	return mapList;
    }
	
	public static HashMap<String, String> getGameList() {
		final String TAG_GAMES = "games";
		final String TAG_ID = "id";
		final String TAG_NAME = "name";
		final String TAG_COUNT = "count";
		HashMap<String, String> gameHashMap = new HashMap<String, String>();
		JSONArray jsonGames = null;
		HashMap<String, String> hmp = null;
		// getting JSON string from URL
		JSONObject json = getJSONFromUrl(URL+"gamelist.php");
		 
		try {
		    // Getting Array of Contacts
		    jsonGames = json.getJSONArray(TAG_GAMES);
		 
		    // looping through All Contacts
		    for(int i = 0; i < jsonGames.length(); i++){
		        JSONObject c = jsonGames.getJSONObject(i);
		 
		        // Storing each json item in variable
		        String id = c.getString(TAG_ID);
		        String name = c.getString(TAG_NAME);
		        String count = c.getString(TAG_COUNT);
                // adding each child node to HashMap key => value
                //contactList.add(id);
		        String result = name + " (" + count + "/4)";
                gameHashMap.put(result, id);
		    }
		} catch (JSONException e) {
		    e.printStackTrace();
		}    	
    	return gameHashMap;
    }
	
	
	
	public static ArrayList<ObjectOnMap> getBuildingAndItemList(String mapId) {
		final String TAG_BUILDINGS = "buildings";
		final String TAG_ITEMS = "items";
		ArrayList<ObjectOnMap> objects = new ArrayList<ObjectOnMap>();
		JSONArray jObjects = null;
		 
		// getting JSON string from URL
		JSONObject json = getJSONFromUrl(URL+"buildingList.php?mapId="+mapId);
		
		try {
		    // Getting Array of buildings
			jObjects = json.getJSONArray(TAG_BUILDINGS);
		 
		    // looping through All Contacts
		    for(int i = 0; i < jObjects.length(); i++){
		        JSONObject building = jObjects.getJSONObject(i);
		        //$data_string .= '{ "id": "'.$id.'", "lat": "'.$lat.'","lon": "'.$lon.'","name": "'.$name.'" },';
		        Log.v("HttpHelper",building.toString());
		        objects.add(new ObjectOnMap(
		        		building.getDouble("lat"), 
		        		building.getDouble("lon"), 
		        		building.getString("id"), 
		        		building.getString("name"), 
		        		"building"));  
		    }
		    jObjects = json.getJSONArray(TAG_ITEMS);
		    for(int i = 0; i < jObjects.length(); i++){
		        JSONObject item = jObjects.getJSONObject(i);
		        //$data_string .= '{ "id": "'.$id.'", "lat": "'.$lat.'","lon": "'.$lon.'","name": "'.$name.'" },';
		        Log.v("HttpHelper",item.toString());
		        objects.add(new ObjectOnMap(
		        		item.getDouble("lat"), 
		        		item.getDouble("lon"), 
		        		item.getString("id"), 
		        		item.getString("name"), 
		        		"item"));  
		    }
		} catch (JSONException e) {
		    e.printStackTrace();
		}	
    	return objects;
    }
	
	private static StringBuilder inputStreamToString(InputStream is){
		String line = "";
		StringBuilder total = new StringBuilder();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		try {
			while ((line = rd.readLine()) != null) {
				total.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return total;
	}

	public static String parseResult(String rawResult){
		return rawResult.split("<!--")[0];
	}
	
	//Ucitava JSON odgovor koji dobije kada mu se prosledi url
	public static JSONObject getJSONFromUrl(String url) {
        // Making HTTP request
        try {
            // defaultHttpClient
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
 
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();           
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }
        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }
        // return JSON String
        return jObj;
    }
}
