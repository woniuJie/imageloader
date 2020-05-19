package com.culiu.core.imageloader;

import com.facebook.common.memory.MemoryTrimType;
import com.facebook.common.memory.MemoryTrimmable;
import com.facebook.common.memory.MemoryTrimmableRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangsai on 2015/12/10.
 */
public class MemoryTrimManager implements MemoryTrimmableRegistry {
    
    private List<MemoryTrimmable> mMemoryList = new ArrayList<>();

    @Override
    public void registerMemoryTrimmable(MemoryTrimmable memoryTrimmable) {
        mMemoryList.add(memoryTrimmable);
    }

    @Override
    public void unregisterMemoryTrimmable(MemoryTrimmable memoryTrimmable) {
        mMemoryList.remove(memoryTrimmable);
    }

    public void trimMemory(MemoryTrimType type) {
        for (MemoryTrimmable mt : mMemoryList) {
            mt.trim(type);
        }
    }

}
