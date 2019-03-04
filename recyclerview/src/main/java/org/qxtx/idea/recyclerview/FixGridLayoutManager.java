/**
 * 1、希望增加翻页功能，但失败了。。。。。。。
 * 2、准备加入为分页准备的焦点到达底部会自动翻页的功能
 * 3、重写了处理定位滚动item后没有主动申请焦点的可能性问题
 */

package org.qxtx.idea.recyclerview;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * 作者：laiyx
 * 日期：2018/1/8
 * 待修改
 * 描述1：重写LayoutManager，手动处理recyclerView中的焦点移动
 * 描述2：用于解决长按方向键导致焦点丢失的问题
 * 描述3：焦点丢失是因为焦点要移动到不可见的item上，无法实现移动动作，故丢失焦点
 * 描述4：网上偶然看到RecyclerView可能会发生定位滚动到某个item时并没有主动获取焦点，已经提供解决逻辑，但没测试
 */
public class FixGridLayoutManager extends android.support.v7.widget.GridLayoutManager {
    private final String TAG = "FixGridLayoutManager";

    private long keyDuration= 0;
    private RecyclerView recyclerView = null;

    private boolean canScrollV = true;
    private boolean canScrollH = true;

    public FixGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }

    //额外的recyclverView参数用于实现右边缘继续往右可以来到下一行
    public FixGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout, RecyclerView recyclerView) {
        super(context, spanCount, orientation, reverseLayout);
        this.recyclerView = recyclerView;
    }

    public FixGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        Properties properties = getProperties(context, attrs, defStyleAttr, defStyleRes);
        this.setSpanCount(properties.spanCount);
    }

    public FixGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    /**
     * 1、焦点移动逻辑
     * <p>
     * 作者：laiyx
     * 日期：2018/1/8
     * 描述：为了彻底消除长按下方向由于没找到view导致焦点跳走的问题，将会逐行item滚动，已经无法快速滚动
     *
     * @param focused   处于焦点的view
     * @param direction 焦点的逻辑运动
     */
    @Override
    public View onInterceptFocusSearch(View focused, int direction) {
        int currentItemPos = -1;
        if (focused.getParent() instanceof RecyclerView) {
            //Log.e(TAG, "属于RecyclerView");
            currentItemPos = this.getPosition(focused);//得到当前焦点的位置
        } else {
            Log.e(TAG, "不是RecyclerView");
            return null;
        }

//        if (direction == View.FOCUS_RIGHT) {
//            boolean isNeedEnter = (currentItemPos != getItemCount() - 1)
//                    && (currentItemPos % getSpanCount() == (getSpanCount() - 1))
//                    && (System.currentTimeMillis() - keyDuration > 500);
//            if (isNeedEnter) {
//                //LogUtils.e("处于右边缘");
//                currentItemPos = getPosition(focused) + 1;
//                if (getChildAt(currentItemPos) != null) {
//                    getChildAt(currentItemPos).requestFocus();
//                    keyDuration = System.currentTimeMillis();
//                    return null;
//                } else if ((getChildAt(currentItemPos) == null) && (recyclerView != null)) {
//                    recyclerView.smoothScrollToPosition(currentItemPos);
//                    keyDuration = System.currentTimeMillis();
//                    return null;
//                }
//            }
//        }
        //禁止快速地向下飞滚
         if (direction == View.FOCUS_DOWN) {
            if (currentItemPos > findLastCompletelyVisibleItemPosition()) {
                //Log.e(TAG, "超过可显示的item");
                return focused;
            }
        }

        return super.onInterceptFocusSearch(focused, direction);
    }

    /**
     * 2、希望整理滚动列表的逻辑 (这破方法只为scrollToPosition()方法服务)
     * <p>
     * 作者：laiyx
     * 日期：2018/01/13
     * 描述：里面的代码其实都是内置的，只是因为【重写了LinearSmoothScroller抽象类】，才要把其它代码也写一遍
     * 备注1：位于LinearLayoutManager中的方法，RecyclerView中的滚动逻辑也是【最终会来到这个方法】
     * 备注2：里面实现了LinearSmoothScroller抽象类，如果需要重写滚动逻辑则需要重写此方法里的抽象类
     *
     * @param recyclerView  RecyclerView自身
     * @param state         状态值（暂不明确）
     * @param posWithScroll 滚动的item的位置
     */
    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int posWithScroll) {
        FixNotFocusScroller fixNotFocusScroller = new FixNotFocusScroller(recyclerView.getContext()) {
            @Override
            public PointF computeScrollVectorForPosition(int targePos) {
                return super.computeScrollVectorForPosition(targePos);
            }
        };
        fixNotFocusScroller.setTargetPosition(posWithScroll); //这是设置了要滚动的位置
        startSmoothScroll(fixNotFocusScroller); //这是开始滚动的
    }

    //重写滚动许可检查
    @Override
    public boolean canScrollVertically() {
        return canScrollV && super.canScrollVertically();
    }
    @Override
    public boolean canScrollHorizontally() {
        return canScrollH && super.canScrollHorizontally();
    }

    //设置滚动许可信息
    public void setScrollVEnable(boolean canScrollV) {
        this.canScrollV = canScrollV;
    }
    public void setScrollHEnable(boolean canScrollH) {
        this.canScrollH = canScrollH;
    }

    //获取滚动许可信息
    public boolean getScrollVEnable() {
        return canScrollV;
    }
    public boolean getScrollHEnable() {
        return canScrollH;
    }

    /**
     * 为SmoothScrollToPosition这个方法服务的抽象类
     * <p>
     * 作者：laiyx
     * 日期：2018/01/13
     * 描述：希望整理滚动列表的逻辑
     * <p>
     * 希望解决定位到某个位置后但却没有获取焦点的问题
     * 滚动结束后主动申请焦点
     */
    abstract class FixNotFocusScroller extends LinearSmoothScroller {
        public FixNotFocusScroller(Context context) {
            super(context);
        }

        @Override
        public void onStart() {
            //Log.e(TAG, "滑动要开始了，回调进入了SmoothScroller抽象类的onStart()方法");
            super.onStart();
        }

        //这里是已经滑动完成了，回调onStop方法
        @Override
        public void onStop() {
            //Log.e(TAG, "滑动完成了，进入了SmoothScroller抽象类的onStop()方法");
            View targeView = findViewByPosition(getTargetPosition());
            if (targeView != null) {
                targeView.requestFocus();
            }

            super.onStop();
        }
    }
}
