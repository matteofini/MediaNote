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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore.Images.Media;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public abstract class MediaNoteAbs extends Activity{
	
	/**
	 * 
	 * @param _id
	 * @param loc
	 */
	protected void deleteLocation(long _id, Location loc){
		MediaNoteDB db = new MediaNoteDB(getApplicationContext());
		db.open();
		int res = db.deleteLocation(_id, loc);
		db.close();
		if(res>0) Log.i("deleteLocation", "location deleted in list "+_id);
		LinearLayout rl = (LinearLayout) getWindow().getDecorView().findViewById(R.id.container);
		rl.removeView(rl.findViewWithTag(loc));
	}
	
	/**
	 * 
	 */
	protected void deleteText(){
		MediaNoteDB db = new MediaNoteDB(getApplicationContext());
		db.open();
		long _id = getIntent().getExtras().getLong("id");
		int res = db.deleteText(_id);
		Log.i("deleteText", "\t "+res+" rows deleted in list with _id "+_id);
		db.close();
		
		LinearLayout ll = (LinearLayout) getWindow().getDecorView().findViewById(R.id.container);
		ll.removeView(ll.findViewById(R.id.rl_text));
	}
	
	/**
	 * 
	 * @param _id
	 * @param uri
	 */
	protected void deleteVoicerec(long _id, final Uri uri){
		MediaNoteDB db = new MediaNoteDB(getApplicationContext());
		db.open();
		int res = db.deleteVoicerec(_id, uri);
		db.close();
		if(res>0) Log.i("deleteVoicerec", "voicerec "+uri.toString()+" deleted in list with _id "+_id);
		AlertDialog d = new AlertDialog.Builder(MediaNoteAbs.this).create();
		d.setTitle("Conferma cancellazione");
		d.setMessage("Eliminare la registrazione dalla memoria?");
		d.setButton(DialogInterface.BUTTON_POSITIVE, "SI", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				File f = new File(uri.toString());
				if(f.delete())
					Log.i("deleteVoicerec", f.getAbsolutePath()+" deleted");
			}
		});
		d.setButton(DialogInterface.BUTTON_NEGATIVE, "NO", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) { dialog.dismiss(); }
		});
		LinearLayout rl = (LinearLayout) getWindow().getDecorView().findViewById(R.id.container);
		rl.removeView(rl.findViewWithTag(uri.toString()));
		d.show();
	}
	
	/**
	 * 
	 * @param _id
	 * @param uri
	 */
	protected void deleteImage(long _id, final Uri uri){
		MediaNoteDB db = new MediaNoteDB(getApplicationContext());
		db.open();
		int res = db.deleteImage(_id, uri);
		db.close();
		if(res>0) Log.i("deleteImage", "image "+uri.toString()+" deleted in list with _id "+_id);
		AlertDialog d = new AlertDialog.Builder(MediaNoteAbs.this).create();
		d.setTitle("Conferma cancellazione");
		d.setMessage("Vuoi cancellare l'immagine anche dalla memoria del telefono?");
		d.setButton(DialogInterface.BUTTON_POSITIVE, "SI", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Cursor c = Media.query(getContentResolver(), uri, new String[]{"_data"}, null, null, null);
				c.moveToFirst();
				File f = new File(c.getString(0));
				if(f.delete())
					Log.i("deleteImage", f.getAbsolutePath()+" deleted");
			}
		});
		d.setButton(DialogInterface.BUTTON_NEGATIVE, "NO", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) { dialog.dismiss(); }
		});
		LinearLayout rl = (LinearLayout) getWindow().getDecorView().findViewById(R.id.container);
		rl.removeView(rl.findViewWithTag(uri.toString()));
		d.show();
	}
	
	/**
	 * 
	 * @param uri
	 * @return
	 */
	public boolean checkImage(Uri uri){
		Cursor c = Media.query(getContentResolver(), uri, new String[]{"_data"}, null, null, null);
		c.moveToFirst();
		return (c.getCount()>0);
	}
	
	/**
	 * 
	 * @param loc
	 * @return
	 */
	protected Address reverseGeolocation(Location loc) {
		android.location.Geocoder geo = new Geocoder(getApplicationContext());
		List<Address> res;
		Address addr = new Address(new Locale(""));
		try {
			res = geo.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
			addr = res.get(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return addr;
	}
	
	/**
	 * 
	 */
	OnCreateContextMenuListener cmenu_text = new OnCreateContextMenuListener() {
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			menu.add("elimina testo");
			menu.getItem(0).setOnMenuItemClickListener(new OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					deleteText();
					return true;
				}
			});
		}
	};
	
	/**
	 * 
	 */
	OnCreateContextMenuListener cmenu_text_edit = new OnCreateContextMenuListener() {
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			menu.add("modifica testo");
			menu.getItem(0).setOnMenuItemClickListener(new OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					callTextEditor();
					return true;
				}
			});
			menu.add("elimina testo");
			menu.getItem(1).setOnMenuItemClickListener(new OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					deleteText();
					return true;
				}
			});
		}
	};
	
	/**
	 * 
	 * @param rowid
	 * @param uri
	 * @return
	 */
	OnCreateContextMenuListener create_cmenu_img(final long rowid, final Uri uri){
		OnCreateContextMenuListener cmenu_img = new OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				menu.add("elimina immagine");
				menu.getItem(0).setOnMenuItemClickListener(new OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem arg0) {
						deleteImage(rowid, uri);
						return true;
					}
				});
			}
		};
		return cmenu_img;
	}
	
	/**
	 * 
	 * @param rowid
	 * @param uri
	 * @return
	 */
	OnCreateContextMenuListener create_cmenu_voicerec(final long rowid, final Uri uri){
		OnCreateContextMenuListener cmenu_voicerec = new OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				menu.add("elimina registrazione");
				menu.getItem(0).setOnMenuItemClickListener(new OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem arg0) {
						deleteVoicerec(rowid, uri);
						return true;
					}
				});
			}
		};
		return cmenu_voicerec;
	}
	
	/**
	 * 
	 * @param rowid
	 * @param loc
	 * @return
	 */
	OnCreateContextMenuListener create_cmenu_loc(final long rowid, final Location loc){
		OnCreateContextMenuListener cmenu_loc = new OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				menu.add("elimina posizione");
				menu.getItem(0).setOnMenuItemClickListener(new OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						deleteLocation(rowid, loc);
						return true;
					}
				});			
			}
		};
		return cmenu_loc;
	};
	
	protected void callTextEditor(){
		Intent i = new Intent();
		i.putExtra("id", getIntent().getExtras().getLong("id"));
		TextView text = (TextView) getWindow().getDecorView().findViewById(R.id.text);
		i.putExtra("text", Html.toHtml((Spanned)text.getText()));
		i.setComponent(new ComponentName("com.matteofini.medianote", "com.matteofini.medianote.TextEditor"));
		startActivityForResult(i, EditList.ACTIVITY_RESULT_TEXT);
	}

	/**
	 * 
	 * @param _id
	 * @param title
	 */
	protected void saveTitle(long _id, String title){
		MediaNoteDB db = new MediaNoteDB(getApplicationContext());
		db.open();
		if(db.setTitle(_id, title)>0)
			Log.println(Log.INFO, "EditList", "title updated");
		db.close();
	}
	
	/**
	 * 
	 * @param _id
	 * @param spannedtext
	 */
	protected void saveText(long _id, Spanned spannedtext){
		MediaNoteDB db = new MediaNoteDB(getApplicationContext());
		db.open();
		db.setText(_id, Html.toHtml(spannedtext));
		Log.println(Log.INFO, "EditList", "text content updated");
		db.close();
	}
	
	/**
	 * 
	 * @param _id
	 * @param loc
	 */
	protected void saveLocation(long _id, Location loc){
		MediaNoteDB db = new MediaNoteDB(getApplicationContext());
		db.open();
		if(!db.existsLocation(_id, loc))
			if(db.addLocation(_id, loc)!=-1)
				Log.i("saveLocation", "Location saved");
		db.close();
	}
	
	/**
	 * 
	 * @param _id
	 * @param uri
	 */
	protected void saveVoicerec(long _id, Uri uri){
		MediaNoteDB db = new MediaNoteDB(getApplicationContext());
		db.open();
		if(!db.existsVoicerec(_id, uri))
			if(db.addVoicerec(_id, uri)!=-1)
				Log.i("saveVoicerec", "Voicerec saved");
		db.close();
	}
	
	/**
	 * 
	 * @param _id
	 * @param uri
	 */
	protected void saveImage(long _id, Uri uri){
		MediaNoteDB db = new MediaNoteDB(getApplicationContext());
		db.open();
		if(!db.existsImage(_id, uri))
			if(db.addImage(_id, uri)!=-1)
				Log.i("saveImage", "Image saved");
		db.close();
	}
	
	/**
	 * 
	 * @param uri
	 * @return
	 */
	protected Bitmap getScaledBitmap(Uri uri){
		Cursor c = Media.query(getContentResolver(), uri, new String[]{"_data"});
		c.moveToFirst();
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inSampleSize = 8;
		Bitmap bm = BitmapFactory.decodeFile(c.getString(0), opt);
		//Bitmap bm = Bitmap.createScaledBitmap(x, x.getWidth()/2, x.getHeight()/2, false);
		return bm;
	}

	/**
	 * 
	 * @param path
	 */
	protected void play(String path) {
		MediaPlayer MP = new MediaPlayer();
		try {
			MP.setDataSource(path);
			MP.prepare();
			MP.start();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param parent_container
	 * @param path
	 * @param name
	 * @return
	 */
	protected ViewGroup layout_add_voicerec(ViewGroup parent_container, final Uri uri) {
		RelativeLayout rl_voice = (RelativeLayout) getLayoutInflater().inflate(R.layout.voice, null);
		CheckedTextView voice_label = (CheckedTextView) rl_voice.findViewById(R.id.voice_label);
		voice_label.setText(uri.getLastPathSegment());
		
		View.OnClickListener ocl = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				play(uri.toString());
			}
		};
		voice_label.setOnClickListener(ocl);
		rl_voice.findViewById(R.id.voice_button).setOnClickListener(ocl);
		return rl_voice;
	}
	
	/**
	 * 
	 * @param parent
	 * @param loc
	 * @return
	 */
	protected ViewGroup layout_add_location(ViewGroup parent, final Location loc, long _id){
		final RelativeLayout rl_loc = (RelativeLayout) getLayoutInflater().inflate(R.layout.location, null);
		Address addr = reverseGeolocation(loc);
		//Address[addressLines=[0:"Via Alfredo Catalani, 15",1:"40069 Zola Predosa BO",2:"Italia"],feature=15,admin=Emilia Romagna,sub-admin=Bologna,locality=Zola Predosa,thoroughfare=Via Alfredo Catalani,postalCode=40069,countryCode=IT,countryName=Italia,hasLatitude=true,latitude=44.482682,hasLongitude=true,longitude=11.2435482,phone=null,url=null,extras=null]
		CheckedTextView loc_label = (CheckedTextView) rl_loc.findViewById(R.id.loc_label);
		String line="";
		for(int j=0;j<addr.getMaxAddressLineIndex();j++){
			line+=addr.getAddressLine(j)+" ";
		}
		loc_label.setText(line+" - "+addr.getCountryName());
		View.OnClickListener loc_click = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse("geo:"+loc.getLatitude()+","+loc.getLongitude()+"?z=13"));
				startActivity(i);
			}
		};
		rl_loc.findViewById(R.id.loc_button).setOnClickListener(loc_click);
		rl_loc.findViewById(R.id.loc_label).setOnClickListener(loc_click);
		registerForContextMenu(rl_loc);
		rl_loc.findViewById(R.id.loc_label).setOnCreateContextMenuListener(create_cmenu_loc(_id, loc));
		return rl_loc;
	}
	
	/**
	 * 
	 * @param parent_container
	 * @param text
	 * @return
	 */
	protected ViewGroup layout_add_text(ViewGroup parent_container, Spanned text){
		RelativeLayout rl_text = (RelativeLayout) getLayoutInflater().inflate(R.layout.text, null);
		CheckedTextView tv = (CheckedTextView) rl_text.findViewById(R.id.text);
		tv.setText(text);
		tv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				callTextEditor();
			}
		});
		registerForContextMenu(tv);
		tv.setOnCreateContextMenuListener(cmenu_text_edit);
		return rl_text;
	}
	
	/**
	 * 
	 * @param parent_container
	 * @param uri
	 * @return
	 */
	protected View layout_add_image(ViewGroup parent_container, final Uri uri, long _id){
		ImageView img = (ImageView) getLayoutInflater().inflate(R.layout.image, null);
		img.setImageBitmap(getScaledBitmap(uri));
		img.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		img.setAdjustViewBounds(true);
		img.setTag(uri.toString());
		img.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(uri);
				startActivity(i);
			}
		});
		registerForContextMenu(img);
		OnCreateContextMenuListener ocl = create_cmenu_img(_id, uri);
		img.setOnCreateContextMenuListener(ocl);
		return img;
	}
}
