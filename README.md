# period_of_month_selection
根据日期时间段，对每个月的旬进行选择<br>
输入：<br>
start_offset: 距离当前日期的旬数，可正可负。如-2表示距离当前日期的前2个旬，9表示距离当前日期的后9个旬；假设当前日期为9月下旬，则-2表示从9月上旬，9表示12月下旬，0表示当前日期所在的旬及9月下旬。<br>
end_offset: 定义同start_offset，但end_offset必须大于start_offset。<br>
输出：<br>
在start_offset和end_offset区间内的所有可选的旬。<br>
