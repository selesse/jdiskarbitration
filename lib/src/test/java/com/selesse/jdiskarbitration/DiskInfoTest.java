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

    @Test
    void testNewVolumeFields() {
        DiskInfo diskInfo = new DiskInfo.Builder()
                .bsdName("disk1s1")
                .volumePath("/Volumes/Test")
                .volumeName("Test")
                .volumeKind("apfs")
                .volumeUUID("ABC-123")
                .volumeMountable(true)
                .volumeNetwork(false)
                .volumeType("hfs")
                .build();

        assertNotNull(diskInfo.volumeInfo());
        assertEquals("/Volumes/Test", diskInfo.volumeInfo().path());
        assertEquals("Test", diskInfo.volumeInfo().name());
        assertEquals("apfs", diskInfo.volumeInfo().kind());
        assertEquals("ABC-123", diskInfo.volumeInfo().uuid());
        assertTrue(diskInfo.volumeInfo().mountable());
        assertFalse(diskInfo.volumeInfo().network());
        assertEquals("hfs", diskInfo.volumeInfo().type());
    }

    @Test
    void testNewDeviceFields() {
        DiskInfo diskInfo = new DiskInfo.Builder()
                .bsdName("disk2")
                .deviceProtocol("USB")
                .deviceModel("MyDrive")
                .deviceVendor("ACME")
                .deviceRevision("2.0")
                .deviceUnit(1L)
                .isInternal(false)
                .deviceGuid("GUID-456")
                .devicePath("/dev/disk2")
                .deviceTdmLocked(false)
                .build();

        assertNotNull(diskInfo.deviceInfo());
        assertEquals("USB", diskInfo.deviceInfo().protocol());
        assertEquals("MyDrive", diskInfo.deviceInfo().model());
        assertEquals("ACME", diskInfo.deviceInfo().vendor());
        assertEquals("2.0", diskInfo.deviceInfo().revision());
        assertEquals(1L, diskInfo.deviceInfo().unit());
        assertFalse(diskInfo.deviceInfo().isInternal());
        assertEquals("GUID-456", diskInfo.deviceInfo().guid());
        assertEquals("/dev/disk2", diskInfo.deviceInfo().path());
        assertFalse(diskInfo.deviceInfo().tdmLocked());
    }

    @Test
    void testNewMediaFields() {
        DiskInfo diskInfo = new DiskInfo.Builder()
                .bsdName("disk3")
                .isRemovable(true)
                .mediaSize(1000000000L)
                .mediaBlockSize(512L)
                .isWritable(true)
                .isWholeDisk(false)
                .isEjectable(true)
                .isLeaf(true)
                .mediaType("SSD")
                .mediaContent("GUID_partition_scheme")
                .mediaUUID("UUID-789")
                .mediaBsdMajor(1)
                .mediaBsdMinor(0)
                .mediaBsdName("disk3")
                .mediaBsdUnit(3)
                .mediaIcon("icon.png")
                .mediaKind("Physical")
                .mediaName("MySSD")
                .mediaPath("/dev/disk3")
                .mediaEncrypted(true)
                .mediaEncryptionDetail(256)
                .build();

        assertNotNull(diskInfo.mediaInfo());
        assertTrue(diskInfo.mediaInfo().isRemovable());
        assertEquals(1000000000L, diskInfo.mediaInfo().mediaSize());
        assertEquals(512L, diskInfo.mediaInfo().mediaBlockSize());
        assertTrue(diskInfo.mediaInfo().isWritable());
        assertFalse(diskInfo.mediaInfo().isWholeDisk());
        assertTrue(diskInfo.mediaInfo().isEjectable());
        assertTrue(diskInfo.mediaInfo().isLeaf());
        assertEquals("SSD", diskInfo.mediaInfo().mediaType());
        assertEquals("GUID_partition_scheme", diskInfo.mediaInfo().mediaContent());
        assertEquals("UUID-789", diskInfo.mediaInfo().mediaUUID());
        assertEquals(1, diskInfo.mediaInfo().bsdMajor());
        assertEquals(0, diskInfo.mediaInfo().bsdMinor());
        assertEquals("disk3", diskInfo.mediaInfo().bsdName());
        assertEquals(3, diskInfo.mediaInfo().bsdUnit());
        assertEquals("icon.png", diskInfo.mediaInfo().icon());
        assertEquals("Physical", diskInfo.mediaInfo().kind());
        assertEquals("MySSD", diskInfo.mediaInfo().name());
        assertEquals("/dev/disk3", diskInfo.mediaInfo().path());
        assertTrue(diskInfo.mediaInfo().encrypted());
        assertEquals(256, diskInfo.mediaInfo().encryptionDetail());
    }

    @Test
    void testNullableFieldsCanBeNull() {
        DiskInfo diskInfo = new DiskInfo.Builder()
                .bsdName("disk4")
                .volumeMountable(null)
                .volumeNetwork(null)
                .deviceTdmLocked(null)
                .mediaEncrypted(null)
                .build();

        assertNotNull(diskInfo.volumeInfo());
        assertNull(diskInfo.volumeInfo().mountable());
        assertNull(diskInfo.volumeInfo().network());

        assertNotNull(diskInfo.deviceInfo());
        assertNull(diskInfo.deviceInfo().tdmLocked());

        assertNotNull(diskInfo.mediaInfo());
        assertNull(diskInfo.mediaInfo().encrypted());
    }
}
