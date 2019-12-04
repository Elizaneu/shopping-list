package com.example.shoppinglist;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import org.json.JSONArray;

import java.io.*;
import java.util.*;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FloatingActionButton B_NewPurchase;
    private LinearLayout LL_get;
    private Button B_get;
    private Button B_delete;
    private EditText ET_get;
    private RecyclerView RV;
    private DataAdapter adapter;

    private ArrayList<Item> purchases = new ArrayList<>();
    private ArrayList<Integer> CheckPurchases = new ArrayList<>();
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, CheckBox> checker = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        findByID();
        buildRecyclerView();
        setClickListener();
        LoadCache();
    }

    private void findByID() {
        B_get = findViewById(R.id.B_get);
        B_delete = findViewById(R.id.B_delete);
        B_NewPurchase = findViewById(R.id.B_NewPurchase);
        LL_get = findViewById(R.id.LL_get);
        ET_get = findViewById(R.id.ET_get);
        RV = findViewById(R.id.RV);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setClickListener() {
        B_NewPurchase.setOnClickListener(this);
        B_get.setOnClickListener(this);
        B_delete.setOnClickListener(this);
    }

    private void buildRecyclerView() {
        adapter = new DataAdapter(purchases);
        RV.setLayoutManager(new LinearLayoutManager(this));
        RV.setAdapter(adapter);
        adapter.setOnClickListener(new ItemClickListener() {
            @Override
            public void CheckBoxClick(int position, CheckBox checkBox) {
                if (checkBox.isChecked()){
                    CheckPurchases.add(position);
                    checker.put(position, checkBox);
                    Collections.sort(CheckPurchases);
                }else{
                    for (int i = 0; i < CheckPurchases.size(); i++) {
                        if (CheckPurchases.get(i) == position){
                            CheckPurchases.remove(i);
                            break;
                        }
                        checker.remove(position);
                    }
                }

            }

            @SuppressLint("RestrictedApi")
            @Override
            public void CardListener(int position, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    LL_get.setVisibility(View.GONE);
                    B_NewPurchase.setVisibility(View.VISIBLE);
                }
            }
        });
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (((ViewHolder)viewHolder).getCheck()) {
                    removeItem(viewHolder.getAdapterPosition());
                    Toast.makeText(MainActivity.this, "Товар удален", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(MainActivity.this, "Товар не помечен галочкой", Toast.LENGTH_SHORT).show();
                    adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                }
            }
            @Override
            public void onChildDraw(@NonNull Canvas c,@NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                final View foregroundView = ((ViewHolder) viewHolder).foreground;

                getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY, actionState, isCurrentlyActive);
            }
            @Override
            public void onChildDrawOver(Canvas c, RecyclerView recyclerView,
                                        RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                        int actionState, boolean isCurrentlyActive) {
                final View foregroundView = ((ViewHolder) viewHolder).foreground;
                getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY, actionState, isCurrentlyActive);
            }
            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                final View foregroundView = ((ViewHolder) viewHolder).foreground;
                getDefaultUIUtil().clearView(foregroundView);
            }
            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                if (viewHolder != null) {
                    final View foregroundView = ((ViewHolder) viewHolder).foreground;
                    getDefaultUIUtil().onSelected(foregroundView);
                }
            }
        }).attachToRecyclerView(RV);
    }

    private void LoadCache(){
        File cache = new File(getCacheDir(), "purchases.txt");
        try {
            FileReader in = new FileReader(cache);
            BufferedReader buffer = new BufferedReader(in);
            String line = buffer.readLine();
            while (line != null) {
                insertItem(purchases.size(), line);
                line = buffer.readLine();
            }
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "Не удалось открыть файл кэша", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Возникла ошибка при загрузки из кэша", Toast.LENGTH_LONG).show();
        }
    }

    private void SaveCache(){
        File cache = new File(getCacheDir(), "purchases.txt");
        try {
            FileWriter out = new FileWriter(cache, false);
            for (Item s:purchases){
                out.write(s.getPurchase());
                out.append('\n');
            }
            out.flush();
        } catch (FileNotFoundException  e) {
            Toast.makeText(this, "Не удалось получить доступ к файлам кэша для перезаписи", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Не удалось записать в кэш изменения", Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.B_NewPurchase:
                LL_get.setVisibility(View.VISIBLE);
                B_NewPurchase.setVisibility(View.GONE);
                break;
            case R.id.B_get:
                String text = ET_get.getText().toString();
                if (!text.equals("")){
                    ET_get.setText("");
                    insertItem(purchases.size(), text);
                    RV.smoothScrollToPosition(purchases.size());
                }else{
                    Toast.makeText(MainActivity.this, "Введите покупку", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.B_delete:
                if (CheckPurchases.size()!=0){
                    AlertDialog.Builder DeleteAll = new AlertDialog.Builder(this);
                    DeleteAll.setTitle("Delete")
                             .setIcon(R.drawable.ic_delete_black_24dp)
                             .setMessage("Вы действительно хотите удалить все отмеченные покупки?")
                             .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                 @Override
                                 public void onClick(DialogInterface dialog, int which) {
                                     for (int i = CheckPurchases.size()-1; i >= 0; i--){
                                         removeItem(CheckPurchases.get(i));
                                     }
                                     CheckPurchases.removeAll(CheckPurchases);
                                     dialog.cancel();
                                 }
                             })
                             .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                                 @Override
                                 public void onClick(DialogInterface dialog, int which) {
                                     dialog.cancel();
                                 }
                             });
                    DeleteAll.create().show();
                }
                else
                    Toast.makeText(MainActivity.this, "Не стоит ни одной галочки", Toast.LENGTH_SHORT).show();

        }
    }

    private void insertItem(int position, String purchase){
        purchases.add(position, new Item(purchase));
        adapter.notifyItemInserted(position);
        SaveCache();
    }

    private void removeItem(int position){
        purchases.remove(position);
        CheckBox c = checker.remove(position);
        if (c != null)
            c.setChecked(false);
        boolean b = true;
        for (int i = 0; i < CheckPurchases.size(); i++) {
            if (CheckPurchases.get(i) == position) {
                CheckPurchases.remove(i);
                b = false;
            }
            if (CheckPurchases.size()!= i && CheckPurchases.get(i) >position && !b)
                CheckPurchases.set(i, CheckPurchases.get(i)-1);
        }
        adapter.notifyItemRemoved(position);
        SaveCache();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onBackPressed() {
       // super.onBackPressed();
        if (B_NewPurchase.getVisibility() == View.VISIBLE) {
            AlertDialog.Builder BackAlert = new AlertDialog.Builder(this);
            BackAlert.setTitle("Quit")
                    .setIcon(R.drawable.ic_delete_black_24dp)
                    .setMessage("Вы действительно хотите выйти?")
                    .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            BackAlert.create().show();
        }else{
            LL_get.setVisibility(View.GONE);
            B_NewPurchase.setVisibility(View.VISIBLE);
        }
    }
}