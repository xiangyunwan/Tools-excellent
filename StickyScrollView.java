package com.letv.jr.demanddeposit.view;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ScrollView;

import com.letv.jr.R;
import com.letv.jr.base.view.scrollview.ScrollViewCallBack;


/**
 * 可以设置悬浮view以及底部随着滚动隐藏view的scrollview，并拥有获取y坐标的接口
 * 
 * @author zhangzhenzhong
 * @version 1.0
 */
public class StickyScrollView extends ScrollView {

  /**
   * 悬浮view的偏移字段，通过设置scrollview的childview的tag,来确定其中某个view是否是悬浮的view
   */
  private static final String STICKY = "sticky";

  /**
   * 当前显示的悬浮view
   */
  private View mCurrentStickyView;

  /**
   * 阴影的drawable
   */
  private Drawable mShadowDrawable;

  /**
   * 悬浮view的列表
   */
  private List<View> mStickyViews;

  /**
   * 悬浮view的偏移量，主要用于存在多个悬浮view时，下个悬浮view顶到当前悬浮view（mCurrentStickyView）的时候，当前view上移的偏移量
   */
  private int mStickyViewTopOffset;

  /**
   * 默认阴影的高度
   */
  private int defaultShadowHeight = 10;

  /**
   * 屏幕密度
   */
  private float density;

  /**
   * 是否处于悬浮区域内，true处于悬浮区，false otherwise
   */
  private boolean redirectTouchToStickyView;

  /**
   * 悬浮view距离scrollview顶部的偏移距离
   */
  private int mHeaderOffset = 0;
  /**
   * 当点击Sticky的时候，实现某些背景的渐变
   */

  private ScrollViewCallBack mViewCallBack;


  private OnScrollToBottomListener onScrollToBottom;
  private Runnable mInvalidataRunnable = new Runnable() {

    @Override
    public void run() {
      if (mCurrentStickyView != null) {
        int left = mCurrentStickyView.getLeft();
        int top = mCurrentStickyView.getTop();
        int right = mCurrentStickyView.getRight();
        int bottom = getScrollY() + (mCurrentStickyView.getHeight() + mStickyViewTopOffset);
        // 直接调用invalidate()方法，请求重新draw()，但只会绘制调用者本身
        invalidate(left, top, right, bottom);
      }

      postDelayed(this, 16);

    }
  };

  public StickyScrollView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);

    init();
  }

  public StickyScrollView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    mShadowDrawable = context.getResources().getDrawable(R.drawable.sticky_shadow_default);
    mStickyViews = new LinkedList<View>();
    density = context.getResources().getDisplayMetrics().density;

    init();
  }

  /**
   * 张振中
   * @param scrollX
   * @param scrollY
   * @param clampedX
   * @param clampedY
     */
  @Override
  protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX,
                                boolean clampedY) {
    super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
    if(scrollY != 0 && null != onScrollToBottom){
      onScrollToBottom.onScrollBottomListener(clampedY);
    }
  }

  public void setOnScrollToBottomLintener(OnScrollToBottomListener listener){
    onScrollToBottom = listener;
  }

  public interface OnScrollToBottomListener{
    public void onScrollBottomListener(boolean isBottom);
  }
  /**
   * 找到设置tag的View
   * 
   * @param viewGroup
   */
  private void findViewByStickyTag(ViewGroup viewGroup) {
    int childCount = ((ViewGroup) viewGroup).getChildCount();
    for (int i = 0; i < childCount; i++) {
      View child = viewGroup.getChildAt(i);

      if (getStringTagForView(child).contains(STICKY)) {
        mStickyViews.add(child);
      }

      if (child instanceof ViewGroup) {
        findViewByStickyTag((ViewGroup) child);
      }
    }

  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);
    if (changed) {
      findViewByStickyTag((ViewGroup) getChildAt(0));
    }
    showStickyView();
  }

  @Override
  protected void onScrollChanged(int l, int t, int oldl, int oldt) {
    super.onScrollChanged(l, t, oldl, oldt);
    if (onScrollListener != null) {
      onScrollListener.onScroll(t);
    }
    if (mViewCallBack != null) {
      mViewCallBack.onScrollChanged(t);
    }
    showStickyView();
  }
  public void setOnScrollCallBack(ScrollViewCallBack callBack){
    mViewCallBack = callBack;
  }
  /**
   * 展示悬浮view
   */
  private void showStickyView() {
    View curStickyView = null;
    View nextStickyView = null;
    // 循环出添加的悬浮view列表
    for (View v : mStickyViews) {
      // 当前循环到的view顶部距离是scrollview顶部之间的距离
      int topOffset = v.getTop() - getScrollY();
      // 此判断即为当前循环的view是否在设置的偏移距离的上面，可以理解为如果偏移距离mHeaderOffset = 0的时候，v的top是否越过了scrollview的顶部
      if (topOffset <= mHeaderOffset) {
        // 是否存在悬浮的view或者当前悬浮的view是否在v的上面，是的话悬浮的view就改为v
        if (curStickyView == null || topOffset > curStickyView.getTop() - getScrollY()) {
          curStickyView = v;
        }
      } else {
        // 是否存在悬浮view下面的view或者当前悬浮view是否在v的上面，是的话v就作为当前悬浮view的下一个备胎
        if (nextStickyView == null || topOffset < nextStickyView.getTop() - getScrollY()) {
          nextStickyView = v;
        }
      }
    }

    if (curStickyView != null) {
      // 下一个备胎view是否顶到了当前悬浮view，如果顶到了，那么当前悬浮view需要上移mStickyViewTopOffset绝对值的距离，
      mStickyViewTopOffset =
          nextStickyView == null ? 0 : Math.min(0, nextStickyView.getTop() - getScrollY()
              - curStickyView.getHeight());
      mCurrentStickyView = curStickyView;
      // 调用绘制方法
      post(mInvalidataRunnable);
    } else {
      mCurrentStickyView = null;
      removeCallbacks(mInvalidataRunnable);

    }

  }

  private String getStringTagForView(View v) {
    Object tag = v.getTag();
    return String.valueOf(tag);
  }

  /**
   * 将sticky画出来
   */
  @Override
  protected void dispatchDraw(Canvas canvas) {
    super.dispatchDraw(canvas);
    if (mCurrentStickyView != null) {
      // 先保存起来
      canvas.save();
      // 将canvas的坐标原点移动到(0, getScrollY() + mStickyViewTopOffset+mHeaderOffset)
      canvas.translate(0, mHeaderOffset + getScrollY() + mStickyViewTopOffset);
      // 如果存在阴影的话将阴影画出来
      if (mShadowDrawable != null) {
        int left = 0;
        int top = mCurrentStickyView.getHeight() + mStickyViewTopOffset;
        int right = mCurrentStickyView.getWidth();
        int bottom = top + (int) (density * defaultShadowHeight + 0.5f);
        mShadowDrawable.setBounds(left, top, right, bottom);
        mShadowDrawable.draw(canvas);
      }

      canvas.clipRect(0, mStickyViewTopOffset, mCurrentStickyView.getWidth(),
          mCurrentStickyView.getHeight());

      mCurrentStickyView.draw(canvas);

      // 重置坐标原点参数
      canvas.restore();
    }
  }
  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
    // 是否按下
    if (ev.getAction() == MotionEvent.ACTION_DOWN) {
      redirectTouchToStickyView = true;
    }

    if (redirectTouchToStickyView) {
      redirectTouchToStickyView = mCurrentStickyView != null;

      if (redirectTouchToStickyView) {
        // 是否在悬浮view的区域内
        redirectTouchToStickyView =
            ev.getY() > mHeaderOffset
                && ev.getY() <= (mCurrentStickyView.getHeight() + mStickyViewTopOffset + mHeaderOffset)
                && ev.getX() >= mCurrentStickyView.getLeft()
                && ev.getX() <= mCurrentStickyView.getRight();
      }
    }

    if (redirectTouchToStickyView) {
      // 调整悬浮view的坐标，使能够触发对应位置的事件
      ev.offsetLocation(0, -1
          * ((getScrollY() + mStickyViewTopOffset + mHeaderOffset) - mCurrentStickyView.getTop()));
    }
    return super.dispatchTouchEvent(ev);
  }


  private boolean hasNotDoneActionDown = true;

  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    if (redirectTouchToStickyView) {
      // 调整悬浮view的坐标，使能够触发对应位置的事件
      ev.offsetLocation(0,
          ((getScrollY() + mStickyViewTopOffset) + mHeaderOffset - mCurrentStickyView.getTop()));
    }
//
//    if (ev.getAction() == MotionEvent.ACTION_DOWN) {
//      hasNotDoneActionDown = false;
//    }
//
//    if (hasNotDoneActionDown) {
//      MotionEvent down = MotionEvent.obtain(ev);
//      down.setAction(MotionEvent.ACTION_DOWN);
//      super.onTouchEvent(down);
//      hasNotDoneActionDown = false;
//    }
//
//    if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
//      hasNotDoneActionDown = true;
//    }
    return super.onTouchEvent(ev);
  }

  // ---------------------scrollMove的部分代码---start----------
  private static final long DELAY = 200;
  private int currentScroll;
  private Runnable scrollCheckTask;
  /* 隐藏 */
  private Animation hideAnimation;
  /* 出现 */
  private Animation showInAnimation;

  private View moveView;
  boolean isDown = false;
  boolean isShow = true;
  boolean first = true;
  private Handler mHandler = new Handler();

  private void setShowAndHide(boolean isShowAnimation) {
    if (moveView == null) {
      return;
    }

    if (isShowAnimation) {
      if (!isShow) {
        moveView.startAnimation(showInAnimation);
      }
    } else {
      if (isShow) {
        moveView.startAnimation(hideAnimation);
      }
    }
  }

  private void init() {
    setOverScrollMode(View.OVER_SCROLL_NEVER);

    hideAnimation =
        new TranslateAnimation(0, 0, 0, 0, Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, 1f);
    hideAnimation.setDuration(300);// 设置动画持续时间
    hideAnimation.setFillAfter(true);
    hideAnimation.setAnimationListener(new AnimationListener() {
      @Override
      public void onAnimationStart(Animation animation) {
        isShow = false;
      }

      @Override
      public void onAnimationRepeat(Animation animation) {}

      @Override
      public void onAnimationEnd(Animation animation) {}
    });

    showInAnimation =
        new TranslateAnimation(0, 0, 0, 0, Animation.RELATIVE_TO_SELF, 1f,
            Animation.RELATIVE_TO_SELF, 0f);
    showInAnimation.setDuration(300);// 设置动画持续时间
    showInAnimation.setFillAfter(true);
    showInAnimation.setAnimationListener(new AnimationListener() {
      @Override
      public void onAnimationStart(Animation animation) {
        isShow = true;
      }

      @Override
      public void onAnimationRepeat(Animation animation) {}

      @Override
      public void onAnimationEnd(Animation animation) {}
    });

    scrollCheckTask = new Runnable() {
      @Override
      public void run() {
        int newScroll = getScrollY();
        if (currentScroll == newScroll && !isDown) {
          if (moveView != null) {
            /* 出现 */
            setShowAndHide(true);
          }
        } else {
          currentScroll = getScrollY();
          mHandler.postDelayed(scrollCheckTask, DELAY);
        }
      }
    };
    setOnTouchListener(new OnTouchListener() {
      int y = 0;

      @Override
      public boolean onTouch(View v, MotionEvent event) {

        if (moveView != null) {
          if (event.getAction() == MotionEvent.ACTION_UP) {
            isDown = false;
            currentScroll = getScrollY();
            first = true;
            mHandler.postDelayed(scrollCheckTask, DELAY);
          } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            // 判断滑动长度，灵敏度为20，
            if (((event.getY() - y) >= 0 && (event.getY() - y) < 20) || ((event.getY() - y) > -20)
                && (event.getY() - y) < 0) {
              isDown = false;
            } else {
              isDown = true;
              setShowAndHide(false);
              mHandler.removeCallbacks(scrollCheckTask);
              if (first) {
                /* 隐藏 */
                // moveView.startAnimation(hideAnimation);
                first = false;
              }
            }

          } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
            y = (int) event.getY();
            // isDown = true;
            // /* 隐藏 */
            // // moveView.startAnimation(hideAnimation);
            // setShowAndHide(false);
            // mHandler.removeCallbacks(scrollCheckTask);
          }
        }

        return false;
      }
    });
  }

  /**
   * 设置能够随着scroll滑动而随之消失显示的view
   * 
   * @param view
   */
  public void setMoveView(View view) {
    moveView = view;
  }

  private float xDistance, yDistance, lastX, lastY;

  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {

    switch (ev.getAction()) {
      case MotionEvent.ACTION_DOWN:
        xDistance = yDistance = 0f;
        lastX = ev.getX();
        lastY = ev.getY();
        break;
      case MotionEvent.ACTION_MOVE:
        final float curX = ev.getX();
        final float curY = ev.getY();
        xDistance += Math.abs(curX - lastX);
        yDistance += Math.abs(curY - lastY);
        lastX = curX;
        lastY = curY;
        if (xDistance > yDistance) {
          return false;
        }
    }
    return super.onInterceptTouchEvent(ev);
  }

  // ---------------------scrollMove的部分代码---end----------

  /******************增加20150329(开始) ****************/
  /**
   * 设置滚动接口，返回滚动的y坐标
   * 
   * @param onScrollListener
   */
  public void setOnScrollListener(OnScrollListener onScrollListener) {
    this.onScrollListener = onScrollListener;
  }

  /**
   * 滚动的回调接口
   */
  public interface OnScrollListener {
    /**
     * 回调方法， 返回MyScrollView滑动的Y方向距离
     * 
     * @param scrollY
     */
    public void onScroll(int scrollY);
  }

  private OnScrollListener onScrollListener;

  /******************增加20150329(结束) ****************/

  /****************** 增加了头部偏移量 20160106(start) ****************/
  /**
   * adding the sticky view's offset of the scrollview which can change the distance between the top
   * of the scrollview and the sticky view dynamically. 增加顶部偏移量的方法，方便动态更改悬浮距离顶部的位置
   * 
   * @param mHeaderOffset 顶部偏移量
   */
  public void setHeaderOffset(int mHeaderOffset) {
    this.mHeaderOffset = mHeaderOffset;
  }
  /******************增加了头部偏移量 20160106(end) ****************/
  
  /******************增加了阴影属性 20160118(start) ****************/
  public void setShadowHeight(int mShadowHeight) {
    this.defaultShadowHeight = mShadowHeight;
  }
  
  /****************** 增加了阴影属性  20160118(end) ****************/
  
  /**
   * 是否达到吸顶坐标
   */
  public boolean isInStickyPosition (){
    int topOffset = mStickyViews.get(0).getTop() - getScrollY();
    if(topOffset <= mHeaderOffset){
      return true;
    }
    return false;
  }

}
