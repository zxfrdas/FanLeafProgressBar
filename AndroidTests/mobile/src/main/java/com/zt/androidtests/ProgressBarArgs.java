package com.zt.androidtests;

import android.graphics.RectF;

public final class ProgressBarArgs {
	public RectF rectPartOfBar;
	public RectF wholeBar;
	public CircleF leftCircle;
	public CircleF rightCircle;
	public float progressStep;

	public static ProgressBarArgs getInstance() {
		return InstanceHolder.sInstance;
	}

	public void setRectPartOfBar(RectF rectF) {
		rectPartOfBar = new RectF(rectF);
	}

	public float dirtyOffset;

	private ProgressBarArgs() {}

	public void setWholeBar(RectF rectF) {
		wholeBar = new RectF(rectF);
	}

	public void setLeftCircle(CircleF circle) {
		leftCircle = new CircleF(circle);
	}

	public void setRightCircle(CircleF circle) {
		rightCircle = new CircleF(circle);
	}

	public void setDirtyOffset(float dirtyOffset) {
		this.dirtyOffset = dirtyOffset;
	}

	public void setProgressStep(float progressStep) {
		this.progressStep = progressStep;
	}

	public static final class CircleF {
		public float radius;
		public float centerX;
		public float centerY;

		public CircleF(float radius, float cx, float cy) {
			this.radius = radius;
			this.centerX = cx;
			this.centerY = cy;
		}

		public CircleF(CircleF circleF) {
			this.radius = circleF.radius;
			this.centerX = circleF.centerX;
			this.centerY = circleF.centerY;
		}

	}

	private static final class InstanceHolder {
		private static final ProgressBarArgs sInstance = new ProgressBarArgs();
	}
}