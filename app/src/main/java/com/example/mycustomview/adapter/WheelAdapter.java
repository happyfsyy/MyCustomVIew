package com.example.mycustomview.adapter;

public interface WheelAdapter<T>{
    int getItemCount();
    T getItem(int index);
    int indexOf(T item);
}
