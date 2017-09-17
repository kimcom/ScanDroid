package ua.pp.scandroid.forms;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import ua.pp.scandroid.R;

public class FrmQuantityActivity extends AppCompatActivity {
    // UI references.
    private TextView mGoodIDView;
    private TextView mBarcodeView;
    private TextView mArticleView;
    private TextView mNameView;
    private TextView mQtyView;
    private View mProgressView;
    private ScrollView mFormView;
    public String docid = "";
    public String good_id = "";
    public String good_art = "";
    public String good_name = "";
    public String good_barcode = "";
    public String good_qty = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quantity);
        // get intent data
        Intent intent = getIntent();
        docid = intent.getExtras().getString("docid");
        good_id = intent.getExtras().getString("good_id");
        good_art = intent.getExtras().getString("good_art");
        good_name = intent.getExtras().getString("good_name");
        good_barcode = intent.getExtras().getString("good_barcode");
        good_qty = intent.getExtras().getString("good_qty");

        mGoodIDView = (TextView) findViewById(R.id.tvGoodID);
        mBarcodeView = (TextView) findViewById(R.id.tvBarcode);
        mArticleView = (TextView) findViewById(R.id.tvArticle);
        mNameView = (TextView) findViewById(R.id.tvName);
        mQtyView = (EditText) findViewById(R.id.tvQty);

        Button mButtonSave = (Button) findViewById(R.id.button_save);
        mButtonSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String qty = mQtyView.getText().toString();
                if (qty.equals("")) qty = "0";
                Intent intent = new Intent();
                intent.putExtra("good_id", good_id);
                intent.putExtra("good_barcode", good_barcode);
                intent.putExtra("good_qty", qty);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        FloatingActionButton mButtonAdd = (FloatingActionButton) findViewById(R.id.button_plus);
        mButtonAdd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
            //int qty = Integer.parseInt(mQtyView.getText().toString());
            Double qty = Double.parseDouble(mQtyView.getText().toString());
            mQtyView.setText(Double.toString(qty + 1));
            }
        });
        FloatingActionButton mButtonMin = (FloatingActionButton) findViewById(R.id.button_minus);
        mButtonMin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
            //int qty = Integer.parseInt(mQtyView.getText().toString());
            Double qty = Double.parseDouble(mQtyView.getText().toString());
            if (qty>0) qty = qty - 1;
            mQtyView.setText(Double.toString(qty));
            }
        });

        mFormView = (ScrollView) findViewById(R.id.main_form);
        mProgressView = findViewById(R.id.quantity_progress);

        mGoodIDView.setText(good_id);
        mBarcodeView.setText(good_barcode);
        mArticleView.setText(good_art);
        mNameView.setText(good_name);
        mQtyView.setText(good_qty);

        mQtyView.requestFocus();
    }
}
