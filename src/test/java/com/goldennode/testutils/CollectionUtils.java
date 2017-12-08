package com.goldennode.testutils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CollectionUtils {
    @SafeVarargs
    public static <T> boolean verifyListContents(List<T> list, T... contents) {
        if (list.size() != contents.length) {
            return false;
        }
        for (int i = 0; i < list.size(); i++) {
            if (!list.get(i).equals(contents[i])) {
                return false;
            }
        }
        return true;
    }

    @SafeVarargs
    public static <K, V> boolean verifyMapContents(Map<K, V> map, K... contents) {
        if (map.size() != contents.length) {
            return false;
        }
        boolean found = false;
        for (K k : map.keySet()) {
            for (K k2 : contents) {
                if (k2.equals(k)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    @SafeVarargs
    public static <T> boolean verifySetContents(Set<T> set, T... contents) {
        if (set.size() != contents.length) {
            return false;
        }
        boolean found = false;
        for (T t : set) {
            for (T t2 : contents) {
                if (t2.equals(t)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    public static void main(String arg[]) {

        HashSet<Object> s = new HashSet<>();
        s.add("1");
        s.add("3");
        s.add("2");
        s.add("7");
        s.add("4");
        s.add("10");
        s.add("9");
        System.out.println(getContents(s));
    }

    public static String getContents(Collection<?> set) {
        return set.stream().map(Object::toString).collect(Collectors.joining(","));
        
    }
}