package com.me.app.periodofmonth.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.me.app.periodofmonth.R;
import com.me.app.periodofmonth.activity.SelectSaleDateActivity;
import com.me.app.periodofmonth.entity.SelectableSaleDateEntity;

import java.util.Calendar;
import java.util.List;

public class SelectableSaleDateListAdapter extends BaseRecyclerViewAdapter {

	private Activity activity;
	private Calendar calendar;

	public SelectableSaleDateListAdapter(Context context, LinearLayoutManager mLayoutManager) {
		super(context, mLayoutManager);
		this.activity = (Activity) context;
	}

	public void updateData(List list, Calendar calendar) {
		super.updateData(list);
		this.calendar = calendar;
	}

	@Override
	public RecyclerView.ViewHolder initViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_selectable_sale_date, null);
		view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		return new SelectableDateViewHolder(view);
	}

	@Override
	public void configViewHolder(RecyclerView.ViewHolder holder, final int dataItemPosition) {
		final SelectableSaleDateEntity entity = (SelectableSaleDateEntity) dataItemList.get(dataItemPosition);
		SelectableDateViewHolder viewHolder = (SelectableDateViewHolder) holder;

		if(dataItemPosition == 0 || entity.month == 1) {
			viewHolder.tv_selectable_date_year.setVisibility(View.VISIBLE);
			int currentYear = calendar.get(Calendar.YEAR);
			if(entity.year - currentYear == 1) {
				viewHolder.tv_selectable_date_year.setText("明年");
			} else if (currentYear - entity.year == 1) {
				viewHolder.tv_selectable_date_year.setText("去年");
			} else if (currentYear == entity.year){
				viewHolder.tv_selectable_date_year.setText("今年");
			} else {
				viewHolder.tv_selectable_date_year.setText(entity.year+"年");
			}
		} else {
			viewHolder.tv_selectable_date_year.setVisibility(View.GONE);
		}
		viewHolder.tv_selectable_date_month.setText(entity.month+"月");
		viewHolder.tv_selectable_date_month_p1.setEnabled(entity.minPeriodOfMonth == 1 && entity.maxPeriodOfMonth >= 1);
		viewHolder.tv_selectable_date_month_p2.setEnabled(entity.minPeriodOfMonth <= 2 && entity.maxPeriodOfMonth >= 2);
		viewHolder.tv_selectable_date_month_p3.setEnabled(entity.minPeriodOfMonth <= 3 && entity.maxPeriodOfMonth == 3);

		viewHolder.tv_selectable_date_month_p1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				entity.selectedPeriod = 1;
				((SelectSaleDateActivity)activity).onDateSelected(entity);
			}
		});
		viewHolder.tv_selectable_date_month_p2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				entity.selectedPeriod = 2;
				((SelectSaleDateActivity)activity).onDateSelected(entity);
			}
		});
		viewHolder.tv_selectable_date_month_p3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				entity.selectedPeriod = 3;
				((SelectSaleDateActivity)activity).onDateSelected(entity);
			}
		});

		viewHolder.tv_list_bottom_hint.setVisibility(dataItemPosition == dataItemList.size() -1 ? View.INVISIBLE : View.GONE);
	}

	private class SelectableDateViewHolder extends RecyclerView.ViewHolder {
		public TextView tv_selectable_date_year;
		public TextView tv_selectable_date_month;
		public TextView tv_selectable_date_month_p1;
		public TextView tv_selectable_date_month_p2;
		public TextView tv_selectable_date_month_p3;
		public TextView tv_list_bottom_hint;

		public SelectableDateViewHolder(View convertView) {
			super(convertView);
			tv_selectable_date_year = (TextView) convertView.findViewById(R.id.tv_selectable_date_year);
			tv_selectable_date_month = (TextView) convertView.findViewById(R.id.tv_selectable_date_month);
			tv_selectable_date_month_p1 = (TextView) convertView.findViewById(R.id.tv_selectable_date_month_p1);
			tv_selectable_date_month_p2 = (TextView) convertView.findViewById(R.id.tv_selectable_date_month_p2);
			tv_selectable_date_month_p3 = (TextView) convertView.findViewById(R.id.tv_selectable_date_month_p3);
			tv_list_bottom_hint = (TextView) convertView.findViewById(R.id.tv_list_bottom_hint);
		}

	}
}