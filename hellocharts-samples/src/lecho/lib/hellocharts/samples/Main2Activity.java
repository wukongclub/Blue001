package lecho.lib.hellocharts.samples;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main2Activity extends AppCompatActivity {


    ///Scan Button
    private TasksCompletedView mTasksView;
    private int mTotalProgress;
    private int mCurrentProgress;
    private Thread scanDeviceThread;
    private int scanDeviceNum;
    ///

    ///List
    private ExpandableListView senserList = null;
    //创建一级条目标题
    private Map<String, String> title_1;
    //创建一级条目容器
    private List<Map<String, String>> mySenserGroup ;
    //子条目内容
    private List<List<Map<String, String>>> mySenserChildName;
    private List<Map<String, String>> mySenserChildNameContaner;
    private List<List<Map<String, String>>> mySenserChildMac;
    private List<Map<String, String>> mySenserChildMacContaner;



    private String nowSenserAddress; // <==要连接的蓝牙设备MAC地址
    private String nowSenserName; // <==要连接的蓝牙设备MAC地址
    private String linkedSenserAddress;
    private String linkedSenserName;




    private String IsLinkSenser;
    private MyExpListAdapter senserListViewAdapter;





    static final int CMD_START_DISCOVERY = 0x00;
    static final int CMD_STOP_DISCOVERY = 0x01;
    static final int CMD_TRY_LINK = 0x02;
    static final int CMD_CLOSE_SOCKET = 0x03;

    static final int CMD_TRYLINK_RETURN = 0x00;


    //初始化列表
    private void initSenserList()
    {
        title_1 = new HashMap<>();
        title_1.put("group", String.valueOf("扫描到的传感器"));

        mySenserGroup = new ArrayList<>();
        mySenserGroup.add(title_1);
    }

    //初始化扫描按钮
    private void initScanButton()
    {
        mTotalProgress = 100;
        mCurrentProgress = 0;
        mTasksView = (TasksCompletedView) findViewById(R.id.tasks_view);
        mTasksView.setOnClickListener(new mTasksViewClickListener());
        mTasksView.setTextPaint("Scan");
        scanDeviceThread = new Thread(new ProgressRunable());
    }

    //初始化蓝牙接收器
    private void  initBluetoothReciver()
    {
        //设置设备被找到广播
        registerReceiver(new BluetoothReciver(), new IntentFilter(BluetoothDevice.ACTION_FOUND));
        //设置扫描结束广播
        registerReceiver(new BluetoothReciver(), new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        registerReceiver(new BluetoothReciver(), new IntentFilter("android.intent.action.ServiceToMain"));
    }

    //初始化窗口
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        //传感器列表
        senserList = (ExpandableListView) findViewById(R.id.expList);
        senserList.setOnChildClickListener(new mySenserListChildOnClickListener());

        IsLinkSenser="F";

        initSenserList(); //初始化列表
        initScanButton(); //初始化扫描按钮
        initBluetoothReciver(); //初始化蓝牙接收器

        Main2Activity.this.startService(new Intent(Main2Activity.this, MyBluetoothService.class));
    }

    //发送广播
    public void sendCmdBroadcast(int cmd ,String command, String value){
        Intent intent = new Intent();//创建Intent对象
        intent.setAction("android.intent.action.cmd");
        if(cmd==CMD_START_DISCOVERY) {
            intent.putExtra("cmd", CMD_START_DISCOVERY);
        }
        else if(cmd==CMD_STOP_DISCOVERY)
        {
            intent.putExtra("cmd", CMD_STOP_DISCOVERY);
        }
        else if(cmd==CMD_TRY_LINK)
        {
            intent.putExtra("cmd", CMD_TRY_LINK);
            intent.putExtra("address", nowSenserAddress);
            intent.putExtra("name", nowSenserName);
        }
        else if(cmd==CMD_CLOSE_SOCKET)
        {
            intent.putExtra("cmd",CMD_CLOSE_SOCKET);
        }

        sendBroadcast(intent);//发送广播
    }


    //扫描 "按钮" 监听器
    private class mTasksViewClickListener implements OnClickListener
    {
        @Override
        public void onClick(View v) {
            if(scanDeviceThread.isAlive()==false && IsLinkSenser!="L")
            {
                mySenserChildName = new ArrayList<List<Map<String,String>>>();
                mySenserChildNameContaner = new ArrayList<Map<String,String>>();
                mySenserChildMac = new ArrayList<List<Map<String,String>>>();
                mySenserChildMacContaner = new ArrayList<Map<String,String>>();
                if(IsLinkSenser!="T")
                {
                    scanDeviceNum=0;
                    senserList.setVisibility(View.GONE);
                }
                else if(IsLinkSenser=="T")
                {
                    Map<String, String> content = new HashMap<String, String>();
                    content.put("child", linkedSenserName);
                    Map<String, String> contentMac = new HashMap<String, String>();
                    contentMac.put("childMac", linkedSenserAddress);

                    mySenserChildNameContaner.add(content);
                    mySenserChildName.add(mySenserChildNameContaner);
                    mySenserChildMacContaner.add(contentMac);
                    mySenserChildMac.add(mySenserChildMacContaner);
                }


                scanDeviceThread = new Thread(new ProgressRunable());
                scanDeviceThread.start();


                sendCmdBroadcast(0,"","");
                //bluetoothAdapter.startDiscovery();
            }
        }
    }

    //扫描ButtonThread
    class ProgressRunable implements Runnable
    {
        @Override
        public void run() {
            while (mCurrentProgress < mTotalProgress) {
                mCurrentProgress += 1;
                mTasksView.setProgress(mCurrentProgress);
                try {
                    Thread.sleep(125);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    //处理返回数据
    private void dealReturnState(int state)
    {
        switch (state) {
            case 1:
                IsLinkSenser="T";
                linkedSenserAddress=nowSenserAddress;
                linkedSenserName=nowSenserName;

                Toast.makeText(getApplicationContext(), "已建立与 “"+nowSenserName+"” 的链接。",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Main2Activity.this, LineColumnDependencyActivity.class);
                startActivity(intent);

//                new AlertDialog.Builder(Main2Activity.this)
//                        .setTitle("连接成功")
//                        .setMessage("已建立与 “"+nowSenserName+"” 的链接。")
//                        .setPositiveButton("好",
//                                new DialogInterface.OnClickListener() {
//                                    public void onClick(
//                                            DialogInterface dialoginterface, int i) {
//
//                                        Intent intent = new Intent(Main2Activity.this, LineChartActivity.class);
//
//                                        startActivity(intent);
//                                    }
//                                })
//                        .show();
                break;

            case 2:
                IsLinkSenser="F";
                new AlertDialog.Builder(Main2Activity.this)
                        .setTitle("连接不成功")
                        .setMessage("请确定 “"+nowSenserName+"” 已打开而且在通信范围内。")
                        .setPositiveButton("好",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialoginterface, int i) {
                                    }
                                })
                        .show();
                break;
        }
    }


    //蓝牙广播接收器
    private class BluetoothReciver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action= intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action))
            {
                BluetoothDevice device =intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                senserList.setVisibility(View.VISIBLE);


                Map<String, String> content = new HashMap<String, String>();
                content.put("child", device.getName());
                Map<String, String> contentMac = new HashMap<String, String>();
                contentMac.put("childMac", device.getAddress());

                mySenserChildNameContaner.add(content);
                mySenserChildName.add(mySenserChildNameContaner);
                mySenserChildMacContaner.add(contentMac);
                mySenserChildMac.add(mySenserChildMacContaner);

                if(scanDeviceNum==0)
                {
                    senserListViewAdapter= upDataListData(senserList,mySenserGroup,mySenserChildName,mySenserChildMac
                            ,R.layout.groups_unpair,new int[]{R.id.groupUnpairedNameTo});
                }
                else
                    senserListViewAdapter.notifyDataSetChanged();

                scanDeviceNum++;
            }

            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
                showScanedDialog();


            else if(intent.getAction().equals("android.intent.action.ServiceToMain")){
                System.out.println("returnnnn");
                Bundle bundle = intent.getExtras();
                int cmd = bundle.getInt("cmd");


                if(cmd == CMD_TRYLINK_RETURN) {
                    dealReturnState(bundle.getInt("state"));
                }

            }

        }
    }

    //扫描结束对话框
    private void showScanedDialog()
    {
        if(scanDeviceNum!=0)
        {
            String text="扫描结束发现 "+scanDeviceNum +" 个传感器";

            Toast.makeText(getApplicationContext(), text,
                    Toast.LENGTH_SHORT).show();
            mTasksView.setProgress(0);
            mTasksView.setTextPaint("Scan");
            mCurrentProgress = 0;
        }
        else
        {
            new AlertDialog.Builder(Main2Activity.this)
                    .setTitle("扫描完成")
                    .setMessage("未发现可用传感器")
                    .setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialoginterface, int i) {
                                    System.out.println("vvvv");
                                    mTasksView.setProgress(0);
                                    mTasksView.setTextPaint("Scan");
                                    mCurrentProgress = 0;
                                }
                            }).show();

        }
    }










    //我的传感器配对按钮监听器
    class mySenserListChildOnClickListener implements ExpandableListView.OnChildClickListener
    {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

            if(scanDeviceThread.isAlive()) {
                scanDeviceThread.interrupt();
                mCurrentProgress = 100;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sendCmdBroadcast(1, "", "");
            }

            List<Object> myObjectList = senserListViewAdapter.getChild(groupPosition, childPosition);

            Map<String, String> SenserName = (Map<String, String>) myObjectList.get(0);
            Map<String, String> SenserAddress = (Map<String, String>) myObjectList.get(1);


            // selaPaired.setSenserState(v, "T", childPosition);

            if(IsLinkSenser=="F") {
                //是否使用传感器标识符
                nowSenserAddress=SenserAddress.get("childMac");
                nowSenserName=SenserName.get("child");
                IsLinkSenser = "L";
                Toast.makeText(getApplicationContext(), "正在尝试与 “"+nowSenserName+"” 建立连接，请稍后····",Toast.LENGTH_SHORT).show();
                sendCmdBroadcast(CMD_TRY_LINK,nowSenserAddress,nowSenserName);
            }
            else if(IsLinkSenser=="T")
            {
                nowSenserAddress=SenserAddress.get("childMac");
                nowSenserName=SenserName.get("child");
                if(!linkedSenserAddress.equals(SenserAddress.get("childMac")))
                {
                    new AlertDialog.Builder(Main2Activity.this)
                            .setTitle("警告")
                            .setMessage("已经与 “"+linkedSenserName+"”  建立连接，是否继续连接新传感器")
                            .setPositiveButton("确定",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialoginterface, int i) {


                                            IsLinkSenser = "L";
                                            Toast.makeText(getApplicationContext(), "正在尝试与 “"+nowSenserName+"” 建立连接，请稍后····",Toast.LENGTH_SHORT).show();
                                            sendCmdBroadcast(CMD_CLOSE_SOCKET, "", "");
                                            sendCmdBroadcast(CMD_TRY_LINK,nowSenserAddress,nowSenserName);
                                        }
                                    })
                            .setNegativeButton("取消",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialoginterface, int i) {
                                        }
                                    })
                            .show();
                }
                else
                {
                    new AlertDialog.Builder(Main2Activity.this)
                            .setTitle("警告")
                            .setMessage("已经与 “" + linkedSenserName + "”  建立连接")
                            .setPositiveButton("确定",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialoginterface, int i) {
                                            Intent intent = new Intent(Main2Activity.this, LineColumnDependencyActivity.class);
                                            startActivity(intent);
                                        }
                                    })
                            .setNegativeButton ("取消配对",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialoginterface, int i) {
                                            sendCmdBroadcast(CMD_CLOSE_SOCKET, "", "");
                                            IsLinkSenser = "F";
                                        }
                                    })
                            .show();
                }
            }
            return false;
        }
    }










    //创建ExpListAdapter对象
    private MyExpListAdapter upDataListData(ExpandableListView thisExpList, List<Map<String, String>> thisGruops, List<List<Map<String, String>>> thisGruopsChilds, List<List<Map<String, String>>> thisGruopsChildsMac, int groupLayout, int[] groupTo)
    {
         MyExpListAdapter thisSelaPaired = new MyExpListAdapter (
                Main2Activity.this,
                thisGruops, groupLayout, new String[]{"group"}, groupTo,
                thisGruopsChilds, R.layout.childs, new String[]{"child"}, new int[]{R.id.childNameTo},
                thisGruopsChildsMac, new String[]{"childMac"}, new int[]{R.id.childMac}
        );
        thisExpList.setAdapter(thisSelaPaired);
        return  thisSelaPaired;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
