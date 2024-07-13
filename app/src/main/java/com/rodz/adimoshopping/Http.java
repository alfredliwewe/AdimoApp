package com.rodz.adimoshopping;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import android.content.*;
import java.util.*;
import android.app.*;

public class Http
{
	Context ctx;
	HttpParams params;
	public Http(HttpParams paramz){
		params = paramz;
		ctx = paramz.ctx;
		this.doJob();
	}
	
	public void doJob(){
		Thread thread = new Thread(new Runnable(){
				public void run(){
					try{
						final String text = httpConn(params.url, "POST", params.params).trim();
						Activity act = (Activity)ctx;
						act.runOnUiThread(new Runnable(){
								public void run(){
									params.onResponse(text);
								}
							});
					}catch(Exception e){}
				}
			});
		thread.start();
		
	}

	public static void JSONPost(JSONPostParams params){
        Thread thread = new Thread(new Runnable(){
            public void run(){
                try{
                    final String text = httpConn2(params.url, "POST", params.json).trim();
                    Activity act = (Activity)params.ctx;
                    act.runOnUiThread(new Runnable(){
                        public void run(){
                            params.onResponse(text);
                        }
                    });
                }catch(Exception e){}
            }
        });
        thread.start();
    }
	
	public String httpConn(String urli, String method, Map<String, Object> params){
        URL url;
        try {
            url = new URL(urli);
            //creating formdata from a linkedhashmap
            StringBuilder postData = new StringBuilder();

            int i = 0;
            for(Map.Entry<String, Object> param:params.entrySet()){
                try {
                    String key = param.getKey();
                    String value = String.valueOf(param.getValue());

                    //appending the form data
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(key, "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(value, "UTF-8"));
                    i += 1;
                } catch (UnsupportedEncodingException ex) {
                    //Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);

            Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

            //get string output
            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0;)
                sb.append((char)c);
            String response = sb.toString();
            return response;
        } catch (UnsupportedEncodingException ex) {
            return ex.toString();
        } catch (IOException exx) {
            return exx.toString();
        }   
    }

    public static String httpConn2(String urli, String method, String json){
        URL url;
        try {
            url = new URL(urli);
            //creating formdata from a linkedhashmap
            StringBuilder postData = new StringBuilder();

            byte[] postDataBytes = json.getBytes("UTF-8");

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);

            Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

            //get string output
            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0;)
                sb.append((char)c);
            String response = sb.toString();
            return response;
        } catch (UnsupportedEncodingException ex) {
            return ex.toString();
        } catch (IOException exx) {
            return exx.toString();
        }
    }

    public static class JSONPostParams{
        Context ctx;
        String url;
        String json;

        public JSONPostParams(Context ctz, String urlz, String json){
            ctx = ctz;
            url = urlz;
            this.json = json;
        }
        public void onResponse(String text){

        }
    }
}