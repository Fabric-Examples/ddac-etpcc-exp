package org.etpcc.algorithms;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Created by liangjiao on 17/10/26.
 */
public class Cost {

    private ArrayList<Entity> entities;
    private ArrayList<Model> models;
    private ArrayList<Transaction> transactions;
    private ArrayList<HashSet<Model>> writeModelsSet;
    private String content = "";
    private static int countGen = 0;

    public Cost(double rate) {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/config.json"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content += line + "\n";
            }
            alterJsonFreq(rate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Cost(ArrayList<Entity> entities, ArrayList<Model> models, ArrayList<Transaction> transactions) {
        this.entities = entities;
        this.models = models;
        this.transactions = transactions;
        try (BufferedReader reader = new BufferedReader(new FileReader("src/config.json"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content += line + "\n";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.writeModelsSet = new ArrayList<>();

        for (int i = 0; i < transactions.size(); i++) {
            Transaction transaction = transactions.get(i);
            writeModelsSet.add(new HashSet<>());
            for (int j = 0; j < models.size(); j++) {
                Model model = models.get(j);
                if (transaction.getWriteSet().contains(model.getType())) {
                    writeModelsSet.get(i).add(model);
                }
            }
        }

    }

    public double getCostPartitionGA(boolean[] unit, boolean print) {
        //构造encryptSet集合
        ArrayList<Model> encryptSet = new ArrayList<>();
        for (int i = 0; i < models.size(); i++) {
            if (unit[i]) {
                encryptSet.add(models.get(i));
            }
        }
        //part1:get costE
        double coste = 0;
        //part2:get costD
        double costd = 0;

        for (int i = 0; i < transactions.size(); i++) {
            Transaction transaction = transactions.get(i);
            double costdt = getCostEnc(transaction.getName(), "cost_dt", null);
            double freq = getCostEnc(transaction.getName(), "freq", null);

            for (int j = 0; j < models.size(); j++) {
                Model model = models.get(j);
                //transaction.getOwner().equals(model.getOwner()) &&
                if (encryptSet.contains(model)) {
                    coste += getCostEnc(transaction.getName(), "cost_enc", model.getType()) * freq;
                }
            }
            //差集
            HashSet<Model> chaModel = (HashSet<Model>) writeModelsSet.get(i).clone();
            chaModel.removeAll(encryptSet);
            int partA = new Alg1GeneralPartition(entities, new ArrayList<Model>(chaModel)).partitionLedger().size();
            //交集
            HashSet<Model> jiaoModel = (HashSet<Model>) writeModelsSet.get(i).clone();
            jiaoModel.retainAll(encryptSet);
            int partB = jiaoModel.size() == 0 ? 0 : 1;
            if (partA + partB >= 2) {
                costd += costdt * 2 * (partA + partB) * freq;
            }
        }
        if (print) {
//            System.out.println(++countGen + " gen cost : " + (costd + coste) + " -- coste = " + coste + " , costd = " + costd + ", encrypt size: " + encryptSet.size());
        }
        return coste + costd;
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

    private static void addIndentBlank(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append('\t');
        }
    }

    public static String formatJson(String jsonStr) {
        if (null == jsonStr || "".equals(jsonStr))
            return "";
        StringBuilder sb = new StringBuilder();
        char last = '\0';
        char current = '\0';
        int indent = 0;
        boolean isInQuotationMarks = false;
        for (int i = 0; i < jsonStr.length(); i++) {
            last = current;
            current = jsonStr.charAt(i);
            switch (current) {
                case '"':
                    if (last != '\\'){
                        isInQuotationMarks = !isInQuotationMarks;
                    }
                    sb.append(current);
                    break;
                case '{':
                case '[':
                    sb.append(current);
                    if (!isInQuotationMarks) {
                        sb.append('\n');
                        indent++;
                        addIndentBlank(sb, indent);
                    }
                    break;
                case '}':
                case ']':
                    if (!isInQuotationMarks) {
                        sb.append('\n');
                        indent--;
                        addIndentBlank(sb, indent);
                    }
                    sb.append(current);
                    break;
                case ',':
                    sb.append(current);
                    if (last != '\\' && !isInQuotationMarks) {
                        sb.append('\n');
                        addIndentBlank(sb, indent);
                    }
                    break;
                default:
                    sb.append(current);
            }
        }

        return sb.toString();
    }

    public void alterJsonFreq(double rate) {
        BigDecimal first = new BigDecimal(10*(1-rate)/2);
        BigDecimal sec = new BigDecimal(10*rate/3);
        double f1 = first.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();  
        double f2 = sec.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue(); 
        double[] freq = new double[5];
        freq[0] = f1;
        freq[1] = f1;
        freq[2] = f2;
        freq[3] = f2;
        freq[4] = f2;
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(content);
            JSONArray jsonArray = (JSONArray) obj;
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                for(Object transaction : jsonObject.keySet()) {
                    JSONObject jsonObject1 = (JSONObject) jsonObject.get(transaction);
                    jsonObject1.put("freq",freq[i]);
                }
            }
            String fileName = "src/config.json";
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));// 输出新的json文件
            bw.write(formatJson(jsonArray.toString()));
            bw.flush();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double getCostPartition(ArrayList<Transaction> transactions, ArrayList<Model> models, ArrayList<Model> encryptSet, boolean print) {
        //part1:get costE
        double coste = 0;
        //part2:get costD
        double costd = 0;

        for (int i = 0; i < transactions.size(); i++) {
            Transaction transaction = transactions.get(i);
            double costdt = getCostEnc(transaction.getName(), "cost_dt", null);
            double freq = getCostEnc(transaction.getName(), "freq", null);
            HashSet<Model> writeModels = new HashSet<>();

            for (int j = 0; j < models.size(); j++) {
                Model model = models.get(j);
                //transaction.getOwner().equals(model.getOwner()) &&
                if (encryptSet.contains(model)) {
                    coste += getCostEnc(transaction.getName(), "cost_enc", model.getType()) * freq;
                }
                if (transaction.getWriteSet().contains(model.getType())) {
                    writeModels.add(model);
                }
            }
            //差集
            HashSet<Model> chaModel = (HashSet<Model>) writeModels.clone();
            chaModel.removeAll(encryptSet);
            int partA = new Alg1GeneralPartition(entities, new ArrayList<Model>(chaModel)).partitionLedger().size();
            //交集
            HashSet<Model> jiaoModel = (HashSet<Model>) writeModels.clone();
            jiaoModel.retainAll(encryptSet);
            int partB = jiaoModel.size() == 0 ? 0 : 1;
            if (partA + partB >= 2) {
                costd += costdt * 2 * (partA + partB) * freq;
            }

        }
        if (print) {
//            System.out.println(encryptSet.size() + " old cost : " + (costd + coste) + " -- coste = " + coste + " , costd = " + costd);
        }
        return coste + costd;
    }

    public double getCostPartitionEnc(ArrayList<Transaction> transactions, ArrayList<Model> models, ArrayList<Model> encryptSet, boolean print) {
        //part1:get costE
        double coste = 0;

        for (int i = 0; i < transactions.size(); i++) {
            Transaction transaction = transactions.get(i);
            double costdt = getCostEnc(transaction.getName(), "cost_dt", null);
            double freq = getCostEnc(transaction.getName(), "freq", null);
            HashSet<Model> writeModels = new HashSet<>();

            for (int j = 0; j < models.size(); j++) {
                Model model = models.get(j);
                //transaction.getOwner().equals(model.getOwner()) &&
                if (encryptSet.contains(model)) {
                    coste += getCostEnc(transaction.getName(), "cost_enc", model.getType()) * freq;
                }
            }
        }
        if (print) {
//            System.out.println(encryptSet.size() + " old cost : " + (costd + coste) + " -- coste = " + coste + " , costd = " + costd);
        }
        return coste;
    }

}
