package com.selesse.jdiskarbitration;

public record DiskInfo(
    String bsdName,
    VolumeInfo volumeInfo,
    DeviceInfo deviceInfo,
    MediaInfo mediaInfo,
    BusInfo busInfo
) {

    // Nested Records
    public record VolumeInfo(String path, String name, String kind, String uuid) {}
    public record DeviceInfo(
        String protocol,
        String model,
        String vendor,
        String revision,
        Long unit,
        boolean isInternal
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
        String mediaUUID
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
        private String deviceProtocol;
        private String deviceModel;
        private String deviceVendor;
        private String deviceRevision;
        private Long deviceUnit;
        private boolean isInternal;
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
        private String busName;
        private String busPath;

        public Builder bsdName(String bsdName) { this.bsdName = bsdName; return this; }
        public Builder volumePath(String volumePath) { this.volumePath = volumePath; return this; }
        public Builder volumeName(String volumeName) { this.volumeName = volumeName; return this; }
        public Builder volumeKind(String volumeKind) { this.volumeKind = volumeKind; return this; }
        public Builder volumeUUID(String volumeUUID) { this.volumeUUID = volumeUUID; return this; }
        public Builder deviceProtocol(String deviceProtocol) { this.deviceProtocol = deviceProtocol; return this; }
        public Builder deviceModel(String deviceModel) { this.deviceModel = deviceModel; return this; }
        public Builder deviceVendor(String deviceVendor) { this.deviceVendor = deviceVendor; return this; }
        public Builder deviceRevision(String deviceRevision) { this.deviceRevision = deviceRevision; return this; }
        public Builder deviceUnit(Long deviceUnit) { this.deviceUnit = deviceUnit; return this; }
        public Builder isInternal(boolean isInternal) { this.isInternal = isInternal; return this; }
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
        public Builder busName(String busName) { this.busName = busName; return this; }
        public Builder busPath(String busPath) { this.busPath = busPath; return this; }

        public DiskInfo build() {
            var volumeInfo = new DiskInfo.VolumeInfo(volumePath, volumeName, volumeKind, volumeUUID);
            var deviceInfo = new DiskInfo.DeviceInfo(
                deviceProtocol, deviceModel, deviceVendor, deviceRevision, deviceUnit, isInternal
            );
            var mediaInfo = new DiskInfo.MediaInfo(
                isRemovable, mediaSize, mediaBlockSize, isWritable, isWholeDisk, isEjectable,
                isLeaf, mediaType, mediaContent, mediaUUID
            );
            var busInfo = new DiskInfo.BusInfo(busName, busPath);
            return new DiskInfo(bsdName, volumeInfo, deviceInfo, mediaInfo, busInfo);
        }
    }
}
