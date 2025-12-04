package com.selesse.jdiskarbitration.internal;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;

interface CoreFoundation extends Library {
    CoreFoundation INSTANCE = Native.load("CoreFoundation", CoreFoundation.class);

    Pointer CFRunLoopGetCurrent();

    void CFRunLoopRun();

    void CFRunLoopStop(Pointer rl);

    Pointer CFDictionaryGetValue(Pointer dict, Pointer key);

    Pointer CFStringGetCStringPtr(Pointer theString, int encoding);

    boolean CFStringGetCString(Pointer theString, byte[] buffer, long bufferSize, int encoding);

    long CFStringGetLength(Pointer theString);

    long CFStringGetMaximumSizeForEncoding(long length, int encoding);

    int kCFStringEncodingUTF8 = 0x08000100;

    void CFRelease(Pointer cf);

    boolean CFBooleanGetValue(Pointer booleanRef);

    boolean CFNumberGetValue(Pointer number, int theType, Pointer valuePtr);

    int kCFNumberSInt32Type = 3;
    int kCFNumberSInt64Type = 4;

    // Using a method to get the pointer to the global constant after the library is loaded
    // This avoids the 'incompatible types' error for static final fields initialized with Native.findSymbol directly
    default Pointer getkCFRunLoopDefaultMode() {
        NativeLibrary lib = NativeLibrary.getInstance("CoreFoundation");
        Pointer symbol = lib.getGlobalVariableAddress("kCFRunLoopDefaultMode");
        if (symbol == null) {
            throw new IllegalStateException("Could not find symbol kCFRunLoopDefaultMode in CoreFoundation framework.");
        }
        return symbol.getPointer(0);
    }

    Pointer CFURLCopyFileSystemPath(Pointer url, int pathStyle);
    long CFURLGetTypeID();

    int kCFURLPOSIXPathStyle = 0;

    // UUID functions
    Pointer CFUUIDCreateString(Pointer allocator, Pointer uuid);
    long CFUUIDGetTypeID();

    // Type checking functions
    long CFGetTypeID(Pointer cf);
    long CFStringGetTypeID();
    long CFNumberGetTypeID();
    long CFBooleanGetTypeID();
    long CFDictionaryGetTypeID();
    long CFDataGetTypeID();
}
