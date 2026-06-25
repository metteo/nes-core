package net.novaware.nes.core.ppu.unit;

import jakarta.inject.Inject;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.ppu.inject.PpuVar;
import net.novaware.nes.core.ppu.memory.ObjAttrMemory;
import net.novaware.nes.core.ppu.register.ObjAttrRegister;
import net.novaware.nes.core.register.BooleanRegister;
import net.novaware.nes.core.register.IntegerCounter;

import static net.novaware.nes.core.ppu.inject.PpuVarName.CH;
import static net.novaware.nes.core.ppu.inject.PpuVarName.DC;
import static net.novaware.nes.core.ppu.inject.PpuVarName.LC;
import static net.novaware.nes.core.ppu.inject.PpuVarName.POA;
import static net.novaware.nes.core.ppu.inject.PpuVarName.SOA;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;

@BoardScope
public class SpriteUnit {

    private final IntegerCounter lineCounter;
    private final IntegerCounter dotCounter;

    private final BooleanRegister spriteSize;

    private final ObjAttrMemory priObjAttrMemory;
    private final ObjAttrMemory secObjAttrMemory;

    @Inject
    public SpriteUnit(
        @PpuVar(LC) IntegerCounter lineCounter,
        @PpuVar(DC) IntegerCounter dotCounter,

        @PpuVar(CH) BooleanRegister spriteSize,

        @PpuVar(POA) ObjAttrMemory priObjAttrMemory,
        @PpuVar(SOA) ObjAttrMemory secObjAttrMemory
    ) {
        this.lineCounter = lineCounter;
        this.dotCounter = dotCounter;

        this.spriteSize = spriteSize;

        this.priObjAttrMemory = priObjAttrMemory;
        this.secObjAttrMemory = secObjAttrMemory;
    }


    private ObjAttrRegister priOamRegister2 = new ObjAttrRegister("temp.poam.reg", 0x100);
    private ObjAttrRegister secOamRegister2 = new ObjAttrRegister("temp.soam.reg", 0x20);

    public void eval() {
        int dot = dotCounter.getValue();
        if (dot == 65) {
            boolean tallSprite = spriteSize.get();
            int height = tallSprite ? 16 : 8;
            int secOamI = 0;
            // TODO: create "VIEW" action that resets oamaddr and sec oam addr? or not
            for(int i = 0; i < 0xFF; i+=4) { // TODO: use OAMADDR instead of i
                int y = sint(priObjAttrMemory.read(ubyte(i)));
                int futureY = lineCounter.getValue() + 1;
                if (y < futureY && futureY <= y+height) {
                    secObjAttrMemory.write(ubyte(secOamI), ubyte(y));
                    secObjAttrMemory.write(ubyte(secOamI+1), priObjAttrMemory.read(ubyte(i+1)));
                    secObjAttrMemory.write(ubyte(secOamI+2), priObjAttrMemory.read(ubyte(i+2)));
                    secObjAttrMemory.write(ubyte(secOamI+3), priObjAttrMemory.read(ubyte(i+3)));

                    secOamI+=4;

                    if (secOamI >= secObjAttrMemory.getSize()) {
                        // TODO: set sprite overflow?
                        break;
                    }
                }
            }
        }
    }
}
