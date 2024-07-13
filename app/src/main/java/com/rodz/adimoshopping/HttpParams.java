package com.rodz.adimoshopping;

import android.content.*;
import java.util.*;

public class HttpParams
{
	Context ctx;
	String url;
	Map<String, Object> params;
	
	public HttpParams(Context ctz, String urlz, Map<String, Object> hparams){
		ctx = ctz;
		url = urlz;
		params = hparams;
	}
	public void onResponse(String text){
		
	}
}