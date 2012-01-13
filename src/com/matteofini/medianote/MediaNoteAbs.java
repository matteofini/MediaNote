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
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class MediaNoteAbs extends Activity{
	
	protected void deleteLocation(long _id, Location loc){
		MediaNoteDB db = new MediaNoteDB(getApplicationContext());
		db.open();
		int res = db.deleteLocation(_id, loc);
		db.close();
		if(res>0) Log.i("deleteLocation", "location deleted in list "+_id);
		LinearLayout rl = (LinearLayout) getWindow().getDecorView().findViewById(R.id.container);
		rl.removeView(rl.findViewWithTag(loc));
	}
	
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
	
	protected void deleteImage(long _id, final Uri uri){
		MediaNoteDB db = new MediaNoteDB(getApplicationContext());
		db.open();
		int res = db.deleteImage(_id, uri);
		db.close();
		if(res>0) Log.i("deleteImage", "deleted image "+uri.toString()+" in list with _id "+_id);
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
	
	public boolean checkImage(Uri uri){
		Cursor c = Media.query(getContentResolver(), uri, new String[]{"_data"}, null, null, null);
		c.moveToFirst();
		return (c.getCount()>0);
	}
	
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
	
	OnCreateContextMenuListener create_cmenu_img(long rowid, Uri uri){
		final long a = rowid;
		final Uri b = uri;
		OnCreateContextMenuListener cmenu_img = new OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				Log.d("create_cmenu_img", "_"+a+"  "+b.toString());
				menu.add("elimina immagine");
				menu.getItem(0).setOnMenuItemClickListener(new OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem arg0) {
						deleteImage(a, b);
						return true;
					}
				});
			}
		};
		return cmenu_img;
	}
	
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

	
	protected void saveLocation(long _id, Location loc){
		MediaNoteDB db = new MediaNoteDB(getApplicationContext());
		db.open();
		if(!db.existsLocation(_id, loc))
			if(db.addLocation(_id, loc)!=-1)
				Log.i("saveLocation", "Location saved");
	}
	
	protected void saveImage(long _id, Uri uri){
		MediaNoteDB db = new MediaNoteDB(getApplicationContext());
		db.open();
		if(!db.existsImage(_id, uri))
			if(db.addImage(_id, uri)!=-1)
				Log.i("saveImage", "Image saved");
	}
	
	protected Bitmap getScaledBitmap(Uri uri){
		Cursor c = Media.query(getContentResolver(), uri, new String[]{"_data"});
		c.moveToFirst();
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inSampleSize = 8;
		Bitmap bm = BitmapFactory.decodeFile(c.getString(0), opt);
		//Bitmap bm = Bitmap.createScaledBitmap(x, x.getWidth()/2, x.getHeight()/2, false);
		return bm;
	}
}
