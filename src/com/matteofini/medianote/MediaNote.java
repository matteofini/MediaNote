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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MediaNote extends ListActivity {
	
	private static final int DIALOG_ADDTITLE = 0;
	private Cursor mCursor;
	private Vibrator VV;
		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        VV = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        
        View b = findViewById(R.id.button_addlist);
        b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_ADDTITLE);
				VV.vibrate(50);
			}
		});
        
        MediaNoteDB db = new MediaNoteDB(MediaNote.this);
        db.open();
        mCursor = db.getList();
        startManagingCursor(mCursor);
        //System.out.println("\t count: "+c.getCount());
        SimpleCursorAdapter sca = new MyAdapter(this, R.layout.item, mCursor, new String[]{"title", "date", "summary"}, new int[]{R.id.item_title, R.id.item_date, R.id.item_preview});
        setListAdapter(sca);   
    } 
    
    public class MyAdapter extends SimpleCursorAdapter{
    	protected int count;
    	
    	public MyAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
    		super(context, layout, c, from, to);
    		count=0;
    	}
    	
    	@Override
    	public View getView(final int position, View convertView, ViewGroup parent) {
     		View v = getLayoutInflater().inflate(R.layout.item, null);
    		
    		final Cursor c = getCursor();
    		c.moveToPosition(position);
    		
    		TextView title = (TextView) v.findViewById(R.id.item_title);
    		title.setText(c.getString(c.getColumnIndex("title")));
    		
    		TextView date = (TextView) v.findViewById(R.id.item_date);
    		int mills = c.getInt(c.getColumnIndex("date"));
    		
    		CharSequence str = DateFormat.format("dd/MM/yy h:mmaa", mills);
    		date.setText(str);
    		/*
    		String c_summ = c.getString(c.getColumnIndex("summary"));
	    	if(c_summ!=null){
	    		Log.i("summary", c_summ);
    			TextView summary = (TextView) v.findViewById(R.id.item_preview);
    			Spanned spanned = Html.fromHtml(c_summ);
    			if(spanned.length()>=20)
    				summary.setText(spanned.subSequence(0, 20));
    			else
    				summary.setText(spanned);
	    	}
	    	*/
    		OnClickListener view_ocl = new OnClickListener() {
				long id = c.getLong(0);
    			@Override
				public void onClick(View v) { 
					view(id);
					VV.vibrate(50);
				}
			};
			v.setOnClickListener(view_ocl);
			
			registerForContextMenu(v);
			v.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
				@Override
				public void onCreateContextMenu(ContextMenu menu, View v,
						ContextMenuInfo menuInfo) {
					menu.add("modifica");
					menu.getItem(0).setOnMenuItemClickListener(new OnMenuItemClickListener() {
						@Override
						public boolean onMenuItemClick(MenuItem item) {
							edit(c.getLong(0));
							return true;
						}
					});
					menu.add("elimina");
					menu.getItem(1).setOnMenuItemClickListener(new OnMenuItemClickListener() {
						@Override
						public boolean onMenuItemClick(MenuItem item) {
							delete(c.getLong(0));
							return true;
						}
					});
					menu.add("duplica lista e contenuto");
					menu.getItem(2).setOnMenuItemClickListener(new OnMenuItemClickListener() {
						@Override
						public boolean onMenuItemClick(MenuItem item) {
							MediaNoteDB db = new MediaNoteDB(getApplicationContext());
							db.open();
							db.cloneListAndContent(c.getLong(0));
							db.close();
							mCursor.requery();
					        setListAdapter(new MyAdapter(MediaNote.this, R.layout.item, mCursor, new String[]{"title", "date", "summary"}, new int[]{R.id.item_title, R.id.item_date, R.id.item_preview}));
							return true;
						}
					});
				}
			});
    		return v;
    	}
    	
    	private void view(long id){
    		Intent i = new Intent();
			i.setComponent(new ComponentName(MediaNote.this, ShowList.class));
			i.putExtra("id", id);
			startActivity(i);
    	}
    	
    	private void edit(long id){
    		Intent i = new Intent();
			i.setComponent(new ComponentName(MediaNote.this, EditList.class));
			i.putExtra("id", id);
			startActivity(i);
    	}
    	
    	private void delete(long id){
    		MediaNoteDB db = new MediaNoteDB(MediaNote.this);
	        db.open();
	        for(Uri uri : db.getImages(id, MediaNote.this)){
	        	if(db.deleteImage(id, uri)>0)
	        		Log.i("delete", "deleted all images");
	        }
	        for(Location loc : db.getLocations(id)){
	        	if(db.deleteLocation(id, loc)>0)
	        		Log.i("delete", "deleted all locations");
	        }
        	for(Uri uri : db.getVoiceRecords(id, MediaNote.this)){
	        	if(db.deleteVoicerec(id, uri)>0)
	        		Log.i("delete", "deleted all records");
	        }
	        int res = db.deleteList(id);
	        Toast.makeText(getApplicationContext(), getResources().getString(R.string.list_deleted), Toast.LENGTH_SHORT).show();
	        if(res>0) Log.i("MediaNote", "list with id "+id+" deleted");
	        db.close();
	        mCursor.requery();
	        setListAdapter(new MyAdapter(MediaNote.this, R.layout.item, mCursor, new String[]{"title", "date", "summary"}, new int[]{R.id.item_title, R.id.item_date, R.id.item_preview}));
    	}
    	
    }

    
    @Override
    protected Dialog onCreateDialog(int id) {
    	if(id==DIALOG_ADDTITLE){
    		AlertDialog d =  new AlertDialog.Builder(MediaNote.this).create();
    		final View ll = getLayoutInflater().inflate(R.layout.dialog_addtitle, null);
    		d.setTitle(getResources().getString(R.string.dialog_addtitle_title));
    		d.setCancelable(true);
    		d.setView(ll);
			d.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {dialog.dismiss();}
			});
			d.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					EditText edit = (EditText) ll.findViewById(R.id.edit_addtitle);
					String str = edit.getText().toString();
					if(!str.equals("")){
						MediaNoteDB db = new MediaNoteDB(getApplicationContext());
						db.open();
						long id = db.addList(str);
						db.close();
						Toast.makeText(getApplicationContext(), getResources().getString(R.string.list_created), Toast.LENGTH_SHORT).show();
						Log.i("MediaNote", "Lista creata con id "+id);
						mCursor.requery();
						setListAdapter(new MyAdapter(MediaNote.this, R.layout.item, mCursor, new String[]{"title", "date", "summary"}, new int[]{R.id.item_title, R.id.item_date, R.id.item_preview}));
						Intent i = new Intent();
						i.putExtra("id", id);
						i.setComponent(new ComponentName("com.matteofini.medianote", "com.matteofini.medianote.EditList"));
						startActivity(i);
					}
					else{
						edit.requestFocus();
						Toast.makeText(getApplicationContext(), "inserisci un titolo non vuoto", Toast.LENGTH_SHORT).show();
					}
				}
			});
			return d;
    	}
    	else
    		return null;
    }
}