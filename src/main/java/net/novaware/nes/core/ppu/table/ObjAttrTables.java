package net.novaware.nes.core.ppu.table;

// TODO: this could be used for decay? rows of pri and sec oam
public class ObjAttrTables {

    /*
    public String printOam() {
        StringBuilder sb = new StringBuilder();
        sb.append("Primary:\n");

        for (int i = 0; i < PRIMARY_ENTRY_COUNT; i++) {
            ObjAttrMemoryBackup.ObjAttrEntry entry = new ObjAttrMemoryBackup.ObjAttrEntry();
            decodeBytesIntoEntry(i, entry);

            sb.append("\t").append(i).append(" -> ").append(printEntry(entry)).append("\n");
        }

        sb.append("Secondary:\n");

        for (int i = 0; i < secondary.size(); i++) {
            ObjAttrMemoryBackup.ObjAttrEntry entry = secondary.get(i);
            sb.append("\t").append(i).append(" -> ").append(entry.primaryIndex).append(" -> ")
                    .append(printEntry(entry)).append("\n");
        }

        return sb.toString();
    }

    static String printEntry(ObjAttrMemoryBackup.ObjAttrEntry entry) {
        StringBuilder sb = new StringBuilder();

        sb.append("y: 0x").append(Hex.s(entry.y)).append(", ");
        sb.append("x: 0x").append(Hex.s(entry.x)).append(", ");
        sb.append("tile: 0x").append(Hex.s(entry.tile)).append(", ");
        sb.append("palette: ").append(sint(entry.palette)).append(", ");
        sb.append("flipV: ").append(entry.flipV).append(", ");
        sb.append("flipH: ").append(entry.flipH).append(", ");
        sb.append("hidden: ").append(entry.hidden).append(", ");
        sb.append("unused: ").append(sint(entry.unused));

        return sb.toString();
    }

 */
}
