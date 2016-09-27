package com.me.app.periodofmonth.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.me.app.periodofmonth.R;
import com.me.app.periodofmonth.entity.SelectableSaleDateEntity;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_DATE = 109;
    private String startYearMonth, endYearMonth;

    private TextView tv_result;
    private EditText et_start_date_offset;
    private EditText et_end_date_offset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et_start_date_offset = (EditText) findViewById(R.id.et_start_date_offset);
        et_end_date_offset = (EditText) findViewById(R.id.et_end_date_offset);
        tv_result = (TextView) findViewById(R.id.tv_result);
        tv_result.setOnClickListener(this);
        Button btn_selection_confirm = (Button) findViewById(R.id.btn_selection_confirm);
        btn_selection_confirm.setOnClickListener(this);
        Button btn_confirm_last_to_next_year = (Button) findViewById(R.id.btn_confirm_last_to_next_year);
        btn_confirm_last_to_next_year.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_DATE:
                    startYearMonth = data.getStringExtra("onSaleDate");
                    endYearMonth = data.getStringExtra("offSaleDate");
                    tv_result.setText(SelectableSaleDateEntity.convert2StrWithYear(startYearMonth)
                            + (TextUtils.isEmpty(endYearMonth) ? "" : "~") + SelectableSaleDateEntity.convert2StrWithYear(endYearMonth));
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_selection_confirm: {
                if (TextUtils.isEmpty(et_start_date_offset.getText()) || TextUtils.isEmpty(et_end_date_offset.getText())) {
                    Toast.makeText(MainActivity.this, "input start and end", Toast.LENGTH_SHORT).show();
                    return;
                }
                int startOffset = Integer.parseInt(et_start_date_offset.getText().toString());
                int endOffset = Integer.parseInt(et_end_date_offset.getText().toString());
                startActivityForResult(SelectSaleDateActivity.getIntent2Me(MainActivity.this, startOffset, endOffset, null, null), REQUEST_CODE_DATE);
                break;
            }
            case R.id.btn_confirm_last_to_next_year: {
                Calendar calendar = Calendar.getInstance();
                int currentMonth = calendar.get(Calendar.MONTH) + 1;
                int currentPeriodOfMonth = SelectableSaleDateEntity.getCurrentPeriodOfMonth(calendar);
                int startOffset = -12 * 3 - (currentMonth - 1) * 3 - (currentPeriodOfMonth - 1);//上一年全部旬数+当年已经过旬数
                int endOffset = (12 - currentMonth) * 3 + (3 - currentPeriodOfMonth) + 12 * 3;//当年剩余旬数+下一年全部旬数
                startActivityForResult(SelectSaleDateActivity.getIntent2Me(MainActivity.this, startOffset, endOffset, startYearMonth, endYearMonth), REQUEST_CODE_DATE);
                break;
            }
            case R.id.tv_result:
                if(!TextUtils.isEmpty(endYearMonth) && !TextUtils.isEmpty(endYearMonth)) {
                    startActivityForResult(SelectSaleDateActivity.getIntent2Me(MainActivity.this, 0, 0, startYearMonth, endYearMonth), REQUEST_CODE_DATE);
                }
                break;
        }
    }
}
