package org.remoteandroid.apps.buzzer.choices;

import org.remoteandroid.apps.buzzer.R;
import org.remoteandroid.apps.buzzer.remote.RemoteVoteImpl;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ChoiceMulti_ABC extends AbstractChoice implements OnCheckedChangeListener,
		OnClickListener
{
	public final static int	CHOICE_A	= 0;

	public final static int	CHOICE_AB	= 1;

	public final static int	CHOICE_AC	= 2;

	public final static int	CHOICE_ABC	= 3;

	public final static int	CHOICE_B	= 4;

	public final static int	CHOICE_BC	= 5;

	public final static int	CHOICE_C	= 6;

	private CheckBox		mA, mB, mC;

	private Button			mSubmit;

	private int				mValue		= -1;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		setContentView(R.layout.multi_abc_choice);
		super.onCreate(savedInstanceState);
		mA = (CheckBox) findViewById(R.id.checkbox_a);
		mB = (CheckBox) findViewById(R.id.checkbox_b);
		mC = (CheckBox) findViewById(R.id.checkbox_c);
		mSubmit = (Button) findViewById(R.id.submit);
		mA.setOnCheckedChangeListener(this);
		mB.setOnCheckedChangeListener(this);
		mC.setOnCheckedChangeListener(this);
		mSubmit.setOnClickListener(this);
	}

	@Override
	public void onClick(View v)
	{
		RemoteVoteImpl.postResult(mValue);
		finish();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		if (mA.isChecked() && !mB.isChecked() && !mC.isChecked())
			mValue = CHOICE_A;
		else if (mA.isChecked() && mB.isChecked() && !mC.isChecked())
			mValue = CHOICE_AB;
		else if (mA.isChecked() && mB.isChecked() && mC.isChecked())
			mValue = CHOICE_ABC;
		else if (mA.isChecked() && !mB.isChecked() && mC.isChecked())
			mValue = CHOICE_AC;
		else if (!mA.isChecked() && mB.isChecked() && !mC.isChecked())
			mValue = CHOICE_B;
		else if (!mA.isChecked() && mB.isChecked() && mC.isChecked())
			mValue = CHOICE_BC;
		else if (!mA.isChecked() && !mB.isChecked() && mC.isChecked())
			mValue = CHOICE_C;
	}

}
