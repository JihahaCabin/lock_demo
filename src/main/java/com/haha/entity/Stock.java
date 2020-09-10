package com.haha.entity;

public class Stock {
    //库存数量
    private static int num=1;

    // 减少库存数量的方法
    public boolean reduceStock(){
        if(num>0){
            try {
                //一些逻辑处理
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            num--;
            return true;
        }

        return false;
    }
}