package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.file.NesFile;
import net.novaware.nes.core.file.NesMeta;
import net.novaware.nes.core.util.UByteBuffer;

import java.nio.ByteBuffer;

// TODO: Converter will update / create footer from meta.
public class NesFileConverter {

    public record Params(
            NesFileVersion version,
            boolean includeInfo, // TODO: think about bitfield or enum instead
            boolean includeTitle
    ) {
    }

    public void convert(NesFile nesFile, Params params) {
        // TODO: assert
        NesMeta meta = nesFile.meta();

        // TODO: writer should use data.header instead. Converter will update / create header from Meta
        UByteBuffer header = new NesHeaderWriter()
                .write(meta, new NesHeaderWriter.Params(params.version, params.includeInfo)).header();


        ByteBuffer footer = new NesFooterWriter()
                .write(meta.title(), meta.footer().toBytes());

    }
}
