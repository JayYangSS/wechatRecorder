package com.imooc_recorder.view;

import com.imooc_recorder.R;
import com.imooc_recorder.view.AudioManager.AudioStateListener;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class AudioRecorderButton extends Button implements AudioStateListener {

	private static final int STATE_NORMAL=1;
	private static final int STATE_RECORDING=2;
	private static final int STATE_WANT_TO_CANCEL=3;
	private static final int DISTANCE_CANCEL=50;
	//当前状态
	private int mCurState=STATE_NORMAL;
	//是否正在录音
	private boolean isRecording = false;
	private DialogManager mDialogManager;
	private AudioManager mAudioManager;
	private float mTime;
	private boolean mReady;//是否触发longClick
	
	public AudioRecorderButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDialogManager=new DialogManager(getContext());
		//TODO:要增加判断是否存在外部存储卡
		String dir=Environment.getExternalStorageDirectory()+"/imooc_recorder_audios";
		mAudioManager=AudioManager.getInstance(dir);
		Log.d("AudioRecorderButton","before");
		mAudioManager.setOnAudioStateListener(this);
		
		setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				mReady=true;
				Log.d("longclick","beforeClick");
				mAudioManager.prepareAudio();
				return false;
			}
		});
	}
	
	//录音完成后的回调
	public interface AudioFinishRecorderListener {
		void onFinish(float seconds,String filePath);
	}
	
	private AudioFinishRecorderListener mListener;
	
	public void setAudioFinishRecorderListener(AudioFinishRecorderListener listener) {
		mListener=listener;
	}
	
	//获取音量大小的runnable
	private Runnable mGetVoiceLevelRunnable=new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(isRecording)
			{
				Log.d("mGetVoiceLevelRunnable","before_try");
				try {
					Thread.sleep(100);
					mTime+=0.1f;//计时
					mHandler.sendEmptyMessage(MSG_VOICE_CHANGED);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};
	
	
	private static final int MSG_AUDIO_PREPARED=0X110;
	private static final int MSG_VOICE_CHANGED=0X111;
	private static final int MSG_DIALOG_DIMISS=0X112;
	private Handler mHandler=new Handler()
	{
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_AUDIO_PREPARED:
				//显示应在在audio end prepare之后
				mDialogManager.showRecordingDialog();
				isRecording=true;
				Log.d("handleMessage","beforeThread");
				//将下面得多线程注释掉在button up时就不会出错，因此问题的原因应该是mAudioManager在BUTTON_UP
				//时已经release掉了，但是在下面得多线程中还去发送MSG_VOICE_CHANGED消息给handler，在调用getVoiceLevel时出错
				new Thread(mGetVoiceLevelRunnable).start();
				Log.d("handleMessage","afterThread");
				break;
			case MSG_VOICE_CHANGED:
				Log.d("MSG_VOICE_CHANGED","in");
				//mDialogManager.showRecordingDialog();
				//测试音量对话框无法更新是getVoiceLevel没有更新
				mDialogManager.updateVoice(mAudioManager.getVoiceLevel(7));			
				break;
			case MSG_DIALOG_DIMISS:
				mDialogManager.dimissDialog();
	            break;

			default:
				break;
			}
	}
  };

	
	@Override
	public void wellPrepared() {
		// TODO Auto-generated method stub
		Log.d("wellPrepared","InsideWellPrepared_before");
		mHandler.sendEmptyMessage(MSG_AUDIO_PREPARED);
		Log.d("wellPrepared","InsideWellPrepared");
	}

	public AudioRecorderButton(Context context) {
		this(context,null);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event){
		// TODO Auto-generated method stub
		int action=event.getAction();
		int x=(int)event.getX();
		int y=(int)event.getY();
		
		
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			changeState(STATE_RECORDING);
			break;
		case MotionEvent.ACTION_MOVE:
			if(isRecording)
			{
				if(isCancel(x,y))
				{
					changeState(STATE_WANT_TO_CANCEL);
				}else {
					changeState(STATE_RECORDING);
				}
			}
			
			break;
		case MotionEvent.ACTION_UP:
			Log.d("ACTION_UP","mReady="+mReady);
			if(!mReady)
			{
				reset();
				return super.onTouchEvent(event);
			}
			if(!isRecording||mTime<0.6f)
			{
				mDialogManager.tooShort();
				mAudioManager.cancel();
				//Log.d("ACTION_UP","up_dissmiss");
				mHandler.sendEmptyMessageDelayed(MSG_DIALOG_DIMISS,300);//延时1.3s
				//Log.d("ACTION_UP","up_dissmiss_done");
			}
			else if(mCurState==STATE_RECORDING) //正常录制结束
			{
				Log.d("ACTION_UP","in1");
				mDialogManager.dimissDialog();
				mAudioManager.release();
				Log.d("ACTION_UP","out1");
				if(mListener!=null)
				{
					Log.d("ACTION_UP","mTime="+mTime);
					mListener.onFinish(mTime, mAudioManager.getCurrentFilePath());
				}
	
			}else if (mCurState==STATE_WANT_TO_CANCEL)
			{
				//cancel
				mDialogManager.dimissDialog();
				mAudioManager.cancel();
			}
			reset();
			Log.d("ACTION_UP","break");
			break;

		default:
			break;
		}
		return super.onTouchEvent(event);
	}

//恢复状态及标志位
	private void reset() {
   /***
    * 在复位时，记得将mAudioManager的isPrepared复位为false(未准备好状态)，这样统计音量的线程就不会再运行，就不会BUTTON_UP时出错了
	*由于在mGetVoiceLevelRunnable线程中，要sleep（）100ms,这就出问题了，若进入while(isRecording)中之前，还没有BUTTON_UP，
	*在sleep了100ms后，BUTTON_UP了，mAudioManager已经释放了，但此时mGetVoiceLevelRunnable继续发送VOICE_CHANGED消息，当去
	*执行getVoiceLevel时，getMaxAmplitude就会出错*/
		isRecording=false;
		mReady=false;
		mTime=0;
		mAudioManager.isPrepared=false;
		changeState(STATE_NORMAL);
	}

   //如果想要取消（超出范围），返回ture
	private boolean isCancel(int x, int y) {
		// TODO Auto-generated method stub
		if(x<0||x>getWidth())
			return true;
		if(y<-DISTANCE_CANCEL||y>getHeight()+DISTANCE_CANCEL)
			return true;
		return false;
	}


	private void changeState(int state) {
		// TODO Auto-generated method stub
		if (mCurState!=state) {
			mCurState=state;
			switch (state) {
			case STATE_NORMAL:
				setBackgroundResource(R.drawable.btn_recorder_normal);
				setText(R.string.str_recorder_normal);
				Log.d("STATE_NORMAL","break");
				break;
			case STATE_RECORDING:
				Log.d("enentDown","beforeDown");
				setBackgroundResource(R.drawable.btn_recordering);
				Log.d("enentDown","afterDown");
				setText(R.string.str_recorder_recording);
				if(isRecording){
					mDialogManager.recording();
				}
				break;
			case STATE_WANT_TO_CANCEL:
				setBackgroundResource(R.drawable.btn_recordering);
				setText(R.string.str_recorder_want_cancel);
				mDialogManager.wantToCancel();
				break;

			default:
				break;
			}
		}
	}

}
