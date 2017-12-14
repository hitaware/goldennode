package com.goldennode.api.core;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.goldennode.testutils.CollectionUtils;
import com.goldennode.testutils.GoldenNodeJunitRunner;
import com.goldennode.testutils.RepeatTest;

public class FixedSizeListTest extends GoldenNodeJunitRunner {
    private static ArrayList<Integer> anotherList;

    @BeforeClass
    public static void init() {
        FixedSizeListTest.anotherList = new ArrayList<Integer>();
        FixedSizeListTest.anotherList.add(10);
        FixedSizeListTest.anotherList.add(11);
        FixedSizeListTest.anotherList.add(12);
    }

    @Test
    @RepeatTest(times = 1)
    public void add() {
        FixedSizeList<Integer> list = new FixedSizeList<>(2);
        Assert.assertEquals(true, list.add(1));
        Assert.assertEquals(true, list.add(2));
        Assert.assertEquals(false, list.add(3));
        Assert.assertEquals(false, list.add(5));
        Assert.assertEquals(false, list.add(6));
        Assert.assertEquals(2, list.size());
        list.clear();
        Assert.assertEquals(true, list.add(7));
        Assert.assertEquals(1, list.size());
    }

    @Test
    @RepeatTest(times = 1)
    public void addAll() {
        FixedSizeList<Integer> list2 = new FixedSizeList<>(5);
        Assert.assertEquals(true, list2.addAll(FixedSizeListTest.anotherList));
        Assert.assertEquals(3, list2.size());
    }

    @Test
    @RepeatTest(times = 1)
    public void addAll2() {
        FixedSizeList<Integer> list3 = new FixedSizeList<>(5);
        list3.add(1);
        list3.add(2);
        list3.add(3);
        list3.add(4);
        Assert.assertEquals(false, list3.addAll(FixedSizeListTest.anotherList));
        Assert.assertEquals(4, list3.size());
    }

    @Test
    @RepeatTest(times = 1)
    public void addAllWithIndex() {
        FixedSizeList<Integer> list4 = new FixedSizeList<>(7);
        list4.add(1);
        list4.add(2);
        list4.add(3);
        list4.add(4);
        Assert.assertEquals(true, list4.addAll(2, FixedSizeListTest.anotherList));
        Assert.assertEquals(7, list4.size());
        Assert.assertEquals(true, CollectionUtils.verifyListContents(list4, 1, 2, 10, 11, 12, 3, 4));
    }
}
