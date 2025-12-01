package com.selesse.jdiskarbitration.internal;

import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;

class DiskArbitrationKeys {

    // Volume information
    public final Pointer kDADiskDescriptionVolumePathKey;
    public final Pointer kDADiskDescriptionVolumeNameKey;
    public final Pointer kDADiskDescriptionVolumeKindKey;
    public final Pointer kDADiskDescriptionVolumeUUIDKey;

    // Device information
    public final Pointer kDADiskDescriptionDeviceProtocolKey;
    public final Pointer kDADiskDescriptionDeviceInternalKey;
    public final Pointer kDADiskDescriptionDeviceModelKey;
    public final Pointer kDADiskDescriptionDeviceVendorKey;
    public final Pointer kDADiskDescriptionDeviceRevisionKey;
    public final Pointer kDADiskDescriptionDeviceUnitKey;

    // Media information
    public final Pointer kDADiskDescriptionMediaRemovableKey;
    public final Pointer kDADiskDescriptionMediaSizeKey;
    public final Pointer kDADiskDescriptionMediaBlockSizeKey;
    public final Pointer kDADiskDescriptionMediaWritableKey;
    public final Pointer kDADiskDescriptionMediaWholeKey;
    public final Pointer kDADiskDescriptionMediaEjectableKey;
    public final Pointer kDADiskDescriptionMediaLeafKey;
    public final Pointer kDADiskDescriptionMediaTypeKey;
    public final Pointer kDADiskDescriptionMediaContentKey;
    public final Pointer kDADiskDescriptionMediaUUIDKey;

    // Bus information
    public final Pointer kDADiskDescriptionBusNameKey;
    public final Pointer kDADiskDescriptionBusPathKey;

    public DiskArbitrationKeys() {
        NativeLibrary lib = NativeLibrary.getInstance("DiskArbitration");

        // Volume information
        this.kDADiskDescriptionVolumePathKey = getSymbol(lib, "kDADiskDescriptionVolumePathKey");
        this.kDADiskDescriptionVolumeNameKey = getSymbol(lib, "kDADiskDescriptionVolumeNameKey");
        this.kDADiskDescriptionVolumeKindKey = getSymbol(lib, "kDADiskDescriptionVolumeKindKey");
        this.kDADiskDescriptionVolumeUUIDKey = getSymbol(lib, "kDADiskDescriptionVolumeUUIDKey");

        // Device information
        this.kDADiskDescriptionDeviceProtocolKey = getSymbol(lib, "kDADiskDescriptionDeviceProtocolKey");
        this.kDADiskDescriptionDeviceInternalKey = getSymbol(lib, "kDADiskDescriptionDeviceInternalKey");
        this.kDADiskDescriptionDeviceModelKey = getSymbol(lib, "kDADiskDescriptionDeviceModelKey");
        this.kDADiskDescriptionDeviceVendorKey = getSymbol(lib, "kDADiskDescriptionDeviceVendorKey");
        this.kDADiskDescriptionDeviceRevisionKey = getSymbol(lib, "kDADiskDescriptionDeviceRevisionKey");
        this.kDADiskDescriptionDeviceUnitKey = getSymbol(lib, "kDADiskDescriptionDeviceUnitKey");

        // Media information
        this.kDADiskDescriptionMediaRemovableKey = getSymbol(lib, "kDADiskDescriptionMediaRemovableKey");
        this.kDADiskDescriptionMediaSizeKey = getSymbol(lib, "kDADiskDescriptionMediaSizeKey");
        this.kDADiskDescriptionMediaBlockSizeKey = getSymbol(lib, "kDADiskDescriptionMediaBlockSizeKey");
        this.kDADiskDescriptionMediaWritableKey = getSymbol(lib, "kDADiskDescriptionMediaWritableKey");
        this.kDADiskDescriptionMediaWholeKey = getSymbol(lib, "kDADiskDescriptionMediaWholeKey");
        this.kDADiskDescriptionMediaEjectableKey = getSymbol(lib, "kDADiskDescriptionMediaEjectableKey");
        this.kDADiskDescriptionMediaLeafKey = getSymbol(lib, "kDADiskDescriptionMediaLeafKey");
        this.kDADiskDescriptionMediaTypeKey = getSymbol(lib, "kDADiskDescriptionMediaTypeKey");
        this.kDADiskDescriptionMediaContentKey = getSymbol(lib, "kDADiskDescriptionMediaContentKey");
        this.kDADiskDescriptionMediaUUIDKey = getSymbol(lib, "kDADiskDescriptionMediaUUIDKey");

        // Bus information
        this.kDADiskDescriptionBusNameKey = getSymbol(lib, "kDADiskDescriptionBusNameKey");
        this.kDADiskDescriptionBusPathKey = getSymbol(lib, "kDADiskDescriptionBusPathKey");
    }

    private Pointer getSymbol(NativeLibrary lib, String symbolName) {
        Pointer symbolAddress = lib.getGlobalVariableAddress(symbolName);
        if (symbolAddress == null || symbolAddress == Pointer.NULL) {
            throw new IllegalStateException("Could not find symbol " + symbolName + " in DiskArbitration framework.");
        }
        // Dereference the symbol to get the actual CFStringRef value
        return symbolAddress.getPointer(0);
    }

    public static final DiskArbitrationKeys INSTANCE = new DiskArbitrationKeys();
}
