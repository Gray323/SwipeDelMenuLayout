package com.example.gray.swipedelmenulayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

/**
 * Created by Gray on 2017/11/21.
 */

public class SwipeDelMenuLayout extends ViewGroup{

    private int mScaleTouchSlop;//为了处理单击事件的冲突
    private int mMaxVelocity;//计算滑动速度用
    private int mPointerId;//多点触摸只计算第一根手指的速度
    private int mHeight;//自己的高度
    private int mRightMenuWidths;//右侧菜单宽度总和（最大滑动距离）
    private int mLimit;//滑动判定临界值（右侧菜单宽度的40%）手指抬起时，超过了展开，没超过收起menu
    private View mContentView;//存储contentView(第一个view)
    private PointF mLastP = new PointF();//上一次的xy
    //仿QQ，侧滑菜单展开时，点击除侧滑菜单之外的区域，关闭侧滑菜单。
    //增加一个布尔值变量，dispatch函数里，每次down时，为true，move时判断，如果是滑动动作，设为false。
    //在Intercept函数的up时，判断这个变量，如果仍为true 说明是点击事件，则关闭菜单。
    private boolean isUnMoved = true;
    //判断手指起始落点，如果距离属于滑动了，就屏蔽一切点击事件
    //up-down的坐标，判断是否属于滑动，如果是，则屏蔽一切点击事件
    private PointF mFirstP = new PointF();
    private boolean isUserSwiped;
    private static SwipeDelMenuLayout mViewCache;//存储的是当前正在展开的view
    private VelocityTracker mVelocityTracker;//滑动速度变量
    private boolean isSwipeEnabled;//右滑删除功能的开关，默认开
    private boolean isIos;//IOS,QQ交互，默认开
    private boolean iosInterceptFlag;//IOS类型下，是否拦截事件的flag
    private boolean isLeftSwipe;//左滑右滑的开关，默认左滑打开菜单

    public SwipeDelMenuLayout(Context context) {
        this(context, null);
    }

    public SwipeDelMenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeDelMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr){
        //获得触发移动事件的最短距离，如果小于这个距离就不触发移动控件
        mScaleTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        //获得允许执行一个fling手势动作的最大速度值
        mMaxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        isSwipeEnabled = true;//右滑删除功能的开关，默认开
        isIos = true;//IOS,QQ交互，默认开
        isLeftSwipe = true;//左滑右滑的开关，默认开
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SwipeDelMenuLayout, defStyleAttr, 0);
        int count = ta.getIndexCount();
        for (int i = 0; i < count; i++){
            int attr = ta.getIndex(i);
            if(attr == R.styleable.SwipeDelMenuLayout_swipeEnable){
                isSwipeEnabled = ta.getBoolean(attr, true);
            }else if (attr == R.styleable.SwipeDelMenuLayout_ios){
                isIos = ta.getBoolean(attr, true);
            }else if (attr == R.styleable.SwipeDelMenuLayout_leftSwipe){
                isLeftSwipe = ta.getBoolean(attr, true);
            }
        }
        ta.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        setClickable(true);//令自己可点击，从而获取触摸事件
        mRightMenuWidths = 0;//由于viewholder的复用机制，每次这里都要手动恢复默认值
        mHeight = 0;
        int contentWidth = 0;//适配GridLayoutManager,将以第一个子item的宽度为控件宽度
        int childCount = getChildCount();

        //为了子view的高，可以matchParent
        final boolean measureMatchParentChildren = MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY;
        boolean isNeedMeasureChildHeight = false;

        for (int i = 0; i < childCount; i++){
            View childView = getChildAt(i);
            //令每一个子view可点击，从而获取触摸事件
            childView.setClickable(true);
            if (childView.getVisibility() != GONE){
                measureChild(childView, widthMeasureSpec, heightMeasureSpec);
                final MarginLayoutParams lp = (MarginLayoutParams) childView.getLayoutParams();
                mHeight = Math.max(mHeight, childView.getMeasuredHeight());
                if (measureMatchParentChildren && lp.height == LayoutParams.MATCH_PARENT){
                    isNeedMeasureChildHeight = true;
                }
                //第一个是left item 第二个开始才是侧滑区域
                if (i > 0){
                    mRightMenuWidths += childView.getMeasuredWidth();
                }else{
                    mContentView = childView;
                    contentWidth = childView.getMeasuredWidth();
                }
            }
        }

        setMeasuredDimension(getPaddingLeft() + getPaddingRight() + contentWidth,
                mHeight + getPaddingTop() + getPaddingBottom());//宽度取第一个contentView的宽度
        mLimit = mRightMenuWidths * 4 / 10;//滑动判断的临界
        //如果子view的height有matchParent属性的，设置子view 高度
        if(isNeedMeasureChildHeight){
            //TODO
        }

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

}
