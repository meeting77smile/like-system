package com.meeting_smile.thumb.manager.cache;

import com.meeting_smile.thumb.manager.cache.Item;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public interface TopK {
    AddResult add(String key, int increment);
    List<Item> list();
    BlockingQueue<Item> expelled();
    void fading();
    long total();
}
