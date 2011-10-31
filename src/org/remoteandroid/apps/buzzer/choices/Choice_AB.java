package org.remoteandroid.apps.buzzer.choices;

import org.remoteandroid.apps.buzzer.R;
import org.remoteandroid.apps.buzzer.remote.RemoteVoteImpl;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Choice_AB extends AbstractChoice implements OnClickListener
{
	private Button			mA, mB;

	public final static int	CHOICE_A	= 0;

	public final static int	CHOICE_B	= 1;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		setContentView(R.layout.aorb_choice);
		super.onCreate(savedInstanceState);
		mA = (Button) findViewById(R.id.button_a);
		mB = (Button) findViewById(R.id.button_b);
		mA.setOnClickListener(this);
		mB.setOnClickListener(this);
	}

	@Override
	public void onClick(View v)
	{
		if (v == mA)
		{
			RemoteVoteImpl.postResult(CHOICE_A);
			finish();
		}
		else if (v == mB)
		{
			RemoteVoteImpl.postResult(CHOICE_B);
			finish();
		}
	}
}
