package com.selesse.jdiskarbitration;

public record DiskInfo(
    String bsdName,
    VolumeInfo volumeInfo,
    DeviceInfo deviceInfo,
    MediaInfo mediaInfo,
    BusInfo busInfo
) {

    // Nested Records
    public record VolumeInfo(
        String path,
        String name,
        String kind,
        String uuid,
        Boolean mountable,
        Boolean network,
        String type
    ) {}
    public record DeviceInfo(
        String protocol,
        String model,
        String vendor,
        String revision,
        Long unit,
        boolean isInternal,
        String guid,
        String path,
        Boolean tdmLocked
    ) {}
    public record MediaInfo(
        boolean isRemovable,
        Long mediaSize,
        Long mediaBlockSize,
        boolean isWritable,
        boolean isWholeDisk,
        boolean isEjectable,
        boolean isLeaf,
        String mediaType,
        String mediaContent,
        String mediaUUID,
        Integer bsdMajor,
        Integer bsdMinor,
        String bsdName,
        Integer bsdUnit,
        String icon,
        String kind,
        String name,
        String path,
        Boolean encrypted,
        Integer encryptionDetail
    ) {}
    public record BusInfo(String name, String path) {}

    public boolean isExternal() { return !deviceInfo.isInternal(); }
    public boolean isUSB() { return "USB".equalsIgnoreCase(deviceInfo.protocol()); }

    public String getFormattedSize() {
        if (mediaInfo.mediaSize() == null) return "Unknown";
        long size = mediaInfo.mediaSize();
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.2f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.2f MB", size / (1024.0 * 1024));
        return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
    }

    public static class Builder {
        private String bsdName;
        private String volumePath;
        private String volumeName;
        private String volumeKind;
        private String volumeUUID;
        private Boolean volumeMountable;
        private Boolean volumeNetwork;
        private String volumeType;
        private String deviceProtocol;
        private String deviceModel;
        private String deviceVendor;
        private String deviceRevision;
        private Long deviceUnit;
        private boolean isInternal;
        private String deviceGuid;
        private String devicePath;
        private Boolean deviceTdmLocked;
        private boolean isRemovable;
        private Long mediaSize;
        private Long mediaBlockSize;
        private boolean isWritable;
        private boolean isWholeDisk;
        private boolean isEjectable;
        private boolean isLeaf;
        private String mediaType;
        private String mediaContent;
        private String mediaUUID;
        private Integer mediaBsdMajor;
        private Integer mediaBsdMinor;
        private String mediaBsdName;
        private Integer mediaBsdUnit;
        private String mediaIcon;
        private String mediaKind;
        private String mediaName;
        private String mediaPath;
        private Boolean mediaEncrypted;
        private Integer mediaEncryptionDetail;
        private String busName;
        private String busPath;

        public Builder bsdName(String bsdName) { this.bsdName = bsdName; return this; }
        public Builder volumePath(String volumePath) { this.volumePath = volumePath; return this; }
        public Builder volumeName(String volumeName) { this.volumeName = volumeName; return this; }
        public Builder volumeKind(String volumeKind) { this.volumeKind = volumeKind; return this; }
        public Builder volumeUUID(String volumeUUID) { this.volumeUUID = volumeUUID; return this; }
        public Builder volumeMountable(Boolean volumeMountable) { this.volumeMountable = volumeMountable; return this; }
        public Builder volumeNetwork(Boolean volumeNetwork) { this.volumeNetwork = volumeNetwork; return this; }
        public Builder volumeType(String volumeType) { this.volumeType = volumeType; return this; }
        public Builder deviceProtocol(String deviceProtocol) { this.deviceProtocol = deviceProtocol; return this; }
        public Builder deviceModel(String deviceModel) { this.deviceModel = deviceModel; return this; }
        public Builder deviceVendor(String deviceVendor) { this.deviceVendor = deviceVendor; return this; }
        public Builder deviceRevision(String deviceRevision) { this.deviceRevision = deviceRevision; return this; }
        public Builder deviceUnit(Long deviceUnit) { this.deviceUnit = deviceUnit; return this; }
        public Builder isInternal(boolean isInternal) { this.isInternal = isInternal; return this; }
        public Builder deviceGuid(String deviceGuid) { this.deviceGuid = deviceGuid; return this; }
        public Builder devicePath(String devicePath) { this.devicePath = devicePath; return this; }
        public Builder deviceTdmLocked(Boolean deviceTdmLocked) { this.deviceTdmLocked = deviceTdmLocked; return this; }
        public Builder isRemovable(boolean isRemovable) { this.isRemovable = isRemovable; return this; }
        public Builder mediaSize(Long mediaSize) { this.mediaSize = mediaSize; return this; }
        public Builder mediaBlockSize(Long mediaBlockSize) { this.mediaBlockSize = mediaBlockSize; return this; }
        public Builder isWritable(boolean isWritable) { this.isWritable = isWritable; return this; }
        public Builder isWholeDisk(boolean isWholeDisk) { this.isWholeDisk = isWholeDisk; return this; }
        public Builder isEjectable(boolean isEjectable) { this.isEjectable = isEjectable; return this; }
        public Builder isLeaf(boolean isLeaf) { this.isLeaf = isLeaf; return this; }
        public Builder mediaType(String mediaType) { this.mediaType = mediaType; return this; }
        public Builder mediaContent(String mediaContent) { this.mediaContent = mediaContent; return this; }
        public Builder mediaUUID(String mediaUUID) { this.mediaUUID = mediaUUID; return this; }
        public Builder mediaBsdMajor(Integer mediaBsdMajor) { this.mediaBsdMajor = mediaBsdMajor; return this; }
        public Builder mediaBsdMinor(Integer mediaBsdMinor) { this.mediaBsdMinor = mediaBsdMinor; return this; }
        public Builder mediaBsdName(String mediaBsdName) { this.mediaBsdName = mediaBsdName; return this; }
        public Builder mediaBsdUnit(Integer mediaBsdUnit) { this.mediaBsdUnit = mediaBsdUnit; return this; }
        public Builder mediaIcon(String mediaIcon) { this.mediaIcon = mediaIcon; return this; }
        public Builder mediaKind(String mediaKind) { this.mediaKind = mediaKind; return this; }
        public Builder mediaName(String mediaName) { this.mediaName = mediaName; return this; }
        public Builder mediaPath(String mediaPath) { this.mediaPath = mediaPath; return this; }
        public Builder mediaEncrypted(Boolean mediaEncrypted) { this.mediaEncrypted = mediaEncrypted; return this; }
        public Builder mediaEncryptionDetail(Integer mediaEncryptionDetail) { this.mediaEncryptionDetail = mediaEncryptionDetail; return this; }
        public Builder busName(String busName) { this.busName = busName; return this; }
        public Builder busPath(String busPath) { this.busPath = busPath; return this; }

        public DiskInfo build() {
            var volumeInfo = new DiskInfo.VolumeInfo(
                volumePath, volumeName, volumeKind, volumeUUID,
                volumeMountable, volumeNetwork, volumeType
            );
            var deviceInfo = new DiskInfo.DeviceInfo(
                deviceProtocol, deviceModel, deviceVendor, deviceRevision, deviceUnit, isInternal,
                deviceGuid, devicePath, deviceTdmLocked
            );
            var mediaInfo = new DiskInfo.MediaInfo(
                isRemovable, mediaSize, mediaBlockSize, isWritable, isWholeDisk, isEjectable,
                isLeaf, mediaType, mediaContent, mediaUUID,
                mediaBsdMajor, mediaBsdMinor, mediaBsdName, mediaBsdUnit,
                mediaIcon, mediaKind, mediaName, mediaPath,
                mediaEncrypted, mediaEncryptionDetail
            );
            var busInfo = new DiskInfo.BusInfo(busName, busPath);
            return new DiskInfo(bsdName, volumeInfo, deviceInfo, mediaInfo, busInfo);
        }
    }
}
