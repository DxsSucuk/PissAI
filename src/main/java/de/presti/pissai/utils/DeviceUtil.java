package de.presti.pissai.utils;

import ai.djl.Device;
import ai.djl.engine.Engine;

public class DeviceUtil {

    /* Return the i'th GPU if it exists, otherwise return the CPU */
    public static Device tryGpu(int i) {
        return Engine.getInstance().getGpuCount() > i ? Device.gpu(i) : Device.cpu();
    }

    /* Return all available GPUs or the [CPU] if no GPU exists */
    public static Device[] tryAllGpus() {
        int gpuCount = Engine.getInstance().getGpuCount();
        if (gpuCount > 0) {
            Device[] devices = new Device[gpuCount];
            for (int i = 0; i < gpuCount; i++) {
                devices[i] = Device.gpu(i);
            }
            return devices;
        }
        return new Device[]{Device.cpu()};
    }

}
