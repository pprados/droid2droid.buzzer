package org.droid2droid.apps.buzzer.charts;

import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.droid2droid.apps.buzzer.R;
import org.droid2droid.apps.buzzer.choices.ChoiceMulti_ABC;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.LinearLayout;

public class ChartMulti_ABC extends AbstractChart
{
	public static class ChartRetain extends Retain
	{
		private int	mVote_a		= 0;

		private int	mVote_ab	= 0;

		private int	mVote_ac	= 0;

		private int	mVote_abc	= 0;

		private int	mVote_b		= 0;

		private int	mVote_bc	= 0;

		private int	mVote_c		= 0;

		@Override
		public void writeToParcel(Parcel dest, int flags)
		{
			super.writeToParcel(dest, flags);
			dest.writeInt(mVote_a);
			dest.writeInt(mVote_ab);
			dest.writeInt(mVote_ac);
			dest.writeInt(mVote_abc);
			dest.writeInt(mVote_b);
			dest.writeInt(mVote_bc);
			dest.writeInt(mVote_c);
		}

		@Override
		protected void readFromParcel(Parcel parcel)
		{
			super.readFromParcel(parcel);
			mVote_a = parcel.readInt();
			mVote_ab = parcel.readInt();
			mVote_ac = parcel.readInt();
			mVote_abc = parcel.readInt();
			mVote_b = parcel.readInt();
			mVote_bc = parcel.readInt();
			mVote_c = parcel.readInt();
		}

		public static final Parcelable.Creator<ChartRetain>	CREATOR	= new Creator<ChartMulti_ABC.ChartRetain>()
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
		mSeries = new CategorySeries("Choice Multi ABC Chart");
		updateValues(chartRetain);
		mChart = new GraphicalView(this, getChart());
		mMain.addView(mChart);
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		super.onReceive(context, intent);
		int result = intent.getIntExtra("result", -2);
		final ChartRetain chartRetain = getRetain();
		chartRetain.mVote_remain = intent.getIntExtra("pending", 0);
		switch (result)
		{
			case ChoiceMulti_ABC.CHOICE_A:
				chartRetain.mVote_a++;
				break;
			case ChoiceMulti_ABC.CHOICE_AB:
				chartRetain.mVote_ab++;
				break;
			case ChoiceMulti_ABC.CHOICE_AC:
				chartRetain.mVote_ac++;
				break;
			case ChoiceMulti_ABC.CHOICE_ABC:
				chartRetain.mVote_abc++;
				break;
			case ChoiceMulti_ABC.CHOICE_B:
				chartRetain.mVote_b++;
				break;
			case ChoiceMulti_ABC.CHOICE_BC:
				chartRetain.mVote_bc++;
				break;
			case ChoiceMulti_ABC.CHOICE_C:
				chartRetain.mVote_c++;
				break;
			case -2:
				break;
			default:
				chartRetain.mVote_null++;
				break;
		}
		if (chartRetain.mVote_remain == 0)
			mGoBack.setEnabled(true);
		mSeries.clear();
		updateValues(chartRetain);
		mChart.invalidate();
	}

	@Override
	public void onClick(View v)
	{
		mAsyncTask.cancel(true);
		finish();

	}

	@Override
	protected Retain newRetain()
	{
		return new ChartRetain();
	}

	private ChartRetain getRetain()
	{
		return (ChartRetain) mRetain;
	}

	private void updateValues(ChartRetain chartRetain)
	{
		int total = chartRetain.mVote_a + chartRetain.mVote_b + chartRetain.mVote_c
				+ +chartRetain.mVote_ab + chartRetain.mVote_bc + chartRetain.mVote_ac
				+ chartRetain.mVote_abc - chartRetain.mVote_null;
		if (chartRetain.mVote_a != 0)
			mSeries.add("A " + " " + pourcentage(chartRetain.mVote_a, total), chartRetain.mVote_a);
		if (chartRetain.mVote_b != 0)
			mSeries.add("B " + " " + pourcentage(chartRetain.mVote_b, total), chartRetain.mVote_b);
		if (chartRetain.mVote_c != 0)
			mSeries.add("C " + " " + pourcentage(chartRetain.mVote_c, total), chartRetain.mVote_c);
		if (chartRetain.mVote_ab != 0)
			mSeries.add("A & B " + " " + pourcentage(chartRetain.mVote_ab, total),
					chartRetain.mVote_ab);
		if (chartRetain.mVote_bc != 0)
			mSeries.add("B & C " + " " + pourcentage(chartRetain.mVote_bc, total),
					chartRetain.mVote_bc);
		if (chartRetain.mVote_ac != 0)
			mSeries.add("A & C " + " " + pourcentage(chartRetain.mVote_ac, total),
					chartRetain.mVote_ac);
		if (chartRetain.mVote_abc != 0)
			mSeries.add("A & B & C " + " " + pourcentage(chartRetain.mVote_abc, total),
					chartRetain.mVote_abc);
		mLabelVoteRemain.setText(String.valueOf(chartRetain.mVote_remain));
		mLabelVoteNull.setText(String.valueOf(chartRetain.mVote_null));
	}
}
