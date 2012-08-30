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
package org.droid2droid.apps.buzzer.charts;

import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.droid2droid.apps.buzzer.R;
import org.droid2droid.apps.buzzer.choices.Choice_YN;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.LinearLayout;

public class Chart_YN extends AbstractChart
{

	public static class ChartRetain extends Retain
	{
		private int	mVote_yes	= 0;

		private int	mVote_no	= 0;

		@Override
		public void writeToParcel(Parcel dest, int flags)
		{
			super.writeToParcel(dest, flags);
			dest.writeInt(mVote_yes);
			dest.writeInt(mVote_no);
		}

		@Override
		protected void readFromParcel(Parcel parcel)
		{
			super.readFromParcel(parcel);
			mVote_yes = parcel.readInt();
			mVote_no = parcel.readInt();
		}

		public static final Parcelable.Creator<ChartRetain>	CREATOR	= new Parcelable.Creator<ChartRetain>()
																	{
																		@Override
																		public ChartRetain createFromParcel(
																				Parcel parcel)
																		{
																			ChartRetain retain = new ChartRetain();
																			retain.readFromParcel(parcel);
																			return retain;
																		}

																		@Override
																		public ChartRetain[] newArray(
																				int size)
																		{
																			return new ChartRetain[size];
																		}
																	};
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		setContentView(R.layout.result_chart);
		super.onCreate(savedInstanceState);
		final ChartRetain chartRetain = getRetain();
		mMain = (LinearLayout) findViewById(R.id.main);
		mSeries = new CategorySeries("Choice Yes or No Chart");
		updateValues(chartRetain);
		// Series
		mChart = new GraphicalView(this, getChart());
		mMain.addView(mChart);
	}

	@Override
	protected void onReceive(Context context, Intent intent)
	{
		super.onReceive(context, intent);
		int result = intent.getIntExtra("result", -2);
		final ChartRetain chartRetain = getRetain();
		chartRetain.mVote_remain = intent.getIntExtra("pending", 0);
		switch (result)
		{
			case Choice_YN.CHOICE_Y:
				chartRetain.mVote_yes++;
				break;
			case Choice_YN.CHOICE_N:
				chartRetain.mVote_no++;
				break;
			case -2:
				break;
			default:
				chartRetain.mVote_null++;
				break;
		}
		mSeries.clear();
		updateValues(chartRetain);
		if (chartRetain.mVote_remain == 0)
			mGoBack.setEnabled(true);
		mChart.invalidate();
	}

	@Override
	public void onClick(View view)
	{
		mAsyncTask.cancel(true);
		finish();
	}

	private ChartRetain getRetain()
	{
		return (ChartRetain) mRetain;
	}

	@Override
	protected Retain newRetain()
	{
		return new ChartRetain();
	}

	private void updateValues(ChartRetain chartRetain)
	{
		int total = chartRetain.mVote_yes + chartRetain.mVote_no - chartRetain.mVote_null;
		if (chartRetain.mVote_yes != 0)
			mSeries.add("YES " + " " + pourcentage(chartRetain.mVote_yes, total),
					chartRetain.mVote_yes);
		if (chartRetain.mVote_no != 0)
			mSeries.add("NO " + " " + pourcentage(chartRetain.mVote_no, total),
					chartRetain.mVote_no);
		mLabelVoteRemain.setText(String.valueOf(chartRetain.mVote_remain));
		mLabelVoteNull.setText(String.valueOf(chartRetain.mVote_null));
	}

}
