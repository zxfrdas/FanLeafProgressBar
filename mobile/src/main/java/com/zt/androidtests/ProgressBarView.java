package com.zt.androidtests;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.concurrent.atomic.AtomicBoolean;

public class ProgressBarView extends View {
	private boolean isHasFocused;
	private boolean isMeasured;
	private int tempWidth;
	private int tempHeight;
	private Handler updateHandler;
	private ProgressBarDrawHelper drawHelper;
	private int drawCostThisTime;
	private int progress;
	private IProgressBarListener listener;
	private AtomicBoolean isLoad;
	private int step;

	public ProgressBarView(Context context) {
		super(context);
		init();
	}

	public ProgressBarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ProgressBarView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public ProgressBarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}

	private void init() {
		drawHelper = new ProgressBarDrawHelper(getResources());
		updateHandler = new UpdateHandler();
		progress = 0;
		isLoad = new AtomicBoolean(false);
		step = 50;
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
		isHasFocused = true;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (!isHasFocused) {
			return;
		}
		if (isMeasured) {
			return;
		}
		if (0 == tempWidth) {
			tempWidth = getMeasuredWidth();
			tempHeight = getMeasuredHeight();
			return;
		}
		if (getMeasuredHeight() <= tempHeight && getMeasuredWidth() <= tempWidth) {
			tempHeight = getMeasuredHeight();
			tempWidth = getMeasuredWidth();
			isMeasured = true;

			ProgressBarArgs args = initProgressArgs((float)tempHeight / 2);
			drawHelper.setProgressBarArgs(args);

			Log.d("ZT", "tempWidth = " + tempWidth + ", tempHeight = " + tempHeight);
			invalidate();
		}
	}

	private ProgressBarArgs initProgressArgs(float radius) {
		ProgressBarArgs args = ProgressBarArgs.getInstance();
		args.setWholeBar(new RectF(0, 0, tempWidth, tempHeight));
		args.setLeftCircle(new ProgressBarArgs.CircleF(radius, radius, radius));
		args.setRightCircle(
				new ProgressBarArgs.CircleF(radius, tempWidth - radius, radius));
		args.setRectPartOfBar(new RectF(radius, 0, tempWidth - radius, tempHeight));
		args.setProgressStep((args.wholeBar.width() - args.leftCircle.radius) / 100);
		args.setDirtyOffset(-1);
		return args;
	}

	public void startLoading() {
		isLoad.set(true);
		callUpdate();
	}

	public boolean isMeasured() {
		return isMeasured;
	}

	public float getCurrentProgressX() {
		return drawHelper.getProgressX();
	}

	public float[] getCurrentProgressHRange() {
		return drawHelper.getProgressHRange();
	}

	public float getBarHeight() {
		return ProgressBarArgs.getInstance().wholeBar.height();
	}

	public void setListener(IProgressBarListener listener) {
		this.listener = listener;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		long drawStart = SystemClock.elapsedRealtime();
		if (!isMeasured) {
			return;
		}
		drawProgressBar(canvas);
		updateProgress(canvas);
		drawCostThisTime = (int) (SystemClock.elapsedRealtime() - drawStart);
//		invalidate();
	}

	private void drawProgressBar(Canvas canvas) {
		drawHelper.drawRectPart(canvas);
		drawHelper.drawLeftCircle(canvas);
		drawHelper.drawRightCircle(canvas);
	}

	private void updateProgress(Canvas canvas) {
		if (isLoad.get()) {
			if (100 > progress) {
				progress += step;
			} else {
				isLoad.set(false);
			}
		}
		drawHelper.updateProgress(progress);
		drawHelper.drawProgressInRightHalfCirclePart(canvas);
		drawHelper.drawProgressInRectPart(canvas);
	}

	private void callUpdate() {
		Message msg = updateHandler.obtainMessage(UpdateHandler.MSG_VALIDATE);
		msg.obj = ProgressBarView.this;
		msg.sendToTarget();
	}

	private static final class UpdateHandler extends Handler {
		public static final int MSG_VALIDATE = 0x01;
		public static final int MSG_END = 0x02;

		public UpdateHandler() {
			super(Looper.getMainLooper());
		}

		@Override
		public void handleMessage(Message msg) {
			final int what = msg.what;
			if (MSG_VALIDATE == what) {
				final ProgressBarView view = (ProgressBarView) msg.obj;
				view.invalidate();
			} else if (MSG_END == what) {
				final IProgressBarListener listener = (IProgressBarListener) msg.obj;
				if (null != listener) {
					listener.onProgressEnd();
				}
			}
		}
	}

}
