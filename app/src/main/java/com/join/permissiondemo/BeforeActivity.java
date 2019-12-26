package com.join.permissiondemo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class BeforeActivity extends Activity {


    private static final String TAG = "BeforeActivity";
//    是否打开了详情页面
    private boolean toAppDetail = false;

//    申请权限时的请求码
    final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 110;
    //所有要申请的权限
    public static final String[] mPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        ArrayList<String> needGetList = null;
        for (String permission : mPermissions) {
//            检查是否已经授权
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
                if (needGetList == null){
                    needGetList = new ArrayList<>();
                }
                //没有授权，添加到待申请列表
                needGetList.add(permission);
            }
        }

        // 进行申请动作
        actionPermission(needGetList, needGetList == null ? true : false);

    }




    private void actionPermission(ArrayList<String> needGetList, boolean result){
        //  有需要申请的权限
        if (needGetList != null && needGetList.size() > 0){
            System.out.println(needGetList.toString());

            String[] arrays = new String[needGetList.size()];
            for (int i = 0; i < needGetList.size(); i++) {
                arrays[i] = needGetList.get(i);
            }
            ActivityCompat.requestPermissions(this, arrays, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }else {
            if (result){
                // 授权成功
                Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "授权失败，您可之后到系统应用管理页面进行手动授权", Toast.LENGTH_SHORT).show();
            }
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                ArrayList<String> denied = new ArrayList<>();
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED){
                        denied.add(permissions[i]);
                    }
                }
                if (denied.size() > 0 ){
                // 有未申请到得权限 需要弹框提醒
                    showRationaleDialog(denied);
                }else {
//                    申请到所有权限  跳转页面
                    actionPermission(null, true);
                }

            }
        }
    }


    /**
     * 弹出提示语
     *
     */
    private void showRationaleDialog(final ArrayList<String> denied) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        TextView mMsg = new TextView(this);
        mMsg.setText("您还有权限未授权完毕，将导致应用无法正常使用，是否跳转到系统设置界面继续授权？");
        mMsg.setGravity(Gravity.CENTER_HORIZONTAL);
        mMsg.setTextSize(18);
        builder.setView(mMsg);
        builder.setCancelable(false);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                try{
//                    打开系统 本应用的详情页面
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts("package", BeforeActivity.this.getPackageName(), null));
                    BeforeActivity.this.startActivity(intent);
                }catch (Exception e){
                    Log.e(TAG, "打开当前本应用设置界面失败： " + e.getMessage());
                    try{
//                        打开系统已安装应用页面
                        BeforeActivity.this.startActivity(new Intent(Settings.ACTION_APPLICATION_SETTINGS));
                    }catch (Exception e2){
                        Log.e(TAG, "打开应用设置界面失败： " + e.getMessage());
                        Toast.makeText(BeforeActivity.this, "打开应用设置界面失败", Toast.LENGTH_SHORT).show();
//                        如果都打开失败  提示用户手动之后手动开启权限  并关闭页面
                        actionPermission(null, false);
                    }
                }
                toAppDetail = true;
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                actionPermission(null, false);
            }
        });
        Dialog tipDialog = builder.create();
        tipDialog.setCanceledOnTouchOutside(false);
        tipDialog.show();

    }


    @Override
    protected void onStart() {
        super.onStart();
        if (toAppDetail){
            toAppDetail = false;
            for (String permission : mPermissions) {
//            检查是否已经授权
                if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
                    actionPermission(null, false);
                    return;
                }
            }
            actionPermission(null, true);
        }
    }
}
