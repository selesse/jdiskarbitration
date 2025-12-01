package com.selesse.jdiskarbitration;

import com.selesse.jdiskarbitration.internal.DiskEventWatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Manager for monitoring disk events on macOS.
 *
 * <p>This class provides a clean API for monitoring disk appearance, disappearance,
 * mount, and unmount events on macOS using the DiskArbitration framework.
 *
 * <p>Example usage:
 * <pre>
 * DiskEventManager manager = DiskEventManager.builder()
 *     .usbOnly()
 *     .externalOnly()
 *     .listener(new DiskEventAdapter() {
 *         {@literal @}Override
 *         public void onDiskMounted(DiskInfo info) {
 *             System.out.println("Mounted: " + info.volumeInfo().name());
 *         }
 *     })
 *     .build();
 *
 * manager.start();
 * // Resources automatically cleaned up on JVM shutdown
 * </pre>
 */
public class DiskEventManager {
    private final DiskEventWatcher watcher;

    private DiskEventManager(DiskEventListener listener, List<Predicate<DiskInfo>> filters) {
        this.watcher = new DiskEventWatcher(listener, filters);
    }

    /**
     * Starts monitoring disk events.
     *
     * <p>This method:
     * <ul>
     *   <li>Starts the DiskArbitration event loop in a background thread</li>
     *   <li>Registers a shutdown hook to automatically clean up resources</li>
     *   <li>Begins invoking listener callbacks when disk events occur</li>
     * </ul>
     *
     * <p>The shutdown hook ensures proper cleanup even if {@link #stop()} is not called.
     */
    public void start() {
        watcher.start();
    }

    /**
     * Stops monitoring disk events and releases resources.
     *
     * <p>This method:
     * <ul>
     *   <li>Stops the DiskArbitration event loop</li>
     *   <li>Unregisters all callbacks</li>
     *   <li>Releases native resources</li>
     *   <li>Removes the shutdown hook (if not being called from it)</li>
     * </ul>
     *
     * <p>It is safe to call this method multiple times.
     */
    public void stop() {
        watcher.stop();
    }

    /**
     * Creates a new builder for configuring a DiskEventManager.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for creating a DiskEventManager with filters and listener.
     */
    public static class Builder {
        private DiskEventListener listener;
        private final List<Predicate<DiskInfo>> filters = new ArrayList<>();

        /**
         * Sets the listener that will receive disk event callbacks.
         *
         * @param listener the event listener (required)
         * @return this builder
         */
        public Builder listener(DiskEventListener listener) {
            this.listener = listener;
            return this;
        }

        /**
         * Adds a custom filter for disk events.
         *
         * <p>Only disks that pass all filters will trigger listener callbacks.
         *
         * @param filter the filter predicate
         * @return this builder
         */
        public Builder filter(Predicate<DiskInfo> filter) {
            this.filters.add(filter);
            return this;
        }

        /**
         * Filters to only external devices (not internal drives).
         *
         * @return this builder
         */
        public Builder externalOnly() {
            return filter(DiskInfo::isExternal);
        }

        /**
         * Filters to only USB devices.
         *
         * @return this builder
         */
        public Builder usbOnly() {
            return filter(DiskInfo::isUSB);
        }

        /**
         * Filters to only removable media.
         *
         * @return this builder
         */
        public Builder removableOnly() {
            return filter(info -> info.mediaInfo().isRemovable());
        }

        /**
         * Filters to only ejectable disks.
         *
         * @return this builder
         */
        public Builder ejectableOnly() {
            return filter(info -> info.mediaInfo().isEjectable());
        }

        /**
         * Filters to only writable disks.
         *
         * @return this builder
         */
        public Builder writableOnly() {
            return filter(info -> info.mediaInfo().isWritable());
        }

        /**
         * Filters to disks with at least the specified size.
         *
         * @param minBytes minimum size in bytes
         * @return this builder
         */
        public Builder minSize(long minBytes) {
            return filter(info -> info.mediaInfo().mediaSize() != null && info.mediaInfo().mediaSize() >= minBytes);
        }

        /**
         * Filters to disks with at most the specified size.
         *
         * @param maxBytes maximum size in bytes
         * @return this builder
         */
        public Builder maxSize(long maxBytes) {
            return filter(info -> info.mediaInfo().mediaSize() != null && info.mediaInfo().mediaSize() <= maxBytes);
        }

        /**
         * Filters to disks using a specific protocol (e.g., "USB", "SATA").
         *
         * @param protocol the protocol name (case-insensitive)
         * @return this builder
         */
        public Builder protocol(String protocol) {
            return filter(info -> protocol.equalsIgnoreCase(info.deviceInfo().protocol()));
        }

        /**
         * Filters to disks with a specific filesystem type (e.g., "exfat", "msdos", "apfs").
         *
         * @param kind the filesystem type (case-insensitive)
         * @return this builder
         */
        public Builder volumeKind(String kind) {
            return filter(info -> kind.equalsIgnoreCase(info.volumeInfo().kind()));
        }

        /**
         * Builds the DiskEventManager.
         *
         * @return a new DiskEventManager instance
         * @throws IllegalStateException if no listener was set
         */
        public DiskEventManager build() {
            if (listener == null) {
                throw new IllegalStateException("Listener must be set");
            }
            return new DiskEventManager(listener, filters);
        }
    }
}
