package com.letv.jr.home.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

/**
 * Utility class to create a split activity animation
 * 实现步骤：将activity的内容解析到bitmap 用自定义的MyImageiew分别绘制出bitmap内容的一部分，然后使用属性动画绘制出分割部分bitmap的动画过程
 * @author Udi Cohen (@udinic)
 */
public class ActivitySplitAnimationUtil {

    public static Bitmap mBitmap = null;
    private static int[] mLoc1;
    private static int[] mLoc2;
    private static ImageView mTopImage;
    private static ImageView mBottomImage;
    private static AnimatorSet mSetAnim;

    /**
     * Start a new Activity with a Split animation
     * 动画开启一个新的页面
     * @param currActivity The current Activity 当前页面
     * @param intent       The Intent needed tot start the new Activity 开启页面的intent
     * @param splitYCoord  The Y coordinate where we want to split the Activity on the animation. -1 will split the Activity equally
     * 					         动画分割的y坐标点，-1则从中间开始分割
     */
    public static void startActivity(Activity currActivity, Intent intent, int splitYCoord) {

        // Preparing the bitmaps that we need to show
        prepare(currActivity, splitYCoord);

        currActivity.startActivity(intent);
        currActivity.overridePendingTransition(0, 0);
    }

    /**
     * Start a new Activity with a Split animation right in the middle of the Activity
     * 启动一个新页面，从中间开启分割动画
     * @param currActivity The current Activity 当前页面
     * @param intent       The Intent needed tot start the new Activity 开启页面的intent
     */
    public static void startActivity(Activity currActivity, Intent intent) {
        startActivity(currActivity, intent, -1);
    }

    /**
     * Preparing the graphics on the destination Activity.
     * Should be called on the destination activity on Activity#onCreate() BEFORE setContentView()
     * 准备页面动画的数据计算工作，要在页面的onCreate方法中的setContentView()之前调用
     *
     * @param destActivity the destination Activity 开启页面的activity
     */
    public static void prepareAnimation(final Activity destActivity) {
        prepare(destActivity,-1);
        mTopImage = createImageView(destActivity, mBitmap, mLoc1);
        mBottomImage = createImageView(destActivity, mBitmap, mLoc2);
    }

    /**
     * Start the animation the reveals the destination Activity
     * Should be called on the destination activity on Activity#onCreate() AFTER setContentView()
     * 开启动画显示目标页面，在页面的onCreate方法中的setContentView()之后调用
     * 
     * @param destActivity the destination Activity 开启页面的activity
     * @param duration The duration of the animation 动画时间长度
     * @param interpolator The interpulator to use for the animation. null for no interpulation.
     * 		      动画的补间器
     */
    public static void animate(final Activity destActivity, final int duration, final TimeInterpolator interpolator) {

        // Post this on the UI thread's message queue. It's needed for the items to be already measured
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                mSetAnim = new AnimatorSet();
                mTopImage.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                mBottomImage.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                mSetAnim.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        clean(destActivity);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        clean(destActivity);
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });

                // Animating the 2 parts away from each other
                Animator anim1 = ObjectAnimator.ofFloat(mTopImage, "translationY", mTopImage.getHeight() * -1);
                Animator anim2 = ObjectAnimator.ofFloat(mBottomImage, "translationY", mBottomImage.getHeight());

                if (interpolator != null) {
                    anim1.setInterpolator(interpolator);
                    anim2.setInterpolator(interpolator);
                }

                mSetAnim.setDuration(duration);
                mSetAnim.playTogether(anim1, anim2);
                mSetAnim.start();
            }
        });
    }

    /**
     * Start the animation that reveals the destination Activity
     * Should be called on the destination activity on Activity#onCreate() AFTER setContentView()
     * 开启动画显示目标页面，在页面的onCreate方法中的setContentView()之后调用
     *
     * @param destActivity the destination Activity 开启页面的activity
     * @param duration The duration of the animation 动画时间长度
     */
    public static void animate(final Activity destActivity, final int duration) {
        animate(destActivity, duration, new DecelerateInterpolator());
    }

    /**
     * Cancel an in progress animation
     * 取消一个动画
     */
    public static void cancel() {
        if (mSetAnim != null)
            mSetAnim.cancel();
    }

    /**
     * Clean stuff
     *
     * @param activity The Activity where the animation is occurring
     */
    private static void clean(Activity activity) {
        if (mTopImage != null) {
            mTopImage.setLayerType(View.LAYER_TYPE_NONE, null);
            try {
                // If we use the regular removeView() we'll get a small UI glitch
                activity.getWindowManager().removeViewImmediate(mBottomImage);
            } catch (Exception ignored) {
            }
        }
        if (mBottomImage != null) {
            mBottomImage.setLayerType(View.LAYER_TYPE_NONE, null);
            try {
                activity.getWindowManager().removeViewImmediate(mTopImage);
            } catch (Exception ignored) {
            }
        }

        mBitmap = null;
    }

    /**
     * Preparing the graphics for the animation
     * 动画开启前的绘制数据计算
     * @param currActivity the current Activity from where we start the new one 开启新页面的activity
     * @param splitYCoord  The Y coordinate where we want to split the activity. -1 will split the activity equally y坐标分割点位置，-1则从中间均分
     */
    private static void prepare(Activity currActivity, int splitYCoord) {

        // Get the content of the activity and put in a bitmap
    	//获取一个activity的内容并把它放到一个bitmap中
        View root = currActivity.getWindow().getDecorView().findViewById(android.R.id.content);
        root.setDrawingCacheEnabled(true);
        mBitmap = root.getDrawingCache();

        // If the split Y coordinate is -1 - We'll split the activity equally 
        //如果y坐标为-1，从中间均分
        splitYCoord = (splitYCoord != -1 ? splitYCoord : mBitmap.getHeight() / 2);

        if (splitYCoord > mBitmap.getHeight())
            throw new IllegalArgumentException("Split Y coordinate [" + splitYCoord + "] exceeds the activity's height [" + mBitmap.getHeight() + "]");

        // Set the location to put the 2 bitmaps on the destination activity  
        //为bitmap设置在页面上显示的位置坐标
        mLoc1 = new int[]{0, splitYCoord, root.getTop()};
        mLoc2 = new int[]{splitYCoord, mBitmap.getHeight(), root.getTop()};
    }

    /**
     * Creating the an image, containing one part of the animation on the destination activity
     * 创造一个包含部分页面内容的image
     * @param destActivity The destination activity 目标页面
     * @param bmp          The Bitmap of the part we want to add to the destination activity  要被加到目标页面的bitmap
     * @param loc          The location this part should be on the screen     bitmap要被显示到页面上的位置
     * @return
     */
    private static ImageView createImageView(Activity destActivity, Bitmap bmp, int loc[]) {
    	MyImageView imageView = new MyImageView(destActivity);
        imageView.setImageBitmap(bmp);
        imageView.setImageOffsets(bmp.getWidth(), loc[0], loc[1]);                     
        
        WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
        windowParams.gravity = Gravity.TOP;
        windowParams.x = 0;
        windowParams.y = loc[2] + loc[0];
        windowParams.height = loc[1] - loc[0];
        windowParams.width = bmp.getWidth();
        windowParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        windowParams.format = PixelFormat.TRANSLUCENT;
        windowParams.windowAnimations = 0;
        destActivity.getWindowManager().addView(imageView, windowParams);

        return imageView;
    }
    
    /**
     * MyImageView
     * Extended ImageView that draws just part of an image, base on start/end position  
     * 自定义imageview，可根据设定的开始和结束坐标位置画出图片的一部分
     */
    private static class MyImageView extends ImageView
    {
    	private Rect mSrcRect;
    	private Rect mDstRect;
    	private Paint mPaint;    	
    	
		public MyImageView(Context context) 
		{
			super(context);
			mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		}
		
		/**
	     * Setting the bitmap offests to control the visible area
	     * 设置宽度和坐标点来控制要切割的可见区域
	     *
	     * @param width		   The bitmap image
	     * @param bmp          The start Y position
	     * @param loc          The end Y position
	     * @return
	     */
		public void setImageOffsets(int width, int startY, int endY)
		{
			mSrcRect = new Rect(0, startY, width, endY);
			mDstRect = new Rect(0, 0, width, endY - startY);
		}
				
		@Override
		protected void onDraw(Canvas canvas)
		{
			Bitmap bm = null;
			Drawable drawable = getDrawable();
			if (null != drawable && drawable instanceof BitmapDrawable)
			{
				bm = ((BitmapDrawable)drawable).getBitmap();
			}
			
			if (null == bm)
			{
				super.onDraw(canvas);
			}
			else
			{
				canvas.drawBitmap(bm, mSrcRect, mDstRect, mPaint);
			}
		}    	
    }
}
