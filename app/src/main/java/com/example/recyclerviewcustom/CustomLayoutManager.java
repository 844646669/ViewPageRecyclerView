package com.example.recyclerviewcustom;

import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.LayoutParams;

import java.util.ArrayList;
import java.util.List;


public class CustomLayoutManager extends RecyclerView.LayoutManager implements RecyclerView.SmoothScroller.ScrollVectorProvider{

    public CustomLayoutManager(int line, int col) {
        this.line = line;
        this.col = col;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {

        return new RecyclerView.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
    }

    /**
     * 这个返回true可以自动根据所有Item的宽高，重新测试RecyclerView的宽高
     * @return
     */
    @Override
    public boolean isAutoMeasureEnabled() {
        return true;
    }


    int line = 2; //每一页line行
    int col = 4; //每一行 col列
    //每个item对应的位置信息
    private List<Rect> rectFList = new ArrayList<>();
    int mXOffset = 0; //当前滑动偏移量（scrollHorizontallyBy方法中记录的偏移量）
    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler); //把Item从界面上剥离下来
        int itemCount = getItemCount(); //获取item数量
        if (itemCount <= 0) {
            return;
        }
        View fistView = recycler.getViewForPosition(0);//获取第一个item
        int width = getWidth();//获取RecyclerView的宽度
        //这里考虑到RecyclerView可能有内边距，所以实际可用宽度应该是减去左右内边距
        int canUseWidth = width - getPaddingLeft() - getPaddingRight();
        /**
         * measureChildWithMargins方法的后两个参数是宽度和高度的使用量，
         * 这里每一行是有4列，所以宽度的使用量应该是3/4 canUseWidth
         */
        int usedWidth = Math.round(canUseWidth * ((col - 1f)/col));
        int usedHeight = 0;//高度不受限制
        //测量第一个Item，这里只测量第一个item，是默认所有的Item大小都是一样的
        measureChildWithMargins(fistView,usedWidth, usedHeight);
        //获取测量后Item的宽高
        int measureWidth = getDecoratedMeasuredWidth(fistView);
        int measureHeight = getDecoratedMeasuredHeight(fistView);
        int colWidth = Math.round((canUseWidth + 0f) / col); //计算出每一列的宽度

        //计算所有Item的位置情况
        rectFList.clear();
        for (int i = 0; i < itemCount; i++) {
            int tarPage = getTarPage(i); //获取第i个Item对应的页码（1、2、3）
            int tarLine = getTarLine(i); //获取对应的行码 （0、1、2、3）
            int tarCol = getTarCol(i); //获取对应的列码（0、1、2、3）
            //计算第tarPage页的偏移量 （每页宽度 * 页码） +  左边内边距
            int xOffset = canUseWidth * (tarPage - 1) + getPaddingLeft();
            //计算 第 i个Item的 上下左右位置
            int left = colWidth * tarCol + xOffset;
            int top = measureHeight * tarLine;
            int right = left + colWidth;
            int button = top + measureHeight;
            Rect rect = new Rect(left, top,right, button);
            rectFList.add(rect);
        }

        //计算当前界面上显示对应的区域
        int pageStartX = mXOffset + getPaddingLeft();
        int pageEndX = pageStartX + canUseWidth;
        //缓存当前界面相邻一个page范围
        pageStartX = pageStartX - canUseWidth;
        pageEndX = pageEndX + canUseWidth;


        //将所有的Item添加到RecyclerView中并设置对应的位置
        for (int i = 0; i < itemCount; i++) {
            Rect rect = rectFList.get(i);
            //判断Item是否处于pageStartX到pageEndX区间内，如果是就把对应的Item添加到RecyclerView中去
            if (rect.right > pageStartX && rect.left < pageEndX) {
                View view = recycler.getViewForPosition(i); //回收池中获取Item
                measureChildWithMargins(view, usedWidth, usedHeight);
                int viewWidth = getDecoratedMeasuredWidth(view);
                int viewHeight = getDecoratedMeasuredHeight(view);
                addView(view);//添加到RecyclerView中去
                //设置对应Item在RecyclerView中的位置
                int left = rect.left - mXOffset - getPaddingLeft(); //这里需要减去偏移量才是当前Item的位置
                int top = rect.top;
                int right = left + viewWidth;
                int button = top + viewHeight;
                Log.d("ccm" + i,String.format("left %d, right %d", left,right));
                layoutDecorated(view, left, top, right, button);
            }
        }


    }

    /**
     * 获取第position个Item在其所在Page对应的行数(0、1、... (line -1))
     * @param position
     * @return
     */
    private int getTarLine(int position) {
        int line = (int) Math.ceil((position + 1f) / this.col);
        line = (line - 1)%this.line;
        return (int) line;
    }

    /**
     * 获取第position个Item在其所在Page对应的列数(0、1、... (col -1))
     * @param position
     * @return
     */
    private int getTarCol(int position) {
        int col = (int) Math.ceil((position) % this.col);
        return col;
    }

    /**
     * 获取第position个Item在所在的page（1、2、3、4.。。）
     * @param position
     * @return
     */
    public int getTarPage(int position) {
        int onePageCount = line * col;
        return (int) Math.ceil((position + 1f) / onePageCount);
    }


    /**
     * 需要支持左右滑动，所以这里需要返回true
     * @return
     */
    @Override
    public boolean canScrollHorizontally() {
        return true;
    }



    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int itemCount = getItemCount(); //获取item数量
        if (itemCount == 0) return 0;
        //获取最后一个item对应的page值就等于获取到总的page数量
        int pageCount = getTarPage(itemCount - 1);
        int width = getWidth();//获取RecyclerView的宽度
        //这里考虑到RecyclerView可能有内边距，所以实际可用宽度应该是减去左右内边距
        int canUseWidth = width - getPaddingLeft() - getPaddingRight();
        int minOffset = 0;//最小滑动值是0
        //每页宽度 * （页面数量 - 1） 得到最大滑动距离 （只有1页的时候是不能滑动的）
        int maxOffset = canUseWidth * (pageCount - 1);
        int realDx = dx; //真实滑动距离
        if (mXOffset + dx < minOffset) { //如果滑动距离小于最小值，则修正真实滑动距离
            realDx = 0 - mXOffset;
        }
        if (mXOffset + dx > maxOffset) { //如果滑动距离小于最大值，则修正真实滑动距离
            realDx = maxOffset - mXOffset;
        }



        //根据偏移量计算当前显示区域
        int pageStartX = mXOffset + getPaddingLeft();
        int pageEndX = pageStartX + canUseWidth;
        //根据realDx计算移动后的显示区域
        pageStartX += realDx;
        pageEndX += realDx;
        //缓存界面相邻一个page范围
        pageStartX -= canUseWidth;
        pageEndX += canUseWidth;

        //遍历已添加到RecyclerView上的Item，判断是否处于pageStartX到pageEndX范围内，如果不是
        //给他移除掉添加到回收池中去
        int childCount = getChildCount();//添加到界面上的Item数量
        List<View> needRemoveView = new ArrayList<>(2);
        for (int i = 0; i < childCount; i++) {
            View item = getChildAt(i);
            int position = getPosition(item);
            Rect rect = rectFList.get(position);
            if (!(rect.right > pageStartX && rect.left < pageEndX)) {
                //不处于pageStartX到pageEndX范围内的Item需要移除
                needRemoveView.add(item);
            }
        }
        for (View item : needRemoveView) {
            removeAndRecycleView(item, recycler); //移除掉
        }

        //添加Item
        int usedWidth = Math.round(canUseWidth * ((col - 1f)/col));
        int usedHeight = 0;//高度不受限制
        for (int i = 0; i < itemCount; i++) {
            Rect rect = rectFList.get(i);
            //判断Item是否处于pageStartX到pageEndX区间内，如果是就把对应的Item添加到RecyclerView中去
            if (rect.right > pageStartX && rect.left < pageEndX) {
                View view = null;
                //查找该Item是否已经在界面上了
                view = findViewByPosition(i);
                if (view == null) { //界面上查找不到，说明需要添加
                    view = recycler.getViewForPosition(i); //回收池中获取Item
                    measureChildWithMargins(view, usedWidth, usedHeight);
                    int viewWidth = getDecoratedMeasuredWidth(view);
                    int viewHeight = getDecoratedMeasuredHeight(view);
                    addView(view);//添加到RecyclerView中去
                    //设置对应Item在RecyclerView中的位置
                    int left = rect.left - mXOffset - getPaddingLeft(); //这里需要减去偏移量才是当前Item的位置
                    int top = rect.top;
                    int right = left + viewWidth;
                    int button = top + viewHeight;
                    layoutDecorated(view, left, top, right, button);
                }
            }
        }

        //计算新的mXOffset
        mXOffset = mXOffset + realDx;
        //执行滑动操作
        offsetChildrenHorizontal(-realDx);


        //返回值是本次滑动的真实距离，RecyclerView是支持嵌套滚动的，这里涉及到嵌套滚动的内容就不做赘述了
        return realDx;
    }


    @Nullable
    @Override
    public PointF computeScrollVectorForPosition(int targetPosition) {
        if (getChildCount() == 0) {
            return null;
        }
        Rect rect = rectFList.get(targetPosition);
        int offset = rect.left - mXOffset;
        PointF pointF = new PointF(offset, 0);
        return pointF;
    }

    /**
     * 计算滚动到targetPosition需要滚动的距离
     * @param targetPosition
     * @return
     */
    public PointF computeScrollForPosition(int targetPosition) {
        if (getChildCount() == 0) {
            return null;
        }

        Rect rect = rectFList.get(targetPosition);
        int offset = rect.left - mXOffset;
        PointF pointF = new PointF(offset, 0);
        return pointF;
    }

    /**
     * 获取最多页数
     * @return
     */
    public int getMaxPage() {
        int onePageCount = line * col;
        return (int) Math.ceil(getItemCount()/(onePageCount + 0f));
    }


    /**
     * 获取第page页第一个item的index
     * @param page
     * @return
     */
    public int getPageFistPosition(int page) {
        return line * col * (page - 1);
    }


    /**
     * 根据position获取对应的Rect
     * @param position
     * @return
     */
    public Rect getRectByPosition(int position) {
        if (rectFList == null || rectFList.size() <= position) return null;
        return rectFList.get(position);
    }




    public int getxOffset() {
        return mXOffset;
    }
}
