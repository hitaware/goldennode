package com.goldennode.api.grid;

import org.junit.Assert;
import org.junit.Test;

import com.goldennode.api.goldennodegrid.GoldenNodeGrid;
import com.goldennode.api.grid.Grid;
import com.goldennode.api.grid.GridException;
import com.goldennode.api.grid.GridFactory;
import com.goldennode.api.grid.GridType;
import com.goldennode.testutils.GoldenNodeJunitRunner;
import com.goldennode.testutils.RepeatTest;

public class GridFactoryTest extends GoldenNodeJunitRunner {
    @Test
    @RepeatTest(times = 1)
    public void createVariousGridObjects() throws GridException {
        Grid c1 = GridFactory.getGrid();
        Grid c2 = GridFactory.getGrid(GridType.GOLDENNODEGRID);
        Grid c3 = GridFactory.getGrid(GridType.GOLDENNODEGRID);
        Assert.assertNotEquals(c1, c2);
        Assert.assertNotEquals(c1, c3);
        Assert.assertNotEquals(c2, c3);
        Assert.assertNotSame(c1, c2);
        Assert.assertNotSame(c1, c3);
        Assert.assertNotSame(c2, c3);
        Assert.assertTrue(c1 instanceof GoldenNodeGrid);
        Assert.assertTrue(c2 instanceof GoldenNodeGrid);
        Assert.assertTrue(c3 instanceof GoldenNodeGrid);
    }
}
