package com.example.leiyu.audiorecdemo;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.BottomSheetDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.czt.mp3recorder.MP3Recorder;
import com.example.leiyu.audiorecdemo.databinding.LfDialogAudioRecordBinding;

import java.io.File;
import java.io.IOException;

import utils.FileUtil;

/**
 * Created by fengjun on 2017/11/23.
 */

public class AudioRecordDialog extends BottomSheetDialog implements View.OnClickListener {
    private static final int RECORD_MIN_TIME = 3;
    private static final int RECORD_MAX_TIME = 15;
    private LfDialogAudioRecordBinding mDataBinding;
    private AnimationDrawable mPlayAnimation;
    private MP3Recorder mMp3Recorder;
    private MediaPlayer mMp3Player;
    private Handler mHandler;
    private int mTimeCount;

    public AudioRecordDialog(Context context) {
        this(context, R.style.lf_dialog_audio_recode_style);
    }

    public AudioRecordDialog(Context context, int theme) {
        super(context, theme);
    }

    public OnAudioRecordListener mListener;

    public interface OnAudioRecordListener {
        void onAudioRecordOk(int duration);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(true);
        setContentView(R.layout.lf_dialog_audio_record);
        mDataBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.lf_dialog_audio_record, null, false);
        setContentView(mDataBinding.getRoot());
        mHandler = new Handler(Looper.getMainLooper());
        setDialogWindow(this);
        setOnDismissListener(mDismissListener);
        setListeners();
    }

    public void setListener(OnAudioRecordListener listener) {
        mListener = listener;
    }

    private void setListeners() {
        mDataBinding.deleteView.setOnClickListener(this);
        mDataBinding.saveView.setOnClickListener(this);
        mDataBinding.replayPlay.setOnClickListener(this);
        mDataBinding.recordView.setOnLongClickListener(mRecordLongClickListener);
        mDataBinding.recordView.setOnTouchListener(mRecordTouchListener);
    }

    private View.OnLongClickListener mRecordLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            boolean startResult = startAudioRecord();
            if(startResult) {
                mDataBinding.rippleView.startWaveAnimation();
            } else {
                Toast.makeText(getContext(), "录音失败", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    };

    private View.OnTouchListener mRecordTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                mHandler.removeCallbacks(mTimeRunnable);
                if(mTimeCount < RECORD_MIN_TIME) {
                    mTimeCount = 0;
                    stopAudioRecord();
                    mDataBinding.recordTime.setText("00:" + (mTimeCount > 9 ? mTimeCount : "0" + mTimeCount));
                    mDataBinding.rippleView.stopWaveAnimation();
                    Toast.makeText(getContext(), "录制时间不可低于3秒，请重试", Toast.LENGTH_SHORT).show();
                } else {
                    goToReplayView();
                }
            }
            return false;
        }
    };

    private void setDialogWindow(Dialog dialog) {
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.CENTER; // 紧贴底部
        lp.width = WindowManager.LayoutParams.MATCH_PARENT; // 宽度持平
        window.setAttributes(lp);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.deleteView) {
            mTimeCount = 0;
            mDataBinding.recordLayout.setVisibility(View.VISIBLE);
            mDataBinding.replayLayout.setVisibility(View.GONE);
            mDataBinding.recordTime.setText("00:" + (mTimeCount > 9 ? mTimeCount : "0" + mTimeCount));
            if(mPlayAnimation != null) {
                mPlayAnimation.stop();
            }
        } else if(id == R.id.replayPlay) {
            if(mMp3Player != null) {
                stopAudioPlayer();
                mDataBinding.replayIcon.setImageResource(R.drawable.lf_bg_audio_replay);
            } else {
                if(startAudioPlayer()) {
                    mDataBinding.replayIcon.setImageResource(R.drawable.lf_drawable_audio_replay_living);
                    mPlayAnimation = (AnimationDrawable) mDataBinding.replayIcon.getDrawable();
                    mPlayAnimation.start();
                } else {
                    Toast.makeText(getContext(), "播放错误", Toast.LENGTH_SHORT).show();
                }
            }
        } else if(id == R.id.saveView) {
            if(mMp3Player != null) {
                stopAudioPlayer();
                mDataBinding.replayIcon.setImageResource(R.drawable.lf_bg_audio_replay);
            }
            if(mListener != null) {
                mListener.onAudioRecordOk(mTimeCount);
            }
            dismiss();
        }
    }

    private boolean startAudioRecord() {
        File audioFile = FileUtil.createSkillAudioFile(getContext());
        if(audioFile.exists()) {
            audioFile.delete();
        }
        if(mMp3Recorder != null && mMp3Recorder.isRecording()) {
            mMp3Recorder.stop();
        }
        mMp3Recorder = new MP3Recorder(audioFile);
        try {
            mMp3Recorder.start();
            mHandler.postDelayed(mTimeRunnable, 1000);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Runnable mTimeRunnable = new Runnable() {
        @Override
        public void run() {
            mHandler.removeCallbacks(mTimeRunnable);
            mTimeCount++;
            mDataBinding.recordTime.setText("00:" + (mTimeCount > 9 ? mTimeCount : "0" + mTimeCount));
            if(mTimeCount < RECORD_MAX_TIME){
                mHandler.postDelayed(mTimeRunnable, 1000);
            }else{
                goToReplayView();
            }
        }
    };

    private void goToReplayView() {
        stopAudioRecord();
        mDataBinding.rippleView.stopWaveAnimation();
        mDataBinding.replayIcon.setImageResource(R.drawable.lf_bg_audio_replay);
        mDataBinding.recordLayout.setVisibility(View.GONE);
        mDataBinding.replayLayout.setVisibility(View.VISIBLE);
        mDataBinding.replayTime.setText("00:" + (mTimeCount > 9 ? mTimeCount : "0" + mTimeCount));
    }

    private void stopAudioRecord() {
        if(mMp3Recorder != null && mMp3Recorder.isRecording()) {
            mMp3Recorder.stop();
        }
    }

    private boolean startAudioPlayer() {
        File audioFile = FileUtil.createSkillAudioFile(getContext());
        if(!audioFile.exists()) {
            return false;
        }
        if(mMp3Player != null) {
            mMp3Player.release();
        }
        mMp3Player = new MediaPlayer();
        try {
            mMp3Player.setDataSource(audioFile.getAbsolutePath());
            mMp3Player.setOnCompletionListener(mPlayCompletionListener);
            mMp3Player.setOnErrorListener(mPlayErrorListener);
            mMp3Player.setVolume(1, 1);
            mMp3Player.setLooping(false);
            mMp3Player.prepare();
            mMp3Player.start();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            mMp3Player.release();
            return false;
        }
    }

    private MediaPlayer.OnCompletionListener mPlayCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            mDataBinding.replayIcon.setImageResource(R.drawable.lf_bg_audio_replay);
        }
    };

    private MediaPlayer.OnErrorListener mPlayErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
            mDataBinding.replayIcon.setImageResource(R.drawable.lf_bg_audio_replay);
            return false;
        }
    };

    private void stopAudioPlayer() {
        if(mMp3Player != null) {
            mMp3Player.release();
            mMp3Player = null;
        }
    }

    private DialogInterface.OnDismissListener mDismissListener = new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialogInterface) {
            stopAudioRecord();
            stopAudioPlayer();
        }
    };
}
