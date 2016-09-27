package com.me.app.periodofmonth.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.me.app.periodofmonth.R;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseRecyclerViewAdapter<V extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	/**
	 * 数据条目列表，对应于数据实体类的集合
	 */
	protected List dataItemList = new ArrayList();

	protected Context context;
	private LinearLayoutManager mLayoutManager;

	private static final int TYPE_HEADER = -3;
	private static final int TYPE_FOOTER = -2;
	private static final int TYPE_EMPTY = -1;
	protected static final int TYPE_NORMAL = 0;

	protected ArrayList<View> headerViews = new ArrayList<>();
	private int headerViewIndex;
	private View emptyView, footerView;
	protected boolean isFooterEnabled = true;

	public BaseRecyclerViewAdapter(Context context, LinearLayoutManager mLayoutManager) {
		super();
		this.context = context;
		this.mLayoutManager = mLayoutManager;
		registerDataObserver();
	}

	private void registerDataObserver() {
		registerAdapterDataObserver(adapterDataObserver);
	}

	public void unregisterDataObserver() {
		unregisterAdapterDataObserver(adapterDataObserver);
	}

	private RecyclerView.AdapterDataObserver adapterDataObserver = new RecyclerView.AdapterDataObserver() {

		@Override
		public void onChanged() {
			super.onChanged();
			try {
				if(getItemCount() >= mLayoutManager.findLastCompletelyVisibleItemPosition() + 1) {
					setFooterViewEnabled(false);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	};

	public void updateData(List list) {
		if(list == null) {
			this.dataItemList = new ArrayList();
		} else {
			this.dataItemList = list;
		}
		notifyDataSetChanged();
		if(emptyView != null){
			emptyView.setVisibility(View.VISIBLE);//一旦数据为空时显示emptyView
		}
	}

	public void addHeaderView(View view) {
		if (!headerViews.contains(view)) {
			headerViews.add(view);
		}
		notifyDataSetChanged();
		if(emptyView != null) {
			emptyView.setVisibility(View.VISIBLE);//一旦数据为空时显示emptyView
		}
	}

	protected <T>T getItem(int possition){
		return (T) dataItemList.get(possition);
	}

	public void setEmptyView(View emptyView) {
		this.emptyView = emptyView;
	}

	public void setFooterViewEnabled(boolean enabled) {
		if(!isFooterEnabled && enabled) {
			notifyItemInserted(getItemCount());
		}
		if(isFooterEnabled && !enabled) {
			notifyItemRemoved(getItemCount());
		}
		isFooterEnabled = enabled;
	}

	@Override
	public int getItemCount() {
		int dataListSize = dataItemList.size();
		if(dataListSize > 0) {
			return dataListSize + headerViews.size() + (isFooterEnabled ? 1 : 0);
		} else {
			return emptyView == null ? 0 : 1;//保证数据为空时显示emptyView
		}
	}

	@Override
	/**
	 * viewItemListPosition: 包含headerViews、footer和实体数据列表的位置
	 */
	public int getItemViewType(int viewItemListPosition) {
		if(dataItemList.size() == 0) {
			return TYPE_EMPTY;
		} else if (isFooterEnabled && viewItemListPosition + 1 == getItemCount()) {
			return TYPE_FOOTER;
		} else if (headerViews.size() > 0 && viewItemListPosition < headerViews.size()) {
			return TYPE_HEADER;
		} else if(getDataViewTypeCount() > 1) {
			return getDataViewType(viewItemListPosition - headerViews.size());
		} else {
			return TYPE_NORMAL;
		}
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == TYPE_FOOTER) {
			footerView = View.inflate(context, R.layout.recycler_view_footer, null);
			footerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			return new DefaultViewHolder(footerView);
		} else if (viewType == TYPE_HEADER) {
			int i = headerViewIndex++;
			headerViews.get(i).setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			return new DefaultViewHolder(headerViews.get(i));
		} else if (viewType == TYPE_EMPTY) {
			emptyView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, parent.getMeasuredHeight()));
			emptyView.setVisibility(View.INVISIBLE);//避免首次加载时看到emptyView
			return new DefaultViewHolder(emptyView);
		} else {
			return initViewHolder(parent, viewType);
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
		if (!(holder instanceof DefaultViewHolder)) {
			configViewHolder((V) holder, position - headerViews.size());
		}
	}

	/**
	 * 创建及初始化ViewHolder
	 * @param parent
	 * @param viewType
	 * @return
	 */
	public abstract V initViewHolder(ViewGroup parent, int viewType);

	/**
	 * 设置ViewHolder
	 * @param holder
	 * @param dataItemPosition 实体数据dataItemList的位置，不考虑headers和footer
	 * @return
	 */
	public abstract void configViewHolder(V holder, int dataItemPosition);

	public static class DefaultViewHolder extends RecyclerView.ViewHolder {

		public DefaultViewHolder(View view) {
			super(view);
		}

	}

	/**
	 * 实体数据itemView类型数目，默认为1，如果大于1，必须在子类重写
	 * @return
	 */
	protected int getDataViewTypeCount() {
		return 1;
	}

	/**
	 * 实体数据itemView类型id，如果itemView类型数目大于1，必须在子类重写，根据dataItemPosition获得特定的id
	 * @param dataItemPosition 实体数据dataItemList的位置，不考虑headers和footer
	 * @return
	 */
	protected int getDataViewType(int dataItemPosition) {
		return TYPE_NORMAL;
	}


	public void setLayoutManager(LinearLayoutManager layoutManager){
		this.mLayoutManager = layoutManager;
	}

	public void removeHeader(View header){
		if (headerViews != null){
			if (headerViews.remove(header)){
			}

		}
	}
}