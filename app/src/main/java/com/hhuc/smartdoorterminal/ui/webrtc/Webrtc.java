package com.hhuc.smartdoorterminal.ui.webrtc;

import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.hhuc.smartdoorterminal.R;
import com.hhuc.smartdoorterminal.modules.hardware.GPIO;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;

import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoLanguage;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoPlayStreamQuality;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;

public class Webrtc extends Fragment {
    private final static String TAG = "Webrtc";
    private View root;
    ImageButton ib_local_mic;
    ImageButton ib_remote_stream_audio;
    TextView local_preview;
    TextView remote_screen;
    TextView notice;
    View local_view;
    View play_view;
    EditText edit_hid;
    Button btnStartVideo;
    Button btnEndVideo;
    boolean publishMicEnable = true;
    boolean playStreamMute = true;
    public static ZegoExpressEngine engine = null;
    private String userID;
    private String userName;
    String roomID;
    String publishStreamID;
    String playStreamID = "";
    static final long appID = 11111111L; // ??????????????????????????????????????????webrtc appID
    static final String appSign = "????????????????????????????????????appSign"; // ??????????????????????????????????????????webrtc appSign
    private Activity mCtx;
    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    String[] PERMISSIONS_STORAGE = {
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO"};

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_communication, container, false);
        init();
        return root;
    }

    private void init() {
        try {
            /* ???????????? */
            if (!checkOrRequestPermission()) {
                ActivityCompat.requestPermissions(getActivity(), PERMISSIONS_STORAGE, ACTION_REQUEST_PERMISSIONS);
            }
            /*?????????*/
            local_preview = root.findViewById(R.id.textView1);
            remote_screen = root.findViewById(R.id.textView2);
            remote_screen.setText("?????????");
            notice = root.findViewById(R.id.textView3);
            ib_local_mic = root.findViewById(R.id.ib_local_mic);
            ib_remote_stream_audio = root.findViewById(R.id.ib_remote_mic);
            edit_hid = root.findViewById(R.id.editHid);
            local_view = root.findViewById(R.id.local_view);
            play_view = root.findViewById(R.id.remote_view);
            btnStartVideo = root.findViewById(R.id.videoStart);
            btnEndVideo = root.findViewById(R.id.videoEnd);
            btnStartVideo.setVisibility(View.VISIBLE);
            btnEndVideo.setVisibility(View.GONE);
            btnStartVideo.setOnClickListener(this::onBtnClick);
            btnEndVideo.setOnClickListener(this::onBtnClick);
            ib_local_mic.setOnClickListener(this::onBtnClick);
            ib_remote_stream_audio.setOnClickListener(this::onBtnClick);
            /* ??????ID */
            userID = "visit";
            userName = "visit";
            /* ???ID */
            publishStreamID = "Visit-Stream";
            InitEngine();
        } catch (Exception er) {
            Log.e(TAG, "init" + er.getMessage());
        }
    }

    private void onBtnClick(View v) {
        try {
            int btnId = v.getId();
            if (btnId == R.id.videoStart) {
                if (edit_hid.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), "????????????????????????!", Toast.LENGTH_SHORT).show();
                } else {
                    btnStartVideo.setVisibility(View.GONE);
                    btnEndVideo.setVisibility(View.VISIBLE);
                    ClickStart();
                }
            } else if (btnId == R.id.videoEnd) {
                btnEndVideo.setVisibility(View.GONE);
                btnStartVideo.setVisibility(View.VISIBLE);
                ClickEnd();
            } else if (btnId == R.id.ib_local_mic) {
                enableLocalMic();
            } else if (btnId == R.id.ib_remote_mic) {
                enableRemoteMic();
            }

        } catch (Exception er) {
            Log.e(TAG, "onClick: " + er.getMessage());
        }
    }

    public void InitEngine() {
        if (engine != null) {
            Toast.makeText(getActivity(), "sdk_already_init", Toast.LENGTH_SHORT).show();
            return;
        }
        /* ?????????SDK, ??????????????????????????????????????? */
        Application application = mCtx.getApplication();
        engine = ZegoExpressEngine.createEngine(appID, appSign, true, ZegoScenario.GENERAL, application, null);
        Log.d(TAG, "init: init_sdk_ok");
        engine.enableHardwareDecoder(true);
        engine.enableHardwareEncoder(true);
        engine.setDebugVerbose(true, ZegoLanguage.CHINESE);
        engine.useFrontCamera(false);
        engine.setEventHandler(new IZegoEventHandler() {
            /* ???????????? */
            @Override
            public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errorCode, JSONObject extendedData) {
                /* ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????SDK???????????????????????? */
                Log.d(TAG, "onRoomStateUpdate: roomID = " + roomID + ", state = " + state + ", errorCode = " + errorCode);
                if (errorCode != 0) {
                    Toast.makeText(getActivity(), String.format("login room fail, errorCode: %d", errorCode), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoUser> userList) {
                /* ???????????????????????????????????????????????????????????????????????????SDK???????????????????????? */
                Log.d(TAG, "onRoomUserUpdate: roomID = " + roomID + ", updateType = " + updateType);
                for (int i = 0; i < userList.size(); i++) {
                    Log.d(TAG, "onRoomUserUpdate: userID = " + userList.get(i).userID + ", userName = " + userList.get(i).userName);
                    if (updateType == ZegoUpdateType.ADD) {
                        remote_screen.setText("????????????");
                    } else if (updateType == ZegoUpdateType.DELETE) {
                        remote_screen.setText("???????????????");
                    }
                }
            }

            @Override
            public void onRoomOnlineUserCountUpdate(String roomID, int count) {
                /* ??????????????????????????????????????? */
                Log.d(TAG, "onRoomOnlineUserCountUpdate: roomID = " + roomID + ", count = " + count);
            }

            @Override
            public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList) {
                /* ????????????????????????????????????????????????????????????????????????????????????SDK???????????????????????? */
                Log.d(TAG, "onRoomStreamUpdate: roomID = " + roomID + ", updateType = " + updateType);
                for (int i = 0; i < streamList.size(); i++) {
                    Log.d(TAG, "onRoomStreamUpdate: streamID = " + streamList.get(i).streamID);
                    playStreamID = streamList.get(i).streamID;
                    if ((ZegoUpdateType.ADD == updateType) && (!streamList.get(i).streamID.equals("Visit-Stream"))) {
                        remote_screen.setText("?????????");
                        /* ???????????? */
                        engine.startPlayingStream(playStreamID, new ZegoCanvas(play_view));
                        openRemoteMic();
                        Log.d(TAG, "Start play stream OK, streamID = " + playStreamID);
                        Toast.makeText(getActivity(), "play_ok", Toast.LENGTH_SHORT).show();
                    } else if ((ZegoUpdateType.DELETE == updateType) && (!streamList.get(i).streamID.equals("Visit-Stream"))) {
                        remote_screen.setText("???????????????");
                        /* ???????????? */
                        engine.stopPlayingStream(playStreamID);
                        closeRemoteMic();
                        Log.d(TAG, "ClickEnd: Stop play stream OK, streamID = " + playStreamID);
                        Toast.makeText(getActivity(), "stop_play_ok", Toast.LENGTH_SHORT).show();

                    }
                }
            }

            public void onIMRecvCustomCommand(String roomID, ZegoUser fromUser, String command) {
                Log.d(TAG, "onIMRecvCustomCommand: roomID = " + roomID + "fromUser :" + fromUser + ", command= " + command);
                //Toast.makeText(getActivity(), command, Toast.LENGTH_SHORT).show();
                Toast.makeText(getActivity(), "???????????????", Toast.LENGTH_LONG).show();
                if (command.equals("openDoor")) {
                    Thread aOpenThread = new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            GPIO.gpio_crtl("26", "out", 0);
                            try {
                                sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            GPIO.gpio_crtl("26", "out", 1);
                        }
                    };
                    aOpenThread.setDaemon(true);
                    aOpenThread.start();
                }
            }

            @Override
            public void onDebugError(int errorCode, String funcName, String info) {
                /* ???????????????????????? */
                Log.d(TAG, "onDebugError: errorCode = " + errorCode + ", funcName = " + funcName + ", info = " + info);
            }

            @Override
            public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode, JSONObject extendedData) {
                /* ????????????????????????????????????????????????????????????????????????????????????????????????????????????SDK???????????????????????? */
                Log.d(TAG, "onPublisherStateUpdate: streamID = " + streamID + ", state = " + state + ", errCode = " + errorCode);
            }

            @Override
            public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errorCode, JSONObject extendedData) {
                /* ????????????????????????????????????????????????????????????????????????????????????????????????????????????SDK???????????????????????? */
                Log.d(TAG, "onPlayerStateUpdate: streamID = " + streamID + ", state = " + state + ", errCode = " + errorCode);
            }

            @Override
            public void onPlayerQualityUpdate(String streamID, ZegoPlayStreamQuality quality) {
                super.onPlayerQualityUpdate(streamID, quality);
            }
        });
        engine.muteMicrophone(!publishMicEnable);
        engine.muteAudioOutput(playStreamMute);
    }

    public void ClickStart() {
        if (engine == null) {
            Toast.makeText(getActivity(), "sdk_not_init", Toast.LENGTH_SHORT).show();
            return;
        }
        /* ?????????????????? */
        ZegoUser user = new ZegoUser(userID, userName);
        /* ?????????????????? */
        roomID = "Room-" + edit_hid.getText().toString();
        notice.setText(roomID);
        /* ??????????????????/?????????????????? */
        ZegoRoomConfig config = new ZegoRoomConfig();
        config.isUserStatusNotify = true;
        engine.loginRoom(roomID, user, config);
        Log.d(TAG, "Login room OK, userID = " + userID + " , userName = " + userName);
        Toast.makeText(getActivity(), "login_room_ok", Toast.LENGTH_SHORT).show();
        /* ???????????? */
        engine.startPublishingStream(publishStreamID);
        Log.d(TAG, "Publish stream OK, streamID = " + publishStreamID);
        /* ??????????????????????????????????????? */
        engine.startPreview(new ZegoCanvas(local_view));
        Log.d(TAG, "Start preview OK");
        Toast.makeText(getActivity(), "preview_ok", Toast.LENGTH_SHORT).show();
    }

    public void ClickEnd() {
        if (engine == null) {
            Toast.makeText(getActivity(), "sdk_not_init", Toast.LENGTH_SHORT).show();
            return;
        }
        /* ???????????? */
        engine.stopPublishingStream();
        /* ?????????????????? */
        engine.stopPreview();
        Log.d(TAG, "ClickEnd: Stop publish stream OK");
        Toast.makeText(getActivity(), "stop_publish_ok", Toast.LENGTH_SHORT).show();
        /* ???????????? */
        if (!playStreamID.isEmpty()) {
            engine.stopPlayingStream(playStreamID);
            closeRemoteMic();
            Log.d(TAG, "ClickEnd: Stop play stream OK, streamID = " + playStreamID);
            Toast.makeText(getActivity(), "stop_play_ok", Toast.LENGTH_SHORT).show();
        }
        /* ???????????? */
        engine.logoutRoom(roomID);
        Log.d(TAG, "ClickEnd: Logout room OK, userID = " + userID + " , userName = " + userName);
        Toast.makeText(getActivity(), "logout_room_ok", Toast.LENGTH_SHORT).show();
        remote_screen.setText("?????????");
        edit_hid.setText(null);
        remote_screen.setText("?????????");

    }

    public void enableLocalMic() {
        if (engine == null) {
            Toast.makeText(getActivity(), "sdk_not_init", Toast.LENGTH_SHORT).show();
            return;
        }
        publishMicEnable = !publishMicEnable;
        if (publishMicEnable) {
            ib_local_mic.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_bottom_microphone_on));
        } else {
            ib_local_mic.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_bottom_microphone_off));
        }
        /* Enable Mic*/
        engine.muteMicrophone(!publishMicEnable);
    }

    public void enableRemoteMic() {
        if (engine == null) {
            Toast.makeText(getActivity(), "sdk_not_init", Toast.LENGTH_SHORT).show();
            return;
        }
        playStreamMute = !playStreamMute;
        if (playStreamMute) {
            ib_remote_stream_audio.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_bottom_microphone_off));
        } else {
            ib_remote_stream_audio.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_bottom_microphone_on));
        }
        /* Enable Mic*/
        engine.muteAudioOutput(playStreamMute);
    }

    public void openRemoteMic() {
        ib_remote_stream_audio.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_bottom_microphone_on));
        engine.muteAudioOutput(false);
    }

    public void closeRemoteMic() {
        ib_remote_stream_audio.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_bottom_microphone_off));
        engine.muteAudioOutput(true);
    }

    /* ????????????????????? */
    public boolean checkOrRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(), "android.permission.CAMERA") != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(getActivity(), "android.permission.RECORD_AUDIO") != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(PERMISSIONS_STORAGE, ACTION_REQUEST_PERMISSIONS);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onAttach(@NotNull Activity activity) {
        super.onAttach(activity);
        mCtx = activity;//mCtx ?????????????????????????????????
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCtx = null;
    }

    @Override
    public void onDestroyView() {
        // Release SDK resources
        ZegoExpressEngine.destroyEngine(null);
        engine = null;
        super.onDestroyView();
    }
}
