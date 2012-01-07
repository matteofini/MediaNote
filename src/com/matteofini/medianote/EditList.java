/**
 *  Program: Media Note
 *  Author: Matteo Fini <mf.calimero@gmail.com>
 *  Year: 2012
 *  
 *	This file is part of Media Note.
 *	Media Note is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  Media Note is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License
 *  along with Media Note.  If not, see <http://www.gnu.org/licenses/>. 
 */
package com.matteofini.medianote;

import android.content.ComponentName;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class EditList extends MediaNoteAbs {
	private LocationManager LM; 
	private Note mNote;
	private Vibrator VV;
	public static final int ACTIVITY_RESULT_TEXT = 1001;
	public static final int ACTIVITY_RESULT_PHOTO = 1002;
	
	private LocationListener loclis = new LocationListener() {
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}
		@Override
		public void onProviderEnabled(String provider) {}
		@Override
		public void onProviderDisabled(String provider) {}
		@Override
		public void onLocationChanged(final Location location) {
			System.out.println("\t\t"+location.getLatitude()+" "+location.getLongitude());
			LinearLayout content = (LinearLayout) getWindow().getDecorView().findViewById(R.id.container);
			RelativeLayout rl_loc = (RelativeLayout) getLayoutInflater().inflate(R.layout.location, null);
			mNote.addLocation(new Location(location));
			
			OnClickListener loc_click = new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse("geo:"+location.getLatitude()+","+location.getLongitude()+"?z=13"));
					startActivity(i);
				}
			};
			rl_loc.findViewById(R.id.loc_button).setOnClickListener(loc_click);
			rl_loc.findViewById(R.id.loc_label).setOnClickListener(loc_click);
			
			Address addr = reverseGeolocation(location);
			CheckedTextView loc_label = (CheckedTextView) rl_loc.findViewById(R.id.loc_label);
			String line="";
			for(int j=0;j<addr.getMaxAddressLineIndex();j++){
				line+=addr.getAddressLine(j)+" ";
			}
			loc_label.setText(line+" - "+addr.getCountryName());
			//Address[addressLines=[0:"Via Alfredo Catalani, 15",1:"40069 Zola Predosa BO",2:"Italia"],feature=15,admin=Emilia Romagna,sub-admin=Bologna,locality=Zola Predosa,thoroughfare=Via Alfredo Catalani,postalCode=40069,countryCode=IT,countryName=Italia,hasLatitude=true,latitude=44.482682,hasLongitude=true,longitude=11.2435482,phone=null,url=null,extras=null]			
			content.addView(rl_loc);
			LM.removeUpdates(this);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		LM = (LocationManager) getSystemService(LOCATION_SERVICE);
		VV = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		mNote = new Note();
		
		final long rowid = getIntent().getExtras().getLong("id");
		MediaNoteDB db = new MediaNoteDB(EditList.this);
		db.open();
		db.getAllContent(rowid, mNote, getApplicationContext());
		View ll = getLayoutInflater().inflate(R.layout.edit,null);
		
		//title & date
    	EditText title = (EditText) ll.findViewById(R.id.editlist_title);
    	title.setText(mNote.getTitle());
    	
    	db.close();
    	LinearLayout content = (LinearLayout) ll.findViewById(R.id.container);
    	if(!mNote.getText().toString().equals("")){
    		RelativeLayout rl_text = (RelativeLayout) getLayoutInflater().inflate(R.layout.text, null);
    		CheckedTextView text = (CheckedTextView) rl_text.findViewById(R.id.text);
    		text.setText(mNote.getText());
    		text.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					callTextEditor();
				}
			});
    		registerForContextMenu(text);
    		text.setOnCreateContextMenuListener(cmenu_text_edit);
    		content.addView(rl_text);
    	}
    	for(final Location loc : mNote.getLocationsList()){
    		final RelativeLayout rl_loc = (RelativeLayout) getLayoutInflater().inflate(R.layout.location, null);
    		OnClickListener loc_click = new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse("geo:"+loc.getLatitude()+","+loc.getLongitude()+"?z=13"));
					startActivity(i);
				}
			};
			rl_loc.findViewById(R.id.loc_button).setOnClickListener(loc_click);
			rl_loc.findViewById(R.id.loc_label).setOnClickListener(loc_click);
			
			Address addr = reverseGeolocation(loc);
			CheckedTextView loc_label = (CheckedTextView) rl_loc.findViewById(R.id.loc_label);
			String line="";
			for(int j=0;j<addr.getMaxAddressLineIndex();j++){
				line+=addr.getAddressLine(j)+" ";
			}
			loc_label.setText(line+" - "+addr.getCountryName());
			//Address[addressLines=[0:"Via Alfredo Catalani, 15",1:"40069 Zola Predosa BO",2:"Italia"],feature=15,admin=Emilia Romagna,sub-admin=Bologna,locality=Zola Predosa,thoroughfare=Via Alfredo Catalani,postalCode=40069,countryCode=IT,countryName=Italia,hasLatitude=true,latitude=44.482682,hasLongitude=true,longitude=11.2435482,phone=null,url=null,extras=null]
    		registerForContextMenu(rl_loc);
			content.addView(rl_loc);
    	}
    	for(Uri uri : mNote.getImgList()){
    		ImageView img = (ImageView) getLayoutInflater().inflate(R.layout.image, null);
			img.setImageURI(uri);
    		img.setScaleType(ImageView.ScaleType.CENTER_CROP);
    		img.setAdjustViewBounds(true);
    		content.addView(img);
    		registerForContextMenu(img);
    	}
    	
    	View b_save = ll.findViewById(R.id.button_save);
    	View b_cancel = ll.findViewById(R.id.button_cancel);
    	
    	b_save.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				VV.vibrate(50);
				MediaNoteDB db = new MediaNoteDB(EditList.this);
				db.open();
				long _id = getIntent().getExtras().getLong("id");
				
				EditText title = (EditText) getWindow().getDecorView().findViewById(R.id.editlist_title);
				if(db.setTitle(_id, title.getText().toString())>0)
					Log.println(Log.INFO, "EditList", "title updated");
				CheckedTextView text = (CheckedTextView) getWindow().getDecorView().findViewById(R.id.text);
				if(text!=null && !text.getText().toString().equals("")){
					db.setText(_id, Html.toHtml((Spanned)text.getText()));
					Log.println(Log.INFO, "EditList", "text content updated");
				}
				for(Uri uri : mNote.getImgList()){
					if(!db.existsImage(_id, uri)){
						if(db.addImage(_id, uri)!=-1)
							Log.println(Log.INFO, "EditList", "added image "+uri);
					}
				}
				for(Location loc : mNote.getLocationsList()){
					if(!db.existsLocation(_id, loc)){
						if(db.addLocation(_id, loc)!=-1)
							Log.println(Log.INFO, "EditList", "added location "+loc.getLatitude()+" "+loc.getLongitude());
					}
				}
				db.close();
				Toast.makeText(getApplicationContext(), "modifiche salvate", Toast.LENGTH_SHORT).show();
			}
		});
    	b_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				VV.vibrate(50);
				finish();
			}
		});
    	setContentView(ll);
    	ll.requestFocus();
	}
		
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.option_edit, menu);
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getOrder()) {
		case 1:{
			Intent i = new Intent();
			i.setComponent(new ComponentName("com.matteofini.medianote", "com.matteofini.medianote.TextEditor"));
			i.putExtra("id", getIntent().getExtras().getLong("id"));
			startActivityForResult(i, ACTIVITY_RESULT_TEXT);
		break;}
		case 2:{
			Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(i, ACTIVITY_RESULT_PHOTO);
		break;}
		case 3:{
			//List<String> providers = LM.getAllProviders();
			if(!LM.isProviderEnabled("network") && !LM.isProviderEnabled("gps")){
				Toast.makeText(EditList.this, "Abilita una origine dati (rete o gps) per 'La mia posizione' nelle impostazioni di sistema", 2).show();
			}
			else if(LM.isProviderEnabled("network")){
				ConnectivityManager CM = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
				NetworkInfo info = CM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				if(!info.isConnected()){
					Toast.makeText(EditList.this, "Non sei connesso a nessuna rete WIFI. Il telefono può essere fuori portata.", 2).show();
					//LM.getLastKnownLocation("network");	//TODO
				}
				else
					LM.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Long.MAX_VALUE, Float.MAX_VALUE, loclis);
			}
			else
				LM.requestLocationUpdates(LocationManager.GPS_PROVIDER, Long.MAX_VALUE, Float.MAX_VALUE, loclis);
		break;}
		default:
			break;
		}
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode==ACTIVITY_RESULT_PHOTO && data!=null){
			final Uri uri = data.getData();
			ImageView img = (ImageView) getLayoutInflater().inflate(R.layout.image, null);
			img.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    		img.setAdjustViewBounds(true);
    		img.setImageURI(uri);
			img.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(uri);
					startActivity(i);
				}
			});
			mNote.addImg(uri);
			LinearLayout ll = (LinearLayout) getWindow().findViewById(R.id.container);
			ll.addView(img);
			View dec = getWindow().getDecorView();
			dec.findViewById(R.id.image);
		}
		else if(requestCode==ACTIVITY_RESULT_TEXT){
			LinearLayout ll = (LinearLayout) getWindow().findViewById(R.id.container);
			CheckedTextView text = null;
			if((text = (CheckedTextView) ll.findViewById(R.id.text))!=null){
				text.setText(Html.fromHtml(data.getExtras().getString("text")));
			}
			else{
				RelativeLayout rl_text = (RelativeLayout) getLayoutInflater().inflate(R.layout.text, null);
				text = (CheckedTextView) rl_text.findViewById(R.id.text);
				text.setText(Html.fromHtml(data.getExtras().getString("text")));
				ll.addView(rl_text);
				text.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						callTextEditor();
					}
				});
				registerForContextMenu(text);
				text.setOnCreateContextMenuListener(cmenu_text_edit);
			}
		}
	}
	
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
        unbindDrawables(getWindow().getDecorView().getRootView());
        System.gc();
    }

    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }
	
}
