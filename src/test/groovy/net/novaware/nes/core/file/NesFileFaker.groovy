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

        byte mapper = (byte)((params.mapper & 0xF) << 4) // TODO: make these methods for readability?
        byte trainer = params.trainerPresent ? 0b100 : 0b0
        byte mirroring = params.nametable == Orientation.HORIZONTAL ? 0b1 : 0b0
        byte flag6 = (byte) (mapper | trainer | mirroring)

        byte[] header = [
                0x4E, 0x45, 0x53, 0x1a,
                (byte)(params.programRomSize / 16 / 1024),
                (byte)(params.videoRomSize / 8 / 1024),
                flag6,
                0x00, // flag 7
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 // bytes 8 - 15
        ]

        Random random = new Random()

        byte[] trainerBytes = new byte[params.trainerPresent ? 512 : 0]
        random.nextBytes(trainerBytes)

        byte[] programRom = new byte[params.programRomSize]
        random.nextBytes(programRom)

        byte[] videoRom = new byte[params.videoRomSize]
        random.nextBytes(videoRom)

        byte[] fileData = new byte[header.length + trainerBytes.length + programRom.length + videoRom.length]

        System.arraycopy(header, 0, fileData, 0, header.length)
        System.arraycopy(trainerBytes, 0, fileData, header.length, trainerBytes.length)
        System.arraycopy(programRom, 0, fileData, header.length + trainerBytes.length, programRom.length)
        System.arraycopy(videoRom, 0, fileData, header.length + trainerBytes.length + programRom.length, videoRom.length)

        return new Result(
                fileData: fileData,
                programRomStart: programRom[0],
                programRomEnd: programRom[programRom.length - 1],
                videoRomStart: videoRom[0],
                videoRomEnd: videoRom[videoRom.length - 1],
                trainerStart: params.trainerPresent ? trainerBytes[0] : 0,
                trainerEnd: params.trainerPresent ? trainerBytes[trainerBytes.length - 1] : 0
        )
    }
}
