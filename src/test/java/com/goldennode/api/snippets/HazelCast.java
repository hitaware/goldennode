package com.goldennode.api.snippets;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class HazelCast {
    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        HazelcastInstance hz1 = Hazelcast.newHazelcastInstance();
        HazelcastInstance hz2 = Hazelcast.newHazelcastInstance();
        List list = hz.getList("mylist");
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println(hz1.getList("mylist").size());
            }
        }, 0, 2000);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                list.add(UUID.randomUUID().toString());
            }
        }, 0, 100);
    }
}
