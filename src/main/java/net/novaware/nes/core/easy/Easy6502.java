package net.novaware.nes.core.easy;

import net.novaware.nes.core.util.Hex;

/**
 * @author Gemini
 */
public class Easy6502 {

    /**
     * Launches the board with the classic Snake game.
     */
    static void main(String[] args) {
        EasyComp comp = EasyComp.newEasyComp();
        EasyBoard board = comp.newEasyBoard();

        board.preload(Hex.b(EasySnake.HEX));
        board.powerOn(true);
    }
}
