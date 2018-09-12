package org.star.learnzk;

import org.apache.zookeeper.*;
import org.apache.zookeeper.ZooDefs.Ids;

import java.io.IOException;

public class ZkTest {

    private static final String CONNECT_STRING = "192.168.213.129:2181,192.168.213.129:2182,192.168.213.129:2183";
    private static final int SESSION_TIMEOUT = 10000;
    private static Integer mutex = new Integer(-1);

    static ZooKeeper zooKeeper = null;

    public static void main(String[] args) {

        try {
            zooKeeper = new ZooKeeper(CONNECT_STRING, SESSION_TIMEOUT, watchedEvent -> {
                synchronized (mutex) {
                    System.out.println("触发 " + watchedEvent.getType() + " 事件，状态：" + watchedEvent.getState() + "，路径：" + watchedEvent.getPath());
                    mutex.notify();
                }
            });

            synchronized (mutex) {

                if (zooKeeper.getState() != ZooKeeper.States.CONNECTED) {
                    mutex.wait();
                }
                zooKeeper.create("/test", "testData".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                zooKeeper.create("/test/testChild", "testChildData".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                System.out.println("获取/test/testChild数据：" + new String(zooKeeper.getData("/test/testChild", true, null)));
                zooKeeper.delete("/test/testChild", 0);
                zooKeeper.delete("/test", 0);
                zooKeeper.create("/test", "testData".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                zooKeeper.delete("/test", 0);
                mutex.wait();

            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }

    }

}
