package org.droid2droid.apps.buzzer.remote;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class RemoteVoteService extends Service
{
	@Override
	public IBinder onBind(Intent intent)
	{
		return new RemoteVoteImpl(getApplicationContext());
	}

}
