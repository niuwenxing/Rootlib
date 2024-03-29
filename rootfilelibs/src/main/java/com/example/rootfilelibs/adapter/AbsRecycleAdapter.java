package com.example.rootfilelibs.adapter;

import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * 列表选择器
 * @param <T>
 */

public abstract class AbsRecycleAdapter<T> extends RecyclerView.Adapter<AbsRecycleAdapter.VH> {

    public static final int CHOICE_MODE_NONE = 0;
    public static final int CHOICE_MODE_SINGLE = 1; //单选
    public static final int CHOICE_MODE_MULTIPLE = 2; //多选
    public static final int INVALID_POSITION = -1;

    private SparseBooleanArray mCheckStates;
    private int mChoiceMode = CHOICE_MODE_NONE;
    private int mCheckedItemCount;

    private OnItemClickListener mItemClickListener;

    private List<T> mData;

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        final VH viewHolder = VH.get(parent, getLayoutId(viewType));

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();

                if (mChoiceMode != CHOICE_MODE_NONE && mData.get(position) instanceof Checkable) {
                    if (mChoiceMode == CHOICE_MODE_SINGLE) {
                        boolean checked = !mCheckStates.get(position, false);
                        if (mCheckedItemCount == 1 && mCheckStates.valueAt(0)) {
                            int lastSelectedPosition = mCheckStates.keyAt(0);
                            ((Checkable) mData.get(lastSelectedPosition)).setChecked(false);
                            notifyItemChanged(lastSelectedPosition);
                        }
                        if (checked) {
                            mCheckStates.clear();
                            mCheckStates.put(position, true);
                            mCheckedItemCount = 1;
                            ((Checkable) mData.get(position)).setChecked(true);
                        } else {
                            mCheckStates.clear();
                            mCheckedItemCount = 0;
                        }
                    } else if (mChoiceMode == CHOICE_MODE_MULTIPLE) {
                        boolean checked = !mCheckStates.get(position, false);
                        mCheckStates.put(position, checked);
                        ((Checkable) mData.get(position)).toggle();
                        if (checked) {
                            mCheckedItemCount++;
                        } else {
                            mCheckedItemCount--;
                        }
                    }
                    notifyItemChanged(position);
                }
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(position, v);
                }
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        convert(holder, mData.get(position), position);
    }

    public abstract int getLayoutId(int viewType);

    public abstract void convert(VH holder, T data, int position);

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    public void setChoiceMode(int choiceMode) {
        mChoiceMode = choiceMode;
        if (mChoiceMode != CHOICE_MODE_NONE) {
            if (mCheckStates == null) {
                mCheckStates = new SparseBooleanArray(0);
            }
        }
    }

    public void setItemChecked(int position, boolean value) {
        if (mChoiceMode == CHOICE_MODE_NONE) {
            return;
        }
        boolean itemCheckChanged;
        if (mChoiceMode == CHOICE_MODE_MULTIPLE) {
            boolean oldValue = mCheckStates.get(position);
            mCheckStates.put(position, value);
            itemCheckChanged = oldValue != value;
            if (itemCheckChanged) {
                if (value) {
                    mCheckedItemCount++;
                } else {
                    mCheckedItemCount--;
                }
            }
        } else {
            if (value || isItemChecked(position)) {
                if (mCheckedItemCount > 0 && mCheckStates.valueAt(0)){
                    int lastPosition = mCheckStates.keyAt(0);
                    boolean isCheckedChanged = lastPosition != position;
                    if (isCheckedChanged){
                        ((Checkable) mData.get(lastPosition)).setChecked(false);
                        notifyItemChanged(lastPosition);
                    }
                }
                mCheckStates.clear();
            }
            if (value) {
                mCheckStates.put(position, true);
                mCheckedItemCount = 1;
            } else if (mCheckStates.size() == 0 || !mCheckStates.valueAt(0)) {
                mCheckedItemCount = 0;
            }
        }
        if (mData.get(position) instanceof Checkable) {
            ((Checkable) mData.get(position)).setChecked(value);
        }
        notifyItemChanged(position);
    }

    public boolean hasChecked() {
        return mCheckedItemCount > 0;
    }

    public int getCheckedItemPosition() {
        if (mChoiceMode == CHOICE_MODE_SINGLE && mCheckStates != null && mCheckStates.size() == 1) {
            return mCheckStates.keyAt(0);
        }
        return INVALID_POSITION;
    }

    public int getCheckedItemSize() {
        return mCheckedItemCount;
    }

    public void clearChoices() {
        if (mCheckStates != null) {
            mCheckStates.clear();
        }
        mCheckedItemCount = 0;
    }

    public SparseBooleanArray getCheckedItemPositions() {
        if (mChoiceMode != CHOICE_MODE_NONE) {
            return mCheckStates;
        }
        return null;
    }

    public boolean isItemChecked(int position) {
        return mChoiceMode != CHOICE_MODE_NONE && mCheckStates != null && mCheckStates.get(position);
    }

    public void setData(List<T> beans) {
        if (null == mData) {
            mData = new ArrayList<>();
        }
        mData.clear();
        if (beans !=null && !beans.isEmpty()) {
            mData.addAll(beans);
        }
    }

    public void addData(List<T> beans) {
        if (null == mData) {
            setData(beans);
        } else {
            mData.addAll(beans);
        }
    }

    public void addData(T t) {
        if (mData == null) {
            mData = new ArrayList<>();
        }
        if (!mData.contains(t)) {
            mData.add(t);
        }
    }

    public void remove(T t) {
        if (null != mData) {
            mData.remove(t);
        }
    }

    public void remove(int position) {
        if (null != mData) {
            mData.remove(position);
        }
    }

    public List<T> getData() {
        return mData;
    }

    public void clear() {
        if (mData != null) {
            mData.clear();
        }
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public T getItem(int position) {
        if (mData != null) {
            return mData.get(position);
        }
        return null;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, View v);
    }

    public static class VH extends RecyclerView.ViewHolder {
        private SparseArray<View> mViews;
        private View mConvertView;

        private VH(View v) {
            super(v);
            mConvertView = v;
            mViews = new SparseArray<>();
        }

        public static VH get(ViewGroup parent, int layoutId) {
            View convertView = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            return new VH(convertView);
        }

        public <T extends View> T getView(int id) {
            View v = mViews.get(id);
            if (v == null) {
                v = mConvertView.findViewById(id);
                mViews.put(id, v);
            }
            return (T) v;
        }

        public void setText(int id, String value) {
            TextView view = getView(id);
            view.setText(value);
        }

        public void setTextColor(int id, @ColorInt int color) {
            TextView view = getView(id);
            view.setTextColor(color);
        }

        public void setBackgroundResource(int id, @DrawableRes int resId) {
            View view = getView(id);
            view.setBackgroundResource(resId);
        }

        public void setViewShow(int id, boolean isShow) {
            View view = getView(id);
            view.setVisibility(isShow ? View.VISIBLE : View.GONE);
        }

        public void setImageResid(int id, int resourceId) {
            ImageView imageView = getView(id);
            imageView.setImageResource(resourceId);
        }

    }


}
