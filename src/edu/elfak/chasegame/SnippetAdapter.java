package edu.elfak.chasegame;



/***
Copyright (c) 2012 CommonsWare, LLC
Licensed under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy
of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
by applicable law or agreed to in writing, software distributed under the
License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
OF ANY KIND, either express or implied. See the License for the specific
language governing permissions and limitations under the License.

From _The Busy Coder's Guide to Android Development_
  http://commonsware.com/Android
*/

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;


class SnippetAdapter implements InfoWindowAdapter {
LayoutInflater inflater=null;

SnippetAdapter(LayoutInflater inflater) {
  this.inflater=inflater;
}

@Override
public View getInfoWindow(Marker marker) {
  return(null);
}

@Override
public View getInfoContents(Marker marker) {
  View popup=inflater.inflate(R.layout.snippetlayout, null);

  TextView tv=(TextView)popup.findViewById(R.id.title);
  tv.setText(marker.getTitle());
  tv=(TextView)popup.findViewById(R.id.snippet);
  tv.setText(marker.getSnippet());
  //if(marker.getSnippet().startsWith(" Novac u banci: "))
  //if(marker.getSnippet()==null)
	//  popup.findViewById(R.id.caption).setVisibility(View.GONE);
  
  return(popup);
}
}