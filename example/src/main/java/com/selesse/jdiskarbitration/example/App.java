package com.selesse.jdiskarbitration.example;

import com.selesse.jdiskarbitration.DiskEventAdapter;
import com.selesse.jdiskarbitration.DiskEventManager;
import com.selesse.jdiskarbitration.DiskInfo;

public class App {
    private static void printDiskInfo(DiskInfo disk) {
        System.out.println("\n┌─ VolumeInfo ─────────────────────────────────────");
        printField("path", disk.volumeInfo().path());
        printField("name", disk.volumeInfo().name());
        printField("kind", disk.volumeInfo().kind());
        printField("uuid", disk.volumeInfo().uuid());
        printField("mountable", disk.volumeInfo().mountable());
        printField("network", disk.volumeInfo().network());
        printField("type", disk.volumeInfo().type());

        System.out.println("\n├─ DeviceInfo ─────────────────────────────────────");
        printField("protocol", disk.deviceInfo().protocol());
        printField("model", disk.deviceInfo().model());
        printField("vendor", disk.deviceInfo().vendor());
        printField("revision", disk.deviceInfo().revision());
        printField("unit", disk.deviceInfo().unit());
        printField("isInternal", disk.deviceInfo().isInternal());
        printField("guid", disk.deviceInfo().guid());
        printField("path", disk.deviceInfo().path());
        printField("tdmLocked", disk.deviceInfo().tdmLocked());

        System.out.println("\n├─ MediaInfo ──────────────────────────────────────");
        printField("isRemovable", disk.mediaInfo().isRemovable());
        printField("mediaSize", disk.mediaInfo().mediaSize() != null ? disk.getFormattedSize() : null);
        printField("mediaBlockSize", disk.mediaInfo().mediaBlockSize());
        printField("isWritable", disk.mediaInfo().isWritable());
        printField("isWholeDisk", disk.mediaInfo().isWholeDisk());
        printField("isEjectable", disk.mediaInfo().isEjectable());
        printField("isLeaf", disk.mediaInfo().isLeaf());
        printField("mediaType", disk.mediaInfo().mediaType());
        printField("mediaContent", disk.mediaInfo().mediaContent());
        printField("mediaUUID", disk.mediaInfo().mediaUUID());
        printField("bsdMajor", disk.mediaInfo().bsdMajor());
        printField("bsdMinor", disk.mediaInfo().bsdMinor());
        printField("bsdName", disk.mediaInfo().bsdName());
        printField("bsdUnit", disk.mediaInfo().bsdUnit());
        printField("icon", disk.mediaInfo().icon());
        printField("kind", disk.mediaInfo().kind());
        printField("name", disk.mediaInfo().name());
        printField("path", disk.mediaInfo().path());
        printField("encrypted", disk.mediaInfo().encrypted());
        printField("encryptionDetail", disk.mediaInfo().encryptionDetail());

        System.out.println("\n└─ BusInfo ────────────────────────────────────────");
        printField("name", disk.busInfo().name());
        printField("path", disk.busInfo().path());
        System.out.println();
    }

    private static void printField(String name, Object value) {
        if (value != null) {
            System.out.printf("  %-20s: %s%n", name, value);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Example: USB External Disks Only ===\n");

        DiskEventManager manager = DiskEventManager.builder()
                .externalOnly()
                .usbOnly()
                .listener(new DiskEventAdapter() {
            @Override
            public void onDiskAppeared(DiskInfo diskInfo) {
                System.out.println("\n╔═══════════════════════════════════════════════════╗");
                System.out.println("║ DISK APPEARED: " + diskInfo.bsdName());
                System.out.println("╚═══════════════════════════════════════════════════╝");
                printDiskInfo(diskInfo);
            }

            @Override
            public void onDiskDisappeared(DiskInfo diskInfo) {
                System.out.println("\n[DISAPPEARED] " + diskInfo.bsdName());
            }

            @Override
            public void onDiskMounted(DiskInfo diskInfo) {
                System.out.println("\n╔═══════════════════════════════════════════════════╗");
                System.out.println("║ DISK MOUNTED: " + diskInfo.bsdName());
                System.out.println("╚═══════════════════════════════════════════════════╝");
                printDiskInfo(diskInfo);
            }

            @Override
            public void onDiskUnmounted(DiskInfo diskInfo) {
                System.out.println("\n[UNMOUNTED] " + diskInfo.bsdName());
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