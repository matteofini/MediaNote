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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.Images.Media;
import android.text.Html;
import android.util.Log;

public class MediaNoteDB{
	
	private SQLiteDatabase db;
	private MediaNoteDbOpenHelper dbh;
	private final Context ctx;
	
	private static final String NAME = "medianoteDB";
	private static final int VERSION = 1;
	private static final String ID = "_id";

	private static final String TABLE_GLOBAL = 
		"create table if not exists global ("+ID+" integer primary key autoincrement, title text not null, date integer default NULL);";
	private static final String TABLE_LIST = "create table if not exists list ("+ID+" integer primary key references global ("+ID+") on delete cascade, content text default NULL);";
	private static final String TABLE_IMG = "create table if not exists images ("+ID+" integer not null references global ("+ID+") on delete cascade, uri text not null, unique (_id,uri));";
	private static final String TABLE_LOC = "create table if not exists locations ("+ID+" integer not null references global ("+ID+") on delete cascade, loc text not null, unique (_id,loc));";
	private static final String TABLE_VOICE = "create table if not exists voicerec ("+ID+" integer not null references global ("+ID+") on delete cascade, uri text not null, unique (_id,uri));";

	
	private class MediaNoteDbOpenHelper extends SQLiteOpenHelper{
		public MediaNoteDbOpenHelper(Context context) {
			super(context, Environment.getExternalStorageDirectory().getAbsolutePath()+"/data/com.matteofini.medianote/"+NAME, null, VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(TABLE_GLOBAL);
			db.execSQL(TABLE_LIST);
			db.execSQL(TABLE_IMG);
			db.execSQL(TABLE_LOC);
			db.execSQL(TABLE_VOICE);
			Log.println(Log.INFO, "MediaNoteDbOpenHelper", "medianoteDB created");
		}
		

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w("MediaNoteDbOpenHelper", "Upgrading database from version " + oldVersion + " to "
	                + newVersion + ", which will destroy all old data");
	        db.execSQL("DROP TABLE IF EXISTS global");
	        db.execSQL("DROP TABLE IF EXISTS list");
	        db.execSQL("DROP TABLE IF EXISTS images");
	        db.execSQL("DROP TABLE IF EXISTS locations");
	        onCreate(db);
		}
	
		@Override
		public void onOpen(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			super.onOpen(db);
			onCreate(db);
		}
	}
	
	public MediaNoteDB(Context ctx){
		this.ctx = ctx;
	}
	
	
	public MediaNoteDB open(){
		dbh = new MediaNoteDbOpenHelper(ctx);
		db = dbh.getWritableDatabase();
		return this;
	}
	
	public void close(){
		dbh.close();
	}
	
	/**
	 * 
	 * @return the set of rows returned by the query SELECT G._id, title, date, content as summary FROM global G LEFT JOIN list L on G._id = L._id
	 */
	public Cursor getList(){
		Cursor c;
		c = db.rawQuery("SELECT G._id, title, date, content as summary FROM global G LEFT JOIN list L on G._id = L._id", null);
		c.moveToFirst();
		return c;
	}
	
	/**
	 * 
	 * @param title
	 * @return the row ID of the newly inserted row, or -1 if an error occurred 
	 */
	public long addList(String title){
		ContentValues cv = new ContentValues();
		cv.put("title", title);
		java.util.Date date = new java.util.Date();
		cv.put("date", date.getTime());
		long res = db.insertOrThrow("global", null, cv);
		return res;
	}
	
	/**
	 * 
	 * @param _id
	 * @return the number of rows affected, 0 otherwise. 
	 */
	public int deleteList(long _id){
		int res = db.delete("global", "_id="+_id, null);
		return res;
	}
	
	/**
	 * 	
	 * @param _id
	 * @return the title
	 */
	public String getTitle(long _id){
		Cursor c;
		c = db.query("global", new String[]{"title"}, "_id="+_id, null, null, null, null);
		c.moveToFirst();
		return c.getString(0);
	}
	
	/**
	 * 
	 * @param _id
	 * @param title
	 * @return the number of rows affected 
	 */
	public int setTitle(long _id, String title){
		ContentValues cv = new ContentValues();
		cv.put("title", title);
		return db.update("global", cv, "_id="+_id, null);
	}
	
	/**
	 * 
	 * @param _id
	 * @return the number of milliseconds since Jan. 1, 1970. 
	 */
	public long getDate(long _id){
		Cursor c;
		c = db.query("global", new String[]{"date"}, "_id="+_id, null, null, null, null);
		c.moveToFirst();
		return c.getLong(0);
	}
	
	/**
	 * 
	 * @param _id
	 * @return Returns true if text content exists (or is null or is an empty string). False otherwise.
	 */
	public boolean existsText(long _id){
		Cursor c = db.query("list", new String[]{"content"}, "_id="+_id, null, null, null, null);
		c.moveToFirst();
		return (c.getCount()>0);
	}
	
	/**
	 *
	 * @param _id
	 * @return the text content if exists and it is not NULL and not an empty string. An empty string otherwise.
	 */
	public String getText(long _id){
		Cursor c;
		c = db.query("list", new String[]{"content"}, "_id="+_id, null, null, null, null);
		c.moveToFirst();
		if(c.getCount()==0 || c.getString(0)==null || c.getString(0).equals(""))
			return "";
		else return c.getString(0);
	}
	
	/**
	 * Adds a new text content to the specified list or update the value of the existing text content.
	 * @param _id the rowid of the list
	 * @param html the html text content to set
	 * @see android.text.Html
	 * @return the row ID of the newly inserted row or the number of rows affected, or -1 if an error occurred
	 */
	public long setText(long _id, String html){
		ContentValues cv = new ContentValues();
		cv.put("content", html);
		if(existsText(_id))
			return db.update("list", cv, "_id="+_id, null);
		else
			return addText(_id, html);
	}
	
	
	/**
	 * 
	 * @param _id
	 * @param content
	 * @return the row ID of the newly inserted row, or -1 if an error occurred 
	 */
	public long addText(long _id, String content){
		ContentValues cv  = new ContentValues();
		cv.put("_id", _id);
		cv.put("content", content);
		return db.insertOrThrow("list", null, cv);
	}
	
	/**
	 * Deletes the entire row with the specified id
	 * @param _id
	 * @return the number of rows affected, 0 otherwise. 
	 */
	public int deleteText(long _id){
		int res = db.delete("list", "_id="+_id, null);
		return res;
	}
	
	
	/**
	 * 
	 * @param _id
	 * @param uri
	 * @return the row ID of the newly inserted row, or -1 if an error occurred 
	 */
	public long addImage(long _id, Uri uri){
		ContentValues cv = new ContentValues();
		cv.put("_id", (int)_id);
		cv.put("uri", uri.toString());
		return db.insert("images", null, cv);
	}
	
	/**
	 * 
	 * @param _id
	 * @param uri
	 * @return the number of rows affected, 0 otherwise
	 */
	public int deleteImage(long _id, Uri uri){
		return db.delete("images", "_id="+_id+" and uri='"+uri.toString()+"'", null);
	}
	
	/**
	 * 
	 * @param the rowid of the list for search on.
	 * @param a String rapresents the Uri of the image
	 * @return True if the specified image exists. False otherwise.
	 */
	public boolean existsImage(long _id, Uri uri){
		Cursor c = db.query("images", new String[]{"_id"}, "_id="+_id+" and uri='"+uri.toString()+"'", null, null, null, null);
		c.moveToFirst();
		return (c.getCount()>0);
	}
	
	/**
	 * Query for all the Uri in the table list with the rowid specified.
	 * @param _id
	 * @return A List of Uri contains all the Uri associated with the list specified.
	 */
	public List<Uri> getImages(long _id, Context ctx){
		Cursor c = db.query("images", new String[]{"uri"}, "_id="+_id, null, null, null, null);
		c.moveToFirst();
		if(c.getCount()==0)
			return new ArrayList<Uri>(0);
		else{
			List<Uri> l = new ArrayList<Uri>(0);
			for(int i=0;i<c.getCount();i++){
				c.moveToPosition(i);
				Cursor j = Media.query(ctx.getContentResolver(), Uri.parse(c.getString(0)), new String[]{"_data"}, null, null, null);
				j.moveToFirst();
				if(j.getCount()>0)
					l.add(Uri.parse(c.getString(0)));
				else
					deleteImage(_id, Uri.parse(c.getString(0)));
			}
			return l;
		}
	}
	
	/**
	 * 
	 * @param _id
	 * @param uri
	 * @return the row ID of the newly inserted row, or -1 if an error occurred 
	 */
	public long addVoicerec(long _id, Uri uri){
		ContentValues cv = new ContentValues();
		cv.put("_id", _id);
		cv.put("uri", uri.toString());
		return db.insert("voicerec", null, cv);
	}
	
	/**
	 * 
	 * @param _id
	 * @param uri
	 * @return the number of rows affected, 0 otherwise
	 */
	public int deleteVoicerec(long _id, Uri uri){
		return db.delete("voicerec", "_id="+_id+" and uri='"+uri.toString()+"'", null);
	}
	
	/**
	 * 
	 * @param the rowid of the list for search on.
	 * @param a String rapresents the Uri of the image
	 * @return True if the specified image exists. False otherwise.
	 */
	public boolean existsVoicerec(long _id, Uri uri){
		Cursor c = db.query("voicerec", new String[]{"_id"}, "_id="+_id+" and uri='"+uri.toString()+"'", null, null, null, null);
		c.moveToFirst();
		return (c.getCount()>0);
	}
	
	/**
	 * 
	 * @param _id
	 * @param olduri
	 * @param newuri
	 * @return the number of rows affected 
	 */
	public long updateVoicerec(long _id, Uri olduri, Uri newuri){
		ContentValues cv = new ContentValues();
		cv.put("uri", newuri.toString());
		return db.update("voicerec", cv, "_id="+_id+" and uri="+olduri.toString(), null);
	}
	
	/**
	 * Query for all the Uri in the table list with the rowid specified.
	 * @param _id
	 * @return A List of Uri contains all the Uri associated with the list specified.
	 */
	public List<Uri> getVoiceRecords(long _id, Context ctx){
		Cursor c = db.query("voicerec", new String[]{"uri"}, "_id="+_id, null, null, null, null);
		c.moveToFirst();
		if(c.getCount()==0)
			return new ArrayList<Uri>(0);
		else{
			List<Uri> l = new ArrayList<Uri>(0);
			for(int i=0;i<c.getCount();i++){
				c.moveToPosition(i);
				File f = new File(c.getString(0));
				if(f.exists())
					l.add(Uri.parse(c.getString(0)));
				else
					deleteVoicerec(_id, Uri.parse(c.getString(0)));	//TODO
			}
			return l;
		}
	}
	
	
	public boolean existsLocation(long _id, Location loc){
		Cursor c = db.query("locations", new String[]{"_id"}, "_id="+_id+" and loc='"+loc.getLatitude()+" "+loc.getLongitude()+"'", null, null, null, null);
		c.moveToFirst();
		return (c.getCount()>0);
	}
	
	/**
	 * 
	 * @param _id
	 * @param loc
	 * @return the row ID of the newly inserted row, or -1 if an error occurred 
	 */
	public long addLocation(long _id, Location loc){
		ContentValues cv = new ContentValues();
		cv.put("_id", (int)_id);
		cv.put("loc", ""+loc.getLatitude()+" "+loc.getLongitude());
		return db.insert("locations", null, cv);
	}
	
	/**
	 * 
	 * @param _id
	 * @param loc
	 * @return the number of rows affected, 0 otherwise
	 */
	public int deleteLocation(long _id, Location loc){
		return db.delete("locations", "_id="+_id+" and loc='"+loc.getLatitude()+" "+loc.getLongitude()+"'", null);
	}
	
	/**
	 * 
	 * @param _id
	 * @param oldloc
	 * @param newloc
	 * @return the number of rows affected 
	 */
	public long updateLocation(long _id, Location oldloc, Location newloc){
		ContentValues cv = new ContentValues();
		cv.put("loc", newloc.getLatitude()+" "+newloc.getLongitude());
		return db.update("locations", cv, "_id="+_id+" and loc="+oldloc.getLatitude()+" "+oldloc.getLongitude(), null);
	}
	
	
	public List<Location> getLocations(long _id){
		Cursor c = db.query("locations", new String[]{"loc"}, "_id="+_id, null, null, null, null);
		c.moveToFirst();
		if(c.getCount()==0){
			return new ArrayList<Location>(0);
		}
		else{
			List<Location> l = new ArrayList<Location>(0);
			for(int i=0;i<c.getCount();i++){
				c.moveToPosition(i);
				Location loc = new Location("");
				String[] split = c.getString(0).split(" ");
				loc.setLatitude(Double.valueOf(split[0]));
				loc.setLongitude(Double.valueOf(split[1]));
				l.add(loc);
			}
			return l;
		}
	}
	
	
	public void getAllContent(long _id, Note mNote, Context ctx){
		mNote.setTitle(getTitle(_id));
		mNote.setDate(new Date(getDate(_id)));
		
		String text = getText(_id);	// text or an empty string
		mNote.setText(Html.fromHtml(text));
		mNote.setImgList(getImages(_id, ctx));	// check if empty
		mNote.setLocationsList(getLocations(_id));	// check if empty
		mNote.setVoicerecList(getVoiceRecords(_id, ctx));
	}
	

	
	public long cloneListAndContent(long oldid){
		long newid = addList(getTitle(oldid));
		if(newid==-1){
			Log.w("cloneListAndContent", "errore durante la clonazione della lista "+oldid+" in addList()");
			return -1;
		}
		Log.i("cloneListAndContent", "List "+newid+" created");
		if(existsText(oldid)){
			addText(newid, getText(oldid));
			Log.i("cloneListAndContent", "text added to list "+newid);
		}
		Cursor c = db.query("images", new String[]{"uri"}, "_id="+oldid, null, null, null, null);
		c.moveToFirst();
		for(int i=0;i<c.getCount();i++){
			addImage(newid, Uri.parse(c.getString(0)));
			Log.i("cloneListAndContent", "image added to list "+newid);
		}
		c = db.query("locations", new String[]{"loc"}, "_id="+oldid, null, null, null, null);
		c.moveToFirst();
		for(int i=0;i<c.getCount();i++){
			Location loc = new Location("");
			String[] split = c.getString(0).split(" ");
			loc.setLatitude(Double.valueOf(split[0]));
			loc.setLongitude(Double.valueOf(split[1]));
			addLocation(newid, loc);
			Log.i("cloneListAndContent", "location added to list "+newid);
		}
		Log.i("cloneListAndContent", "cloning list "+oldid+" finished");
		return newid;
	}
	
}
