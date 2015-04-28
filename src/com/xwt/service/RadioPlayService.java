package com.xwt.service;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;

import com.xwt.util.RadioOperationInfo;

public class RadioPlayService extends Service implements OnPreparedListener
{
    private MediaPlayer mMediaPlayer = null;
    
    private String RadioName;
    
    private String RadioPath;
    
    private RadioOperationReceiver mReceiver = null;
    
    private boolean ISFIRST = false;
    
    @Override
    public IBinder onBind(Intent intent)
    {
        
        return null;
    }
    
    @Override
    public void onCreate()
    {
        
        super.onCreate();
        if (mMediaPlayer != null)
        {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (mReceiver != null)
        {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        mMediaPlayer = new MediaPlayer(RadioPlayService.this);
        mReceiver = new RadioOperationReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(RadioOperationInfo.RADIO_OPERATION_PLAY);
        filter.addAction(RadioOperationInfo.RADIO_OPERATION_STOP);
        registerReceiver(mReceiver, filter);
        
    }
    
    @Override
    public void onDestroy()
    {
        
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        if (mReceiver != null)
        {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        if (mMediaPlayer != null)
        {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
    
    @Override
    public void onPrepared(MediaPlayer mp)
    {
        mMediaPlayer.start();
    }
    
    private void RadioStop()
    {
        if (mMediaPlayer != null)
        {
            mMediaPlayer.stop();
        }
    }
    
    private void NextRadioPlay()
    {
        if (mMediaPlayer != null)
        {
            mMediaPlayer.start();
        }
    }
    
    private void FirstRadioPlay()
    {
        try
        {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(RadioPath);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(this);
            mHandler.sendEmptyMessage(1);
            Intent intent = new Intent();
            intent.setAction(RadioOperationInfo.RADIO_OPERATION_CHANGE);
            sendBroadcast(intent);
        }
        catch (Exception e)
        {
            System.out.println("Error" + e.getMessage());
        }
    }
    
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler()
    {
        public void handleMessage(android.os.Message msg)
        {
            if (msg.what == 1)
            {
                if (mMediaPlayer != null)
                {
                    Intent intent = new Intent();
                    intent.setAction(RadioOperationInfo.RADIO_OPERATION_ACTION);
                    intent.putExtra(RadioOperationInfo.RADIO_INFO_NAME, RadioName);
                    intent.putExtra(RadioOperationInfo.RADIO_OPERATION_ISPLAY, mMediaPlayer.isPlaying());
                    sendBroadcast(intent);
                    if (RadioName != null)
                    {
                        mHandler.sendEmptyMessageDelayed(1, 1000);
                    }
                }
            }
        };
    };
    
    private class RadioOperationReceiver extends BroadcastReceiver
    {
        
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (RadioOperationInfo.RADIO_OPERATION_PLAY.equals(action))
            {
                String TempRadioName = intent.getStringExtra(RadioOperationInfo.RADIO_INFO_NAME);
                String TempRadioPath = intent.getStringExtra(RadioOperationInfo.RADIO_INFO_PATH);
                if (TempRadioName.equals(RadioName) == false)
                {
                    ISFIRST = false;
                    RadioName = TempRadioName;
                    RadioPath = TempRadioPath;
                    if (RadioPath != null && ISFIRST == false)
                    {
                        FirstRadioPlay();
                        ISFIRST = true;
                        System.out.println("-->1service" + ISFIRST);
                    }
                }
                else
                {
                    NextRadioPlay();
                    System.out.println("-->2service" + ISFIRST);
                }
            }
            else if (RadioOperationInfo.RADIO_OPERATION_STOP.equals(action))
            {
                String TempRadioName = intent.getStringExtra(RadioOperationInfo.RADIO_INFO_NAME);
                if (RadioPath != null && TempRadioName.equals(RadioName))
                {
                    RadioStop();
                }
            }
        }
        
    }
}
