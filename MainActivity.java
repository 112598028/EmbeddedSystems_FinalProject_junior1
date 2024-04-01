package com.example.afinal;

import static java.lang.Integer.sum;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener, View.OnClickListener ,
        DatePickerDialog.OnDateSetListener {

    static final String DB_NAME = "productDB";
    static final String TB_NAME = "productList";
    static final String[] FROM = new String[] {"date", "reason", "money"};
    SQLiteDatabase db;
    Cursor cur, total_cost, total_save;
    SimpleCursorAdapter adapter;

    Calendar c = Calendar.getInstance();
    TextView click_date;

    Spinner spinner_reason;
    EditText edit_money;

    Button btn_add, btn_update, btn_delete;
    ListView lv;

    TextView txv_cost, txv_save;

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.home:
                break;
            case R.id.info:
                Intent it = new Intent(MainActivity.this, info.class);
                startActivity(it);
                finish();
                break;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        click_date = (TextView) findViewById(R.id.click_date);
        spinner_reason = (Spinner) findViewById(R.id.spinner_reason);
        edit_money = (EditText)findViewById(R.id.edit_money);
        btn_add = (Button)findViewById(R.id.btn_add);
        btn_update = (Button)findViewById(R.id.btn_update);
        btn_delete = (Button) findViewById(R.id.btn_delete);
        txv_cost = (TextView)findViewById(R.id.text_cost);
        txv_save = (TextView)findViewById(R.id.text_save);

        click_date.setOnClickListener(this);
        txv_cost.setOnClickListener(this);
        txv_save.setOnClickListener(this);


        db = openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);

        String createTable = "CREATE TABLE IF NOT EXISTS " + TB_NAME +
        "(_id INTEGER PRIMARY KEY AUTOINCREMENT, "+
        "date VARCHAR(32), " +
        "reason VARCHAR(8), " +
        "money INT)";

        db.execSQL(createTable);

        cur = db.rawQuery("SELECT * FROM " + TB_NAME, null);


        // 若是空的則寫入測試資料
        if(cur.getCount()==0){
            addData("2021/01/10", "支出_食", 150);
        }

        // 建立Adapter物件
        adapter = new SimpleCursorAdapter(this, R.layout.item, cur, FROM,
        new int[] {R.id.date, R.id.reason, R.id.money}, 0);

        lv = (ListView) findViewById(R.id.lv);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);
        requery();
    }

    @Override
    public void onClick(View view) {
        if(view == click_date){
            new DatePickerDialog(this, this,
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)).show();

        }

        if (view == txv_cost) {
            total_cost = db.rawQuery("SELECT SUM(money) FROM " + TB_NAME + " where reason != '存入' ", null);
            if(total_cost.moveToFirst()){
                int num = total_cost.getInt(0);
                Toast.makeText(this, "目前已花費：" + num, Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "error：", Toast.LENGTH_SHORT).show();
            }
        }

        if (view == txv_save) {
            total_save = db.rawQuery("SELECT SUM(money) FROM " + TB_NAME + " where reason = '存入' ", null);

            if(total_save.moveToFirst()){
                int num = total_save.getInt(0);
                Toast.makeText(this, "目前已存入：" + num, Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "error：", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDateSet(DatePicker datePicker, int y, int m, int d) {
        click_date.setText(y + "/" + (m+1) + "/" + d);
    }

    private void addData(String date, String reason, int money) {
        ContentValues cv = new ContentValues(2);
        cv.put(FROM[0], date);
        cv.put(FROM[1], reason);
        cv.put(FROM[2], money);

        db.insert(TB_NAME, null, cv);
    }

    private void update(String date, String reason, int money, int id){
        ContentValues cv = new ContentValues(3);
        cv.put(FROM[0], date);
        cv.put(FROM[1], reason);
        cv.put(FROM[2], money);

        db.update(TB_NAME, cv, "_id="+id, null);
    }

    private void requery() {
        cur = db.rawQuery("SELECT * FROM " + TB_NAME, null);
        adapter.changeCursor(cur);

        /*if (cur.getCount() != 0){
            total_cost = db.rawQuery("SELECT SUM(money) FROM " + TB_NAME + " where reason NOT IN 存入 ", null);
            txv_cost.setText(getString(R.string.cost) + (CharSequence) total_cost);

            total_save = db.rawQuery("SELECT SUM(money) FROM " + TB_NAME + " where reason IN 存入 ", null);
            txv_save.setText(getString(R.string.save) +(CharSequence) total_save);
        }*/

    }

    @SuppressLint("Range")
    @Override
    public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
        cur.moveToPosition(position);

        // 讀出品名、價格，並顯示資料
        click_date.setText(cur.getString(cur.getColumnIndex(FROM[0])));
        edit_money.setText(cur.getString(cur.getColumnIndex(FROM[2])));

        btn_update.setEnabled(true);
        btn_delete.setEnabled(true);
    }

    public void add(View view) {
        String dateStr = click_date.getText().toString().trim();

        String [] reason = getResources().getStringArray(R.array.reason);
        int index = spinner_reason.getSelectedItemPosition();
        String reasonStr = reason[index];

        String moneyStr = edit_money.getText().toString();
        int money = Integer.parseInt(moneyStr);

        if(dateStr.length() == 0 || reasonStr.length() == 0 || money == 0){
            return;
        }

        if(view.getId() == R.id.btn_update){
            update(dateStr, reasonStr, money, cur.getInt(0));
        }
        else{
            addData(dateStr, reasonStr, money);
        }

        requery();
    }

    public void delete(View view) {
        db.delete(TB_NAME, "_id="+cur.getInt(0), null);
        requery();
    }

    protected void onDestroy(){
        super.onDestroy();
        db.close(); //關閉資料庫
    }
}