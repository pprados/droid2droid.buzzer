package org.remoteandroid.apps.buzzer;


import java.nio.charset.Charset;

import org.remoteandroid.RemoteAndroidInfo;
import org.remoteandroid.RemoteAndroidManager;
import org.remoteandroid.apps.buzzer.charts.ChartMulti_ABC;
import org.remoteandroid.apps.buzzer.charts.Chart_AB;
import org.remoteandroid.apps.buzzer.charts.Chart_ABC;
import org.remoteandroid.apps.buzzer.charts.Chart_ABCD;
import org.remoteandroid.apps.buzzer.charts.Chart_YN;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
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
{
	CreateNdefMessageCallback mNfcCallBack;
	
	public static boolean		DEBUG			= true;
	public static boolean		USE_NFC			= true;
	public static boolean		USE_BUMP		= true;

	public static final int		YES_NO			= 0;

	public static final int		A_B				= 1;

	public static final int		A_B_C			= 2;

	public static final int		A_B_C_D			= 3;

	public static final int		Multi_A_B_C		= 4;

	public static final int		Multi_A_B_C_D	= 5;

	public static final String	REGISTER		= "org.remoteandroid.apps.buzzer.REGISTER";

	private static final int 	REQUEST_CONNECT_CODE=1;
	
	private static final int	DIALOG_MARKET	= 1;

	private EditText			mEditText;

	private TextView			mDevices;

	private int					mTime;

	private Handler				mHandler		= new Handler();

	private CheckBox			mDiscover;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Intent market = RemoteAndroidManager.getIntentForMarket(this);
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
		if (USE_NFC)
			onNfcCreate();
	}

	@Override
	public void onNewIntent(Intent intent)
	{
		// onResume gets called after this to handle the intent
		setIntent(intent);
		if (USE_NFC)
			onNfcNewIntent(intent);
	}
	@Override
	protected void onResume()
	{
		super.onResume();
		registerReceiver(mReceiver, new IntentFilter(REGISTER));
		Intent market = RemoteAndroidManager.getIntentForMarket(this);
		if (market != null)
		{

			showDialog(DIALOG_MARKET);
		}
		if (USE_NFC)
			onNfcResume();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		unregisterReceiver(mReceiver);
		if (USE_NFC)
			onNfcPause();
	}
	
	@Override
	public Dialog onCreateDialog(int id)
	{
		switch (id)
		{
			case DIALOG_MARKET:
				return new AlertDialog.Builder(this)
						.setMessage("Install the application Remote Android ?") // TODO: NLS
						.setPositiveButton("Install", new DialogInterface.OnClickListener()
						{

							@Override
							public void onClick(DialogInterface paramDialogInterface, int paramInt)
							{
								startActivity(RemoteAndroidManager
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
			RemoteAndroidInfo info=(RemoteAndroidInfo)data.getParcelableExtra(RemoteAndroidManager.EXTRA_DISCOVER);
			startService(new Intent(MultiConnectionService.ACTION_ADD_DEVICE)
				.putExtra(RemoteAndroidManager.EXTRA_DISCOVER,info)
				.putExtra(RemoteAndroidManager.EXTRA_UPDATE, data.getBooleanExtra(RemoteAndroidManager.EXTRA_UPDATE, false)));
		}
	}
	
	@Override
	protected void onListItemClick(ListView parent, View view, final int position, long id)
	{
		super.onListItemClick(parent, view, position, id);
		if (!MultiConnectionService.mVotes.isEmpty())
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
				Intent intent=new Intent(RemoteAndroidManager.ACTION_CONNECT_ANDROID);
				if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH)
				{
					intent.putExtra(RemoteAndroidManager.EXTRA_THEME_ID,android.R.style.Theme_Holo_Light_DarkActionBar);
				}
				else
				{
					intent.putExtra(RemoteAndroidManager.EXTRA_THEME_ID,android.R.style.Theme_Holo_Light_NoActionBar);
				}
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
	private BroadcastReceiver	mReceiver	= new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			mDevices.setText(" " + MultiConnectionService.sMe.getSize());
		}
	};

	//---------------------------
	// Register a listener when another device ask my tag
	NfcAdapter mNfcAdapter;
	protected void onNfcCreate()
	{
		if (USE_NFC && USE_BUMP && Build.VERSION.SDK_INT>=Build.VERSION_CODES.GINGERBREAD)
		{
			mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
	        if (mNfcAdapter != null) 
	        {
	        	mNfcAdapter.setNdefPushMessageCallback(new CreateNdefMessageCallback()
	        	{

					@Override
					public NdefMessage createNdefMessage(NfcEvent event)
					{
						// Publish my RemoteAndroidInfo
						return MultiConnectionService.mManager.createNdefMessage();
					}
	        		
	        	}, this);
	        }
		}
	}
	
	protected void onNfcNewIntent(Intent intent)
	{
		Parcelable tag=intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		if (tag!=null)
		{
			// Check the caller. Refuse spoof events
			checkCallingPermission("com.android.nfc.permission.NFCEE_ADMIN");
			startService(new Intent(MultiConnectionService.ACTION_NDEF_DISCOVER)
				.putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES,intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)));
			
		}
	}

	protected void onNfcResume()
	{
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.GINGERBREAD)
		{
			NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
			if (USE_NFC && USE_BUMP && mNfcAdapter!=null)
			{
				PendingIntent pendingIntent = 
						PendingIntent.getActivity(this, 0, 
							new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
				mNfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
			}
		}
	}
	// Unregister the exposition of my tag
    protected void onNfcPause()
    {
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.GINGERBREAD)
		{
	    	if (USE_NFC && USE_BUMP && mNfcAdapter!=null)
	    	{
	    		mNfcAdapter.disableForegroundDispatch(this);
	    	}
		}
    }
    
//	public static NdefMessage createNdefMessage(Context context,RemoteAndroidInfo info,boolean expose)
//	{
//		Messages.BroadcastMsg.Builder broadcastBuilder = Messages.BroadcastMsg.newBuilder();
//		Messages.BroadcastMsg msg=broadcastBuilder
//			.setType(expose ? Messages.BroadcastMsg.Type.EXPOSE : Messages.BroadcastMsg.Type.CONNECT)
//			.setIdentity(ProtobufConvs.toIdentity(info))
//			.build();
//		byte[] payload=msg.toByteArray();
//		return new NdefMessage(
//			new NdefRecord[]
//			{
//				NdefRecord.createApplicationRecord("org.remoteandroid"),
//				new NdefRecord(NdefRecord.TNF_MIME_MEDIA, NDEF_MIME_TYPE, new byte[0], payload),
////														NdefRecord.createUri("www.remotandroid.org")
//			}
//		);
//		
//	}
//	private static final byte[] NDEF_MIME_TYPE="application/org.remoteandroid.apps.buzzer".getBytes(Charset.forName("US-ASCII"));
//	public NdefMessage createNdefRecord(byte[] payload) 
//	{
//		return new NdefMessage(
//				new NdefRecord[]
//				{
//					new NdefRecord(NdefRecord.TNF_MIME_MEDIA, NDEF_MIME_TYPE, new byte[0], payload),
//					NdefRecord.createApplicationRecord("org.remoteandroid.apps.buzzer")
//				}
//			);
//    }
	

}
