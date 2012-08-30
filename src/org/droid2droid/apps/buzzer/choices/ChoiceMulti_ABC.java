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
package org.droid2droid.apps.buzzer.choices;

import org.droid2droid.apps.buzzer.R;
import org.droid2droid.apps.buzzer.remote.RemoteVoteImpl;

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
