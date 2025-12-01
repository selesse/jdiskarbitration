package com.selesse.jdiskarbitration.example;

import com.selesse.jdiskarbitration.DiskEventAdapter;
import com.selesse.jdiskarbitration.DiskEventManager;
import com.selesse.jdiskarbitration.DiskInfo;

public class App {
    public static void main(String[] args) {
        System.out.println("=== Example: USB External Disks Only ===\n");

        DiskEventManager manager = DiskEventManager.builder()
                .externalOnly()
                .usbOnly()
                .listener(new DiskEventAdapter() {
            @Override
            public void onDiskAppeared(DiskInfo diskInfo) {
                System.out.println("═══════════════════════════════════════════════════");
                System.out.println("USB disk appeared: " + diskInfo.bsdName());
                if (diskInfo.deviceInfo().vendor() != null || diskInfo.deviceInfo().model() != null) {
                    System.out.println("  Device: " +
                        (diskInfo.deviceInfo().vendor() != null ? diskInfo.deviceInfo().vendor() + " " : "") +
                        (diskInfo.deviceInfo().model() != null ? diskInfo.deviceInfo().model() : ""));
                }
                if (diskInfo.mediaInfo().mediaSize() != null) {
                    System.out.println("  Size: " + diskInfo.getFormattedSize());
                }
                System.out.println("  Properties: " +
                    "Removable=" + diskInfo.mediaInfo().isRemovable() +
                    ", Ejectable=" + diskInfo.mediaInfo().isEjectable() +
                    ", Writable=" + diskInfo.mediaInfo().isWritable() +
                    ", WholeDisk=" + diskInfo.mediaInfo().isWholeDisk());
            }

            @Override
            public void onDiskDisappeared(DiskInfo diskInfo) {
                System.out.println("USB disk disappeared: " + diskInfo.bsdName());
            }

            @Override
            public void onDiskDescriptionChanged(DiskInfo diskInfo) {
                System.out.println("USB disk description changed: " + diskInfo.bsdName());
            }

            @Override
            public void onDiskMounted(DiskInfo diskInfo) {
                System.out.println("───────────────────────────────────────────────────");
                System.out.println("USB disk MOUNTED: " + diskInfo.bsdName());
                if (diskInfo.volumeInfo().name() != null) {
                    System.out.println("  Volume: " + diskInfo.volumeInfo().name());
                }
                System.out.println("  Path: " + diskInfo.volumeInfo().path());
                if (diskInfo.volumeInfo().kind() != null) {
                    System.out.println("  Filesystem: " + diskInfo.volumeInfo().kind());
                }
                if (diskInfo.mediaInfo().mediaSize() != null) {
                    System.out.println("  Size: " + diskInfo.getFormattedSize());
                }
                if (diskInfo.mediaInfo().mediaUUID() != null) {
                    System.out.println("  Media UUID: " + diskInfo.mediaInfo().mediaUUID());
                }
                if (diskInfo.volumeInfo().uuid() != null) {
                    System.out.println("  Volume UUID: " + diskInfo.volumeInfo().uuid());
                }
                System.out.println("═══════════════════════════════════════════════════");
            }

            @Override
            public void onDiskUnmounted(DiskInfo diskInfo) {
                System.out.println("USB disk unmounted: " + diskInfo.bsdName());
            }
        })
                .build();

        System.out.println("Starting USB watcher. Press Ctrl+C to stop.");
        manager.start();

        // Keep the main thread alive indefinitely, waiting for a shutdown signal.
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            System.out.println("Main thread interrupted. Shutting down.");
            Thread.currentThread().interrupt();
        }
        System.out.println("USB watcher main thread finished.");
    }
}