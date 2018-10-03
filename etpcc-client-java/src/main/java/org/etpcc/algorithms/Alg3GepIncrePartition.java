package org.etpcc.algorithms;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by liangjiao on 17/11/6.
 */
public class Alg3GepIncrePartition {
    private ArrayList<Entity> entities;
    private ArrayList<Model> models;
    private ArrayList<Transaction> transactions;
    private ArrayList<Model> encryptMinSet;


    public Alg3GepIncrePartition(ArrayList<Entity> entities, ArrayList<Model> models, ArrayList<Transaction> transactions) {
        this.entities = entities;
        this.models = models;
        this.transactions = transactions;
        encryptMinSet = new ArrayList<>();
    }

    public ArrayList<Entity> getEntities() {
        return entities;
    }

    public void setEntities(ArrayList<Entity> entities) {
        this.entities = entities;
    }

    public ArrayList<Model> getModels() {
        return models;
    }

    public void setModels(ArrayList<Model> models) {
        this.models = models;
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(ArrayList<Transaction> transactions) {
        this.transactions = transactions;
    }

    public ArrayList<Model> getEncryptMinSet() {
        return encryptMinSet;
    }

    public void setEncryptMinSet(ArrayList<Model> encryptMinSet) {
        this.encryptMinSet = encryptMinSet;
    }

    //========alg5: GEP Test
    public ArrayList<HashSet<Model>> alg3GeneralEnceyptionPartition() {
        ArrayList<HashSet<Model>> partition = new ArrayList<>();
        if (models == null || models.size() == 0) return partition;
        ArrayList<Model> encryptSet = new ArrayList<>();
        ArrayList<Model> candidateM = (ArrayList<Model>) models.clone();
        CostIncre costIncre = new CostIncre(entities, models, transactions);
        double costPartition = costIncre.getCostPartitionTest(null, true, encryptSet);
//        System.out.println("new null encrypt cost: " + costPartition);
        //记录
        ArrayList<Model> encryptMinAll = (ArrayList<Model>) encryptSet.clone();  //全局最优,初始化
        double minCostAll = costPartition; //全局最小
        while (candidateM.size() != 0) {
            Model modelMin = null; //记录当前这一轮的最小值对应的model
            double costMin = Double.MAX_VALUE;
            for (int i = 0; i < candidateM.size(); i++) {
                Model model = candidateM.get(i);
                //计算把model加进去encrypt里的cost
                costPartition = costIncre.getCostPartitionTest(model, false, encryptSet);
                if (modelMin == null) {
                    modelMin = model;
                    costMin = costPartition;
                } else if (new BigDecimal(costPartition).compareTo(new BigDecimal(costMin)) < 0) {
                    modelMin = model;
                    costMin = costPartition;
                }
            }
            //print
            costIncre.getCostPartitionTest(modelMin, true, encryptSet);
            encryptSet.add(modelMin);
            candidateM.remove(modelMin);

            //更新差集,直接遍历删除 n^2复杂度
            for (int i = 0; i < transactions.size(); i++) {
                CostIncre.writeModels.get(i).remove(modelMin);
                CostIncre.diffPartition.set(i, new Alg1GeneralPartition(entities, new ArrayList<Model>(CostIncre.writeModels.get(i))).partitionLedger());
            }
            //修改交集 n复杂度
            for (int i = 0; i < transactions.size(); i++) {
                Transaction transaction = transactions.get(i);
                if (transaction.getWriteSet().contains(modelMin.getType())) {
                    int res = CostIncre.innerE.get(i);
                    CostIncre.innerE.set(i, res + 1);
                }
            }
            //修改coste
            double coste = CostIncre.getCostEnc();
            for (int i = 0; i < transactions.size(); i++) {
                Transaction transaction = transactions.get(i);
                double freq = costIncre.getCostEnc(transaction.getName(), "freq", null);
                //增量计算coste
                coste += costIncre.getCostEnc(transaction.getName(), "cost_enc", modelMin.getType()) * freq;
            }
            CostIncre.setCostEnc(coste);
            //更新最小值
            if (new BigDecimal(costMin).compareTo(new BigDecimal(minCostAll)) < 0) {
                encryptMinAll = (ArrayList<Model>) encryptSet.clone();
                minCostAll = costMin;
            }

        }
        setEncryptMinSet(encryptMinAll);
//        System.out.println();
//        System.out.println("encryptMin size : " + encryptMinAll.size() + ", cost min : " + minCostAll);
//        System.out.println("old cost new encrypt :" + new Cost(entities,models,transactions).getCostPartition(transactions, models, encryptMinAll, true));
        partition.add(new HashSet<>(encryptMinAll));
        ArrayList<Model> part = (ArrayList<Model>) models.clone();
        part.removeAll(encryptMinAll);
        partition.addAll(new Alg1GeneralPartition(entities, part).partitionLedger());
        return partition;
    }

    //========alg5: generalEncryptionPartitioning
//    public ArrayList<Model> oldGeneralEnceyptionPartitioning() {
//        ArrayList<HashSet<Model>> partition = new ArrayList<>();
//        if (models == null || models.size() == 0) return new ArrayList<>();
//
//        HashSet<Model> encryptSet = new HashSet<>();
////        partition = new Alg1LedgerPartitioning(entities,models).partitionLedger();
////        partition.add(encryptSet); //alg5-line2
//
//        HashSet<Model> candidateM = new HashSet<>(models);
//
//        HashMap<HashSet<Model>, Double> s = new HashMap<>();
//        Cost cost = new Cost(entities,models,transactions);
//        double costPartition = cost.getCostPartition(transactions, models, new ArrayList<>(encryptSet),false);
//        s.put(encryptSet, costPartition);
//        HashSet<Model> encryptNow = (HashSet<Model>) encryptSet.clone();  //初始值
//
//        while (candidateM.size() != 0) {
//            int flag = 1; //record min
//            Model modelMin = new ArrayList<Model>(candidateM).get(0);
//            double minCost = Double.MAX_VALUE;
//            for (Model model : candidateM) {
//                HashSet<Model> encryptM = (HashSet<Model>) encryptNow.clone();
//                encryptM.add(model);  //Om = Onow 并 model
////                ArrayList<HashSet<Model>> partitionM = new ArrayList<>();  //定义Pm
////                partitionM.add(encryptM);
//                ArrayList<Model> modelM = (ArrayList<Model>) models.clone();
//                modelM.removeAll(encryptM);
////                partitionM.addAll(new Alg1LedgerPartitioning(entities,modelM).partitionLedger());
//                costPartition = cost.getCostPartition(transactions, modelM, new ArrayList<>(encryptM),true);
//                if (flag == 1) {
//                    modelMin = model;
//                    minCost = costPartition;
//                    flag = 0;
//                }
//                if (new BigDecimal(costPartition).compareTo(new BigDecimal(minCost)) < 0) {
//                    modelMin = model;
//                    minCost = costPartition;
//                }
//            }
//            encryptNow.add(modelMin);
//            candidateM.remove(modelMin);
////            System.out.println("encryprNow size " + encryptNow.size());
//            s.put((HashSet<Model>) encryptNow.clone(), minCost);
//        }
//        //Omin
//        HashSet<Model> encryptMin = new ArrayList<HashSet<Model>>(s.keySet()).get(0);
//        //print s
////        System.out.println();
//        System.out.println("encryptMin size : " + encryptMin.size());
//        double costMin = s.get(encryptMin);
//        for (HashSet<Model> encryptModel : s.keySet()) {
//            if (new BigDecimal(s.get(encryptModel)).compareTo(new BigDecimal(costMin)) < 0) {
//                encryptMin = encryptModel;
//                costMin = s.get(encryptMin);
//            }
//        }
//        return new ArrayList<>(encryptMin);
//    }

    public static void main(String[] args) {
        Initialization initialization = new Initialization(10, 20);
        initialization.constructRel(0, 2);
        long tm = System.currentTimeMillis();
        Alg3GepIncrePartition alg3GepIncrePartition = new Alg3GepIncrePartition(initialization.getEntities(), initialization.getModels(), initialization.getTransactions());
        ArrayList<HashSet<Model>> modelsPartition = alg3GepIncrePartition.alg3GeneralEnceyptionPartition();
        long tmEnd = System.currentTimeMillis();
        System.out.println("partition size : " + modelsPartition.size());
        int count = 1;
        for(HashSet<Model> modelHashSet : modelsPartition) {
            System.out.print(count++ + "th partition：" + modelHashSet.size() + " --- ");
            for(Model model : modelHashSet) {
                System.out.print(model.getName() + ", ");
            }
            System.out.println();
        }
        System.out.println("running time : " + (tmEnd - tm));
        System.out.println("=================================");
    }
}
