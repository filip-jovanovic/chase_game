package edu.elfak.chasegame;

import android.app.Application;
import android.content.Context;

public class ChaseGameApplication extends Application {

	private static ChaseGameApplication instance;
	
	public ChaseGameApplication() {
		instance = this;
	}
	
	public static Context getContext(){
		return instance;
	}

}
