package com.hhuc.smartdoorterminal.modules.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.hhuc.smartdoorterminal.R;
import com.hhuc.smartdoorterminal.ui.face.FaceRegisterAndRecognize;

public class LoginDialog extends DialogFragment implements View.OnClickListener {
    private EditText mUsername;
    private EditText mPassword;
    private Button btn;
    private ImageView iv;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //设置背景透明
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_login, null);
        iv = view.findViewById(R.id.login_iv);
        mUsername = view.findViewById(R.id.login_et1);
        btn = view.findViewById(R.id.login_btn);
        mPassword = view.findViewById(R.id.login_et2);
        iv.setOnClickListener(this);
        btn.setOnClickListener(this);
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.login_btn) {
            if (getTargetFragment() == null) {
                return;
            }
            if (mUsername.getText().toString().isEmpty())
                Toast.makeText(getActivity(), "用户名不能为空!", Toast.LENGTH_SHORT).show();
            else if (mPassword.getText().toString().isEmpty())
                Toast.makeText(getActivity(), "密码不能为空!", Toast.LENGTH_SHORT).show();
            else {
                Intent intent = new Intent();
                intent.putExtra("USERNAME", mUsername.getText().toString());
                intent.putExtra("USERPASSWORD", mPassword.getText().toString());
                getTargetFragment().onActivityResult(FaceRegisterAndRecognize.REQUEST_CODE, Activity.RESULT_OK, intent);
            }
        } else if (v.getId() == R.id.login_iv) {
            dismiss();
        }
    }

}
