package com.zt.androidtests;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Region;

public final class ProgressBarDrawHelper {
	private ProgressBarArgs args;
	private Paint yellowPink;
	private Paint orange;
	private RectF rightCircleMask;
	private float widthOfProgress;

	public ProgressBarDrawHelper(Resources resource) {
		yellowPink = new Paint();
		yellowPink.setColor(resource.getColor(R.color.pink_yellow));
		yellowPink.setStyle(Paint.Style.FILL);
		yellowPink.setAntiAlias(true);

		orange = new Paint(yellowPink);
		orange.setColor(resource.getColor(R.color.orange));
	}

	public void setProgressBarArgs(ProgressBarArgs args) {
		this.args = args;
		rightCircleMask = new RectF(0, 0, 0, 0);
	}

	public void drawLeftCircle(Canvas canvas) {
		canvas.drawCircle(args.leftCircle.centerX, args.leftCircle.centerY, args.leftCircle.radius,
						  orange);
	}

	public void drawRightCircle(Canvas canvas) {
		canvas.drawCircle(args.rightCircle.centerX, args.rightCircle.centerY,
						  args.rightCircle.radius, yellowPink);
	}

	public void drawRectPart(Canvas canvas) {
		canvas.drawRect(args.rectPartOfBar, yellowPink);
	}

	public void updateProgress(int progress) {
		widthOfProgress = progress * args.progressStep;
		if (widthOfProgress <= args.rightCircle.radius) {
			float rightCircleMaskLength = (float) (2 * Math.sqrt(
					Math.pow(args.rightCircle.radius, 2) -
					Math.pow(args.rightCircle.radius - widthOfProgress, 2)));
			final float left = args.wholeBar.right - widthOfProgress;
			final float top = args.rightCircle.radius - rightCircleMaskLength / 2;
			final float right = args.wholeBar.right;
			final float bottom = args.rightCircle.radius + rightCircleMaskLength / 2;
			rightCircleMask.left = left;
			rightCircleMask.top = top;
			rightCircleMask.right = right;
			rightCircleMask.bottom = bottom;
		} else {
			rightCircleMask.left = args.wholeBar.right - args.rightCircle.radius + args.dirtyOffset;
			rightCircleMask.top = args.wholeBar.top;
			rightCircleMask.right = args.wholeBar.right;
			rightCircleMask.bottom = args.wholeBar.bottom;
		}
	}

	public void drawProgressInRightHalfCirclePart(Canvas canvas) {
		canvas.save();
		canvas.clipRect(rightCircleMask, Region.Op.INTERSECT);
		canvas.drawCircle(args.rightCircle.centerX, args.rightCircle.centerY, args.rightCircle.radius, orange);
		canvas.restore();
	}

	public void drawProgressInRectPart(Canvas canvas) {
		if (widthOfProgress <= args.rightCircle.radius) {
			return;
		}
		canvas.drawRect(args.wholeBar.right - widthOfProgress, args.wholeBar.top,
						args.rectPartOfBar.right, args.rectPartOfBar.bottom, orange);
	}

	public float getProgressX() {
		return args.wholeBar.width() - widthOfProgress;
	}

	public float[] getProgressHRange() {
		float[] range = new float[2];
		range[0] = rightCircleMask.top;
		range[1] = rightCircleMask.bottom;
		return range;
	}

}
