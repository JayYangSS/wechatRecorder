package com.imooc_recorder.view;

import java.io.File;
import java.util.UUID;

import android.media.MediaRecorder;
import android.support.v7.appcompat.R.integer;
import android.util.Log;

public class AudioManager {
	private MediaRecorder mMediaRecorder;
	private String mDir;
	private String mCurrentFilePath;
	private static AudioManager mInstance;
	public boolean isPrepared;
	
	private AudioManager(String dir) {
		// TODO Auto-generated constructor stub
		mDir=dir;
	}
	
	
	//回调准备完毕
	public interface AudioStateListener
	{
		//抽象方法
		void wellPrepared();
	}
	
	public AudioStateListener mListener;
	public void setOnAudioStateListener(AudioStateListener listener) {
		mListener=listener;
		Log.d("setOnAudioStateListener","in");
	}
	
	//这个方法的作用？？？？？
	public static AudioManager getInstance(String dir)
	{
		if(mInstance==null)
		{
			//获得对象锁，一个时间只能有一个线程对该对象进行操作
			synchronized (AudioManager.class) {
				if(mInstance==null)
					mInstance=new AudioManager(dir);
			}
		}
		
		return mInstance;
	}
	
	public void prepareAudio() {
		Log.d("preparedAudio","before_try");
		try {
			Log.d("preparedAudio","In_try");
			isPrepared=false;
			File dir = new File(mDir);
			//这句写错了，没加！，导致dir的文件路径没有创建。。。
			if (!dir.exists())
				dir.mkdirs();
			String fileName = generateFileName();
			File file = new File(dir, fileName);
			
			
			
			mCurrentFilePath=file.getAbsolutePath();
			
			
			mMediaRecorder = new MediaRecorder();
			//设置MediaRecoredr的音频源为麦克风
			mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			//设置输出文件
			mMediaRecorder.setOutputFile(file.getAbsolutePath());
			
			//设置音频的格式
			mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
			//设置音频的编码为amr
			mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			Log.d("preparedAudio","before_prepare");
			//由于之前的路径没有建立，setOutputFile没有设置成功，导致prepare函数抛出异常，程序崩溃
			mMediaRecorder.prepare();
			Log.d("preparedAudio","before_start");
			mMediaRecorder.start();
			//准备结束
			isPrepared=true;
			Log.d("preparedAudio","before_if");
			if(mListener!=null)
				mListener.wellPrepared();
			Log.d("preparedAudio","inside_if");
		} catch (Exception e) {
			// TODO: handle exception
			Log.d("preparedAudio","prepareAudio_exception");
			e.printStackTrace();
		}
		Log.d("preparedAudio","InsidePreparedAudio_out");
	}
	
	//随机生成文件的名称
	private String generateFileName() {
		return UUID.randomUUID().toString()+".amr";
	}

	public int getVoiceLevel(int maxLevel) {
		if(isPrepared)
		{
			try {
				//mMediaRecorder.getMaxAmplitude():1-32767
				int voiceLevel=maxLevel*mMediaRecorder.getMaxAmplitude()/32768+1;
				int p=mMediaRecorder.getMaxAmplitude();
				//经过测试，音量显示没有更新是由于getMaxAmplitude返回总是0，但是在真机测试时可以改变音量的，虚拟机上就不行
				Log.d("getVoiceLevel","maxlevel="+p);
				return voiceLevel;
				
			} catch (IllegalStateException e) {}
		}
		return 1;
	}
	
	public void release() {
		Log.d("release","in");
		mMediaRecorder.stop();
		mMediaRecorder.release();
		mMediaRecorder=null;
		Log.d("release","out");
	}
	
	public void cancel() {
		release();
		if(mCurrentFilePath!=null)
		{
			File file=new File(mCurrentFilePath);
			file.delete();
			mCurrentFilePath=null;
		}
		
	}

	public String getCurrentFilePath() {
		return mCurrentFilePath;
	}
}
