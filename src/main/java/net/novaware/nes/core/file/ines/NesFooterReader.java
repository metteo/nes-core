package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.file.NesMeta;
import net.novaware.nes.core.util.Quantity;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static net.novaware.nes.core.util.Quantity.Unit.BYTES;

public class NesFooterReader extends NesFooterHandler {

    public NesMeta read(ByteBuffer footer, NesMeta meta) {
        String title = read(footer);

        final NesMeta maybeNewMeta;

        if (!title.isEmpty()) {
            maybeNewMeta = NesMeta.builder(meta)
                    .title(title)
                    .footer(new Quantity(footer.capacity(), BYTES))
                    .build();
        } else {
            maybeNewMeta = meta;
        }

        return maybeNewMeta;
    }

    public String read(ByteBuffer footer) {
        byte[] titleBytes = new byte[footer.capacity()];
        footer.get(titleBytes).rewind();

        String title = new String(titleBytes, StandardCharsets.US_ASCII);

        return title.trim();
    }
}
