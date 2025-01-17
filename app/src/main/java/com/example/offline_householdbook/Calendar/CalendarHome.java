package com.example.offline_householdbook.Calendar;

import static com.example.offline_householdbook.MainHome.getCurrentDate;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.offline_householdbook.R;
import com.example.offline_householdbook.ReportWidget;
import com.example.offline_householdbook.db.DBHelper;
import com.example.offline_householdbook.db.FinancialRecord;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.ChipGroup;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CalendarHome extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private RecyclerView recyclerView;
    private FinancialRecordAdapter adapter;
    private DBHelper dbHelper;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_home);

        // DBHelper 초기화
        dbHelper = new DBHelper(getApplicationContext());

        // CalendarView 초기화 및 날짜 선택 리스너 설정
        calendarView = findViewById(R.id.calendarView);
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            String selectedDate = date.getDate().toString();  // 날짜를 "yyyy-MM-dd" 형식으로 변환
            loadRecordsForSelectedDate(selectedDate);
        });
        // 메서드 이름은 sql문+테이블이름(+~)
//        // insertFinancialRecord는 FinancialRecord객체를 생성하여 전달하면 됨
//        dbHelper.insertFinancialRecord(new FinancialRecord("2024-11-14", "문구", 10000, "메모"));
//        dbHelper.insertFinancialRecord(new FinancialRecord("2024-11-13", "교통", -10000, "메모"));
//        dbHelper.insertFinancialRecord(new FinancialRecord("2024-11-12", "취미", -10000, "메모"));
//        dbHelper.insertFinancialRecord(new FinancialRecord("2024-11-11", "외식", 10000, "메모"));
//        dbHelper.insertFinancialRecord(new FinancialRecord("2024-11-10", "게임", -10000, "메모"));
//        dbHelper.insertFinancialRecord(new FinancialRecord("2024-11-15", "휴가", 10000, "메모"));

        // RecyclerView 초기화 및 어댑터 설정
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new FadeOutItemAnimator()); // CustomItemAnimator 적용
        adapter = new FinancialRecordAdapter(new ArrayList<>());

        adapter.setOnItemClickListener(new FinancialRecordAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FinancialRecord record) {
            }

            @Override
            public void onDeleteClick(FinancialRecord record) {
                deleteRecordForSelectedDate(record);
            }
        });

        recyclerView.setAdapter(adapter);

        textView = findViewById(R.id.res_txt); // TextView 초기화

        // 이미지 버튼 클릭 리스너 설정
        findViewById(R.id.imageView_Insert).setOnClickListener(v -> showBottomSheetDialog());

        updateCalendarDecorators();
        calendarView.addDecorator(new CombinedDecorator(dbHelper, getCurrentDate()));
    }

    private void loadRecordsForSelectedDate(String date) {
        ArrayList<FinancialRecord> records = dbHelper.selectFinancialRecordsByDate(date);

        int totalAmount = 0;
        for (FinancialRecord record : records) {
            totalAmount += record.getAmount();
        }

        // 금액 포맷 업데이트
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("ko", "KR"));
        String formattedAmount = formatter.format(totalAmount);
        textView.setText(totalAmount >= 0 ? "+" + formattedAmount : formattedAmount);
        textView.setTextColor(getResources().getColor(
                totalAmount >= 0 ? android.R.color.holo_blue_dark : android.R.color.holo_red_dark
        ));

        // 어댑터 데이터 업데이트
        adapter.updateData(records);
    }


    // BottomSheetDialog 띄우는 메서드
    private void showBottomSheetDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.calendar_bottom_sheet, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // ChipGroup
        ChipGroup chipGroup = bottomSheetView.findViewById(R.id.chipGroup);
        Spinner spinner = bottomSheetView.findViewById(R.id.CategorySpin);
        //처음에 초기화가 필요, 지출 상태로 초기화
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(),
                R.array.expense_spinner_items, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        final int[] i = {0};// 0은 지출 1은 수입
        // 선택된 chip의 id를 가져와서 스피너 초기화
        chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup chipGroup, int checkedId) {
                if (checkedId == -1) {
                    if(i[0] == 0)
                        chipGroup.check(R.id.chipExpense);
                    else
                        chipGroup.check(R.id.chipIncome);
                }

                if (checkedId == R.id.chipExpense) {
                    // "지출" 선택 시 기존 spinner의 설정 유지
                    i[0] = 0;
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(),
                            R.array.expense_spinner_items, android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);
                } else if (checkedId == R.id.chipIncome) {
                    // "수입" 선택 시 spinner 초기화 (아이템 1개만 포함)
                    i[0] = 1;
                    chipGroup.check(R.id.chipIncome);
                    ArrayAdapter<CharSequence> incomeAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
                            R.array.income_spinner_items, android.R.layout.simple_spinner_item);
                    incomeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(incomeAdapter);
                }
            }
        });

        // 캘린더 뷰
        String selectedDate = getSelectedDate(calendarView);
        //날짜 표시 덱스트 뷰
        TextView textView = bottomSheetView.findViewById(R.id.select_dayText);
        textView.setText(selectedDate);

        // 금액과 메모 입력을 위한 EditText
        EditText moneyEdit = bottomSheetView.findViewById(R.id.moneyEdit);
        EditText textMemo = bottomSheetView.findViewById(R.id.memoEdit);

        // 버튼 클릭 리스너
        Button btnAction1 = bottomSheetView.findViewById(R.id.bottomBtn);
        btnAction1.setOnClickListener(v -> {
            // 사용자 입력 값 가져오기
            String date = selectedDate;
            String category = spinner.getSelectedItem().toString();
            String moneyString = moneyEdit.getText().toString();
            String memo = textMemo.getText().toString();

            if (selectedDate == null) {
                Toast.makeText(this, "날짜를 선택해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (moneyString.isEmpty()) {
                Toast.makeText(this, "금액을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            int money;
            try {
                money = Integer.parseInt(moneyString);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "유효한 금액을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            if(money <= 0 ){
                Toast.makeText(this, "0 혹은 그 이하의 금액은 입력할 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            if(chipGroup.getCheckedChipId() == R.id.chipExpense)
                money = -money;

            // FinancialRecord 생성 및 DB 저장
            FinancialRecord record = new FinancialRecord(date, category, money, memo);
            dbHelper.insertFinancialRecord(record);

            loadRecordsForSelectedDate(date);
            // 캘린더 데코레이터 갱신
            updateCalendarDecorators();

            //위젯 업데이트 함수 호출
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());

            // 특정 위젯 프로바이더의 위젯 ID 가져오기
            ComponentName widgetComponent = new ComponentName(getApplicationContext(), ReportWidget.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(widgetComponent);

            // 각 위젯 ID를 이용해 updateAppWidget 호출
            for (int appWidgetId : appWidgetIds) {
                ReportWidget.updateAppWidget(getApplicationContext(), appWidgetManager, appWidgetId);
            }

            // BottomSheetDialog 닫기
            bottomSheetDialog.dismiss();
        });


        bottomSheetDialog.show();
    }

    private String getSelectedDate(MaterialCalendarView calendarView) {
        CalendarDay selectedDay = calendarView.getSelectedDate(); // CalendarDay 객체 반환
        if (selectedDay != null) {
            int year = selectedDay.getYear();   // 연도
            int month = selectedDay.getMonth(); // 월 (0부터 시작)
            int day = selectedDay.getDay();     // 일

            // 월이 0부터 시작하므로 +1 해줘야 실제 월이 됩니다.
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return sdf.format(new Date(year - 1900, month-1, day)); // Date 객체로 변환 후 포맷
        }
        return null; // 선택된 날짜가 없을 경우
    }

    public void saveRecordForSelectedDate(String date, String categoryName,int amount, String memo){
        FinancialRecord record= new FinancialRecord(date, categoryName,amount,memo);
        dbHelper.insertFinancialRecord(record);
    }

    public void updateRecordForSelectedDate(int id,  String date, String categoryName,int amount, String memo) {
        FinancialRecord record= new FinancialRecord(date, categoryName,amount,memo);
        dbHelper.updateFinancialRecord(id, record);
    }

    public void deleteRecordForSelectedDate(FinancialRecord record) {
        int position = adapter.getData().indexOf(record);
        if (position != -1) {
            // 먼저 화면에서만 아이템을 제거하고 애니메이션 실행
            adapter.getData().remove(position);
            adapter.notifyItemRemoved(position);

            // 애니메이션이 완료된 후에 실제 DB 삭제 및 데이터 갱신
            recyclerView.postDelayed(() -> {
                // DB에서 데이터 삭제
                dbHelper.deleteFinancialRecord(record.get_id());
                updateCalendarDecorators();
                // 현재 선택된 날짜의 모든 데이터를 다시 로드하기 전에 약간의 지연
                recyclerView.postDelayed(() -> {
                    String selectedDate = getSelectedDate(calendarView);
                    loadRecordsForSelectedDate(selectedDate);
                }, 50);
            }, 350);
        }

    }

    private void updateCalendarDecorators() {
        calendarView.removeDecorators(); // 기존 데코레이터 제거

        ArrayList<String> allDates = dbHelper.getAllDates(); // DB의 모든 날짜 가져오기
        for (String date : allDates) {
            // CombinedDecorator를 이용해 날짜 꾸미기
            calendarView.addDecorator(new CombinedDecorator(dbHelper, date));
        }
    }

}