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

import java.util.Calendar;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ZoomControls;

public class ShowList extends MediaNoteAbs{
	private Note mNote;
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		return mNote;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent i = getIntent();
		final long rowid = i.getExtras().getLong("id");
		Log.i("ShowList", "Show list with _id "+rowid);
		final MediaNoteDB db = new MediaNoteDB(ShowList.this);
        db.open();
        mNote = (Note)getLastNonConfigurationInstance();
        if(mNote==null){
        	mNote = new Note();
        	db.getAllContent(rowid, mNote, getApplicationContext());
		}
        if(mNote.isEmpty()){	
        	Intent i1 = new Intent();
        	i1.putExtra("id", rowid);
        	i1.setComponent(new ComponentName("com.matteofini.medianote", "com.matteofini.medianote.EditList"));
        	db.close();
        	startActivity(i1);
        	finish();      	
        }
        else{
        	final View rl = getLayoutInflater().inflate(R.layout.showlist, null);
        	TextView title = (TextView) rl.findViewById(R.id.title);
        	TextView date = (TextView) rl.findViewById(R.id.date);
        	title.setText(mNote.getTitle());
        	CharSequence str = DateFormat.format("MM/dd/yy h:mmaa", mNote.getDate());
        	date.setText(str);
        	
        	final LinearLayout container = (LinearLayout) rl.findViewById(R.id.container);
        	
        	if(!mNote.isTextEmpty()){
        		ViewGroup text = layout_add_text(container, mNote.getText());
            	container.addView(text);
        	}
        	for (Location loc : mNote.getLocationsList()) {
        		ViewGroup location = layout_add_location(container, loc, rowid);
        		container.addView(location);
        	}
        	for(Uri uri : mNote.getImgList()){
        		View image = layout_add_image(container, uri, rowid);
        		container.addView(image);
        	}
        	for(Uri uri : mNote.getVoicerecList()){
        		ViewGroup voice = layout_add_voicerec(container, uri);
        		container.addView(voice);
        	}

        	View b_cal = rl.findViewById(R.id.button_calendar);
        	b_cal.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Bundle arg = new Bundle();
					arg.putLong("id", rowid);
					showDialog(0, arg);
				}
			});
        	db.close();
        	setContentView(rl);
        	rl.findViewById(R.id.button_edit2).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					finish();
					onDestroy();
					Intent i = new Intent();
					i.setComponent(new ComponentName(getApplicationContext(), EditList.class));
					i.putExtra("id", rowid);
					startActivity(i);
				}
			});
        	
        	ZoomControls zoom = (ZoomControls) rl.findViewById(R.id.zoomControls1);
        	zoom.setOnZoomInClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if(rl.findViewById(R.id.text)!=null){
						TextView text = (TextView) rl.findViewById(R.id.text);
						text.setTextSize(text.getTextSize()+1);
					}
				}
			});
        	zoom.setOnZoomOutClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if(rl.findViewById(R.id.text)!=null){
						TextView text = (TextView) rl.findViewById(R.id.text);
						text.setTextSize(text.getTextSize()-1);
					}
				}
			});
        }
	}
	
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode==EditList.ACTIVITY_RESULT_TEXT){
			LinearLayout ll = (LinearLayout) getWindow().findViewById(R.id.container);
			CheckedTextView text = (CheckedTextView) ll.findViewById(R.id.text);
			text.setText(Html.fromHtml(data.getExtras().getString("text")));
			MediaNoteDB db = new MediaNoteDB(getApplicationContext());
			db.open();
			long res = db.setText(getIntent().getExtras().getLong("id"), data.getExtras().getString("text"));
			if(res>0) Log.i("ShowList", "text updated");
			db.close();
		}
	}
	

	private static DatePickerDialog.OnDateSetListener setdate_callback;
	private static TimePickerDialog.OnTimeSetListener settime_callback;
	
	@Override
	protected Dialog onCreateDialog(int id, final Bundle args) {
		if(id==0){
			AlertDialog.Builder adb = new AlertDialog.Builder(this);
			AlertDialog ad = adb.create();
			View v = getLayoutInflater().inflate(R.layout.timedatedialog, null);
			ad.setView(v);
			ad.setTitle("Imposta data e ora dell'evento");
			
			final TextView date = (TextView) v.findViewById(R.id.label_setdate);
			final TextView time = (TextView) v.findViewById(R.id.label_settime);
			final Calendar now = Calendar.getInstance();
			date.setText(DateFormat.format("MM/dd/yyyy", now.getTime()));
			time.setText(DateFormat.format("h:mmaa", now.getTime()));
			
			View b_setdate = v.findViewById(R.id.button_setdate);
			View b_settime = v.findViewById(R.id.button_settime);
			View b_ok = v.findViewById(R.id.button_setdatetimeOK);
			
			setdate_callback = new DatePickerDialog.OnDateSetListener() {
				@Override
				public void onDateSet(DatePicker view, int year, int monthOfYear,
						int dayOfMonth) {
					now.set(year, monthOfYear, dayOfMonth);
					date.setText(DateFormat.format("dd/MM/yyyy", now.getTime()));
				}
			};
			
			settime_callback = new TimePickerDialog.OnTimeSetListener() {
				@Override
				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
					now.set(Calendar.HOUR_OF_DAY, hourOfDay);
					now.set(Calendar.MINUTE, minute);
					time.setText(DateFormat.format("h:mmaa", now.getTime()));
				}
			};
			
			b_setdate.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					showDialog(1);
				}
			});
			b_settime.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					showDialog(2);
				}
			});
			b_ok.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent();
					i.putExtra("beginTime", now.getTimeInMillis());
					i.putExtra("title", mNote.getTitle());
					i.putExtra("description", mNote.getText());
					i.setComponent(new ComponentName("com.android.calendar", "com.android.calendar.EditEvent"));
					startActivity(i);	
				}
			});
			return ad;
		}
		else if(id==1){
			Calendar c = Calendar.getInstance();
			DatePickerDialog dp = new DatePickerDialog(this, setdate_callback, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
			return dp;
		}
		else if(id==2){
			Calendar c = Calendar.getInstance();
			TimePickerDialog tp = new TimePickerDialog(this, settime_callback, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
			return tp;
		}
		return null;
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
