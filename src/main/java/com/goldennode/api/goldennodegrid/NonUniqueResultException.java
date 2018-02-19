package com.goldennode.api.goldennodegrid;

import com.goldennode.api.grid.GridException;

public class NonUniqueResultException extends GridException {
    private static final long serialVersionUID = 1L;

    public NonUniqueResultException(String string) {
        super(string);
    }
}
