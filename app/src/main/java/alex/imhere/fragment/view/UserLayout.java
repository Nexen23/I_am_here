package alex.imhere.fragment.view;

import android.animation.TimeAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
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
	private float padding;

	private TimeAnimator gradientAnimation = new TimeAnimator();
	private long lifetime = LIFETIME_DEAFULT, updateTickMs = 25;
	private long accumulatorMs = 0;
	private float gradientOffset = 0f;

	public UserLayout(Context context) {
		super(context);
		onInitialize();
		// TODO: 24.08.2015 pass statesColors as args for View in XML
	}

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
		/*int userBornColor = getContext().getResources().getColor(R.color.user_born);
		int userAliveColor = getContext().getResources().getColor(R.color.user_alive);
		int userDeadColor = getContext().getResources().getColor(R.color.user_dead);
		int[] colors = new int[]{userBornColor, userAliveColor, userDeadColor};*/
		//setGradientStatesColors(colors);
		//setLifetime(lifetime);

		setWillNotDraw(false);

		borderPaint = new Paint();
		borderPaint.setColor(Color.BLACK);
		borderPaint.setStrokeWidth(1);
		borderPaint.setStyle(Paint.Style.STROKE);

		fillPaint = new Paint();
		fillPaint.setColor(colors[0]);
		fillPaint.setStyle(Paint.Style.FILL);

		Resources r = getResources();
		padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, r.getDisplayMetrics());

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
		//if (isGradientAnimationRunning()) {
			startGradientAnimation();
		//}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		RectF rect = new RectF();
		rect.left = padding + gradientOffset;
		rect.right = width - 1 - padding + gradientOffset;

		rect.top = padding;
		rect.bottom = height - 1 - padding;

		canvas.save();
		canvas.translate(-gradientOffset, 0);
		canvas.drawRect(rect, fillPaint);

		canvas.restore();
		canvas.drawRect(1, 1, width - 2, height - 2, borderPaint);

		super.onDraw(canvas);
	}
}
