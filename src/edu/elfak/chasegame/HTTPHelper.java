package edu.elfak.chasegame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.widget.Toast;


public class HTTPHelper {

	public static final String LOGIN_URL = "http://android-test-rig.comuf.com/login.php";

	
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
}
