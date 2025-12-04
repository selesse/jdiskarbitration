package com.selesse.jdiskarbitration.internal;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * Utility class for reading and converting values from CoreFoundation dictionaries.
 * Handles type checking and safe conversion of CF types to Java types.
 */
class CoreFoundationValueReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoreFoundationValueReader.class);

    private final CoreFoundation cf;

    CoreFoundationValueReader(CoreFoundation cf) {
        this.cf = cf;
    }

    private <T> T getValue(Pointer dict, Pointer key, long expectedTypeID, Function<Pointer, T> converter) {
        if (key == null || key == Pointer.NULL) {
            return null;
        }
        try {
            Pointer ref = cf.CFDictionaryGetValue(dict, key);
            if (ref == null || ref == Pointer.NULL) {
                return null;
            }

            long actualTypeID = cf.CFGetTypeID(ref);
            if (actualTypeID != expectedTypeID) {
                LOGGER.debug("Type mismatch: actual={}, expected={}", actualTypeID, expectedTypeID);
                return null;
            }

            return converter.apply(ref);
        } catch (Exception e) {
            LOGGER.debug("Failed to get value for key", e);
            return null;
        }
    }

    /**
     * Reads a string value from a CFDictionary.
     * Returns null if the key doesn't exist or the value is not a CFString.
     */
    String getString(Pointer dict, Pointer key) {
        return getValue(dict, key, cf.CFStringGetTypeID(), this::convertToString);
    }

    /**
     * Reads a UUID string from a CFDictionary.
     * Converts CFUUIDRef to a string representation.
     */
    String getUUID(Pointer dict, Pointer key) {
        return getValue(dict, key, cf.CFUUIDGetTypeID(), uuidRef -> {
            Pointer uuidString = cf.CFUUIDCreateString(null, uuidRef);
            if (uuidString == null || uuidString == Pointer.NULL) {
                return null;
            }
            try {
                return convertToString(uuidString);
            } finally {
                cf.CFRelease(uuidString);
            }
        });
    }

    /**
     * Reads a boolean value from a CFDictionary.
     * Returns false if the key doesn't exist.
     */
    boolean getBoolean(Pointer dict, Pointer key) {
        Boolean value = getValue(dict, key, cf.CFBooleanGetTypeID(), cf::CFBooleanGetValue);
        return value != null && value;
    }

    /**
     * Reads a nullable Boolean value from a CFDictionary.
     * Returns null if the key doesn't exist, allowing distinction between absent and false.
     */
    Boolean getBooleanNullable(Pointer dict, Pointer key) {
        return getValue(dict, key, cf.CFBooleanGetTypeID(), cf::CFBooleanGetValue);
    }

    /**
     * Reads a Long value from a CFDictionary.
     * Returns null if the key doesn't exist or conversion fails.
     */
    Long getLong(Pointer dict, Pointer key) {
        return getValue(dict, key, cf.CFNumberGetTypeID(), ref -> {
            Memory mem = new Memory(8);
            boolean success = cf.CFNumberGetValue(ref, CoreFoundation.kCFNumberSInt64Type, mem);
            return success ? mem.getLong(0) : null;
        });
    }

    /**
     * Reads an Integer value from a CFDictionary.
     * Returns null if the key doesn't exist or conversion fails.
     */
    Integer getInteger(Pointer dict, Pointer key) {
        return getValue(dict, key, cf.CFNumberGetTypeID(), ref -> {
            Memory mem = new Memory(4);
            boolean success = cf.CFNumberGetValue(ref, CoreFoundation.kCFNumberSInt32Type, mem);
            return success ? mem.getInt(0) : null;
        });
    }

    /**
     * Reads a volume path from a CFDictionary.
     * The value is a CFURL which needs special handling.
     */
    String getVolumePath(Pointer dict, Pointer key) {
        return getValue(dict, key, cf.CFURLGetTypeID(), volumePathRef -> {
            Pointer cfStringPath = cf.CFURLCopyFileSystemPath(volumePathRef, CoreFoundation.kCFURLPOSIXPathStyle);
            if (cfStringPath == null || cfStringPath == Pointer.NULL) {
                return null;
            }
            try {
                return convertToString(cfStringPath);
            } finally {
                cf.CFRelease(cfStringPath);
            }
        });
    }

    /**
     * Converts a CFString pointer to a Java String.
     * Performs type checking to ensure the pointer is actually a CFString.
     */
    private String convertToString(Pointer pointer) {
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
}
