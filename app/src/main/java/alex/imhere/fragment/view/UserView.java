package alex.imhere.fragment.view;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import alex.imhere.R;

public class UserView extends View {
	private Paint borderPaint, fillPaint;
	private LinearGradient gradient;

	private long lifetime = 2000;

	int userBornColor;
	int userAliveColor;
	int userDeadColor;
	private int height, width;
	private float circlePx;
	private float padding;

	int colors[];

	public UserView(Context context/*, long lifetime*/) {
		super(context);
		initialize(lifetime);
		// TODO: 24.08.2015 pass colors as args for View in XML
	}

	public UserView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(lifetime);
	}

	public UserView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initialize(lifetime);
	}

	private void initialize(long lifetime) {
		this.lifetime = lifetime;
		userBornColor = getContext().getResources().getColor(R.color.user_born);
		userAliveColor = getContext().getResources().getColor(R.color.user_alive);
		userDeadColor = getContext().getResources().getColor(R.color.user_dead);
		colors = new int[]{userBornColor, userAliveColor, userAliveColor, userDeadColor};

		borderPaint = new Paint();
		borderPaint.setColor(Color.BLACK);
		borderPaint.setStrokeWidth(1);
		borderPaint.setStyle(Paint.Style.STROKE);

		fillPaint = new Paint();
		fillPaint.setColor(userBornColor);
		fillPaint.setStyle(Paint.Style.FILL);

		Resources r = getResources();
		circlePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, r.getDisplayMetrics());
		padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, r.getDisplayMetrics());



		ArgbEvaluator evaluator = new ArgbEvaluator();
		ObjectAnimator animator = ObjectAnimator.ofObject(this, "color", evaluator, userBornColor, userAliveColor, userAliveColor, userDeadColor);
		animator.setDuration(lifetime).setRepeatCount(ValueAnimator.INFINITE);
		animator.start();
	}

	public void setColor(int color) {
		//fillPaint.setColor(color);
		if (gradient != null) {
			colors[0] = color;
		}
		invalidate();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		width = getWidth();
		height = getHeight();

		//if (gradient == null) {
			gradient = new LinearGradient(
					0, height / 2, width - 1, height / 2,
					colors, null, Shader.TileMode.REPEAT);
			fillPaint.setShader(gradient);
		//}

		//canvas.drawBitmap(bitmap, 0, 0, borderPaint);
		//float r = Math.min(Math.min(circlePx, height / 2 - 3), width / 2 - 3);
		//canvas.drawCircle(width / 2 - 3, height / 2 - 3, r, fillPaint);

		canvas.drawRect(padding, padding, width - 1 - padding, height - 1 - padding, fillPaint);

		canvas.drawRect(0, 0, width - 1, height - 1, borderPaint);
	}
}
