# jdiskarbitration

A Java library for monitoring disk events on macOS using the DiskArbitration framework.

## Features

- 🔌 Monitor USB device mount/unmount events
- 📀 Track all disk appearance/disappearance events
- 🎯 Powerful filtering system (USB-only, external-only, size-based, etc.)
- 📊 Comprehensive disk metadata (vendor, model, size, filesystem type, etc.)
- ⚡ Lightweight JNA-based implementation

## Installation

### Gradle

```gradle
dependencies {
    implementation 'com.selesse:jdiskarbitration:1.0.0'
}
```

## Quick Start

### Basic Usage

```java
// Use DiskEventAdapter to only implement the events you care about
DiskEventManager manager = DiskEventManager.builder()
    .listener(new DiskEventAdapter() {
        @Override
        public void onDiskMounted(DiskInfo diskInfo) {
            System.out.println("Disk mounted: " + diskInfo.volumeInfo().name() +
                             " at " + diskInfo.volumeInfo().path());
        }

        @Override
        public void onDiskUnmounted(DiskInfo diskInfo) {
            System.out.println("Disk unmounted: " + diskInfo.bsdName());
        }
    })
    .build();

manager.start();

// Resources are automatically cleaned up on JVM shutdown
// Or manually: manager.stop();
```

**Note**: The manager automatically registers a shutdown hook to clean up resources when the JVM exits. You don't need to manually add shutdown hooks unless you want custom cleanup behavior.

### Using the Builder with Filters

```java
DiskEventManager manager = DiskEventManager.builder()
        .externalOnly()              // Only external devices
        .usbOnly()                   // Only USB protocol
        .minSize(1024 * 1024)        // At least 1MB
        .listener(myListener)
        .build();

manager.start();
```

## Filtering Options

The builder provides powerful filtering capabilities:

```java
DiskEventManager.builder()
    // Convenience filters
    .externalOnly()                  // External devices only
    .usbOnly()                       // USB protocol only
    .removableOnly()                 // Removable media only
    .ejectableOnly()                 // Ejectable disks only
    .writableOnly()                  // Writable disks only

    // Size filters
    .minSize(1024 * 1024 * 1024)    // Minimum 1GB
    .maxSize(128L * 1024 * 1024 * 1024) // Maximum 128GB

    // Property filters
    .protocol("USB")                 // Specific protocol
    .volumeKind("exfat")            // Specific filesystem

    // Custom filters
    .filter(info -> info.getVolumeName() != null)
    .filter(info -> !info.getVolumeName().startsWith("Time Machine"))

    .listener(myListener)
    .build();
```

## Available Disk Information

The `DiskInfo` record and its nested types provide disk metadata.

### DiskInfo Record
- `bsdName()` - BSD device name (e.g., "disk2s1")

### VolumeInfo Record (`volumeInfo()`)
- `path()` - Mount path (e.g., "/Volumes/USB Drive")
- `name()` - Volume name (e.g., "USB Drive")
- `kind()` - Filesystem type (e.g., "exfat", "msdos", "apfs")
- `uuid()` - Volume UUID
- `mountable()` - Whether volume is mountable (Boolean, may be null)
- `network()` - Whether volume is on network (Boolean, may be null)
- `type()` - Volume type

### DeviceInfo Record (`deviceInfo()`)
- `protocol()` - Protocol (e.g., "USB", "SATA", "Virtual Interface")
- `model()` - Device model
- `vendor()` - Device vendor/manufacturer
- `revision()` - Firmware revision
- `unit()` - Device unit number
- `isInternal()` - Whether device is internal
- `guid()` - Device GUID
- `path()` - Device path
- `tdmLocked()` - Whether Target Disk Mode is locked (Boolean, may be null)

### MediaInfo Record (`mediaInfo()`)
- `mediaSize()` - Size in bytes
- `mediaBlockSize()` - Block size
- `isRemovable()` - Whether media is removable
- `isEjectable()` - Whether disk can be ejected
- `isWritable()` - Whether disk is writable
- `isWholeDisk()` - Whether this is whole disk vs partition
- `isLeaf()` - Whether this is a leaf node
- `mediaType()` - Media type
- `mediaContent()` - Media content identifier
- `mediaUUID()` - Media UUID
- `bsdMajor()` - BSD major number
- `bsdMinor()` - BSD minor number
- `bsdName()` - BSD name
- `bsdUnit()` - BSD unit number
- `icon()` - Icon bundle identifier (e.g., "com.apple.finder")
- `kind()` - Media kind
- `name()` - Media name
- `path()` - Media path
- `encrypted()` - Whether media is encrypted (Boolean, may be null)
- `encryptionDetail()` - Encryption detail code (Integer, e.g., 256 for AES-256)

### BusInfo Record (`busInfo()`)
- `name()` - Bus name (e.g., "USB")
- `path()` - Bus path

### Convenience Methods on DiskInfo
- `isExternal()` - Returns `!deviceInfo().isInternal()`
- `isUSB()` - Returns true if device protocol is "USB"
- `getFormattedSize()` - Human-readable size (e.g., "16.37 GB")

## Examples

### Monitor Only USB Flash Drives

```java
DiskEventManager manager = DiskEventManager.builder()
        .usbOnly()
        .removableOnly()
        .minSize(1024 * 1024)  // At least 1MB
        .listener(new DiskEventAdapter() {
            @Override
            public void onDiskMounted(DiskInfo info) {
                System.out.printf("USB drive mounted: %s (%s) at %s%n",
                    info.volumeInfo().name(),
                    info.getFormattedSize(),
                    info.volumeInfo().path());
            }
        })
        .build();

manager.start();
```

### Monitor External Hard Drives (Not USB Sticks)

```java
DiskEventManager manager = DiskEventManager.builder()
        .externalOnly()
        .filter(info -> !info.mediaInfo().isRemovable())  // Not removable (hard drives)
        .minSize(100L * 1024 * 1024 * 1024)   // At least 100GB
        .listener(myListener)
        .build();
```

### Custom Filtering Logic

```java
DiskEventManager manager = DiskEventManager.builder()
        .filter(info -> {
            // Only exFAT or FAT32 filesystems
            String kind = info.volumeInfo().kind();
            return "exfat".equalsIgnoreCase(kind) ||
                   "msdos".equalsIgnoreCase(kind);
        })
        .filter(info -> {
            // Not Time Machine backups
            String name = info.volumeInfo().name();
            return name == null || !name.contains("Time Machine");
        })
        .listener(myListener)
        .build();
```
