package swindroid.suntime.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import swindroid.suntime.Cities;
import swindroid.suntime.R;
import swindroid.suntime.calc.AstronomicalCalendar;
import swindroid.suntime.calc.GeoLocation;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity 
{
	List<String> locations = new ArrayList<String>();
	private Adapter mAdapter;

	String city_var = "Melbourne";
	Double lat_var = -37.50;
	Double long_var = 145.01;
	String tz_var = "Australia/Melbourne";
	TextView city_text;
	String locationTV;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		convertStreamToString(getResources().openRawResource(R.raw.au_locations));

		mAdapter = new Adapter();
		ListView listNote = (ListView) findViewById(R.id.listView);
		listNote.setAdapter(mAdapter);
		city_text = (TextView) findViewById(R.id.locationTV);

		for (int i = 0; i < locations.size(); i++) {
			String original = locations.get(i);
			String[] separated = original.split(",");
			Cities city = new Cities();
			String input = String.valueOf(i) + ". " + separated[0];
			city.setCity(input);
			createNewNote(city);
		}

		listNote.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int whichItem, long id) {
				DataPopulate(whichItem);
				//Toast.makeText(getApplicationContext(), city_var, Toast.LENGTH_SHORT).show();
				locationTV = city_var + ", AU";
				city_text.setText(locationTV);
				initializeUI();
			}
		});

        initializeUI();
    }

	public void DataPopulate(int num) {
		String original = locations.get(num);
		String[] separated = original.split(",");
		city_var = separated[0];
		lat_var = Double.valueOf(separated[1]);
		long_var = Double.valueOf(separated[2]);
		tz_var = separated[3];
	}

	public void createNewNote(Cities n) {
		mAdapter.addNote(n);
	}

	public void convertStreamToString(InputStream is) {
		String line;
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		try {
			while ((line = reader.readLine()) != null) {
				locations.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void initializeUI()
	{
		DatePicker dp = (DatePicker) findViewById(R.id.datePicker);
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		dp.init(year,month,day,dateChangeHandler); // setup initial values and reg. handler
		updateTime(year, month, day);
	}
    
	private void updateTime(int year, int monthOfYear, int dayOfMonth)
	{
		TimeZone tz = TimeZone.getTimeZone(tz_var);
		GeoLocation geolocation = new GeoLocation(city_var, lat_var, long_var, tz);
		AstronomicalCalendar ac = new AstronomicalCalendar(geolocation);
		ac.getCalendar().set(year, monthOfYear, dayOfMonth);
		Date srise = ac.getSunrise();
		Date sset = ac.getSunset();
		
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		
		TextView sunriseTV = (TextView) findViewById(R.id.sunriseTimeTV);
		TextView sunsetTV = (TextView) findViewById(R.id.sunsetTimeTV);
		Log.d("SUNRISE Unformatted", srise+"");
		
		sunriseTV.setText(sdf.format(srise));
		sunsetTV.setText(sdf.format(sset));		
	}
	
	OnDateChangedListener dateChangeHandler = new OnDateChangedListener()
	{
		public void onDateChanged(DatePicker dp, int year, int monthOfYear, int dayOfMonth)
		{
			updateTime(year, monthOfYear, dayOfMonth);
		}	
	};

	public class Adapter extends BaseAdapter {

		List<Cities> cityList = new ArrayList<Cities>();

		@Override
		public int getCount() {
			return cityList.size();
		}

		@Override
		public Cities getItem(int whichItem) {
			return cityList.get(whichItem);
		}

		@Override
		public long getItemId(int whichItem) {
			return whichItem;
		}

		@Override
		public View getView(int whichItem, View view, ViewGroup viewGroup) {
			if (view == null) {
				LayoutInflater intflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = intflater.inflate(R.layout.listitem, viewGroup, false);
			}

			TextView txtCity = (TextView) view.findViewById(R.id.txtCity);
			Cities temp = cityList.get(whichItem);
			txtCity.setText(temp.getCity());
			return view;
		}

		public void addNote(Cities n) {
			cityList.add(n);
			notifyDataSetChanged();
		}
	}
}