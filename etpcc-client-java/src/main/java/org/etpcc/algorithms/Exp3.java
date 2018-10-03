package org.etpcc.algorithms;

import java.util.ArrayList;

/**
 * Created by liangjiao on 17/11/22.
 */
public class Exp3 {
    public static void main(String[] args) {
        if(args.length != 2) {
            System.out.println("input: percent rate , e.g., 10 0 ,20 65 ,30 100");
            return;
        }
        int percent = Integer.parseInt(args[0]);
        double rate = Double.parseDouble(args[1]) / 100;
//        String r = "0";
//        String n = "4";
//        int numGroup = Integer.parseInt(n);
//        double rate = Double.parseDouble(r) / 100;


        //构建关系：20个warehouse，但是有分组，分别是0，2，4，6，8，10，但是不会限制每个company连哪个
        //求gep的cost
//        double rate = 0;
//        int percent = 80;
        Initialization initialization = new Initialization(10, 20);
        initialization.constructRelPercent(0, 2, percent);
        Cost cost1 = new Cost(rate);  //修改rate对应的freq
        Cost cost = new Cost(initialization.getEntities(), initialization.getModels(), initialization.getTransactions());
        Alg3GepIncrePartition alg3GepIncrePartition = new Alg3GepIncrePartition(initialization.getEntities(), initialization.getModels(), initialization.getTransactions());
        alg3GepIncrePartition.alg3GeneralEnceyptionPartition();
        double alg3Cost = cost.getCostPartition(initialization.getTransactions(), initialization.getModels(), new ArrayList<>(alg3GepIncrePartition.getEncryptMinSet()), true);
        System.out.println("exp3 cost -- percent ：" + percent + ", rate : " + rate + " -- alg3 : " + String.format("%.2f", alg3Cost));

    }
}
