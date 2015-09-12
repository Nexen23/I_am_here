package alex.imhere.view;

import android.animation.TimeAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;

import alex.imhere.R;
import alex.imhere.util.ArrayUtils;

public class UserLayout extends FrameLayout {
	static public final int LIFETIME_DEAFULT = 2300;
	static public final int[] COLORS_DEFAULT = {Color.GRAY, Color.WHITE, Color.BLACK};
	static public final float SIDES_GAP_DEFAULT = 0f;
	static public final boolean START_ANIMATION_ON_CREATION_DEFAULT = false;

	Paint borderPaint, fillPaint;
	Path shapePath, shapeBorderPath, tempPath = new Path();

	int colors[] = {};
	int height = 0, width = 0;
	float sidesGap;

	boolean startAnimationOnCreation;
	TimeAnimator gradientAnimation;

	long lifetime;
	long timeElapsed = 0, accumulatorMs = 0, updateTickMs = 25;
	float gradientOffset = 0f;

	public UserLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public UserLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		if (!isInEditMode()) {
			gradientAnimation = new TimeAnimator();
			parseAttrs(attrs, defStyleAttr);
			onInitialize();
		} else {
			setLifetime(LIFETIME_DEAFULT);
			setSidesGap(SIDES_GAP_DEFAULT);
			setGradientStatesColors(COLORS_DEFAULT.clone());
			setStartAnimationOnCreation(START_ANIMATION_ON_CREATION_DEFAULT);
			onInitialize();

			setBackgroundColor(colors[0]);
		}
	}

	public void parseAttrs(AttributeSet attrs, int defStyleAttr) {
		TypedArray attributes = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.UserLayout, defStyleAttr, 0);
		try
		{
			setLifetime(attributes.getInt(R.styleable.UserLayout_lifetime, LIFETIME_DEAFULT));
			setSidesGap(attributes.getDimension(R.styleable.UserLayout_sides_gaps, SIDES_GAP_DEFAULT));
			setStartAnimationOnCreation( attributes.getBoolean(R.styleable.UserLayout_start_animation_on_creation, START_ANIMATION_ON_CREATION_DEFAULT) );

			int statesColorsResourceId = attributes.getResourceId(R.styleable.UserLayout_states_colors, 0);
			TypedArray colorsResources = getResources().obtainTypedArray(statesColorsResourceId);
			try {
				int[] colorsInts = new int[colorsResources.length()];
				for (int i = 0; i < colorsResources.length(); ++i) {
					colorsInts[i] = colorsResources.getColor(i, Color.BLACK);
				}
				setGradientStatesColors(colorsInts);
			} catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				colorsResources.recycle();
			}
		} catch (Exception e) {
			e.printStackTrace();
			setLifetime(LIFETIME_DEAFULT);
			setSidesGap(SIDES_GAP_DEFAULT);
			setGradientStatesColors(COLORS_DEFAULT.clone());
			setStartAnimationOnCreation(START_ANIMATION_ON_CREATION_DEFAULT);
		} finally {
			attributes.recycle();
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		stopGradientAnimation();
	}

	protected void onInitialize() {
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

		/*Resources r = getResources();
		sidesGap = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, r.getDisplayMetrics());*/
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

		shapePath = getParallelogramPath(width, height, sidesGap);
		shapeBorderPath = getParallelogramPath(width, height, sidesGap);

		resolveTimeElapsed();
		if (startAnimationOnCreation) {
			startGradientAnimation();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.save();
		canvas.translate(-gradientOffset, 0);
		shapePath.offset(gradientOffset, 0f, tempPath);
		canvas.drawPath(tempPath, fillPaint);
		canvas.restore();

		canvas.drawPath(shapeBorderPath, borderPaint);

		// drawing border of real Layout for child Views
		//canvas.drawRect(sidesGap, 0f, (width - 1) - sidesGap, height - 1, borderPaint);

		super.onDraw(canvas);
	}

	private Path getParallelogramPath(float width, float height, float sidesGap) {
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

	public void setStartAnimationOnCreation(boolean startAnimationOnCreation) {
		this.startAnimationOnCreation = startAnimationOnCreation;
	}

	public void setGradientStatesColors(int[] statesColors) {
		ArrayList<Integer> colors = new ArrayList<>();
		for (int i = 0; i < statesColors.length; i++) {
			colors.add(statesColors[i]);
			colors.add(statesColors[i]); // purposly: for states
		}

		this.colors = ArrayUtils.ToInts(colors);
	}

	public void setLifetime(long lifetime) {
		setLifetime(lifetime, 0);
	}

	public void setLifetime(long lifetime, long timeElapsed) {
		this.lifetime = lifetime;
		this.timeElapsed = timeElapsed;
		resolveTimeElapsed();
	}

	public void setSidesGap(float sidesGap) {
		this.sidesGap = sidesGap;
		int sidesGapCeiled = (int) Math.ceil(sidesGap);
		setPadding(sidesGapCeiled, 0, sidesGapCeiled, 0);
	}

	public void startGradientAnimation() {
		stopGradientAnimation();
		resolveTimeElapsed();

		if (!isInEditMode()) {
			TimeAnimator.TimeListener gradientAnimationTickListener = new TimeAnimator.TimeListener() {
				@Override
				public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
					final float gradientOffsetCoef = (float) (updateTickMs) / lifetime;
					final int colorsCount = colors.length - 1;
					final long gradientWidth = width * colorsCount;

					if (totalTime > (lifetime - timeElapsed)) {
						animation.setTimeListener(null);
						animation.cancel();
						gradientOffset = gradientWidth;
						invalidate();
					} else {
						accumulatorMs += deltaTime;

						final long gradientOffsetsCount = accumulatorMs / updateTickMs;
						gradientOffset += (gradientOffsetsCount * gradientWidth) * gradientOffsetCoef;
						accumulatorMs %= updateTickMs;

						boolean gradientOffsetChanged = (gradientOffsetsCount > 0);
						if (gradientOffsetChanged) {
							invalidate();
						}
					}
				}
			};
			gradientAnimation.setTimeListener(gradientAnimationTickListener); // comment this to use preview
			gradientAnimation.start();
		}
	}

	public void stopGradientAnimation() {
		gradientAnimation.setTimeListener(null);
		gradientAnimation.cancel();
		accumulatorMs = 0;
		gradientOffset = 0;
	}

	public boolean isGradientAnimationRunning() {
		return gradientAnimation.isRunning();
	}

	public void clearTimeElapsed() {
		timeElapsed = 0;
	}

	public void resolveTimeElapsed() {
		final float gradientOffsetCoef = (float) (timeElapsed) / lifetime;
		final int colorsCount = this.colors.length - 1;
		final long gradientWidth = width * colorsCount;
		gradientOffset = gradientWidth * gradientOffsetCoef;
	}
}
