package alex.imhere.fragment.view;

import android.animation.TimeAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.FrameLayout;

import java.util.ArrayList;

import alex.imhere.R;
import alex.imhere.util.ArrayUtils;

public class UserLayout extends FrameLayout {
	static public final int LIFETIME_DEAFULT = 2300;
	static public final int[] COLORS_DEFAULT = {Color.WHITE, Color.BLACK};

	private Paint borderPaint, fillPaint;

	private int colors[] = {};
	private int height = 0, width = 0;

	private TimeAnimator gradientAnimation = new TimeAnimator();
	private long lifetime = LIFETIME_DEAFULT, updateTickMs = 25;
	private long accumulatorMs = 0;
	private float gradientOffset = 0f;
	private float sidesGap;
	private Path shapePath, shapeBorderPath, tempPath = new Path();

	public UserLayout(Context context, AttributeSet attrs) {
		super(context, attrs);

		parseAttrs(attrs, 0);
		onInitialize();
	}

	public UserLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		parseAttrs(attrs, defStyleAttr);
		onInitialize();
	}

	public void parseAttrs(AttributeSet attrs, int defStyleAttr) {
		// TODO: 24.08.2015 what is defStyleAttr?
		TypedArray attributes = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.UserLayout, defStyleAttr, 0);
		try
		{
			setLifetime( attributes.getInt(R.styleable.UserLayout_lifetime, LIFETIME_DEAFULT) );
			int statesColorsResourceId = attributes.getResourceId(R.styleable.UserLayout_states_colors, 0);
			TypedArray colorsResources = getResources().obtainTypedArray(statesColorsResourceId);
			int[] colorsInts = new int[colorsResources.length()];
			for (int i = 0; i < colorsResources.length(); ++i) {
				colorsInts[i] = colorsResources.getColor(i, Color.BLACK);
			}
			setGradientStatesColors(colorsInts);
		}
		catch (Exception e) {
			e.printStackTrace();
			setLifetime(LIFETIME_DEAFULT);
			setGradientStatesColors(COLORS_DEFAULT.clone());
		}
		finally
		{
			attributes.recycle();
		}
	}

	private void onInitialize() {
		setWillNotDraw(false);

		borderPaint = new Paint();
		borderPaint.setColor(Color.BLACK);
		borderPaint.setStrokeWidth(2);
		borderPaint.setStyle(Paint.Style.STROKE);
		borderPaint.setAntiAlias(true);

		fillPaint = new Paint();
		fillPaint.setColor(colors[0]);
		fillPaint.setStyle(Paint.Style.FILL);
		fillPaint.setAntiAlias(true);

		Resources r = getResources();
		sidesGap = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, r.getDisplayMetrics());

	}

	public void setGradientStatesColors(int[] statesColors) {
		ArrayList<Integer> colors = new ArrayList<>();
		for (int i = 0; i < statesColors.length; i++) {
			colors.add(statesColors[i]);
			colors.add(statesColors[i]); // purposly
		}

		this.colors = ArrayUtils.ToInts(colors);
	}

	public void setLifetime(long lifetime) {
		this.lifetime = lifetime;
	}

	public void startGradientAnimation() {
		stopGradientAnimation();

		final float gradientOffsetCoef = (float) (updateTickMs) / lifetime;
		final int colorsCount = this.colors.length - 1;
		gradientAnimation.setTimeListener(new TimeAnimator.TimeListener() {
			@Override
			public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
				//totalTime = totalTime % lifetime; // TODO: 24.08.2015 delete this after debugging

				final long gradientWidth = width * colorsCount;
				if (totalTime > lifetime) {
					animation.cancel();
					gradientOffset = gradientWidth;
					invalidate();
				} else {
					accumulatorMs += deltaTime;

					final long gradientOffsetsCount = accumulatorMs / updateTickMs;
					gradientOffset += (gradientOffsetsCount * gradientWidth) * gradientOffsetCoef;
					accumulatorMs %= updateTickMs;

					boolean gradientOffsetChanged = (gradientOffsetsCount > 0) ? true : false;
					if (gradientOffsetChanged) {
						invalidate();
					}
				}
			}
		});

		gradientAnimation.start();
	}

	public void stopGradientAnimation() {
		gradientAnimation.cancel();
		accumulatorMs = 0;
		gradientOffset = 0;
	}

	public boolean isGradientAnimationRunning() {
		return gradientAnimation.isRunning();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		width = getWidth();
		height = getHeight();

		LinearGradient gradient = new LinearGradient(
				0, height / 2, width * colors.length - 1, height / 2,
				colors, null, Shader.TileMode.REPEAT);
		fillPaint.setShader(gradient);

		shapePath = getParallelogrammPath(width, height, sidesGap);
		shapeBorderPath = getParallelogrammPath(width, height, sidesGap);

		//if (isGradientAnimationRunning()) {
			startGradientAnimation();
		//}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.save();
		canvas.translate(-gradientOffset, 0);
		shapePath.offset(gradientOffset, 0f, tempPath);
		canvas.drawPath(tempPath, fillPaint);
		canvas.restore();

		canvas.drawPath(shapeBorderPath, borderPaint);

		super.onDraw(canvas);
	}

	private Path getParallelogrammPath(float width, float height, float sidesGap) {
		Path path = new Path();

		float[] pLeftBottom = {0f, height - 1},
				pLeftTop = {sidesGap, 0f},
				pRightTop = {(width - 1), 0f},
				pRightBottom = {(width - 1) - sidesGap, height - 1};

		path.moveTo(pLeftBottom[0], pLeftBottom[1]);

		path.lineTo(pLeftTop[0], pLeftTop[1]);
		path.lineTo(pRightTop[0], pRightTop[1]);
		path.lineTo(pRightBottom[0], pRightBottom[1]);
		path.lineTo(pLeftBottom[0], pLeftBottom[1]);

		return path;
	}
}
