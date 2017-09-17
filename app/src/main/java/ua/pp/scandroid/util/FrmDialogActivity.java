package ua.pp.scandroid.util;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import ua.pp.scandroid.R;

public class FrmDialogActivity extends AppCompatActivity {
    public TextView tvMessage;
    public Button btnOk;
    public Button btnCancel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);

        tvMessage   = (TextView) findViewById(R.id.textMessage);

        btnOk       = (Button) findViewById(R.id.btnOk);
        btnCancel   = (Button) findViewById(R.id.btnCancel);

        // get intent data
        Intent intent = getIntent();
        // Selected image id
        String title = intent.getExtras().getString("title");
        String message = intent.getExtras().getString("message");

        String btn_ok = intent.getExtras().getString("btn_ok");
        String btn_cancel = intent.getExtras().getString("btn_cancel");

        setTitle(title);

        tvMessage.setText(message);

        if (!btn_ok.equals("0") && !btn_ok.equals("")){
            btnOk.setVisibility(View.VISIBLE);
            btnOk.setText(btn_ok);
            btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.putExtra("result", "ok");
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        }
        if (!btn_cancel.equals("0") && !btn_cancel.equals("")){
            btnCancel.setVisibility(View.VISIBLE);
            btnCancel.setText(btn_cancel);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.putExtra("result", "cancel");
                    setResult(RESULT_CANCELED, intent);
                    finish();
                }
            });
        }
    }
}
