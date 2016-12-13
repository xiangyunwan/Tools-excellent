package com.letv.jr.demanddeposit.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.letv.jr.R;

/**
 * 
 * @author zhangzhenzhong
 * 
 */
public class CircleView extends View {
	/**
	 * 画笔对象的引用
	 */
	private Paint paint;

	private String color;

	public CircleView(Context context) {
		this(context, null);
	}

	public CircleView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CircleView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		paint = new Paint();
		paint.setAntiAlias(true); // 消除锯齿
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// 画圆圈
		float strockWidth = getResources().getDimension(
				R.dimen.circle_divider);
		float centre = getWidth() / 2; // 圆心x坐标
		float radius = centre - strockWidth / 2; // 圆半径
		paint.setStyle(Paint.Style.STROKE); // 设置空心
		paint.setStrokeWidth(strockWidth); // 设置圆环的宽度
		paint.setColor(TextUtils.isEmpty(color) ? Color.parseColor("#999999")
				: Color.parseColor(color));
		canvas.drawCircle(centre, centre, radius, paint); // 画出圆环
	}

	/**
	 * 圆圈组件更新颜色
	 */
	public void updateDateView(String color) {
		this.color = color;
		postInvalidate();

	}
}
