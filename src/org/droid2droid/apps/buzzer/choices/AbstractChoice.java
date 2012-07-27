package org.droid2droid.apps.buzzer.choices;

import org.droid2droid.apps.buzzer.R;
import org.droid2droid.apps.buzzer.remote.RemoteVoteImpl;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ProgressBar;

public abstract class AbstractChoice extends Activity
{
	ProgressBar			mTimeBar;

	AsyncTask<?, ?, ?>	mAsyncTask;

	private int			mTime;

	long				mStartTime;

	public static int	TIME_OUT	= -1;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		mStartTime = intent.getLongExtra("startTime", System.currentTimeMillis());
		mTime = intent.getIntExtra("time", 0);
		mTimeBar = (ProgressBar) findViewById(R.id.timeBar);
		mTimeBar.setMax(mTime * 1000);
		startUpdateTimeBar();
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
						//
					}
					if (isCancelled())
						return null;
					progresStatus = (int) (((System.currentTimeMillis() - mStartTime)));
					publishProgress(progresStatus);
				} while (progresStatus < mTime * 1000);
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
				RemoteVoteImpl.postResult(TIME_OUT);
				finish();
			};
		}.execute();

	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		mAsyncTask.cancel(true);
	}
}
