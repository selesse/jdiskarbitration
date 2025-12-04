package com.selesse.jdiskarbitration.internal;

import com.selesse.jdiskarbitration.DiskInfo;
import com.sun.jna.Pointer;

/**
 * Maps DiskArbitration DADisk objects to DiskInfo records.
 * Handles extraction and conversion of all disk properties from CoreFoundation dictionaries.
 */
class DiskInfoMapper {
    private final DiskArbitration da;
    private final CoreFoundation cf;
    private final CoreFoundationValueReader cfReader;
    private final DiskArbitrationKeys keys;

    DiskInfoMapper(DiskArbitration da, CoreFoundation cf, CoreFoundationValueReader cfReader) {
        this.da = da;
        this.cf = cf;
        this.cfReader = cfReader;
        this.keys = DiskArbitrationKeys.INSTANCE;
    }

    /**
     * Converts a DADisk pointer to a DiskInfo object.
     * Extracts all available disk properties from the DiskArbitration framework.
     *
     * @param daDisk Pointer to a DADisk object
     * @return DiskInfo with all available properties, or null if BSD name is unavailable
     */
    DiskInfo fromDADisk(Pointer daDisk) {
        Pointer bsdNamePtr = da.DADiskGetBSDName(daDisk);
        if (bsdNamePtr == null) {
            return null;
        }
        String bsdName = bsdNamePtr.getString(0);

        Pointer dict = da.DADiskCopyDescription(daDisk);
        if (dict == null) {
            return new DiskInfo.Builder().bsdName(bsdName).build();
        }

        try {
            return buildDiskInfo(bsdName, dict);
        } finally {
            cf.CFRelease(dict);
        }
    }

    /**
     * Builds a complete DiskInfo object from a BSD name and CFDictionary.
     * Maps all volume, device, media, and bus properties.
     */
    private DiskInfo buildDiskInfo(String bsdName, Pointer dict) {
        DiskInfo.Builder builder = new DiskInfo.Builder().bsdName(bsdName);

        mapVolumeProperties(builder, dict);
        mapDeviceProperties(builder, dict);
        mapMediaProperties(builder, dict);
        mapBusProperties(builder, dict);

        return builder.build();
    }

    /**
     * Maps volume-related properties from the dictionary to the builder.
     */
    private void mapVolumeProperties(DiskInfo.Builder builder, Pointer dict) {
        builder.volumePath(cfReader.getVolumePath(dict, keys.kDADiskDescriptionVolumePathKey));
        builder.volumeName(cfReader.getString(dict, keys.kDADiskDescriptionVolumeNameKey));
        builder.volumeKind(cfReader.getString(dict, keys.kDADiskDescriptionVolumeKindKey));
        builder.volumeUUID(cfReader.getUUID(dict, keys.kDADiskDescriptionVolumeUUIDKey));
        builder.volumeMountable(cfReader.getBooleanNullable(dict, keys.kDADiskDescriptionVolumeMountableKey));
        builder.volumeNetwork(cfReader.getBooleanNullable(dict, keys.kDADiskDescriptionVolumeNetworkKey));
        builder.volumeType(cfReader.getString(dict, keys.kDADiskDescriptionVolumeTypeKey));
    }

    /**
     * Maps device-related properties from the dictionary to the builder.
     */
    private void mapDeviceProperties(DiskInfo.Builder builder, Pointer dict) {
        builder.deviceProtocol(cfReader.getString(dict, keys.kDADiskDescriptionDeviceProtocolKey));
        builder.deviceModel(cfReader.getString(dict, keys.kDADiskDescriptionDeviceModelKey));
        builder.deviceVendor(cfReader.getString(dict, keys.kDADiskDescriptionDeviceVendorKey));
        builder.deviceRevision(cfReader.getString(dict, keys.kDADiskDescriptionDeviceRevisionKey));
        builder.deviceUnit(cfReader.getLong(dict, keys.kDADiskDescriptionDeviceUnitKey));
        builder.isInternal(cfReader.getBoolean(dict, keys.kDADiskDescriptionDeviceInternalKey));
        builder.deviceGuid(cfReader.getUUID(dict, keys.kDADiskDescriptionDeviceGUIDKey));
        builder.devicePath(cfReader.getString(dict, keys.kDADiskDescriptionDevicePathKey));
        builder.deviceTdmLocked(cfReader.getBooleanNullable(dict, keys.kDADiskDescriptionDeviceTDMLockedKey));
    }

    /**
     * Maps media-related properties from the dictionary to the builder.
     */
    private void mapMediaProperties(DiskInfo.Builder builder, Pointer dict) {
        builder.isRemovable(cfReader.getBoolean(dict, keys.kDADiskDescriptionMediaRemovableKey));
        builder.mediaSize(cfReader.getLong(dict, keys.kDADiskDescriptionMediaSizeKey));
        builder.mediaBlockSize(cfReader.getLong(dict, keys.kDADiskDescriptionMediaBlockSizeKey));
        builder.isWritable(cfReader.getBoolean(dict, keys.kDADiskDescriptionMediaWritableKey));
        builder.isWholeDisk(cfReader.getBoolean(dict, keys.kDADiskDescriptionMediaWholeKey));
        builder.isEjectable(cfReader.getBoolean(dict, keys.kDADiskDescriptionMediaEjectableKey));
        builder.isLeaf(cfReader.getBoolean(dict, keys.kDADiskDescriptionMediaLeafKey));
        builder.mediaType(cfReader.getString(dict, keys.kDADiskDescriptionMediaTypeKey));
        builder.mediaContent(cfReader.getString(dict, keys.kDADiskDescriptionMediaContentKey));
        builder.mediaUUID(cfReader.getUUID(dict, keys.kDADiskDescriptionMediaUUIDKey));
        builder.mediaBsdMajor(cfReader.getInteger(dict, keys.kDADiskDescriptionMediaBSDMajorKey));
        builder.mediaBsdMinor(cfReader.getInteger(dict, keys.kDADiskDescriptionMediaBSDMinorKey));
        builder.mediaBsdName(cfReader.getString(dict, keys.kDADiskDescriptionMediaBSDNameKey));
        builder.mediaBsdUnit(cfReader.getInteger(dict, keys.kDADiskDescriptionMediaBSDUnitKey));
        builder.mediaIcon(cfReader.getIconBundleIdentifier(dict, keys.kDADiskDescriptionMediaIconKey));
        builder.mediaKind(cfReader.getString(dict, keys.kDADiskDescriptionMediaKindKey));
        builder.mediaName(cfReader.getString(dict, keys.kDADiskDescriptionMediaNameKey));
        builder.mediaPath(cfReader.getString(dict, keys.kDADiskDescriptionMediaPathKey));
        builder.mediaEncrypted(cfReader.getBooleanNullable(dict, keys.kDADiskDescriptionMediaEncryptedKey));
        builder.mediaEncryptionDetail(cfReader.getInteger(dict, keys.kDADiskDescriptionMediaEncryptionDetailKey));
    }

    /**
     * Maps bus-related properties from the dictionary to the builder.
     */
    private void mapBusProperties(DiskInfo.Builder builder, Pointer dict) {
        builder.busName(cfReader.getString(dict, keys.kDADiskDescriptionBusNameKey));
        builder.busPath(cfReader.getString(dict, keys.kDADiskDescriptionBusPathKey));
    }
}
