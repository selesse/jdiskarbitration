package com.selesse.jdiskarbitration.internal;

import com.selesse.jdiskarbitration.DiskEventListener;
import com.selesse.jdiskarbitration.DiskInfo;
import com.sun.jna.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Internal implementation that handles JNA interactions with macOS DiskArbitration framework.
 * This class should not be used directly by library consumers.
 */
public class DiskEventWatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiskEventWatcher.class);

    private final DiskArbitration da;
    private final CoreFoundation cf;
    private final DiskInfoMapper diskInfoMapper;
    private Pointer session;
    private Pointer runLoop;
    private Thread runLoopThread;

    private final DiskArbitration.DADiskAppearedCallback appearedCallback;
    private final DiskArbitration.DADiskDisappearedCallback disappearedCallback;
    private final DiskArbitration.DADiskDescriptionChangedCallback descriptionChangedCallback;

    private final Set<String> mountedDisks = new HashSet<>();
    private Thread shutdownHook;

    private final List<Predicate<DiskInfo>> filters;

    public DiskEventWatcher(DiskEventListener listener, List<Predicate<DiskInfo>> filters) {
        this.filters = filters;
        da = DiskArbitration.INSTANCE;
        cf = CoreFoundation.INSTANCE;
        CoreFoundationValueReader cfReader = new CoreFoundationValueReader(cf);
        diskInfoMapper = new DiskInfoMapper(da, cf, cfReader);

        appearedCallback = (disk, context) -> {
            DiskInfo diskInfo = getDiskInfo(disk);
            if (diskInfo == null || shouldIgnore(diskInfo)) {
                return;
            }
            listener.onDiskAppeared(diskInfo);
        };

        disappearedCallback = (daDisk, context) -> {
            DiskInfo diskInfo = getDiskInfo(daDisk);
            if (diskInfo == null || shouldIgnore(diskInfo)) {
                return;
            }
            String bsdName = diskInfo.bsdName();
            if (mountedDisks.contains(bsdName)) {
                listener.onDiskUnmounted(diskInfo);
                mountedDisks.remove(bsdName);
            }
            listener.onDiskDisappeared(diskInfo);
        };

        descriptionChangedCallback = (daDisk, p, context) -> {
            DiskInfo diskInfo = getDiskInfo(daDisk);
            if (diskInfo == null || shouldIgnore(diskInfo)) {
                return;
            }
            String bsdName = diskInfo.bsdName();
            String volumePath = diskInfo.volumeInfo().path();

            // Check if this is a mount or unmount event
            if (volumePath != null && !mountedDisks.contains(bsdName)) {
                listener.onDiskMounted(diskInfo);
                mountedDisks.add(bsdName);
            } else if (volumePath == null && mountedDisks.contains(bsdName)) {
                listener.onDiskUnmounted(diskInfo);
                mountedDisks.remove(bsdName);
            }

            listener.onDiskDescriptionChanged(diskInfo);
        };
    }

    private DiskInfo getDiskInfo(Pointer daDisk) {
        return diskInfoMapper.fromDADisk(daDisk);
    }

    public void start() {
        runLoopThread = new Thread(this::runLoopThread, "DiskWatcher-Mac");
        runLoopThread.setDaemon(true);
        runLoopThread.start();

        // Register shutdown hook to clean up resources automatically
        shutdownHook = new Thread(this::stop, "DiskEventWatcher-ShutdownHook");
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    private void runLoopThread() {
        session = da.DASessionCreate(null);
        runLoop = cf.CFRunLoopGetCurrent();

        da.DARegisterDiskAppearedCallback(session, null, appearedCallback, null);
        da.DARegisterDiskDisappearedCallback(session, null, disappearedCallback, null);
        da.DARegisterDiskDescriptionChangedCallback(session, null, null, descriptionChangedCallback, null);

        // Schedule session with CFRunLoop (mandatory!)
        da.DASessionScheduleWithRunLoop(session, runLoop, cf.getkCFRunLoopDefaultMode());

        cf.CFRunLoopRun();

        // Clean up CoreFoundation resources on the same thread that created them
        try {
            da.DAUnregisterCallback(session, appearedCallback, null);
            da.DAUnregisterCallback(session, disappearedCallback, null);
            da.DAUnregisterCallback(session, descriptionChangedCallback, null);
            da.DASessionUnscheduleFromRunLoop(session, runLoop, cf.getkCFRunLoopDefaultMode());
            cf.CFRelease(session);
        } catch (Exception e) {
            LOGGER.error("Exception while trying to unregister callbacks", e);
        }
    }

    public void stop() {
        // Stop the run loop - this will cause CFRunLoopRun() to return
        if (runLoop != null) {
            cf.CFRunLoopStop(runLoop);
        }

        // Wait for the run loop thread to finish
        // This ensures all CoreFoundation operations complete on the correct thread
        if (runLoopThread != null && runLoopThread.isAlive()) {
            try {
                runLoopThread.join(5000); // Wait up to 5 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Remove shutdown hook if it was registered and we're not being called from it
        if (shutdownHook != null && Thread.currentThread() != shutdownHook) {
            try {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            } catch (IllegalStateException e) {
                // Shutdown already in progress, ignore
            }
        }

        // Clear references
        session = null;
        runLoop = null;
        runLoopThread = null;
    }

    private boolean shouldIgnore(DiskInfo diskInfo) {
        for (Predicate<DiskInfo> filter : filters) {
            if (!filter.test(diskInfo)) {
                return true;
            }
        }
        return false;
    }
}
