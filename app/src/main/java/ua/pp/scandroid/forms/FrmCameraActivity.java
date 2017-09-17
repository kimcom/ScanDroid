package ua.pp.scandroid.forms;

import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.ViewGroup;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import ua.pp.scandroid.R;

public class FrmCameraActivity extends AppCompatActivity{

    private ZXingScannerView mScannerView;
    private boolean mFlash = true;
    public static final List<BarcodeFormat> ALL_FORMATS = new ArrayList<>();
    static {
        ALL_FORMATS.add(BarcodeFormat.EAN_8);
        ALL_FORMATS.add(BarcodeFormat.EAN_13);
    }

    private String barcode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.frm_doc_camera_frame);
        mScannerView = new ZXingScannerView(this);
        contentFrame.addView(mScannerView);
        mScannerView.setFlash(mFlash);
        mScannerView.setAutoFocus(true);
        mScannerView.setFormats(ALL_FORMATS);
        mScannerView.setResultHandler(new MyResultHandler());
        mScannerView.startCamera();
    }

    @Override
    protected void onDestroy() {
//        Intent intent = new Intent();
//        intent.putExtra("barcode", "");
//        setResult(RESULT_CANCELED, intent);
//        finish();
        super.onDestroy();
        if (mScannerView != null ) mScannerView.stopCamera();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mScannerView != null ) mScannerView.stopCamera();
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (mScannerView != null ) mScannerView.stopCamera();
    }

    private class MyResultHandler implements ZXingScannerView.ResultHandler{
        @Override
        public void handleResult(Result rawResult) {
            mScannerView.stopCamera();
            Intent intent = new Intent();
            intent.putExtra("barcode", rawResult.getText());
            setResult(RESULT_OK, intent);
            finish();
//            if (mDbTask != null) return;
//            mDbTask = new DbTask(DbTask.ADD_GOOD_BY_BARCODE, rawResult.getText());
//            mDbTask.execute((Void) null);
        }
    }

}
