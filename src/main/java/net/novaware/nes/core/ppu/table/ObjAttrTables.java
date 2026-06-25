package net.novaware.nes.core.ppu.table;

import jakarta.inject.Inject;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.ppu.inject.PpuVar;
import net.novaware.nes.core.ppu.memory.ObjAttrMemory;
import net.novaware.nes.core.ppu.register.ObjAttrRegister;
import net.novaware.nes.core.util.Hex;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.ppu.inject.PpuVarName.POA;
import static net.novaware.nes.core.ppu.inject.PpuVarName.SOA;
import static net.novaware.nes.core.ppu.table.ObjAttrTable.asFlipH;
import static net.novaware.nes.core.ppu.table.ObjAttrTable.asFlipV;
import static net.novaware.nes.core.ppu.table.ObjAttrTable.asHidden;
import static net.novaware.nes.core.ppu.table.ObjAttrTable.asPalette;
import static net.novaware.nes.core.ppu.table.ObjAttrTable.asUnused;
import static net.novaware.nes.core.util.UTypes.sint;

// TODO: this could be used for decay? rows of pri and sec oam
@BoardScope
public class ObjAttrTables implements Tables {

    private final ObjAttrTable priOam;
    private final ObjAttrTable secOam;

    @Inject
    public ObjAttrTables(
        @PpuVar(POA) ObjAttrMemory priOam,
        @PpuVar(SOA) ObjAttrMemory secOam
    ) {
        // private tables with independent cursor to not affect main cursors
        this.priOam = new ObjAttrTable("", new ObjAttrRegister("", priOam.getSize()), priOam);
        this.secOam = new ObjAttrTable("", new ObjAttrRegister("", secOam.getSize()), secOam);
    }


    public String print() {
        StringBuilder sb = new StringBuilder();
        sb.append("Primary:\n");

        for (int row = 0; row < priOam.size(); row++) {
            priOam.setRow(row);
            sb.append("\t").append(row).append(" -> ").append(printRow(priOam)).append("\n");
        }

        sb.append("Secondary:\n");

        for (int row = 0; row < secOam.size(); row++) {
            secOam.setRow(row);
            sb.append("\t").append(row).append(" -> ").append("no index for now").append(" -> ")
                    .append(printRow(secOam)).append("\n");
        }

        return sb.toString();
    }

    static String printRow(ObjAttrTable table) {
        StringBuilder sb = new StringBuilder();

        sb.append("y: 0x").append(Hex.s(table.getY())).append(", ");
        sb.append("x: 0x").append(Hex.s(table.getX())).append(", ");
        sb.append("tile: 0x").append(Hex.s(table.getTile())).append(", ");
        @Unsigned byte attr = table.getAttr();
        sb.append("palette: ").append(sint(asPalette(attr))).append(", ");
        sb.append("flipV: ").append(asFlipV(attr)).append(", ");
        sb.append("flipH: ").append(asFlipH(attr)).append(", ");
        sb.append("hidden: ").append(asHidden(attr)).append(", ");
        sb.append("unused: ").append(sint(asUnused(attr)));

        return sb.toString();
    }

}
