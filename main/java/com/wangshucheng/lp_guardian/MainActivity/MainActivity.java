package com.wangshucheng.lp_guardian.MainActivity;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.database.sqlite.SQLiteDatabase;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wangshucheng.lp_guardian.BmobSever.Upload;
import com.wangshucheng.lp_guardian.BuildConfig;
import com.wangshucheng.lp_guardian.R;
import com.wangshucheng.lp_guardian.data.AppInfo;
import com.wangshucheng.lp_guardian.data.DbHelper;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class MainActivity extends AppCompatActivity {

    private ProgressDialog mProgressDialog;
    private AppAdapter mAppAdapter;
    private SQLiteDatabase mSQLiteDatabase;
    private ArrayList<AppInfo> mAppInfos = new ArrayList<>();
    int appCount = 0;
    //private LocationGuardian locationGuardian;
    //private Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bmob.initialize(this, "26fcb39b025d73b63afa71fb31848a59");

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_app);
        assert recyclerView != null;
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
        mAppAdapter = new AppAdapter(mAppInfos);
        recyclerView.setAdapter(mAppAdapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMax(100);
        mProgressDialog.setMessage("Scanning All Apps");
        mProgressDialog.show();

        mSQLiteDatabase = new DbHelper(this).getWritableDatabase();
        GetAppInfoTask getAppInfoTask = new GetAppInfoTask();
        getAppInfoTask.execute();
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);

    }

    @Override
    protected void onStart() {
        super.onStart();
        appCount++;
    }
    @Override
    protected void onStop() {
        super.onStop();
        appCount--;
        if (appCount ==0) {
            Toast.makeText(getApplication(), "runningBackground", Toast.LENGTH_LONG).show();
            if (getPackageName().equals("com.google.android.apps.maps")) {
                Toast.makeText(getApplication(), "googlemap", Toast.LENGTH_SHORT).show();
                //OnlyGoogleMap onlyGoogleMap = new OnlyGoogleMap();

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }






    private class GetAppInfoTask extends AsyncTask<Integer, Integer, ArrayList<AppInfo>> {

        @Override
        protected ArrayList<AppInfo> doInBackground(Integer[] params) {
            ArrayList<AppInfo> appList = new ArrayList<>();
            List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
            for (int i = 0; i < packages.size(); i++) {
                PackageInfo packageInfo = packages.get(i);
                AppInfo tmpInfo = new AppInfo();
                tmpInfo.appName = packageInfo.applicationInfo.loadLabel(getPackageManager()).toString();
                tmpInfo.packageName = packageInfo.packageName;
                tmpInfo.versionName = packageInfo.versionName;
                tmpInfo.versionCode = packageInfo.versionCode;
                tmpInfo.appIcon = packageInfo.applicationInfo.loadIcon(getPackageManager());
                if (!packageInfo.packageName.equals(BuildConfig.APPLICATION_ID))
                    appList.add(tmpInfo);
                publishProgress(i / packages.size() * 100);
            }
            return appList;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            mProgressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<AppInfo> o) {
            mProgressDialog.dismiss();
            mAppInfos.addAll(o);
            mAppAdapter.notifyDataSetChanged();
        }
    }

    class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppViewHolder> {

        ArrayList<AppInfo> mAppInfos;


        AppAdapter(ArrayList<AppInfo> appInfos) {
            this.mAppInfos = appInfos;
        }

        @Override
        public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new AppViewHolder(getLayoutInflater().inflate(R.layout.app_item, parent, false));
        }

        @Override
        public void onBindViewHolder(final AppViewHolder holder, int position) {
            holder.ivIcon.setImageDrawable(mAppInfos.get(position).getAppIcon());
            holder.tvName.setText(mAppInfos.get(position).getAppName());
            holder.tvPackageName.setText(mAppInfos.get(position).getPackageName());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    new AlertDialog.Builder(MainActivity.this).setTitle("set your guardian level")
                            .setSingleChoiceItems(new String[]{"exact position", "only track", "only city", "no location"}, 0, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.put("package_name", mAppInfos.get(holder.getAdapterPosition()).getPackageName());
                                    contentValues.put("level", i + 1);
                                    mSQLiteDatabase.insertWithOnConflict(DbHelper.APP_TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
                                    dialogInterface.dismiss();

                                    //submit method 2
                                    TelephonyManager telephonyManager = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
                                    String tvImei = telephonyManager.getDeviceId();
                                    String tvPackageName = mAppInfos.get(holder.getAdapterPosition()).getPackageName();
                                    String tvName = mAppInfos.get(holder.getAdapterPosition()).getAppName();
                                    String tvLevel = Integer.toString(i);

                                    if (tvPackageName.equals("") || tvName.equals("")||tvLevel.equals("")||tvImei.equals("")) {
                                        return;
                                    }
                                    Upload uploadObj = new Upload();
                                    uploadObj.setTvImei(tvImei);
                                    uploadObj.setTvPackageName(tvPackageName);
                                    uploadObj.setTvName(tvName);
                                    uploadObj.setTvLevel(tvLevel);
                                    System.out.println("BmobUpload: Imei:" + tvImei + "/packagename:" + tvPackageName + "/name: " + tvName + "/guardianlevel: " + tvLevel);
                                    Log.i("BmobUpload: Imei:", tvImei + "/packagename:" + tvPackageName + "/name: " + tvName + "/guardianlevel: " + tvLevel);
                                    uploadObj.save(new SaveListener<String>() {
                                        @Override
                                        public void done(String s, BmobException e) {
                                            Toast.makeText(MainActivity.this, "done", Toast.LENGTH_LONG).show();
                                        }
                                    });

                                }
                            }).show();


                }

            });
        }

        @Override
        public int getItemCount() {
            return mAppInfos.size();
        }

        class AppViewHolder extends RecyclerView.ViewHolder {

            ImageView ivIcon;
            TextView tvName;
            TextView tvPackageName;

            AppViewHolder(View itemView) {
                super(itemView);
                ivIcon = (ImageView) itemView.findViewById(R.id.iv_icon);
                tvName = (TextView) itemView.findViewById(R.id.tv_name);
                tvPackageName = (TextView) itemView.findViewById(R.id.tv_package_name);
            }
        }



    }



}






