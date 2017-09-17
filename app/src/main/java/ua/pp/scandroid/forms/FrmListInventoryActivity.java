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
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

public class FrmListInventoryActivity extends AppCompatActivity{
    private DbTask mDbTask = null;
    private View mProgressView;
    private View mFormView;
    private ListView listview = null;
    private ArrayList<ObjectItem> listItems = new ArrayList<ObjectItem>();
    private ListAdapter adapter;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_inventory);

        mFormView = findViewById(R.id.form_list_doc);
        mProgressView = findViewById(R.id.list_doc_progress);

        FloatingActionButton fab_add = (FloatingActionButton) findViewById(R.id.fab_doc_invenory_add);
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDbTask != null) {
                    return;
                }
                showProgress(true);
                mDbTask = new DbTask(DbTask.INVENTORY_ADD_DOC);
                mDbTask.execute((Void) null);
            }
        });
        //gridview = (GridView) findViewById(R.id.gridview);
        listview = (ListView) findViewById(R.id.list_doc);
        //gridview.setAdapter(new ImageTextAdapter(this));
        //getDocList();
        listview.setOnItemClickListener(listviewOnItemClickListener);
    }

    @Override
    protected void onResume() {
        getDocList();
        super.onResume();
    }

    private void getDocList(){
        if (mDbTask != null) {
            return;
        }
        showProgress(true);
        mDbTask = new DbTask(DbTask.INVENTORY_LIST);
        mDbTask.execute((Void) null);
    }
    public void setListDocAdapter(){
        if (listItems.size()==0) return;
        adapter = new MySimpleAdapter(getApplicationContext(), listItems, R.layout.activity_list_inventory_cellgrid,
                new String[] { ObjectItem.USER_DOC_ID, ObjectItem.DOC_DT, ObjectItem.NOTES, ObjectItem.GOOD_CNT}, new int[] {
                R.id.textDocID, R.id.textDocDT, R.id.textNotes, R.id.textGoodCnt});
        listview.setAdapter(adapter);
        listview.setSelection(0);
        listview.smoothScrollToPosition(0);
        listview.setItemChecked(0, true);

        Toast.makeText(getApplicationContext(), "Для внесения товара\nвыберите документ!", Toast.LENGTH_SHORT).show();
    }

    private ListView.OnItemClickListener listviewOnItemClickListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            ObjectItem item = (ObjectItem) parent.getAdapter().getItem(position);
            String docid = item.get(ObjectItem.DOC_ID);
            String userdocid = item.get(ObjectItem.USER_DOC_ID);
            String docdt = item.get(ObjectItem.DOC_DT);
            String notes = item.get(ObjectItem.NOTES);
            // TODO открываем документ
            Intent i = new Intent(getApplicationContext(),
                    FrmDocInventoryActivity.class);
            // passing array index
            i.putExtra("docid", docid);
            i.putExtra("userdocid", userdocid);
            i.putExtra("docdt", docdt);
            i.putExtra("notes", notes);
            startActivity(i);

        }
    };

    // TODO Represents an asynchronous task ConnectionDB
    public class DbTask extends AsyncTask<Void, Void, Boolean> {
        private ConnectionDb cnn = null;
        private ResultSet res = null;
        private ConfigReader config = null;
        private String dbAction = "";
        public final static String INVENTORY_LIST = "inventory_list";
        public final static String INVENTORY_ADD_DOC = "inventory_add_doc";
        public final static String INVENTORY_DEL_DOC = "inventory_del_doc";

        public DbTask(String action) {
            dbAction = action;
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            if (dbAction.equals("")) return false;
            // TODO: attempt authentication against a network service.
            cnn = ConnectionDb.getInstance();
            if (cnn == null) return false;
            if (dbAction.equals(INVENTORY_LIST)) {
                res = cnn.getDataResultSet("pr_doc_android",dbAction,"UserID="+cnn.userID+"&Serial="+Build.SERIAL);
                return res!=null;
            }
            if (dbAction.equals(INVENTORY_ADD_DOC)) {
                //System.out.println(dbAction+",UserID="+cnn.userID+"&Serial="+Build.SERIAL+"&DeviceID="+cnn.deviceID);
                res = cnn.getDataResultSet("pr_doc_android",dbAction,"UserID="+cnn.userID+"&Serial="+Build.SERIAL);
                return res!=null;
            }
            return true;
        }
        @Override
        protected void onPostExecute(final Boolean success) {
            mDbTask = null;
            showProgress(false);
            if (success) {
                if (dbAction.equals(INVENTORY_LIST)) {
                    boolean result = false;
                    synchronized(listItems) {
                        listItems.clear();
                    }
                    try {
                        while (res.next()) {
                            //res.getString("DocID");
                            synchronized(listItems) {
                                listItems.add(new ObjectItem(res.getString("DocID"), res.getString("UserDocID"), res.getString("DT"), res.getString("Notes"), res.getString("CountGood")));
                            }
                            result = true;
                        }
                    } catch (SQLException e) {
                        MyUtil.errorToLog(this.getClass().getName(),e.getMessage());
                        Toast.makeText(getApplicationContext(), "Ошибка при заполнении таблицы!", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (result) {
                        setListDocAdapter();
                    }else {
                        Toast.makeText(getApplicationContext(), "Список документов пуст!", Toast.LENGTH_LONG).show();
                    }
                }
                if (dbAction.equals(INVENTORY_ADD_DOC)) {
                    boolean result = false;
                    try {
                        res.absolute(1);
                            System.out.println(res.getString("DocID")+" "+res.getString("UserDocID")+"  "+res.getBoolean("OperState"));
                            if (res.getBoolean("OperState")) {
                                synchronized (listItems) {
                                    listItems.add(0, new ObjectItem(res.getString("DocID"), res.getString("UserDocID"), res.getString("DT"), res.getString("Notes"), res.getString("CountGood")));
                                }
                                result = true;
                            }else {
                                MyUtil.errorDialog(getParent(),"ERROR!",res.getString("RU"),R.string.yes,0);
                                result = false;
                            }

                    } catch (SQLException e) {
                        MyUtil.errorToLog(this.getClass().getName(),e.getMessage());
                        Toast.makeText(getApplicationContext(), "Ошибка при заполнении таблицы!", Toast.LENGTH_LONG).show();
                        MyUtil.errorDialog(FrmListInventoryActivity.this, "Error: " + e.getErrorCode(), e.getMessage(), 0, R.string.cancel);
                        return;
                    }
                    if (result) {
                        setListDocAdapter();
                    }else {
                        Toast.makeText(getApplicationContext(), "Список документов пуст!", Toast.LENGTH_LONG).show();
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

    public class ObjectItem extends HashMap<String, String> {
        public static final String DOC_ID = "DocID";
        public static final String USER_DOC_ID = "Number";
        public static final String DOC_DT = "Date";
        public static final String NOTES = "Notes";
        public static final String GOOD_CNT = "Rows";
        // Конструктор с параметрами
        public ObjectItem(String docid, String userdocid, String docdt, String notes, String good_cnt) {
            super();
            super.put(DOC_ID, docid);
            super.put(USER_DOC_ID, userdocid);
            super.put(DOC_DT, docdt);
            super.put(NOTES, notes);
            super.put(GOOD_CNT, good_cnt);
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
                tv = (TextView) v.findViewById(R.id.textDocID);
//                tv.setBackground(getResources().getDrawable(R.drawable.border_line_white_first));
                tv.setTypeface(null, Typeface.BOLD);
                tv = (TextView) v.findViewById(R.id.textDocDT);
//                tv.setBackground(getResources().getDrawable(R.drawable.border_line_white_first));
                tv.setTypeface(null, Typeface.BOLD);
                tv = (TextView) v.findViewById(R.id.textGoodCnt);
//                tv.setBackground(getResources().getDrawable(R.drawable.border_line_white_first));
                tv.setTypeface(null, Typeface.BOLD);
                tv = (TextView) v.findViewById(R.id.textNotes);
//                tv.setBackground(getResources().getDrawable(R.drawable.border_line_gray_first));
                tv.setTypeface(null, Typeface.BOLD);
            }else{
                tv = (TextView) v.findViewById(R.id.textDocID);
//                tv.setBackground(getResources().getDrawable(R.drawable.border_line_white));
                tv.setTypeface(null, Typeface.NORMAL);
                tv = (TextView) v.findViewById(R.id.textDocDT);
//                tv.setBackground(getResources().getDrawable(R.drawable.border_line_white));
                tv.setTypeface(null, Typeface.NORMAL);
                tv = (TextView) v.findViewById(R.id.textGoodCnt);
//                tv.setBackground(getResources().getDrawable(R.drawable.border_line_white));
                tv.setTypeface(null, Typeface.BOLD);
                tv = (TextView) v.findViewById(R.id.textNotes);
//                tv.setBackground(getResources().getDrawable(R.drawable.border_line_gray));
                tv.setTypeface(null, Typeface.BOLD);
            }
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
