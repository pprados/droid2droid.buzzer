package org.droid2droid.apps.buzzer.choices;

import org.droid2droid.apps.buzzer.R;
import org.droid2droid.apps.buzzer.remote.RemoteVoteImpl;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Choice_YN extends AbstractChoice implements OnClickListener
{
	public final static int	CHOICE_Y	= 0;

	public final static int	CHOICE_N	= 1;

	private Button			mYes, mNo;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		setContentView(R.layout.yesorno_choice);
		super.onCreate(savedInstanceState);
		mYes = (Button) findViewById(R.id.button_yes);
		mNo = (Button) findViewById(R.id.button_no);
		mYes.setOnClickListener(this);
		mNo.setOnClickListener(this);
	}

	@Override
	public void onClick(View v)
	{
		if (v == mYes)
		{
			RemoteVoteImpl.postResult(CHOICE_Y);
			finish();
		}
		else if (v == mNo)
		{
			RemoteVoteImpl.postResult(CHOICE_N);
			finish();
		}
	}
}
