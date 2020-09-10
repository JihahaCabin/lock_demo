package com.haha.utils;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class ZkLock implements Lock {

    //zk客户端
    private ZooKeeper zk;

    //锁的根节点
    private String root ="/locks";
    //当前锁的名称
    private String lockName;

    //当前线程创建的序列node
    private ThreadLocal<String> nodeId = new ThreadLocal<>();

    private final static byte[] data = new byte[0];

    public ZkLock(ZooKeeper zooKeeper, String lockName){
        this.zk = zooKeeper;
        this.lockName = lockName;

        try {
            Stat stat = zk.exists(root, false);
            if(stat==null) {
                //创建根节点
                zk.create(root, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 监听器，监听临时节点的删除
     */
    class LockWatcher implements Watcher{

        private CountDownLatch latch;

        public LockWatcher(CountDownLatch latch){
            this.latch = latch;
        }

        public void process(WatchedEvent watchedEvent) {
            if(watchedEvent.getType()== Event.EventType.NodeDeleted){
                latch.countDown();
            }
        }
    }

    public void lock() {
        try {
            //在根节点下创建临时有序节点
            String myNode = zk.create(root + "/" + lockName, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

            System.out.println(Thread.currentThread().getName()+myNode+" create");

            //获取根节点下的所有根节点下
            List<String> subNodes = zk.getChildren(root, false);

            //排序
            TreeSet<String> sortedNodes = new TreeSet<String>();
            for(String node:subNodes){
                sortedNodes.add(root+"/"+node);
            }
            //获取序号最下的子节点
            String smallNode = sortedNodes.first();

            //如果该次创建的临时有序节点是最小的子节点，则表示取得锁
            if(myNode.equals(smallNode) ){
                System.out.println(Thread.currentThread().getName()+myNode+" get lock");
                this.nodeId.set(myNode);
                return;
            }

            //否则，取得当前节点的前一个节点
            String preNode = sortedNodes.lower(myNode);

            CountDownLatch latch = new CountDownLatch(1);
            //查询前一个节点，同时注册监听
            Stat stat = zk.exists(preNode, new LockWatcher(latch));
            // 如果比自己小的前一个节点查询时，已经不存在则无需等待，如果存在则监听
            if(stat!=null){
                System.out.println(Thread.currentThread().getName()+myNode+" waiting for "+ root+"/"+preNode+" release lock");
                latch.await();//等待
            }
            nodeId.set(myNode);

        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public void unlock() {
        try{
            //释放锁时，只需要将本次创建的临时有序节点移除掉
            System.out.println(Thread.currentThread().getName()+" unlock");
            if(nodeId!=null){
                zk.delete(nodeId.get(),-1);
            }
            nodeId.remove();

        }catch (InterruptedException e){
            e.printStackTrace();
        }catch (KeeperException e){
            e.printStackTrace();
        }
    }

    public void lockInterruptibly() throws InterruptedException {

    }

    public boolean tryLock() {
        return false;
    }

    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    public Condition newCondition() {
        return null;
    }
}
