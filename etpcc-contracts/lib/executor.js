/**
 * Track the trade of a commodity from one trader to another
 * @param {org.etpcc.NewOrder} order_p - the trade to be processed
 * @transaction
 */
function new_order_transaction(order_p) {

    /**
     * 需要查询的字段
     */
    var two_PC = order_p.two_PC;
    var extra = order_p;
    var factory = getFactory();
    initEncryptionInfo(extra);
    var start = new Date().getTime();

    var orderObject, new_orderObject;

    var D_NEXT_O_ID = order_p.district.D_NEXT_O_ID;
    var order_lines_computed = [];
    console.log(order_p.district);
    order_p.district.D_NEXT_O_ID++;
    if(!order_p.transactionId) {
        // should not happen
        order_p.transactionId = Math.random().toString().substr(2);
    }

    var transactionId = order_p.transactionId.replace(/-/g,'');

    /** update district */
    return updateAssetRegistry('org.etpcc', 'District', order_p.district, two_PC, extra)
        .then(function () {
            /**生成 order记录 对象 */
            orderObject = {};
            orderObject.O_ID = D_NEXT_O_ID.toString();
            orderObject.O_D_ID = order_p.district.D_ID;
            orderObject.O_W_ID = order_p.warehouse.W_ID;
            orderObject.O_C_ID = order_p.customer.C_ID;

            orderObject.O_ID__O_D_ID__O_W_ID = 'O#' + transactionId + 'D#' + orderObject.O_D_ID + 'W#' + orderObject.O_W_ID;
            orderObject.O_ENTRY_D = new Date();
            orderObject.O_CARRIER_ID = null;
            orderObject.O_OL_CNT = order_p.order_lines.length; /*generate count of orderlines*/
	        var flag = 1;
	        for (var i = 0; i < order_p.order_lines.length; i++) {

                if (order_p.warehouse.W_ID !== order_p.order_lines[i].OL_SUPPLY_W_ID.W_ID) {
                    flag = 0;
                    break;
                }
            }
            orderObject.O_ALL_LOCAL = flag;
            //改为relation
            //orderObject.O_C_ID__O_D_ID__O_W_ID = factory.newRelationship('org.etpcc', 'Customer', order_p.customer.C_ID);

            return insertAssetRegistry('org.etpcc', 'Order', orderObject.O_ID__O_D_ID__O_W_ID, orderObject, two_PC, extra);
        }).then(function () {
            /**生成 newOrder记录 对象 */
            new_orderObject = {};
            new_orderObject.NO_O_ID = D_NEXT_O_ID.toString();
            new_orderObject.NO_D_ID = order_p.district.D_ID;
            new_orderObject.NO_W_ID = order_p.warehouse.W_ID;
            new_orderObject.NO_O_ID__NO_D_ID__NO_W_ID = 'O#' + transactionId + 'D#' + new_orderObject.NO_D_ID + 'W#' + new_orderObject.NO_W_ID;
            //new_orderObject.F_NO_O_ID__NO_D_ID__NO_W_ID = factory.newRelationship('org.etpcc', 'Order', orderObject.O_ID__O_D_ID__O_W_ID);

            /**插入 一条新 new_order 记录 */
            return insertAssetRegistry('org.etpcc', 'New_Order', new_orderObject.NO_O_ID__NO_D_ID__NO_W_ID, new_orderObject, two_PC, extra);

        }).then(function () {
            var promises = [];
            order_p.order_lines.forEach(function (order_line_current) {
                var stockId = 'I#' + order_line_current.OL_I_ID.I_ID + 'W#' + order_line_current.OL_SUPPLY_W_ID.W_ID;
                var promise = queryAssetRegistry('org.etpcc', 'Stock', stockId, extra)
                    .then(function (stock) {

                    if (stock.S_QUANTITY - order_line_current.OL_QUANTITY >= 10) {
                        stock.S_QUANTITY -= order_line_current.OL_QUANTITY;
                    } else {
                        stock.S_QUANTITY = 91 + stock.S_QUANTITY - order_line_current.OL_QUANTITY;
                    }
                    stock.S_YTD += order_line_current.OL_QUANTITY;
                    stock.S_ORDER_CNT += 1;
                    if (order_line_current.OL_SUPPLY_W_ID.W_ID != order_p.warehouse.W_ID) {
                        stock.S_REMOTE_CNT += 1;
                    }

                    var BRAND_GENERIC = 'G';
                    if (order_line_current.OL_I_ID.I_DATA.indexOf('ORIGINAL') >= 0 &&
                        stock.S_DATA.indexOf('ORIGINAL') >= 0) {
                        BRAND_GENERIC = 'B';
                    }

                    order_lines_computed.push({
                        OL_I_ID: order_line_current.OL_I_ID.I_ID,
                        OL_SUPPLY_W_ID: order_line_current.OL_SUPPLY_W_ID.W_ID,
                        OL_QUANTITY: order_line_current.OL_QUANTITY,
                        I_PRICE: order_line_current.OL_I_ID.I_PRICE,
                        I_NAME: order_line_current.OL_I_ID.I_NAME,
                        I_DATA: order_line_current.OL_I_ID.I_DATA,
                        S_QUANTITY: stock.S_QUANTITY,
                        BRAND_GENERIC: BRAND_GENERIC,
                        OL_AMOUNT: order_line_current.OL_QUANTITY * order_line_current.OL_I_ID.I_PRICE,
                        S_DIST_XX: 'S_DIST_0' + order_line_current.OL_I_ID.I_ID
                    });

                    return updateAssetRegistry('org.etpcc', 'Stock', stock, two_PC, extra);
                });

                promises.push(promise);

            });
            return Promise.all(promises);
        }).then(function () {
            var promises = [];

            order_lines_computed.forEach(function (current, index) {
                var new_orderLineObject = {
                    OL_O_ID: D_NEXT_O_ID.toString(),
                    OL_D_ID: order_p.district.D_ID,
                    O_W_ID: order_p.warehouse.W_ID,
                    OL_NUMBER: 1, // no one uses it
                    OL_I_ID: current.OL_I_ID,
                    OL_SUPPLY_W_ID: current.OL_SUPPLY_W_ID,
                    OL_DELIVERY_D: new Date(),
                    OL_QUANTITY: current.OL_QUANTITY,
                    OL_AMOUNT: current.OL_AMOUNT,
                    OL_DIST_INFO: current.S_DIST_XX,
                };
                var stockId = 'W#' + current.OL_SUPPLY_W_ID + 'I#' + current.OL_I_ID;
                console.log(">>" + index, new_orderLineObject, current);
                new_orderLineObject.id = 'O#' + transactionId + 'D#' + new_orderLineObject.OL_D_ID + 'W#' + new_orderLineObject.O_W_ID;
                //new_orderLineObject.order = getFactory().newRelationship('org.etpcc', 'Order', new_orderLineObject.id);
                //new_orderLineObject.stock = getFactory().newRelationship('org.etpcc', 'Stock', stockId);
                var promise = insertAssetRegistry('org.etpcc', 'Order_Line', new_orderLineObject.id, new_orderLineObject, two_PC, extra);
                promises.push(promise);
            });
            return Promise.all(promises);
        }).then(function(){
            var end = new Date().getTime();
            console.log(">>>>>>> new_order_transaction " + (end -start) + "ms");
            return true;
        });
}

/**
 * Track the trade of a commodity from one trader to another
 * @param {org.etpcc.Payment} payment_p - the trade to be processed
 * @transaction
 */
function payment_transaction(payment_p) {
    var factory = getFactory();
    var two_PC = payment_p.two_PC;
    var extra = payment_p;
    initEncryptionInfo(extra);

    /** update customer */
    payment_p.customer.C_BALANCE -= payment_p.H_AMOUNT;
    payment_p.customer.C_YTD_PAYMENT += payment_p.H_AMOUNT;
    payment_p.customer.C_PAYMENT_CNT++;
    if (payment_p.customer.C_CREDIT == 'BC') {
        var tmpStr = payment_p.customer.C_ID + payment_p.customer.C_D_ID + payment_p.customer.C_W_ID + payment_p.district.D_ID + payment_p.warehouse.W_ID + payment_p.H_AMOUNT;
        payment_p.customer.C_DATA = tmpStr + payment_p.customer.C_DATA;
        if (payment_p.customer.C_DATA.length > 500) {
            payment_p.customer.C_DATA = payment_p.customer.C_DATA.substr(0, 500);
        }
    }

    return updateAssetRegistry('org.etpcc', 'Customer', payment_p.customer, two_PC, extra)
        .then(function () {
            /** update warehouse */
            payment_p.warehouse.W_YTD += payment_p.H_AMOUNT;
            return updateParticipantRegistry('org.etpcc', 'Warehouse', payment_p.warehouse, two_PC)
                .then(function () {
                    /** update district */
                    payment_p.district.D_YTD += payment_p.H_AMOUNT;
                    return updateAssetRegistry('org.etpcc', 'District', payment_p.district, two_PC, extra)
                        .then(function () {
                            var new_history_object = {
                                H_ID: 'H#' + new Date().getTime(),
                                H_C_ID: payment_p.customer.C_ID,
                                H_C_D_ID: payment_p.customer.C_D_ID,
                                H_D_ID: payment_p.district.D_ID,
                                H_W_ID: payment_p.warehouse.W_ID,
                                H_DATE: new Date(),
                                H_AMOUNT: payment_p.H_AMOUNT,
                                H_DATA: payment_p.warehouse.W_NAME + payment_p.district.D_NAME
                            };

                            //var f_customer_id = payment_p.customer.C_ID + payment_p.customer.C_D_ID + payment_p.customer.C_W_ID;
                            //var f_district_id = payment_p.district.D_ID + payment_p.warehouse.W_ID;
                            //new_history_object.H_C_ID__H_C_D_ID__H_C_W_ID = factory.newRelationship('org.etpcc', 'Customer', f_customer_id);
                            //new_history_object.H_D_ID__H_W_ID = factory.newRelationship('org.etpcc', 'District', f_district_id);
                            console.log("new_history_object", new_history_object);

                            return insertAssetRegistry('org.etpcc', 'History', new_history_object.H_ID, new_history_object, two_PC, extra);
                        })
                })
        });
}

/**
 * Track the trade of a commodity from one trader to another
 * @param {org.etpcc.Delivery} delivery_p - the trade to be processed
 * @transaction
 */
function delivery_transaction(delivery_p) {
    // var D_IDs = [1, 2, 3, 4, 5];
    var two_PC = delivery_p.two_PC;
    var extra = delivery_p;
    initEncryptionInfo(extra);

    var start = new Date().getTime();
    var D_IDs = delivery_p.districtNum;
    var promises = [];
    D_IDs.forEach(function (D_ID, index) {

        //for simple etpcc, we only process the first two districts.
        if(index > 1) {
            return;
        }

        var tmp_O_C_ID;
        var tmp_OL_AMOUNT = 0;

        var _update_order;

        var queryStr = "WHERE (NO_W_ID == '" + delivery_p.warehouse.W_ID + "'" + "AND NO_D_ID == '" + D_ID + "')";
        var queryCallback = function(a) {
            return a.NO_W_ID == delivery_p.warehouse.W_ID && a.NO_D_ID == D_ID;
         };
        // Execute the query.
        var promise = complexQuery("org.etpcc", "New_Order", queryStr, queryCallback, {}, extra)
            .then(function (assets) {
                var min_new_order;
                //not handle yet: find minimal new_order
                assets.forEach(function (t) {
                    var tmp = 9999999999;
                    Object.getOwnPropertyNames(t).forEach(function(value){
                        console.log("t >>" + t[value]);
                        if (parseInt(t.NO_O_ID) < tmp) {
                            tmp = parseInt(t.NO_O_ID);
                            min_new_order = t;
                        }
                    });
                });

                if(!min_new_order) {
                    return Promise.resolve();
                } else {
                    return Promise.resolve().then(function () {
                        // remove new_order object
                        console.log("min_new_order:id >> " + min_new_order.NO_O_ID__NO_D_ID__NO_W_ID);
                        return removeAssetRegistry('org.etpcc', 'New_Order', min_new_order.NO_O_ID__NO_D_ID__NO_W_ID, two_PC, extra);
                    }).then(function () {
                        console.log("remove>>");
                        //update order object
                        var id =  min_new_order.NO_O_ID__NO_D_ID__NO_W_ID;

                        console.log("id >>:: " + id);

                        return queryAssetRegistry('org.etpcc', 'Order', id, extra);
                    }).then(function (_order) {
                        //update order table
                        tmp_O_C_ID = _order.O_C_ID;
                        _order.O_CARRIER_ID = delivery_p.O_CARRIER_ID;
                        console.log('updated order', _order);
                        _update_order = _order;
                        return updateAssetRegistry('org.etpcc', 'Order', _order, two_PC, extra);
                    }).then(function () {
                        //query order-line
                        //BUG: query can not resolve "WHERE (xx AND xx AND xx)"
                        var queryStatement = 'WHERE (OL_D_ID == _$O_D_ID AND OL_O_ID == _$O_ID)';
                        var qparam = {
                            O_D_ID: _update_order.O_D_ID,
                            O_ID: _update_order.O_ID
                        };
                        var querycb = function(a){return a.OL_D_ID == _update_order.O_D_ID && a.OL_O_ID == _update_order.O_ID};

                        // Execute the query.
                        return complexQuery("org.etpcc", "Order_Line", queryStatement, querycb, qparam, extra);
                    }).then(function (orderLine_assets) {
                        // composer bug work around
                        orderLine_assets = orderLine_assets.filter(function(a){return a.O_W_ID == _update_order.O_W_ID});

                        var promises2 = [];
                        orderLine_assets.forEach(function (orderLine_asset) {
                            console.log("orderLine_assets: " + orderLine_assets.id);
                            // Process each asset.
                            orderLine_asset.OL_DELIVERY_D = new Date();
                            tmp_OL_AMOUNT += orderLine_asset.OL_AMOUNT;
                            Object.getOwnPropertyNames(orderLine_asset).forEach(function(value) {
                                console.log("orderLine_asset >>:: [" + value + "]" + orderLine_asset[value]);
                            });

                            promises2.push(updateAssetRegistry('org.etpcc', 'Order_Line', orderLine_asset, two_PC, extra));
                        });
                        return Promise.all(promises2);
                    }).then(function () {
                        //query customer
                        //id = 'C#' + tmp_O_C_ID + 'D#' + D_ID + 'W#' + delivery_p.warehouse.W_ID;
                        return queryAssetRegistry('org.etpcc', 'Customer', tmp_O_C_ID);
                    }).then(function (_customer) {
                        console.log("tmp_OL_AMOUNT >>:: " + tmp_OL_AMOUNT);
                        console.log("_customer.C_BALANCE >>:: " + _customer.C_BALANCE);
                        _customer.C_BALANCE += tmp_OL_AMOUNT;
                        console.log("_customer.C_D_ID 2>>:: " + _customer.C_D_ID);
                        _customer.C_DELIVERY_CNT++;

                        console.log("updated customer", _customer);

                        //!!!! BUG: can not update _customer !!!
                        return updateAssetRegistry('org.etpcc', 'Customer', _customer, two_PC, extra);
                        // return insertAssetRegistry('org.etpcc', 'Customer', _customer.id, _customer)

                    });
                }
            });
        promises.push(promise);
    });
    return Promise.all(promises).then(function(){
        var end = new Date().getTime();
        console.log(">>>>>>> delivery_transaction " + (end -start) + "ms");
        return true;
    });
}

/**
 * Track the trade of a commodity from one trader to another
 * @param {org.etpcc.OrderStatus} orderStatus_p - the trade to be processed
 * @transaction
 */
function orderStatus_transaction(orderStatus_p) {
    var extra = orderStatus_p;
    initEncryptionInfo(extra);

    //  find the order of all warehouses for customer
    var queryStatement = 'WHERE ( (O_D_ID == _$C_D_ID) AND (O_C_ID == _$C_ID) )';
    var  qparam = {
        C_D_ID: orderStatus_p.customer.C_D_ID,
        C_ID: orderStatus_p.customer.C_ID
    };

    var queryCallback = function(o) {
        return o.O_D_ID ==orderStatus_p.customer.C_D_ID && o.O_C_ID == orderStatus_p.customer.C_ID;
    };
    var start = new Date().getTime();
    
    return complexQuery("org.etpcc", "Order", queryStatement, queryCallback, qparam, extra)
        .then(function (orders) {
        var max_order;
        orders.forEach(function (t) {
            var tmp = -1;
            Object.getOwnPropertyNames(t).forEach(function(value) {
                console.log("t >>" + t[value]);
                if (parseInt(t.O_ID) > tmp) {
                    tmp = parseInt(t.O_ID);
                    max_order = t;
                }
            });
        });

        if(!max_order) {
            return Promise.resolve();
        } else {
            return Promise.resolve()
                .then(function () {
                    var queryStatement = 'WHERE ( (OL_D_ID == _$O_D_ID) AND (OL_O_ID == _$O_ID) )';
                    var qparam = {
                        O_D_ID: max_order.O_D_ID,
                        O_ID: max_order.O_ID
                    };
                    var queryCallback = function(a){
                        return a.OL_D_ID == max_order.O_D_ID && a.OL_O_ID == max_order.O_ID;
                    };
                    return complexQuery("org.etpcc", "Order_Line", queryStatement, queryCallback, qparam, extra);
                }).then(function (orderLine_assets) {
                    // composer bug work around
                    orderLine_assets = orderLine_assets.filter(function(a){return a.O_W_ID == max_order.O_W_ID});

                    orderLine_assets.forEach(function (orderLine_asset) {
                        console.log("orderLine_asset[OL_I_ID] >>" + orderLine_asset.OL_I_ID);
                        console.log("orderLine_asset[OL_SUPPLY_W_ID] >>" + orderLine_asset.OL_SUPPLY_W_ID);
                        console.log("orderLine_asset[OL_QUANTITY] >>" + orderLine_asset.OL_QUANTITY);
                        console.log("orderLine_asset[OL_AMOUNT] >>" + orderLine_asset.OL_AMOUNT);
                        console.log("orderLine_asset[OL_DELIVERY_D] >>" + orderLine_asset.OL_DELIVERY_D);
                    });
                });
        }
    }).then(function(){
        var end = new Date().getTime();
        console.log(">>>>>>> orderStatus_transaction " + (end -start) + "ms");
        return true;
    });
}

/**
 * Track the trade of a commodity from one trader to another
 * @param {org.etpcc.StockLevel} stockLevel_p - the trade to be processed
 * @transaction
 */
function stockLevel_transaction(stockLevel_p) {
    var extra = stockLevel_p;
    initEncryptionInfo(extra);

    if(!stockLevel_p.district.D_W_IDS || stockLevel_p.district.D_W_IDS.length === 0) {
        console.log('No warehouse is associated. ');
        return Promise.resolve();
    }
    var queryStatement = 'WHERE ( (OL_D_ID == _$O_D_ID) AND (OL_O_ID > _$D_NEXT_O_ID_20 )  )';
    var queryCallback = function(a){
        return a.OL_D_ID == stockLevel_p.district.D_ID && a.OL_O_ID > parseInt(stockLevel_p.district.D_NEXT_O_ID) - 20;
    };
    var qparam = {
        O_D_ID: stockLevel_p.district.D_ID,
        //  D_NEXT_O_ID: parseInt(stockLevel_p.district.D_NEXT_O_ID),
        D_NEXT_O_ID_20: parseInt(stockLevel_p.district.D_NEXT_O_ID) - 20
    };

    var start = new Date().getTime();
    // TODO only the first warehouse is queried now!!
    // Execute the query.
    return complexQuery("org.etpcc", "Order_Line", queryStatement, queryCallback, qparam, extra)
        .then(function (orderLine_assets) {
            // composer bug work around
            orderLine_assets = orderLine_assets.filter(function(a){return a.O_W_ID == stockLevel_p.district.D_W_IDS[0].W_ID});

            console.log('orderLine_assets',orderLine_assets);
            return orderLine_assets;
        }).then(function(ols) {
            if(!ols || ols.length == 0) {
                return;
            }
            //TODO to concat the or conditions for all the orderlines in ols
            var queryStatement = 'WHERE ( S_I_ID == _$OL_I_ID AND (S_QUANTITY < _$threshold ) )';
            var queryCallback = function(a) {
                return a.S_I_ID == ols[0].OL_I_ID && a.S_QUANTITY < 1000;
            };
            var query_param = {
                OL_I_ID: ols[0].OL_I_ID,
                threshold: 1000
            };
            return complexQuery("org.etpcc", "Stock", queryStatement, queryCallback, query_param, extra)
                .then(function (stock_assets) {
                // composer bug work around
                stock_assets = stock_assets.filter(function(a){return a.S_W_ID == stockLevel_p.district.D_W_IDS[0].W_ID});

                console.log('stock_assets',stock_assets);
            });
        }).then(function(){
            var end = new Date().getTime();
            console.log(">>>>>>> stockLevel_transaction " + (end -start) + "ms");
            return true;
        });

}


function ontestQuery(queryParam) {
    var queryStatement = queryParam.query;
    console.log(queryStatement);
    var query_OrderLine = buildQuery(queryStatement);
    // Execute the query.
    return query(query_OrderLine);
}

/**
 * Fabric XA
 * @param {org.etpcc.BranchTransaction} branch - the branch transaction to be commited, only write set is exposed.
 * @transaction
 */
function BranchTransaction(branch) {
    var operations = branch.operations;
    var extra = branch;
    if(extra.two_PC === true) {
        throw new Error("Cannot do 2pc during branch tx");
    }
    var promises = [];
    operations.forEach(function(operation) {
        var promise, parsedPayload, object;
        switch(operation.op) {
        case 'insertA':
            parsedPayload = JSON.parse(operation.payload);
            object = getSerializer().fromJSON(parsedPayload.content);
            promise = insertAssetRegistry('org.etpcc', operation.modelName, parsedPayload.id, object, false, extra);
            break;
        case 'insertP':
            parsedPayload = JSON.parse(operation.payload);
            object = getSerializer().fromJSON(parsedPayload.content);
            promise = insertParticipantRegistry('org.etpcc', operation.modelName, parsedPayload.id, object);
            break;
        case 'updateA':
            parsedPayload = JSON.parse(operation.payload);
            object = getSerializer().fromJSON(parsedPayload);
            promise = updateAssetRegistry('org.etpcc', operation.modelName, object, false, extra);
            break;
        case 'updateP':
            parsedPayload = JSON.parse(operation.payload);
            object = getSerializer().fromJSON(parsedPayload);
            promise = updateParticipantRegistry('org.etpcc', operation.modelName, object);
            break;
        case 'removeA':
            promise = removeAssetRegistry('org.etpcc', operation.modelName, operation.payload, false, extra);
            break;
        default:
            throw new Error('Unknown op code : ' + operation.op);
        }
        promises.push(promise);
    });
    return Promise.all(promises);
}