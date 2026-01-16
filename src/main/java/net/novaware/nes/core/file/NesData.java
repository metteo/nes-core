package net.novaware.nes.core.file;

import com.google.auto.value.AutoBuilder;
import net.novaware.nes.core.util.UByteBuffer;

import java.nio.ByteBuffer;

/**
 * NES File split into distinct data sections
 *
 * @param header    optional original header read from iNES / NES 2.0 file
 * @param trainer   usually contains mapper register translation and video memory caching code
 * @param program   instructions for the CPU to execute (PRG-ROM / Program ROM)
 * @param video     graphics data for the PPU to render (CHR-ROM / Character ROM)
 * @param misc      misc data depending on the console / mapper
 * @param footer    data after all specified sections in the file. May contain game title
 */
public record NesData(
        UByteBuffer header, // NOTE: header type and version would be useful?
        ByteBuffer trainer,
        ByteBuffer program,
        ByteBuffer video,
        ByteBuffer misc,
        ByteBuffer footer
) {

    private static boolean hasData(ByteBuffer buffer) {
        return buffer.capacity() > 0;
    }

    public boolean hasHeader() {
        return hasData(header.unwrap());
    }

    public boolean hasTrainer() {
        return hasData(trainer);
    }

    public boolean hasVideo() {
        return hasData(video);
    }

    public boolean hasFooter() {
        return hasData(footer);
    }

    public static Builder builder() {
        return new AutoBuilder_NesData_Builder();
    }

    public static Builder builder(NesData data) {
        return new AutoBuilder_NesData_Builder(data);
    }

    @AutoBuilder
    public interface Builder {
        Builder header(UByteBuffer header);

        Builder trainer(ByteBuffer trainer);

        Builder program(ByteBuffer program);

        Builder video(ByteBuffer video);

        Builder misc(ByteBuffer misc);

        Builder footer(ByteBuffer footer);

        private static ByteBuffer emptyBuffer() {
            return ByteBuffer.allocate(0);
        }

        default Builder noHeader() {
            return header(UByteBuffer.empty());
        }

        default Builder noTrainer() {
            return trainer(emptyBuffer());
        }

        default Builder noProgram() {
            return program(emptyBuffer());
        }

        default Builder noVideo() {
            return video(emptyBuffer());
        }

        default Builder noMisc() {
            return misc(emptyBuffer());
        }

        default Builder noFooter() {
            return footer(emptyBuffer());
        }

        NesData build();
    }
}
