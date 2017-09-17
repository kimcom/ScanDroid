package ua.pp.scandroid.forms;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.pp.scandroid.R;
import ua.pp.scandroid.db.ConnectionDb;
import ua.pp.scandroid.fragments.FrmDocInventoryPageFragment;
import ua.pp.scandroid.util.ConfigReader;
import ua.pp.scandroid.util.MyUtil;
import ua.pp.scandroid.util.ZoomOutPageTransformer;

public class FrmDocInventoryActivity extends AppCompatActivity {
    private static int REQUEST_CAMERA = 11;
    private static int REQUEST_QUANTITY = 12;
    private static int REQUEST_SEARCH = 14;
    public static String TAG = "doc_inventory";
    ConfigReader config = null;

    private String barcode = "";
    private boolean blSearchBarcodeListRu = false;

    private boolean blMessage1 = false;
    private DbTask mDbTask = null;
    private View mFormView;
    private View mProgressView;
    private ListView mListView = null;
    private ArrayList<ObjectItem> listItems = new ArrayList<ObjectItem>();
    private ListAdapter adapter;
    private PagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private View vwDocHeader = null;
    private View vwDocBody = null;
    private int mSection = 0;

    private int[] soundID = new int[4];

    public String docid = "";
    public String userdocid = "";
    public String docdt = "";
    public String notes = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doc_inventory);

        config = ConfigReader.getInstance(getBaseContext());

        // get intent data
        Intent intent = getIntent();
        docid = intent.getExtras().getString("docid");
        userdocid = intent.getExtras().getString("userdocid");
        docdt = intent.getExtras().getString("docdt");
        notes = intent.getExtras().getString("notes");

        setTitle("Инвентаризация №" + userdocid);

        mProgressView = findViewById(R.id.doc_progress);
        mFormView = findViewById(R.id.container);

        // Связываем кнопку громкости с нашим приложением
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        soundID[0] = R.raw.vk2;
        soundID[1] = R.raw.viber2;
        soundID[2] = R.raw.bam2;
        soundID[3] = R.raw.icq2;

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mSection = position;
                if (position == 1) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mFormView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
//                    if (!blMessage2) {
//                        blMessage2 = true;
//                        Toast.makeText(getApplicationContext(), "Сканируйте товары!", Toast.LENGTH_SHORT).show();
//                    }
                    vwDocBody = (View) findViewById(R.id.frm_doc_body);
                    if (vwDocBody!=null){
                        mListView = (ListView) vwDocBody.findViewById(R.id.list_goods_body);
                        if (mListView!=null) mListView.requestFocus();
                    }
                }
            }
            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());

        //soundPlay(soundID[2]);
    }

    private void soundPlay(int raw_sound){
        //Объект проигрывателя, который будет проигрывать звук на кнопке.
        MediaPlayer mp = MediaPlayer.create(this, raw_sound);
        //Звук будет проигрываться только 1 раз:
        mp.setLooping(false);
        //Установка обработчика события на момент готовности проигрывателя:
        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
        {
            public void onPrepared(MediaPlayer mp)
            {
                //При готовности к проигрыванию запуск вывода звука:
                mp.start();
            }
        });
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            public void onCompletion(MediaPlayer mp)
            {
                mp.release();
            }
        });
    }
    private void soundPlay2(int raw_sound){
        //System.out.println("mediaPlayer start " + raw_sound);
        //MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), raw_sound);
        synchronized (MediaPlayer.class) {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, raw_sound);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mediaPlayer) {
                //System.out.println("mediaPlayer stop ");
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
            }
        });
            mediaPlayer.setLooping(false);
            mediaPlayer.start();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if(keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            return super.dispatchKeyEvent(event);
        }
        if (mSection==0){
            return super.dispatchKeyEvent(event);
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN){
            //System.out.println("dispatchKeyEvent mSection:"+mSection+"  barcode:"+barcode+"    KeyEvent:"+KeyEvent.keyCodeToString(event.getKeyCode())+"  Action="+event.toString());
            if(keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_SHIFT_LEFT) {
                if (mDbTask != null) {
                    barcode = "";
                    return false;
                }
                mDbTask = new DbTask(DbTask.DOC_GOOD_ADD_BY_BARCODE, barcode);
                mDbTask.execute((Void) null);
                barcode = "";
                return false;
            }else if (keyCode >= 7 && keyCode <=16 ){
                barcode += Character.toString((char) event.getUnicodeChar());
                return false;
            }else{
                barcode = "";
                return false;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //System.out.println("onResume: mSection="+mSection);
        if (mSection==1) {
            if (mDbTask != null) return;
            showProgress(true);
            mDbTask = new DbTask(DbTask.DOC_BODY);
            mDbTask.execute((Void) null);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    @Override
    protected void onStop() {
        super.onStop();
    }
    @Override
    protected void onPause() {
        super.onPause();
    }

    public void startFrmDocHeader(){
        vwDocHeader = (View) findViewById(R.id.frm_doc_header);
        TextView textDocID = (TextView) vwDocHeader.findViewById(R.id.textDocID);
        textDocID.setText(userdocid);
        TextView textDocDT = (TextView) vwDocHeader.findViewById(R.id.textDocDT);
        textDocDT.setText(docdt);
        EditText textDocNotes = (EditText) vwDocHeader.findViewById(R.id.textDocNotes);
        textDocNotes.setText(notes);
        if (!blMessage1) {
            blMessage1 = true;
            Toast.makeText(getApplicationContext(), "После изменения описания\nнажмите кнопку \"Сохранить\"!", Toast.LENGTH_SHORT).show();
        }
    }

    public void startFrmDocBody(){
        vwDocBody = (View) findViewById(R.id.frm_doc_body);
        mListView = (ListView) vwDocBody.findViewById(R.id.list_goods_body);
        mListView.setOnItemClickListener(mListViewOnItemClickListener);
        //System.out.println("startFrmDocBody");
        if (mDbTask != null) return;
        showProgress(true);
        mDbTask = new DbTask(DbTask.DOC_BODY);
        mDbTask.execute((Void) null);

        FloatingActionButton fab_camera = (FloatingActionButton) vwDocBody.findViewById(R.id.fab_doc_inventory_camera);
        fab_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), FrmCameraActivity.class);
                startActivityForResult(i,REQUEST_CAMERA);
            }
        });
        if (!config.PERMISSION_CAMERA) fab_camera.setVisibility(View.INVISIBLE);

        FloatingActionButton fab_update = (FloatingActionButton) vwDocBody.findViewById(R.id.fab_doc_inventory_refresh);
        fab_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDbTask != null) return;
                showProgress(true);
                mDbTask = new DbTask(DbTask.DOC_BODY);
                mDbTask.execute((Void) null);
            }
        });

        FloatingActionButton fab_add = (FloatingActionButton) vwDocBody.findViewById(R.id.fab_doc_inventory_add_good);
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), FrmSearchActivity.class);
                i.putExtra("docid", docid);
                startActivityForResult(i,REQUEST_SEARCH);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        System.out.println("requestCode:"+requestCode+" resultCode:"+resultCode);
        if (requestCode==REQUEST_CAMERA){ //11 - ответ от активити с камерой
            //if (data == null) {return;}
            if(resultCode==RESULT_OK) {
                String barcode = data.getStringExtra("barcode");
                if (barcode.equals("")) return;
                if (mDbTask != null) return;
                showProgress(true);
                mDbTask = new DbTask(DbTask.DOC_GOOD_ADD_BY_BARCODE, barcode);
                mDbTask.execute((Void) null);
            }
        }
        if (requestCode==REQUEST_QUANTITY) { //12 - ответ от активити изменения кол-ва
            if(resultCode==RESULT_OK) {
                String good_id = data.getStringExtra("good_id");
                String good_qty = data.getStringExtra("good_qty");
                String good_barcode = data.getStringExtra("good_barcode");
                if (mDbTask != null) return;
                showProgress(true);
                mDbTask = new DbTask(DbTask.DOC_GOOD_QTY_SET, good_id, good_qty, good_barcode);
                mDbTask.execute((Void) null);
            }
        }
        if (requestCode==REQUEST_SEARCH) { //14 - ответ от активити изменения кол-ва
            if(resultCode==RESULT_OK) {
                String good_id = data.getStringExtra("good_id");
                String good_qty = data.getStringExtra("good_qty");
                if (mDbTask != null) return;
                showProgress(true);
                //mDbTask = new DbTask(DbTask.DOC_GOOD_QTY_SET, good_id, good_qty, "");
                System.out.println("good_id:"+good_id);
                mDbTask = new DbTask(DbTask.DOC_GOOD_ADD_BY_GOODID, good_id, good_qty, "");
                mDbTask.execute((Void) null);
            }
        }
    }

    public void setCurrentListPos(int pos){
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mListView.setSelection(pos);
        mListView.smoothScrollToPosition(pos);
        mListView.setItemChecked(pos, true);
    }

    public void onClickBtnSaveNotes(View btn){
        if (mDbTask != null) return;
        showProgress(true);
        EditText tv = (EditText) findViewById(R.id.textDocNotes);
        mDbTask = new DbTask(DbTask.DOC_SET_NOTES, tv.getText().toString());
        mDbTask.execute((Void) null);
    }

    public void setListBodyDocAdapter(){
        if (listItems.size()==0) return;
        if (mListView == null) return;
        adapter = new MySimpleAdapter(getApplicationContext(), listItems, R.layout.activity_doc_inventory_body_cellgrid,
                new String[] { ObjectItem.GOOD_ID, ObjectItem.BARCODE, ObjectItem.ARTICLE, ObjectItem.PRICE, ObjectItem.QTY, ObjectItem.NAME},
                new int[]    { R.id.tvGoodID, R.id.tvBarcode, R.id.tvArticle, R.id.tvPrice, R.id.tvQty, R.id.tvName});
        mListView.setAdapter(adapter);
        setCurrentListPos(0);
    }

    private ListView.OnItemClickListener mListViewOnItemClickListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            ObjectItem item = (ObjectItem) parent.getAdapter().getItem(position);
            String good_id = item.get(ObjectItem.GOOD_ID);
            String good_art = item.get(ObjectItem.ARTICLE);
            String good_name = item.get(ObjectItem.NAME);
            String good_barcode = item.get(ObjectItem.BARCODE);
            String good_qty = item.get(ObjectItem.QTY);

            Intent i = new Intent(getApplicationContext(), FrmQuantityActivity.class);
            i.putExtra("docid", docid);
            i.putExtra("good_id", good_id);
            i.putExtra("good_art", good_art);
            i.putExtra("good_name", good_name);
            i.putExtra("good_barcode", good_barcode);
            i.putExtra("good_qty", good_qty);
            startActivityForResult(i,REQUEST_QUANTITY);
        }
    };

    // TODO Represents an asynchronous task ConnectionDB
    private class DbTask extends AsyncTask<Void, Void, Boolean> {
        private ConnectionDb cnn = null;
        private ResultSet res = null;
        private String dbAction = "";
        private String dbBarcode = "";
        private String dbNotes = "";
        private String dbQty = "";
        private String dbResultText = "";
        private String dbGoodID = "";
        private String dbGoodName = "";
        public final static String DOC_BODY = "inventory_body";
        public final static String DOC_GOOD_QTY_SET = "inventory_doc_good_qty_set";
        public final static String DOC_GOOD_ADD_BY_BARCODE = "inventory_add_good_by_barcode";
        public final static String DOC_GOOD_ADD_BY_GOODID = "inventory_add_good_by_barcode";
        public final static String DOC_SET_NOTES = "inventory_set_notes";
        public final static String SEARCH_BARCODE_LIST_RU = "search_barcode_list_ru";

        public DbTask(String action) {
            dbAction = action;
        }
        public DbTask(String action, String goodID, String qty, String good_barcode) {
            dbAction = action;
            dbGoodID = goodID;
            dbQty = qty;
            dbBarcode = good_barcode;
        }
        public DbTask(String action, String str) {
            dbAction = action;
            if (action == DOC_GOOD_ADD_BY_BARCODE || action == SEARCH_BARCODE_LIST_RU){
                dbBarcode = str;
            }
            if (action == DOC_SET_NOTES){
                dbNotes = str;
            }
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            //System.out.println("dbAction:"+dbAction+"   dbGoodID:"+dbGoodID);
            if (dbAction.equals("")) return false;
            if ((dbAction.equals(DOC_GOOD_ADD_BY_BARCODE) && dbBarcode.equals("")) &&
                    (dbAction.equals(DOC_GOOD_ADD_BY_GOODID) && dbGoodID.equals(""))) return false;
            if (dbAction.equals(SEARCH_BARCODE_LIST_RU) && dbBarcode.equals("")) return false;

            if (dbAction.equals(SEARCH_BARCODE_LIST_RU)){
                String url = "http://www.barcode-list.ru/barcode/RU/%D0%9F%D0%BE%D0%B8%D1%81%D0%BA.htm?barcode="+dbBarcode;
                //String url = "http://barcode-list.com/barcode/EN/Search.htm?barcode="+dbBarcode;
                try {
                    Document doc  = Jsoup.connect(url).get();
                    Elements table = doc.select("table.randomBarcodes");
                    if (!table.isEmpty()){
                        //System.out.println("table:"+table);
                        Element tr = table.select("tr").get(1);
                        Element td = tr.select("td").get(1);
//                        dbResultText += td.text()+"\n";
                        td = tr.select("td").get(2);
                        dbGoodName = td.text();
//                        td = tr.select("td").get(3);
//                        dbResultText += td.text()+"\n";
                    }else{
                        return false;
                    }
                    //System.out.println("dbGoodName="+dbGoodName);
                } catch (IOException e) {
                    System.out.println("ощибка: "+e.getMessage());
                }
                return true;
            }
            // TODO: attempt authentication against a network service.
            cnn = ConnectionDb.getInstance();
            if (cnn == null) return false;
            if (dbAction.equals(DOC_BODY)) {
                res = cnn.getDataResultSet("pr_doc_android",dbAction,"UserID="+cnn.userID+"&Serial="+Build.SERIAL+"&DocID=" + docid);
                return res != null;
            }
            if (dbAction.equals(DOC_GOOD_ADD_BY_BARCODE)) {
                //System.out.println(dbAction + ",UserID="+cnn.userID+"&Serial="+Build.SERIAL+"&DocID=" + docid + "&Barcode=" + dbBarcode + "&GoodID=" + dbGoodID + "&DeviceID="+cnn.deviceID);
                res = cnn.getDataResultSet("pr_doc_android",dbAction, "UserID="+cnn.userID+"&Serial="+Build.SERIAL+"&DocID=" + docid + "&Barcode=" + dbBarcode + "&GoodID=" + dbGoodID + "&DeviceID="+cnn.deviceID);
                return res != null;
            }
//            if (dbAction.equals(DOC_GOOD_ADD_BY_GOODID)) {
//                System.out.println(dbAction + ",UserID="+cnn.userID+"&Serial="+Build.SERIAL+"&DocID=" + docid + "&Barcode=" + dbBarcode + "&GoodID=" + dbGoodID + "&DeviceID="+cnn.deviceID);
//                res = cnn.getDataResultSet("pr_doc_android",dbAction, "UserID="+cnn.userID+"&Serial="+Build.SERIAL+"&DocID=" + docid + "&Barcode=" + dbBarcode + "&GoodID=" + dbGoodID + "&DeviceID="+cnn.deviceID);
//                return res != null;
//            }
            if (dbAction.equals(DOC_SET_NOTES)) {
                System.out.println(dbAction + ",UserID="+cnn.userID+"&Serial="+Build.SERIAL+"&DocID=" + docid + "&Notes=" + dbNotes);
                res = cnn.getDataResultSet("pr_doc_android",dbAction, "UserID="+cnn.userID+"&Serial="+Build.SERIAL+"&DocID=" + docid + "&Notes=" + dbNotes);
                return res != null;
            }
            if (dbAction.equals(DOC_GOOD_QTY_SET)) {
                System.out.println(dbAction + ",UserID="+cnn.userID+"&Serial="+Build.SERIAL+"&DocID=" + docid + "&GoodID=" + dbGoodID + "&Quantity="+dbQty+"&Barcode="+dbBarcode);
                res = cnn.getDataResultSet("pr_doc_android",dbAction,"UserID="+cnn.userID+"&Serial="+Build.SERIAL+"&DocID=" + docid + "&GoodID=" + dbGoodID + "&Quantity="+dbQty+"&Barcode="+dbBarcode);
                return res != null;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mDbTask = null;
            if (success) {
                if (dbAction.equals(SEARCH_BARCODE_LIST_RU)){
                    if (dbGoodName.length()>0){
                    //Toast.makeText(getApplicationContext(), "Найден товар:\n"+dbResultText, Toast.LENGTH_LONG).show();
                        int qty = 0;
                        synchronized(listItems) {
                            for(ObjectItem obj : new ArrayList<ObjectItem>(listItems)) {
                                if(obj.get(ObjectItem.BARCODE).equals(dbBarcode)){
                                    qty = Integer.parseInt(obj.get(ObjectItem.QTY));
                                    listItems.remove(obj);
                                }
                            }
                        }
                        listItems.add(0,new ObjectItem("0", dbBarcode, dbBarcode, dbGoodName, Integer.toString(qty+1), ""));
                        setListBodyDocAdapter();
                    }else{
                        Toast.makeText(getApplicationContext(), "Штрих-код не найден в базе данных http://www.barcode-list.ru !", Toast.LENGTH_SHORT).show();
                    }
                }
                if (dbAction.equals(DOC_BODY)) {
                    boolean result = false;
                    synchronized(listItems) {
                        listItems.clear();
                    }
                    try {
                        while (res.next()) {
                            synchronized(listItems) {
                                listItems.add(new ObjectItem(res.getString("GoodID"), res.getString("Barcode"), res.getString("Article"), res.getString("Name"), res.getString("Qty"), res.getString("Price")));
                            }
                            result = true;
                        }
                    } catch (SQLException e) {
                        MyUtil.errorToLog(this.getClass().getName(), e.getMessage());
                        soundPlay(soundID[2]);
                        Toast.makeText(getApplicationContext(), "ОШИБКА!\n\n"+e.getMessage(), Toast.LENGTH_LONG).show();
                        showProgress(false);
                        return;
                    }
                    if (result) {
                        setListBodyDocAdapter();
                    }else {
                        Toast.makeText(getApplicationContext(), "В документе нет товаров!", Toast.LENGTH_SHORT).show();
                    }
                }
                if (dbAction.equals(DOC_GOOD_ADD_BY_BARCODE)) {
                    boolean result = false;
                    try {
                        while (res.next()) {
                            synchronized(listItems) {
                                for(ObjectItem obj : new ArrayList<ObjectItem>(listItems)) {
                                    if(obj.get(ObjectItem.GOOD_ID).equals(res.getString("GoodID")))
                                            listItems.remove(obj);
                                }
                                if (res.getInt("Qty")!=0){
                                    listItems.add(0,new ObjectItem(res.getString("GoodID"), res.getString("Barcode"), res.getString("Article"), res.getString("Name"), res.getString("Qty"), res.getString("Price")));
                                    result = true;
                                }
                            }
                        }
                    } catch (SQLException e) {
                        MyUtil.errorToLog(TAG, e.getMessage());
                        soundPlay(soundID[2]);
                        Toast.makeText(getApplicationContext(), "ОШИБКА!\n\n"+e.getMessage(), Toast.LENGTH_LONG).show();
                        showProgress(false);
                        return;
                    }
                    if (result) {
                        soundPlay(soundID[0]);
                        setListBodyDocAdapter();
                    }else {
                        if (!blSearchBarcodeListRu){
                            soundPlay(soundID[1]);
                            Toast.makeText(getApplicationContext(), "Штрих-код не найден!", Toast.LENGTH_SHORT).show();
                        }else{
                            soundPlay(soundID[3]);
                            String bcd = dbBarcode;
                            mDbTask = new DbTask(DbTask.SEARCH_BARCODE_LIST_RU, bcd);
                            mDbTask.execute((Void) null);
                        }
                    }
                }
                if (dbAction.equals(DOC_GOOD_QTY_SET)) {
                    boolean result = false;
                    try {
                        while (res.next()) {
                            synchronized(listItems) {
                                for(ObjectItem obj : new ArrayList<ObjectItem>(listItems)) {
                                    if(obj.get(ObjectItem.GOOD_ID).equals(res.getString("GoodID")))
                                        listItems.remove(obj);
                                }
                                if (res.getInt("Qty")!=0)
                                    listItems.add(0,new ObjectItem(res.getString("GoodID"), res.getString("Barcode"), res.getString("Article"), res.getString("Name"), res.getString("Qty"), res.getString("Price")));
                            }
                            result = true;
                        }
                    } catch (SQLException e) {
                        MyUtil.errorToLog(TAG, e.getMessage());
                        soundPlay(soundID[2]);
                        Toast.makeText(getApplicationContext(), "ОШИБКА!\n\n"+e.getMessage(), Toast.LENGTH_LONG).show();
                        showProgress(false);
                        return;
                    }
                    if (result) {
                        soundPlay(soundID[0]);
                        setListBodyDocAdapter();
                    }else {
                        soundPlay(soundID[2]);
                        Toast.makeText(getApplicationContext(), "Возникла ошибка!\n\nКоличество НЕ изменилось!\n\nСообщите разработчику!", Toast.LENGTH_LONG).show();
                    }
                }
                if (dbAction.equals(DOC_SET_NOTES)) {
                    boolean result = false;
                    try {
                        while (res.next()) {
                            result = true;
                        }
                    } catch (SQLException e) {
                        MyUtil.errorToLog(TAG, e.getMessage());
                        soundPlay(soundID[2]);
                        Toast.makeText(getApplicationContext(), "ОШИБКА!\n\n"+e.getMessage(), Toast.LENGTH_LONG).show();
                        showProgress(false);
                        return;
                    }
                    if (result) {
                        Toast.makeText(getApplicationContext(), "Изменения успешно сохранены!", Toast.LENGTH_SHORT).show();
                    }else {
                        soundPlay(soundID[2]);
                        Toast.makeText(getApplicationContext(), "Ошибка при записи описания документа!", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                soundPlay(soundID[2]);
                Toast.makeText(getApplicationContext(), "Нет связи с сервером!", Toast.LENGTH_SHORT).show();
            }
            showProgress(false);
        }
        @Override
        protected void onCancelled() {
            mDbTask = null;
            showProgress(false);
        }
    }

    private class ObjectItem extends HashMap<String, String> {
        public static final String GOOD_ID = "GoodID";
        public static final String BARCODE = "Barcode";
        public static final String ARTICLE = "Article";
        public static final String NAME = "Name";
        public static final String QTY = "Qty";
        public static final String PRICE = "Price";
        // Конструктор с параметрами
        public ObjectItem(String goodid, String barcode, String article, String name, String qty, String price) {
            super();
            super.put(GOOD_ID, goodid);
            super.put(BARCODE, barcode);
            super.put(ARTICLE, article);
            super.put(NAME, name);
            super.put(QTY, qty);
            super.put(PRICE, price);
        }
    }

    private class MySimpleAdapter extends SimpleAdapter{
        /**
         * Constructor
         *
         * @param context  The context where the View associated with this SimpleAdapter is running
         * @param data     A List of Maps. Each entry in the List corresponds to one row in the list. The
         *                 Maps contain the data for each row, and should include all the entries specified in
         *                 "from"
         * @param resource Resource identifier of a view layout that defines the views for this list
         *                 item. The layout file should include at least those named views defined in "to"
         * @param from     A list of column names that will be added to the Map associated with each
         *                 item.
         * @param to       The views that should display column in the "from" parameter. These should all be
         *                 TextViews. The first N views in this list are given the values of the first N columns
         */
        public MySimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);
            TextView tv = null;
            if (position==0) {
                tv = (TextView) v.findViewById(R.id.tvBarcode);
                tv.setTypeface(null, Typeface.BOLD);
                tv = (TextView) v.findViewById(R.id.tvArticle);
                tv.setTypeface(null, Typeface.BOLD);
                tv = (TextView) v.findViewById(R.id.tvName);
                tv.setTypeface(null, Typeface.BOLD);
                tv = (TextView) v.findViewById(R.id.tvQty);
                tv.setTypeface(null, Typeface.BOLD);
                tv = (TextView) v.findViewById(R.id.tvPrice);
                tv.setTypeface(null, Typeface.BOLD);
            }else{
                tv = (TextView) v.findViewById(R.id.tvBarcode);
                tv.setTypeface(null, Typeface.NORMAL);
                tv = (TextView) v.findViewById(R.id.tvArticle);
                tv.setTypeface(null, Typeface.NORMAL);
                tv = (TextView) v.findViewById(R.id.tvName);
                tv.setTypeface(null, Typeface.NORMAL);
                tv = (TextView) v.findViewById(R.id.tvQty);
                tv.setTypeface(null, Typeface.NORMAL);
                tv = (TextView) v.findViewById(R.id.tvPrice);
                tv.setTypeface(null, Typeface.NORMAL);
            }
            return v;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //return new PlaceholderFragment().newInstance(position + 1);
            mSection = position;
            Bundle argums = new Bundle();
            argums.putInt("sectionNumber",position);
            return FrmDocInventoryPageFragment.newInstance(argums);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                        return "ДОКУМЕНТ";
                case 1:
                    return "ТОВАРЫ";
            }
            return null;
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}
