package net.novaware.nes.core.cpu.memory;

import jakarta.inject.Inject;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.memory.BusOp;
import net.novaware.nes.core.memory.ControlBus;
import net.novaware.nes.core.memory.DataBus;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.memory.MemoryDevice;
import net.novaware.nes.core.memory.MemoryPage;
import net.novaware.nes.core.memory.OpenBus;
import net.novaware.nes.core.memory.PagedMemory;
import net.novaware.nes.core.register.CycleCounter;
import net.novaware.nes.core.util.uml.Owned;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import java.util.function.IntPredicate;

import static net.novaware.nes.core.cpu.inject.CpuVarName.APU;
import static net.novaware.nes.core.cpu.inject.CpuVarName.ATM;
import static net.novaware.nes.core.cpu.inject.CpuVarName.CC;
import static net.novaware.nes.core.cpu.inject.CpuVarName.PPU;
import static net.novaware.nes.core.cpu.inject.CpuVarName.RAM;
import static net.novaware.nes.core.cpu.inject.CpuVarName.TMR;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.APU_REGISTERS_END;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.APU_REGISTERS_START;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.APU_TEST_REGISTERS_END;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.APU_TEST_REGISTERS_START;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.CARTRIDGE_END;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.CARTRIDGE_FDS_END;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.CARTRIDGE_FDS_START;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.CARTRIDGE_START;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.IO_REGISTERS_END;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.IO_REGISTERS_START;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.MEMORY_END;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.MEMORY_START;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.PPU_REGISTERS_MIRROR_END;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.PPU_REGISTERS_START;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.RAM_MIRROR_END;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.RAM_START;
import static net.novaware.nes.core.util.UTypes.sint;

public class CpuBus implements MemoryBus {

    public static final IntPredicate RAM_RANGE      = a -> sint(RAM_START) <= a                && a <= sint(RAM_MIRROR_END);
    public static final IntPredicate PPU_REGS_RANGE = a -> sint(PPU_REGISTERS_START) <= a      && a <= sint(PPU_REGISTERS_MIRROR_END);
    public static final IntPredicate APU_RANGE   =    a -> sint(APU_REGISTERS_START) <= a   && a <= sint(APU_REGISTERS_END);
    public static final IntPredicate IO_RANGE   =     a -> sint(IO_REGISTERS_START) <= a   && a <= sint(IO_REGISTERS_END);
    public static final IntPredicate APU_TEST_RANGE = a -> sint(APU_TEST_REGISTERS_START) <= a && a <= sint(APU_TEST_REGISTERS_END);
    public static final IntPredicate CARTRIDGE_FDS_RANGE = a -> sint(CARTRIDGE_FDS_START) <= a && a <= sint(CARTRIDGE_FDS_END);
    public static final IntPredicate CARTRIDGE_RANGE = a -> sint(CARTRIDGE_START) <= a         && a <= sint(CARTRIDGE_END);

    @Used
    private final CycleCounter cycleCounter;

    @Owned
    private final PagedMemory pagedMemory;

    @Owned
    private final MemoryPage page40;

    @Used
    private MemoryDevice ram;

    @Used
    private MemoryDevice ppu;

    @Used
    private MemoryDevice apu;

    @Used
    private MemoryDevice apuTest;

    @Used
    private MemoryDevice timer;

    @Used
    private MemoryDevice cartridge = new OpenBus(CARTRIDGE_FDS_START, CARTRIDGE_END);

    @Used
    private MemoryDevice expansion = new OpenBus(MEMORY_START, MEMORY_END);

    private MemoryDevice currentSegment;
    private IntPredicate currentRange;
    private @Unsigned short addressLatch; // translated into specific segment range

    @Inject
    public CpuBus(
            @CpuVar(CC) CycleCounter cycleCounter,
            @CpuVar(RAM) MemoryDevice ram,
            @CpuVar(PPU) MemoryDevice ppu,
            @CpuVar(APU) MemoryDevice apu,
            @CpuVar(ATM) MemoryDevice apuTest, // TODO: apu test
            @CpuVar(TMR) MemoryDevice timer // TODO: timer
    ) {
        this.pagedMemory = new PagedMemory(new OpenBus(MEMORY_START, MEMORY_END));
        this.page40 = new MemoryPage(40, new OpenBus(APU_REGISTERS_START, CARTRIDGE_FDS_END));

        this.cycleCounter = cycleCounter;
        this.ram = ram;
        this.ppu = ppu;
        this.apu = apu;
        this.apuTest = apuTest;
        this.timer = timer;

//        pagedMemory.attach(ram);
//        pagedMemory.attach(ppu);
//        pagedMemory.attach(page40);
//        pagedMemory.attach(cartridge);

        page40.attach(apu);
        page40.attach(apuTest);
        page40.attach(timer);
        page40.attach(cartridge);

        currentSegment = ram;
        currentRange = RAM_RANGE;
    }

    @Override
    public void specify(@Unsigned short address) {
        addressLatch = address;
        cycleCounter.increment();

        int addressInt = sint(address);

//        if (currentRange.test(addressInt)) {
//            return; // fast track
//        }

        // TODO: try to implement page based indexing instead of if/else

        if (RAM_RANGE.test(addressInt)) { // TODO: maybe switch to BankedMemory
            currentRange = RAM_RANGE;
            currentSegment = ram;

        } else if (PPU_REGS_RANGE.test(addressInt)) {
            currentRange = PPU_REGS_RANGE;
            currentSegment = ppu;

        } else if (APU_RANGE.test(addressInt)) {
            currentRange = APU_RANGE;
            currentSegment = apu;

        } else if (IO_RANGE.test(addressInt)) {
            currentRange = IO_RANGE;
            currentSegment = apu;

        } else if (APU_TEST_RANGE.test(addressInt)) {
            currentRange = APU_TEST_RANGE;
            currentSegment = apuTest;

        } else if (CARTRIDGE_FDS_RANGE.test(addressInt)) {
            currentRange = CARTRIDGE_FDS_RANGE;
            currentSegment = cartridge;

        } else if (CARTRIDGE_RANGE.test(addressInt)) {
            // TODO: cartridge can listen to full address range, not only 4020-FFFF
            // TODO: cartridge can listen to all address bus calls since they don't break anything
            // TODO: cartridge can listen to all writes since the cpu is the source (multiple devices may accept)
            // TODO: cartridge can only respond to reads if there is no other device responding (how to detect open bus?)
            currentRange = CARTRIDGE_RANGE;
            currentSegment = cartridge;
        }
    }

    @Override
    public @Unsigned byte readByte() {
        @Unsigned byte data = currentSegment.specifyThen(addressLatch).readByte();

        return data;
    }

    @Override
    public BusOp currentOp() {
        return pagedMemory.currentOp();
    }

    @Override
    public void writeByte(@Unsigned byte data) {
        currentSegment.specifyThen(addressLatch).writeByte(data);
    }

    @Override
    public void attach(MemoryDevice memoryDevice) {
        cartridge = memoryDevice;

//        List<MemoryDevice> replaced = pagedMemory.attach(memoryDevice);
//        if (!replaced.isEmpty()) {
//            System.out.println("Replaced following devices: " + replaced);
//        }
    }

    @Override
    public ControlBus.Line access(@Unsigned short address) {
        pagedMemory.access(address);
        return this;
    }

    @Override
    public DataBus.Read read() {
        pagedMemory.read();
        return this;
    }

    @Override
    public DataBus.Write write() {
        pagedMemory.write();
        return this;
    }

    @Override
    public @Unsigned byte data() {
        return pagedMemory.data();
    }

    @Override
    public void data(@Unsigned byte data) {
        pagedMemory.data(data);
    }
}
