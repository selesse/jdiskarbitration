package com.selesse.jdiskarbitration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@EnabledOnOs(OS.MAC)
class JnaIntegrationTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(JnaIntegrationTest.class);
    private static final String VOLUME_NAME = "JDiskArbitration-Test";
    private static final String DISK_IMAGE_PATH = "/tmp/" + VOLUME_NAME + ".dmg";
    private static final String DISK_SIZE = "1m";

    private String devicePath;
    private DiskEventManager eventManager;
    private CountDownLatch appearLatch;
    private CountDownLatch mountLatch;
    private CountDownLatch disappearLatch;
    private List<DiskInfo> appearedDisks;
    private List<DiskInfo> mountedDisks;
    private List<DiskInfo> disappearedDisks;
    private DiskEventListener listener;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        cleanup(); // Ensure no leftovers from previous runs

        appearLatch = new CountDownLatch(1);
        mountLatch = new CountDownLatch(1);
        disappearLatch = new CountDownLatch(1);
        appearedDisks = new ArrayList<>();
        mountedDisks = new ArrayList<>();
        disappearedDisks = new ArrayList<>();

        listener = new DiskEventAdapter() {
            @Override
            public void onDiskAppeared(DiskInfo diskInfo) {
                LOGGER.info("onDiskAppeared event received: {}", diskInfo);
                if (diskInfo.volumeInfo().name() != null && diskInfo.volumeInfo().name().equals(VOLUME_NAME)) {
                    appearedDisks.add(diskInfo);
                    appearLatch.countDown();
                }
            }

            @Override
            public void onDiskMounted(DiskInfo diskInfo) {
                LOGGER.info("onDiskMounted event received: {}", diskInfo);
                if (diskInfo.volumeInfo().name() != null && diskInfo.volumeInfo().name().equals(VOLUME_NAME)) {
                    mountedDisks.add(diskInfo);
                    mountLatch.countDown();
                }
            }

            @Override
            public void onDiskDisappeared(DiskInfo diskInfo) {
                LOGGER.info("onDiskDisappeared event received: {}", diskInfo);
                // Match by BSD name against the device path we attached
                if (devicePath != null && diskInfo.bsdName().equals(devicePath.replace("/dev/", ""))) {
                    disappearedDisks.add(diskInfo);
                    disappearLatch.countDown();
                }
            }
        };

        LOGGER.info("Starting DiskEventManager...");
        eventManager = DiskEventManager.builder()
                .listener(listener)
                .filter(diskInfo -> {
                    // Only listen for our test volume by name
                    // For disappear events, the volume name might be null, so we let those through
                    // and the listener will filter by devicePath
                    String volumeName = diskInfo.volumeInfo().name();
                    return volumeName == null || VOLUME_NAME.equals(volumeName);
                })
                .build();
        eventManager.start();
        LOGGER.info("DiskEventManager started.");

        createDiskImage();
        devicePath = attachDiskImage();
    }

    @AfterEach
    void tearDown() throws IOException, InterruptedException {
        if (devicePath != null) {
            detachDiskImage(devicePath);
        }
        cleanup();
        if (eventManager != null) {
            LOGGER.info("Stopping DiskEventManager...");
            eventManager.stop();
            LOGGER.info("DiskEventManager stopped.");
        }
    }

    private void createDiskImage() throws IOException, InterruptedException {
        executeCommand("hdiutil", "create", "-size", DISK_SIZE, "-fs", "HFS+", "-volname", VOLUME_NAME, DISK_IMAGE_PATH);
    }

    private String attachDiskImage() throws IOException, InterruptedException {
        // The -plist flag makes hdiutil output machine-readable XML
        String output = executeCommand("hdiutil", "attach", "-plist", DISK_IMAGE_PATH);
        // A simple plist parser to find the device entry
        // The plist has <key>dev-entry</key> followed by <string>/dev/diskXsY</string> on the next line
        String[] lines = output.split("\n");
        for (int i = 0; i < lines.length - 1; i++) {
            if (lines[i].contains("<key>dev-entry</key>")) {
                // Next line should have the <string> value
                String nextLine = lines[i + 1].trim();
                if (nextLine.contains("<string>")) {
                    return nextLine.replaceAll(".*<string>(.*)</string>.*", "$1").trim();
                }
            }
        }
        fail("Could not find device path in hdiutil output");
        return null;
    }

    private void detachDiskImage(String device) throws IOException, InterruptedException {
        executeCommand("hdiutil", "detach", device);
    }

    private void cleanup() {
        // Unmount all JDiskArbitration-Test volumes by searching for mounted volumes
        try {
            File volumesDir = new File("/Volumes");
            File[] volumes = volumesDir.listFiles();
            if (volumes != null) {
                for (File volume : volumes) {
                    if (volume.getName().startsWith(VOLUME_NAME)) {
                        LOGGER.info("Unmounting leftover volume: {}", volume.getAbsolutePath());
                        try {
                            executeCommand("hdiutil", "detach", volume.getAbsolutePath(), "-force");
                        } catch (Exception e) {
                            LOGGER.warn("Failed to unmount {} {}", volume, e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Error during cleanup: {}", e.getMessage());
        }

        // Remove the disk image file
        File diskImage = new File(DISK_IMAGE_PATH);
        if (diskImage.exists()) {
            try {
                executeCommand("rm", DISK_IMAGE_PATH);
            } catch (Exception e) {
                LOGGER.warn("Failed to remove disk image: {}", e.getMessage());
            }
        }
    }

    private String executeCommand(String... command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = processBuilder.start();
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                LOGGER.info(line);
            }
        }
        process.waitFor(2, TimeUnit.SECONDS);
        return output.toString();
    }

    @Test
    void testDiskMountAndUnmountEvents() throws InterruptedException {
        LOGGER.info("DiskEventManager started. Waiting for events...");

        // The disk is already mounted by setUp, so we just wait for the events
        assertTrue(appearLatch.await(2, TimeUnit.SECONDS), "Disk did not appear");
        LOGGER.info("Disk appeared. Waiting for mount event...");
        assertTrue(mountLatch.await(2, TimeUnit.SECONDS), "Disk did not mount");
        LOGGER.info("Disk mounted. Verifying appeared and mounted disk info...");

        assertEquals(1, appearedDisks.size());
        DiskInfo appearedDisk = appearedDisks.get(0);
        assertEquals(VOLUME_NAME, appearedDisk.volumeInfo().name());
        // Note: path might be null when disk first appears, before it's mounted

        assertEquals(1, mountedDisks.size());
        DiskInfo mountedDisk = mountedDisks.get(0);
        assertEquals(VOLUME_NAME, mountedDisk.volumeInfo().name());
        assertNotNull(mountedDisk.volumeInfo().path());
        assertTrue(mountedDisk.volumeInfo().path().startsWith("/Volumes/"));

        LOGGER.info("Detaching disk...");
        // Now, detach the disk
        assertDoesNotThrow(() -> detachDiskImage(devicePath));
        LOGGER.info("Disk detached. Waiting for disappear event...");
        assertTrue(disappearLatch.await(10, TimeUnit.SECONDS), "Disk did not disappear");
        LOGGER.info("Disk disappeared. Verifying disappeared disk info...");
        assertEquals(1, disappearedDisks.size());
    }
}
