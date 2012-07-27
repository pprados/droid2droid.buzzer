package org.droid2droid.apps.buzzer;


import static org.droid2droid.Droid2DroidManager.ACTION_CONNECT_ANDROID;
import static org.droid2droid.Droid2DroidManager.EXTRA_DISCOVER;
import static org.droid2droid.Droid2DroidManager.EXTRA_FLAGS;
import static org.droid2droid.Droid2DroidManager.EXTRA_THEME_ID;
import static org.droid2droid.Droid2DroidManager.EXTRA_UPDATE;
import static org.droid2droid.Droid2DroidManager.FLAG_ACCEPT_ANONYMOUS;
import static org.droid2droid.Droid2DroidManager.FLAG_PROPOSE_PAIRING;

import org.droid2droid.Droid2DroidManager;
import org.droid2droid.RemoteAndroidInfo;
import org.droid2droid.apps.buzzer.charts.ChartMulti_ABC;
import org.droid2droid.apps.buzzer.charts.Chart_AB;
import org.droid2droid.apps.buzzer.charts.Chart_ABC;
import org.droid2droid.apps.buzzer.charts.Chart_ABCD;
import org.droid2droid.apps.buzzer.charts.Chart_YN;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BuzzerActivity extends ListActivity
implements CreateNdefMessageCallback
{
	public static boolean		DEBUG			= true;
	public static boolean		USE_NFC			= true;
	public static boolean		USE_BUMP		= true;

	public static final int		YES_NO			= 0;

	public static final int		A_B				= 1;

	public static final int		A_B_C			= 2;

	public static final int		A_B_C_D			= 3;

	public static final int		Multi_A_B_C		= 4;

	public static final int		Multi_A_B_C_D	= 5;

	public static final String	REGISTER		= "org.droid2droid.apps.buzzer.REGISTER";

	private static final int 	REQUEST_CONNECT_CODE=1;
	
	private static final int	DIALOG_MARKET	= 1;

	private EditText			mEditText;

	private TextView			mDevices;

	private int				mTime;

	private final Handler		mHandler		= new Handler();

	private CheckBox			mDiscover;
	
	private NfcAdapter			mNfcAdapter;
	
	@Override
	@TargetApi(14)
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Intent market = Droid2DroidManager.getIntentForMarket(this);
		if (market != null)
		{
			finish();
			return;
		}
		setContentView(R.layout.main);
		mEditText = (EditText) findViewById(R.id.editText);
		mDevices = (TextView) findViewById(R.id.active_connection_number);
		mDiscover=(CheckBox)findViewById(R.id.discover);
		mDiscover.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener()
		{
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				if (isChecked)
					startService(new Intent(MultiConnectionService.ACTION_START_DISCOVER));
				else
					startService(new Intent(MultiConnectionService.ACTION_STOP_DISCOVER));
				
			}
		});
		setListAdapter(new ArrayAdapter<String>(this, R.layout.choicerow, R.id.label,
				getResources().getStringArray(R.array.choice_array)));
		int size=0;
		if (MultiConnectionService.sMe!=null)
			size=MultiConnectionService.sMe.getSize();
		mDevices.setText(" " + size);
		startService(new Intent(MultiConnectionService.ACTION_CONNECT));
		if (VERSION.SDK_INT>VERSION_CODES.ICE_CREAM_SANDWICH)
		{
			mNfcAdapter=NfcAdapter.getDefaultAdapter(this);
			mNfcAdapter=NfcAdapter.getDefaultAdapter(this);
			mNfcAdapter.setNdefPushMessageCallback(this, this);
		}
	}

	@Override
	public void onNewIntent(Intent intent)
	{
		// onResume gets called after this to handle the intent
		setIntent(intent);
	}
	@Override
	protected void onResume()
	{
		super.onResume();
		registerReceiver(mReceiver, new IntentFilter(REGISTER));
		Intent market = Droid2DroidManager.getIntentForMarket(this);
		if (market != null)
		{

			showDialog(DIALOG_MARKET);
		}
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		unregisterReceiver(mReceiver);
	}
	
	@Override
	public Dialog onCreateDialog(int id)
	{
		switch (id)
		{
			case DIALOG_MARKET:
				return new AlertDialog.Builder(this)
						.setMessage("Install the application Droid2Droid ?") // TODO: NLS
						.setPositiveButton("Install", new DialogInterface.OnClickListener()
						{

							@Override
							public void onClick(DialogInterface paramDialogInterface, int paramInt)
							{
								startActivity(Droid2DroidManager
										.getIntentForMarket(BuzzerActivity.this));
								finish();
							}
						}).setNegativeButton("Cancel", new DialogInterface.OnClickListener()
						{

							@Override
							public void onClick(DialogInterface paramDialogInterface, int paramInt)
							{
								finish();
							}
						}).create();
			default:
				return null;
		}
	}

	private void postStartActivity(final Intent intent)
	{
		mHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				startActivity(intent);
			}
		});
	}

	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode==REQUEST_CONNECT_CODE && resultCode==Activity.RESULT_OK)
		{
			RemoteAndroidInfo info=(RemoteAndroidInfo)data.getParcelableExtra(EXTRA_DISCOVER);
			startService(new Intent(MultiConnectionService.ACTION_ADD_DEVICE)
				.putExtra(EXTRA_DISCOVER,info)
				.putExtra(EXTRA_UPDATE, data.getBooleanExtra(EXTRA_UPDATE, false)));
		}
	}
	
	@Override
	protected void onListItemClick(ListView parent, View view, final int position, long id)
	{
		super.onListItemClick(parent, view, position, id);
		if (!MultiConnectionService.sVotes.isEmpty())
		{
			mTime = Integer.parseInt(mEditText.getText().toString());
			Intent intent = null;
			switch (position)
			{
				case YES_NO:
					intent = new Intent(getApplicationContext(), Chart_YN.class).putExtra(
							"position", position);
					break;
				case A_B:
					intent = new Intent(getApplicationContext(), Chart_AB.class).putExtra(
							"position", position);
					break;
				case A_B_C:
					intent = new Intent(getApplicationContext(), Chart_ABC.class).putExtra(
							"position", position);
					break;
				case A_B_C_D:
					intent = new Intent(getApplicationContext(), Chart_ABCD.class).putExtra(
							"position", position);
					break;
				case Multi_A_B_C:
					intent = new Intent(getApplicationContext(), ChartMulti_ABC.class).putExtra(
							"position", position);
					break;
				case Multi_A_B_C_D:
					break;
				default:
					break;
			}
			postStartActivity(intent.putExtra("time", mTime).putExtra("position", position));
		}
		else
			Toast.makeText(this, "No devices found for connection. Please try later",
					Toast.LENGTH_LONG).show();

	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}
	@Override
	public void onBackPressed()
	{
		startService(new Intent(MultiConnectionService.ACTION_QUIT));
		super.onBackPressed();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.ctx_menu, menu);
	    return true;
	}	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
	    switch (item.getItemId()) 
	    {
		    case R.id.add:
				Intent intent=new Intent(ACTION_CONNECT_ANDROID);
				if (VERSION.SDK_INT>=VERSION_CODES.ICE_CREAM_SANDWICH)
				{
					intent.putExtra(EXTRA_THEME_ID,android.R.style.Theme_Holo_Light_DarkActionBar);
				}
				else
				{
					intent.putExtra(EXTRA_THEME_ID,android.R.style.Theme_Holo_Light_NoActionBar);
				}
				intent.putExtra(EXTRA_FLAGS,FLAG_PROPOSE_PAIRING|FLAG_ACCEPT_ANONYMOUS);
		    	startActivityForResult(intent, REQUEST_CONNECT_CODE);
		        return true;
		    default:
		        return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
    protected void onUserLeaveHint() 
	{
		super.onUserLeaveHint(); // FIXME
	}
	private final BroadcastReceiver	mReceiver	= new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			mDevices.setText(" " + MultiConnectionService.sMe.getSize());
		}
	};
	@Override
	@TargetApi(14)
	public NdefMessage createNdefMessage(NfcEvent event)
	{
		startService(new Intent(MultiConnectionService.ACTION_START_DISCOVER));
		return (MultiConnectionService.mManager!=null) ? MultiConnectionService.mManager.createNdefMessage() : null;
	}

}
