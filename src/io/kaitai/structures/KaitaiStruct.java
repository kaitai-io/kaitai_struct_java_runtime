package io.kaitai.structures;

import java.io.IOException;

public class KaitaiStruct {
    protected KaitaiStream _io;
    protected KaitaiStruct _parent;

    public KaitaiStruct(KaitaiStream _io) throws IOException {
        this._io = _io;
    }

    public KaitaiStruct(KaitaiStream _io, KaitaiStruct _parent) throws IOException {
        this._io = _io;
        this._parent = _parent;
    }
}
