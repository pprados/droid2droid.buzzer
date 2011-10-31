package org.remoteandroid.apps.buzzer;

import org.remoteandroid.RemoteAndroidManager;
import org.remoteandroid.apps.buzzer.charts.ChartMulti_ABC;
import org.remoteandroid.apps.buzzer.charts.Chart_AB;
import org.remoteandroid.apps.buzzer.charts.Chart_ABC;
import org.remoteandroid.apps.buzzer.charts.Chart_ABCD;
import org.remoteandroid.apps.buzzer.charts.Chart_YN;
import org.remoteandroid.apps.buzzer.remote.RemoteVote;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SelectActivity extends ListActivity
{
	public static boolean		DEBUG			= true;

	public static final int		YES_NO			= 0;

	public static final int		A_B				= 1;

	public static final int		A_B_C			= 2;

	public static final int		A_B_C_D			= 3;

	public static final int		Multi_A_B_C		= 4;

	public static final int		Multi_A_B_C_D	= 5;

	public static final String	REGISTER		= "org.remoteandroid.apps.buzzer.REGISTER";

	private int					mConnectedDevices;

	private static final int	DIALOG_MARKET	= 1;

	RemoteVote					mVote;

	private EditText			mEditText;

	private TextView			mDevices;

	int							mTime;

	Handler						mHandler		= new Handler();

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
		mConnectedDevices = 0;
		IntentFilter filter = new IntentFilter(REGISTER);
		registerReceiver(mReceiver, filter);
		mEditText = (EditText) findViewById(R.id.editText);
		mDevices = (TextView) findViewById(R.id.active_connection_number);
		setListAdapter(new ArrayAdapter<String>(this, R.layout.choicerow, R.id.label,
				getResources().getStringArray(R.array.choice_array)));
		mDevices.setText(" " + mConnectedDevices);
		startService(new Intent(MultiConnectionService.ACTION_CONNECT));
	}

	public static void enableStrictMode()
	{
		if (DEBUG && Build.VERSION.SDK_INT >= 9/* Build.VERSION_CODES.GINGERBREAD */)
		{
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll()
					.penaltyLog()
					// .penaltyDialog()
					.build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyDropBox()
					.build());
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		Intent market = RemoteAndroidManager.getIntentForMarket(this);
		if (market != null)
		{

			showDialog(DIALOG_MARKET);
		}

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
										.getIntentForMarket(SelectActivity.this));
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
		startService(new Intent(MultiConnectionService.ACTION_QUIT));
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}

	private BroadcastReceiver	mReceiver	= new BroadcastReceiver()
											{
												@Override
												public void onReceive(Context context, Intent intent)
												{
													++mConnectedDevices;
													mDevices.setText(" " + mConnectedDevices);
												}
											};
}
