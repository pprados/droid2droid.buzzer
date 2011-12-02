package org.remoteandroid.apps.buzzer;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.remoteandroid.ListRemoteAndroidInfo;
import org.remoteandroid.RemoteAndroid;
import org.remoteandroid.RemoteAndroid.PublishListener;
import org.remoteandroid.RemoteAndroidInfo;
import org.remoteandroid.RemoteAndroidManager;
import org.remoteandroid.apps.buzzer.charts.AbstractChart;
import org.remoteandroid.apps.buzzer.remote.RemoteVote;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import android.widget.Toast;

public class MultiConnectionService extends Service
{
	public static final String TAG = "AbstractChart";

	public static final String ACTION_CONNECT = "org.remoteandroid.apps.buzzer.CONNECT";

	public static final String ACTION_ADD_DEVICE = "org.remoteandroid.apps.buzzer.ADD_DEVICE";

	public static final String ACTION_VOTE = "org.remoteandroid.apps.buzzer.VOTE";

	public static final String ACTION_QUIT = "org.remoteandroid.apps.buzzer.QUIT";

	private enum Mode {
		CONNECT, VOTE, WAIT
	};

	private Mode mState = Mode.CONNECT;

	private RemoteAndroidManager mManager;

	public static Map<String, RemoteVote> mVotes = Collections.synchronizedMap(new HashMap<String, RemoteVote>());

	private long mStartTime;

	private int mPosition;

	private int mTemps;

	private AtomicInteger mWaitingVote = new AtomicInteger();

	// List of knowns devices
	ListRemoteAndroidInfo mAndroids;

	Handler mHandler = new Handler();

	public void onDiscover(final RemoteAndroidInfo remoteAndroidInfo, boolean replace)
	{
		if (remoteAndroidInfo.getUris().length == 0)
			return;
		if (replace)
			return; // TODO Optimise la connexion
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				for (String uri : remoteAndroidInfo.getUris())
				{

					if (connect(remoteAndroidInfo, uri, true))
					{
						if (mState == Mode.VOTE)
						{
							mWaitingVote.incrementAndGet();
							doVote(mVotes.get(uri));
						}
						break;
					}
				}
			}
		}).start();
		
	}
	@Override
	public void onCreate()
	{
		super.onCreate();
		mManager = RemoteAndroidManager.getManager(this);

		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				mAndroids = mManager.newDiscoveredAndroid(new ListRemoteAndroidInfo.DiscoverListener()
				{
					@Override
					public void onDiscover(final RemoteAndroidInfo remoteAndroidInfo, boolean replace)
					{
						MultiConnectionService.this.onDiscover(remoteAndroidInfo,replace);
					}

					@Override
					public void onDiscoverStart()
					{
					}

					@Override
					public void onDiscoverStop()
					{
					}
				});
				mAndroids.start(RemoteAndroidManager.DISCOVER_BEST_EFFORT);
			}
		}).start();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if (mAndroids != null)
			mAndroids.cancel();
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		String action = intent.getAction();
		Log.i(TAG, "start command " + action);
		if (ACTION_ADD_DEVICE.equals(action))
		{
			RemoteAndroidInfo remoteAndroidInfo=(RemoteAndroidInfo)intent.getParcelableExtra(RemoteAndroidManager.EXTRA_DISCOVER);
			boolean replace=intent.getBooleanExtra(RemoteAndroidManager.EXTRA_UPDATE,false);
			onDiscover(remoteAndroidInfo, replace);
		}
		else if (ACTION_CONNECT.equals(action))
		{
			mState = Mode.CONNECT;
		}
		else if (ACTION_VOTE.equals(action))
		{
			if (mState != Mode.CONNECT)
			{
				if (mState != Mode.WAIT)
					// throw new IllegalArgumentException("Current vote");
					// Signaler que le vote est toujours en cours et attends
					// d'autres participants
					Toast.makeText(
						this, "Pending vote. Please wait", Toast.LENGTH_LONG).show();
			}
			mState = Mode.VOTE;
			mStartTime = System.currentTimeMillis();
			mTemps = intent.getIntExtra(
				"time", 0);
			mPosition = intent.getIntExtra(
				"position", -1);
			mWaitingVote = new AtomicInteger(mVotes.size());
			// -------------------------------------------
			Intent intentInfo = new Intent(AbstractChart.CHART_RESULTS);
			intentInfo.putExtra(
				"max", mVotes.size()) // Le nombre de votant
					.putExtra(
						"pending", mWaitingVote.get()) // Le nombre de vote en
														// attente
					.putExtra(
						"time", mTemps).putExtra(
						"startTime", mStartTime);
			sendBroadcast(intentInfo);
			// ------------------------------------------------

			for (final Iterator<RemoteVote> i = mVotes.values().iterator(); i.hasNext();)
			{
				final RemoteVote vote = i.next();
				new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						doVote(vote);
					}

				}).start();
			}
		}
		else if (ACTION_QUIT.equals(action))
		{
			stopSelf();
		}
		return 0;// START_REDELIVER_INTENT;
	}

	private boolean connect(final RemoteAndroidInfo info, final String uri, final boolean block)
	{
		if (info.getUris().length == 0)
			return false;
		class Result
		{
			volatile boolean rc;
		}
		final Result result = new Result();
		

		mManager.bindRemoteAndroid(
			new Intent(Intent.ACTION_MAIN, Uri.parse(uri)), new ServiceConnection()
			{
				@Override
				public void onServiceDisconnected(ComponentName name)
				{
					mVotes.remove(uri);
					mAndroids.remove(info);
					if (block)
					{
						synchronized (MultiConnectionService.this)
						{
							MultiConnectionService.this.notify();
						}
					}
				}

				@Override
				public void onServiceConnected(ComponentName name, IBinder service)
				{
					final RemoteAndroid rA = (RemoteAndroid) service;
					rA.setExecuteTimeout(60*60000L);
					try
					{
						rA.pushMe(
							getApplicationContext(), new PublishListener()
							{
								@Override
								public void onProgress(int progress)
								{
									// setStatus("Progress..."+progress/100+"%");
								}

								@Override
								public void onFinish(int status)
								{
									if (status == -2)
										;
									// setStatus("Impossible to install application not from market");
									else if (status == -1)
										;
									// setStatus("Install refused");
									else if (status >= 0)
									{
										rA.bindService(
											new Intent("org.remoteandroid.apps.buzzer.Vote"), new ServiceConnection()
											{
												RemoteVote vote;

												@Override
												public void onServiceDisconnected(ComponentName name)
												{
													vote = null;
												}

												@Override
												public void onServiceConnected(ComponentName name, IBinder service)
												{
													vote = RemoteVote.Stub.asInterface(service);
													mVotes.put(uri, vote);
													if (block)
													{
														result.rc=true;
														synchronized (MultiConnectionService.this)
														{
															MultiConnectionService.this.notify();
														}
													}
													try
													{
														Intent intent = new Intent(SelectActivity.REGISTER);
														sendBroadcast(intent);
														vote.standby();
														mState = Mode.WAIT;
													}
													catch (RemoteException e)
													{
														e.printStackTrace();
													}
												}
											}, Context.BIND_AUTO_CREATE);
									}
								}

								@Override
								public void onError(Throwable e)
								{
									// TODO
								}

								@Override
								public boolean askIsPushApk()
								{
									return true;
								}
							}, 0 /*
								 * (SelectActivity.DEBUG) ?
								 * RemoteAndroid.INSTALL_REPLACE_EXISTING : 0
								 */, 60000);
					}
					catch (RemoteException e)
					{
						e.printStackTrace();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}, 0);
		if (block)
		{
			synchronized (this)
			{
				try
				{
					wait();
					return result.rc;
				}
				catch (InterruptedException e)
				{
					return false; // Waiting correct
				} // FIXME
			}
		}
		return false;
	}

	private void doVote(RemoteVote vote)
	{
		try
		{
			int result = vote.vote(
				mPosition, mStartTime, mTemps);
			int pending = mWaitingVote.decrementAndGet();
			Intent intent = new Intent(AbstractChart.CHART_RESULTS);
			intent.putExtra(
				"result", result); // Le résultat du vote
			intent.putExtra(
				"max", mVotes.size()); // Le nombre de votant
			intent.putExtra(
				"pending", pending); // Le nombre de vote en attente
			if (pending == 0)
				mState = Mode.CONNECT;
			sendBroadcast(intent);
		}
		catch (RemoteException e)
		{
			e.printStackTrace();
			mVotes.remove(vote);
			Intent intent = new Intent(AbstractChart.CHART_RESULTS);
			intent.putExtra(
				"result", -1); // Le résultat du vote
			intent.putExtra(
				"max", mVotes.size()); // Le nombre de votant
			int pending = mWaitingVote.decrementAndGet();
			intent.putExtra(
				"pending", pending); // Le nombre de vote en attente
			if (pending == 0)
				mState = Mode.CONNECT;
			sendBroadcast(intent);
		}
	}

}
