package com.selesse.jdiskarbitration.internal;

import com.selesse.jdiskarbitration.DiskInfo;
import com.sun.jna.Pointer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiskInfoMapperTest {

    @Mock
    private DiskArbitration da;

    @Mock
    private CoreFoundation cf;

    @Mock
    private CoreFoundationValueReader cfReader;

    private DiskInfoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DiskInfoMapper(da, cf, cfReader);
    }

    @Test
    void fromDADisk_withNullBSDName_returnsNull() {
        Pointer disk = mock(Pointer.class);
        when(da.DADiskGetBSDName(disk)).thenReturn(null);

        DiskInfo result = mapper.fromDADisk(disk);
        assertNull(result);
    }

    @Test
    void fromDADisk_withNullDescription_returnsMinimalDiskInfo() {
        Pointer disk = mock(Pointer.class);
        Pointer bsdNamePtr = mock(Pointer.class);

        when(da.DADiskGetBSDName(disk)).thenReturn(bsdNamePtr);
        when(bsdNamePtr.getString(0)).thenReturn("disk1");
        when(da.DADiskCopyDescription(disk)).thenReturn(null);

        DiskInfo result = mapper.fromDADisk(disk);

        assertNotNull(result);
        assertEquals("disk1", result.bsdName());
        assertNotNull(result.volumeInfo());
        assertNotNull(result.deviceInfo());
        assertNotNull(result.mediaInfo());
        assertNotNull(result.busInfo());
    }

    @Test
    void fromDADisk_withFullDescription_mapsAllFields() {
        Pointer disk = mock(Pointer.class);
        Pointer bsdNamePtr = mock(Pointer.class);
        Pointer dict = mock(Pointer.class);

        // Setup BSD name
        when(da.DADiskGetBSDName(disk)).thenReturn(bsdNamePtr);
        when(bsdNamePtr.getString(0)).thenReturn("disk2s1");
        when(da.DADiskCopyDescription(disk)).thenReturn(dict);

        // Mock volume properties
        when(cfReader.getVolumePath(eq(dict), any())).thenReturn("/Volumes/TestDisk");
        when(cfReader.getString(eq(dict), any())).thenReturn("TestValue");
        when(cfReader.getUUID(eq(dict), any())).thenReturn("UUID-123");
        when(cfReader.getBooleanNullable(eq(dict), any())).thenReturn(true);

        // Mock device properties
        when(cfReader.getBoolean(eq(dict), any())).thenReturn(false);
        when(cfReader.getLong(eq(dict), any())).thenReturn(42L);

        // Mock media properties
        when(cfReader.getInteger(eq(dict), any())).thenReturn(10);
        when(cfReader.getIconBundleIdentifier(eq(dict), any())).thenReturn("com.apple.finder");

        DiskInfo result = mapper.fromDADisk(disk);

        assertNotNull(result);
        assertEquals("disk2s1", result.bsdName());

        // Verify volume info is populated
        assertNotNull(result.volumeInfo());
        assertEquals("/Volumes/TestDisk", result.volumeInfo().path());

        // Verify CFRelease was called for the dictionary
        verify(cf).CFRelease(dict);
    }

    @Test
    void fromDADisk_releasesResourcesEvenOnException() {
        Pointer disk = mock(Pointer.class);
        Pointer bsdNamePtr = mock(Pointer.class);
        Pointer dict = mock(Pointer.class);

        when(da.DADiskGetBSDName(disk)).thenReturn(bsdNamePtr);
        when(bsdNamePtr.getString(0)).thenReturn("disk3");
        when(da.DADiskCopyDescription(disk)).thenReturn(dict);

        // Cause an exception during mapping
        when(cfReader.getVolumePath(any(), any())).thenThrow(new RuntimeException("Test exception"));

        assertThrows(RuntimeException.class, () -> mapper.fromDADisk(disk));

        // Verify CFRelease is still called despite exception
        verify(cf).CFRelease(dict);
    }

    @Test
    void fromDADisk_mapsAllVolumeProperties() {
        Pointer disk = mock(Pointer.class);
        Pointer bsdNamePtr = mock(Pointer.class);
        Pointer dict = mock(Pointer.class);

        when(da.DADiskGetBSDName(disk)).thenReturn(bsdNamePtr);
        when(bsdNamePtr.getString(0)).thenReturn("disk1");
        when(da.DADiskCopyDescription(disk)).thenReturn(dict);

        // Setup specific volume property responses
        when(cfReader.getVolumePath(eq(dict), any())).thenReturn("/Volumes/MyDisk");
        when(cfReader.getString(eq(dict), any())).thenAnswer(invocation -> {
            // Simplified - in reality would check which key
            return "StringValue";
        });
        when(cfReader.getUUID(eq(dict), any())).thenReturn("550e8400-e29b-41d4-a716-446655440000");
        when(cfReader.getBooleanNullable(eq(dict), any())).thenReturn(true);
        when(cfReader.getBoolean(eq(dict), any())).thenReturn(false);
        when(cfReader.getLong(eq(dict), any())).thenReturn(100L);
        when(cfReader.getInteger(eq(dict), any())).thenReturn(5);
        when(cfReader.getIconBundleIdentifier(eq(dict), any())).thenReturn("com.apple.finder");

        DiskInfo result = mapper.fromDADisk(disk);

        assertNotNull(result);
        assertNotNull(result.volumeInfo());
        assertEquals("/Volumes/MyDisk", result.volumeInfo().path());

        // Verify the reader methods were called
        verify(cfReader, atLeastOnce()).getVolumePath(eq(dict), any());
        verify(cfReader, atLeastOnce()).getString(eq(dict), any());
        verify(cfReader, atLeastOnce()).getUUID(eq(dict), any());
        verify(cfReader, atLeastOnce()).getBooleanNullable(eq(dict), any());
        verify(cfReader, atLeastOnce()).getBoolean(eq(dict), any());
        verify(cfReader, atLeastOnce()).getLong(eq(dict), any());
        verify(cfReader, atLeastOnce()).getInteger(eq(dict), any());
        verify(cfReader, atLeastOnce()).getIconBundleIdentifier(eq(dict), any());
    }
}
