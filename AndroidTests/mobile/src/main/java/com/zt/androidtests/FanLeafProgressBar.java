package com.zt.androidtests;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TypeEvaluator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Random;

public class FanLeafProgressBar extends FrameLayout {
	Handler ui;
	private ImageView fan;
	private ProgressBarView bar;

	public FanLeafProgressBar(Context context) {
		super(context);
		init();
	}

	public FanLeafProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public FanLeafProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public FanLeafProgressBar(Context context, AttributeSet attrs, int defStyleAttr,
							  int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}

	private void init() {
		ui = new UI();
		LayoutInflater.from(getContext()).inflate(R.layout.fan_leaf_progress_bar, this);
		fan = (ImageView) findViewById(R.id.fan);
		bar = (ProgressBarView) findViewById(R.id.bar);
	}

	public void startLoading() {
		Log.d("ZT", "start loading");
		ui.removeMessages(UI.MSG_CHECK_MEASURED);
		Message message = ui.obtainMessage(UI.MSG_CHECK_MEASURED, this);
		message.sendToTarget();
	}

	private void realStartLoading() {
		Animator ro = AnimatorInflater.loadAnimator(getContext(), R.animator.rotate);
		ro.setTarget(fan);
		ro.start();

		initLeafAnimator(createLeaf());

		setRepeat();
	}

	private ImageView createLeaf() {
		ImageView leaf = new ImageView(getContext());
		leaf.setImageDrawable(getResources().getDrawable(R.drawable.leaf));
		leaf.setMaxHeight(30);
		leaf.setMaxWidth(30);
		leaf.setAdjustViewBounds(true);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		leaf.setLayoutParams(params);
		leaf.setVisibility(View.VISIBLE);
		addView(leaf);
		return leaf;
	}

	private void initLeafAnimator(final ImageView leaf) {
		PropertyValuesHolder tX = PropertyValuesHolder.ofFloat("translationX", 0f,
															   bar.getCurrentProgressX());
		final float topY = bar.getCurrentProgressHRange()[0];
		final float bottomY = bar.getCurrentProgressHRange()[1];
		final float range = new Random(SystemClock.elapsedRealtime()).nextFloat() *
							(-2 * (bottomY - topY)) - bottomY;
		PropertyValuesHolder tY = PropertyValuesHolder.ofFloat("translationY", 0f, 0f);
		tY.setEvaluator(new Sin(leaf, bar.getCurrentProgressX()));

		ObjectAnimator sinAnimator = ObjectAnimator.ofPropertyValuesHolder(leaf, tX, tY);
		sinAnimator.setDuration(3000);
		sinAnimator.setInterpolator(new DecelerateInterpolator());

		final int rotateCount = new Random(System.nanoTime()).nextInt(7) + 1;
		ObjectAnimator rotation = ObjectAnimator.ofFloat(leaf, "rotation", 0f, (float)(rotateCount * 180));
		rotation.setDuration(3000);
		rotation.setInterpolator(new DecelerateInterpolator());

		final int costTime = new Random(System.nanoTime()).nextInt(6) * 200;
		ObjectAnimator alpha = ObjectAnimator.ofFloat(leaf, "alpha", 0f, 1f);
		alpha.setDuration(costTime);
		alpha.setInterpolator(new LinearInterpolator());

		final ObjectAnimator disappear = ObjectAnimator.ofFloat(leaf, "alpha", 1f, 0f);
		disappear.setDuration(costTime);
		disappear.setInterpolator(new LinearInterpolator());
		disappear.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				leaf.setVisibility(View.GONE);
				removeView(leaf);
				animation.removeAllListeners();
			}
		});
		rotation.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				disappear.start();
				animation.removeAllListeners();

			}
		});

		AnimatorSet set = new AnimatorSet();
		set.playTogether(sinAnimator, rotation, alpha);
		set.start();
	}

	private void setRepeat() {
		final Runnable doAnimator = new Runnable() {
			@Override
			public void run() {
				initLeafAnimator(createLeaf());
			}
		};
		Runnable repeat = new Runnable() {
			@Override
			public void run() {
				ui.postDelayed(doAnimator, 1000);
				ui.postDelayed(this, 1000);
			}
		};
		repeat.run();
	}

	private class Sin implements TypeEvaluator<Float> {
		private ImageView leaf;
		private int period;
		public Sin(ImageView leaf, float xTotal) {
			this.leaf = leaf;
			period = Math.round(xTotal);
		}

		@Override
		public Float evaluate(float fraction, Float startValue, Float endValue) {
			final float amplitude = bar.getBarHeight() / 2;
			final float w = (float) ((5 * Math.PI) / (2 * Math.round(bar.getCurrentProgressX())));
			final float offset = (endValue - startValue) /** fraction*/;
			final float result = startValue + amplitude * (float) Math.sin(
					w * leaf.getTranslationX() - Math.PI / 2) + bar.getBarHeight() / 2/*offset*/;
			return result;
		}
	}

	private static final class UI extends Handler {
		public static final int MSG_START_LOADING = 0x01;
		public static final int MSG_CHECK_MEASURED = 0x02;

		private static final int CHECK_DELAY_MS = 100;

		public UI() {
			super(Looper.getMainLooper());
		}

		@Override
		public void handleMessage(Message msg) {
			final int what = msg.what;
			final FanLeafProgressBar view = (FanLeafProgressBar) msg.obj;
			if (MSG_START_LOADING == what) {
				view.realStartLoading();
			} else if (MSG_CHECK_MEASURED == what) {
				if (view.bar.isMeasured()) {
					removeMessages(MSG_START_LOADING);
					sendMessage(obtainMessage(MSG_START_LOADING, view));
				} else {
					removeMessages(MSG_CHECK_MEASURED);
					sendMessageDelayed(obtainMessage(MSG_CHECK_MEASURED, view), CHECK_DELAY_MS);
				}
			}
			super.handleMessage(msg);
		}
	}


}
