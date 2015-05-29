package com.imooc_recorder.view;

import com.imooc_recorder.R;

import android.R.integer;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;

public class AudioRecorderButton extends Button {

	private static final int STATE_NORMAL=1;
	private static final int STATE_RECORDING=2;
	private static final int STATE_WANT_TO_CANCEL=3;
	private static final int DISTANCE_CANCEL=50;
	//当前状态
	private int mCurState=STATE_NORMAL;
	//是否正在录音
	boolean isRecording = false;
	public AudioRecorderButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
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
			//TODO
			isRecording=true;
			changeState(STATE_RECORDING);
			break;
		case MotionEvent.ACTION_MOVE:
			if(isRecording)
			{
				if(wantToCancel(x,y))
				{
					changeState(STATE_WANT_TO_CANCEL);
				}else {
					changeState(STATE_NORMAL);
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


	private boolean wantToCancel(int x, int y) {
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
					//TODO:Dialog.recording();
				}
				break;
			case STATE_WANT_TO_CANCEL:
				setBackgroundResource(R.drawable.btn_recordering);
				setText(R.string.str_recorder_want_cancel);
				//TODO:dialog.wantCancel();
				break;

			default:
				break;
			}
		}
	}
}
