package org.etpcc.algorithms;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by liangjiao on 17/11/24.
 */
public class Exp4 {
    public static void main(String[] args) {
        if(args.length != 3) {
            System.out.println("input: numCompany numWarehouse rate , e.g., 1 2 65");
            return;
        }
        int numCompany = Integer.parseInt(args[0]);
        int numWarehouse = Integer.parseInt(args[1]);
        double rate = Double.parseDouble(args[2]) / 100;
//        double rate = 0.65;
//        int numCompany = 2;
//        int numWarehouse = 4;
        Initialization initialization = new Initialization(numCompany, numWarehouse);
        initialization.constructRelSmall(2);
        Cost cost1 = new Cost(rate);  //修改rate对应的freq

        //gep
        Cost cost2 = new Cost(initialization.getEntities(), initialization.getModels(), initialization.getTransactions());
        Alg3GepIncrePartition alg3GepIncrePartition = new Alg3GepIncrePartition(initialization.getEntities(), initialization.getModels(), initialization.getTransactions());
        alg3GepIncrePartition.alg3GeneralEnceyptionPartition();
        double alg3Cost = cost2.getCostPartition(initialization.getTransactions(), initialization.getModels(), new ArrayList<>(alg3GepIncrePartition.getEncryptMinSet()), true);
        ArrayList<Model> encryptSet = alg3GepIncrePartition.getEncryptMinSet();
        System.out.print("alg3 encryptSet: " + encryptSet.size() + " --- ");
        for(Model model : encryptSet) {
            System.out.print(model.getName() + ", ");
        }
        System.out.println();

        Cost cost = new Cost(initialization.getEntities(), initialization.getModels(), initialization.getTransactions());
        //exhaust
        Alg4ExhaustPartition alg4ExhaustPartition = new Alg4ExhaustPartition(initialization.getEntities(), initialization.getModels(), initialization.getTransactions());
        alg4ExhaustPartition.alg4OptimumEncryptionPartition();
        double alg4Cost = cost.getCostPartition(initialization.getTransactions(), initialization.getModels(), new ArrayList<>(alg4ExhaustPartition.getEncryptMinSet()), true);
        ArrayList<Model> encryptSet1 = alg4ExhaustPartition.getEncryptMinSet();
        System.out.print("alg4 encryptSet: " + encryptSet1.size() + " --- ");
        for(Model model : encryptSet1) {
            System.out.print(model.getName() + ", ");
        }
        System.out.println();
        System.out.println("exp4 --- numCompany = " + numCompany + ", numWarehouse = " + numWarehouse + ", model = " + (3+2*numWarehouse+4*numCompany) + " -- alg3 cost = " + alg3Cost + ", alg4 cost = " + alg4Cost);
    }
}
