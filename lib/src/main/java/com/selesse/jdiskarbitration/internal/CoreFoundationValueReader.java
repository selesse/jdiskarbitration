package com.selesse.jdiskarbitration.internal;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

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

    /**
     * Reads a string value from a CFDictionary.
     * Returns null if the key doesn't exist or the value is not a CFString.
     */
    String getString(Pointer dict, Pointer key) {
        if (key == null || key == Pointer.NULL) {
            return null;
        }
        try {
            Pointer ref = cf.CFDictionaryGetValue(dict, key);
            if (ref == null || ref == Pointer.NULL) {
                return null;
            }
            return convertToString(ref);
        } catch (Exception e) {
            LOGGER.debug("Failed to get string value for key", e);
            return null;
        }
    }

    /**
     * Reads a UUID string from a CFDictionary.
     * Converts CFUUIDRef to a string representation.
     */
    String getUUID(Pointer dict, Pointer key) {
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
                return convertToString(uuidString);
            } finally {
                cf.CFRelease(uuidString); // Release the created string
            }
        } catch (Exception e) {
            LOGGER.debug("Failed to get UUID value for key", e);
            return null;
        }
    }

    /**
     * Reads a boolean value from a CFDictionary.
     * Returns false if the key doesn't exist.
     */
    boolean getBoolean(Pointer dict, Pointer key) {
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

    /**
     * Reads a nullable Boolean value from a CFDictionary.
     * Returns null if the key doesn't exist, allowing distinction between absent and false.
     */
    Boolean getBooleanNullable(Pointer dict, Pointer key) {
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

    /**
     * Reads a Long value from a CFDictionary.
     * Returns null if the key doesn't exist or conversion fails.
     */
    Long getLong(Pointer dict, Pointer key) {
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

    /**
     * Reads an Integer value from a CFDictionary.
     * Returns null if the key doesn't exist or conversion fails.
     */
    Integer getInteger(Pointer dict, Pointer key) {
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

    /**
     * Reads a volume path from a CFDictionary.
     * The value is a CFURL which needs special handling.
     */
    String getVolumePath(Pointer dict, Pointer key) {
        if (key == null || key == Pointer.NULL) {
            return null;
        }
        try {
            Pointer volumePathRef = cf.CFDictionaryGetValue(dict, key);
            if (volumePathRef == null || volumePathRef == Pointer.NULL) {
                return null;
            }
            Pointer cfStringPath = cf.CFURLCopyFileSystemPath(volumePathRef, CoreFoundation.kCFURLPOSIXPathStyle);
            if (cfStringPath == null || cfStringPath == Pointer.NULL) {
                return null;
            }
            try {
                return convertToString(cfStringPath);
            } finally {
                cf.CFRelease(cfStringPath);
            }
        } catch (Exception e) {
            LOGGER.debug("Failed to get volume path for key", e);
            return null;
        }
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
