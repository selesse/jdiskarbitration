package com.selesse.jdiskarbitration;

/**
 * An abstract adapter class for receiving disk events.
 * The methods in this class are empty. This class exists as convenience for creating listener objects.
 *
 * Extend this class to implement only the events you care about.
 *
 * Example:
 * <pre>
 * DiskEventManager manager = DiskEventManager.builder()
 *     .usbOnly()
 *     .listener(new DiskEventAdapter() {
 *         {@literal @}Override
 *         public void onDiskMounted(DiskInfo diskInfo) {
 *             System.out.println("USB mounted: " + diskInfo.volumeInfo().name());
 *         }
 *     })
 *     .build();
 * </pre>
 */
public abstract class DiskEventAdapter implements DiskEventListener {

    @Override
    public void onDiskAppeared(DiskInfo diskInfo) {
        // Default implementation does nothing
    }

    @Override
    public void onDiskDisappeared(DiskInfo diskInfo) {
        // Default implementation does nothing
    }

    @Override
    public void onDiskDescriptionChanged(DiskInfo diskInfo) {
        // Default implementation does nothing
    }

    @Override
    public void onDiskMounted(DiskInfo diskInfo) {
        // Default implementation does nothing
    }

    @Override
    public void onDiskUnmounted(DiskInfo diskInfo) {
        // Default implementation does nothing
    }
}
