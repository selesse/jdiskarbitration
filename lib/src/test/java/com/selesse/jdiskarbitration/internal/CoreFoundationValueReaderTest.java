package com.selesse.jdiskarbitration.internal;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoreFoundationValueReaderTest {

    @Mock
    private CoreFoundation cf;

    private CoreFoundationValueReader reader;

    @BeforeEach
    void setUp() {
        reader = new CoreFoundationValueReader(cf);
    }

    @Test
    void getString_withNullKey_returnsNull() {
        String result = reader.getString(mock(Pointer.class), null);
        assertNull(result);

        result = reader.getString(mock(Pointer.class), Pointer.NULL);
        assertNull(result);
    }

    @Test
    void getString_withMissingValue_returnsNull() {
        Pointer dict = mock(Pointer.class);
        Pointer key = mock(Pointer.class);

        when(cf.CFDictionaryGetValue(dict, key)).thenReturn(null);

        String result = reader.getString(dict, key);
        assertNull(result);
    }

    @Test
    void getString_withTypeMismatch_returnsNull() {
        Pointer dict = mock(Pointer.class);
        Pointer key = mock(Pointer.class);
        Pointer value = mock(Pointer.class);

        when(cf.CFDictionaryGetValue(dict, key)).thenReturn(value);
        when(cf.CFGetTypeID(value)).thenReturn(123L);
        when(cf.CFStringGetTypeID()).thenReturn(456L);

        String result = reader.getString(dict, key);
        assertNull(result);
    }

    @Test
    void getString_withValidString_returnsValue() {
        Pointer dict = mock(Pointer.class);
        Pointer key = mock(Pointer.class);
        Pointer value = mock(Pointer.class);
        Pointer cString = mock(Pointer.class);

        when(cf.CFDictionaryGetValue(dict, key)).thenReturn(value);
        when(cf.CFGetTypeID(value)).thenReturn(100L);
        when(cf.CFStringGetTypeID()).thenReturn(100L);
        when(cf.CFStringGetCStringPtr(value, CoreFoundation.kCFStringEncodingUTF8)).thenReturn(cString);
        when(cString.getString(0, "UTF-8")).thenReturn("test string");

        String result = reader.getString(dict, key);
        assertEquals("test string", result);
    }

    @Test
    void getBoolean_withValidBoolean_returnsTrue() {
        Pointer dict = mock(Pointer.class);
        Pointer key = mock(Pointer.class);
        Pointer value = mock(Pointer.class);

        when(cf.CFDictionaryGetValue(dict, key)).thenReturn(value);
        when(cf.CFGetTypeID(value)).thenReturn(200L);
        when(cf.CFBooleanGetTypeID()).thenReturn(200L);
        when(cf.CFBooleanGetValue(value)).thenReturn(true);

        boolean result = reader.getBoolean(dict, key);
        assertTrue(result);
    }

    @Test
    void getBoolean_withNullKey_returnsFalse() {
        boolean result = reader.getBoolean(mock(Pointer.class), null);
        assertFalse(result);
    }

    @Test
    void getBooleanNullable_withNullKey_returnsNull() {
        Boolean result = reader.getBooleanNullable(mock(Pointer.class), null);
        assertNull(result);
    }

    @Test
    void getBooleanNullable_withTypeMismatch_returnsNull() {
        Pointer dict = mock(Pointer.class);
        Pointer key = mock(Pointer.class);
        Pointer value = mock(Pointer.class);

        when(cf.CFDictionaryGetValue(dict, key)).thenReturn(value);
        when(cf.CFGetTypeID(value)).thenReturn(200L);
        when(cf.CFBooleanGetTypeID()).thenReturn(999L);

        Boolean result = reader.getBooleanNullable(dict, key);
        assertNull(result);
    }

    @Test
    void getLong_withValidNumber_returnsValue() {
        Pointer dict = mock(Pointer.class);
        Pointer key = mock(Pointer.class);
        Pointer value = mock(Pointer.class);

        when(cf.CFDictionaryGetValue(dict, key)).thenReturn(value);
        when(cf.CFGetTypeID(value)).thenReturn(300L);
        when(cf.CFNumberGetTypeID()).thenReturn(300L);
        when(cf.CFNumberGetValue(eq(value), eq(CoreFoundation.kCFNumberSInt64Type), any(Memory.class)))
            .thenAnswer(invocation -> {
                Memory mem = invocation.getArgument(2);
                mem.setLong(0, 42L);
                return true;
            });

        Long result = reader.getLong(dict, key);
        assertEquals(42L, result);
    }

    @Test
    void getLong_withConversionFailure_returnsNull() {
        Pointer dict = mock(Pointer.class);
        Pointer key = mock(Pointer.class);
        Pointer value = mock(Pointer.class);

        when(cf.CFDictionaryGetValue(dict, key)).thenReturn(value);
        when(cf.CFGetTypeID(value)).thenReturn(300L);
        when(cf.CFNumberGetTypeID()).thenReturn(300L);
        when(cf.CFNumberGetValue(eq(value), eq(CoreFoundation.kCFNumberSInt64Type), any(Memory.class)))
            .thenReturn(false);

        Long result = reader.getLong(dict, key);
        assertNull(result);
    }

    @Test
    void getInteger_withValidNumber_returnsValue() {
        Pointer dict = mock(Pointer.class);
        Pointer key = mock(Pointer.class);
        Pointer value = mock(Pointer.class);

        when(cf.CFDictionaryGetValue(dict, key)).thenReturn(value);
        when(cf.CFGetTypeID(value)).thenReturn(300L);
        when(cf.CFNumberGetTypeID()).thenReturn(300L);
        when(cf.CFNumberGetValue(eq(value), eq(CoreFoundation.kCFNumberSInt32Type), any(Memory.class)))
            .thenAnswer(invocation -> {
                Memory mem = invocation.getArgument(2);
                mem.setInt(0, 123);
                return true;
            });

        Integer result = reader.getInteger(dict, key);
        assertEquals(123, result);
    }

    @Test
    void getUUID_withValidUUID_returnsString() {
        Pointer dict = mock(Pointer.class);
        Pointer key = mock(Pointer.class);
        Pointer uuidRef = mock(Pointer.class);
        Pointer uuidString = mock(Pointer.class);
        Pointer cString = mock(Pointer.class);

        when(cf.CFDictionaryGetValue(dict, key)).thenReturn(uuidRef);
        when(cf.CFGetTypeID(uuidRef)).thenReturn(400L);
        when(cf.CFUUIDGetTypeID()).thenReturn(400L);
        when(cf.CFUUIDCreateString(null, uuidRef)).thenReturn(uuidString);
        when(cf.CFGetTypeID(uuidString)).thenReturn(100L);
        when(cf.CFStringGetTypeID()).thenReturn(100L);
        when(cf.CFStringGetCStringPtr(uuidString, CoreFoundation.kCFStringEncodingUTF8)).thenReturn(cString);
        when(cString.getString(0, "UTF-8")).thenReturn("550e8400-e29b-41d4-a716-446655440000");

        String result = reader.getUUID(dict, key);
        assertEquals("550e8400-e29b-41d4-a716-446655440000", result);

        verify(cf).CFRelease(uuidString);
    }

    @Test
    void getVolumePath_withValidURL_returnsPath() {
        Pointer dict = mock(Pointer.class);
        Pointer key = mock(Pointer.class);
        Pointer urlRef = mock(Pointer.class);
        Pointer pathString = mock(Pointer.class);
        Pointer cString = mock(Pointer.class);

        when(cf.CFDictionaryGetValue(dict, key)).thenReturn(urlRef);
        when(cf.CFGetTypeID(urlRef)).thenReturn(500L);
        when(cf.CFURLGetTypeID()).thenReturn(500L);
        when(cf.CFURLCopyFileSystemPath(urlRef, CoreFoundation.kCFURLPOSIXPathStyle)).thenReturn(pathString);
        when(cf.CFGetTypeID(pathString)).thenReturn(100L);
        when(cf.CFStringGetTypeID()).thenReturn(100L);
        when(cf.CFStringGetCStringPtr(pathString, CoreFoundation.kCFStringEncodingUTF8)).thenReturn(cString);
        when(cString.getString(0, "UTF-8")).thenReturn("/Volumes/MyDisk");

        String result = reader.getVolumePath(dict, key);
        assertEquals("/Volumes/MyDisk", result);

        verify(cf).CFRelease(pathString);
    }

    @Test
    void getValue_withException_returnsNull() {
        Pointer dict = mock(Pointer.class);
        Pointer key = mock(Pointer.class);
        Pointer value = mock(Pointer.class);

        when(cf.CFDictionaryGetValue(dict, key)).thenReturn(value);
        when(cf.CFGetTypeID(value)).thenReturn(100L);
        when(cf.CFStringGetTypeID()).thenReturn(100L);
        when(cf.CFStringGetCStringPtr(any(), anyInt())).thenThrow(new RuntimeException("Test exception"));

        String result = reader.getString(dict, key);
        assertNull(result);
    }
}
