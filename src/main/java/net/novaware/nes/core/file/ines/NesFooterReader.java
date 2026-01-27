package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.file.NesMeta;
import net.novaware.nes.core.util.Quantity;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static net.novaware.nes.core.util.Chars.isPrintable;
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

        final int end = Math.min(titleBytes.length, 128); // 127 too
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        for (int i = 0; i < end; i++) {
            int b = titleBytes[i] & 0xFF; // TODO: quick patch. will need more annotations for signedness

            if (isPrintable(b)) {
                out.write(b);
            } else {
                break;
            }
        }

        String title = out.toString(StandardCharsets.US_ASCII);

        return title.trim();
    }
}
