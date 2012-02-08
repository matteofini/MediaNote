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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class TextEditor extends Activity {
	
	private EditText mEditContent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		RelativeLayout ll = (RelativeLayout) getLayoutInflater().inflate(R.layout.texteditor, null);
		mEditContent = (EditText) ll.findViewById(R.id.edittext);
		String html = getIntent().getExtras().getString("text");
		if(html!=null){
			mEditContent.setText(Html.fromHtml(html));
		}
		
		ll.findViewById(R.id.button_bold).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				applyStyleBold();//applyStyle(STYLE_BOLD);
			}
		});
    	
    	ll.findViewById(R.id.button_italic).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//applyStyle(STYLE_ITALIC);
				applyStyleItalic();
			}
		});
    	
    	ll.findViewById(R.id.button_underline).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//applyStyle(STYLE_UNDERLINE);
				applyStyleUnderline();
			}
		});
    	
    	ll.findViewById(R.id.button_strike).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//applyStyle(STYLE_STRIKE);
				applyStyleStrike();
			}
		});
    	
    	ll.findViewById(R.id.button_link).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				applyStyleLink();
			}
		});
    	
    	ll.findViewById(R.id.button_ok).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				EditText edit = (EditText) getWindow().getDecorView().findViewById(R.id.edittext);
				String html = Html.toHtml(edit.getText());
				
				Intent i = new Intent();
				i.putExtra("id", getIntent().getExtras().getLong("id"));
				i.putExtra("text", html);
				setResult(EditList.ACTIVITY_RESULT_TEXT, i);
			}
		});
    	
    	setContentView(ll);
    	Toast.makeText(getApplicationContext(), R.string.toast_texteditor_activity, Toast.LENGTH_LONG).show();
	}
	
	public void applyStyleBold(){
		if(mEditContent==null) return;
		int start = mEditContent.getSelectionStart();
		int end = mEditContent.getSelectionEnd();
		if(start==end){
			Toast.makeText(TextEditor.this, "Nessun testo selezionato", Toast.LENGTH_LONG).show();
		}
		else{
			int removed=0;
			StyleSpan[] spans = mEditContent.getText().getSpans(start, end, StyleSpan.class);
			if(spans.length!=0){
				for(int i=0;i<spans.length;i++){
					if(spans[i].getStyle()==Typeface.BOLD){
						mEditContent.getText().removeSpan(spans[i]);
						System.out.println("\t removed span start from "+mEditContent.getText().getSpanStart(spans[0]));
						removed++;
					}
				}
				if(removed==0){
					mEditContent.getText().setSpan(new StyleSpan(Typeface.BOLD), start, end, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
					System.out.println("\t attached span from "+start+" to "+end);
				}
			}
			else{
				mEditContent.getText().setSpan(new StyleSpan(Typeface.BOLD), start, end, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
				System.out.println("\t attached span from "+start+" to "+end);
			}
			System.out.println(Html.toHtml(mEditContent.getText()));
		}
	}
	
	public void applyStyleItalic(){
		if(mEditContent==null) return;
		int start = mEditContent.getSelectionStart();
		int end = mEditContent.getSelectionEnd();
		if(start==end){
			Toast.makeText(TextEditor.this, "Nessun testo selezionato", Toast.LENGTH_LONG).show();
		}
		else{
			int removed=0;
			StyleSpan[] spans = mEditContent.getText().getSpans(start, end, StyleSpan.class);
			if(spans.length!=0){
				for(int i=0;i<spans.length;i++){
					if(spans[i].getStyle()==Typeface.ITALIC){
						mEditContent.getText().removeSpan(spans[i]);
						System.out.println("\t removed span start from "+mEditContent.getText().getSpanStart(spans[0]));
						removed++;
					}
				}
				if(removed==0){
					mEditContent.getText().setSpan(new StyleSpan(Typeface.ITALIC), start, end, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
					System.out.println("\t attached span from "+start+" to "+end);
				}
			}
			else{
				mEditContent.getText().setSpan(new StyleSpan(Typeface.ITALIC), start, end, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
				System.out.println("\t attached span from "+start+" to "+end);
			}
			System.out.println(Html.toHtml(mEditContent.getText()));
		}
	}

	public void applyStyleUnderline(){
		if(mEditContent==null) return;
		int start = mEditContent.getSelectionStart();
		int end = mEditContent.getSelectionEnd();
		if(start==end){
			Toast.makeText(TextEditor.this, "Nessun testo selezionato", Toast.LENGTH_LONG).show();
		}
		else{
			UnderlineSpan[] spans = mEditContent.getText().getSpans(start, end, UnderlineSpan.class);
			if(spans.length!=0){
				for(int i=0;i<spans.length;i++){
					mEditContent.getText().removeSpan(spans[i]);
					System.out.println("\t removed span start from "+mEditContent.getText().getSpanStart(spans[0]));
				}
			}
			else{
				mEditContent.getText().setSpan(new UnderlineSpan(), start, end, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
				System.out.println("\t attached span from "+start+" to "+end);
			}
			System.out.println(Html.toHtml(mEditContent.getText()));
		}
	}

	public void applyStyleStrike(){
		if(mEditContent==null) return;
		int start = mEditContent.getSelectionStart();
		int end = mEditContent.getSelectionEnd();
		if(start==end){
			Toast.makeText(TextEditor.this, "Nessun testo selezionato", Toast.LENGTH_LONG).show();
		}
		else{
			StrikethroughSpan[] spans = mEditContent.getText().getSpans(start, end, StrikethroughSpan.class);
			if(spans.length!=0){
				for(int i=0;i<spans.length;i++){
					mEditContent.getText().removeSpan(spans[i]);
					System.out.println("\t removed span start from "+mEditContent.getText().getSpanStart(spans[0]));
				}
			}
			else{
				mEditContent.getText().setSpan(new StrikethroughSpan(), start, end, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
				System.out.println("\t attached span from "+start+" to "+end);
			}
			System.out.println(Html.toHtml(mEditContent.getText()));
		}
	}

	public void applyStyleLink(){
		if(mEditContent==null) return;
		final int start = mEditContent.getSelectionStart();
		final int end = mEditContent.getSelectionEnd();
		URLSpan[] spans = mEditContent.getText().getSpans(start, end, URLSpan.class);
		if(spans.length!=0){
			for(int i=0;i<spans.length;i++){
				mEditContent.getText().removeSpan(spans[i]);
				System.out.println("\t removed span start from "+mEditContent.getText().getSpanStart(spans[0]));
			}
		}
		else{
			final View dialog_layout = getLayoutInflater().inflate(R.layout.dialog_link, null);
			AlertDialog dialog = new AlertDialog.Builder(TextEditor.this)
		    .setTitle("Inserisci l'indirizzo del link")
		    .setView(dialog_layout)
		    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		            EditText edit = (EditText) dialog_layout.findViewById(R.id.edit_link);
		            setLink(edit.getText(), start, end);				            
		        }

				private void setLink(Editable url, int start, int end) {
					//View ll = getLayoutInflater().inflate(R.layout.edit,null);
					URLSpan urlspan = new URLSpan(url.toString());
					if(start==end){
						mEditContent.getText().insert(mEditContent.getSelectionEnd(), Html.fromHtml("<a href='"+url+"'>"+url+"</a>"));
					}
					else
						mEditContent.getText().setSpan(urlspan, mEditContent.getSelectionStart(), mEditContent.getSelectionEnd(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
					System.out.println("\t attached span from "+start+" to "+end);
					
				}
		    })
		    .setNegativeButton("Cancel", null).create();
			dialog.show();
		}
		System.out.println(Html.toHtml(mEditContent.getText()));
	}; 
}
