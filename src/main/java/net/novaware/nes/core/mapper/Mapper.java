package net.novaware.nes.core.mapper;

/**
 * @see <a href="https://www.nesdev.org/wiki/Mapper">Mapper on nesdev.org</a>
 */
public interface Mapper {
    // mapping program rom
    // mapping video rom
    // mapping save ram
    // nametable mirroring
    // mapping chr-ram or rom
    // generate interrupts (conditional too)
    // sound generation
    // attribute table mapping alteration

    int getNumber();

}
