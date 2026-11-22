package com.waterage.app;

import android.app.AlertDialog;
import android.content.Intent;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BakiHisabActivity extends AppCompatActivity {

    private List<BakiModel> bakiList = new ArrayList<>();
    private BakiAdapter adapter;
    private TextView tvTotalBaki;
    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baki_hisab);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // লগইন করা না থাকলে লগইন পেজে পাঠাবে
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // প্রতিটি ইউজারের জন্য আলাদা অনলাইন ডেটাবেজ ফোল্ডার
        dbRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.getUid())
                .child("baki_list");

        TextView tvBakiHeader = findViewById(R.id.tvBakiHeader);
        tvTotalBaki = findViewById(R.id.tvTotalBaki);
        ListView listView = findViewById(R.id.listViewBaki);
        FloatingActionButton fabAddBaki = findViewById(R.id.fabAddBaki);

        adapter = new BakiAdapter();
        listView.setAdapter(adapter);

        // হেডারে ক্লিক করলে লগআউট অপশন দেখাবে
        tvBakiHeader.setOnClickListener(v -> showLogoutDialog(currentUser.getEmail()));

        fabAddBaki.setOnClickListener(v -> showAddBakiDialog());

        // অনলাইন থেকে সব হিসাব অটোমেটিক লোড করা
        loadBakiFromCloud();
    }

    private void showLogoutDialog(String email) {
        new AlertDialog.Builder(this)
                .setTitle("অ্যাকাউন্ট")
                .setMessage("লগইন করা জিমেইল: " + email + "\n\nআপনি কি লগআউট করতে চান?")
                .setPositiveButton("লগআউট", (dialog, which) -> {
                    mAuth.signOut();
                    Toast.makeText(this, "লগআউট সফল হয়েছে", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("থাক", null)
                .show();
    }

    private void loadBakiFromCloud() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bakiList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    BakiModel item = ds.getValue(BakiModel.class);
                    if (item != null) {
                        item.key = ds.getKey();
                        bakiList.add(item);
                    }
                }
                // নতুন হিসাব সবার উপরে দেখানোর জন্য উল্টে দেওয়া
                Collections.reverse(bakiList);
                updateTotalBaki();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BakiHisabActivity.this, "ডেটা লোড করতে সমস্যা হয়েছে!", Toast.LENGTH_SHORT).show();
            }
        });
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
                    String key = dbRef.push().getKey();

                    if (key != null) {
                        BakiModel model = new BakiModel(key, title, amount, currentDate, isPaid);
                        dbRef.child(key).setValue(model)
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(this, "অনলাইনে হিসাব সেভ হয়েছে!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "সেভ হয়নি, ইন্টারনেট চেক করুন!", Toast.LENGTH_SHORT).show());
                    }
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
            tvTotalBaki.setTextColor(Color.parseColor("#FF6B6B"));
        } else {
            tvTotalBaki.setTextColor(Color.parseColor("#2EF2A1"));
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
            return position;
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
                if (item.key != null) {
                    dbRef.child(item.key).removeValue()
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(BakiHisabActivity.this, "হিসাব মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show());
                }
            });

            return convertView;
        }
    }
}
