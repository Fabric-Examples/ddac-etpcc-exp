package org.etpcc.algorithms;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;

/**
 * Created by liangjiao on 17/11/13.
 */
public class Exp2 {
    public static void main(String[] args) {
//        if(args.length != 1) {
//            System.out.println("input: rate (e.g., 20% freq: 4 + 4 + 0.67 + 0.67 + 0.67)");
//            return;
//        }
//        double rate = Double.parseDouble(args[0]) / 100;
        String r = "20";
        double rate = Double.parseDouble(r)/100;
        Initialization initialization = new Initialization(10, 20);
        initialization.constructRel(0, 2);

        Cost cost1 = new Cost(rate);  //修改rate对应的freq
        
        Cost cost = new Cost(initialization.getEntities(), initialization.getModels(), initialization.getTransactions());

        Alg1GeneralPartition alg1GeneralPartition = new Alg1GeneralPartition(initialization.getEntities(), initialization.getModels());
        alg1GeneralPartition.alg1GeneralPartition();
        double alg1Cost = cost.getCostPartition(initialization.getTransactions(),initialization.getModels(),new ArrayList<>(),true);
        // System.out.println("alg1 cost : " + alg1Cost);
        // System.out.println();

        Alg2EncryptionPartition alg2EncryptionPartition = new Alg2EncryptionPartition(initialization.getEntities(), initialization.getModels(), initialization.getTransactions());
        alg2EncryptionPartition.alg2EncryptionPartition();
        // System.out.println("al2 ===== " + alg2EncryptionPartition.getEncryptionSet().size());
        double alg2Cost = cost.getCostPartitionEnc(initialization.getTransactions(),initialization.getModels(),new ArrayList<>(alg2EncryptionPartition.getEncryptionSet()),true);
        // System.out.println("alg2 cost : " + alg2Cost);
        // System.out.println();

        Alg3GepIncrePartition alg3GepIncrePartition = new Alg3GepIncrePartition(initialization.getEntities(), initialization.getModels(), initialization.getTransactions());
        alg3GepIncrePartition.alg3GeneralEnceyptionPartition();
        double alg3Cost = cost.getCostPartition(initialization.getTransactions(),initialization.getModels(),new ArrayList<>(alg3GepIncrePartition.getEncryptMinSet()),true);
        // System.out.println("alg3 cost : " + alg3Cost);
        // System.out.println();
        System.out.println("exp2 cost -- " + rate + " -- alg1, alg2, alg3 : " + String.format("%.2f",alg1Cost) + " # " + String.format("%.2f",alg2Cost) + " # " + String.format("%.2f",alg3Cost));

    }
}
