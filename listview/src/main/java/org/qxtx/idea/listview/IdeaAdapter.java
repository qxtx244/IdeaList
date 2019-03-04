package org.qxtx.idea.listview;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by QXTX-OBOOK on 2017/9/10.
 * 一个通配适配器
 * 传参：
 * 1、上下文
 * 2、item
 * 3、布局id
 */

public abstract class IdeaAdapter<T> extends BaseAdapter {
    private Context context;
    private ArrayList<T> list;
    private int layoutRes;//布局的id需要从外面传进来，bind中需要用它来获取convertView

    public IdeaAdapter(Context context, ArrayList<T> list, int layoutRes) {
        this.context = context;
        this.list = list;
        this.layoutRes = layoutRes;
    }

    @Override
    public int getCount() {
        return list == null? 0 : list.size();
    }
    @Override
    public T getItem(int position) {
        return list.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    //ViewGroup:上层layout， 利用它可以对上层layout进行操作
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        /*将获取convertView部分放到Holder构造中
         * 完成了findbyId部分
         * 完成了实例化convertView
         */
        Holder holder = Holder.bind(position, convertView, parent, layoutRes, context);

        /*这里的实现交由外部完成，set数据部分
         * 因为由于布局不一样，里面的属性也不一定相同，所以要在外部自定义完成；
         * holder提供了set方法
         */
        bindView(holder, getItem(position));
        //注意：这里返回的convertView不能为空！！！否则会报
        return holder.convertView;
    }


/*/***********************静态内部类Holder*****************************/
    /**
     * 这里的Holder需要完成的工作
     * 1、保存convertView
     * 2、设置Tag
     * 3、暴露一堆set方法，用于在外部实现set属性
     */
    public static class Holder {
        private int position;
        private View convertView;//保存主view
        private Context context;

        /**初始化item的view列表
         * SparseArray有更好的性能较之ArrayList
         * 这是为了保存item中的各种id
         * 因为不知道不同的item中有多少属性，所以用一个list来存储各种id
         */
        private SparseArray<View> itemView;


        /**构造方法
         * SparseArray初始化
         * 在这里找到主view并设置Tag
         * 保存context和itemView
         */
        public Holder(Context context, ViewGroup parent, int layoutRes) {
            itemView = new SparseArray<>();
            this.context = context;
            convertView = LayoutInflater.from(context).inflate(layoutRes, parent, false);
            convertView.setTag(this);
        }


        /**绑定view
         * 注意findById并不能在内部做，因为不同layout可能有不同的各种属性
         * 这里获取的position，是为了给各种暴露的set方法使用
         */
        public static Holder bind(int position, View convertView,
                                  ViewGroup parent, int layoutRes, Context context) {
            Holder holder = null;
            if (convertView == null) {
                Log.e("消息", "convertView为空");
                holder = new Holder(context, parent, layoutRes);//这里完成了holder的两项基本工作
            } else {
                //从convertView中获取到holder
                holder = (Holder)convertView.getTag();
                holder.convertView = convertView;
            }
            holder.position = position;//得到一个position
            return holder;
        }


/*/*************实例化item的各种属性、获取item位置*******************/
        /**
         * 这个方法的功能是获取各种id提供给之后的set方法使用
         * 并将这些id添加到一个list中保存
         */
        public <T extends View>T getView(int id) {
            T t = (T)itemView.get(id);//从列表中获得id
            if (t == null) {
                //如果列表中没有，则从主View中拿，然后放进列表中，变相的完成了findById()方法
                t = (T)convertView.findViewById(id);
                //将id保存起来。因为sparseArray还可以用一个key来索引数据，提供两种索引方式。
                itemView.put(id, t);
            }
            return t;//返回的可以是一个id
        }

        //提供获取当前的item
        public View getItem() {
            return convertView;
        }
        //提供获取当前条目的位置
        public int getItemPosition() {
            return position;
        }


/*/******************实例化item中的各种属性************************/
        /**上文获取了一堆id，这里将要set一堆数据
         * 通过传入一个id，将此id保存到item列表中
         * CharSequence：字符数据，接口，String为实现接口的一种
         *
         * 目前set方法有
         * 1、setText
         * 2、setImageResource(ImageView)
         * 2、setBackgroundResource(Button、ImageButton)
         * 3、setVisibility
         * 4、setOnClickListener
         * 5、setItemOnClickLIstener
         */

        //设置文本
        public Holder setText(int id, CharSequence text) {
            View view = getView(id);
            if (view instanceof TextView) {
                ((TextView) view).setText(text.toString());//需要将view转换成相应的view
            }
            return this;//为什么要返回一个Holder类型的值？？？？看起来用不到，能不能不返回？
        }

        //设置文本框最大宽度
        public void setTextWidth(int id, int len) {
            View view = getView(id);
            ((TextView)view).setMaxWidth(len);
            ((TextView)view).setSingleLine(true);
        }
        //设置文本过长时显示模式
        public void setTextMarquee(int id) {
            View view = getView(id);
            ((TextView)view).setEllipsize(TextUtils.TruncateAt.MARQUEE);//跑马灯
            ((TextView)view).setSingleLine(true);
        }

        //设置背景，包括Button， ImageButton，但不包括ImageView
        public Holder setBackgroundResource(int id, int drawableRes) {
            View view = getView(id);
            view.setBackgroundResource(drawableRes);
            return this;
        }

        //设置背景，ImageView独占
        public Holder setImageResource(int id, int drawableRes) {
            View view = getView(id);
            ((ImageView)view).setImageResource(drawableRes);
            return this;
        }

        //设置可见/隐藏
        public Holder setVisibility(int id, int visible) {
            View view = getView(id);
            view.setVisibility(visible);
            return this;
        }

        //设置监听item中的按钮点击
        public Holder setOnClickListener(int id, View.OnClickListener click) {
            View view = getView(id);
            view.setOnClickListener(click);
            return this;
        }

        //设置监听item点击
        public Holder setItemOnClickLIstener(ListView listView, AdapterView.OnItemClickListener itemClick) {
            listView.setOnItemClickListener(itemClick);
            return this;
        }
    }


/*/******************为Adapter暴露添加元素的方法************************/
    /**
     * 1、添加一个item（默认在队列末端）
     * 2、在制定位置添加 一个tiem
     */

    //添加 一个item（默认位置为列表末端）
    public void add(T obj) {
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(obj);
        notifyDataSetChanged();
    }

    //在制定位置添加 一个item
    public void add(int position, T obj) {
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(position, obj);
        notifyDataSetChanged();
    }

    public abstract void bindView(Holder holder, T obj);//抽象方法，用来设置各种属性
}
