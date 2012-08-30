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
package org.droid2droid.apps.buzzer.charts;

import org.achartengine.chart.PieChart;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.droid2droid.apps.buzzer.MultiConnectionService;
import org.droid2droid.apps.buzzer.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public abstract class AbstractChart extends Activity implements OnClickListener
{
	public static String			CHART_RESULTS	= "org.droid2droid.apps.buzzer.Charts.Results";

	public static final String	TAG				= "AbstractChart";

	protected TextView				mLabelVoteNumber, mLabelVoteRemain, mLabelVoteNull;

	protected AsyncTask<?, ?, ?>	mAsyncTask;

	protected ProgressBar			mTimeBar;

	protected Button				mGoBack;

	protected View					mChart;

	protected DefaultRenderer		mRenderer;

	protected CategorySeries		mSeries;

	protected Retain				mRetain;

	protected int					mPosition;

	protected LinearLayout			mMain;

	protected abstract Retain newRetain();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		IntentFilter filter = new IntentFilter(CHART_RESULTS);
		registerReceiver(mAbsReceiver, filter);
		mLabelVoteNumber = (TextView) findViewById(R.id.number_vote);
		mLabelVoteRemain = (TextView) findViewById(R.id.vote_remain);
		mLabelVoteNull = (TextView) findViewById(R.id.vote_null);
		mTimeBar = (ProgressBar) findViewById(R.id.timeBar);
		mGoBack = (Button) findViewById(R.id.go_back);
		mGoBack.setOnClickListener(this);
		//mGoBack.setEnabled(false);

		// Restore retain
		// 1. Previously saved with onRetainNonConfigurationInstance ?
		// 2. Previously saved with onSaveInstanceState ?
		// 3. else, create an instance
		mRetain = (Retain) getLastNonConfigurationInstance();
		if (mRetain == null)
		{
			Intent intent = getIntent();
			if (savedInstanceState != null)
			{
				mRetain = savedInstanceState.getParcelable("retain");
				mLabelVoteNumber.setText(String.valueOf(mRetain.mVote_number));
				mLabelVoteRemain.setText(String.valueOf(mRetain.mVote_remain));
				mLabelVoteNull.setText(String.valueOf(mRetain.mVote_null));
				if (mRetain.mVote_remain == 0)
					mGoBack.setEnabled(true);
			}
			else
			{
				mRetain = newRetain();
				mRetain.time = intent.getIntExtra("time", 0);
				mRetain.startTime = intent.getLongExtra("startTime", System.currentTimeMillis());
				mRenderer = new DefaultRenderer();
				int position = intent.getIntExtra("position", -1);
				Log.d(TAG, "startService...");
				startService(new Intent(MultiConnectionService.ACTION_VOTE).putExtra("time",
						mRetain.time).putExtra("position", position));
			}
		}
		else
		{
			mRetain = savedInstanceState.getParcelable("retain");
			mLabelVoteNumber.setText(String.valueOf(mRetain.mVote_number));
			mLabelVoteRemain.setText(String.valueOf(mRetain.mVote_remain));
			mLabelVoteNull.setText(String.valueOf(mRetain.mVote_null));
			if (mRetain.mVote_remain == 0)
				mGoBack.setEnabled(true);
		}
		mTimeBar.setMax(mRetain.time * 1000);
		startUpdateTimeBar();
	}

	public static abstract class Retain implements Parcelable
	{
		protected long	startTime;

		protected int	mVote_number	= 0;

		protected int	mVote_remain	= 0;

		protected int	mVote_null		= 0;

		protected int	time;

		@Override
		public int describeContents()
		{
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags)
		{
			dest.writeLong(startTime);
			dest.writeInt(mVote_number);
			dest.writeInt(mVote_remain);
			dest.writeInt(mVote_null);
			dest.writeInt(time);
		}

		protected void readFromParcel(Parcel parcel)
		{
			startTime = parcel.readLong();
			mVote_number = parcel.readInt();
			mVote_remain = parcel.readInt();
			mVote_null = parcel.readInt();
			time = parcel.readInt();
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		mAsyncTask.cancel(true);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putParcelable("retain", mRetain);
	}

	@Override
	public final Object onRetainNonConfigurationInstance()
	{
		return mRetain;
	}

	private void startUpdateTimeBar()
	{
		mAsyncTask = new AsyncTask<Void, Integer, Void>()
		{
			@Override
			protected Void doInBackground(Void... params)
			{
				int progresStatus;
				do
				{
					try
					{
						Thread.sleep(200);
					}
					catch (Exception e)
					{
						// TODO : A voir avec philippe
					}
					if (isCancelled())
						return null;
					progresStatus = (int) (((System.currentTimeMillis() - mRetain.startTime)));
					publishProgress(progresStatus);
				} while (progresStatus < mRetain.time * 1000);
				return null;
			}

			@Override
			protected void onProgressUpdate(Integer... values)
			{
				super.onProgressUpdate(values);
				mTimeBar.setProgress(values[0]);
			}

			@Override
			protected void onPostExecute(Void result)
			{
				mGoBack.setEnabled(true);
			};
		}.execute();
	}

	protected final org.achartengine.chart.AbstractChart getChart()
	{
		// Renderer
		mRenderer = new DefaultRenderer();
		mRenderer.setLabelsTextSize(15);
		mRenderer.setLegendTextSize(15);
		mRenderer.setLabelsColor(Color.BLACK);
		for (int color : new int[]
		{ Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN, Color.LTGRAY, Color.CYAN, Color.DKGRAY,
				Color.MAGENTA, Color.GRAY, Color.TRANSPARENT, Color.BLACK, Color.WHITE })
		{
			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
			r.setColor(color);
			mRenderer.addSeriesRenderer(r);
		}
		mRenderer.setZoomButtonsVisible(false);
		mRenderer.setZoomEnabled(false);
		mRenderer.setChartTitleTextSize(20);
		// Graphic
		return new PieChart(mSeries, mRenderer);
	}

	protected double arrondi(double A, int B)
	{
		return ((int) (A * Math.pow(10, B) + .5)) / Math.pow(10, B);
	}

	protected String pourcentage(double value, double total)
	{
		return String.valueOf((arrondi(value / total, 2) * 100) + " %");
	}

	protected void onReceive(Context context, Intent intent)
	{
		mRetain.mVote_number = intent.getIntExtra("max", -1);
		mRetain.mVote_remain = intent.getIntExtra("pending", -1);
		mLabelVoteNumber.setText(String.valueOf(mRetain.mVote_number));
		mLabelVoteRemain.setText(String.valueOf(mRetain.mVote_remain));
		mLabelVoteNull.setText(String.valueOf(mRetain.mVote_null));
	}

	BroadcastReceiver	mAbsReceiver	= new BroadcastReceiver()
										{
											@Override
											public void onReceive(Context context, Intent intent)
											{
												AbstractChart.this.onReceive(context, intent);
											}
										};

	@Override
	protected void onStop()
	{
		super.onStop();
		unregisterReceiver(mAbsReceiver);
	}
}
