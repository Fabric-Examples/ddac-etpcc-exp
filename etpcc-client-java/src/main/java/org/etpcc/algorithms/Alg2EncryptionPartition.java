package org.etpcc.algorithms;

import java.security.Key;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

/**
 * Created by liangjiao on 17/10/26.
 */
public class Alg2EncryptionPartition {

    private ArrayList<Entity> entities;
    private ArrayList<Model> models;
    private ArrayList<Transaction> transactions;
    private Cost cost;
    private static TreeSet<Model> encryptionSet = new TreeSet<>();

    public Alg2EncryptionPartition(ArrayList<Entity> entities, ArrayList<Model> models, ArrayList<Transaction> transactions) {

        this.entities = entities;
        this.models = models;
        this.transactions = transactions;
        this.cost = new Cost(entities,models,transactions);
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

    public TreeSet<Model> getEncryptionSet() {
        return encryptionSet;
    }

    public void setEncryptionSet(TreeSet<Model> encryptionSet) {
        Alg2EncryptionPartition.encryptionSet = encryptionSet;
    }

    //========alg3: select encryptionSet
    public TreeSet<Model> selectEncryptionSet() {
        if (models == null || models.size() == 0) return encryptionSet;
        HashSet<TupleModel<Model, Model>> modelPair = new HashSet<>();
        //construct pairs
        //pairs: the models belong to an owner could construct pair 2-2, the models belong to diff related owner could construct pair.
        //#1: transaction - models
        HashMap<Transaction, HashSet<Model>> transactionToModel = new HashMap<>(); //depart model according to transaction
        for (int i = 0; i < transactions.size(); i++) {
            Transaction transaction = transactions.get(i);
            for (int j = 0; j < models.size(); j++) {
                Model model = models.get(j);
                if (transaction.getWriteSet().contains(model.getType())) {
                    if (!transactionToModel.keySet().contains(transaction)) {
                        transactionToModel.put(transaction, new HashSet<>());
                    }
                    transactionToModel.get(transaction).add(model);
                }
            }
        }
        //#2: modelSet - depart it according to owner
        for (HashSet<Model> modelSet : transactionToModel.values()) {
            ArrayList<Model> shareModels = new ArrayList<>();  //公共model
            HashMap<Entity, HashSet<Model>> warehouseMap = new HashMap<>();
            HashMap<Entity, HashSet<Model>> companyMap = new HashMap<>();
            for (Model model : modelSet) {
                if(model.getType().equals("District") || model.getType().equals("Customer") || model.getType().equals("Item")) {
                    //公共model
                    shareModels.add(model);
                }
                else if (entities.get(model.getId()).getType().equals("company")) {
                    if (!companyMap.keySet().contains(entities.get(model.getId()))) {
                        companyMap.put(entities.get(model.getId()), new HashSet<>());
                    }
                    companyMap.get(entities.get(model.getId())).add(model);
                } else if (entities.get(model.getId()).getType().equals("warehouse")) {
                    if (!warehouseMap.keySet().contains(entities.get(model.getId()))) {
                        warehouseMap.put(entities.get(model.getId()), new HashSet<>());
                    }
                    warehouseMap.get(entities.get(model.getId())).add(model);
                }
            }
            for (Entity warehosueEntity : warehouseMap.keySet()) { //warehouse的model自己相互成对
                ArrayList<Model> warehouseModel = new ArrayList<>(warehouseMap.get(warehosueEntity));
                for (int i = 0; i < warehouseModel.size() - 1; i++) {  //model pair from warehosue itself
                    for (int j = i + 1; j < warehouseModel.size(); j++) {
                        modelPair.add(new TupleModel<>(warehouseModel.get(i), warehouseModel.get(j)));
                    }
                }
            }
            for (Entity companyEntity : companyMap.keySet()) {
                ArrayList<Model> companyModel = new ArrayList<>(companyMap.get(companyEntity));
                for (int i = 0; i < companyModel.size() - 1; i++) {  //model pair from company itself，自己成对
                    for (int j = i + 1; j < companyModel.size(); j++) {
                        modelPair.add(new TupleModel<>(companyModel.get(i), companyModel.get(j)));
                    }
                }
                for (Entity warehosueEntity : warehouseMap.keySet()) { //有合作关系的warehouse和company成对
                    ArrayList<Model> warehouseModel = new ArrayList<>(warehouseMap.get(warehosueEntity));
                    if (Initialization.readGroupMap.get(companyEntity).contains(warehosueEntity)) { //model pair from company and warehouse
                        for (int i = 0; i < companyModel.size(); i++) {
                            for (int j = 0; j < warehouseModel.size(); j++) {
                                modelPair.add(new TupleModel<>(companyModel.get(i), warehouseModel.get(j)));
                            }
                        }

                    }
                }
            }
            //公共model与剩余的结合
            for(Model model : shareModels) {
                for(Model model1 : modelSet) { //model和所有该交易有关的成对
                    modelPair.add(new TupleModel<>(model, model1));
                }
                modelSet.add(model);  //包含自己两两配对
            }
//            System.out.println("print pair : " + modelPair.size());
//            int count = 1;
//            //测试输出modelpair
//            for(TupleModel<Model,Model>tupleModel : modelPair) {
//                System.out.println(count ++ + " : <" + tupleModel.getLeft().getName() + ", " + tupleModel.getRight().getName() + ">");
//            }
        }
        int count = alterEncryptSet(modelPair);
//        System.out.println("======== count = " + count);
        return encryptionSet;
    }

    private int alterEncryptSet(HashSet<TupleModel<Model, Model>> modelPair) {
        HashMap<Model, HashSet<Model>> readGroupMapCp = (HashMap<Model, HashSet<Model>>) Initialization.readGroupMapCp.clone();
        int previousSize = encryptionSet.size();
        int count = 1;
        while (true) {
            for (TupleModel<Model, Model> pair : modelPair) {
                Model model1 = pair.getLeft();
                Model model2 = pair.getRight();
                HashSet<Model> tmpRes1 = (HashSet<Model>) readGroupMapCp.get(model1).clone();
                HashSet<Model> tmpRes2 = (HashSet<Model>) readGroupMapCp.get(model2).clone();
                tmpRes1.retainAll(tmpRes2); //jiao

                if (!tmpRes1.equals(readGroupMapCp.get(model2)) && !encryptionSet.contains(model1)) {
//          Key key = genKey();
//          //encrypt entity1 model
//          byte[] res = encryption(models.get(i), key);
//          String result = res.toString();
                    encryptionSet.add(model1);
                }
                if (!tmpRes1.equals(readGroupMapCp.get(model1)) && !encryptionSet.contains(model2)) {
                    //gen new key
                    //encrypt entity2 model
//          Key key = genKey();
//          byte[] res = encryption(models.get(j), key);
//          String result = res.toString();
                    encryptionSet.add(model2);
                }
                tmpRes1 = (HashSet<Model>) readGroupMapCp.get(model1).clone();
                tmpRes2 = (HashSet<Model>) readGroupMapCp.get(model2).clone();
                tmpRes1.addAll(tmpRes2);
                readGroupMapCp.put(model1, tmpRes1);
                readGroupMapCp.put(model2, tmpRes1);
            }
            if (previousSize == encryptionSet.size()) break;
            previousSize = encryptionSet.size();
            count++;
        }
//        System.out.println("cost partition " + encryptionSet.size() + " --- result : " + cost.getCostPartition(transactions,models,new ArrayList<>(encryptionSet),true));
        return count;
    }

    public ArrayList<HashSet<Model>> alg2EncryptionPartition() {
        ArrayList<HashSet<Model>> res = new ArrayList<>();
        selectEncryptionSet();
        res.add(new HashSet<>(encryptionSet));
        ArrayList<Model> models1 = (ArrayList<Model>) models.clone();
        models1.removeAll(encryptionSet);
        res.addAll(new Alg1GeneralPartition(entities,models1).partitionLedger());
        return res;
    }

    private Key genKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("DESede");
            keyGenerator.init(new SecureRandom());
            SecretKey secretKey = keyGenerator.generateKey();
            DESedeKeySpec deSedeKeySpec = new DESedeKeySpec(secretKey.getEncoded());
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("DESede");
            Key key = secretKeyFactory.generateSecret(deSedeKeySpec);
            return key;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //errs: data models should be encrypted
    private byte[] encryption(Model model, Key key) {
        try {
            Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] result = cipher.doFinal(model.toString().getBytes()); //加密后的entity
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        Initialization initialization = new Initialization(10, 20);
        initialization.constructRel(0, 2);
        Alg2EncryptionPartition alg2EncryptionPartition = new Alg2EncryptionPartition(initialization.getEntities(), initialization.getModels(), initialization.getTransactions());
        ArrayList<HashSet<Model>> modelsPartition = alg2EncryptionPartition.alg2EncryptionPartition();
        System.out.println("partition size : " + modelsPartition.size());
        int count = 1;
        for(HashSet<Model> modelHashSet : modelsPartition) {
            System.out.print(count++ + "th partition：" + modelHashSet.size() + " --- ");
            for(Model model : modelHashSet) {
                System.out.print(model.getName() + ", ");
            }
            System.out.println();
        }
    }
}
