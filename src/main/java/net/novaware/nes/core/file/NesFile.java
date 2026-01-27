package net.novaware.nes.core.file;

import java.net.URI;

import static java.util.Objects.requireNonNull;

/**
 * There is some intended duplication in this record:
 * <ul>
 *     <li>
 *         <code>meta</code> & <code>data.header</code> fields -
 *         When reading a file header field is populated with an
 *         original version of the header if available and then
 *         parsed into meta section.
 *         Later if the whole file needs to be saved the original
 *         header will be used instead of parsed section to
 *         preserve it.
 *         It's possible original header when read was in old
 *         version and user may want to upgrade it.
 *         In such case a converter will use the meta section to
 *         create new header and replace the old one in data section.
 *         It's possible there is no header (.unh file). In such
 *         case it's necessary to get meta info from external
 *         source (xml, online) and possibly generate a header.
 *     </li>
 *     <li>
 *         <code>meta.title</code> & <code>data.footer</code> -
 *         When reading a file trailing section may contain the
 *         title of the software. Such information is put in
 *         title field but only the ASCII printable part.
 *         The original trailing data is stored in data.footer
 *         If there is no trailing data the title is derived from
 *         the origin part (file name without the extension).
 *         Later if the whole file needs to be saved, the original
 *         footer will be used instead of parsed section.
 *         The trailing data in the file is non-standard and
 *         user may want to clear it out or on the other hand
 *         add it based on the file name or other information.
 *     </li>
 *     <li>
 *         sizes in meta and sizes of buffers in data sections -
 *         the process of reading the files is executed in stages
 *         First, the fixed size header is read and parsed.
 *         This gives the information about the rest of data.
 *         Different slicing points are calculated and added
 *         in meta for later use.
 *         Second, different sections of the file are sliced
 *         into dedicated buffers and stored in data.
 *         Third, hash values are calculated and stored in
 *         hash section.
 *
 *         In case of headerless file:
 *         First hash whole file and look up meta info about it
 *         Second, slice out sections of the file
 *         Third, hash data sections
 *         Fourth, verify integrity against looked up info.
 *
 *         In case of generated file meta section acts as a
 *         blueprint how random data section should be
 *         generated. Hash section is calculated afterward.
 *     </li>
 * </ul>
 *
 * It's possible to have a file without data section.
 * In such case it was initialized from offline / online metadata DB
 * and can be used to identify unheadered files or to fill missing
 * parts of existing files.
 *
 * @param origin location of the file (scheme decides if local or remote)
 * @param meta information required to interpret the data
 * @param data different parts of a NES software
 * @param hash results used to identify the software
 */
public record NesFile (
    URI origin,
    NesMeta meta,
    NesData data,
    NesHash hash
) {

    public NesFile(
        URI origin,
        NesMeta meta,
        NesData data,
        NesHash hash
    ) {
        this.origin = requireNonNull(origin, "origin must not be null");
        this.meta = requireNonNull(meta, "meta must not be null");
        this.data = requireNonNull(data, "data must not be null");
        this.hash = requireNonNull(hash, "hash must not be null");

        // TODO: validate different section against each other, or not?
    }

    public boolean hasData() {
        return data != null;
    }
}


