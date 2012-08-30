/******************************************************************************
 *
 * droid2droid - Distributed Android Framework
 * ==========================================
 *
 * Copyright (C) 2012 by Atos (http://www.http://atos.net)
 * http://www.droid2droid.org
 *
 ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
******************************************************************************/
package org.droid2droid.apps.buzzer;

import static org.droid2droid.Droid2DroidManager.DISCOVER_INFINITELY;
import static org.droid2droid.Droid2DroidManager.EXTRA_DISCOVER;
import static org.droid2droid.Droid2DroidManager.EXTRA_UPDATE;
import static org.droid2droid.Droid2DroidManager.FLAG_ACCEPT_ANONYMOUS;
import static org.droid2droid.Droid2DroidManager.FLAG_PROPOSE_PAIRING;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.droid2droid.Droid2DroidManager;
import org.droid2droid.ListRemoteAndroidInfo;
import org.droid2droid.RemoteAndroid;
import org.droid2droid.RemoteAndroid.PublishListener;
import org.droid2droid.RemoteAndroidInfo;
import org.droid2droid.apps.buzzer.charts.AbstractChart;
import org.droid2droid.apps.buzzer.remote.RemoteVote;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
public class MultiConnectionService extends Service
{
	public static MultiConnectionService sMe;
	public static final String TAG = "AbstractChart";

	public static final String ACTION_START_DISCOVER = "org.droid2droid.apps.buzzer.START_DISCOVER";

	public static final String ACTION_STOP_DISCOVER = "org.droid2droid.apps.buzzer.STOP_DISCOVER";
	
	public static final String ACTION_CONNECT = "org.droid2droid.apps.buzzer.CONNECT";

	public static final String ACTION_ADD_DEVICE = "org.droid2droid.apps.buzzer.ADD_DEVICE";

	public static final String ACTION_VOTE = "org.droid2droid.apps.buzzer.VOTE";

	public static final String ACTION_QUIT = "org.droid2droid.apps.buzzer.QUIT";

	private enum Mode {
		CONNECT, VOTE, WAIT
	};

	private Mode mState = Mode.CONNECT;

	// FIXME: remove static
	public static Droid2DroidManager mManager;

	public static Map<String, RemoteVote> sVotes = Collections.synchronizedMap(new HashMap<String, RemoteVote>());
	public static List<RemoteAndroid> sRemoteAndroids=Collections.synchronizedList(new ArrayList<RemoteAndroid>());
	
	private long mStartTime;

	private int mPosition;

	private int mTemps;

	private AtomicInteger mWaitingVote = new AtomicInteger();

	private boolean mDiscover;
	// List of knowns devices
	ListRemoteAndroidInfo mAndroids;

	Handler mHandler = new Handler();

	public void onDiscover(final RemoteAndroidInfo remoteAndroidInfo, boolean replace)
	{
		if (!mDiscover)
			return;
		if (remoteAndroidInfo.getUris().length == 0)
			return;
		// If try a new connexion, and must pairing devices, the discover fire, but i must ignore it now. I will manage in the onResult.
		mAndroids.remove(remoteAndroidInfo);
		mAndroids.add(remoteAndroidInfo);
//		if (replace)
//			return; // TODO Optimise la connexion
		startConnection(remoteAndroidInfo);
		
	}
	private void startConnection(final RemoteAndroidInfo remoteAndroidInfo)
	{
		Log.d("Buzzer","Start connection with "+remoteAndroidInfo.getName());
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				if (mManager==null)
				{
Log.e(TAG,"SLEEP");				
					try { Thread.sleep(1000); } catch (InterruptedException e) 
					{
						// Ignore
					}
					if (mManager==null)
					{
Log.e(TAG,"Refuse start. mManager not initialized");				
						return;
					}
				}
				for (String uri : remoteAndroidInfo.getUris())
				{

					Log.d("Buzzer","try connection with "+uri+" for "+remoteAndroidInfo.getName());
					if (connect(remoteAndroidInfo, uri, true))
					{
						Log.d("Buzzer","Connected with "+uri+" for "+remoteAndroidInfo.getName());
						if (mState == Mode.VOTE)
						{
							mWaitingVote.incrementAndGet();
							doVote(sVotes.get(uri));
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
		if (sMe!=null) return;
		Log.d("service","Service onCreate");
		sMe=this;
		mAndroids=Droid2DroidManager.newDiscoveredAndroid(this, new ListRemoteAndroidInfo.DiscoverListener()
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
		Droid2DroidManager.bindManager(this, new Droid2DroidManager.ManagerListener()
		{
			
			@Override
			public void unbind(Droid2DroidManager manager)
			{
				mManager=null;
			}
			
			@Override
			public void bind(Droid2DroidManager manager)
			{
				mManager=manager;
			}
		});
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Log.d("service","Service onDestroy");
		stopService();
	}
	private synchronized void stopService()
	{
		if (sMe==null)
			return;
		sMe=null;
		mAndroids.close();
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				stopSyncService();
			}
		}).start();
//		new AsyncTask<Void, Void, Void>()
//		{
//			@Override
//			protected Void doInBackground(Void... params)
//			{
//				stopSyncService();
//				return null;
//			}
//		}.execute();
		stopSelf();
	}

	private void stopSyncService()
	{
		for (RemoteVote vote:sVotes.values())
		{
			try
			{
				vote.exit();
			}
			catch (RemoteException e)
			{
				// Ignore
			}
		}
		sVotes.clear();
		for (RemoteAndroid ra:sRemoteAndroids)
		{
			ra.close();
		}
		sRemoteAndroids.clear();
		if (mManager!=null)
			mManager.close();
		sMe=null;
		
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	int getSize()
	{
		return (sVotes==null) ? 0 : sVotes.size();
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		String action = intent.getAction();
		Log.i(TAG, "start command " + action);
		if (ACTION_START_DISCOVER.equals(action))
		{
			startDiscover();
		}
		else if (ACTION_STOP_DISCOVER.equals(action))
		{
			stopDiscover();
		}
		else if (ACTION_ADD_DEVICE.equals(action))
		{
			addDevice(intent);
		}
		else if (ACTION_CONNECT.equals(action))
		{
			connect();
		}
		else if (ACTION_VOTE.equals(action))
		{
			vote(intent);
		}
		else if (ACTION_QUIT.equals(action))
		{
			stopService();
		}
		return START_NOT_STICKY;//START_REDELIVER_INTENT;
	}
	private void startDiscover()
	{
		if (mManager!=null)
		{
			mDiscover=true;
			mManager.startDiscover(FLAG_ACCEPT_ANONYMOUS,DISCOVER_INFINITELY);
		}
	}
	private void stopDiscover()
	{
		if (mManager!=null)
		{
			mDiscover=false;
			mManager.cancelDiscover();
		}
	}
	private void addDevice(Intent intent)
	{
		RemoteAndroidInfo remoteAndroidInfo=(RemoteAndroidInfo)intent.getParcelableExtra(EXTRA_DISCOVER);
		boolean replace=intent.getBooleanExtra(EXTRA_UPDATE,false);
		registerDevice(remoteAndroidInfo,replace);
	}
	public void registerDevice(RemoteAndroidInfo remoteAndroidInfo,boolean replace) // FIXME: use replace
	{
		for (String uri:remoteAndroidInfo.getUris())
		{
			if (sVotes.get(uri)!=null) 
			{
				Log.d(TAG,"Refuse to register another time "+remoteAndroidInfo);
				return;
			}
		}
		mAndroids.add(remoteAndroidInfo);
		startConnection(remoteAndroidInfo);
	}
	private void connect()
	{
		mState = Mode.CONNECT;
	}
	
	private void vote(Intent intent)
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
		mWaitingVote = new AtomicInteger(sVotes.size());
		// -------------------------------------------
		Intent intentInfo = new Intent(AbstractChart.CHART_RESULTS);
		intentInfo.putExtra(
			"max", sVotes.size()) // Le nombre de votant
				.putExtra(
					"pending", mWaitingVote.get()) // Le nombre de vote en
													// attente
				.putExtra(
					"time", mTemps).putExtra(
					"startTime", mStartTime);
		sendBroadcast(intentInfo);
		// ------------------------------------------------

		for (final Iterator<RemoteVote> i = sVotes.values().iterator(); i.hasNext();)
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
					sVotes.remove(uri);
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
					sRemoteAndroids.add(rA);
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
										Toast.makeText(MultiConnectionService.this, 
											"Device "+rA.getInfos().getName()+" accept only applications from market.", 
											Toast.LENGTH_LONG);
									else if (status == -1)
									{
										// Refused
										sVotes.remove(uri);
										mAndroids.remove(info);
									}
									else if (status >= 0)
									{
										rA.bindService(
											new Intent("org.droid2droid.apps.buzzer.Vote"), new ServiceConnection()
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
													sVotes.put(uri, vote);
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
														Intent intent = new Intent(BuzzerActivity.REGISTER);
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
									sVotes.remove(uri);
									mAndroids.remove(info);
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
			}, FLAG_PROPOSE_PAIRING|FLAG_ACCEPT_ANONYMOUS);
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
			vote.standby();
			int pending = mWaitingVote.decrementAndGet();
			Intent intent = new Intent(AbstractChart.CHART_RESULTS);
			intent.putExtra(
				"result", result); // Le résultat du vote
			intent.putExtra(
				"max", sVotes.size()); // Le nombre de votant
			intent.putExtra(
				"pending", pending); // Le nombre de vote en attente
			if (pending == 0)
				connect();
			sendBroadcast(intent);
		}
		catch (RemoteException e)
		{
			e.printStackTrace();
			sVotes.remove(vote);
			Intent intent = new Intent(AbstractChart.CHART_RESULTS);
			intent.putExtra(
				"result", -1); // Le résultat du vote
			intent.putExtra(
				"max", sVotes.size()); // Le nombre de votant
			int pending = mWaitingVote.decrementAndGet();
			intent.putExtra(
				"pending", pending); // Le nombre de vote en attente
			if (pending == 0)
				connect();
			sendBroadcast(intent);
		}
	}

}
