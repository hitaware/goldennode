package com.goldennode.api.cluster;

public class Undo {
    // List<String> list;
    //
    // @Before
    // public void init() {
    // list = new ClusteredList<String>();
    //
    // }
    //
    // @Test
    // public void undoOperations() {
    // Assert.assertEquals(((ClusteredList) list).getVersion(), 1);
    //
    // LOGGER.debug("Adding 5 items");
    // list.add("bir");
    // list.add("iki");
    // list.add("uc");
    // list.add("dort");
    // list.add("bes");
    // Assert.assertEquals(list.size(), 5);
    // Assert.assertEquals(((ClusteredList) list).getHistory().size(), 5);
    // Assert.assertEquals(((ClusteredList) list).getVersion(), 6);
    //
    // LOGGER.debug("Removing 2nd index");
    // list.remove(2);
    // Assert.assertEquals(list.size(), 4);
    // Assert.assertEquals(((ClusteredList) list).getHistory().size(), 6);
    // Assert.assertEquals(((ClusteredList) list).getVersion(), 7);
    //
    // LOGGER.debug("undo");
    // ((ClusteredList) list).undo(7);
    // Assert.assertEquals(list.size(), 5);
    // Assert.assertEquals(((ClusteredList) list).getHistory().size(), 5);
    // Assert.assertEquals(((ClusteredList) list).getVersion(), 6);
    //
    // LOGGER.debug("Adding alti");
    // list.add("alti");
    // Assert.assertEquals(list.size(), 6);
    // Assert.assertEquals(((ClusteredList) list).getHistory().size(), 6);
    // Assert.assertEquals(((ClusteredList) list).getVersion(), 7);
    //
    // LOGGER.debug("undo");
    // ((ClusteredList) list).undo(7);
    // Assert.assertEquals(list.size(), 5);
    // Assert.assertEquals(((ClusteredList) list).getHistory().size(), 5);
    // Assert.assertEquals(((ClusteredList) list).getVersion(), 6);
    //
    // LOGGER.debug("clear");
    // list.clear();
    // Assert.assertEquals(list.size(), 0);
    // Assert.assertEquals(((ClusteredList) list).getHistory().size(), 6);
    // Assert.assertEquals(((ClusteredList) list).getVersion(), 7);
    //
    // LOGGER.debug("undo");
    // ((ClusteredList) list).undo(7);
    // Assert.assertEquals(list.size(), 5);
    // Assert.assertEquals(((ClusteredList) list).getHistory().size(), 5);
    // Assert.assertEquals(((ClusteredList) list).getVersion(), 6);
    //
    // LOGGER.debug("3 undos");
    // ((ClusteredList) list).undo(6);
    // Assert.assertEquals(((ClusteredList) list).getVersion(), 5);
    // ((ClusteredList) list).undo(5);
    // Assert.assertEquals(((ClusteredList) list).getVersion(), 4);
    // ((ClusteredList) list).undo(4);
    // Assert.assertEquals(list.size(), 2);
    // Assert.assertEquals(((ClusteredList) list).getHistory().size(), 2);
    // Assert.assertEquals(((ClusteredList) list).getVersion(), 3);
    //
    // LOGGER.debug("Setting first element to on");
    // ((ClusteredList) list).set(0, "on");
    // Assert.assertEquals(list.size(), 2);
    // Assert.assertEquals(((ClusteredList) list).getHistory().size(), 3);
    // Assert.assertEquals(((ClusteredList) list).getVersion(), 4);
    //
    // LOGGER.debug("undo");
    // ((ClusteredList) list).undo(4);
    // Assert.assertEquals(list.size(), 2);
    // Assert.assertEquals(((ClusteredList) list).getHistory().size(), 2);
    // Assert.assertEquals(((ClusteredList) list).getVersion(), 3);
    //
    // }
    //
    // private void printList() {
    // LOGGER.debug("-----------");
    // LOGGER.debug("List Size:" + list.size());
    // LOGGER.debug("List Hist Size:"
    // + ((ClusteredList) list).getHistory().size());
    // LOGGER.debug("List:");
    // for (int i = 0; i < list.size(); i++) {
    // LOGGER.debug("Element" + (i + 1) + ":" + list.get(i));
    // }
    // LOGGER.debug("Hist:");
    // for (int i = 0; i < ((ClusteredList) list).getHistory().size(); i++) {
    // LOGGER.debug("Element" + (i + 1) + ":"
    // + ((ClusteredList) list).getHistory().get(i));
    // }
    //
    // }
}
