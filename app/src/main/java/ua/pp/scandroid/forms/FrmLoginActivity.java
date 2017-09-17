package ua.pp.scandroid.forms;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.ResultSet;
import java.sql.SQLException;

import ua.pp.scandroid.R;
import ua.pp.scandroid.db.ConnectionDb;
import ua.pp.scandroid.util.ConfigReader;
import ua.pp.scandroid.util.MyUtil;

public class FrmLoginActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{

    private static int REQUEST_PERMISSIONS = 5;
    private static int REQUEST_READ_PHONE_STATE = 1;
    private static int REQUEST_WRITE_EXTERNAL_STORAGE = 2;
    private static int REQUEST_CAMERA = 3;

    private UserLoginTask mAuthTask = null;

    ConfigReader config = null;
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mPassword2View;
    private View mPassword2Layot;
    private View mProgressView;
    private View mLoginFormView;
    private Button mButtonSignIn;
    private Button mButtonRegister;
    private Button mButtonBack;
    private String mParam = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        config = ConfigReader.getInstance(getBaseContext());
        if (config == null) {
            MyUtil.errorDialog(getParent(),"ERROR!",R.string.message_no_config,R.string.ok,0);
            return;
        }

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPassword2View = (EditText) findViewById(R.id.password2);
        mPassword2Layot = findViewById(R.id.password2_layot);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mButtonSignIn = (Button) findViewById(R.id.button_sign_in);
        mButtonSignIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mButtonBack = (Button) findViewById(R.id.button_back);
        mButtonBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mButtonSignIn.setVisibility(View.VISIBLE);
                mPassword2Layot.setVisibility(View.GONE);
                mButtonRegister.setText(R.string.action_register);
                mButtonBack.setVisibility(View.GONE);
            }
        });

        mButtonRegister = (Button) findViewById(R.id.button_register);
        mButtonRegister.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mButtonRegister.getText()==getString(R.string.action_register)) {
                    mButtonSignIn.setVisibility(View.GONE);
                    mPassword2Layot.setVisibility(View.VISIBLE);
                    mButtonRegister.setText(R.string.action_register2);
                    mButtonBack.setVisibility(View.VISIBLE);
                    MyUtil.alertDialog(FrmLoginActivity.this, "INFORMATION!", R.string.message_register_form_fill, R.string.ok, 0);
                }else{
                    attemptRegister();
                    //MyUtil.alertDialog(FrmLoginActivity.this, "Register OK!", R.string.action_register2, R.string.ok, 0);
                }
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        myPermission();

        if (Build.SERIAL.length()==0){
            MyUtil.alertDialog(getParent(),"ERROR!",R.string.message_no_serial,R.string.ok,0);
            finish();
        }

        mEmailView.setText("1@1.1");
        mPasswordView.setText("111111");
        mPassword2View.setText("111111");

    }

    public void myPermission(){
//System.out.println("CAMERA:"+ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA));
//System.out.println("CAMERA2:"+ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA));
//System.out.println("READ_PHONE_STATE:"+ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE));
//System.out.println("READ_PHONE_STATE2:"+ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE));
//System.out.println("WRITE_EXTERNAL_STORAGE:"+ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE));
//System.out.println("WRITE_EXTERNAL_STORAGE2:"+ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE));
        //TODO проверка разрешений на доступ
        String IMEI = "";
        String line1number = "";
        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED ){
            config.PERMISSION_CAMERA = true;
        }
        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED ) {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null){
                IMEI = tm.getDeviceId();
                line1number = tm.getLine1Number();
            }
        }
        mParam = "&Device="+Build.MODEL+"&Android="+Build.VERSION.RELEASE+"&Serial="+Build.SERIAL+"&IMEI="+IMEI+"&Line1="+line1number;
//System.out.println("mParam: "+mParam);
        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ) {
            config.PERMISSION_WRITE_EXTERNAL_STORAGE = true;
        }

        //TODO запрос разрешений на доступ
        if (    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                ) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//System.out.println("requestCode: "+requestCode+"    grantResults.length: "+grantResults.length);
        if(requestCode == REQUEST_PERMISSIONS){
            boolean showMessage = false;
            for(int i = 0; i < grantResults.length; i++){
                if (grantResults[i]!=PackageManager.PERMISSION_GRANTED){
                    showMessage = true;
                    break;
                }
            }
            System.out.println("showMessage: "+showMessage);
            if ( ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED ){
                config.PERMISSION_CAMERA = true;
            }
            if ( ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED ) {
                String IMEI = "";
                String line1number = "";
                TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (tm != null){
                    IMEI = tm.getDeviceId();
                    line1number = tm.getLine1Number();
                }
                mParam = "&Device="+Build.MODEL+"&Android="+Build.VERSION.RELEASE+"&Serial="+Build.SERIAL+"&IMEI="+IMEI+"&Line1="+line1number;
//System.out.println("mParam: "+mParam);
            }
            if ( ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ) {
                config.PERMISSION_WRITE_EXTERNAL_STORAGE = true;
            }

            if (showMessage){
                if (    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        ) {
                    MyUtil.dialogActivity(this,"ВНИМАНИЕ!","Для работы приложения\nтребуются разрешения:\n\nКАМЕРА - для сканирования\n\nSDCARD - для сохранения настроек\n\nСТАТУС телефона - для идентификации устройства!","Соласен","Отмена");
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //if (data == null) {return;}
        if (requestCode==MyUtil.DIALOG_ACTIVITY){
            //String result = data.getStringExtra("result");
            if(resultCode==-1) myPermission();
            if(resultCode==0) finish();
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        //String terminaid = mTerminalView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(UserLoginTask.LOGIN);
            mAuthTask.execute((Void) null);
        }
    }

    private void attemptRegister() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mPassword2View.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String password2 = mPassword2View.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (!TextUtils.isEmpty(password2) && !isPasswordValid(password2)) {
            mPassword2View.setError(getString(R.string.error_invalid_password));
            focusView = mPassword2View;
            cancel = true;
        }else if (!password.equals(password2)) {
            mPassword2View.setError(getString(R.string.error_invalid_password2));
            focusView = mPassword2View;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(UserLoginTask.REGISTER);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() >= 6;
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

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private ConnectionDb cnn = null;
        private ResultSet res = null;

        public final static String LOGIN = "login";
        public final static String REGISTER = "register";

        private String dbAction = "";
        private boolean loginStatus = false;
        private boolean registerStatus = false;

        private final String mEmail;
        private final String mPassword;
        private final String mPassword2;

        UserLoginTask(String action) {
            mEmail = mEmailView.getText().toString();
            mPassword = mPasswordView.getText().toString();
            mPassword2 = mPassword2View.getText().toString();
            dbAction = action;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (dbAction.equals("")) return false;
            // TODO: attempt authentication against a network service.
            cnn = ConnectionDb.getInstance();
            if (cnn == null) return false;
            if (dbAction.equals(LOGIN)) {
                //System.out.println(dbAction+",  Email="+mEmail+"&Password="+mPassword+mParam);
                res = cnn.getDataResultSet("pr_login",dbAction,"Email="+mEmail+"&Password="+mPassword+mParam);
                loginStatus = (res!=null);
                return res!=null;
            }
            if (dbAction.equals(REGISTER)) {
                //System.out.println(dbAction+",  Email="+mEmail+"&Password="+mPassword+"&Password2="+mPassword2+mParam);
                res = cnn.getDataResultSet("pr_login",dbAction,"Email="+mEmail+"&Password="+mPassword+"&Password2="+mPassword2+mParam);
                registerStatus = (res!=null);
                return res!=null;
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                if (dbAction.equals(LOGIN)) {
                    if (loginStatus) {
                        try {
                            res.absolute(1);
//                            MyUtil.alertDialog(FrmLoginActivity.this, "Info", res.getString(1) + "\n" + res.getString(2) + "\n" + res.getString(3) + "\n" + res.getString(4) + "\n" + res.getString(5) + "\n" + res.getString(6) + "\n" + res.getString(7), 0, R.string.cancel);
                            cnn.userID = res.getInt("_UserID");
                            cnn.deviceID = res.getInt("_DeviceID");
                            cnn.accessLevel = res.getInt("_AccessLevel");
                            cnn.status = res.getInt("_status");
                            Intent intent = new Intent(getApplicationContext(), FrmMenuActivity.class);
                            //Intent intent = new Intent(getApplicationContext(), FrmCameraActivity.class);
                            startActivity(intent);
                            showProgress(false);
                            finish();
                        } catch (SQLException e) {
                            cnn.userID = 0;
                            cnn.deviceID = 0;
                            cnn.accessLevel = 0;
                            cnn.status = -1;
                            showProgress(false);
                            MyUtil.errorDialog(FrmLoginActivity.this, "Error: " + e.getErrorCode(), e.getMessage(), 0, R.string.cancel);
                        }
                        //                    Toast.makeText(getApplicationContext(), "Вход успешный!", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        showProgress(false);
                        mPasswordView.setError(getString(R.string.error_incorrect_password));
                        mPasswordView.requestFocus();
                        return;
                    }
                }
                if (dbAction.equals(REGISTER)) {
                    showProgress(false);
                    if (registerStatus) {
                        try {
                            res.absolute(1);
                            MyUtil.alertDialog(FrmLoginActivity.this, "Info", res.getString(1) + "\n" + res.getString(2) + "\n" + res.getString(3), 0, R.string.cancel);
                        } catch (SQLException e) {
                            MyUtil.errorDialog(FrmLoginActivity.this, "Error: " + e.getErrorCode(), e.getMessage(), 0, R.string.cancel);
                        }
                        //                    Toast.makeText(getApplicationContext(), "Вход успешный!", Toast.LENGTH_SHORT).show();
                        //                    Intent intent = new Intent(getApplicationContext(), FrmInventoryListActivity.class);
                        //                    startActivity(intent);
                        //                    finish();
                    } else {
                        mPasswordView.setError(getString(R.string.error_incorrect_password));
                        mPasswordView.requestFocus();
                    }
                    return;
                }
            } else {
                showProgress(false);
                Toast.makeText(getApplicationContext(), "Нет связи с сервером!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

