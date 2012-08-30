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

public class Choice_ABCD extends AbstractChoice implements OnClickListener
{
	private Button			mA, mB, mC, mD;

	public final static int	CHOICE_A	= 0;

	public final static int	CHOICE_B	= 1;

	public final static int	CHOICE_C	= 2;

	public final static int	CHOICE_D	= 3;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		setContentView(R.layout.aorborcord_choice);
		super.onCreate(savedInstanceState);
		mA = (Button) findViewById(R.id.button_a);
		mB = (Button) findViewById(R.id.button_b);
		mC = (Button) findViewById(R.id.button_c);
		mD = (Button) findViewById(R.id.button_d);
		mA.setOnClickListener(this);
		mB.setOnClickListener(this);
		mC.setOnClickListener(this);
		mD.setOnClickListener(this);
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
		else if (v == mD)
		{
			RemoteVoteImpl.postResult(CHOICE_D);
			finish();
		}
	}

}
