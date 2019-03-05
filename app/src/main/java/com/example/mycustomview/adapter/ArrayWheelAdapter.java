package com.example.mycustomview.adapter;

import android.widget.ArrayAdapter;

import java.util.List;

public class ArrayWheelAdapter<T> implements WheelAdapter<T>{
    private List<T> list;
    public ArrayWheelAdapter(List<T> list){
        this.list=list;
    }
    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public T getItem(int index) {
        return list.get(index);
    }

    @Override
    public int indexOf(T item) {
        return list.indexOf(item);
    }
}
