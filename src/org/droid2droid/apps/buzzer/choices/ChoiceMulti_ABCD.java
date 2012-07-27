package org.droid2droid.apps.buzzer.choices;

import org.droid2droid.apps.buzzer.R;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ChoiceMulti_ABCD extends AbstractChoice implements OnCheckedChangeListener,
		OnClickListener
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		setContentView(R.layout.multi_abcd_choice);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		// TODO Auto-generated method stub

	}

}
