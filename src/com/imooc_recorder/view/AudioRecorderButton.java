package com.imooc_recorder.view;

import com.imooc_recorder.R;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class AudioRecorderButton extends Button {

	private static final int STATE_NORMAL=1;
	private static final int STATE_RECORDING=2;
	private static final int STATE_WANT_TO_CANCEL=3;
	private static final int DISTANCE_CANCEL=50;
	//当前状态
	private int mCurState=STATE_NORMAL;
	//是否正在录音
	private boolean isRecording = false;
	private DialogManager mDialogManager;
	
	
	
	
	public AudioRecorderButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDialogManager=new DialogManager(getContext());
		setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				//TODO:真正显示应在在audio end prepare之后
				mDialogManager.showRecordingDialog();
				isRecording=true;
				return false;
			}
		});
	}
	

	public AudioRecorderButton(Context context) {
		this(context,null);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
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
			if(mCurState==STATE_RECORDING){
				//release
				//callbackToActivity
			}else if (mCurState==STATE_WANT_TO_CANCEL) {
				//cancel
				
			}
			reset();
			break;

		default:
			break;
		}
		return super.onTouchEvent(event);
	}

//恢复状态及标志位
	private void reset() {
		// TODO Auto-generated method stub
		isRecording=false;
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
				break;
			case STATE_RECORDING:
				setBackgroundResource(R.drawable.btn_recordering);
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
