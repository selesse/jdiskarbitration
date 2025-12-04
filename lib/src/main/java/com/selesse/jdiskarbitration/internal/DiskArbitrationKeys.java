package com.selesse.jdiskarbitration.internal;

import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;

// https://developer.apple.com/documentation/diskarbitration/diskarbitration-constants
class DiskArbitrationKeys {

    // Volume information
    public final Pointer kDADiskDescriptionVolumePathKey;
    public final Pointer kDADiskDescriptionVolumeNameKey;
    public final Pointer kDADiskDescriptionVolumeKindKey;
    public final Pointer kDADiskDescriptionVolumeUUIDKey;
    public final Pointer kDADiskDescriptionVolumeMountableKey;
    public final Pointer kDADiskDescriptionVolumeNetworkKey;
    public final Pointer kDADiskDescriptionVolumeTypeKey;

    // Device information
    public final Pointer kDADiskDescriptionDeviceProtocolKey;
    public final Pointer kDADiskDescriptionDeviceInternalKey;
    public final Pointer kDADiskDescriptionDeviceModelKey;
    public final Pointer kDADiskDescriptionDeviceVendorKey;
    public final Pointer kDADiskDescriptionDeviceRevisionKey;
    public final Pointer kDADiskDescriptionDeviceUnitKey;
    public final Pointer kDADiskDescriptionDeviceGUIDKey;
    public final Pointer kDADiskDescriptionDevicePathKey;
    public final Pointer kDADiskDescriptionDeviceTDMLockedKey;

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
    public final Pointer kDADiskDescriptionMediaBSDMajorKey;
    public final Pointer kDADiskDescriptionMediaBSDMinorKey;
    public final Pointer kDADiskDescriptionMediaBSDNameKey;
    public final Pointer kDADiskDescriptionMediaBSDUnitKey;
    public final Pointer kDADiskDescriptionMediaIconKey;
    public final Pointer kDADiskDescriptionMediaKindKey;
    public final Pointer kDADiskDescriptionMediaNameKey;
    public final Pointer kDADiskDescriptionMediaPathKey;
    public final Pointer kDADiskDescriptionMediaEncryptedKey;
    public final Pointer kDADiskDescriptionMediaEncryptionDetailKey;

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
        this.kDADiskDescriptionVolumeMountableKey = getSymbol(lib, "kDADiskDescriptionVolumeMountableKey");
        this.kDADiskDescriptionVolumeNetworkKey = getSymbol(lib, "kDADiskDescriptionVolumeNetworkKey");
        this.kDADiskDescriptionVolumeTypeKey = getSymbol(lib, "kDADiskDescriptionVolumeTypeKey");

        // Device information
        this.kDADiskDescriptionDeviceProtocolKey = getSymbol(lib, "kDADiskDescriptionDeviceProtocolKey");
        this.kDADiskDescriptionDeviceInternalKey = getSymbol(lib, "kDADiskDescriptionDeviceInternalKey");
        this.kDADiskDescriptionDeviceModelKey = getSymbol(lib, "kDADiskDescriptionDeviceModelKey");
        this.kDADiskDescriptionDeviceVendorKey = getSymbol(lib, "kDADiskDescriptionDeviceVendorKey");
        this.kDADiskDescriptionDeviceRevisionKey = getSymbol(lib, "kDADiskDescriptionDeviceRevisionKey");
        this.kDADiskDescriptionDeviceUnitKey = getSymbol(lib, "kDADiskDescriptionDeviceUnitKey");
        this.kDADiskDescriptionDeviceGUIDKey = getSymbol(lib, "kDADiskDescriptionDeviceGUIDKey");
        this.kDADiskDescriptionDevicePathKey = getSymbol(lib, "kDADiskDescriptionDevicePathKey");
        this.kDADiskDescriptionDeviceTDMLockedKey = getSymbol(lib, "kDADiskDescriptionDeviceTDMLockedKey");

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
        this.kDADiskDescriptionMediaBSDMajorKey = getSymbol(lib, "kDADiskDescriptionMediaBSDMajorKey");
        this.kDADiskDescriptionMediaBSDMinorKey = getSymbol(lib, "kDADiskDescriptionMediaBSDMinorKey");
        this.kDADiskDescriptionMediaBSDNameKey = getSymbol(lib, "kDADiskDescriptionMediaBSDNameKey");
        this.kDADiskDescriptionMediaBSDUnitKey = getSymbol(lib, "kDADiskDescriptionMediaBSDUnitKey");
        this.kDADiskDescriptionMediaIconKey = getSymbol(lib, "kDADiskDescriptionMediaIconKey");
        this.kDADiskDescriptionMediaKindKey = getSymbol(lib, "kDADiskDescriptionMediaKindKey");
        this.kDADiskDescriptionMediaNameKey = getSymbol(lib, "kDADiskDescriptionMediaNameKey");
        this.kDADiskDescriptionMediaPathKey = getSymbol(lib, "kDADiskDescriptionMediaPathKey");
        this.kDADiskDescriptionMediaEncryptedKey = getSymbol(lib, "kDADiskDescriptionMediaEncryptedKey");
        this.kDADiskDescriptionMediaEncryptionDetailKey = getSymbol(lib, "kDADiskDescriptionMediaEncryptionDetailKey");

        // Bus information
        this.kDADiskDescriptionBusNameKey = getSymbol(lib, "kDADiskDescriptionBusNameKey");
        this.kDADiskDescriptionBusPathKey = getSymbol(lib, "kDADiskDescriptionBusPathKey");
    }

    private Pointer getSymbol(NativeLibrary lib, String symbolName) {
        try {
            Pointer symbolAddress = lib.getGlobalVariableAddress(symbolName);
            if (symbolAddress == null || symbolAddress == Pointer.NULL) {
                return null;
            }
            // Dereference the symbol to get the actual CFStringRef value
            return symbolAddress.getPointer(0);
        } catch (Exception e) {
            // Symbol doesn't exist in this version of macOS
            return null;
        }
    }

    public static final DiskArbitrationKeys INSTANCE = new DiskArbitrationKeys();
}
