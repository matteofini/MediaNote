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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.location.Location;
import android.net.Uri;
import android.text.Html;
import android.text.Spanned;

public class Note {
	
	private String mTitle;
	private Spanned mText;
	private List<Uri> mImgURI;
	private List<Location> mLoc;
	private Date mDate;
	private List<Uri> mVoicerec;
	// voice recording?

	public Note() {
		mText=Html.fromHtml("");
		mTitle="";
		mImgURI=new ArrayList<Uri>(0);
		mLoc=new ArrayList<Location>(0);
		mVoicerec = new ArrayList<Uri>(0);
	}
	
	public Note(String title, Spanned text, List<Uri> images, List<Location> positions) {
		mTitle = title;
		mText = text;
		mImgURI = images;
		mLoc = positions;
	}

	public boolean isEmpty(){
		return (mText.toString().equals("") && mImgURI.size()==0 && mLoc.size()==0);
	}
	
	public boolean isTextEmpty(){
		return (mText.toString().equals(""));
	}

	public Spanned getText() {
		return mText;
	}

	public void setText(Spanned mText) {
		this.mText = mText;
	}

	/** images **/
	
	public void setImgList(List<Uri> l){
		mImgURI = l;
	}
	
	public List<Uri> getImgList() {
		return mImgURI;
	}

	public void addImg(Uri uri){
		mImgURI.add(uri);
	}
	
	public Uri removeImg(int index){
		 return mImgURI.remove(index);
	}
	
	public int getImgPosition(Uri uri){
		return mImgURI.indexOf(uri);
	}
	
	public Uri getImgAtPosition(int i){
		return mImgURI.get(i);
	}
	
	/** voicerec **/
	
	public void setVoicerecList(List<Uri> l){
		mVoicerec = l;
	}
	
	public List<Uri> getVoicerecList() {
		return mVoicerec;
	}

	public void addVoicerec(Uri uri){
		mVoicerec.add(uri);
	}
	
	public Uri removeVoicerec(int index){
		 return mVoicerec.remove(index);
	}
	
	public int getVoicerecPosition(Uri uri){
		return mVoicerec.indexOf(uri);
	}
	
	public Uri getVoicerecAtPosition(int i){
		return mVoicerec.get(i);
	}
	
	/** locations **/
	public void setLocationsList(List<Location> l){
		mLoc = l;
	}
	
	public List<Location> getLocationsList(){
		return mLoc;
	}
	
	public Location getLocationAtPosition(int index){
		return mLoc.get(index);
	}
	
	public double getLocLat(int index){
		Location loc = mLoc.get(index);
		return loc.getLatitude();
	}
	
	public double getLocLong(int index){
		Location loc = mLoc.get(index);
		return loc.getLongitude();
	}
	
	public void addLocation(Location loc){
		mLoc.add(loc);
	}
	
	public Location removeLocation(int index){
		return mLoc.remove(index);
	}
	
	public int getLocationPosition(Location loc){
		return mLoc.indexOf(loc);
	}
	/*
	public List<Location> getmLoc() {
		return mLoc;
	}

	public void setLoc(List<Location> mLoc) {
		this.mLoc = mLoc;
	}
	*/
	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String mTitle) {
		this.mTitle = mTitle;
	}

	public Date getDate() {
		return mDate;
	}

	public void setDate(Date mDate) {
		this.mDate = mDate;
	}
}
