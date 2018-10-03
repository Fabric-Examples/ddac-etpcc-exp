
function random_a_string(x, y) {
    var len = x + Math.floor(Math.random() * ((y + 1) - x));
    var characters = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
    var chars_len = characters.length;
    var result = '';

    for (var i = 0; i < len; i++) {
        result = result + characters.substr(1 + Math.floor(Math.random() * chars_len), 1);
    }

    return result;
}

function random_n_string(x, y) {
    var len = x + Math.floor(Math.random() * ((y + 1) - x));
    var characters = '0123456789';
    var chars_len = characters.length;
    var result = '';

    for (var i = 0; i < len; i++) {
        result = result + characters.substr(1 + Math.floor(Math.random() * chars_len), 1);
    }

    return result;
}

function generate_c_last(num) {
    var arr = new Array('BAR', 'OUGHT', 'ABLE', 'PRI', 'PRES', 'ESE', 'ANTI', 'CALLY', 'ATION', 'EING');

    var first = Math.floor(num / 100);
    var second = Math.floor((num % 100) / 10);
    var third = Math.floor(num % 10);

    return arr[first] + arr[second] + arr[third];
}

/**C is a run-time constant random ly chosen within [0 .. A] that can be varied without altering performance.
The same C value, per field (C_LAST, C_ID, and OL_I_ID), must be used by all emulated terminals. */
function NURand(A, x, y, C) {
    var result = ((Math.floor(Math.random() * (A + 1)) | ((x + Math.floor(Math.random() * ((y + 1) - x))) + C)) % (y - x + 1)) + x;
    return result;
}

/**
 * Track the trade of a commodity from one trader to another
 * @param {org.etpcc.Data_add_Item} data_add_item - the trade to be processed
 * @transaction
 */
function add_item(data_add_item) {
    var num = data_add_item.recordNum;
    var series = 1;
    var promises = [];
    for (var i = 0; i < num; i++) {
        /**生成 new_item记录 对象 */
        var new_item_Object = new Object();
        /**生成数据 */
        new_item_Object.I_ID = (series++).toString();
        new_item_Object.I_IM_ID = (Math.floor(Math.random() * 10000) + 1).toString();
        new_item_Object.I_NAME = (random_a_string(14, 24)).toString();
        new_item_Object.I_PRICE = 1 + (Math.random() * 99);
        if (Math.random() < 0.1) {
            var tmpStr = (random_a_string(26, 50)).toString();
            var start = Math.floor(Math.random * (tmpStr.length - 8)) + 1;
            var head = tmpStr.substring(0, start);
            var tail = tmpStr.substring(start + 8);
            new_item_Object.I_DATA = head + 'ORIGINAL' + tail;
        } else {
            new_item_Object.I_DATA = (random_a_string(26, 50)).toString();
        }

        /**插入 一条新 new_item 记录 */
        var promise = insertAssetRegistry('org.etpcc', 'Item', new_item_Object.I_ID, new_item_Object);
        promises.push(promise);
    }
    return Promise.all(promises);
}

/**
 * Track the trade of a commodity from one trader to another
 * @param {org.etpcc.Data_add_Warehouse} data_add_warehouse - the trade to be processed
 * @transaction
 */
function add_warehouse(data_add_warehouse) {
    var num = data_add_warehouse.recordNum;
    var series = 1;
    var promises = [];
    for (var i = 0; i < num; i++) {
        /**生成 new_warehouse记录 对象 */
        var new_warehouse_Object = new Object();
        /**生成数据 */
        new_warehouse_Object.W_ID = (series++).toString();
        new_warehouse_Object.W_NAME = (random_a_string(6, 10)).toString();
        new_warehouse_Object.W_STREET_1 = (random_a_string(10, 20)).toString();
        new_warehouse_Object.W_STREET_2 = (random_a_string(10, 20)).toString();
        new_warehouse_Object.W_CITY = (random_a_string(10, 20)).toString();
        new_warehouse_Object.W_STATE = (random_a_string(2, 2)).toString();
        new_warehouse_Object.W_ZIP = (random_n_string(4, 4)).toString() + '11111';
        new_warehouse_Object.W_TAX = Math.random() * 0.2;
        new_warehouse_Object.W_YTD = 300000.0;

        /**插入 一条新 new_warehouse 记录 */
        var promise = insertParticipantRegistry('org.etpcc', 'Warehouse', new_warehouse_Object.W_ID, new_warehouse_Object);
        promises.push(promise);
    }
    return Promise.all(promises);
}

/**
 * Track the trade of a commodity from one trader to another
 * @param {org.etpcc.Data_add_District} data_add_district - the trade to be processed
 * @transaction
 */
function add_district(data_add_district) {
    var warehouseNum = data_add_district.warehouseNum;
    var districtNum = data_add_district.districtNum;
    var districtPerWareHouse = data_add_district.perWarehouse;
    var warehouseChains = Math.floor(data_add_district.warehouseNum * 
        districtPerWareHouse / districtNum);
    var chainSize = Math.floor(warehouseNum / warehouseChains);
    console.log('chain: ', warehouseChains, chainSize);
    var series = 1;
    var factory = getFactory();
    var promises = [];
    for (var i = 0; i < districtNum; i++) {
        var new_district_Object = new Object();
        new_district_Object.D_ID = (series++).toString();
        new_district_Object.D_NAME = (random_a_string(6, 10)).toString();
        new_district_Object.D_STREET_1 = (random_a_string(10, 20)).toString();
        new_district_Object.D_STREET_2 = (random_a_string(10, 20)).toString();
        new_district_Object.D_CITY = (random_a_string(10, 20)).toString();
        new_district_Object.D_STATE = (random_a_string(2, 2)).toString();
        new_district_Object.D_ZIP = (random_n_string(4, 4)).toString() + '11111';
        new_district_Object.D_TAX = Math.random() * 0.2;
        new_district_Object.D_YTD = 300000.0;
        new_district_Object.D_NEXT_O_ID = 3001;
        new_district_Object.D_W_IDS = [];
        for (var j = 0; j < warehouseChains; j++) {
            new_district_Object.D_W_IDS.push(
                factory.newRelationship('org.etpcc', 'Warehouse', 
                    (j * chainSize + (i % chainSize) + 1).toString())
            )
        }
        /**插入 一条新 new_district 记录 */
        var promise = insertAssetRegistry('org.etpcc', 'District', new_district_Object.D_ID, new_district_Object);
        promises.push(promise);
    }
    return Promise.all(promises);
}

/**
 * Track the trade of a commodity from one trader to another
 * @param {org.etpcc.Data_add_Stock} data_add_stock - the trade to be processed
 * @transaction
 */
function add_stock(data_add_stock) {
    var warehouseNum = data_add_stock.warehouseNum;
    var factory = getFactory();
    var itemNum = data_add_stock.itemNum;
    var promises = [];
    for (var i = 1; i <= warehouseNum; i++) {
        for (var j = 1; j <= itemNum; j++) {
            var new_stock_Object = new Object();

            new_stock_Object.S_I_ID__S_W_ID = 'I#' + j.toString() + 'W#' + i.toString();
            new_stock_Object.S_QUANTITY = 10 + Math.floor(Math.random() * (100 - 10));
            new_stock_Object.S_DIST_01 = (random_a_string(24, 24)).toString();
            new_stock_Object.S_DIST_02 = (random_a_string(24, 24)).toString();
            new_stock_Object.S_DIST_03 = (random_a_string(24, 24)).toString();
            new_stock_Object.S_DIST_04 = (random_a_string(24, 24)).toString();
            new_stock_Object.S_DIST_05 = (random_a_string(24, 24)).toString();
            new_stock_Object.S_DIST_06 = (random_a_string(24, 24)).toString();
            new_stock_Object.S_DIST_07 = (random_a_string(24, 24)).toString();
            new_stock_Object.S_DIST_08 = (random_a_string(24, 24)).toString();
            new_stock_Object.S_DIST_09 = (random_a_string(24, 24)).toString();
            new_stock_Object.S_DIST_10 = (random_a_string(24, 24)).toString();
            new_stock_Object.S_YTD = 0;
            new_stock_Object.S_ORDER_CNT = 0;
            new_stock_Object.S_REMOTE_CNT = 0;
            if (Math.random() < 0.1) {
                var tmpStr = (random_a_string(26, 50)).toString();
                var start = Math.floor(Math.random * (tmpStr.length - 8)) + 1;
                var head = tmpStr.substring(0, start);
                var tail = tmpStr.substring(start + 8);
                new_stock_Object.S_DATA = head + 'ORIGINAL' + tail;
            } else {
                new_stock_Object.S_DATA = (random_a_string(26, 50)).toString();
            }

            new_stock_Object.S_W_ID =  i.toString();
            //no need for queryParticipantRegistry('org.etpcc.Warehouse', 'org.etpcc', 'Warehouse', i.toString());
            new_stock_Object.S_I_ID = j.toString();
            //no need queryAssetRegistry('org.etpcc.Item', 'org.etpcc', 'Item', j.toString());            

            /**插入 一条新 new_stock 记录 */
            var promise = insertAssetRegistry('org.etpcc', 'Stock', new_stock_Object.S_I_ID__S_W_ID, new_stock_Object, false, {targeWarehouse: i.toString()});
            promises.push(promise);
        }
    }
    return Promise.all(promises);

}

function add_company(data_add_company) {
    var factory = getFactory();
    return getParticipantRegistry('org.etpcc.Company')
        .then(function (registry) {
            var promises = [];
            for (var i = 1; i <= data_add_company.recordNum; i++) {
                var company = factory.newResource('org.etpcc', 'Company', i.toString(), {generate: 'sample'});
                company.P_NAME = (random_a_string(24, 24)).toString();
                company.P_STREET_1 = (random_a_string(24, 24)).toString();
                company.P_STREET_2 = (random_a_string(24, 24)).toString();
                company.P_CITY = (random_a_string(24, 24)).toString();
                company.P_STATE = (random_a_string(24, 24)).toString();
                company.P_ZIP = (random_a_string(24, 24)).toString();
                promises.push(
                    registry.add(company)
                );
            }
            return Promise.all(promises);
        });
}

/**
 * Generate customers of Extended TPC-C
 * @param {org.etpcc.Data_add_Customer} tx - the workload to be added
 * @transaction
 */
function add_customer(data_add_customer) {
    return getAssetRegistry('org.etpcc.Customer')
        .then(function (registry) {
            var promises = [];
            var factory = getFactory();
            console.log('data_add_customer', data_add_customer.recordNum, data_add_customer.perDistrict)
            for (var i = 1; i <= data_add_customer.recordNum; i++) {
                var customer = factory.newResource('org.etpcc', 'Customer', i.toString(), {generate: 'sample'});
                customer.C_D_ID = (((i - 1) % data_add_customer.perDistrict) + 1).toString();
                customer.C_FIRST = (random_a_string(24, 24)).toString();
                customer.C_MIDDLE = (random_a_string(24, 24)).toString();
                customer.C_LAST = (random_a_string(24, 24)).toString();
                customer.C_STREET_1 = (random_a_string(24, 24)).toString();
                customer.C_STREET_2 = (random_a_string(24, 24)).toString();
                customer.C_CITY = (random_a_string(24, 24)).toString();
                customer.C_STATE = (random_a_string(24, 24)).toString();
                customer.C_ZIP = (random_a_string(24, 24)).toString();
                customer.C_PHONE = (random_a_string(24, 24)).toString();
                customer.C_SINCE = new Date();
                customer.C_CREDIT  = (random_a_string(24, 24)).toString();
                customer.C_CREDIT_LIM = 10000;
                customer.C_DISCOUNT = Math.random();
                customer.C_BALANCE = 10000;
                customer.C_YTD_PAYMENT = 10000;
                customer.C_PAYMENT_CNT = 10000;
                customer.C_DELIVERY_CNT = 10000;
                customer.C_DATA = (random_a_string(24, 24)).toString();
                promises.push(
                    registry.add(customer)
                );
            }
            return Promise.all(promises);
        });
}


/**
 * Generate the workload of Extended TPC-C
 * @param {org.etpcc.GenerateLoad} tx - the workload to be added
 * @transaction
 */
function generateLoad(tx) {
    var numOfCustomers = tx.customersPerDistrict * tx.numOfDistricts;
    console.log('numOfCustomers', numOfCustomers);
    return add_company({recordNum: tx.numOfCompanies})
        .then(function(){
            return add_warehouse({recordNum: tx.numOfWarehouses});
        }).then(function(){
            return add_district({districtNum: tx.numOfDistricts, warehouseNum: tx.numOfWarehouses, perWarehouse: tx.districtsPerWarehouse});
        }).then(function(){
            return add_customer({recordNum: numOfCustomers, perDistrict: tx.customersPerDistrict});
        }).then(function(){
            return add_item({recordNum: tx.numOfItems});
        }).then(function(){
            return add_stock({warehouseNum: tx.numOfWarehouses, itemNum: tx.numOfItems});
        });
}

function deleteAll(model){
    return getAssetRegistry('org.etpcc.' + model)
        .then(function (registry) {
            return registry.removeAll();
        });
}

/**
 * Clear the workload of Extended TPC-C
 * @param {org.etpcc.DeleteAll} tx - the workload to clear
 * @transaction
 */
function DeleteAll(tx) {
    return deleteAll('Customer')
        .then(function(){
            return deleteAll('District');
        }).then(function(){
            return deleteAll('History');
        }).then(function(){
            return deleteAll('Item');
        }).then(function(){
            return deleteAll('New_Order');
        }).then(function(){
            return deleteAll('Order');
        }).then(function(){
            return deleteAll('Order_Line');
        }).then(function(){
            return deleteAll('Stock');
        });
}



/**
 * Clear the workload of Extended TPC-C
 * @param {org.etpcc.Count} tx - the workload to clear
 * @transaction
 */
function Count(tx) {
    return getAssetRegistry('org.etpcc.' + tx.model)
        .then(function (registry) {
            return registry.getAll();
        }).then(function(a) {
            console.log("!!! Length", a.length)
        });
}


/**
 * Generate Order_line of Extended TPC-C
 * @param {org.etpcc.Data_add_all_OrderLine} tx - the workload to be added
 * @transaction
 */
function add_all_orderLine(data_add_all_orderLine) {
    var promises2 = [];
    for(var j = 1; j <= 10; j++) {
        var promise2 = getAssetRegistry('org.etpcc.Order_Line'+j)
        .then(function (registry) {
            return Promise.all([registry, registry.getAll(), j]);
        }).then(function(presult){
            var registry = presult[0];
            var data = presult[1];
            var j = presult[2];
            var len = data.length;
            console.log('how long ' + len);
            var promises = [];
            var factory = getFactory();
            // console.log('data_add_orderLine', data_add_orderLine.recordNum, j)
            for (var i = len; i <= data_add_all_orderLine.recordNum; i++) {
                var new_orderLineObject = factory.newResource('org.etpcc', 'Order_Line'+j, i.toString(), {generate: 'sample'});             
                
                var OL_O_ID = i.toString();        
                var OL_D_ID = (i % 50) + 1;

                new_orderLineObject.OL_O_ID = OL_O_ID.toString();
                new_orderLineObject.OL_D_ID= OL_D_ID.toString();
                new_orderLineObject.O_W_ID="1";
                new_orderLineObject.OL_NUMBER= 1;
                new_orderLineObject.OL_I_ID="1";
                new_orderLineObject.OL_SUPPLY_W_ID= "1";
                new_orderLineObject.OL_DELIVERY_D=new Date();
                new_orderLineObject.OL_QUANTITY= 1.0;
                new_orderLineObject.OL_AMOUNT=1.0;
                new_orderLineObject.OL_DIST_INFO= "current.S_DIST_XX";

                var stockId = 'W#1I#1';
                console.log(">>" + i, new_orderLineObject)
                new_orderLineObject.id = i.toString();
                new_orderLineObject.order = getFactory().newRelationship('org.etpcc', 'Order', (i/10).toString());
                promises.push(
                    registry.add(new_orderLineObject)
                );
            }
            return Promise.all(promises);
        });
        promises2.push(promise2)
    } 
    return Promise.all(promises2)
}


/**
 * Generate Order_line of Extended TPC-C
 * @param {org.etpcc.Data_add_all_Order} tx - the workload to be added
 * @transaction
 */
function add_all_order(data_add_all_order) {
    var promises2 = [];
    for(var j = 1; j <= 10; j++) {
        var promise2 = getAssetRegistry('org.etpcc.Order'+j)
        .then(function (registry) {
            return Promise.all([registry, registry.getAll(), j]);
        }).then(function(presult){
            var registry = presult[0];
            var data = presult[1];
            var j = presult[2];
            var len = data.length;
            console.log('how long ' + len);
            var promises = [];
            var factory = getFactory();
            for (var i = len; i <= data_add_all_order.recordNum; i++) {
                var new_orderObject = factory.newResource('org.etpcc', 'Order'+j, i.toString(), {generate: 'sample'});             
                    
                new_orderObject.O_ID = i.toString();
                new_orderObject.O_D_ID = '2'
                new_orderObject.O_W_ID = '2'
                new_orderObject.O_C_ID = '2'

                new_orderObject.O_ENTRY_D = new Date();
                new_orderObject.O_OL_CNT = 1
                new_orderObject.O_ALL_LOCAL = 1

                new_orderObject.O_ID__O_D_ID__O_W_ID = i.toString();
              
                promises.push(
                    registry.add(new_orderObject)
                );
            }
            return Promise.all(promises);
        });
        promises2.push(promise2)
    } 
    return Promise.all(promises2)
}