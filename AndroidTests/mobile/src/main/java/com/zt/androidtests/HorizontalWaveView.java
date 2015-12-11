package com.zt.androidtests;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class HorizontalWaveView extends View {
	private Path mWavePath;
	private Paint mPaint;
	private int mMeasuredWidth;
	private int mMeasuredHeight;
	private Rect mRect;
	private Handler mHandler;
	private boolean isWindowFocused;
	private boolean isMeasureFinishThisTurn;

	public HorizontalWaveView(Context context) {
		super(context);
		init();
	}

	public HorizontalWaveView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public HorizontalWaveView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public HorizontalWaveView(Context context, AttributeSet attrs, int defStyleAttr,
							  int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}

	private void init() {
		mWavePath = new Path();

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(getContext().getResources().getColor(R.color.orange));

		mRect = new Rect();

		mHandler = new UpdateHandler();
	}

	public void increaseProgress(int step) {
		mHandler.removeMessages(UpdateHandler.MSG_UPDATE_RECT);
		mHandler.obtainMessage(UpdateHandler.MSG_UPDATE_RECT, step, -1,
							   HorizontalWaveView.this).sendToTarget();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (!isWindowFocused) {
			return;
		}
		if (isMeasureFinishThisTurn) {
			return;
		}
		mMeasuredWidth = getMeasuredWidth();
		mMeasuredHeight = getMeasuredHeight();
//		Log.d("ZT", "measure width = " + mMeasuredWidth + ", measure height = " + mMeasuredHeight);
		update();
	}

	private void update() {
		mRect.left = mMeasuredWidth;
		mRect.top = 0;
		mRect.right = mMeasuredWidth;
		mRect.bottom = mMeasuredHeight;
//		post(new Runnable() {
//			@Override
//			public void run() {
//				mHandler.removeMessages(UpdateHandler.MSG_UPDATE_RECT);
//				mHandler.obtainMessage(UpdateHandler.MSG_UPDATE_RECT,
//									   HorizontalWaveView.this).sendToTarget();
//			}
//		});
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
//		Log.d("ZT", "onWindowFocusChaged focus = " + hasWindowFocus);
		isWindowFocused = hasWindowFocus;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawRect(mRect, mPaint);
	}

	private static final class UpdateHandler extends Handler {
		public static final int MSG_UPDATE_RECT = 0x01;
		public UpdateHandler() {
			super(Looper.getMainLooper());
		}

		@Override
		public void handleMessage(Message msg) {
			final int what = msg.what;
			final HorizontalWaveView view = (HorizontalWaveView) msg.obj;
			final int step = msg.arg1;
			if (MSG_UPDATE_RECT == what) {
				if (!view.isMeasureFinishThisTurn) {
					view.isMeasureFinishThisTurn = true;
				}
				if (0 >= view.mRect.left) {
					removeMessages(UpdateHandler.MSG_UPDATE_RECT);
					Log.d("ZT", "done");
					return;
				}
				view.mRect.left -= step;
				view.invalidate();
//				Message message = obtainMessage(UpdateHandler.MSG_UPDATE_RECT, view);
//				removeMessages(UpdateHandler.MSG_UPDATE_RECT);
//				sendMessageDelayed(message, 16);
			}
		}
	}

}
