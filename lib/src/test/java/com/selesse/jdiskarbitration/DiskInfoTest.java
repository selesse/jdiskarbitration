package com.selesse.jdiskarbitration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiskInfoTest {

    @Test
    void testBuilderCreatesValidDiskInfo() {
        DiskInfo diskInfo = new DiskInfo.Builder()
                .bsdName("disk1")
                .deviceProtocol("USB")
                .isInternal(false)
                .isRemovable(true)
                .mediaSize(16_000_000_000L)
                .build();

        assertEquals("disk1", diskInfo.bsdName());
        assertEquals("USB", diskInfo.deviceInfo().protocol());
        assertFalse(diskInfo.deviceInfo().isInternal());
        assertTrue(diskInfo.isExternal());
        assertTrue(diskInfo.mediaInfo().isRemovable());
        assertTrue(diskInfo.isUSB());
    }

    @Test
    void testFullDiskInfo() {
        DiskInfo diskInfo = new DiskInfo.Builder()
                .bsdName("disk1s1")
                .volumePath("/Volumes/MyDisk")
                .volumeName("MyDisk")
                .volumeKind("apfs")
                .volumeUUID("ABC-DEF")
                .deviceProtocol("USB")
                .deviceModel("My USB Device")
                .deviceVendor("ACME")
                .deviceRevision("1.0")
                .deviceUnit(1L)
                .isInternal(false)
                .isRemovable(true)
                .mediaSize(16_000_000_000L)
                .mediaBlockSize(512L)
                .isWritable(true)
                .isWholeDisk(false)
                .isEjectable(true)
                .isLeaf(true)
                .mediaType("Generic")
                .mediaContent("GUID_partition_scheme")
                .mediaUUID("123-456")
                .busName("USB")
                .busPath("some/path")
                .build();

        assertEquals("disk1s1", diskInfo.bsdName());

        // VolumeInfo
        assertNotNull(diskInfo.volumeInfo());
        assertEquals("/Volumes/MyDisk", diskInfo.volumeInfo().path());
        assertEquals("MyDisk", diskInfo.volumeInfo().name());
        assertEquals("apfs", diskInfo.volumeInfo().kind());
        assertEquals("ABC-DEF", diskInfo.volumeInfo().uuid());

        // DeviceInfo
        assertNotNull(diskInfo.deviceInfo());
        assertEquals("USB", diskInfo.deviceInfo().protocol());
        assertEquals("My USB Device", diskInfo.deviceInfo().model());
        assertEquals("ACME", diskInfo.deviceInfo().vendor());
        assertEquals("1.0", diskInfo.deviceInfo().revision());
        assertEquals(1L, diskInfo.deviceInfo().unit());
        assertFalse(diskInfo.deviceInfo().isInternal());

        // MediaInfo
        assertNotNull(diskInfo.mediaInfo());
        assertTrue(diskInfo.mediaInfo().isRemovable());
        assertEquals(16_000_000_000L, diskInfo.mediaInfo().mediaSize());
        assertEquals(512L, diskInfo.mediaInfo().mediaBlockSize());
        assertTrue(diskInfo.mediaInfo().isWritable());
        assertFalse(diskInfo.mediaInfo().isWholeDisk());
        assertTrue(diskInfo.mediaInfo().isEjectable());
        assertTrue(diskInfo.mediaInfo().isLeaf());
        assertEquals("Generic", diskInfo.mediaInfo().mediaType());
        assertEquals("GUID_partition_scheme", diskInfo.mediaInfo().mediaContent());
        assertEquals("123-456", diskInfo.mediaInfo().mediaUUID());

        // BusInfo
        assertNotNull(diskInfo.busInfo());
        assertEquals("USB", diskInfo.busInfo().name());
        assertEquals("some/path", diskInfo.busInfo().path());
    }

    @Test
    void testFormattedSize() {
        assertEquals("Unknown", new DiskInfo.Builder().build().getFormattedSize());
        assertEquals("512 B", new DiskInfo.Builder().mediaSize(512L).build().getFormattedSize());
        assertEquals("1.00 KB", new DiskInfo.Builder().mediaSize(1024L).build().getFormattedSize());
        assertEquals("1.50 KB", new DiskInfo.Builder().mediaSize(1536L).build().getFormattedSize());
        assertEquals("1.00 MB", new DiskInfo.Builder().mediaSize(1024L * 1024).build().getFormattedSize());
        assertEquals("1.50 MB", new DiskInfo.Builder().mediaSize((long) (1024L * 1024 * 1.5)).build().getFormattedSize());
        assertEquals("1.00 GB", new DiskInfo.Builder().mediaSize(1024L * 1024 * 1024).build().getFormattedSize());
        assertEquals("1.50 GB", new DiskInfo.Builder().mediaSize((long) (1024L * 1024 * 1024 * 1.5)).build().getFormattedSize());
    }

    @Test
    void testUSBDetection() {
        DiskInfo usb = new DiskInfo.Builder()
                .bsdName("disk1")
                .deviceProtocol("USB")
                .build();
        assertTrue(usb.isUSB());

        DiskInfo sata = new DiskInfo.Builder()
                .bsdName("disk2")
                .deviceProtocol("SATA")
                .build();
        assertFalse(sata.isUSB());
    }

    @Test
    void testExternalDetection() {
        DiskInfo external = new DiskInfo.Builder()
                .bsdName("disk1")
                .isInternal(false)
                .build();
        assertTrue(external.isExternal());

        DiskInfo internal = new DiskInfo.Builder()
                .bsdName("disk2")
                .isInternal(true)
                .build();
        assertFalse(internal.isExternal());
    }
}
