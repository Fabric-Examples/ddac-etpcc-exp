package org.etpcc.algorithms;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by liangjiao on 17/11/14.
 */
public class Alg4ExhaustPartition {

    private ArrayList<Entity> entities;
    private ArrayList<Model> models;
    private ArrayList<Transaction> transactions;
    private static ArrayList<Model> encryptMinSet;
    private static double minCost;
    private static ArrayList<Model> tmpArr = new ArrayList<>();
    private Cost cost;

    public static ArrayList<Model> getEncryptMinSet() {
        return encryptMinSet;
    }

    public static void setEncryptMinSet(ArrayList<Model> encryptMinSet) {
        Alg4ExhaustPartition.encryptMinSet = encryptMinSet;
    }

    public Alg4ExhaustPartition(ArrayList<Entity> entities, ArrayList<Model> models, ArrayList<Transaction> transactions) {
        this.entities = entities;
        this.models = models;
        this.transactions = transactions;
        encryptMinSet = new ArrayList<>();
        minCost = 0;
        this.cost = new Cost(entities,models,transactions);
    }

    //穷举法算model最优解
    public ArrayList<HashSet<Model>> alg4OptimumEncryptionPartition() {
        ArrayList<HashSet<Model>> partition = new ArrayList<>();
        ArrayList<Model> models1 = (ArrayList<Model>) models.clone();
        //开始为空
        minCost = cost.getCostPartition(transactions,models,encryptMinSet,true);
        for(int k=0;k<=models.size();k++) { //穷举
            combine(0, k, models1);
        }
        partition.add(new HashSet<>(encryptMinSet));
        ArrayList<Model> part = (ArrayList<Model>) models.clone();
        part.removeAll(encryptMinSet);
        partition.addAll(new Alg1GeneralPartition(entities, part).partitionLedger());
        return partition;
    }

    public void combine(int index, int k, ArrayList<Model> models) {

        if (k == 1) {
            for (int i = index; i < models.size(); i++) {
                tmpArr.add(models.get(i));
                //print
//                for(Model model:tmpArr) {
//                    System.out.print(model.getName() + ",");
//                }
//                System.out.println();
                double costPartition = cost.getCostPartition(transactions,models,new ArrayList<>(tmpArr),true);
                if(costPartition < minCost ) {  //最小值
                    minCost = costPartition;
                    encryptMinSet = new ArrayList<>(tmpArr);
                }
                tmpArr.remove((Object) models.get(i));
            }
        } else if (k > 1) {
            for (int i = index; i <= models.size() - k; i++) {
                tmpArr.add(models.get(i));
                combine(i + 1, k - 1, models);
                tmpArr.remove((Object) models.get(i));
            }
        } else {
            return;
        }
    }

    public static void main(String[] args) {
        Initialization initialization = new Initialization(1, 1);
        initialization.constructRelSmall(2);
        Alg4ExhaustPartition alg4ExhaustPartition = new Alg4ExhaustPartition(initialization.getEntities(), initialization.getModels(), initialization.getTransactions());
        alg4ExhaustPartition.alg4OptimumEncryptionPartition();
        System.out.println("exp4 exhaust cost: mincost = " + minCost);
        for(Model model:encryptMinSet) {
            System.out.print(model.getName() + ", ");
        }
        System.out.println();
    }
}



