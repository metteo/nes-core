package net.novaware.nes.core.ppu.memory;

import jakarta.inject.Inject;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.cpu.memory.CpuBusBridge;
import net.novaware.nes.core.cpu.memory.CpuMemMap;
import net.novaware.nes.core.memory.DataBus;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.memory.MemoryDevice;
import net.novaware.nes.core.memory.OpenLine;
import net.novaware.nes.core.ppu.inject.PpuVar;
import net.novaware.nes.core.ppu.register.PpuStatusRegister;
import net.novaware.nes.core.ppu.register.ViewPortRegister;
import net.novaware.nes.core.register.BooleanRegister;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.ShortRegister;
import net.novaware.nes.core.util.Nameable;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.memory.CpuMemMap.PPU_BUS_ADDRESS_REGISTER;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.PPU_BUS_DATA_REGISTER;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.PPU_CONTROL_REGISTER;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.PPU_MASK_REGISTER;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.PPU_OAM_ADDRESS_REGISTER;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.PPU_OAM_DATA_REGISTER;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.PPU_REGISTERS_END;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.PPU_REGISTERS_MIRROR_END;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.PPU_REGISTERS_START;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.PPU_SCROLL_REGISTER;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.PPU_STATUS_REGISTER;
import static net.novaware.nes.core.ppu.inject.PpuVarName.BUS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.CB;
import static net.novaware.nes.core.ppu.inject.PpuVarName.CH;
import static net.novaware.nes.core.ppu.inject.PpuVarName.CI;
import static net.novaware.nes.core.ppu.inject.PpuVarName.CP;
import static net.novaware.nes.core.ppu.inject.PpuVarName.CS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.CV;
import static net.novaware.nes.core.ppu.inject.PpuVarName.DR;
import static net.novaware.nes.core.ppu.inject.PpuVarName.EB;
import static net.novaware.nes.core.ppu.inject.PpuVarName.EG;
import static net.novaware.nes.core.ppu.inject.PpuVarName.ER;
import static net.novaware.nes.core.ppu.inject.PpuVarName.GS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.MB;
import static net.novaware.nes.core.ppu.inject.PpuVarName.MS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.OAM;
import static net.novaware.nes.core.ppu.inject.PpuVarName.PS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.RB;
import static net.novaware.nes.core.ppu.inject.PpuVarName.RL;
import static net.novaware.nes.core.ppu.inject.PpuVarName.RS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.T;
import static net.novaware.nes.core.ppu.inject.PpuVarName.VX;
import static net.novaware.nes.core.ppu.inject.PpuVarName.W;
import static net.novaware.nes.core.ppu.register.ViewPortRegister.COARSE_MASK;
import static net.novaware.nes.core.ppu.register.ViewPortRegister.FINE_MASK;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;
import static net.novaware.nes.core.util.UTypes.ushort;

/**
 * @see https://www.nesdev.org/wiki/PPU_pinout
 *
 * TODO: expose PPU pinout on Ppu class and direct calls from CPU through this class into Ppu class
 * TODO: implement register set delays for ppu usecase, maybe replace the latch class in irq disable case
 */
@BoardScope
public class PpuMemDevice implements MemoryDevice.ReadWrite, Nameable, CpuBusBridge {

    private final MemoryBus ppuBus; // TODO: probably shouldn't have direct access, instead through ppu?
    private final PaletteMemory palette;
    private final ObjAttrMemory oam;

    private final ViewPortRegister currentViewPort;
    private final ViewPortRegister tempViewPort;
    private final BooleanRegister secondWrite;
    private final ByteRegister dataReadBuffer;

    private final PpuStatusRegister statusRegister;

    private final ByteRegister vramAddressIncrement;
    private final ShortRegister backgroundPatternTable;
    private final ShortRegister spritePatternTable;
    private final BooleanRegister vBlankInterruptEnabled;
    private final BooleanRegister masterSlaveSelect;
    private final BooleanRegister spriteSize;

    private final BooleanRegister emphasizeRed;
    private final BooleanRegister emphasizeGreen;
    private final BooleanRegister emphasizeBlue;
    private final BooleanRegister renderSprite; // TODO: there is a delay after changing (2-3 dots)
    private final BooleanRegister renderBackground;
    private final BooleanRegister maskSprite;
    private final BooleanRegister maskBackground;
    private final BooleanRegister greyscale;

    private final BooleanRegister resetLock;

    private final ByteRegister oamAddressLatch;

    private final Handler emptyHandler = new EmptyHandler();
    private Handler[] readHandlers = new Handler[8];
    private Handler[] writeHandlers = new Handler[8];

    private Handler readHandlerLatch;
    private Handler writeHandlerLatch;

    private DataBus.Line dataLine = new OpenLine();

    private @Unsigned short addressLatch;

    @Inject
    public PpuMemDevice(
        @PpuVar(BUS) MemoryBus ppuBus,
        PaletteMemory palette,
        ObjAttrMemory oam,

        @PpuVar(VX) ViewPortRegister currentViewPort,
        @PpuVar(T) ViewPortRegister tempViewPort,
        @PpuVar(W) BooleanRegister secondWrite,
        @PpuVar(DR) ByteRegister dataReadBuffer,


        @PpuVar(PS) PpuStatusRegister statusRegister,

        @PpuVar(CI) ByteRegister vramAddressIncrement,
        @PpuVar(CB) ShortRegister backgroundPatternTable,
        @PpuVar(CS) ShortRegister spritePatternTable,
        @PpuVar(CV) BooleanRegister vBlankInterruptEnabled,
        @PpuVar(CP) BooleanRegister masterSlaveSelect,
        @PpuVar(CH) BooleanRegister spriteSize,

        @PpuVar(ER) BooleanRegister emphasizeRed,
        @PpuVar(EG) BooleanRegister emphasizeGreen,
        @PpuVar(EB) BooleanRegister emphasizeBlue,
        @PpuVar(RS) BooleanRegister renderSprite,
        @PpuVar(RB) BooleanRegister renderBackground,
        @PpuVar(MS) BooleanRegister maskSprite,
        @PpuVar(MB) BooleanRegister maskBackground,
        @PpuVar(GS) BooleanRegister greyscale,

        @PpuVar(OAM) ByteRegister oamAddress,

        @PpuVar(RL) BooleanRegister resetLock
    ) {
        this.ppuBus = ppuBus;
        this.palette = palette;
        this.oam = oam;

        this.currentViewPort = currentViewPort;
        this.tempViewPort = tempViewPort;
        this.secondWrite = secondWrite;
        this.dataReadBuffer = dataReadBuffer;

        this.statusRegister = statusRegister;

        this.vramAddressIncrement = vramAddressIncrement;
        this.backgroundPatternTable = backgroundPatternTable;
        this.spritePatternTable = spritePatternTable;
        this.vBlankInterruptEnabled = vBlankInterruptEnabled;
        this.masterSlaveSelect = masterSlaveSelect;
        this.spriteSize = spriteSize;

        this.emphasizeRed = emphasizeRed;
        this.emphasizeGreen = emphasizeGreen;
        this.emphasizeBlue = emphasizeBlue;
        this.renderSprite = renderSprite;
        this.renderBackground = renderBackground;
        this.maskSprite = maskSprite;
        this.maskBackground = maskBackground;
        this.greyscale = greyscale;
        this.resetLock = resetLock;
        this.oamAddressLatch = oamAddress;

        for (int i = 0; i < readHandlers.length; i++) {
            readHandlers[i] = emptyHandler;
            writeHandlers[i] = emptyHandler;
        }

        readHandlerLatch = emptyHandler;
        writeHandlerLatch = emptyHandler;

        readHandlers [idx(PPU_STATUS_REGISTER)]      = new StatusHandler();
        readHandlers [idx(PPU_OAM_DATA_REGISTER)]    = new OamDataHandler();
        readHandlers [idx(PPU_BUS_DATA_REGISTER)]    = new DataBusHandler();

        writeHandlers[idx(PPU_CONTROL_REGISTER)]     = new ControlHandler();
        writeHandlers[idx(PPU_MASK_REGISTER)]        = new MaskHandler();
        writeHandlers[idx(PPU_OAM_ADDRESS_REGISTER)] = new OamAddressHandler();
        writeHandlers[idx(PPU_OAM_DATA_REGISTER)]    = new OamDataHandler();
        writeHandlers[idx(PPU_SCROLL_REGISTER)]      = new ScrollHandler();
        writeHandlers[idx(PPU_BUS_ADDRESS_REGISTER)] = new AddressBusHandler();
        writeHandlers[idx(PPU_BUS_DATA_REGISTER)]    = new DataBusHandler();
    }

    private static int idx(@Unsigned short address) {
        return (sint(address) - sint(PPU_REGISTERS_START)) & 0b111;
    }

    @Override
    public String getName() {
        return "CPU<->PPU";
    }

    @Override
    public @Unsigned short getStartAddress() {
        return CpuMemMap.PPU_REGISTERS_START;
    }

    @Override
    public @Unsigned short getEndAddress() {
        return PPU_REGISTERS_MIRROR_END;
    }

    @Override
    public void onAttach(DataBus.Line dataLine) { // TODO: THIS IS CPU D0..7 pins on ppu (3 bits)
        this.dataLine = dataLine;

        palette.onAttach(dataLine);
    }

    @Override
    public void onAccess(@Unsigned short address) { // TODO: THIS IS CPU A0..2 pins on ppu (3 bits)
        final int addrInt = sint(address);

        addressLatch = ushort(addrInt & sint(PPU_REGISTERS_END));
        int index = idx(address);

        readHandlerLatch = readHandlers[index];
        writeHandlerLatch = writeHandlers[index];
    }

    @Override
    public void onRead() {                       // TODO: THIS IS PPU R/W pin
        readHandlerLatch.onRead();
    }

    @Override
    public void onWrite() {                       // TODO: THIS IS PPU R/W pin
        writeHandlerLatch.onWrite();
    }

    @Override
    public void onDetach() {
        dataLine = new OpenLine();

        palette.onDetach();
    }

    public interface Handler {

        default void onRead() {}

        default void onWrite() {}
    }

    class EmptyHandler implements Handler {}

    class ControlHandler implements Handler {

        @Override
        public void onWrite() {
            if (resetLock.get()) {
                return;
            }

            int data = sint(dataLine.data());

            // 0xvphb_sinn
            int nn = data & 0b11;
            int i  = ((data & 0x4) >> 2) == 0 ? 1 : 32;
            int s  = ((data & 0x8) >> 3) * 0x1000;

            int     b = ((data & 0x10) >> 4) * 0x1000;
            boolean h = (data & 0x20) != 0;
            boolean p = (data & 0x40) != 0;
            boolean v = (data & 0x80) != 0;

            tempViewPort.setNameTable(nn);
            vramAddressIncrement.setAsByte(i);
            spritePatternTable.setAsShort(s);
            backgroundPatternTable.setAsShort(b);
            spriteSize.set(h);
            masterSlaveSelect.set(p); // TODO: if false read ext pins (0) as backdrop, if true use ext second PPU
            vBlankInterruptEnabled.set(v); // TODO: may trigger NMI
        }
    }

    class MaskHandler implements Handler {

        @Override
        public void onWrite() {
            if (resetLock.get()) {
                return;
            }

            int data = sint(dataLine.data());

            boolean eb = (data & 0x80) != 0;
            boolean e6 = (data & 0x40) != 0;
            boolean e5 = (data & 0x20) != 0;
            boolean rs = (data & 0x10) != 0;

            boolean rb = (data & 0x08) != 0;
            boolean ms = (data & 0x04) == 0;
            boolean mb = (data & 0x02) == 0;
            boolean gs = (data & 0x01) != 0;

            final boolean ntsc = true; // TODO: inject platform and check if pal / dendy

            emphasizeBlue.set(eb);
            emphasizeGreen.set(ntsc ? e6 : e5);
            emphasizeRed.set(ntsc ? e5 : e6);
            renderSprite.set(rs);
            renderBackground.set(rb);
            maskSprite.set(ms);
            maskBackground.set(mb);
            greyscale.set(gs);
        }
    }

    class StatusHandler implements Handler {

        @Override
        public void onRead() {
            int vb = statusRegister.isVerticalBlank() ? 0x80 : 0;
            int s0h = statusRegister.isSpriteZeroHit() ? 0x40 : 0;
            int so = statusRegister.isSpriteOverflow() ? 0x20 : 0;

            // TODO: 5 bits openbus or 4 bits of 2C05 PPU ID
            @Unsigned byte status = ubyte(vb | s0h | so);
            dataLine.data(status);

            secondWrite.set(false);
            statusRegister.setVerticalBlankDelayed(false, 1);
        }
    }

    class OamAddressHandler implements Handler {

        @Override
        public void onWrite() {
            oamAddressLatch.set(dataLine.data());
        }
    }

    class OamDataHandler implements Handler {

        @Override
        public void onRead() {
            @Unsigned byte oamAddr = oamAddressLatch.get();
            @Unsigned byte oamData = oam.read(oamAddr);

            dataLine.data(oamData);
        }

        @Override
        public void onWrite() {
            @Unsigned byte oamAddr = oamAddressLatch.get();
            @Unsigned byte oamData = dataLine.data();

            oam.write(oamAddr, oamData);

            oamAddressLatch.setAsByte(sint(oamAddr) + 1);
        }
    }

    class ScrollHandler implements Handler {

        @Override
        public void onWrite() {
            int data = sint(dataLine.data());

            int fine = data & FINE_MASK;
            int coarse = (data >> 3) & COARSE_MASK;

            final boolean firstWrite = !secondWrite.get(); // TODO: simplify everywhere
            if (firstWrite) {
                tempViewPort.setCoarseX(coarse);
                currentViewPort.setFineX(fine);

                secondWrite.set(true);
            } else {
                tempViewPort.setCoarseY(coarse);
                tempViewPort.setFineY(fine);

                secondWrite.set(false);
            }
        }
    }

    class AddressBusHandler implements Handler {

        @Override
        public void onWrite() {
            @Unsigned byte data = dataLine.data();

            final boolean firstWrite = !secondWrite.get();
            if (firstWrite) {
                int dataInt = sint(data);
                tempViewPort.high(ubyte(dataInt & 0b0011_1111));
                secondWrite.set(true);
            } else {
                tempViewPort.low(data);
                secondWrite.set(false);

                // TODO: wait 1-1.5 dots

                tempViewPort.transfer(currentViewPort);
            }
        }
    }

    class DataBusHandler implements Handler {

        @Override
        public void onRead() {
            @Unsigned short ppuAddress = currentViewPort.get();

            if (sint(ppuAddress) < sint(PpuMemMap.PALETTE_RAM_START)) {
                @Unsigned byte bufferedData = dataReadBuffer.get();
                dataLine.data(bufferedData);

                @Unsigned byte data = ppuBus.access(ppuAddress).read().data();
                dataReadBuffer.set(data);
            } else {
                // TODO: this is more complex: https://www.nesdev.org/wiki/PPU_registers#Reading_palette_RAM
                palette.onAccess(ppuAddress);
                palette.onRead();
            }
        }

        @Override
        public void onWrite() {
            @Unsigned short ppuAddress = currentViewPort.get();

            if (sint(ppuAddress) < sint(PpuMemMap.PALETTE_RAM_START)) {
                @Unsigned byte data = dataLine.data();

                ppuBus.access(ppuAddress).write().data(data);

            } else {
                palette.onAccess(ppuAddress);
                palette.onWrite();
            }

            int nextPpuAddress = sint(ppuAddress) + vramAddressIncrement.getAsInt();
            currentViewPort.set(ushort(nextPpuAddress));
        }
    }
}
