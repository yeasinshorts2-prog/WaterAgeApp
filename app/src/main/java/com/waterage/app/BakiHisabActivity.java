package com.waterage.app;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BakiHisabActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "BakiHisabPrefs";
    private static final String KEY_BAKI_LIST = "baki_list";

    private List<BakiModel> bakiList = new ArrayList<>();
    private BakiAdapter adapter;
    private Gson gson = new Gson();
    private TextView tvTotalBaki;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baki_hisab);

        tvTotalBaki = findViewById(R.id.tvTotalBaki);
        ListView listView = findViewById(R.id.listViewBaki);
        FloatingActionButton fabAddBaki = findViewById(R.id.fabAddBaki);

        loadBakiList();
        updateTotalBaki();

        adapter = new BakiAdapter();
        listView.setAdapter(adapter);

        fabAddBaki.setOnClickListener(v -> showAddBakiDialog());
    }

    private void showAddBakiDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_baki, null);

        EditText etTitle = dialogView.findViewById(R.id.etBakiTitle);
        EditText etAmount = dialogView.findViewById(R.id.etBakiAmount);
        RadioButton rbJoma = dialogView.findViewById(R.id.rbJoma);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("সেভ করুন", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String amountStr = etAmount.getText().toString().trim();

                    if (title.isEmpty() || amountStr.isEmpty()) {
                        Toast.makeText(this, "বিবরণ এবং টাকার পরিমাণ লিখুন!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double amount;
                    try {
                        amount = Double.parseDouble(amountStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "সঠিক টাকার পরিমাণ লিখুন!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean isPaid = rbJoma.isChecked();
                    String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
                    int id = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);

                    BakiModel model = new BakiModel(id, title, amount, currentDate, isPaid);
                    bakiList.add(0, model); // নতুন হিসাব সবার উপরে দেখাবে
                    saveBakiList();
                    updateTotalBaki();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "হিসাব সেভ হয়েছে!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("বাতিল", null)
                .show();
    }

    private void updateTotalBaki() {
        double total = 0;
        for (BakiModel item : bakiList) {
            if (item.isPaid) {
                total -= item.amount;
            } else {
                total += item.amount;
            }
        }

        String formattedAmount = (total == (long) total)
                ? String.format(Locale.getDefault(), "%d", (long) total)
                : String.format(Locale.getDefault(), "%.2f", total);

        tvTotalBaki.setText(formattedAmount + " টাকা");
        if (total > 0) {
            tvTotalBaki.setTextColor(Color.parseColor("#FF6B6B")); // লাল (বাকি আছে)
        } else {
            tvTotalBaki.setTextColor(Color.parseColor("#2EF2A1")); // সবুজ (বাকি নেই বা জমা আছে)
        }
    }

    private void saveBakiList() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(KEY_BAKI_LIST, gson.toJson(bakiList)).apply();
    }

    private void loadBakiList() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String json = prefs.getString(KEY_BAKI_LIST, null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<BakiModel>>() {}.getType();
            List<BakiModel> loaded = gson.fromJson(json, type);
            if (loaded != null) {
                bakiList = loaded;
            }
        }
    }

    private class BakiAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return bakiList.size();
        }

        @Override
        public Object getItem(int position) {
            return bakiList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return bakiList.get(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(BakiHisabActivity.this).inflate(R.layout.item_baki, parent, false);
            }

            BakiModel item = bakiList.get(position);

            TextView tvTitle = convertView.findViewById(R.id.tvBakiTitle);
            TextView tvAmount = convertView.findViewById(R.id.tvBakiAmount);
            TextView tvDate = convertView.findViewById(R.id.tvBakiDate);
            Button btnDelete = convertView.findViewById(R.id.btnDeleteBaki);

            tvTitle.setText(item.title);

            String amtStr = (item.amount == (long) item.amount)
                    ? String.format(Locale.getDefault(), "%d", (long) item.amount)
                    : String.format(Locale.getDefault(), "%.2f", item.amount);

            if (item.isPaid) {
                tvAmount.setText("- " + amtStr + " টাকা");
                tvAmount.setTextColor(Color.parseColor("#2EF2A1"));
                tvDate.setText("তারিখ: " + item.date + " | জমা");
            } else {
                tvAmount.setText("+ " + amtStr + " টাকা");
                tvAmount.setTextColor(Color.parseColor("#FF6B6B"));
                tvDate.setText("তারিখ: " + item.date + " | বাকি");
            }

            btnDelete.setOnClickListener(v -> {
                bakiList.remove(position);
                saveBakiList();
                updateTotalBaki();
                notifyDataSetChanged();
                Toast.makeText(BakiHisabActivity.this, "হিসাব মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show();
            });

            return convertView;
        }
    }
}
