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

import java.util.Date;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
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
import android.widget.TextView;
import android.widget.Toast;

public class EditList extends MediaNoteAbs {
	private LocationManager LM; 
	private Note mNote;
	private Vibrator VV;
	public static final int ACTIVITY_RESULT_TEXT = 1001;
	public static final int ACTIVITY_RESULT_PHOTO = 1002;
	public static final int ACTIVITY_RESULT_AUDIO = 1003;
	public static final int DIALOG_VOICEREC = 2004;
	
	private LocationListener loclis = new LocationListener() {
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}
		@Override
		public void onProviderEnabled(String provider) {}
		@Override
		public void onProviderDisabled(String provider) {}
		@Override
		public void onLocationChanged(final Location location) {
			Log.i("LocationListener", location.getLatitude()+" "+location.getLongitude());
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
			//saveLocation(getIntent().getExtras().getLong("id"), location);
			LM.removeUpdates(this);
		}
	};
	
	public Object onRetainNonConfigurationInstance() {
		return mNote;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		LM = (LocationManager) getSystemService(LOCATION_SERVICE);
		VV = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		
		final long rowid = getIntent().getExtras().getLong("id");
		MediaNoteDB db = new MediaNoteDB(EditList.this);
		db.open();
		mNote = (Note) getLastNonConfigurationInstance();
        if(mNote==null){
        	mNote = new Note();
			db.getAllContent(rowid, mNote, getApplicationContext());
        }
		View ll = getLayoutInflater().inflate(R.layout.edit,null);
		
		//title & date
    	EditText title = (EditText) ll.findViewById(R.id.editlist_title);
    	title.setText(mNote.getTitle());
    	
    	db.close();
    	LinearLayout content = (LinearLayout) ll.findViewById(R.id.container);
    	
    	if(!mNote.getText().toString().equals("")){
    		ViewGroup text = layout_add_text(content, mNote.getText());
        	content.addView(text);
    	}
    	
    	for(final Location loc : mNote.getLocationsList()){
    		ViewGroup location = layout_add_location(content, loc, rowid);
			content.addView(location);
    	}
    	for(Uri uri : mNote.getImgList()){
    		View image = layout_add_image(content, uri, rowid);		
    		content.addView(image);
    	}
    	for(Uri uri : mNote.getVoicerecList()){
    		ViewGroup voice = layout_add_voicerec(content, uri, rowid);
    		content.addView(voice);
    	}
    	
    	View b_save = ll.findViewById(R.id.button_save);
    	View b_cancel = ll.findViewById(R.id.button_cancel);
    	
    	b_save.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				VV.vibrate(50);
				long _id = getIntent().getExtras().getLong("id");
				
				EditText title = (EditText) getWindow().getDecorView().findViewById(R.id.editlist_title);
				saveTitle(_id, title.getText().toString());
				CheckedTextView text = (CheckedTextView) getWindow().getDecorView().findViewById(R.id.text);
				if(text!=null && !text.getText().toString().equals(""))
					saveText(_id, (Spanned)text.getText());
				for(Uri uri : mNote.getImgList()){
					saveImage(_id, uri);
				}
				for(Location loc : mNote.getLocationsList()){
					saveLocation(_id, loc);
				}
				for(Uri uri : mNote.getVoicerecList()){
					saveVoicerec(_id, uri);
				}
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
		break;
		}
		case 2:{
			Intent i = new Intent("android.media.action.IMAGE_CAPTURE");
            //Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(i, ACTIVITY_RESULT_PHOTO);
			break;
		}
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
			break;
		}
		case 4:{
			/*
			Intent i = new Intent(EditList.this, AudioCapture.class);
			i.putExtra("id", getIntent().getExtras().getLong("id"));
			startActivityForResult(i, ACTIVITY_RESULT_AUDIO);
			*/
			Bundle b = new Bundle();
			b.putLong("id", getIntent().getExtras().getLong("id"));
			showDialog(DIALOG_VOICEREC, b);
			break;
		}
		default:
			
			break;
		}
		return true;
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		if(id==DIALOG_VOICEREC){
			final Dialog d = new Dialog(EditList.this);
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
				d.setTitle(R.string.title_dialog_audiocapture);
				View rl = getLayoutInflater().inflate(R.layout.dialog_audiorecord, null); 
				d.setContentView(rl);
				final MediaRecorder MR = new MediaRecorder();
				try{
					MR.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
					MR.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
					final long _id = getIntent().getExtras().getLong("id");
					final String extStorageDir = Environment.getExternalStorageDirectory().getAbsolutePath()+"/VoiceRecorder/";
					final String name = "medianote_audio_"+_id+"_"+new Date().getTime()+".amr";
					MR.setOutputFile(extStorageDir+name);
					MR.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
					MR.prepare();
					MR.start();

					rl.findViewById(R.id.stop).setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							MR.stop();
							d.dismiss();
							LinearLayout content = (LinearLayout) getWindow().getDecorView().findViewById(R.id.container);
							ViewGroup ll_voice = layout_add_voicerec(content, Uri.parse(extStorageDir+name), _id);
							mNote.addVoicerec(Uri.parse(extStorageDir+name));
							content.addView(ll_voice);
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else{
				d.setTitle(R.string.error);
				TextView tv = new TextView(this);
				tv.setText(R.string.label_dialog_audiocapture_error);
			}
			return d;
		}
		return null;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode==ACTIVITY_RESULT_PHOTO && data!=null){
			final Uri uri = data.getData();
			ImageView img = (ImageView) getLayoutInflater().inflate(R.layout.image, null);
			img.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    		img.setAdjustViewBounds(true);
    		img.setImageBitmap(getScaledBitmap(uri));
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
