package martin.app.bitunion;

import android.os.Bundle;
import android.app.Activity;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class SettingsActivity extends Activity {
	
	private TextView titleTextView;
	private SeekBar titleSeekBar;
	private TextView contentTextView;
	private SeekBar contentSeekBar;
	private Switch nettypeSwitch;
	private int titletextsize;
	private int contenttextsize;
	private int RECOMMENDTITESIZE;
	private int RECOMMENDCONTENTSIZE;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		// Show the Up button in the action bar.
		setupActionBar();
		
		if (MainActivity.PIXDENSITY > DisplayMetrics.DENSITY_HIGH) {
			RECOMMENDTITESIZE = 14;
			RECOMMENDCONTENTSIZE = 14;
		} else {
			RECOMMENDTITESIZE = 12;
			RECOMMENDCONTENTSIZE = 12;
		}
		titletextsize = MainActivity.settings.titletextsize;
		contenttextsize = MainActivity.settings.contenttextsize;
		
		titleTextView = (TextView) findViewById(R.id.setting_titletextview);
		titleSeekBar = (SeekBar) findViewById(R.id.seekbar_titletextsize);
		contentTextView = (TextView) findViewById(R.id.setting_contenttextview);
		contentSeekBar = (SeekBar) findViewById(R.id.seekbar_contenttextsize);
		nettypeSwitch = (Switch) findViewById(R.id.netswitch);
		nettypeSwitch.setChecked(MainActivity.settings.mNetType == 1);
//		titleTextView.setText("标题文本大小" + titletextsize + "\t(推荐"+ RECOMMENDTITESIZE +")");
//		titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, titletextsize);
		titleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				titletextsize = progress + 10;
				titleTextView.setText("帖子标题文本大小" + titletextsize + "\t(推荐"+ RECOMMENDTITESIZE +")");
				titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, titletextsize);
			}
		});
		titleSeekBar.setProgress(titletextsize - 10);
//		contentTextView.setText("帖子内容文本大小" + contenttextsize + "\t(推荐"+ RECOMMENDCONTENTSIZE +")");
//		contentTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, contenttextsize);
		contentSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				contenttextsize = progress + 10;
				contentTextView.setText("帖子内容文本大小" + contenttextsize + "\t(推荐"+ RECOMMENDCONTENTSIZE +")");
				contentTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, contenttextsize);
			}
		});
		contentSeekBar.setProgress(contenttextsize - 10);
		nettypeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Log.i("SettingsActivity", "外网模式>>" + isChecked);
				MainActivity.settings.mNetType = isChecked ? 1 : 0;
			}
		});	
	}
	
	protected void onDestroy() {
		MainActivity.settings.titletextsize = titletextsize;
		MainActivity.settings.contenttextsize = contenttextsize;
		SharedPreferences config = getSharedPreferences("config", MODE_PRIVATE);
		SharedPreferences.Editor editor = config.edit();
		editor.putInt("titletextsize", titletextsize);
		editor.putInt("contenttextsize", contenttextsize);
		editor.commit();
		Log.i("SettingActivity", "Config Saved>>" + titletextsize + ">>" + contenttextsize);
		super.onDestroy();
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
