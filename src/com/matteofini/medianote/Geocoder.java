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


public class Geocoder {
	
	public static String doGeocoding(double lat, double longt, String xpath){
		/*
		Thread t_gdata = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					AndroidHttpClient cli = AndroidHttpClient.newInstance("");
					HttpResponse response = cli.execute(new HttpGet(URI.create("http://maps.googleapis.com/maps/api/geocode/xml?latlng="+loc.getLatitude()+","+loc.getLongitude()+"&sensor=true")));
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder db = dbf.newDocumentBuilder();
					Document doc = db.parse(response.getEntity().getContent());
					XPath xp = XPathFactory.newInstance().newXPath();
					final String address = xp.evaluate("\\formatted_address", doc);
					
					System.out.print(address);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							
						}
					});
					cli.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		*/
		//t_gdata.start();
		return xpath;
	}

}
