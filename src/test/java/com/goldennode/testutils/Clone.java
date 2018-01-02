package com.goldennode.testutils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

public class Clone extends GoldenNodeJunitRunner {
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Clone.class);
    List<InnerObject> list;
    List<InnerObject> list2;

    @Before
    public void init() {
        list = Collections.synchronizedList(new ArrayList<InnerObject>());
        list.add(new InnerObject(new Date()));
        list.add(new InnerObject(new Date()));
        list2 = new ArrayList<InnerObject>();
        list2.add(new InnerObject(new Date()));
        list2.add(new InnerObject(new Date()));
    }

    @SuppressWarnings("unchecked")
    @Test
    @RepeatTest(times = 1)
    public void test() throws Exception {
        LOGGER.debug("Cloning lists");
        List<InnerObject> cloneList = Collections.synchronizedList(new ArrayList<InnerObject>(list));
        List<InnerObject> cloneList2 = (List<InnerObject>) ((ArrayList<InnerObject>) list2).clone();
        Assert.assertEquals(cloneList, list);
        Assert.assertNotSame(cloneList, list);
        Assert.assertEquals(cloneList2, list2);
        Assert.assertNotSame(cloneList2, list2);
        cloneList.add(new InnerObject(new Date()));
        cloneList2.add(new InnerObject(new Date()));
        Assert.assertNotEquals(cloneList, list);
        Assert.assertNotSame(cloneList, list);
        Assert.assertNotEquals(cloneList2, list2);
        Assert.assertNotSame(cloneList2, list2);
        Assert.assertNotEquals(list.size(), cloneList.size());
        Assert.assertNotEquals(list2.size(), cloneList2.size());
    }

    class InnerObject {
        public Date str;

        public InnerObject(Date str) {
            this.str = str;
        }
    }
}