package org.droid2droid.apps.buzzer.choices;

import org.droid2droid.apps.buzzer.R;
import org.droid2droid.apps.buzzer.remote.RemoteVoteImpl;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Choice_ABC extends AbstractChoice implements OnClickListener
{
	private Button			mA, mB, mC;

	public final static int	CHOICE_A	= 0;

	public final static int	CHOICE_B	= 1;

	public final static int	CHOICE_C	= 2;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		setContentView(R.layout.aorborc_choice);
		super.onCreate(savedInstanceState);
		mA = (Button) findViewById(R.id.button_a);
		mB = (Button) findViewById(R.id.button_b);
		mC = (Button) findViewById(R.id.button_c);
		mA.setOnClickListener(this);
		mB.setOnClickListener(this);
		mC.setOnClickListener(this);
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
		else if (v == mC)
		{
			RemoteVoteImpl.postResult(CHOICE_C);
			finish();
		}
	}

}
