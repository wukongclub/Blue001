package lecho.lib.hellocharts.samples;

import android.app.AlertDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by guess on 2015/9/25.
 */
public class MyBluetoothService extends Service {


    public boolean threadFlag = true;
    MyThread myThread;
    CommandReceiver cmdReceiver;//继承自BroadcastReceiver对象，用于得到Activity发送过来的命令

    /**************service 命令*********/

    static final int CMD_START_DISCOVERY = 0x00;
    static final int CMD_STOP_DISCOVERY = 0x01;
    static final int CMD_TRY_LINK = 0x02;
    static final int CMD_CLOSE_SOCKET = 0x03;
    static final int CMD_GET_DATA = 0x04;
    static final int CMD_SETTING_RECORD= 0x05;

    static final int CMD_REQUES_RECORD= 0x06;



    static final int CMD_TRYLINK_RETURN = 0x00;
    static final int CMD_RETURN_DATA = 0x01;



    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private InputStream inStream = null;
    public  boolean bluetoothFlag  = true;
    private boolean testComunnicationFlag;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    private String nowSenserAddress; // <==要连接的蓝牙设备MAC地址
    private String nowSenserName; // <==要连接的蓝牙设备MAC地址

    private int nowValue;

    private int CMD_RECORD_TIME_VALUE;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    //初始化蓝牙
    private void initBluetooth()
    {
        //得到BluetoothAdapter对象
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter!=null)
        {
            //判断蓝牙是否可用
            if(!mBluetoothAdapter.isEnabled())
                mBluetoothAdapter.enable();
        }
        else
        {
            //没有蓝牙
//            new AlertDialog.Builder(Main2Activity.this)
//                    .setTitle("错误")
//                    .setMessage("您的设备没有蓝牙")
//                    .setPositiveButton("好",
//                            new DialogInterface.OnClickListener() {
//                                public void onClick(
//                                        DialogInterface dialoginterface, int i) {
//                                }
//                            })
//                    .show();
        }
    }

    @Override
    public void onCreate() {
        initBluetooth();
        super.onCreate();
        System.out.println("MyBlueServiceOnCreate----->");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        System.out.println("MyBlueServiceonStartCommand----->");

        registerReceiver(new CommandReceiver(), new IntentFilter("android.intent.action.cmd"));

//        doJob();//调用方法启动线程
        return super.onStartCommand(intent, flags, startId);
    }


    public void doJob(){

        threadFlag = true;
        myThread = new MyThread();
        myThread.start();

    }


    public class MyThread extends Thread{
        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();

            // System.out.println("readThread...connectDevice");
            while(threadFlag){

                System.out.println("readThread...");
                int value = readByte();
                System.out.println(value);
                if(value != -1){
                    //showToast(value + "");

                }

                try{
                    Thread.sleep(5);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }













    public void sendCmd(int cmd)//串口发送数据
    {
        System.out.print("hello ");
        if(!bluetoothFlag){
            return;
        }
        System.out.print("sendCmd ");

        byte[] msgBuffer=new byte[0];
        if(cmd==1||cmd==2)
        {
            msgBuffer= new byte[22];
            if(cmd==1) {
                msgBuffer[0] = 0x1A;
                msgBuffer[1] = 0x00;
                msgBuffer[2] = 0x00;
                msgBuffer[3] = 0x00;
                msgBuffer[4] = 0x00;
                msgBuffer[5] = 0x0F;
                msgBuffer[6] = 0x0F;
                msgBuffer[7] = 0x0F;
                msgBuffer[8] = 0x0F;
                msgBuffer[9] = 0x00;
                msgBuffer[10] = 0x00;
                msgBuffer[11] = 0x00;
                msgBuffer[12] = 0x01;
                msgBuffer[13] = 0x00;
                msgBuffer[14] = 0x00;
                msgBuffer[15] = 0x00;
                msgBuffer[16] = 0x00;
                msgBuffer[17] = 0x01;
                msgBuffer[18] = 0x01;
                msgBuffer[19] = 0x0d;
                msgBuffer[20] = 0x00;
                msgBuffer[21] = 0x1D;
            }
            else {
                msgBuffer[0] = 0x1A;
                msgBuffer[1] = 0x00;
                msgBuffer[2] = 0x00;
                msgBuffer[3] = 0x00;
                msgBuffer[4] = 0x00;
                msgBuffer[5] = 0x00;
                msgBuffer[6] = 0x00;
                msgBuffer[7] = 0x00;
                msgBuffer[8] = 0x01;
                msgBuffer[9] = 0x01;
                msgBuffer[10] = 0x00;
                msgBuffer[11] = 0x00;
                msgBuffer[12] = 0x03;
                msgBuffer[13] = 0x00;
                msgBuffer[14] = 0x00;
                msgBuffer[15] = 0x00;
                msgBuffer[16] = 0x00;
                msgBuffer[17] = 0x08;
                msgBuffer[18] = 0x09;
                msgBuffer[19] = 0x00;
                msgBuffer[20] = 0x0B;
                msgBuffer[21] = 0x1D;
            }
        }
        else if(cmd==CMD_SETTING_RECORD)
        {
            msgBuffer = new byte[30];
            msgBuffer[0] = 0x1A;
            msgBuffer[1] = 0x00;
            msgBuffer[2] = 0x00;
            msgBuffer[3] = 0x00;
            msgBuffer[4] = 0x00;
            msgBuffer[5] = 0x00;
            msgBuffer[6] = 0x00;
            msgBuffer[7] = 0x00;
            msgBuffer[8] = 0x01;
            msgBuffer[9] = 0x01;
            msgBuffer[10] = 0x00;
            msgBuffer[11] = 0x00;
            msgBuffer[12] = 0x09;
            msgBuffer[13] = 0x00;
            msgBuffer[14] = 0x00;
            msgBuffer[15] = 0x00;
            msgBuffer[16] = 0x04;

            if(CMD_RECORD_TIME_VALUE==0)
            {
                System.out.println("开始设置请求参数1");
                msgBuffer[17] = 0x0C;
                msgBuffer[18] = 0x00;
                msgBuffer[19] = 0x05;
                msgBuffer[20] = 0x0D;
                msgBuffer[21] = 0x00;
                msgBuffer[22] = 0x00;
                msgBuffer[23] = 0x00;
                msgBuffer[24] = 0x00;
                msgBuffer[25] = 0x00;
                msgBuffer[26] = 0x00;
                msgBuffer[27] = 0x00;
                msgBuffer[28] = 0x05;
            }
            msgBuffer[29] = 0x1D;
        }
        else if(cmd==CMD_RETURN_DATA)
        {
            System.out.println("开始设接收数据");
            msgBuffer= new byte[22];
                msgBuffer[0] = 0x1A;
                msgBuffer[1] = 0x00;
                msgBuffer[2] = 0x00;
                msgBuffer[3] = 0x00;
                msgBuffer[4] = 0x00;
                msgBuffer[5] = 0x00;
                msgBuffer[6] = 0x00;
                msgBuffer[7] = 0x00;
                msgBuffer[8] = 0x01;
                msgBuffer[9] = 0x01;
                msgBuffer[10] = 0x00;
                msgBuffer[11] = 0x00;
                msgBuffer[12] = 0x0B;
                msgBuffer[13] = 0x00;
                msgBuffer[14] = 0x00;
                msgBuffer[15] = 0x00;
                msgBuffer[16] = 0x00;
                msgBuffer[17] = 0x00;
                msgBuffer[18] = 0x08;
                msgBuffer[19] = 0x0C;
                msgBuffer[20] = 0x09;
                msgBuffer[21] = 0x1D;
        }
        try {
            outStream.write(msgBuffer, 0, msgBuffer.length);
            outStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeSocket()
    {
        //IsLinkSenser="F";
        try {
            btSocket.close();
            bluetoothFlag = false;
        } catch (IOException e2) {
            System.out.println("连接没有建立，无法关闭套接字！");
        }
    }

    //蓝牙连接函数
    private int tryLinkSenser()
    {
        int returnValue=0;

        System.out.println("正在尝试连接蓝牙设备，请稍后···· "+nowSenserAddress);
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(nowSenserAddress);
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            System.out.println("套接字创建失败！");
            bluetoothFlag = false;
        }
        System.out.println("成功连接蓝牙设备！");
        try {
            System.out.println("正在建立链接");
            btSocket.connect();
            System.out.println("连接成功建立，可以开始操控了!");
            bluetoothFlag = true;
            //returnValue=1;
            testComunnicationFlag=true;

        } catch (IOException e) {
            System.out.println("WTF");
            testComunnicationFlag=false;
            returnValue =2;
            closeSocket();
        }

        if (bluetoothFlag) {
            try {
                inStream = btSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            } //绑定读接口
            try {
                outStream = btSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            } //绑定写接口


            if(testComunnicationFlag==true)
            {
                System.out.println("进入test");
                testComunnicationFlag=false;
                sendCmd(1);
                System.out.println("nima");
            }

            try {
                System.out.println("T1");
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            try
            {
                int length = inStream.available();

                if(length==30) {
                    int[] msgBuffer = new int[length];

                    for (int i = 0; i < length; i++) {
                        int str = inStream.read();
                        msgBuffer[i] = str;
                    }
                    System.out.println("T3");
                    for (int i = 0; i < length; i++) {
                        System.out.print(msgBuffer[i] + " ");
                    }
                    System.out.println("T4");

                    if((msgBuffer[11]<<4|msgBuffer[12])==2)
                    {
                        System.out.println("vvdd ");
                        returnValue=1;
                    }
                    else {
                        returnValue =2;
                        closeSocket();
                    }
                }
                else {
                    returnValue = 2;
                    closeSocket();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                System.out.println("e1");
            }
            System.out.println("ovt");
        }
        return returnValue;
    }

    public class MyLinkThread extends Thread{
        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();
            //System.out.println("aa " + tryLinkSenser());

            sendBroadcastToActivity(CMD_TRYLINK_RETURN,tryLinkSenser());
        }
    }

    public void sendBroadcastToActivity(int cmd,int value){

        // System.out.println("showToast");
        Intent intent = new Intent();
        if(cmd==0)
        {
            intent.putExtra("cmd", CMD_TRYLINK_RETURN);
            intent.putExtra("state", value);
            intent.setAction("android.intent.action.ServiceToMain");
        }

        else if(cmd==CMD_RETURN_DATA)
        {
            intent.putExtra("cmd", CMD_RETURN_DATA);
            intent.putExtra("value", value);
            intent.setAction("android.intent.action.ServiceToLineChart");
        }




        sendBroadcast(intent);
    }








    private int tryGetData()
    {
        int returnValue=0;

        System.out.println("蓝牙状态 in 实时请求数据"+bluetoothFlag);
        if (bluetoothFlag) {
            sendCmd(2);
            try {
                System.out.println("等待");
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try
            {
                int length = inStream.available();

                if(length==30) {
                    byte[] msgBuffer = new byte[length];

                    for (int i = 0; i < length; i++) {
                        byte str = (byte)inStream.read();
                        msgBuffer[i] = str;
                    }

                    for (int i = 0; i < length; i++) {
                        System.out.print(msgBuffer[i] + " ");
                    }




                    nowValue=(msgBuffer[25]<<12|msgBuffer[26]<<8|msgBuffer[27]<<4|msgBuffer[28]);

                    System.out.println("nowValue "+nowValue);
                    returnValue = 1;

                }
                else {
                    returnValue = 2;

                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return returnValue;
    }

    private void trySettingRecordData()
    {
        int returnValue=0;

        System.out.println("蓝牙状态 in 设置时间 "+bluetoothFlag);
        if (bluetoothFlag) {
            sendCmd(CMD_SETTING_RECORD);
            try {
                System.out.println("等待");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try
            {
                int length = inStream.available();
                System.out.println("接收到数据长度 "+length);
                if(length==22) {
                    byte[] msgBuffer = new byte[length];

                    for (int i = 0; i < msgBuffer.length; i++) {
                        byte str = (byte)inStream.read();
                        msgBuffer[i] = str;
                    }

                    for (int i = 0; i < msgBuffer.length; i++) {
                        System.out.print(msgBuffer[i] + " ");
                    }


                    returnValue = 1;

                }
                else {
                    returnValue = 2;

                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        //return  returnValue;
    }

    private void tryGetRecordData()
    {
        int returnValue=0;

        System.out.println("蓝牙状态 in 请求历史数据 "+bluetoothFlag);
        if (bluetoothFlag) {
            sendCmd(CMD_RETURN_DATA);
            try {
                System.out.println("等待");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try
            {
                int length = inStream.available();

                if(length>0) {
                    byte[] msgBuffer = new byte[4096];

                    for (int i = 0; i < msgBuffer.length; i++) {
                        byte str = (byte)inStream.read();
                        msgBuffer[i] = str;
                    }

                    for (int i = 0; i < msgBuffer.length; i++) {
                        System.out.print(msgBuffer[i] + " ");
                    }


                    returnValue = 1;

                }
                else {
                    returnValue = 2;

                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        //return  returnValue;
    }

    public class MyGetDataThread extends Thread{
        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();
            //System.out.println("aa " + tryLinkSenser());


            if(tryGetData()==1)
                sendBroadcastToActivity(CMD_RETURN_DATA,nowValue);

        }
    }
    public class MyStartRecordThread extends Thread{
        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();
            //System.out.println("aa " + tryLinkSenser());

            trySettingRecordData();
//            if (bluetoothFlag)
//                sendCmd(CMD_START_RECORD);
        }
    }
    public class MyRequestRecordThread extends Thread{
        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();
            //System.out.println("aa " + tryLinkSenser());
            tryGetRecordData();
        }
    }





    //接收Activity传送过来的命令
    private class CommandReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals("android.intent.action.cmd")){
                int cmd = intent.getIntExtra("cmd", -1);//获取Extra信息

                if(cmd==CMD_START_DISCOVERY)
                {
                    mBluetoothAdapter.startDiscovery();
                }
                else if(cmd==CMD_STOP_DISCOVERY)
                {
                    if(mBluetoothAdapter.isDiscovering())
                        mBluetoothAdapter.cancelDiscovery();
                }
                else if(cmd==CMD_TRY_LINK)
                {
                    nowSenserAddress = intent.getStringExtra("address");
                    nowSenserName =  intent.getStringExtra("name");
                    new MyLinkThread().start();
                }
                else if(cmd==CMD_CLOSE_SOCKET)
                {
                    closeSocket();
                }

                else if(cmd==CMD_GET_DATA)
                {
                    System.out.println("GGeeetttt");
                    new MyGetDataThread().start();
                }
                else if(cmd==CMD_SETTING_RECORD)
                {
                    System.out.println("开始设置请求时间");
                    CMD_RECORD_TIME_VALUE = intent.getIntExtra("value", -1);//获取Extra信息
                    new MyStartRecordThread().start();
                }
                else if(cmd==CMD_RETURN_DATA)
                {
                    System.out.println("开始取得数据");
                    new MyRequestRecordThread().start();
                }
            }
        }
    }


        public int readByte(){//return -1 if no data
        int ret = -1;
        if(!bluetoothFlag){
            return ret;
        }
        try {
            ret = inStream.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }












    public void stopService(){//停止服务
        threadFlag = false;//停止线程
        stopSelf();//停止服务
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(cmdReceiver);//取消注册的CommandReceiver
        threadFlag = false;
        boolean retry = true;
        while(retry){
            try{
                myThread.join();
                retry = false;
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
