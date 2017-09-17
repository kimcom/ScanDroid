package ua.pp.scandroid.forms;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.pp.scandroid.R;
import ua.pp.scandroid.db.ConnectionDb;
import ua.pp.scandroid.util.ConfigReader;
import ua.pp.scandroid.util.MyUtil;

public class FrmSearchActivity extends AppCompatActivity {
    // UI references.
    private ConfigReader config = null;
    private boolean bl = true; //если true - изменяет количество только по нажатию кнопки
    private DbTask mDbTask = null;
    private ListView mListView = null;
    private ArrayList<ObjectItem> listItems = new ArrayList<ObjectItem>();
    private ListAdapter adapter;
    private EditText mArticleView;
    private EditText mNameView;
    private View mProgressView;
    private View mFormView;
    private Button mBtnSearch;
    public String docid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        // get intent data
        Intent intent = getIntent();
        docid = intent.getExtras().getString("docid");

        config = ConfigReader.getInstance(getApplicationContext());

        mArticleView = (EditText) findViewById(R.id.tvArticle);
        mNameView = (EditText) findViewById(R.id.tvName);
        mBtnSearch = (Button) findViewById(R.id.button_search);
        mBtnSearch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                config.good_article = mArticleView.getText().toString();
                config.good_name = mNameView.getText().toString();

                if (mDbTask != null) return;
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                //imm.hideSoftInputFromWindow(mFormView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                //imm.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0);
                showProgress(true);
                mDbTask = new DbTask(DbTask.GOOD_SEARCH, mArticleView.getText().toString(), mNameView.getText().toString());
                mDbTask.execute((Void) null);
            }
        });

        mFormView = findViewById(R.id.search_form);
        mProgressView = findViewById(R.id.search_progress);

        mListView = (ListView) findViewById(R.id.list_goods_search);
        //mListView.setOnItemClickListener(mListViewOnItemClickListener);
        mListView.setOnItemLongClickListener(mListViewOnItemLongClickListener);

        mArticleView.setText(config.good_article);
        mNameView.setText(config.good_name);

        mArticleView.requestFocus();

//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.showSoftInput(mArticleView,InputMethodManager.SHOW_FORCED);
//        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void setCurrentListPos(int pos){
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mListView.setSelection(pos);
        mListView.smoothScrollToPosition(pos);
        mListView.setItemChecked(pos, true);
    }

    public void setListBodyDocAdapter(){
        if (listItems.size()==0) return;
        if (mListView == null) return;
        adapter = new SimpleAdapter(getApplicationContext(), listItems, R.layout.activity_search_cellgrid,
                new String[] { ObjectItem.GOOD_ID, ObjectItem.ARTICLE, ObjectItem.PRICE, ObjectItem.NAME},
                new int[]    { R.id.tvGoodID, R.id.tvArticle, R.id.tvPrice, R.id.tvName});
        mListView.setAdapter(adapter);
        setCurrentListPos(0);
    }

    private ListView.OnItemLongClickListener mListViewOnItemLongClickListener = new ListView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            ObjectItem item = (ObjectItem) parent.getAdapter().getItem(position);
            String good_id = item.get(ObjectItem.GOOD_ID);
            String qty = "1";
            Intent intent = new Intent();
            intent.putExtra("good_id", good_id);
            intent.putExtra("good_qty", qty);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }
    };



    // TODO Represents an asynchronous task ConnectionDB
    public class DbTask extends AsyncTask<Void, Void, Boolean> {
        private ConnectionDb cnn = null;
        private ResultSet res = null;
        private String dbAction = "";
        private String dbArticle = "";
        private String dbName = "";
        public final static String GOOD_SEARCH = "good_search";

        public DbTask(String action, String good_article, String good_name) {
            dbAction = action;
            dbArticle = good_article;
            dbName = good_name;
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            if (dbAction.equals("")) return false;
            // TODO: attempt authentication against a network service.
            cnn = ConnectionDb.getInstance();
            if (cnn == null) return false;
            if (dbAction.equals(GOOD_SEARCH)) {
                res = cnn.getDataResultSet("pr_doc_android",dbAction,"UserID="+cnn.userID+"&Serial="+Build.SERIAL+"&DocID=" + docid+"&Article="+dbArticle+"&Name="+dbName);
                return res!=null;
            }
            return true;
        }
        @Override
        protected void onPostExecute(final Boolean success) {
            mDbTask = null;
            showProgress(false);
            if (success) {
                if (dbAction.equals(GOOD_SEARCH)) {
                    boolean result = false;
                    synchronized(listItems) {
                        listItems.clear();
                    }
                    try {
                        while (res.next()) {
                            synchronized(listItems) {
                                listItems.add(new ObjectItem(res.getString("GoodID"), res.getString("Article"), res.getString("Name"), res.getString("Qty"), res.getString("Price")));
                            }
                            result = true;
                        }
                    } catch (SQLException e) {
                        MyUtil.errorToLog(this.getClass().getName(), e.getMessage());
                        Toast.makeText(getApplicationContext(), "ОШИБКА!\n\n"+e.getMessage(), Toast.LENGTH_LONG).show();
                        showProgress(false);
                        return;
                    }
                    if (result) {
                        setListBodyDocAdapter();
                    }else {
                        Toast.makeText(getApplicationContext(), "Товары не найдены!", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(getApplicationContext(), "Нет связи с сервером!", Toast.LENGTH_LONG).show();
            }
        }
        @Override
        protected void onCancelled() {
            mDbTask = null;
            showProgress(false);
        }
    }

    private class ObjectItem extends HashMap<String, String> {
        public static final String GOOD_ID = "GoodID";
        public static final String ARTICLE = "Article";
        public static final String NAME = "Name";
        public static final String QTY = "Qty";
        public static final String PRICE = "Price";
        // Конструктор с параметрами
        public ObjectItem(String goodid, String article, String name, String qty, String price) {
            super();
            super.put(GOOD_ID, goodid);
            super.put(ARTICLE, article);
            super.put(NAME, name);
            super.put(QTY, qty);
            super.put(PRICE, price);
        }
    }

    private class MySimpleAdapter extends SimpleAdapter {
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
//            TextView tv = null;
//            if (position==0) {
//                tv = (TextView) v.findViewById(R.id.tvArticle);
//                tv.setTypeface(null, Typeface.BOLD);
//                tv = (TextView) v.findViewById(R.id.tvPrice);
//                tv.setTypeface(null, Typeface.BOLD);
//                tv = (TextView) v.findViewById(R.id.tvName);
//                tv.setTypeface(null, Typeface.BOLD);
//            }else{
//                tv = (TextView) v.findViewById(R.id.tvArticle);
//                tv.setTypeface(null, Typeface.NORMAL);
//                tv = (TextView) v.findViewById(R.id.tvPrice);
//                tv.setTypeface(null, Typeface.NORMAL);
//                tv = (TextView) v.findViewById(R.id.tvName);
//                tv.setTypeface(null, Typeface.NORMAL);
//            }
            return v;
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
