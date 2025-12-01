package com.selesse.jdiskarbitration.internal;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

interface DiskArbitration extends Library {
    DiskArbitration INSTANCE = Native.load("DiskArbitration", DiskArbitration.class);

    interface DADiskDescriptionChangedCallback extends Callback {
        void invoke(Pointer daDisk, PointerByReference p, Pointer context);
    }

    interface DADiskDisappearedCallback extends Callback {
        void invoke(Pointer daDisk, Pointer context);
    }

    interface DADiskAppearedCallback extends Callback {
        void invoke(Pointer disk, Pointer context);
    }

    Pointer DADiskCopyDescription(Pointer disk);

    Pointer DADiskGetBSDName(Pointer disk);

    Pointer DASessionCreate(Pointer allocator);

    void DARegisterDiskAppearedCallback(Pointer session, Pointer match, DADiskAppearedCallback callback, Pointer context);

    void DARegisterDiskDisappearedCallback(Pointer session, Pointer match, DADiskDisappearedCallback callback, Pointer context);

    void DARegisterDiskDescriptionChangedCallback(Pointer session, Pointer match, Pointer probe, DADiskDescriptionChangedCallback callback, Pointer context);

    void DAUnregisterCallback(Pointer session, DADiskAppearedCallback callback, Pointer context);

    void DAUnregisterCallback(Pointer session, DADiskDisappearedCallback callback, Pointer context);

    void DAUnregisterCallback(Pointer session, DADiskDescriptionChangedCallback callback, Pointer context);

    void DASessionScheduleWithRunLoop(Pointer session, Pointer runLoop, Pointer runLoopMode);

    void DASessionUnscheduleFromRunLoop(Pointer session, Pointer runLoop, Pointer runLoopMode);
}
