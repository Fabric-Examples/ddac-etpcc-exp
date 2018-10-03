package org.etpcc.algorithms;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by liangjiao on 17/11/13.
 */
public class Exp1 {
    public static void main(String[] args) {
        if(args.length != 2) {
            System.out.println("input: numCompany numWarehouse");
            return;
        }
        Cost cost1 = new Cost(0.60);
        int numCompany = Integer.parseInt(args[0]);
        int numWarehouse = Integer.parseInt(args[1]);
//        int numCompany = 10;
//        int numWarehouse = 20;
        ArrayList<Long> timePerm = new ArrayList<>();
        Initialization initialization = new Initialization(numCompany, numWarehouse);
        initialization.constructRel(0, 2);
//        System.out.println(" +++++++++++++++++++++++++++++++++++++++ " + initialization.getNumCompany() + " company");
        //比例：60% : 2+2+2+2+2 config.json
        //alg1 - Partition

        long alg1Start = System.currentTimeMillis();
        Alg1GeneralPartition alg1GeneralPartition = new Alg1GeneralPartition(initialization.getEntities(), initialization.getModels());
        ArrayList<HashSet<Model>> modelsPartition1 = alg1GeneralPartition.alg1GeneralPartition(); //加密集没有
        long alg1End = System.currentTimeMillis();
        timePerm.add(alg1End - alg1Start);
//        System.out.println("/* ============  alg1 Exp1 Time Performance ======================*/" + initialization.getNumCompany() + " company");
//        System.out.println("time cost(ns): " + (alg1End - alg1Start));
//        System.out.println("partition size : " + modelsPartition1.size());
//        int count = 1;
//        for (HashSet<Model> modelHashSet : modelsPartition1) {
//            System.out.print(count++ + "th partition：");
//            for (Model model : modelHashSet) {
//                System.out.print(model.getName() + ", ");
//            }
//            System.out.println();
//        }
//        System.out.println();
//        System.out.println();

        //alg2 - Encrypt
        long alg2Start = System.currentTimeMillis();
        Alg2EncryptionPartition alg2EncryptionPartition = new Alg2EncryptionPartition(initialization.getEntities(), initialization.getModels(), initialization.getTransactions());
        ArrayList<HashSet<Model>> modelsPartition2 = alg2EncryptionPartition.alg2EncryptionPartition();
        long alg2End = System.currentTimeMillis();
        timePerm.add(alg2End - alg2Start);
//        System.out.println("/* ============  alg2 Exp1 Time Performance ======================*/" + initialization.getNumCompany() + " company");
//        System.out.println("time cost(ns): " + (alg2End - alg2Start));
//        System.out.println("partition size : " + modelsPartition2.size());
//        count = 1;
//        for (HashSet<Model> modelHashSet : modelsPartition2) {
//            System.out.print(count++ + "th partition：");
//            for (Model model : modelHashSet) {
//                System.out.print(model.getName() + ", ");
//            }
//            System.out.println();
//        }
//        System.out.println();
//        System.out.println();

        //alg3 - gep
        long alg3Start = System.currentTimeMillis();
        Alg3GepIncrePartition alg3GepIncrePartition = new Alg3GepIncrePartition(initialization.getEntities(), initialization.getModels(), initialization.getTransactions());
        ArrayList<HashSet<Model>> modelsPartition3 = alg3GepIncrePartition.alg3GeneralEnceyptionPartition();
        long alg3End = System.currentTimeMillis();
        timePerm.add(alg3End - alg3Start);
//        System.out.println("/* ============  alg3 Exp1 Time Performance ======================*/" + initialization.getNumCompany() + " company");
//        System.out.println("time cost(ns): " + (alg3End - alg3Start));
//        System.out.println("partition size : " + modelsPartition3.size());
//        count = 1;
//        for (HashSet<Model> modelHashSet : modelsPartition3) {
//            System.out.print(count++ + "th partition：");
//            for (Model model : modelHashSet) {
//                System.out.print(model.getName() + ", ");
//            }
//            System.out.println();
//        }
//        System.out.println();
//        System.out.println();
//        System.out.println();
        System.out.print("company: " + numCompany + ", warehouse: " + numWarehouse + " --- ");
        System.out.println("time performance alg1, alg2, alg3 (ms): " + timePerm.get(0) + " # " + timePerm.get(1) + " # " + timePerm.get(2));
        System.out.println();


    }
}
