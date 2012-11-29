package edu.elfak.chasegame;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

public class ChaseGameApplication extends Application {

	private static ChaseGameApplication instance;
	
	public ChaseGameApplication() {
		instance = this;
	}
	
	public static Context getContext(){
		return instance;
	}

}
