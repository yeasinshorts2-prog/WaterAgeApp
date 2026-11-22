package com.waterage.app;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AgeCalculatorActivity extends AppCompatActivity {

    private Calendar birthCal = Calendar.getInstance();
    private Calendar currentCal = Calendar.getInstance();
    private boolean isBirthDateSelected = false;

    private Button btnBirthDate, btnCurrentDate, btnCalculateAge;
    private LinearLayout layoutAgeResult;
    private TextView tvAgeYMD, tvTotalDays;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd / MM / yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_age_calculator);

        btnBirthDate = findViewById(R.id.btnBirthDate);
        btnCurrentDate = findViewById(R.id.btnCurrentDate);
        btnCalculateAge = findViewById(R.id.btnCalculateAge);
        layoutAgeResult = findViewById(R.id.layoutAgeResult);
        tvAgeYMD = findViewById(R.id.tvAgeYMD);
        tvTotalDays = findViewById(R.id.tvTotalDays);

        // বর্তমান তারিখে আজকের তারিখ দেখানো
        btnCurrentDate.setText(sdf.format(currentCal.getTime()));

        btnBirthDate.setOnClickListener(v -> showDatePicker(birthCal, true));
        btnCurrentDate.setOnClickListener(v -> showDatePicker(currentCal, false));
        btnCalculateAge.setOnClickListener(v -> calculateAge());
    }

    private void showDatePicker(Calendar calendar, boolean isBirth) {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth, 0, 0, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    if (isBirth) {
                        isBirthDateSelected = true;
                        btnBirthDate.setText(sdf.format(calendar.getTime()));
                    } else {
                        btnCurrentDate.setText(sdf.format(calendar.getTime()));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void calculateAge() {
        if (!isBirthDateSelected) {
            Toast.makeText(this, "অনুগ্রহ করে প্রথমে জন্ম তারিখ সিলেক্ট করুন", Toast.LENGTH_SHORT).show();
            return;
        }

        // সময় ০ করে শুধু তারিখ হিসাব করা
        Calendar start = (Calendar) birthCal.clone();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = (Calendar) currentCal.clone();
        end.set(Calendar.HOUR_OF_DAY, 0);
        end.set(Calendar.MINUTE, 0);
        end.set(Calendar.SECOND, 0);
        end.set(Calendar.MILLISECOND, 0);

        if (start.after(end)) {
            Toast.makeText(this, "জন্ম তারিখ বর্তমান তারিখের পরে হতে পারবে না!", Toast.LENGTH_SHORT).show();
            return;
        }

        // মোট দিন হিসাব করা
        long diffMillis = end.getTimeInMillis() - start.getTimeInMillis();
        long totalDays = TimeUnit.MILLISECONDS.toDays(diffMillis);

        // বছর, মাস, দিন হিসাব করা
        int startYear = start.get(Calendar.YEAR);
        int startMonth = start.get(Calendar.MONTH);
        int startDay = start.get(Calendar.DAY_OF_MONTH);

        int endYear = end.get(Calendar.YEAR);
        int endMonth = end.get(Calendar.MONTH);
        int endDay = end.get(Calendar.DAY_OF_MONTH);

        int years = endYear - startYear;
        int months = endMonth - startMonth;
        int days = endDay - startDay;

        if (days < 0) {
            months--;
            Calendar prevMonth = (Calendar) end.clone();
            prevMonth.add(Calendar.MONTH, -1);
            days += prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
        }

        if (months < 0) {
            years--;
            months += 12;
        }

        tvAgeYMD.setText(years + " বছর, " + months + " মাস, " + days + " দিন");
        tvTotalDays.setText(totalDays + " দিন");
        layoutAgeResult.setVisibility(View.VISIBLE);
    }
}