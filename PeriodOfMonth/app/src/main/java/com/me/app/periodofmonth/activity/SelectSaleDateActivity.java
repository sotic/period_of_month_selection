package com.me.app.periodofmonth.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.me.app.periodofmonth.R;
import com.me.app.periodofmonth.adapter.SelectableSaleDateListAdapter;
import com.me.app.periodofmonth.entity.SelectableSaleDateEntity;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SelectSaleDateActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView rv_selectable_dates;
    private SelectableSaleDateListAdapter adapter;
    private ArrayList<SelectableSaleDateEntity> selectableDatesFromNowOn = new ArrayList<>();//从现在到13个月之后的时间列表。默认初始化
    private ArrayList<SelectableSaleDateEntity> selectableDatesThreeMonthsBeforeNow;//现在之前三个月的时间列表，只有"现在有货"时初始化
    private ArrayList<SelectableSaleDateEntity> selectableDatesStartSale, selectableDatesEndSale;

    private LinearLayout ll_page_select_sale_date, ll_selected_start_sale_date, ll_selected_end_sale_date;
    private View bg_selected_dates_bottom;
    private TextView tv_selected_start_sale_date, tv_selected_end_sale_date, tv_select_dates_done;

    private SelectableSaleDateEntity selectedStartSaleDate, selectedEndSaleDate;
    private static final int SELECTING_DATE_START = 1;
    private static final int SELECTING_DATE_END = 2;
    private int selectingDate;

    private int startOffset, endOffset;
    private String startSaleDate, endSaleDate;
    private SelectableSaleDateEntity currentDateEntity;
    private Calendar calendar;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_select_sale_date);
        startOffset = getIntent().getIntExtra("start_offset", 0);
        endOffset = getIntent().getIntExtra("end_offset", 0);
        startSaleDate = getIntent().getStringExtra("start_sale_date");
        endSaleDate = getIntent().getStringExtra("end_sale_date");
        initView();
    }

    private void initView(){
        ll_page_select_sale_date = (LinearLayout) findViewById(R.id.ll_page_select_sale_date);
        ll_selected_start_sale_date = (LinearLayout) findViewById(R.id.ll_selected_start_sale_date);
        ll_selected_start_sale_date.setOnClickListener(this);
        ll_selected_end_sale_date = (LinearLayout) findViewById(R.id.ll_selected_end_sale_date);
        ll_selected_end_sale_date.setOnClickListener(this);
        bg_selected_dates_bottom = findViewById(R.id.bg_selected_dates_bottom);
        tv_selected_start_sale_date = (TextView) findViewById(R.id.tv_selected_start_sale_date);
        tv_selected_end_sale_date = (TextView) findViewById(R.id.tv_selected_end_sale_date);
        tv_select_dates_done = (TextView) findViewById(R.id.tv_select_dates_done);
        tv_select_dates_done.setOnClickListener(this);

        rv_selectable_dates = (RecyclerView) findViewById(R.id.rv_selectable_dates);
        rv_selectable_dates.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        rv_selectable_dates.setLayoutManager(mLayoutManager);
        rv_selectable_dates.setItemAnimator(new DefaultItemAnimator());
        adapter = new SelectableSaleDateListAdapter(this, mLayoutManager);
        adapter.setFooterViewEnabled(false);
        rv_selectable_dates.setAdapter(adapter);

        initSelectableDates(true);

        //页面打开时已经选择上市时间
        if(!TextUtils.isEmpty(startSaleDate)) {
            SelectableSaleDateEntity startSaleDateEntity = SelectableSaleDateEntity.convert2Obj(startSaleDate);
            if(shouldSetEditingDate(startSaleDateEntity)) {
                tv_selected_start_sale_date.setText(SelectableSaleDateEntity.convert2Str(startSaleDate));
                selectedStartSaleDate = SelectableSaleDateEntity.convert2Obj(startSaleDate);
                updateSelectableDateListEndSale(selectedStartSaleDate);
            }
        }

        //页面打开时已经选择下市时间
        if(!TextUtils.isEmpty(endSaleDate)) {
            SelectableSaleDateEntity endSaleDateEntity = SelectableSaleDateEntity.convert2Obj(endSaleDate);
            if(shouldSetEditingDate(endSaleDateEntity)) {
                tv_selected_end_sale_date.setText(SelectableSaleDateEntity.convert2Str(endSaleDate));
                selectedEndSaleDate = SelectableSaleDateEntity.convert2Obj(endSaleDate);
                updateSelectableDateListStartSale(selectedEndSaleDate);
            }
        }
    }

    /**
     * 在编辑模式下，判断是否需要设置已选择的日期，并更新日期列表
     * [发布供应场景(非品质撮合场景)]当前时间晚于已填写开始时间：开始时间按无效处理，点击进入后清空选项，重新选择时间
     * [发布供应场景(非品质撮合场景)]当前时间晚于已填写结束时间：结束时间按无效处理，点击进入后请空选项，重新选择时间
     * @param editingDateEntity
     * @return
     */
    private boolean shouldSetEditingDate(SelectableSaleDateEntity editingDateEntity) {
        return editingDateEntity != null && editingDateEntity.compareTo(currentDateEntity) >= 0;
    }

    private void initSelectableDates(final boolean isSync) {
        if(isSync) {
            initSelectableDateList();
        } else {
            new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... params) {
                    try {
                        URL url = new URL("http://www.baidu.com");//取得资源对象
                        URLConnection uc = url.openConnection();//生成连接对象
                        uc.connect(); //发出连接
                        long dateLong = uc.getDate();
                        calendar.setTimeInMillis(dateLong);
                        if(currentDateEntity != null) { //当前日期变量已经创建，判断与网络返回的日期是否相同
                            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                            if(currentDateEntity.year != calendar.get(Calendar.YEAR)
                                    || currentDateEntity.month != calendar.get(Calendar.MONTH) + 1
                                    || currentDateEntity.minPeriodOfMonth != (dayOfMonth == 31 ? 3 : (dayOfMonth - 1) / 10 + 1)) {
                                initSelectableDateList();
                                return true;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return false;
                }

                protected void onPostExecute(Boolean refreshDateList) {
                    if(refreshDateList && rv_selectable_dates.isShown() && adapter != null) {
                        if(selectingDate == SELECTING_DATE_START) {
                            adapter.updateData(selectableDatesStartSale, calendar);
                        } else if(selectingDate == SELECTING_DATE_END) {
                            adapter.updateData(selectableDatesEndSale, calendar);
                        }
                    }
                }

            }.execute();
        }
    }

    private synchronized void initSelectableDateList() {
        calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        currentDateEntity = new SelectableSaleDateEntity();
        currentDateEntity.year = currentYear;
        currentDateEntity.month = currentMonth;

        selectableDatesFromNowOn.clear();

        selectableDatesFromNowOn = SelectableSaleDateEntity.getSelectableDatesBy(calendar, startOffset, endOffset);

    }

    private void updateSelectableDateListStartSale(SelectableSaleDateEntity selectedEndSaleDate) {
        //根据选择的结束时间调整开始时间范围
        if(selectableDatesStartSale == null) {
            selectableDatesStartSale = new ArrayList<>();
        } else {
            selectableDatesStartSale.clear();
        }
        int index = selectableDatesFromNowOn.indexOf(selectedEndSaleDate);
        if(index > 0) {
            List<SelectableSaleDateEntity> subList = selectableDatesFromNowOn.subList(0, index);
            selectableDatesStartSale.addAll(subList);
        } else {//shouldn't happen
            selectableDatesStartSale.addAll(selectableDatesFromNowOn);
        }

        if(selectedEndSaleDate.selectedPeriod > 1) {//如果选择下市时间为某月中旬或下旬，则需要包含当月
            SelectableSaleDateEntity entity = new SelectableSaleDateEntity();
            entity.year = selectedEndSaleDate.year;
            entity.month = selectedEndSaleDate.month;
            entity.maxPeriodOfMonth = selectedEndSaleDate.selectedPeriod - 1;
            selectableDatesStartSale.add(entity);
        }

    }

    private void updateSelectableDateListEndSale(SelectableSaleDateEntity selectedStartDate) {
        //根据选择的开始时间调整结束时间范围
        if(selectableDatesEndSale == null) {
            selectableDatesEndSale = new ArrayList<>();
        } else {
            selectableDatesEndSale.clear();
        }
        int index = selectableDatesFromNowOn.indexOf(selectedStartDate);
        int listSize = selectableDatesFromNowOn.size();
        if(index >= 0) {
            List<SelectableSaleDateEntity> subList = selectableDatesFromNowOn.subList(index + 1, listSize);//如果选择上市时间为某月下旬，则不包含当月
            selectableDatesEndSale.addAll(subList);
        } else {
            selectableDatesEndSale.addAll(selectableDatesFromNowOn);
        }
        if(selectedStartDate.selectedPeriod < 3) {
            SelectableSaleDateEntity entity = new SelectableSaleDateEntity();
            entity.year = selectedStartDate.year;
            entity.month = selectedStartDate.month;
            entity.minPeriodOfMonth = selectedStartDate.selectedPeriod + 1;
            selectableDatesEndSale.add(0, entity);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_selected_start_sale_date:
                if(selectingDate == SELECTING_DATE_START && rv_selectable_dates.isShown()) {//再次点击，收起list
                    rv_selectable_dates.setVisibility(View.GONE);
                    bg_selected_dates_bottom.setVisibility(View.GONE);
                    ll_page_select_sale_date.setBackgroundResource(R.color.white);
                    tryShowingConfirmBtn();
                } else {
                    if(selectableDatesStartSale == null) {
                        selectableDatesStartSale = new ArrayList<>();
                        selectableDatesStartSale.addAll(selectableDatesFromNowOn);
                    }
                    adapter.updateData(selectableDatesStartSale, calendar);
                    rv_selectable_dates.setVisibility(View.VISIBLE);
                    ll_page_select_sale_date.setBackgroundResource(R.color.color_f2f2f2);
                    bg_selected_dates_bottom.setVisibility(View.VISIBLE);
                    tv_select_dates_done.setVisibility(View.GONE);
                    ll_selected_start_sale_date.setBackgroundResource(R.drawable.bg_green_stroke_with_corners_px_6);
                    ll_selected_end_sale_date.setBackgroundResource(R.drawable.bg_grey_stroke_with_corners_px_6);
                    bg_selected_dates_bottom.setBackgroundResource(R.drawable.bg_selected_dates_bottom_left);
                }
                selectingDate = SELECTING_DATE_START;
                break;
            case R.id.ll_selected_end_sale_date:
                if(TextUtils.isEmpty(startSaleDate)) {//无上市时间，下市时间不可点
                    Toast.makeText(this, "请首先选择上市时间", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(selectingDate == SELECTING_DATE_END && rv_selectable_dates.isShown()) {//再次点击，收起list
                    rv_selectable_dates.setVisibility(View.GONE);
                    bg_selected_dates_bottom.setVisibility(View.GONE);
                    ll_page_select_sale_date.setBackgroundResource(R.color.white);
                    tryShowingConfirmBtn();
                } else {
                    if(selectableDatesEndSale == null) {
                        selectableDatesEndSale = new ArrayList<>();
                        selectableDatesEndSale.addAll(selectableDatesFromNowOn);
                    }
                    adapter.updateData(selectableDatesEndSale, calendar);
                    rv_selectable_dates.setVisibility(View.VISIBLE);
                    ll_page_select_sale_date.setBackgroundResource(R.color.color_f2f2f2);
                    bg_selected_dates_bottom.setVisibility(View.VISIBLE);
                    tv_select_dates_done.setVisibility(View.GONE);
                    ll_selected_start_sale_date.setBackgroundResource(R.drawable.bg_grey_stroke_with_corners_px_6);
                    ll_selected_end_sale_date.setBackgroundResource(R.drawable.bg_green_stroke_with_corners_px_6);
                    bg_selected_dates_bottom.setBackgroundResource(R.drawable.bg_selected_dates_bottom_right);
                }
                selectingDate = SELECTING_DATE_END;
                break;
            case R.id.tv_select_dates_done:
                if(TextUtils.isEmpty(startSaleDate)) {
                    Toast.makeText(this, "请选择开始时间", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra("onSaleDate", startSaleDate);
                intent.putExtra("offSaleDate", endSaleDate);
                setResult(RESULT_OK,intent);
                finish();
                break;
            default:
                break;
        }
    }

    public void onDateSelected(final SelectableSaleDateEntity entity) {
        rv_selectable_dates.postDelayed(new Runnable() {
            @Override
            public void run() {
                rv_selectable_dates.setVisibility(View.GONE);
                bg_selected_dates_bottom.setVisibility(View.GONE);
                ll_page_select_sale_date.setBackgroundResource(R.color.white);
                if(selectingDate == SELECTING_DATE_START) {
                    tv_selected_start_sale_date.setText(SelectableSaleDateEntity.convert2Str(entity.month, entity.selectedPeriod));
                } else if(selectingDate == SELECTING_DATE_END) {
                    tv_selected_end_sale_date.setText(SelectableSaleDateEntity.convert2Str(entity.month, entity.selectedPeriod));
                }

                tryShowingConfirmBtn();
            }
        }, 30);

        if(selectingDate == SELECTING_DATE_START) {
            selectedStartSaleDate = null;
            startSaleDate = entity.year + "-" + (entity.month < 10 ? "0" : "") + entity.month + entity.selectedPeriod;
            //选择上市时间后，更新可选的下市时间列表
            if(selectableDatesEndSale == null || selectableDatesEndSale.size() == 0
                    || selectedStartSaleDate == null || entity.compareTo(selectedStartSaleDate) != 0) {
                updateSelectableDateListEndSale(entity);
            }
            selectedStartSaleDate = entity;
        } else if(selectingDate == SELECTING_DATE_END) {
            selectedEndSaleDate = null;
            endSaleDate = entity.year + "-" + (entity.month < 10 ? "0" : "") + entity.month + entity.selectedPeriod;
            //选择下市时间后，更新可选的上市时间列表
            if(selectableDatesStartSale == null || selectableDatesStartSale.size() == 0
                    || selectedEndSaleDate == null || entity.compareTo(selectedEndSaleDate) != 0) {
                updateSelectableDateListStartSale(entity);
            }
            selectedEndSaleDate = entity;
        }

    }

    private void tryShowingConfirmBtn() {
        if(!TextUtils.isEmpty(startSaleDate) && !TextUtils.isEmpty(endSaleDate)) {
            bg_selected_dates_bottom.setVisibility(View.GONE);
            tv_select_dates_done.setVisibility(View.VISIBLE);
        }
    }

    public static Intent getIntent2Me(Context context, int start_offset, int end_offset, String selected_start_sale_date, String selected_end_sale_date) {
        Intent intent = new Intent(context, SelectSaleDateActivity.class);
        Bundle bundle = new Bundle();
        intent.putExtra("start_offset", start_offset);
        intent.putExtra("end_offset", end_offset);
        intent.putExtra("selected_start_sale_date", selected_start_sale_date);
        intent.putExtra("selected_end_sale_date", selected_end_sale_date);
        intent.putExtras(bundle);
        return intent;
    }

}
