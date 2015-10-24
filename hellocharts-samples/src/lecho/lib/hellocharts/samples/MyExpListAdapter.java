package lecho.lib.hellocharts.samples;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;

import android.content.Context;
import android.os.*;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

/**
 * Created by guess on 2015/9/30.
 */
public class MyExpListAdapter extends SimpleExpandableListAdapter {
    private List<? extends Map<String, ?>> mGroupData;
    private String[] mGroupFrom;
    private int[] mGroupTo;

    private List<? extends List<? extends Map<String, ?>>> mChildData;
    private List<? extends List<? extends Map<String, ?>>> mChildDataMac;

    private int mChildLayout;

    private String[] mChildFrom;
    private int[] mChildTo;

    private String[] mChildFromMac;
    private int[] mChildToMac;




    //参数: 1.上下文
    // 2.一级集合   3.一级样式文件 4. 一级条目键值      5.一级显示控件名
    // 6. 二级集合 7. 二级样式 8.二级条目键值    9.二级显示控件名

    MyExpListAdapter(
            Context context,
            List<? extends Map<String, ?>> groupData  ,int groupLayout,  String[] groupFrom,     int[] groupTo,
            List<? extends List<? extends Map<String, ?>>> childData,    int childLayout,        String[] childFrom,  int[] childTo,
            List<? extends List<? extends Map<String, ?>>> childDataMac, String[] childFromMac,  int[] childToMac
          )

    {
        super(context, groupData, groupLayout, groupLayout, groupFrom, groupTo,
                childData, childLayout, childLayout, childFrom, childTo);
        mGroupData = groupData;
        mGroupFrom = groupFrom;
        mGroupTo = groupTo;

        mChildData = childData;
        mChildDataMac=childDataMac;

        mChildLayout = childLayout;

        mChildFrom = childFrom;
        mChildTo = childTo;

        mChildFromMac = childFromMac;
        mChildToMac = childToMac;
    }



    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        View v;
        if (convertView == null) {
            v = newChildView(isLastChild, parent);
        } else {
            v = convertView;
        }
        bindView(v, mChildData.get(groupPosition).get(childPosition), mChildFrom, mChildTo);
        bindView(v, mChildDataMac.get(groupPosition).get(childPosition), mChildFromMac, mChildToMac);

        return v;
    }

    public void setSenserState(View convertView,String state, int childPosition)
    {

        System.out.println("childPosition " + childPosition);

        List<? extends Map<String, ?>> la1= mChildData.get(0);

        Map<String, String> la2=( Map<String, String>)la1.get(childPosition);


        System.out.println("SenserName " + la2.get("child"));
        System.out.println("SenserName " + la2.get("childMac"));

        la2.clear();
        la2.put("child", "aa");

        //la1.remove(childPosition);



//        TextView v = (TextView)convertView.findViewById(R.id.childState);
//        ProgressBar pb = (ProgressBar)convertView.findViewById(R.id.StateProgressBar);
//
//        if (v != null) {
//            if (state=="T")
//            {
//                v.setText("已连接");
//                v.setTextColor(android.graphics.Color.RED);
//            }
//            else if (state=="F")
//            {
//                v.setText("未连接");
//                v.setTextColor(0xff757575);
////                pb.setVisibility(View.VISIBLE);
////                v.setVisibility(View.INVISIBLE);
//            }
//            else
//            {
//
//            }
//        }

    }



    @Override
    public List<Object> getChild(int groupPosition, int childPosition) {


        List<Object> DataMacList=new ArrayList<Object>();
        DataMacList.add(mChildData.get(groupPosition).get(childPosition));
        DataMacList.add(mChildDataMac.get(groupPosition).get(childPosition));

        return DataMacList;
    }



    private void bindView(View view,   Map<String, ?> data,String[] from,int[] to) {

        int len = to.length;

        for (int i = 0; i < len; i++) {
            TextView v = (TextView)view.findViewById(to[i]);

            if (v != null) {
                v.setText((String)data.get(from[i]));
            }
        }
    }



    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        View v;
        if (convertView == null) {
            v = newGroupView(isExpanded, parent);
        } else {
            v = convertView;
        }

        bindView(v, mGroupData.get(groupPosition), mGroupFrom, mGroupTo,
                groupPosition);
        return v;
    }



    public void bindView(View view, Map<String, ?> data, String[] from,
                          int[] to, int groupPosition) {
        int len = to.length;

        for (int i = 0; i < len; i++) {
            TextView v = (TextView) view.findViewById(to[i]);
            if (v != null) {
                if (i == 1) {
                    //System.out.println("aaa");
                    // 这里实现组内有多少子条目数
                    v.setText((String) data.get(from[i]) + " ("
                            + getChildrenCount(groupPosition) + ")");
                } else {
                    //System.out.println("xxx");
                   // v.setText((String) data.get(from[i]));

                    v.setText((String) data.get(from[i]) + " ("
                            + getChildrenCount(groupPosition) + ")");
                }
            }
        }
    }

}