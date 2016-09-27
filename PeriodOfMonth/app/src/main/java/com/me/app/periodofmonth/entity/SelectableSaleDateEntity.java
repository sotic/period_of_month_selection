package com.me.app.periodofmonth.entity;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SelectableSaleDateEntity {
    //periodOfMonth: 旬

    private static String patternStr = "^(\\d{4})-(\\d{2})(\\d)";
    private static Pattern pattern = Pattern.compile(patternStr);
    public int year;
    public int month;
    public int minPeriodOfMonth = 1;//可选的最小旬，默认为1
    public int maxPeriodOfMonth = 3;//可选的最大旬，默认为3
    public int selectedPeriod;

    /**
     *
     * @param current 当前时间的Calendar对象
     * @param startOffset 相对当前时间偏移量，以旬为单位 如-1代表当前时间的前一旬
     * @param endOffset 相对当前时间偏移量，以旬为单位 如+37代表当前时间之后(12*3+1)旬，即一年加一旬
     * @return
     */
    public static ArrayList<SelectableSaleDateEntity> getSelectableDatesBy(Calendar current, int startOffset, int endOffset) {
        if(current == null || startOffset >= endOffset) {
            return null;
        }

        int currentYear = current.get(Calendar.YEAR);
        int currentMonth = current.get(Calendar.MONTH) + 1;
        int currentPeriodOfMonth = getCurrentPeriodOfMonth(current);//计算旬

        ArrayList<SelectableSaleDateEntity> result = new ArrayList<>();
        int monthSteps; //需要显示的月数量
        int startMonthOffset;//第一个月的偏移量，即第一个显示的月份距现在时间相差几个月
        int totalPeriod = endOffset - startOffset + 1;//总旬数
        int startPeriod = (currentPeriodOfMonth + startOffset % 3) % 3;//起始旬
        if(startPeriod == 0) {
            startPeriod = 3;
        }
        int endPeriod = (currentPeriodOfMonth + endOffset % 3) % 3;//结束旬
        if(endPeriod == 0) {
            endPeriod = 3;
        }
        //计算月数
        monthSteps = getMonthSteps(totalPeriod, startPeriod);
        //计算第一个显示的月份的偏移量，可能为负数
        startMonthOffset = getStartMonthOffset(startOffset, currentPeriodOfMonth);

        for(int i = 0; i < monthSteps; i++) {//从当前时间开始的monthSteps个月
            int year = currentYear;
            int month = (currentMonth + startMonthOffset + i) % 12;
            if(month <= 0) {
                month = 12 + month;
            }
            if(currentMonth + startMonthOffset + i > 12) {
                year++;
            } else if(currentMonth + startMonthOffset + i <= 0) {
                year--;
            }
            SelectableSaleDateEntity entity = new SelectableSaleDateEntity();
            entity.year = year;
            entity.month = month;
            if(i == 0) {//第一个月
                if (startPeriod == 3) {
                    entity.minPeriodOfMonth = 3;
                } else {
                    entity.minPeriodOfMonth = startPeriod;
                }
                entity.maxPeriodOfMonth = 3;
            } else if(i == monthSteps - 1) {//最后一月
                if(endPeriod == 3) {
                    entity.maxPeriodOfMonth = 3;
                } else {
                    entity.maxPeriodOfMonth = endPeriod;
                }
                entity.minPeriodOfMonth = 1;
            } else {//其他月份
                entity.minPeriodOfMonth = 1;
                entity.maxPeriodOfMonth = 3;
            }
            result.add(entity);
        }
        return result;
    }

    public static int getCurrentPeriodOfMonth(Calendar current) {
        int currentDayOfMonth = current.get(Calendar.DAY_OF_MONTH);
        return currentDayOfMonth == 31 ? 3 : (currentDayOfMonth - 1) / 10 + 1;
    }

    private static int getMonthSteps(int totalPeriod, int startPeriod) {
        int monthSteps;
        monthSteps = 1 //第一个月
                + (totalPeriod - (4 - startPeriod)) / 3 //除了第一个月所包含的旬数，剩余旬数需要的月数；(4 - startPeriod)为第一个月包含的旬数
                + ((totalPeriod - (4 - startPeriod)) % 3 > 0 ? 1 : 0); //如不能整除，则加1个月
        return monthSteps;
    }

    private static int getStartMonthOffset(int startOffset, int currentPeriodOfMonth) {
        int startMonthOffset;
        if (startOffset + currentPeriodOfMonth <= 3 && startOffset + currentPeriodOfMonth > 0) {
            startMonthOffset = 0;
        } else {
            startMonthOffset = (Math.abs(startOffset) - (currentPeriodOfMonth - 1)) / 3
                    + ((Math.abs(startOffset) - (currentPeriodOfMonth - 1)) % 3 == 0 ? 0 : 1);
        }
        return startOffset >= 0 ? startMonthOffset : -startMonthOffset;
    }

    public static String convert2Str(int month, int selectedPeriod) {
        String periodOfMonth;
        if (selectedPeriod == 1) {
            periodOfMonth = "上旬";
        } else if (selectedPeriod == 2) {
            periodOfMonth = "中旬";
        } else {
            periodOfMonth = "下旬";
        }
        return month + "月" + periodOfMonth;
    }

    /**
     * @param apiParam: "2016-073"
     * @return "7月上旬"
     */
    public static String convert2Str(String apiParam) {
        String result = "";
        if (!TextUtils.isEmpty(apiParam)) {
            Matcher m = pattern.matcher(apiParam);
            if (m.find()) {
                String month = m.group(2);
                month = (month.startsWith("0") ? month.substring(1) : month) + "月";
                String periodOfMonth;
                if (m.group(3).equals("1")) {
                    periodOfMonth = "上旬";
                } else if (m.group(3).equals("2")) {
                    periodOfMonth = "中旬";
                } else {
                    periodOfMonth = "下旬";
                }
                result = month + periodOfMonth;
            }
        }
        return result;
    }


    /**
     * @param apiParam "2016-011"
     * @return 今年1月上旬
     */
    public static String convert2StrWithYear(String apiParam) {
        String result = "";
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        if (!TextUtils.isEmpty(apiParam)) {
            Matcher m = pattern.matcher(apiParam);
            if (m.find()) {
                int year;
                String yearStr;
                try {
                    year = Integer.parseInt(m.group(1));
                } catch (Exception e) {
                    year = currentYear;
                }
                if (currentYear > year) {
                    yearStr = "去年";
                } else if (currentYear == year) {
                    yearStr = "今年";
                } else {
                    yearStr = "明年";
                }
                result = yearStr + convert2Str(apiParam);
            }
        }

        return result;
    }

    /**
     * @param apiParam: "2016-073"
     * @return SelectableSaleDateEntity实例
     */
    @Nullable
    public static SelectableSaleDateEntity convert2Obj(String apiParam) {
        if (TextUtils.isEmpty(apiParam)) {
            return null;
        }
        SelectableSaleDateEntity entity = null;
        Matcher m = pattern.matcher(apiParam);
        if (m.find()) {
            entity = new SelectableSaleDateEntity();
            entity.year = Integer.parseInt(m.group(1));
            entity.month = Integer.parseInt(m.group(2));
            entity.selectedPeriod = Integer.parseInt(m.group(3));
        }
        return entity;
    }

    public int compareTo(SelectableSaleDateEntity another) {
        if (another == null) return 1;
        if (this.year > another.year) return 1;
        if (this.equals(another) && this.selectedPeriod == another.selectedPeriod) return 0;
        return (this.year < another.year || this.month < another.month
                || this.month == another.month && this.selectedPeriod < another.selectedPeriod) ? -1 : 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SelectableSaleDateEntity that = (SelectableSaleDateEntity) o;

        if (year != that.year) return false;
        if (month != that.month) return false;
        return true;

    }

    @Override
    public int hashCode() {
        int result = year;
        result = 31 * result + month;
        return result;
    }

    @Override
    public String toString() {
        return year + "年" + month + "月" + selectedPeriod + "旬 (minPrd" + minPeriodOfMonth + ")";
    }

}
