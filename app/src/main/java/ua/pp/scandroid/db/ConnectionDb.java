package ua.pp.scandroid.db;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import ua.pp.scandroid.BuildConfig;
import ua.pp.scandroid.util.ConfigReader;
import ua.pp.scandroid.util.MyUtil;

public final class ConnectionDb{
    private static ConnectionDb instance    = null;
    private String              cnnString1  = "";
    private String              cnnString2  = "";
    private String              userName    = "";
    private String              password    = "";
    private Connection cnn      = null;
    private Statement stmt      = null;
    public int					serverID;
    public int                  userID;
    public int                  deviceID;
    public int                  status;
    public int                  accessLevel = -1;
    public int					clientID;
//    public BigDecimal           currentCheckID;
//    public BigDecimal           returnID;
//    public BigDecimal           returnIDFiscalNumber;
//    public int					matrixID;
//    public int					checkFlagReturn;
//    public int                  checkStatus;
//    public int                  checkTypePayment;
//    public String               checkCardID;
//    public BigDecimal           checkSumBase;
//    public BigDecimal           checkSumDiscount;
//    public BigDecimal           checkSum;
    //public sql.Date				checkDT_close;
//    private ResultSet resClientInfo;
//    private ResultSet           resCardInfo;
//    private ResultSet           resPromoInfo;
//    private ResultSet           resOrderInfo;
//    private ResultSet           resStickerInfo;
    private static ConfigReader config = ConfigReader.getInstance(null);

    //init cnn
    public static ConnectionDb getInstance() {
        if (instance == null) {
            try {
                instance = new ConnectionDb();
            } catch (Exception e) {
                instance = null;
                MyUtil.errorToLog(BuildConfig.APPLICATION_ID+".db.ConnectionDb", e);
                //DialogBoxs.viewError(e);
            }
        }
        return instance;
    }
    public static ConnectionDb getInstanceSilent() {
        if (instance == null) {
            try {
                instance = new ConnectionDb();
            } catch (Exception e) {
                instance = null;
                MyUtil.errorToLog(BuildConfig.APPLICATION_ID+".db.ConnectionDb", e);
            }
        }
        return instance;
    }
    private void Init(){
        ConfigReader config = ConfigReader.getInstance(null);
        //&amp;AutoCommit=true&amp;autoReconnect=true
//        cnnString1   = "jdbc:mysql://" + config.SERVER_ADDRESS_1 + ":" + config.SERVER_PORT + "/" + config.SERVER_DB + "?useCompression=true&autoReconnect=true&noAccessToProcedureBodies=true";
//        cnnString2   = "jdbc:mysql://" + config.SERVER_ADDRESS_2 + ":" + config.SERVER_PORT + "/" + config.SERVER_DB + "?useCompression=true&autoReconnect=true&noAccessToProcedureBodies=true";
        cnnString1   = "jdbc:mysql://" + config.SERVER_ADDRESS_1 + ":" + config.SERVER_PORT + "/" + config.SERVER_DB + "?autoReconnect=true&noAccessToProcedureBodies=true";
        //cnnString1   = "jdbc:mysql://" + config.SERVER_ADDRESS_1 + ":" + config.SERVER_PORT + "/" + config.SERVER_DB + "?autoReconnect=true";
        cnnString2   = "jdbc:mysql://" + config.SERVER_ADDRESS_2 + ":" + config.SERVER_PORT + "/" + config.SERVER_DB + "?autoReconnect=true&noAccessToProcedureBodies=true";
        //cnnString   = "jdbc:mariadb://" + config.SERVER_ADDRESS + ":" + config.SERVER_PORT + "/" + config.SERVER_DB + "?useCompression=true&allowMultiQueries=true";
        userName    = config.SERVER_DB;
        password    = "149521";
    }
    public boolean statusClosed(){
        try {
            return cnn.isClosed();
        } catch (SQLException ex) {
            //Logger.getLogger(ConnectionDb.class.getName()).log(Level.SEVERE, null, ex);
            MyUtil.errorToLog(this.getClass().getName(), ex);
            return true;
        }
    }
    public boolean statusValid() {
        try {
            return cnn.isValid(config.TIME_WAIT-2);
        } catch (SQLException ex) {
            MyUtil.errorToLog(this.getClass().getName(), ex);
            return false;
        }
    }
    public boolean statusValid(int timeout) {
        try {
            return cnn.isValid(timeout);
        } catch (SQLException ex) {
            MyUtil.errorToLog(this.getClass().getName(), ex);
            return false;
        }
    }
    public boolean startConnect() {
        boolean status = false;
//		java.util.Date dt = new java.util.Date();
//		System.out.println("1. поднимаем соединение: " + dt.toString());
        try {
            Init();
            Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception e) {
            MyUtil.errorToLog(this.getClass().getName(), e);
            throw new IllegalArgumentException("Error load driver.");
        }
        try {
//System.out.println(MyUtil.getCurrentDateTime(1) + " " + cnnString1);
            //System.out.println(cnnString1);
            cnn = DriverManager.getConnection(cnnString1, userName, password);
            serverID = 1;
            status = true;
        } catch (SQLException err) {
            MyUtil.errorToLog(this.getClass().getName(), err);
            try {
//System.out.println(MyUtil.getCurrentDateTime(1) + " " + cnnString2);
                cnn = DriverManager.getConnection(cnnString2, userName, password);
                serverID = 2;
                status = true;
            } catch (SQLException err2) {
                MyUtil.errorToLog(this.getClass().getName(), err2);
            }
        }
        return status;
    }
    protected ConnectionDb() {
//		java.util.Date dt = new java.util.Date();
//		System.out.println("0. поднимаем соединение: " + dt.toString());
        try {
            Init();
            //Log.i(this.getClass().getName(),cnnString1+" "+ userName+" "+ password);
            Class.forName("com.mysql.jdbc.Driver");
            //Class.forName("org.mariadb.jdbc.Driver");
        } catch (Exception e) {
            MyUtil.errorToLog(this.getClass().getName(), e);
            throw new IllegalArgumentException("Error load driver.");
        }
        try {
//System.out.println(MyUtil.getCurrentDateTime(1) + " " + cnnString1);
            //Log.i(this.getClass().getName(),cnnString1+" "+ userName+" "+ password);
            cnn = DriverManager.getConnection(cnnString1, userName, password);
            //Log.i(this.getClass().getName(),"connect ok");
            serverID = 1;
        } catch (SQLException err) {
            //Log.i(this.getClass().getName(),"connect bad");
            MyUtil.errorToLog(this.getClass().getName(), cnnString1 +"\n"+ err);
            try {
//System.out.println(MyUtil.getCurrentDateTime(1) + " " + cnnString2);
                cnn = DriverManager.getConnection(cnnString2, userName, password);
                serverID = 2;
            } catch (SQLException err2) {
                MyUtil.errorToLog(this.getClass().getName(), cnnString2 + "\n" + err2);
                throw new IllegalArgumentException("Error: " + err2);
            }
        }
    }
    public void close() {
        if (cnn != null) {
            try {
                cnn.close();
            } catch (SQLException ex) {
                MyUtil.errorToLog(this.getClass().getName(), ex);
            }
        }
    }
    public void destroy(){
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ex) {
                //throw new IllegalArgumentException("Error: " + ex);
                MyUtil.errorToLog(this.getClass().getName(), ex);
                stmt = null;
                return;
            }
            stmt = null;
        }
        if (cnn != null) {
            try {
                cnn.close();
            } catch (SQLException ex) {
                //throw new IllegalArgumentException("Error: " + ex);
                MyUtil.errorToLog(this.getClass().getName(), ex);
                cnn = null;
                return;
            }
            cnn = null;
        }
        instance    = null;
    }
    //users




    //scandroid
    public ResultSet getDataResultSet(String procedure, String action, String param) {
        if (cnn == null) {
            MyUtil.errorToLog(this.getClass().getName(), new IllegalArgumentException("getDataResultSet: parameter [cnn] cannot be null!"));
            return null;
        }
        try {
            //System.out.println("{call "+procedure+"(?,?,?)}");
            //CallableStatement cs = cnn.prepareCall("{call pr_login(?,?,?)}");
            CallableStatement cs = cnn.prepareCall("{call "+procedure+"(?,?,?)}");
            cs.setString(1, action);
            cs.registerOutParameter(2, Types.INTEGER);
            cs.setString(3, param);
            ResultSet res = cs.executeQuery();
//            res.absolute(1);
//            System.out.println(res.getString(1));
            return res;
        } catch (SQLException e) {
            //System.out.println("ERROR  "+e.getMessage());
            MyUtil.errorToLog(this.getClass().getName(), e);
            return null;
        }
    }

    public int getDataInt(String procedure, String action, String param) {
        if (cnn == null) {
            MyUtil.errorToLog(this.getClass().getName(), new IllegalArgumentException("getDataBoolean: parameter [cnn] cannot be null!"));
            return -1;
        }
        try {
            CallableStatement cs = cnn.prepareCall("{call "+procedure+"(?,?,?)}");
            cs.setString(1, action);
            cs.registerOutParameter(2, Types.INTEGER);
            cs.setString(3, param);
            return cs.getInt(2);
        } catch (SQLException e) {
            MyUtil.errorToLog(this.getClass().getName(), e);
            return -1;
        }
    }
}
