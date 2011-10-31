package org.remoteandroid.apps.buzzer.remote;

import org.remoteandroid.apps.buzzer.SelectActivity;
import org.remoteandroid.apps.buzzer.WaitingActivity;
import org.remoteandroid.apps.buzzer.choices.ChoiceMulti_ABC;
import org.remoteandroid.apps.buzzer.choices.ChoiceMulti_ABCD;
import org.remoteandroid.apps.buzzer.choices.Choice_AB;
import org.remoteandroid.apps.buzzer.choices.Choice_ABC;
import org.remoteandroid.apps.buzzer.choices.Choice_ABCD;
import org.remoteandroid.apps.buzzer.choices.Choice_YN;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.RemoteException;

public class RemoteVoteImpl extends RemoteVote.Stub
{
	private Context			mContext;

	private static Object	sLock		= new Object();

	private static int		sLastResult	= -1;

	private Handler			mHandler	= new Handler();

	public RemoteVoteImpl(Context context)
	{
		this.mContext = context;
	}

	private void postStartActivity(final Intent intent)
	{
		mHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				mContext.startActivity(intent);
			}
		});
	}

	@Override
	public int vote(int position, long startTime, int temps) throws RemoteException
	{
		switch (position)
		{
			case SelectActivity.YES_NO:
				postStartActivity(new Intent(this.mContext, Choice_YN.class)
						.putExtra("startTime", System.currentTimeMillis()).putExtra("time", temps)
						.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
				break;
			case SelectActivity.A_B:
				postStartActivity(new Intent(this.mContext, Choice_AB.class)
						.putExtra("startTime", System.currentTimeMillis()).putExtra("time", temps)
						.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
				break;
			case SelectActivity.A_B_C:
				postStartActivity(new Intent(this.mContext, Choice_ABC.class)
						.putExtra("startTime", System.currentTimeMillis()).putExtra("time", temps)
						.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
				break;
			case SelectActivity.A_B_C_D:
				postStartActivity(new Intent(this.mContext, Choice_ABCD.class)
						.putExtra("startTime", System.currentTimeMillis()).putExtra("time", temps)
						.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
				break;
			case SelectActivity.Multi_A_B_C:
				postStartActivity(new Intent(this.mContext, ChoiceMulti_ABC.class)
						.putExtra("startTime", System.currentTimeMillis()).putExtra("time", temps)
						.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
				break;
			case SelectActivity.Multi_A_B_C_D:
				postStartActivity(new Intent(this.mContext, ChoiceMulti_ABCD.class)
						.putExtra("startTime", System.currentTimeMillis()).putExtra("time", temps)
						.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
				break;
			default:
				break;
		}
		// Bloquer un lock pour attendre la reponse grace a la methode getResult
		return getResult(temps);
	}

	public static void postResult(int r)
	{
		synchronized (sLock)
		{
			sLastResult = r;
			sLock.notify();
		}
	}

	private static int getResult(int temps)
	{
		synchronized (sLock)
		{
			try
			{
				sLock.wait(temps * 1000L + 500);
				return sLastResult;

			}
			catch (InterruptedException e)
			{
				return -1;
			}
		}
	}

	@Override
	public void standby() throws RemoteException
	{
		postStartActivity(new Intent(mContext, WaitingActivity.class)
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
	}

}
