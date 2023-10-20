package com.example.recyclerviewcustom;

import android.graphics.PointF;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

public class CustomSnapHelper extends SnapHelper {
    private RecyclerView mRecyclerView;
    @Override
    public void attachToRecyclerView(@Nullable RecyclerView recyclerView) throws IllegalStateException {
        this.mRecyclerView = recyclerView;
        super.attachToRecyclerView(recyclerView);
    }

    /**
     * 计算滚动到targetView需要滚动的距离
     * @param layoutManager
     * @param targetView
     * @return
     */
    @Nullable
    @Override
    public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager, @NonNull View targetView) {
        Log.d("ccm", "calculateDistanceToFinalSnap");
        if (layoutManager instanceof CustomLayoutManager) {
            CustomLayoutManager customLayoutManager = (CustomLayoutManager) layoutManager;
            int position = customLayoutManager.getPosition(targetView); //得到targetView对应的position
            PointF pointF = customLayoutManager.computeScrollForPosition(position); //计算需要滚动距离
            int[] a = new int[2];
            a[0] = Math.round(pointF.x);
            a[1] = Math.round(pointF.y);
            return a;
        } else {
            int[] a = new int[2];
            a[0] = 0;
            a[1] = 0;
            Log.d("ccm", "calculateDistanceToFinalSnap");
            return a;
        }
    }

    /**
     * 计算出当前页面需要对齐的Item
     * @param layoutManager
     * @return
     */
    @Nullable
    @Override
    public View findSnapView(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager instanceof CustomLayoutManager) {
            int childCount = layoutManager.getChildCount();
            if (childCount <= 0) return null;
            //获取当前屏幕上最小的Item
            int minItem = 9999;
            for (int i = 0; i < childCount; i++) {
                View child = layoutManager.getChildAt(i);
                int decoratedRight = layoutManager.getDecoratedRight(child);
                int decoratedLeft = layoutManager.getDecoratedLeft(child);
                if (decoratedRight > layoutManager.getPaddingLeft() && decoratedLeft < layoutManager.getWidth() - layoutManager.getPaddingRight()) {
                    int position = layoutManager.getPosition(child);
                    minItem = Math.min(minItem, position);
                }
            }


            if (minItem == 9999) return null;
            CustomLayoutManager customLayoutManager = (CustomLayoutManager) layoutManager;
            int tarPage = customLayoutManager.getTarPage(minItem);//计算出minItem对应的page
            int maxPage = customLayoutManager.getMaxPage(); //获取总的page数量
            int pageFistPosition = customLayoutManager.getPageFistPosition(tarPage); //获取当前page的第一个Item
            if (tarPage == maxPage) { //如果当前pege已经是最后一页了，返回当前page的第一个Item
                return customLayoutManager.findViewByPosition(pageFistPosition);
            }
            int nextPageFistPosition = customLayoutManager.getPageFistPosition(tarPage + 1); //获取下一个page的第一个Item
            //得到当前page的第一个Item和下一个page的第一个Item 对应的位子数据
            Rect lastRect = customLayoutManager.getRectByPosition(pageFistPosition);
            if (lastRect == null) return null;
            Rect thisRect = customLayoutManager.getRectByPosition(nextPageFistPosition);
            if (lastRect == null) return null;

            int xOffset = customLayoutManager.getxOffset(); //拿到偏移量
            if ((xOffset - lastRect.left) > (thisRect.left - xOffset)) { //哪个page距离屏幕边缘近，滚动到该page
                return customLayoutManager.findViewByPosition(nextPageFistPosition);
            } else {
                return customLayoutManager.findViewByPosition(pageFistPosition);
            }
        }
        return null;
    }

    /**
     * 根据速度（velocityX）计算需要滚动到哪个Item
     * @param layoutManager
     * @param velocityX
     * @param velocityY
     * @return 目标Item对应的position
     */
    @Override
    public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
        Log.d("ccm", "findTargetSnapPosition");
        if (layoutManager instanceof CustomLayoutManager) {
//            Log.d("ccm","findTargetSnapPosition");
            int childCount = layoutManager.getChildCount();
            if (childCount <= 0) return RecyclerView.NO_POSITION;
            //获取当前屏幕上最小的Item
            int minItem = 9999;
            for (int i = 0 ; i < childCount; i++) {
                View child = layoutManager.getChildAt(i);
                int decoratedRight = layoutManager.getDecoratedRight(child);
                int decoratedLeft = layoutManager.getDecoratedLeft(child);
                if (decoratedRight > layoutManager.getPaddingLeft() && decoratedLeft < layoutManager.getWidth() - layoutManager.getPaddingRight()) {
                    int position = layoutManager.getPosition(child);
                    minItem = Math.min(minItem, position);
                }
            }
            if (minItem == 9999) return RecyclerView.NO_POSITION;

            CustomLayoutManager customLayoutManager = (CustomLayoutManager) layoutManager;
            int tarPage = customLayoutManager.getTarPage(minItem); //计算出minItem对应的page
            int maxPage = customLayoutManager.getMaxPage(); //获取总的page数量
            if (velocityX > 0) {//左滑
                if (tarPage < maxPage) {
                    //未到最后一页，滚动到下一页的第一个Item
                    return customLayoutManager.getPageFistPosition(tarPage + 1);
                } else {
                    //已经到最后一页了，滚动到当前页的第一个Item
                    return customLayoutManager.getPageFistPosition(maxPage);
                }
            } else {
                if (tarPage <= 1) {
                    //未到第一页，滚动到上一个页的第一个Item
                    return customLayoutManager.getPageFistPosition(1);
                } else {
                    //已经到第一页了，滚动到第一页的第一个Iitem
                    return customLayoutManager.getPageFistPosition(tarPage);
                }
            }
        } else {
            return RecyclerView.NO_POSITION;
        }
    }
    @Nullable
    @Override
    protected RecyclerView.SmoothScroller createScroller(RecyclerView.LayoutManager layoutManager) {
        return !(layoutManager instanceof RecyclerView.SmoothScroller.ScrollVectorProvider) ? null : new LinearSmoothScroller(mRecyclerView.getContext()) {
            protected void onTargetFound(View targetView, RecyclerView.State state, Action action) {
                int[] snapDistances = calculateDistanceToFinalSnap(layoutManager, targetView);
                int dx = snapDistances[0];
                int dy = snapDistances[1];
                int time = this.calculateTimeForDeceleration(Math.max(Math.abs(dx), Math.abs(dy)));
                if (time > 0) {
                    action.update(dx, dy, time, this.mDecelerateInterpolator);
                }

            }

            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return 25.0f / (float)displayMetrics.densityDpi;
            }

            protected int calculateTimeForScrolling(int dx) {
                return Math.min(100, super.calculateTimeForScrolling(dx));
            }

        };
    }
}
