package com.selesse.jdiskarbitration;

public interface DiskEventListener {
    void onDiskAppeared(DiskInfo diskInfo);
    void onDiskDisappeared(DiskInfo diskInfo);
    void onDiskDescriptionChanged(DiskInfo diskInfo);
    void onDiskMounted(DiskInfo diskInfo);
    void onDiskUnmounted(DiskInfo diskInfo);
}
