package org.etpcc.algorithms;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by liangjiao on 17/10/26.
 */
public class CostIncre {

    private ArrayList<Entity> entities;
    private ArrayList<Model> models;
    private ArrayList<Transaction> transactions;
    private String content = "";
    public static ArrayList<ArrayList<HashSet<Model>>> diffPartition; //按交易，记录差集的子账本数量，初始值加密集为0，子账本model为所有t的写集合model，每次加一个model，相当于数量判断是否减1，最后确定model后，再进行子账本调整
    public static ArrayList<HashSet<Model>> writeModels;
    public static ArrayList<Integer> innerE; //按交易，记录交集，初始值加密集为0，交集为空，里面的个数为0，随着加密集的增加，当这个交集的个数大于2的时候，就不再需要计算这部分了，如果writemodel包含这个model，则结果为上一轮的交集个数+1
    public static double costEnc; //上一轮的costEnc,只有当找到这个model时候才能修改该值

    public CostIncre(ArrayList<Entity> entities) {
        this.entities = entities;
        try (BufferedReader reader = new BufferedReader(new FileReader("src/config.json"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content += line + "\n";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public CostIncre(ArrayList<Entity> entities, ArrayList<Model> models, ArrayList<Transaction> transactions) {
        this.entities = entities;
        this.models = models;
        this.transactions = transactions;
        this.writeModels = new ArrayList<>();
        this.diffPartition = new ArrayList<>();
        this.innerE = new ArrayList<>(transactions.size()-1);
        //初始化
        for(int i=0;i<transactions.size();i++) {
            this.writeModels.add(new HashSet<>());
            this.diffPartition.add(new ArrayList<>());
            this.innerE.add(0);
        }
        try (BufferedReader reader = new BufferedReader(new FileReader("src/config.json"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content += line + "\n";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //计算writemodels
        for(int i=0;i<models.size();i++) {
            Model model = models.get(i);
            for(int j=0;j<transactions.size();j++) {
                if(transactions.get(j).getWriteSet().contains(model.getType())) {
                    writeModels.get(j).add(model);
                }
            }
        }
        //初始化cost增量值
        for(int i=0;i<transactions.size();i++) {
            HashSet<Model> writeModelsSet = writeModels.get(i);
            diffPartition.set(i,new Alg1GeneralPartition(entities, new ArrayList<Model>(writeModelsSet)).partitionLedger());  //初始值是没有encrypt集
        }
        costEnc = 0; //加密集为空，交集为空
    }

    public static ArrayList<ArrayList<HashSet<Model>>> getDiffPartition() {
        return diffPartition;
    }

    public static void setDiffPartition(ArrayList<ArrayList<HashSet<Model>>> diffPartition) {
        CostIncre.diffPartition = diffPartition;
    }

    public static ArrayList<Integer> getInnerE() {
        return innerE;
    }

    public static void setInnerE(ArrayList<Integer> innerE) {
        CostIncre.innerE = innerE;
    }

    public static double getCostEnc() {
        return costEnc;
    }

    public static void setCostEnc(double costEnc) {
        CostIncre.costEnc = costEnc;
    }

    public double getCostEnc(String transaction, String costType, String item) {
        double res = 0;
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(content);
            JSONArray jsonArray = (JSONArray) obj;
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                if (jsonObject.keySet().contains(transaction)) {
                    JSONObject jsonObject1 = (JSONObject) jsonObject.get(transaction);
                    if (!costType.equals("cost_enc")) {
                        if (jsonObject1.get(costType) != null) {
                            res = Double.parseDouble(jsonObject1.get(costType).toString());
                        }
                        return res;
                    } else {
                        JSONArray jsonArray1 = (JSONArray) jsonObject1.get(costType);
                        for (int j = 0; j < jsonArray1.size(); j++) {
                            JSONObject jsonObject2 = (JSONObject) jsonArray1.get(j);
                            if (jsonObject2.keySet().contains(item)) {
                                if (jsonObject1.get(costType) != null) {
                                    res = Double.parseDouble(jsonObject2.get(item).toString());
                                }
                                return res;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }


    public double getCostPartitionTest(Model model, boolean print,ArrayList<Model> encryptSet) {
        //先计算coste,增量
        double coste = costEnc;
        //计算costd,与上一次计算的关系不大，要重新计算
        double costd = 0;
        int partA = 0; //差集的子账本数量
        int partB = 0; //交集的e值

        for (int i = 0; i < transactions.size(); i++) {
            Transaction transaction = transactions.get(i);
            double freq = getCostEnc(transaction.getName(), "freq", null);
            double costdt = getCostEnc(transaction.getName(), "cost_dt", null);
            //增量计算coste
            if(model != null) {
                coste += getCostEnc(transaction.getName(), "cost_enc", model.getType()) * freq;
            }
            //计算costd
            HashSet<Model> tmpPartitionPart = new HashSet<>();
            tmpPartitionPart.add(model); //假设只有孤立的一个model被单独分了账本
            if (model != null && transaction.getWriteSet().contains(model.getType()) && diffPartition.get(i).contains(tmpPartitionPart)) { //第i，对应第i个交易
                partA = diffPartition.get(i).size() - 1;
            } else {
                partA = diffPartition.get(i).size();
            }
            if (innerE.get(i) > 0 || (model != null && transaction.getWriteSet().contains(model.getType()))) {
                partB = 1;
            } else {
                partB = 0;
            }
            if (partA + partB >= 2) {
                costd += costdt * 2 * (partA + partB) * freq;
            }
        }
        if(print) {
//            System.out.println((encryptSet.size()+1) + " new cost : " + (costd+coste) + " -- coste = " + coste + " , costd = " + costd);
        }
        return costd + coste;
    }

//    public double getCostPartition(ArrayList<Transaction> transactions, ArrayList<Model> models, HashSet<Model> encryptSet) {
//        return getCostPartition(transactions, models, encryptSet, false);
//    }
//
//    public double getCostPartition(ArrayList<Transaction> transactions, ArrayList<Model> models, HashSet<Model> encryptSet, boolean print) {
//        //part1:get costE
//        double coste = 0;
//        //part2:get costD
//        double costd = 0;
//
//        for (int i = 0; i < transactions.size(); i++) {
//            Transaction transaction = transactions.get(i);
//            double costdt = getCostEnc(transaction.getName(), "cost_dt", null);
//            double freq = getCostEnc(transaction.getName(), "freq", null);
//            HashSet<Model> writeModels = new HashSet<>();
//
//            for (int j = 0; j < models.size(); j++) {
//                Model model = models.get(j);
//                //transaction.getOwner().equals(model.getOwner()) &&
//                if (encryptSet.contains(model)) {
//                    coste += getCostEnc(transaction.getName(), "cost_enc", model.getType()) * freq;
//                }
//                if (transaction.getWriteSet().contains(model.getType())) {
//                    writeModels.add(model);
//                }
//            }
//            //差集
//            HashSet<Model> chaModel = (HashSet<Model>) writeModels.clone();
//            chaModel.removeAll(encryptSet);
//            int partA = new Alg1GeneralPartition(entities, new ArrayList<Model>(chaModel)).partitionLedger().size();
//            //交集
//            HashSet<Model> jiaoModel = (HashSet<Model>) writeModels.clone();
//            jiaoModel.retainAll(encryptSet);
//            int partB = jiaoModel.size() == 0 ? 0 : 1;
//            if (partA + partB >= 2) {
//                costd += costdt * 2 * (partA + partB) * freq;
//            }
//        }
//        if (print)
//            System.out.println("cost: " + (coste + costd) + "; encrypt :" + encryptSet.size() + ", coste = " + coste + ", " + "costd = " + costd);
//        return coste + costd;
//    }

    public static void main(String[] args) {
        double res;
        res = new CostIncre(new ArrayList<Entity>()).getCostEnc("pay", "freq", "order_line");
        System.out.println(res);
        res = new CostIncre(new ArrayList<Entity>()).getCostEnc("pay", "cost_enc", "district");
        System.out.println(res);
    }
}
