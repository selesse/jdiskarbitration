package com.selesse.jdiskarbitration.internal;

import com.selesse.jdiskarbitration.DiskEventListener;
import com.selesse.jdiskarbitration.DiskInfo;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
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
        Pointer bsdNamePtr = da.DADiskGetBSDName(daDisk);
        if (bsdNamePtr == null) {
            return null;
        }
        String bsdName = bsdNamePtr.getString(0);

        Pointer dict = da.DADiskCopyDescription(daDisk);
        if (dict == null) {
            return new DiskInfo.Builder().bsdName(bsdName).build();
        }

        try {
            DiskInfo.Builder builder = new DiskInfo.Builder().bsdName(bsdName);

            // Volume information
            builder.volumePath(getVolumePath(dict));
            builder.volumeName(getStringValue(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionVolumeNameKey));
            builder.volumeKind(getStringValue(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionVolumeKindKey));
            builder.volumeUUID(getUUIDString(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionVolumeUUIDKey));
            builder.volumeMountable(getBooleanValueNullable(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionVolumeMountableKey));
            builder.volumeNetwork(getBooleanValueNullable(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionVolumeNetworkKey));
            builder.volumeType(getStringValue(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionVolumeTypeKey));

            // Device information
            builder.deviceProtocol(getStringValue(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionDeviceProtocolKey));
            builder.deviceModel(getStringValue(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionDeviceModelKey));
            builder.deviceVendor(getStringValue(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionDeviceVendorKey));
            builder.deviceRevision(getStringValue(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionDeviceRevisionKey));
            builder.deviceUnit(getNumberValue(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionDeviceUnitKey));
            builder.isInternal(getBooleanValue(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionDeviceInternalKey));
            builder.deviceGuid(getUUIDString(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionDeviceGUIDKey));
            builder.devicePath(getStringValue(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionDevicePathKey));
            builder.deviceTdmLocked(getBooleanValueNullable(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionDeviceTDMLockedKey));

            // Media information
            builder.isRemovable(getBooleanValue(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionMediaRemovableKey));
            builder.mediaSize(getNumberValue(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionMediaSizeKey));
            builder.mediaBlockSize(getNumberValue(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionMediaBlockSizeKey));
            builder.isWritable(getBooleanValue(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionMediaWritableKey));
            builder.isWholeDisk(getBooleanValue(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionMediaWholeKey));
            builder.isEjectable(getBooleanValue(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionMediaEjectableKey));
            builder.isLeaf(getBooleanValue(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionMediaLeafKey));
            builder.mediaType(getStringValue(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionMediaTypeKey));
            builder.mediaContent(getStringValue(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionMediaContentKey));
            builder.mediaUUID(getUUIDString(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionMediaUUIDKey));
            builder.mediaBsdMajor(getIntegerValue(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionMediaBSDMajorKey));
            builder.mediaBsdMinor(getIntegerValue(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionMediaBSDMinorKey));
            builder.mediaBsdName(getStringValue(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionMediaBSDNameKey));
            builder.mediaBsdUnit(getIntegerValue(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionMediaBSDUnitKey));
            builder.mediaIcon(getStringValue(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionMediaIconKey));
            builder.mediaKind(getStringValue(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionMediaKindKey));
            builder.mediaName(getStringValue(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionMediaNameKey));
            builder.mediaPath(getStringValue(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionMediaPathKey));
            builder.mediaEncrypted(getBooleanValueNullable(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionMediaEncryptedKey));
            builder.mediaEncryptionDetail(getStringValue(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionMediaEncryptionDetailKey));

            // Bus information
            builder.busName(getStringValue(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionBusNameKey));
            builder.busPath(getStringValue(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionBusPathKey));

            return builder.build();
        } finally {
            cf.CFRelease(dict);
        }
    }

    private String getVolumePath(Pointer dict) {
        Pointer volumePathRef = cf.CFDictionaryGetValue(dict, DiskArbitrationKeys.INSTANCE.kDADiskDescriptionVolumePathKey);
        if (volumePathRef == null || volumePathRef == Pointer.NULL) {
            return null;
        }
        Pointer cfStringPath = cf.CFURLCopyFileSystemPath(volumePathRef, CoreFoundation.kCFURLPOSIXPathStyle);
        if (cfStringPath == null || cfStringPath == Pointer.NULL) {
            return null;
        }
        try {
            return cfPointerToString(cfStringPath);
        } finally {
            cf.CFRelease(cfStringPath);
        }
    }

    private String getStringValue(Pointer dict, Pointer key) {
        if (key == null || key == Pointer.NULL) {
            return null;
        }
        try {
            Pointer ref = cf.CFDictionaryGetValue(dict, key);
            if (ref == null || ref == Pointer.NULL) {
                return null;
            }
            return cfPointerToString(ref);
        } catch (Exception e) {
            LOGGER.debug("Failed to get string value for key", e);
            return null;
        }
    }

    private String getUUIDString(Pointer dict, Pointer key) {
        if (key == null || key == Pointer.NULL) {
            return null;
        }
        try {
            Pointer uuidRef = cf.CFDictionaryGetValue(dict, key);
            if (uuidRef == null || uuidRef == Pointer.NULL) {
                return null;
            }
            // Convert CFUUIDRef to CFString
            Pointer uuidString = cf.CFUUIDCreateString(null, uuidRef);
            if (uuidString == null || uuidString == Pointer.NULL) {
                return null;
            }
            try {
                return cfPointerToString(uuidString);
            } finally {
                cf.CFRelease(uuidString); // Release the created string
            }
        } catch (Exception e) {
            LOGGER.debug("Failed to get UUID value for key", e);
            return null;
        }
    }

    private boolean getBooleanValue(Pointer dict, Pointer key) {
        if (key == null || key == Pointer.NULL) {
            return false;
        }
        try {
            Pointer ref = cf.CFDictionaryGetValue(dict, key);
            if (ref == null || ref == Pointer.NULL) {
                return false;
            }
            return cf.CFBooleanGetValue(ref);
        } catch (Exception e) {
            LOGGER.debug("Failed to get boolean value for key", e);
            return false;
        }
    }

    private Boolean getBooleanValueNullable(Pointer dict, Pointer key) {
        if (key == null || key == Pointer.NULL) {
            return null;
        }
        try {
            Pointer ref = cf.CFDictionaryGetValue(dict, key);
            if (ref == null || ref == Pointer.NULL) {
                return null;
            }
            return cf.CFBooleanGetValue(ref);
        } catch (Exception e) {
            LOGGER.debug("Failed to get boolean value for key", e);
            return null;
        }
    }

    private Long getNumberValue(Pointer dict, Pointer key) {
        if (key == null || key == Pointer.NULL) {
            return null;
        }
        try {
            Pointer ref = cf.CFDictionaryGetValue(dict, key);
            if (ref == null || ref == Pointer.NULL) {
                return null;
            }
            Memory mem = new Memory(8);
            boolean success = cf.CFNumberGetValue(ref, CoreFoundation.kCFNumberSInt64Type, mem);
            if (!success) {
                return null;
            }
            return mem.getLong(0);
        } catch (Exception e) {
            LOGGER.debug("Failed to get number value for key", e);
            return null;
        }
    }

    private Integer getIntegerValue(Pointer dict, Pointer key) {
        if (key == null || key == Pointer.NULL) {
            return null;
        }
        try {
            Pointer ref = cf.CFDictionaryGetValue(dict, key);
            if (ref == null || ref == Pointer.NULL) {
                return null;
            }
            Memory mem = new Memory(4);
            boolean success = cf.CFNumberGetValue(ref, CoreFoundation.kCFNumberSInt32Type, mem);
            if (!success) {
                return null;
            }
            return mem.getInt(0);
        } catch (Exception e) {
            LOGGER.debug("Failed to get integer value for key", e);
            return null;
        }
    }

    private String cfPointerToString(Pointer pointer) {
        if (pointer == null || pointer == Pointer.NULL) {
            return null;
        }

        try {
            // Check if the pointer is actually a CFString
            long typeID = cf.CFGetTypeID(pointer);
            long stringTypeID = cf.CFStringGetTypeID();

            if (typeID != stringTypeID) {
                // Not a string - might be CFData, CFDictionary, etc.
                LOGGER.debug("Value is not a CFString (typeID: {}, expected: {})", typeID, stringTypeID);
                return null;
            }

            // Try fast path first
            Pointer cStringPtr = cf.CFStringGetCStringPtr(pointer, CoreFoundation.kCFStringEncodingUTF8);
            if (cStringPtr != null && cStringPtr != Pointer.NULL) {
                return cStringPtr.getString(0, "UTF-8");
            }

            // Fall back to copying the string
            long length = cf.CFStringGetLength(pointer);
            if (length == 0) {
                return "";
            }

            // Allocate buffer (use max size for safety)
            long maxSize = cf.CFStringGetMaximumSizeForEncoding(length, CoreFoundation.kCFStringEncodingUTF8) + 1;
            byte[] buffer = new byte[(int) maxSize];

            boolean success = cf.CFStringGetCString(pointer, buffer, maxSize, CoreFoundation.kCFStringEncodingUTF8);
            if (!success) {
                return null;
            }

            // Find null terminator and convert to String
            int nullIndex = 0;
            while (nullIndex < buffer.length && buffer[nullIndex] != 0) {
                nullIndex++;
            }

            return new String(buffer, 0, nullIndex, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Value is not a CFString (might be CFData, CFDictionary, etc.)
            LOGGER.debug("Failed to convert pointer to string", e);
            return null;
        }
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
