/**
 *
 * 一些开源的RecyclerView的LayoutManager
 * 1、FanLayoutManaget：https://github.com/Cleveroad/FanLayoutManager 扇叶转动
 * 2、CarouselLayoutManager：https://github.com/Azoft/CarouselLayoutManager 传送带（卡片轮播）效果
 * 3、ChipsLayoutManager：https://github.com/BelooS/ChipsLayoutManager 流式布局效果（标签云）
 * 4、HiveLayoutManager：https://github.com/Chacojack/HiveLayoutManager 蜂巢效果（国人作品）
 * 5、vLayout：https://github.com/alibaba/vlayout 布局混排效果（天猫app所使用）
 * 6、flexbox-layout https://github.com/google/flexbox-layout flexbox效果（谷歌的东西，原本不支持recyclerView）
 * 7、LondonEyeLayoutManager https://github.com/danylovolokh/LondonEyeLayoutManager 环形菜单效果
 */

package org.qxtx.idea.recyclerview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for recyclerView. It can put a view list with item of multi style, and the {@link MyHolder} can
 * set item easily is the adapter have. Other hands, it provide interface {@link MultiLayout} and a default
 * item Decoration {@link DefaultItemDecoration}. You can change item style in any time but it may not be a good
 * idea to do that.
 * @param <T>   A type convert to adapter.
 */
public abstract class IdeaAdapter<T> extends RecyclerView.Adapter<IdeaAdapter<T>.MyHolder> {
    private static final String TAG = "IdeaAdapter";

    private final WeakReference<Context> context;
    private List<T> data;
    private int layoutId;
    private MultiLayout multiLayout;

    abstract void onBind(MyHolder viewHolder, int pos, List<T> data);

    public IdeaAdapter(Context context) {
        this(context, -1, null);
    }

    public IdeaAdapter(Context context, int layoutId, List<T> data) {
        this.context = new WeakReference<>(context);
        this.layoutId = layoutId;

        if (data == null) {
            this.data = new ArrayList<>();
        } else {
            this.data = data;
        }
    }

    public IdeaAdapter(Context context, MultiLayout multiLayout, List<T> data) {
        this.context = new WeakReference<>(context);
        this.multiLayout = multiLayout;

        if (data == null) {
            this.data = new ArrayList<>();
        } else {
            this.data = data;
        }
    }

    /**
     * Return a item id from the position on the item list while {@link #multiLayout} is not null,
     * and return {@link #layoutId} when it was reValue or return 0 with others.
     * @param position position for a item
     * @return item id for the item
     */
    @Override
    public int getItemViewType(int position) {
        if (multiLayout != null) {
            return multiLayout.getLayoutId(position);
        } else if (layoutId != -1) {
            return layoutId;
        }

        return super.getItemViewType(position);
    }

    /**
     * It will call {@link #getItemViewType(int)} to get a viewType.
     * @param viewType  It was result from {@link #getItemViewType(int)}
     */
    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (multiLayout != null) {
            layoutId = viewType;
        }

        if (context == null || context.get() == null) {
            return null;
        }

        View itemView = null; 
        try {
            itemView = LayoutInflater.from(context.get()).inflate(layoutId, parent, false);
        } catch (Exception i) {
            Log.e(TAG, "Inflate fail");
            itemView = new View(context.get());
            itemView.setId(View.generateViewId());
            return new MyHolder(itemView);
        }
        return new MyHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyHolder viewHolder, int position) {
        onBind(viewHolder, position, data);
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public long getItemId(int position) {
        return multiLayout == null ? layoutId : multiLayout.getLayoutId(position);
    }

    /**
     * If {@link #multiLayout} is not null, it will be useless.
     * @param layoutId item id
     */
    public IdeaAdapter<T> setSingleLayoutId(int layoutId) {
        this.layoutId = layoutId;
        return this;
    }

    /**
     * It will invalidate {@link #layoutId}.
     */
    public IdeaAdapter<T> setMultiLayout(MultiLayout multiLayout) {
        this.multiLayout = multiLayout;
        return this;
    }

    /**
     * It will invalidate {@link #data} also refresh the view list.
     * @param data  Data list
     * @return  {@link IdeaAdapter} The Object that call with this
     */
    public IdeaAdapter<T> setListData(List<T> data) {
        if (this.data == null) {
            this.data = data;
        } else if (this.data != data) {
            this.data.clear();
            notifyDataSetChanged();
            this.data = data;
        }

        return this;
    }

    /**
     * ViewHolder that get item view and set something for item.
     */
    public class MyHolder extends RecyclerView.ViewHolder {
        private SparseArray<View> views;
        private View viewItem;

        MyHolder(View itemView) {
            super(itemView);

            views = new SparseArray<>();
            viewItem = itemView;
        }

        /**
         * Get itemView from the item layout by id.
         */
        <T extends View>T getView(int viewId) {
            View v = views.get(viewId);
            if (v == null) {
                v = viewItem.findViewById(viewId);
                views.append(viewId, v);
            }
            return (T)v;
        }

        /**
         * Set listener for the view search by a resId on the current item.
         * @param resId view of item layout
         * @param listener Listener object
         * @return Result of set a listener for a item view.
         */
        public boolean setListener(int resId, Object listener) {
            View v = getView(resId);
            if (v == null || listener == null) {
                Log.e(TAG, "Failure due to take a null object pointer or view is not found by @resId.");
                return false;
            }

            Class c = listener.getClass();
            Class[] classes = c.getInterfaces();
            String listenerName = null;
            for (Class aClass : classes) {
                String name = aClass.getName();
                if (name.contains("android.view.View$On") && name.endsWith("Listener")) {
                    listenerName = aClass.getName();
                    break;
                }
            }

            if (listenerName == null) {
                return false;
            }

            switch (listenerName) {
                case InterfaceName.CLICK:
                    v.setOnClickListener((View.OnClickListener)listener);
                    break;
                case InterfaceName.LONG_CLICK:
                    v.setOnLongClickListener((View.OnLongClickListener)listener);
                    break;
                case InterfaceName.FOCUS_CHANGED:
                    v.setOnFocusChangeListener((View.OnFocusChangeListener)listener);
                    break;
                case InterfaceName.TOUCH:
                    v.setOnTouchListener((View.OnTouchListener)listener);
                    break;
                case InterfaceName.KEY:
                    v.setOnKeyListener((View.OnKeyListener)listener);
                    break;
                case InterfaceName.GENERIC_MOTION:
                    v.setOnGenericMotionListener((View.OnGenericMotionListener)listener);
                    break;
                default:
                    Log.e(TAG, "MyHolder@setListener: Unknown listener.");
                    return false;
            }

            return true;
        }

        public boolean setText(int resId, CharSequence text) {
            View v = getView(resId);
            if (v == null || !(v instanceof TextView)) {
                Log.e(TAG, "Failture due to a null view object.");
                return false;
            }

            ((TextView)v).setText(text);

            return true;
        }

        public boolean setImageDrawable(int resId, Drawable drawable) {
            View v = getView(resId);
            if (v == null) {
                Log.e(TAG, "Failure due to a null view Object");
                return false;
            }

            ((ImageView)v).setImageDrawable(drawable);

            return true;
        }

        public boolean setImageResource(int resId, int imgRes) {
            View v = getView(resId);
            if (v == null) {
                Log.e(TAG, "Failure due to a null view Object");
                return false;
            }

            ((ImageView)v).setImageResource(imgRes);

            return true;
        }

        public boolean setImageBitmap(int resId, Bitmap bm) {
            View v = getView(resId);
            if (v == null) {
                Log.e(TAG, "Failure due to a null view Object");
                return false;
            }

            ((ImageView)v).setImageBitmap(bm);

            return true;
        }

        private final class InterfaceName {
            final static String SCROLL_CHANGED = "android.view.View$OnScrollChangeListener";
            final static String LAYOUT_CHANGED = "android.view.View$OnLayoutChangeListener";
            final static String CAPTURED_POINTER = "android.view.View$OnCapturedPointerListener";
            final static String KEY = "android.view.View$OnKeyListener";
            final static String TOUCH = "android.view.View$OnTouchListener";
            final static String HOVER = "android.view.View$OnHoverListener";
            final static String GENERIC_MOTION = "android.view.View$OnGenericMotionListener";
            final static String LONG_CLICK = "android.view.View$OnLongClickListener";
            final static String DRAG = "android.view.View$OnDragListener";
            final static String FOCUS_CHANGED = "android.view.View$OnFocusChangeListener";
            final static String CLICK = "android.view.View$OnClickListener";
            final static String CONTEXT_CLICK = "android.view.View$OnContextClickListener";
            final static String CONTEXT_MENU = "android.view.View$OnCreateContextMenuListener";
            final static String SYSTEMUI_VISIBILITY_CHANGE = "android.view.View$OnSystemUiVisibilityChangeListener";
            final static String ATTACH_STATE_CHANGE = "android.view.View$OnAttachStateChangeListener";
            final static String APPLY_WINDOW_INSETS = "android.view.View$OnApplyWindowInsetsListener";
        }
    }

    /**
     * Default item decoration implement. Auto value is (2, 2, 0, 0).
     */
    public static class DefaultItemDecoration extends RecyclerView.ItemDecoration {
        private int left = 2;
        private int top = 2;
        private int right = 0;
        private int bottom = 0;

        public DefaultItemDecoration() {}

        public DefaultItemDecoration(int left, int top, int right, int bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.set(left, top, right, bottom);
        }
    }

    /**
     * It can be used to set a multi type list.
     */
    public interface MultiLayout {
        int getLayoutId(int pos);
    }
}
