package org.etpcc.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by liangjiao on 17/10/26.
 */
public class Initialization {
    private int numCompany;
    private int numWarehouse;
    private final int numRelation = 5;
    public static HashMap<Entity, HashSet<Entity>> warehouseMap = new HashMap<>();
    public static HashMap<Entity, HashSet<Entity>> readGroupMap = new HashMap<>();
    public static ArrayList<Entity> entities = new ArrayList<>();  //实验中是30个实体参与者
    public static ArrayList<Model> models = new ArrayList<>(); //实验中是10*6+20*3个model
    public static ArrayList<Transaction> transactions = new ArrayList<>(); //所有transaction
    public static HashMap<Model, HashSet<Entity>> readGroupMapCp = new HashMap<>();

    public Initialization(int numCompany, int numWarehouse) {
        this.numCompany = numCompany;
        this.numWarehouse = numWarehouse;
    }

    public int getNumCompany() {
        return numCompany;
    }

    public void setNumCompany(int numCompany) {
        this.numCompany = numCompany;
    }

    public int getNumWarehouse() {
        return numWarehouse;
    }

    public void setNumWarehouse(int numWarehouse) {
        this.numWarehouse = numWarehouse;
    }

    public static HashMap<Entity, HashSet<Entity>> getWarehouseMap() {
        return warehouseMap;
    }


    public static HashMap<Entity, HashSet<Entity>> getReadGroupMap() {
        return readGroupMap;
    }


    public static ArrayList<Entity> getEntities() {
        return entities;
    }

    public static void setEntities(ArrayList<Entity> entities) {
        Initialization.entities = entities;
    }

    public static ArrayList<Model> getModels() {
        return models;
    }

    public static void setModels(ArrayList<Model> models) {
        Initialization.models = models;
    }

    public static ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public static void setTransactions(ArrayList<Transaction> transactions) {
        Initialization.transactions = transactions;
    }

    //========initial: warehouse group: max:4
    public HashMap<Model, HashSet<Entity>> constructRel(int numCrossCompany, int numGroupWarehouse) {
        //transactions
        transactions.add(new Transaction("norder"));
        transactions.add(new Transaction("pay"));
        transactions.add(new Transaction("orders"));
        transactions.add(new Transaction("deliver"));
        transactions.add(new Transaction("stockl"));
        Model district = new Model("District", "District", -1); //share
        Model customer = new Model("Customer", "Customer", -1);
        Model item = new Model("Item", "Item", -1);
        models.add(district);
        models.add(customer);
        models.add(item);
        //numCompany: cross
        for (int i = 1; i <= numWarehouse; i++) {
            Entity warehouse = new Entity("warehouse" + i, "warehouse", new HashSet<>());
            entities.add(warehouse);
            warehouse.getModelSet().add(new Model("Warehouse" + i, "Warehouse", i - 1));
            warehouse.getModelSet().add(new Model("Stock" + i, "Stock", i - 1));
            models.addAll(warehouse.getModelSet());
            warehouseMap.put(warehouse, new HashSet<>());
        }
        int numEachGroup = numWarehouse / numGroupWarehouse;
        if (numWarehouse % numGroupWarehouse != 0) numEachGroup++;
        //construct rel : not cross algorithms
        for (int i = 1; i <= numCompany; i++) {
            Entity company = new Entity("company" + i, "company", new HashSet<>());
            entities.add(company);
            company.getModelSet().add(new Model("History" + i, "History", i + numWarehouse - 1));
            company.getModelSet().add(new Model("New_Order" + i, "New_Order", i + numWarehouse - 1));
            company.getModelSet().add(new Model("Order" + i, "Order", i + numWarehouse - 1));
            company.getModelSet().add(new Model("Order_Line" + i, "Order_Line", i + numWarehouse - 1));
            models.addAll(company.getModelSet());
            //cal index group
            int indexGroup = (i - 1) % numGroupWarehouse;
            HashSet<Entity> companyReadGroup = new HashSet<>();
//            int numRel = i;
            if (i <= numCrossCompany) {  //cross warehouse group
                int index = 0;
                //cross warehouse group
                while (true) {
                    index = ThreadLocalRandom.current().nextInt(numWarehouse);
                    if ((index < indexGroup * numEachGroup && index >= 0) || (index >= (indexGroup + 1) * (numEachGroup) && index < numWarehouse))
                        break;
                }
                Entity warehouse = entities.get(index);
                warehouseMap.get(warehouse).add(company);
                companyReadGroup.add(warehouse);
            }
            while (companyReadGroup.size() < numRelation) {
                int indexLow = indexGroup * numEachGroup;
                int indexHigh = (indexGroup + 1) * numEachGroup > numWarehouse ? numWarehouse : (indexGroup + 1) * numEachGroup;
                int index = ThreadLocalRandom.current().nextInt(indexLow, indexHigh);
                Entity warehouse = entities.get(index);
                warehouseMap.get(warehouse).add(company);  //warehouseMap add companys
                companyReadGroup.add(warehouse);
            }
            companyReadGroup.add(company);  //itself
            readGroupMap.put(company, companyReadGroup);
        }
        //cal warehouse read group
        for (int i = 0; i < numWarehouse; i++) {
            Entity warehouse = entities.get(i);
            HashSet<Entity> warehouseReadGroup = warehouseMap.get(warehouse);  //related companys
            warehouseReadGroup.add(warehouse); //itself
            int indexStart = (i / numEachGroup) * numEachGroup;
            int indexEnd = (i / numEachGroup + 1) * numEachGroup > numWarehouse ? numWarehouse : (i / numEachGroup + 1) * numEachGroup;
            for (int j = indexStart; j < indexEnd; j++) {
                warehouseReadGroup.add(entities.get(j));
            }
            readGroupMap.put(warehouse, warehouseReadGroup);
        }
        constructModelReadGroup();
        return readGroupMapCp;
    }

    public void constructModelReadGroup() {
        HashMap<Entity, HashSet<Entity>> readGroup = Initialization.getReadGroupMap();
        readGroupMapCp.put(models.get(0), new HashSet<>(entities));
        readGroupMapCp.put(models.get(1), new HashSet<>(entities));
        readGroupMapCp.put(models.get(2), new HashSet<>(entities));
        for (Entity entity : readGroup.keySet()) { //拿出entity
            HashSet<Entity> readGroupEntity = readGroup.get(entity);
            HashSet<Model> modelSet = entity.getModelSet();  //拿出model
            for (Model model : modelSet) {
                readGroupMapCp.put(model, readGroupEntity);
            }
        }
    }

    //========initial: warehouse group: max:4
//    public HashMap<Model, HashSet<Entity>> constructRelCompanyNotLimited(int numGroupWarehouse) {
//        //transactions
//        transactions.add(new Transaction("norder"));
//        transactions.add(new Transaction("pay"));
//        transactions.add(new Transaction("orders"));
//        transactions.add(new Transaction("deliver"));
//        transactions.add(new Transaction("stockl"));
//        Model district = new Model("District", "District", -1); //share
//        Model customer = new Model("Customer", "Customer", -1);
//        Model item = new Model("Item", "Item", -1);
//        models.add(district);
//        models.add(customer);
//        models.add(item);
//        //numCompany: cross
//        for (int i = 1; i <= numWarehouse; i++) {
//            Entity warehouse = new Entity("warehouse" + i, "warehouse", new HashSet<>());
//            entities.add(warehouse);
//            warehouse.getModelSet().add(new Model("Warehouse" + i, "Warehouse", i - 1));
//            warehouse.getModelSet().add(new Model("Stock" + i, "Stock", i - 1));
//            models.addAll(warehouse.getModelSet());
//            warehouseMap.put(warehouse, new HashSet<>());
//        }
//        int numEachGroup = numWarehouse / numGroupWarehouse; //每个组有多少个warehouse
//        if (numWarehouse % numGroupWarehouse != 0) numEachGroup++;
//        //计算company的readgroup，5个warehouse和他自己
//        for (int i = 1; i <= numCompany; i++) {
//            Entity company = new Entity("company" + i, "company", new HashSet<>());
//            entities.add(company);
//            company.getModelSet().add(new Model("History" + i, "History", i + numWarehouse - 1));
//            company.getModelSet().add(new Model("New_Order" + i, "New_Order", i + numWarehouse - 1));
//            company.getModelSet().add(new Model("Order" + i, "Order", i + numWarehouse - 1));
//            company.getModelSet().add(new Model("Order_Line" + i, "Order_Line", i + numWarehouse - 1));
//            models.addAll(company.getModelSet());
//            //cal index group
//            int indexGroup = (i - 1) % numGroupWarehouse; //group的index
//            HashSet<Entity> companyReadGroup = new HashSet<>();
//            while (companyReadGroup.size() < numRelation) {
//                int index = ThreadLocalRandom.current().nextInt(0, 20); //随机找warehouse与company联合
//                Entity warehouse = entities.get(index);
//                warehouseMap.get(warehouse).add(company);  //warehouseMap add companys
//                companyReadGroup.add(warehouse);
//            }
//            companyReadGroup.add(company);  //itself
//            readGroupMap.put(company, companyReadGroup);
//        }
//        //cal warehouse read group, 把同组的加进去
//        for (int i = 0; i < numWarehouse; i++) {
//            Entity warehouse = entities.get(i);
//            HashSet<Entity> warehouseReadGroup = warehouseMap.get(warehouse);  //related companys
//            warehouseReadGroup.add(warehouse); //itself
//            int indexStart = (i / numEachGroup) * numEachGroup;
//            int indexEnd = (i / numEachGroup + 1) * numEachGroup > numWarehouse ? numWarehouse : (i / numEachGroup + 1) * numEachGroup;
//            for (int j = indexStart; j < indexEnd; j++) {
//                warehouseReadGroup.add(entities.get(j));
//            }
//            readGroupMap.put(warehouse, warehouseReadGroup);
//        }
//        constructModelReadGroup();
//        return readGroupMapCp;
//    }

    public HashMap<Model, HashSet<Entity>> constructRelPercent(int numCrossCompany, int numGroupWarehouse, int percent) {
        //transactions
        transactions.add(new Transaction("norder"));
        transactions.add(new Transaction("pay"));
        transactions.add(new Transaction("orders"));
        transactions.add(new Transaction("deliver"));
        transactions.add(new Transaction("stockl"));
        Model district = new Model("District", "District", -1); //share
        Model customer = new Model("Customer", "Customer", -1);
        Model item = new Model("Item", "Item", -1);
        models.add(district);
        models.add(customer);
        models.add(item);
        //numCompany: cross
        for (int i = 1; i <= numWarehouse; i++) {
            Entity warehouse = new Entity("warehouse" + i, "warehouse", new HashSet<>());
            entities.add(warehouse);
            warehouse.getModelSet().add(new Model("Warehouse" + i, "Warehouse", i - 1));
            warehouse.getModelSet().add(new Model("Stock" + i, "Stock", i - 1));
            models.addAll(warehouse.getModelSet());
            warehouseMap.put(warehouse, new HashSet<>());
        }
        int numEachGroup = numWarehouse / numGroupWarehouse;
        if (numWarehouse % numGroupWarehouse != 0) numEachGroup++;
        //construct rel : not cross algorithms
        for (int i = 1; i <= numCompany; i++) {
            Entity company = new Entity("company" + i, "company", new HashSet<>());
            entities.add(company);
            company.getModelSet().add(new Model("History" + i, "History", i + numWarehouse - 1));
            company.getModelSet().add(new Model("New_Order" + i, "New_Order", i + numWarehouse - 1));
            company.getModelSet().add(new Model("Order" + i, "Order", i + numWarehouse - 1));
            company.getModelSet().add(new Model("Order_Line" + i, "Order_Line", i + numWarehouse - 1));
            models.addAll(company.getModelSet());
            //cal index group
            int indexGroup = (i - 1) % numGroupWarehouse;
            HashSet<Entity> companyReadGroup = new HashSet<>();
//            int numRel = i;
            if (i <= numCrossCompany) {  //cross warehouse group
                int index = 0;
                //cross warehouse group
                while (true) {
                    index = ThreadLocalRandom.current().nextInt(numWarehouse);
                    if ((index < indexGroup * numEachGroup && index >= 0) || (index >= (indexGroup + 1) * (numEachGroup) && index < numWarehouse))
                        break;
                }
                Entity warehouse = entities.get(index);
                warehouseMap.get(warehouse).add(company);
                companyReadGroup.add(warehouse);
            }
            while (companyReadGroup.size() < numRelation) {
                int indexLow = indexGroup * numEachGroup;
                int indexHigh = (indexGroup + 1) * numEachGroup > numWarehouse ? numWarehouse : (indexGroup + 1) * numEachGroup;
                int index = ThreadLocalRandom.current().nextInt(indexLow, indexHigh);
                Entity warehouse = entities.get(index);
                warehouseMap.get(warehouse).add(company);  //warehouseMap add companys
                companyReadGroup.add(warehouse);
            }
            companyReadGroup.add(company);  //itself
            readGroupMap.put(company, companyReadGroup);
        }
        //cal warehouse read group
        for (int i = 0; i < numWarehouse; i++) {
            Entity warehouse = entities.get(i);
            HashSet<Entity> warehouseReadGroup = warehouseMap.get(warehouse);  //related companys
            warehouseReadGroup.add(warehouse); //itself
            int indexStart = (i / numEachGroup) * numEachGroup;
            int indexEnd = (i / numEachGroup + 1) * numEachGroup > numWarehouse ? numWarehouse : (i / numEachGroup + 1) * numEachGroup;
            for (int j = indexStart; j < indexEnd; j++) {
                warehouseReadGroup.add(entities.get(j));
            }
            readGroupMap.put(warehouse, warehouseReadGroup);
        }
        int leadCompany = numCompany * percent / 100;
        int leadWarehouse = numWarehouse * percent / 100;
        for (int i = 0; i < leadCompany; i++) {
            Entity company = entities.get(i + 20);
            readGroupMap.put(company, new HashSet<>(entities));
        }
        for (int i = 0; i < leadWarehouse; i++) {
            Entity warehouse = entities.get(i);
            readGroupMap.put(warehouse, new HashSet<>(entities));
        }
        constructModelReadGroup();
        return readGroupMapCp;
    }


    public HashMap<Model, HashSet<Entity>> constructRelSmall(int numRel) {
        //transactions
        transactions.add(new Transaction("norder"));
        transactions.add(new Transaction("pay"));
        transactions.add(new Transaction("orders"));
        transactions.add(new Transaction("deliver"));
        transactions.add(new Transaction("stockl"));
        Model district = new Model("District", "District", -1); //share
        Model customer = new Model("Customer", "Customer", -1);
        Model item = new Model("Item", "Item", -1);
        models.add(district);
        models.add(customer);
        models.add(item);
        //numCompany: cross
        for (int i = 1; i <= numWarehouse; i++) {
            Entity warehouse = new Entity("warehouse" + i, "warehouse", new HashSet<>());
            entities.add(warehouse);
            warehouse.getModelSet().add(new Model("Warehouse" + i, "Warehouse", i - 1));
            warehouse.getModelSet().add(new Model("Stock" + i, "Stock", i - 1));
            models.addAll(warehouse.getModelSet());
            warehouseMap.put(warehouse, new HashSet<>());
        }
        ArrayList<Entity> warehouses = (ArrayList<Entity>) entities.clone();
        //计算company的readgroup，5个warehouse和他自己
        for (int i = 1; i <= numCompany; i++) {
            Entity company = new Entity("company" + i, "company", new HashSet<>());
            entities.add(company);
            company.getModelSet().add(new Model("History" + i, "History", i + numWarehouse - 1));
            company.getModelSet().add(new Model("New_Order" + i, "New_Order", i + numWarehouse - 1));
            company.getModelSet().add(new Model("Order" + i, "Order", i + numWarehouse - 1));
            company.getModelSet().add(new Model("Order_Line" + i, "Order_Line", i + numWarehouse - 1));
            models.addAll(company.getModelSet());
            HashSet<Entity> companyReadGroup = new HashSet<>();
            while (companyReadGroup.size() < numRel) {
                int index = ThreadLocalRandom.current().nextInt(0, numWarehouse); //随机找warehouse与company联合
                Entity warehouse = entities.get(index);
                warehouseMap.get(warehouse).add(company);  //warehouseMap add companys
                companyReadGroup.add(warehouse);
            }
            companyReadGroup.add(company);  //itself
            readGroupMap.put(company, companyReadGroup);
        }
        //cal warehouse read group, 把同组的加进去
        for (int i = 0; i < numWarehouse; i++) {
            Entity warehouse = entities.get(i);
            HashSet<Entity> warehouseReadGroup = warehouseMap.get(warehouse);  //related companys
            warehouseReadGroup.addAll(warehouses);
            readGroupMap.put(warehouse, warehouseReadGroup);
        }
        constructModelReadGroup();
        return readGroupMapCp;
    }

//    public HashMap<Model, HashSet<Entity>> constructRelShareWarehosueCompanyNotLimited(int numGroupWarehouse) {
//        //transactions
//        transactions.add(new Transaction("norder"));
//        transactions.add(new Transaction("pay"));
//        transactions.add(new Transaction("orders"));
//        transactions.add(new Transaction("deliver"));
//        transactions.add(new Transaction("stockl"));
//        Model district = new Model("District", "District", -1); //share
//        Model customer = new Model("Customer", "Customer", -1);
//        Model item = new Model("Item", "Item", -1);
//        models.add(district);
//        models.add(customer);
//        models.add(item);
//        //numCompany: cross
//        for (int i = 1; i <= numWarehouse; i++) {
//            Entity warehouse = new Entity("warehouse" + i, "warehouse", new HashSet<>());
//            entities.add(warehouse);
//            warehouse.getModelSet().add(new Model("Warehouse" + i, "Warehouse", i - 1));
//            warehouse.getModelSet().add(new Model("Stock" + i, "Stock", i - 1));
//            models.addAll(warehouse.getModelSet());
//            warehouseMap.put(warehouse, new HashSet<>());
//        }
//        int numEachGroup = numWarehouse / numGroupWarehouse; //每个组有多少个warehouse
//        if (numWarehouse % numGroupWarehouse != 0) numEachGroup++;
//        //计算company的readgroup，5个warehouse和他自己
//        for (int i = 1; i <= numCompany; i++) {
//            Entity company = new Entity("company" + i, "company", new HashSet<>());
//            entities.add(company);
//            company.getModelSet().add(new Model("History" + i, "History", i + numWarehouse - 1));
//            company.getModelSet().add(new Model("New_Order" + i, "New_Order", i + numWarehouse - 1));
//            company.getModelSet().add(new Model("Order" + i, "Order", i + numWarehouse - 1));
//            company.getModelSet().add(new Model("Order_Line" + i, "Order_Line", i + numWarehouse - 1));
//            models.addAll(company.getModelSet());
//            //cal index group
//            int indexGroup = (i - 1) % numGroupWarehouse; //group的index
//            HashSet<Entity> companyReadGroup = new HashSet<>();
//            while (companyReadGroup.size() < numRelation) {
//                int index = ThreadLocalRandom.current().nextInt(0, 20); //随机找warehouse与company联合
//                Entity warehouse = entities.get(index);
//                warehouseMap.get(warehouse).add(company);  //warehouseMap add companys
//                companyReadGroup.add(warehouse);
//            }
//            companyReadGroup.add(company);  //itself
//            readGroupMap.put(company, companyReadGroup);
//        }
//        //cal warehouse read group, 把同组的加进去
//        int j;
//        for (int i = 0; i < numWarehouse; i++) {
//            int limit = i + numEachGroup > numWarehouse ? numWarehouse : i + numEachGroup;
//            HashSet<Entity> warehouseReadGroup = new HashSet<>();
//            for (j = i; j < limit; j++) {
//                Entity warehouse = entities.get(j);
//                warehouseReadGroup.addAll(warehouseMap.get(warehouse));  //related companys
//                warehouseReadGroup.add(warehouse); //itself
//            }
//
//            for (j = i; j < limit; j++) {
//                Entity warehouse = entities.get(j);
//                readGroupMap.put(warehouse, warehouseReadGroup);
//            }
////            System.out.print("warehouse readgroup : ");
////            for(Entity warehouse : warehouseReadGroup) {
////                System.out.print(warehouse.getName() + ", ");
////            }
////            System.out.println();
//            i = limit;
//        }
//        constructModelReadGroup();
//        return readGroupMapCp;
//    }

    public static void main(String[] args) {
        Initialization initialization = new Initialization(1, 2);
        initialization.constructRelSmall(2);
        System.out.println(readGroupMapCp.size());
        for (Model model : readGroupMapCp.keySet()) {
            System.out.print(model.getName() + " --- " + readGroupMapCp.get(model).size() + " : ");
            for (Entity entity : readGroupMapCp.get(model)) {
                System.out.print(entity.getName() + " , ");
            }
            System.out.println();
        }
    }
}
