package com.coolweather.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.app.R;
import com.coolweather.app.service.AutoUpdateService;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

public class WeatherActivity extends Activity implements OnClickListener{
	public static final int Red=1;
	public static final int Blue=2;
	private LinearLayout weatherInfoLayout;
	private TextView cityNameText;
	private TextView publishText;
	private TextView weatherDespText;
	private TextView temp1Text;
	private TextView temp2Text;
	private TextView currentDateText;
	private Button switchButton;
	private Button refreshWeather;
	private RelativeLayout colorLayout;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		weatherInfoLayout=(LinearLayout)findViewById(R.id.weather_info_layout);
		cityNameText=(TextView)findViewById(R.id.city_name);
		publishText=(TextView)findViewById(R.id.publish_text);
		weatherDespText=(TextView)findViewById(R.id.weather_desp);
		temp1Text=(TextView)findViewById(R.id.temp1);
		temp2Text=(TextView)findViewById(R.id.temp2);
		currentDateText=(TextView)findViewById(R.id.current_date);
		switchButton=(Button)findViewById(R.id.switch_city);
		refreshWeather=(Button)findViewById(R.id.refresh_weather);
		colorLayout=(RelativeLayout)findViewById(R.id.color);
		switchButton.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
		String countyCode=getIntent().getStringExtra("county_code");
		if (!TextUtils.isEmpty(countyCode)) {
			publishText.setText("同步中。。。");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		}else {
			showWeather();
		}
		SharedPreferences sp=getSharedPreferences("color", MODE_PRIVATE);
		if (sp==null) {
			SharedPreferences.Editor editor=getSharedPreferences("color", MODE_PRIVATE).edit();
			editor.putInt("mycolor", 0xFFFFFF00);
			colorLayout.setBackgroundColor(0xFFFFFF00);
			Log.d("color", "sp is null");
		}else {
			colorLayout.setBackgroundColor(sp.getInt("mycolor", 0xFFFFFF00));
			Log.d("color", "sp is not null");
		}
//		weatherInfoLayout.setBackgroundColor(0x000000);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		if (item.getItemId()==Red) {
			SharedPreferences.Editor editor=getSharedPreferences("color", MODE_PRIVATE).edit();
			editor.putInt("mycolor", 0xFFFF0000);
			editor.commit();
			Toast.makeText(this, "choose red", Toast.LENGTH_SHORT).show();
			colorLayout.setBackgroundColor(0xFFFF0000);
		}else if (item.getItemId()==Blue) {
			SharedPreferences.Editor editor=getSharedPreferences("color", MODE_PRIVATE).edit();
			editor.putInt("mycolor", 0xFF0000FF);
			editor.commit();
			Toast.makeText(this, "choose blue", Toast.LENGTH_SHORT).show();
			colorLayout.setBackgroundColor(0xFF0000FF);
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add(0, Red, 1, "Red Color");
		menu.add(0, Blue, 2, "Blue Color");
		return super.onCreateOptionsMenu(menu);
	}
	private void queryWeatherCode(String countyCode) {
		String address="http://www.weather.com.cn/data/list3/city"+countyCode+".xml";
		queryFromServer(address,"countyCode");
	}
	private void queryWeatherInfo(String weatherCode) {
		String address="http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
		queryFromServer(address,"weatherCode");
	}
	private void queryFromServer(final String address,final String type) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				if ("countyCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {
						String[] array=response.split("\\|");
						if (array!=null && array.length==2) {
							String weatherCode=array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				}else if ("weatherCode".equals(type)) {
					Utility.handleWeatherResponse(WeatherActivity.this, response);
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							showWeather();
						}
					});
					
				}
				
			}
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						publishText.setText("同步是比较");
					}
				});
			}
		});
	}
	public void showWeather() {
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText(prefs.getString("city_name", ""));
		temp1Text.setText(prefs.getString("temp1", ""));
		temp2Text.setText(prefs.getString("temp2", ""));
		weatherDespText.setText(prefs.getString("weather_desp", ""));
		publishText.setText("今天"+prefs.getString("publish_time","")+"发布");
		currentDateText.setText(prefs.getString("current_date",""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		Intent intent=new Intent(this, AutoUpdateService.class);
		startService(intent);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.switch_city:
			Intent intent=new Intent(this,ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh_weather:	
			publishText.setText("同步中。。。");
			SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode=prefs.getString("weather_code", "");
			if (!TextUtils.isEmpty(weatherCode)) {
				queryWeatherInfo(weatherCode);
			}
			break;
		default:
			break;
		}
	}

}
