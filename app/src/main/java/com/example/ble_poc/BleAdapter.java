package com.example.ble_poc;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BleAdapter extends ArrayAdapter<Beacon> {

	List<Beacon> dataList;
	Context contextMain;
	public BleAdapter(Context context, int resource, List<Beacon> objects) {
		super(context, resource, objects);
		dataList=objects;
		contextMain=context;
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return dataList.size();
	}

	@Override
	public Beacon getItem(int position) {
		// TODO Auto-generated method stub
		return dataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		TextView deviceName = null;
		if(convertView==null)
		{
			convertView=LayoutInflater.from(contextMain).inflate(R.layout.listitem,null);
			 deviceName=(TextView)convertView.findViewById(R.id.textView1);
		}
		else
		{
			 deviceName=(TextView)convertView.findViewById(R.id.textView1);

		}
		if(getItem(position).deviceName!=null)
		deviceName.setText(getItem(position).deviceName);
		return convertView;
	}

}
