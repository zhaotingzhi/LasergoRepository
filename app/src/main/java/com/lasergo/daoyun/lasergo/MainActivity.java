package com.lasergo.daoyun.lasergo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.lasergo.daoyun.conts.Conts;
import com.lasergo.view.FolderFilePicker;
import com.zjl.autolayout.AutoUtils;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by zhaotingzhi on 2017/3/27.
 */

public class MainActivity extends Activity implements View.OnClickListener {
    private EditText start_angle, stop_angle;
    private Spinner speed_array, circle_nums;
    private Button bt_start_scan, bt_stop_scan, bt_check_file;
    private Session session;
    private Channel channelshell = null;
    public static ChannelSftp channelSftp = null;
    String[] str1 = { "1", "2", "3" };
    String[] str2 = { "2", "1", "3" };
    String downloadFile = null;
    private String resultPath;
    private long fileSize;
    private ListView file_list;
    private List<FileItem> fileItemArrayList = new ArrayList<FileItem>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AutoUtils.auto(this);
        Resources resources =getResources();
        ArrayAdapter speed_adap = new ArrayAdapter<String>(this, R.layout.spinner_item, resources.getStringArray(R.array.speed_array));
        ArrayAdapter circle_adap = new ArrayAdapter<String>(this, R.layout.spinner_item, resources.getStringArray(R.array.circle_number));
        speed_array = (Spinner) this.findViewById(R.id.speed_array);
        circle_nums = (Spinner) this.findViewById(R.id.circle_number);
        start_angle = (EditText) this.findViewById(R.id.start_angle);
        stop_angle = (EditText) this.findViewById(R.id.stop_angle);

        speed_array.setAdapter(speed_adap);
        circle_nums.setAdapter(circle_adap);
        if (Conts.session != null && Conts.session.isConnected()) {
            session = Conts.session;
        } else {
            Toast.makeText(MainActivity.this, R.string.connect_error, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
        bt_start_scan = (Button) this.findViewById(R.id.bt_start_scan);
        bt_stop_scan = (Button) this.findViewById(R.id.bt_stop_scan);
        bt_check_file = (Button) this.findViewById(R.id.bt_check_file);
        bt_start_scan.setOnClickListener(this);
        bt_stop_scan.setOnClickListener(this);
        bt_check_file.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_start_scan:
                startScan_method();
                break;
            case R.id.bt_stop_scan:
                stopScan_method();
                break;
            case R.id.bt_check_file:
                check_file();
                break;
            default:
                break;
        }
    }
    MyAdapter  mAdapter;
    private void check_file() {
        if (session == null || !session.isConnected()) {
            Toast.makeText(MainActivity.this, "请先登录", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
            finish();
            return;
        }

        if(!fileItemArrayList.isEmpty()){
            fileItemArrayList.clear();
        }
        try {

            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            channelSftp.setFilenameEncoding("UTF-8");
            Vector vector = channelSftp.ls(getString(R.string.file_directory));
            for (Object obj : vector) {
                if (obj instanceof ChannelSftp.LsEntry) {
                    String fileNa = ((ChannelSftp.LsEntry) obj).getFilename();
                    if (fileNa.charAt(0) != '.' && fileNa.substring(fileNa.length() - 4).equals(".pcd")) {
                        FileItem fileItem  = new FileItem(fileNa);
                        fileItemArrayList.add(fileItem);
                    }

                }
            }
        } catch (SftpException e) {
            e.printStackTrace();
        } catch (JSchException e) {
            e.printStackTrace();
        }
        if (fileItemArrayList.isEmpty()) {
            Toast.makeText(MainActivity.this, getString(R.string.file_no_found), Toast.LENGTH_SHORT).show();
            return;
        }
        LinearLayout layout = (LinearLayout) LinearLayout.inflate(getApplicationContext(), R.layout.file_view, null);
        file_list = (ListView) layout.findViewById(R.id.file_list);
        mAdapter = new MyAdapter(MainActivity.this,fileItemArrayList);
        file_list.setAdapter(mAdapter);
        file_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                downloadFile = fileItemArrayList.get(position).getFileName().toString();
                mAdapter.changeSelected(position);//刷新

            }
        });
        file_list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                downloadFile = fileItemArrayList.get(position).getFileName().toString();
                mAdapter.changeSelected(position);//刷新


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).setTitle(getString(R.string.scan_file)).setView(layout)
                // .setAdapter(new ArrayAdapter<String>(MainActivity.this,
                // R.layout.item, R.id.tv, items),
                // new AlertDialog.OnClickListener() {
                // @Override
                // public void onClick(DialogInterface dialog, int which) {
                // downloadFile = items[which];
                // }
                // })

                .setNegativeButton(getString(R.string.canceling), new AlertDialog.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                    }

                }).setNeutralButton(getString(R.string.previewing), new AlertDialog.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (downloadFile == null) {
                            Toast.makeText(getApplicationContext(), getString(R.string.please_select_one_file), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String[] ss = downloadFile.split("\\.");
                        String str = ss[0].toString() + "Sp." + ss[1].toString();
                        Intent intent = new Intent(MainActivity.this, PreviewActivity.class);
                        intent.putExtra("downloadFileName", str);
                        startActivity(intent);
                    }
                }).setPositiveButton(getString(R.string.download), new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (downloadFile == null) {
                            Toast.makeText(getApplicationContext(), getString(R.string.please_select_one_file), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        FolderFilePicker picker = new FolderFilePicker(MainActivity.this, new FolderFilePicker.PickPathEvent() {
                            @Override
                            public void onPickEvent(String restPath) {
                                resultPath = restPath;
                                new AsyncTask<String, Void, Boolean>() {

                                    @Override
                                    protected Boolean doInBackground(String... params) {
                                        OutputStream out = null;
                                        InputStream is = null;
                                        try {
                                            String filename = "/home/clickmox/Documents/" + downloadFile;
                                            SftpATTRS attr;
                                            attr = channelSftp.stat(filename);
                                            fileSize = attr.getSize();
                                            String dst = resultPath + "/" + downloadFile;
                                            out = new FileOutputStream(dst);
                                            // 添加回调函数监控进度
                                            is = channelSftp.get(filename, new MyProgressMonitor());
                                            byte[] buff = new byte[1024 * 2];
                                            int read;
                                            if (is != null) {
                                                do {
                                                    read = is.read(buff, 0, buff.length);
                                                    if (read > 0) {
                                                        out.write(buff, 0, read);
                                                    }
                                                    out.flush();
                                                } while (read >= 0);
                                            }
                                        } catch (SftpException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        } finally {
                                            try {
                                                is.close();
                                                out.close();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        return null;
                                    }
                                }.execute();
                            }
                        });
                        picker.show();
                    }
                }).show();

    }


    private class FileItem {
        private String FileName;
        private FileItem(String fileName){
            this.FileName = fileName;
        }


        public void setFileName(String fileName) {
            FileName = fileName;
        }

        public String getFileName() {
            return FileName;
        }
    }
    class MyAdapter extends BaseAdapter
    {
        private Context mContext = null;
        private List<FileItem> mMarkerData = null;
        int mSelect = 0;   //选中项

        public void changeSelected(int positon){ //刷新方法
            if(positon != mSelect){
                mSelect = positon;
                notifyDataSetChanged();
            }
        }


        public MyAdapter(Context context, List<FileItem> markerItems)
        {
            mContext = context;
            mMarkerData = markerItems;
        }

        public void setMarkerData(List<FileItem> markerItems)
        {
            mMarkerData = markerItems;
        }

        @Override
        public int getCount()
        {
            int count = 0;
            if (null != mMarkerData)
            {
                count = mMarkerData.size();
            }
            return count;
        }

        @Override
        public FileItem getItem(int position)
        {
            FileItem item = null;

            if (null != mMarkerData)
            {
                item = mMarkerData.get(position);
            }

            return item;
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder viewHolder = null;
            if (null == convertView)
            {
                viewHolder = new ViewHolder();
                LayoutInflater mInflater = LayoutInflater.from(mContext);
                convertView = mInflater.inflate(R.layout.item, null);

                viewHolder.filename = (TextView) convertView.findViewById(R.id.tv);
                convertView.setTag(viewHolder);
            }
            else
            {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            // set item values to the viewHolder:

            FileItem markerItem = getItem(position);
            if (null != markerItem)
            {
                viewHolder.filename.setText(markerItem.getFileName());
            }

            if(mSelect==position){
                convertView.setBackgroundResource(R.color.seashell);  //选中项背景
            }else{
                convertView.setBackgroundResource(R.color.white);  //其他项背景
            }


            return convertView;
        }

      class ViewHolder
        {
            TextView filename;
        }

    }








    private long transfered;
    private ProgressDialog dialog;
    private Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            if (msg.what == 11) {
                dialog = ProgressDialog.show(MainActivity.this, "", getString(R.string.loading), true, true);
            }
            if (msg.what == 22) {
                dialog.dismiss();
            }
        };
    };

    class MyProgressMonitor implements SftpProgressMonitor {

        @Override
        public boolean count(long count) {
            if (transfered < 1024) {
                System.out.println("Currently transferred total size: " + transfered + " bytes");
            }
            if ((transfered > 1024) && (transfered < 1048576)) {
                System.out.println("Currently transferred total size: " + (transfered / 1024) + "K bytes");
            } else {
                System.out.println("Currently transferred total size: " + (transfered / 1024 / 1024) + "M bytes");
            }
            return true;
        }

        @Override
        public void end() {
            handler.sendEmptyMessage(22);
        }

        @Override
        public void init(int op, String src, String dest, long max) {
            handler.sendEmptyMessage(11);
        }

    }

    private void stopScan_method() {
        if (session == null || !session.isConnected()) {
            Toast.makeText(MainActivity.this, getString(R.string.please_login), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
            finish();
            return;
        }

        try {
            if (channelshell != null && channelshell.isConnected()) {
                channelshell.disconnect();
            }
            channelshell = session.openChannel("shell");
            channelshell.setInputStream(new ByteArrayInputStream(
                    getString(R.string.stop_command).getBytes()));

            channelshell.connect();

        } catch (Exception e) {
            if (channelshell != null) {
                channelshell.disconnect();
            }
        }

    }

    private void startScan_method() {

        if (session == null || !session.isConnected()) {
            Toast.makeText(MainActivity.this, getString(R.string.please_login), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
            finish();
            return;
        }
        try {
            if (channelshell != null && channelshell.isConnected()) {
                channelshell.disconnect();
            }
            channelshell = session.openChannel("shell");
            String startAngle = start_angle.getText().toString();
            String stopAngle = stop_angle.getText().toString();

            String start_str = "rostopic pub dxl_setting std_msgs/Int16MultiArray \'{data:[2,"
                    + str1[(int) speed_array.getSelectedItemId()] + "," + str2[(int) circle_nums.getSelectedItemId()]
                    + "," + startAngle + "," + stopAngle + "]}\' \n";
            channelshell.setInputStream(new ByteArrayInputStream(start_str.getBytes()));
            channelshell.connect();

        } catch (Exception e) {
            if (channelshell != null) {
                channelshell.disconnect();
            }
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        if (channelSftp != null && channelSftp.isConnected()) {
            channelSftp.disconnect();
            channelSftp = null;
        }
        super.onDestroy();
    }


}
