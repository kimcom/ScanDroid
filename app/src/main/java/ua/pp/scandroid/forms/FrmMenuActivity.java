package ua.pp.scandroid.forms;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import ua.pp.scandroid.R;

public class FrmMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    public void clickLineInventory(View v){
        Intent intent = new Intent(getApplicationContext(), FrmListInventoryActivity.class);
        startActivity(intent);
    }
    public void clickLineReceipt(View v){
        Snackbar.make(v, "clickLineReceipt!", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }
    public void clickLineSale(View v){
        Snackbar.make(v, "clickLineSale!", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }
    public void clickLineSpis(View v){
        Snackbar.make(v, "clickLineSpis!", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }
    public void clickLineOrder(View v){
        Snackbar.make(v, "clickLineOrder!", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }
    public void clickLineSetting(View v){
        Snackbar.make(v, "clickLineSetting!", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }
}
