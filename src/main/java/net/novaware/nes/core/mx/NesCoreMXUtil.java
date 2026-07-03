package net.novaware.nes.core.mx;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

public class NesCoreMXUtil {

    public static void register(NesCoreMXBean bean) throws JMException {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName("net.novaware.nes.core:type=NesCore");
        mbs.registerMBean(bean, name);
    }
}
