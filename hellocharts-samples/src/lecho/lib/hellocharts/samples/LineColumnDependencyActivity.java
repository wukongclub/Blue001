package lecho.lib.hellocharts.samples;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener;
import lecho.lib.hellocharts.listener.ViewportChangeListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.LineChartView;
import lecho.lib.hellocharts.view.PreviewLineChartView;

public class LineColumnDependencyActivity extends AppCompatActivity  {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_column_dependency);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
        }
    }



    /**
     * A placeholder fragment containing a simple view.
     */
    public class PlaceholderFragment extends Fragment {
        public final String[] months = new String[]{"Mon", "Tue", "Wen", "Thu", "Fri", "Sat", "Sun",};

        public final String[] days = new String[]{"1", "2", "3", "4", "15", "6", "7","8", "9", "10", "11", "12", "13", "14","15", "16", "17", "18", "19","20"};

        private LineChartView chartTop;
        private ColumnChartView chartBottom;

        private LineChartData lineData;
        private ColumnChartData columnData;


        private LineChartView chart;
        private PreviewLineChartView previewChart;
        private LineChartData data;
        /**
         * Deep copy of data.
         */
        private LineChartData previewData;


        private Button buttonRecord;

        boolean buttonRecordState;

        //private int i = 0;
        private int TIME = 10000;

        TextView myTempleTextView;
        private boolean isFilled = false;
        private boolean hasLabels = false;
        private boolean hasLabelForSelected = false;
        private boolean hasAxesNames = true;

       // float[] templeValue = new float[20];



        static final int CMD_GET_DATA = 0x04;
        static final int CMD_START_RECORD= 0x05;
        static final int CMD_REQUES_RECORD= 0x06;

        static final int CMD_RETURN_DATA = 0x01;

        int nowColor=ChartUtils.COLOR_BLUE;// .pickColor();

        float templeValue;



        Handler handler = new Handler();
        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            setHasOptionsMenu(true);
            View rootView = inflater.inflate(R.layout.fragment_line_column_dependency, container, false);

            // *** TOP LINE CHART ***
            chartTop = (LineChartView) rootView.findViewById(R.id.chart_top);
            generateInitialLineData();


            myTempleTextView=(TextView)rootView.findViewById(R.id.TempletextView);
            myTempleTextView.setTextColor(nowColor);

            buttonRecord=(Button)rootView.findViewById(R.id.buttonRecord);
            buttonRecordState=false;

            buttonRecord.setOnClickListener(new Button.OnClickListener(){
                @Override
                public void onClick(View v) {

                    if (buttonRecordState == false)
                    {

                    //选项数组
                    String[] choices={"5秒","10秒","30秒","60秒","5分钟","10分钟","30分钟","1小时","2小时"};
                    //包含多个选项的对话框
                    AlertDialog dialog = new AlertDialog.Builder(LineColumnDependencyActivity.this)
                            //.setIcon(android.R.drawable.btn_star)
                            .setTitle("参数选择")
                            .setItems(choices, onselect).create();
                    dialog.show();
                    }
                    else
                    {
                        sendCmdBroadcast(CMD_REQUES_RECORD,0);
                        buttonRecordState = false;
                        buttonRecord.setText("记录");

                    }
                }
            });


            initBluetoothReciver();

            // *** BOTTOM COLUMN CHART ***

            chartBottom = (ColumnChartView) rootView.findViewById(R.id.chart_bottom);

            chartBottom.setVisibility(View.GONE);

            //generateColumnData();


            /////////////////////////////////////////////////////////
            chart = (LineChartView) rootView.findViewById(R.id.chart_temple);
            previewChart = (PreviewLineChartView) rootView.findViewById(R.id.chart_preview_temple);

//             Generate data for previewed chart and copy of that data for preview chart.
           generateDefaultData();

            chart.setLineChartData(data);
            // Disable zoom/scroll for previewed chart, visible chart ranges depends on preview chart viewport so
            // zoom/scroll is unnecessary.
            chart.setZoomEnabled(false);
            chart.setScrollEnabled(false);

            previewChart.setLineChartData(previewData);
            previewChart.setViewportChangeListener(new ViewportListener());

            previewX(false);


            chart.setVisibility(View.GONE);
            previewChart.setVisibility(View.GONE);

            ////////////////////////////////////////////////





            handler.postDelayed(runnable, TIME); //每隔1s执行
            return rootView;
        }

        DialogInterface.OnClickListener onselect = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

                System.out.println("sssssssssssss " + which);

                buttonRecord.setText("结束记录");

                buttonRecordState=true;

               sendCmdBroadcast(CMD_START_RECORD, which);
                switch (which) {
                    case 0:
                        Toast.makeText(LineColumnDependencyActivity.this, "5s",Toast.LENGTH_SHORT).show();
                        //sendCmdBroadcast(CMD_START_RECORD,0);
                        break;
                    case 1:
                        Toast.makeText(LineColumnDependencyActivity.this, "10s",Toast.LENGTH_SHORT).show();

                        break;
                    case 2:
                        Toast.makeText(LineColumnDependencyActivity.this, "30s",Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        Toast.makeText(LineColumnDependencyActivity.this, "60s",Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        Toast.makeText(LineColumnDependencyActivity.this, "5min",Toast.LENGTH_SHORT).show();
                        break;
                    case 5:
                        Toast.makeText(LineColumnDependencyActivity.this, "10min",Toast.LENGTH_SHORT).show();
                        break;
                    case 6:
                        Toast.makeText(LineColumnDependencyActivity.this, "30min",Toast.LENGTH_SHORT).show();
                        break;
                    case 7:
                        Toast.makeText(LineColumnDependencyActivity.this, "1hours",Toast.LENGTH_SHORT).show();
                        break;
                    case 8:
                        Toast.makeText(LineColumnDependencyActivity.this, "2hours",Toast.LENGTH_SHORT).show();
                        break;
                }
            }

        };


        @Override
        public void onDestroy() {
            super.onDestroy();

            handler.removeCallbacks(runnable);
            Toast.makeText(LineColumnDependencyActivity.this, "ddddddddddddddd",Toast.LENGTH_SHORT).show();
        }

        private void generateDefaultData() {
            int numValues = 500;

            List<PointValue> values = new ArrayList<PointValue>();
            for (int i = 0; i < numValues; ++i) {
                values.add(new PointValue(i, (float) Math.random() * 100f));
            }

            Line line = new Line(values);
            line.setColor(ChartUtils.COLOR_GREEN);
            line.setHasPoints(false);// too many values so don't draw points.

            List<Line> lines = new ArrayList<Line>();
            lines.add(line);

            data = new LineChartData(lines);
            data.setAxisXBottom(new Axis());
            data.setAxisYLeft(new Axis().setHasLines(true));

            // prepare preview data, is better to use separate deep copy for preview chart.
            // Set color to grey to make preview area more visible.
            previewData = new LineChartData(data);
            previewData.getLines().get(0).setColor(ChartUtils.DEFAULT_DARKEN_COLOR);

        }


        private void previewY() {
            Viewport tempViewport = new Viewport(chart.getMaximumViewport());
            float dy = tempViewport.height() / 4;
            tempViewport.inset(0, dy);
            previewChart.setCurrentViewportWithAnimation(tempViewport);
            previewChart.setZoomType(ZoomType.VERTICAL);
        }

        private void previewX(boolean animate) {
            Viewport tempViewport = new Viewport(chart.getMaximumViewport());
            float dx = tempViewport.width() / 4;
            tempViewport.inset(dx, 0);
            if (animate) {
                previewChart.setCurrentViewportWithAnimation(tempViewport);
            } else {
                previewChart.setCurrentViewport(tempViewport);
            }
            previewChart.setZoomType(ZoomType.HORIZONTAL);
        }

        private void previewXY() {
            // Better to not modify viewport of any chart directly so create a copy.
            Viewport tempViewport = new Viewport(chart.getMaximumViewport());
            // Make temp viewport smaller.
            float dx = tempViewport.width() / 4;
            float dy = tempViewport.height() / 4;
            tempViewport.inset(dx, dy);
            previewChart.setCurrentViewportWithAnimation(tempViewport);
        }

        private class ViewportListener implements ViewportChangeListener {

            @Override
            public void onViewportChanged(Viewport newViewport) {
                // don't use animation, it is unnecessary when using preview chart.
                chart.setCurrentViewport(newViewport);
            }

        }








        //蓝牙广播接收器
        private class BluetoothReciver extends BroadcastReceiver
        {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action= intent.getAction();


                if(action.equals("android.intent.action.ServiceToLineChart")){
                    System.out.println("returnnnn22");
                    Bundle bundle = intent.getExtras();
                    int cmd = bundle.getInt("cmd");

                    if (cmd==CMD_RETURN_DATA)
                    {


                        templeValue=bundle.getInt("value");
                        templeValue=templeValue/(float)10;

                        myTempleTextView.setText("当前温度 " + templeValue + "℃");

                        generateLineData(nowColor, 0,(float)templeValue);


//                        System.out.println("get " + templeText);
//

                        //myTempleTextView.setText("ss");
                    }


                }

            }
        }

        //初始化蓝牙接收器
        private void  initBluetoothReciver()
        {
            registerReceiver(new BluetoothReciver(), new IntentFilter("android.intent.action.ServiceToLineChart"));
        }




        Runnable runnable = new Runnable() {

            @Override
            public void run()
            {
                    try {
                        handler.postDelayed(this, TIME);
                        //tvShow.setText(Integer.toString(i++));

                        sendCmdBroadcast(CMD_GET_DATA,0);//请求数据
//
//                    double textTemple= Math.random()*190-60;
//
//                   // System.out.println("TTT "+textTemple);
//                    textTemple=Math.floor(textTemple*10d)/ 10;
//
//                    generateLineData(nowColor, i,(float)textTemple);
//
//                    myTempleTextView.setText("当前温度 " + textTemple + "℃");
//
//
//                    i++;
//                    if(i==20)
//                        i=0;

                        //System.out.println("do...");
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        System.out.println("exception...");
                    }

            }
        };

        //发送广播
        public void sendCmdBroadcast(int cmd,int value){
            Intent intent = new Intent();//创建Intent对象
            intent.setAction("android.intent.action.cmd");
            intent.putExtra("cmd", cmd);
            intent.putExtra("value", value);



            sendBroadcast(intent);//发送广播
        }




        //列
        private void generateColumnData() {

            int numSubcolumns = 1;
            int numColumns = months.length;

            List<AxisValue> axisValues = new ArrayList<AxisValue>();
            List<Column> columns = new ArrayList<Column>();
            List<SubcolumnValue> values;
            for (int i = 0; i < numColumns; ++i) {



                values = new ArrayList<SubcolumnValue>();
                for (int j = 0; j < numSubcolumns; ++j) {
                    //values.add(new SubcolumnValue((float) 2 * 50f + 5, Color.rgb(255,0,0)));//设置数据高度,随机颜色

                    values.add(new SubcolumnValue((float) Math.random() * 50f + 5, ChartUtils.pickColor()));
                }

                axisValues.add(new AxisValue(i).setLabel(months[i]));

                columns.add(new Column(values).setHasLabelsOnlyForSelected(true));
            }

            columnData = new ColumnChartData(columns);

            columnData.setAxisXBottom(new Axis(axisValues).setHasLines(true));
            columnData.setAxisYLeft(new Axis().setHasLines(true).setMaxLabelChars(2));

            chartBottom.setColumnChartData(columnData);

            // Set value touch listener that will trigger changes for chartTop.
            chartBottom.setOnValueTouchListener(new ValueTouchListener());

            // Set selection mode to keep selected month column highlighted.
            chartBottom.setValueSelectionEnabled(true);

            chartBottom.setZoomType(ZoomType.HORIZONTAL);

        }

        /**
         * Generates initial data for line chart. At the begining all Y values are equals 0. That will change when user
         * will select value on column chart.
         *
         * 为线路图生成初始数据。开始时所有Y的值等于0。当用户改变柱状图的值时Y的值改变
         *
         *
         */
        private void generateInitialLineData() {
            int numValues = 19;

            List<AxisValue> axisValues = new ArrayList<AxisValue>();
            List<PointValue> values = new ArrayList<PointValue>();
            for (int i = 0; i < numValues; ++i) {
                values.add(new PointValue(i, 0));
                axisValues.add(new AxisValue(i).setLabel(days[i]));
            }

            Line line = new Line(values);
            line.setColor(ChartUtils.COLOR_BLUE).setCubic(true);
            line.setHasPoints(false);

            line.getShape();

            line.setCubic(false);

            List<Line> lines = new ArrayList<Line>();
            lines.add(line);



            Axis axisX = new Axis(axisValues);
            Axis axisY = new Axis().setHasLines(true);
            if (hasAxesNames) {
                axisX.setName("时间");
                //axisY.setName("温度");

   //             axisX.setTextColor(Color.rgb(255,0, 0 ));
 //              axisY.setTextColor(Color.rgb(255, 0,0));
            }

            lineData = new LineChartData(lines);
            lineData.setAxisXBottom(axisX.setHasLines(true));
            lineData.setAxisYLeft(axisY.setMaxLabelChars(3));



            chartTop.setLineChartData(lineData);

            // For build-up animation you have to disable viewport recalculation.
            //建立动画你必须禁用视图重新计算。
            chartTop.setViewportCalculationEnabled(false);

            // And set initial max viewport and current viewport- remember to set viewports after data.
            //设置初始值和当前视口视口记得要在数据集视图。
            Viewport v = new Viewport(0, 130, 20, -60);
            chartTop.setMaximumViewport(v);
            chartTop.setCurrentViewport(v);

            chartTop.setZoomType(ZoomType.HORIZONTAL);

            toggleFilled();
            toggleLabels();
            toggleAxesNames();
        }

        private void toggleAxesNames() {
            hasAxesNames = !hasAxesNames;


        }

        private void toggleFilled() {
            isFilled = !isFilled;

            //generateData();
        }

        private void toggleLabels() {
            hasLabels = !hasLabels;

            if (hasLabels) {
                hasLabelForSelected = false;
                chartTop.setValueSelectionEnabled(hasLabelForSelected);
            }

            // generateData();
        }






        private void generateLineData(int color, int range, float textTemple) {
            // Cancel last animation if not finished.
            //chartTop.cancelDataAnimation();

            // Modify data targets
            //修改数据目标
            Line line = lineData.getLines().get(0);// For this example there is always only one line.
            line.setColor(color);
            line.setFilled(isFilled);
            line.setHasLabels(hasLabels);

            List<PointValue> lineList=line.getValues();

//            for (int j=lineList.size()-1;j>-1;j--)
//            {
//                PointValue value=lineList.get(j);
//                if (j<1)
//                {
//                    value.setTarget(value.getX(), textTemple);
//                }
//                else
//                {
//                    PointValue nextValue=lineList.get(j-1);
//                    value.setTarget(value.getX(), nextValue.getY());
//                }
//            }

            for (int j=0;j<lineList.size();j++)
            {
                PointValue value=lineList.get(j);
                if (j==lineList.size()-1)
                {
                    value.setTarget(value.getX(), textTemple);
                }
                else
                {
                    PointValue nextValue=lineList.get(j+1);
                    value.setTarget(value.getX(), nextValue.getY());
                }
            }

            // Start new data animation with 300ms duration;
            chartTop.startDataAnimation(0);
        }

        private class ValueTouchListener implements ColumnChartOnValueSelectListener {

            @Override
            public void onValueSelected(int columnIndex, int subcolumnIndex, SubcolumnValue value) {

                System.out.println("sss " + columnIndex + " " + subcolumnIndex + " "+ value);

               // generateLineData(value.getColor(), columnIndex);
            }

            @Override
            public void onValueDeselected() {

               // generateLineData(ChartUtils.COLOR_GREEN, 0);

            }
        }


        // MENU
        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.temlpe_menu, menu);
        }
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.save_temple_data) {

                //chartBottom.setVisibility(View.GONE);
                chartTop.setVisibility(View.VISIBLE);
                myTempleTextView.setVisibility(View.VISIBLE);


                chart.setVisibility(View.GONE);
                previewChart.setVisibility(View.GONE);

                SimpleDateFormat formatter =new    SimpleDateFormat    ("yyyy年MM月dd日    HH:mm:ss     ");
                Date curDate    =   new    Date(System.currentTimeMillis());//获取当前时间
                String    str    =    formatter.format(curDate);
                str+="\r\n";


                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){


                    File sdcardDir =Environment.getExternalStorageDirectory();
                    //得到一个路径，内容是sdcard的文件夹路径和名字
                    String path=sdcardDir.getPath()+"/aaaaaaaaaaaa";
                    File path1 = new File(path);
                    if (!path1.exists()) {
                        path1.mkdirs();
                    }

                    File sdFile = new File(path, "ttt.txt");


                    try {
//                        FileOutputStream fos = new FileOutputStream(sdFile);
//                        ObjectOutputStream oos = new ObjectOutputStream(fos);
//                        oos.writeObject(str);// 写入
//                        fos.close(); // 关闭输出流
                        RandomAccessFile raf = new RandomAccessFile(sdFile, "rw");
                        // 将文件记录指针移动最后
                        raf.seek(sdFile.length());
                        // 输出文件内容
                        raf.write(str.getBytes());
                        raf.close();

                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    System.out.println("chengg");
                    //Toast.makeText(WebviewTencentActivity.this, "成功保存到sd卡", Toast.LENGTH_LONG).show();

                }



                return true;
            }
            else if (id == R.id.show_temple_data) {





                final String[] arrayFruit = new String[] { "1.txt", "2.txt", "3.txt", "4.txt" };

                Dialog alertDialog = new AlertDialog.Builder(LineColumnDependencyActivity.this).
                        setTitle("请选择文件")
                        //.setIcon(R.drawable.ic_launcher)
                        .setItems(arrayFruit, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Toast.makeText(Dialog_AlertDialogDemoActivity.this, arrayFruit[which], Toast.LENGTH_SHORT).show();


                                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {


                                    File sdcardDir = Environment.getExternalStorageDirectory();
                                    //得到一个路径，内容是sdcard的文件夹路径和名字
                                    String path = sdcardDir.getPath() + "/aaaaaaaaaaaa";

                                    File[] files = new File(path).listFiles();
                                    for (File file : files) {
                                        System.out.println("file: " + file);
                                    }

                                    System.out.println("Over ");

                                }

//                                generateColumnData();
//                                chartBottom.setVisibility(View.VISIBLE);


                                chartTop.setVisibility(View.GONE);
                                myTempleTextView.setVisibility(View.GONE);

                                chart.setVisibility(View.VISIBLE);
                                previewChart.setVisibility(View.VISIBLE);

                            }
                        }).
                                setNegativeButton("取消", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // TODO Auto-generated method stub
                                    }
                                }).
                                create();
                alertDialog.show();

                return true;
            }

            return super.onOptionsItemSelected(item);
        }
    }
}
