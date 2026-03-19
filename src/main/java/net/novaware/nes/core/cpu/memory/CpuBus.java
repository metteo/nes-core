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

    private BusOp busOp = BusOp.DATA_READ; // TODO: randomize between data read / write

    @Owned
    private final PagedMemory internal;

    @Used
    private MemoryDevice.ReadWrite cartridge = new MemoryDevice.EmptyDevice();

    @Used
    private MemoryDevice.ReadWrite expansion = new MemoryDevice.EmptyDevice();

    @Owned
    private final OpenBus openBus;

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
        this.openBus = new OpenBus(MEMORY_START, MEMORY_END);

        this.internal = new PagedMemory(openBus);

        this.page40 = new MemoryPage(40, new OpenBus(APU_REGISTERS_START, CARTRIDGE_FDS_END));

        this.cycleCounter = cycleCounter;
        this.ram = ram;
        this.ppu = ppu;
        this.apu = apu;
        this.apuTest = apuTest;
        this.timer = timer;

//        internal.attach(ram);
//        internal.attach(ppu);
//        internal.attach(page40);
//        internal.attach(cartridge);
        internal.onAttach(dataLine);

        page40.attach(apu);
        page40.attach(apuTest);
        page40.attach(timer);
        //page40.attach(cartridge);

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
            currentSegment = (MemoryDevice) cartridge;

        } else if (CARTRIDGE_RANGE.test(addressInt)) {
            currentRange = CARTRIDGE_RANGE;
            currentSegment = (MemoryDevice) cartridge;
        }
    }

    @Override
    public @Unsigned byte readByte() {
        @Unsigned byte data = currentSegment.specifyThen(addressLatch).readByte();

        return data;
    }

    @Override
    public BusOp currentOp() {
        return busOp;
    }

    @Override
    public void writeByte(@Unsigned byte data) {
        currentSegment.specifyThen(addressLatch).writeByte(data);
    }

    @Override
    public void attachCartridge(MemoryDevice.ReadWrite cartridge) {
        this.cartridge = cartridge;
        this.cartridge.onAttach(dataLine);
    }

    @Override
    public void detachCartridge() {
        cartridge.onDetach();
        cartridge = new MemoryDevice.EmptyDevice();
    }

    @Override
    public void attachExpansion(MemoryDevice.ReadWrite expansion) {
        this.expansion = expansion;
        this.expansion.onAttach(dataLine);
    }

    @Override
    public void detachExpansion() {
        expansion.onDetach();
        expansion = new MemoryDevice.EmptyDevice();
    }

    @Override
    public ControlBus.Line access(@Unsigned short address) {
        assert busOp == BusOp.DATA_READ || busOp == BusOp.DATA_WRITE; // compile out, TODO: consider JCP or Manifold

        busOp = BusOp.ADDRESS_ACCESS;
        addressLatch = address;
        cycleCounter.increment();

        internal.onAccess(address);
        cartridge.onAccess(address);
        expansion.onAccess(address);

        return this;
    }

    @Override
    public DataBus.Read read() {
        assert busOp == BusOp.ADDRESS_ACCESS; // compile out

        busOp = BusOp.CONTROL_READ;

        return this;
    }

    @Override
    public DataBus.Write write() {
        assert busOp == BusOp.ADDRESS_ACCESS; // compile out

        busOp = BusOp.CONTROL_WRITE;

        return this;
    }

    TempLine dataLine = new TempLine();

    @Override
    public @Unsigned byte data() {
        assert busOp == BusOp.CONTROL_READ; // compile out

        busOp = BusOp.DATA_READ;

        internal.onRead();
        cartridge.onRead();
        expansion.onRead();

        return dataLine.cycle(); // CPU reading the line
    }

    @Override
    public void data(@Unsigned byte data) {
        assert busOp == BusOp.CONTROL_WRITE; // compile out

        busOp = BusOp.DATA_WRITE;

        dataLine.data(data); // CPU driving the line

        internal.onWrite();
        cartridge.onWrite();
        expansion.onWrite();

        dataLine.cycle();
    }
}
