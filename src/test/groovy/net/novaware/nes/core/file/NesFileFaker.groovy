package net.novaware.nes.core.file

class NesFileFaker {

    static class Params {
        Version version

        /**
         * In Bytes
         */
        int programRomSize

        /**
         * In Bytes
         */
        int videoRomSize
        boolean trainerPresent
        Orientation nametable
        int mapper
    }

    static class Result {
        byte[] fileData
        byte programRomStart
        byte programRomEnd
        byte videoRomStart
        byte videoRomEnd
        byte trainerStart
        byte trainerEnd
    }

    static enum Version {
        iNES, NES_v2_0
    }

    static enum Orientation {
        VERTICAL, // 0
        HORIZONTAL // 1
    }

    Result generate(Params params) {
        if (params.version != Version.iNES) {
            throw new IllegalArgumentException("Only iNES is supported for now")
        }

        byte[] header = [
                0x4E, 0x45, 0x53, 0x1a,
                (byte)(params.programRomSize / 16 / 1024),
                (byte)(params.videoRomSize / 8 / 1024),
                0x00, // flag 6
                0x00, // flag 7
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 // bytes 8 - 15
        ]

        byte[] programRom = new byte[params.programRomSize]

        Random random = new Random();
        random.nextBytes(programRom)

        byte[] videoRom = new byte[params.videoRomSize]
        random.nextBytes(videoRom)

        byte[] fileData = new byte[header.length + programRom.length + videoRom.length]

        System.arraycopy(header, 0, fileData, 0, header.length)
        System.arraycopy(programRom, 0, fileData, header.length, programRom.length)
        System.arraycopy(videoRom, 0, fileData, header.length + programRom.length, videoRom.length)

        return new Result(
                fileData: fileData,
                programRomStart: programRom[0],
                programRomEnd: programRom[programRom.length - 1],
                videoRomStart: videoRom[0],
                videoRomEnd: videoRom[videoRom.length - 1]
        )
    }
}
