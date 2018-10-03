package org.etpcc.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by liangjiao on 17/10/26.
 */
public class Alg1GeneralPartition {

    private ArrayList<Entity> entities;
    private ArrayList<Model> models;

    public Alg1GeneralPartition(ArrayList<Entity> entities, ArrayList<Model> models) {
        this.entities = entities;
        this.models = models;
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

    //=======alg1: partition ledger
    public ArrayList<HashSet<Model>> partitionLedger() {
        ArrayList<HashSet<Model>> res = new ArrayList<>();
        HashMap<HashSet<Entity>, HashSet<Model>> partitionMap = new HashMap<>();
        for (int i = 0; i < models.size(); i++) {
            Model model = models.get(i);
            HashSet<Entity> readGroup = Initialization.readGroupMapCp.get(model);
            if (!partitionMap.containsKey(readGroup)) {
                HashSet<Model> modelSet = new HashSet<>();
                modelSet.add(models.get(i));
                partitionMap.put(readGroup, modelSet);
            } else {
                partitionMap.get(readGroup).add(models.get(i));
            }
        }
        res.addAll(new ArrayList<>(partitionMap.values()));
        return res;
    }

    public ArrayList<HashSet<Model>> alg1GeneralPartition() {
        ArrayList<HashSet<Model>> res = new ArrayList<>();
        res.add(new HashSet<>()); //加密集为空
        res.addAll(partitionLedger());
        return res;
    }

    public static void main(String[] args) {
        Initialization initialization = new Initialization(1,3);
//        initialization.constructRel(0,2);
        initialization.constructRelSmall(2);
        Alg1GeneralPartition alg1GeneralPartition = new Alg1GeneralPartition(initialization.getEntities(), initialization.getModels());
        ArrayList<HashSet<Model>> modelsPartition = alg1GeneralPartition.alg1GeneralPartition();
        System.out.println(initialization.getModels().size());
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
