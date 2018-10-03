var ownerMap = {
    History: 1,
    Order: 1,
    New_Order: 1,
    Order_Line: 1,
    Stock: 2
} ;

function getOwner(tableName) {
    var owner = ownerMap[tableName];
    console.debug(tableName, ownerMap, owner);
    switch (owner) {
        case 1: return 'targeCompany';
        case 2: return 'targeWarehouse';
        default: return null;
    }
}

function getActualTableName(tableName, extra) {
    var owner = getOwner(tableName);
    console.debug('owner', owner);
    if(!owner){
        return tableName;
    }
    if(!extra || !extra[owner]) {
        throw new Error('Must specify the owner ID for ' + tableName);
    }
    return tableName + extra[owner];
}

var encPart = {};
var enckey = '1238912ab234ef133123be3232d109af';

function initEncryptionInfo(extra) {
    if(!extra.encryptionSet) {
        console.debug('no partition found!!');
        return;
    }
    extra.encryptionSet.forEach(function(m) {
        encPart[m] = true;
    });
}

function encrypt(payload) {
    var toEnc = JSON.stringify(payload);
    var result = GibberishAES.enc(toEnc, enckey);
    console.debug("Enc Text", result);
    return result;
}

function decrypt(payload) {
    var decText = GibberishAES.dec(payload, enckey);
    console.debug("Dec Text", decText);
    return JSON.parse(decText);
}

/** query and insert registry function*/
function queryAssetRegistry(namespace, tableName, id, extra) {
    var actualTable = getActualTableName(tableName, extra);
    var className = namespace + '.' + actualTable;
    return getAssetRegistry(className)
        .then(function (queryRegistry) {
            return queryRegistry.get(id);
        }).then(function(asset) {
            if(!encPart[actualTable]){
                return asset;
            }
            if(!asset.encPayload) {
                // here we just encrypt the model to simulate the cost
                var toInsert = getSerializer().toJSON(asset, {convertResourcesToRelationships: true});
                asset.encPayload = encrypt(toInsert);
            }
            var dec = decrypt(asset.encPayload);
		    return getSerializer().fromJSON(dec);
        });
}

function complexQuery(namespace, tableName, whereClause, whereCallback, qparam, extra) {
    var actualTable = getActualTableName(tableName, extra);
    var className = namespace + '.' + actualTable;
    var start = new Date().getTime();
    var queryStr = "SELECT " + className + " ";
    if(!encPart[actualTable]) {
        queryStr += whereClause;
    } else {
        qparam = {};
    }
    console.debug(queryStr);
    var q = buildQuery(queryStr);
    var start2;
    return query(q, qparam)
        .then(function(assets) {
            start2 = new Date().getTime();
            if(!encPart[actualTable]){
                return assets;
            }
            var objects = [];
            assets.forEach(function(asset){
                var dec;
                if(!asset.encPayload) {
                    // here we just encrypt the model to simulate the cost
                    var toInsert = getSerializer().toJSON(asset, {convertResourcesToRelationships: true});
                    asset.encPayload = encrypt(toInsert);
                }
                dec = decrypt(asset.encPayload);
                objects.push(getSerializer().fromJSON(dec));
            });
            return objects;
        }).then(function (assets) {
            if(!encPart[actualTable]){
                return assets;
            }
            return assets.filter(whereCallback);
        }).then(function(assets){
            var end = new Date().getTime();
            console.log(">>>>>>> spend in complex query: " + (end -start) + "ms, actual " + (start2 - start) + "ms");
            return assets;
        });;
}


function emitEvent(op, modelName, payload){
    var factory = getFactory();

    var writeSetEvent = factory.newEvent('org.etpcc', 'WriteSetEvent');
    writeSetEvent.operation = factory.newConcept('org.etpcc', 'Operation');
    writeSetEvent.operation.op = op;
    writeSetEvent.operation.modelName = modelName;
    writeSetEvent.operation.payload = payload;
    
    emit(writeSetEvent);
}


function insertAssetRegistry(namespace, tableName, id, newObject, two_PC, extra) {
    tableName = getActualTableName(tableName, extra);
    console.debug('insertA', tableName, extra);
    var className = namespace + '.' + tableName;
    var insertObject = getFactory().newResource(namespace, tableName, id);

    Object.getOwnPropertyNames(newObject).forEach(function(value) {
        insertObject[value] = newObject[value];
    });

	var toInsert;
	if(encPart[tableName]){
        toInsert = getSerializer().toJSON(insertObject, {convertResourcesToRelationships: true});
        insertObject.encPayload = encrypt(toInsert);
    }

    if(two_PC) {
        toInsert = getSerializer().toJSON(insertObject, {convertResourcesToRelationships: true});
        emitEvent('insertA', tableName, JSON.stringify({id: id, content: toInsert}));
        return Promise.resolve();
    }

    return getAssetRegistry(className)
        .then(function (registry) {
           return registry.add(insertObject);
        })
}


function insertParticipantRegistry(namespace, tableName, id, newObject, two_PC) {
    console.debug('insertP', tableName);
    var className = namespace + '.' + tableName;
    var factory = getFactory();
    var insertObject = factory.newResource(namespace, tableName, id);

	Object.getOwnPropertyNames(newObject).forEach(function(value) {
        insertObject[value] = newObject[value];
    });

    if(two_PC) {
        var toInsert = getSerializer().toJSON(insertObject, {convertResourcesToRelationships: true});
        emitEvent('insertP', tableName, JSON.stringify({id: id, content: toInsert}));
        return Promise.resolve();
    }
    return getParticipantRegistry(className)
        .then(function (registry) {
            return registry.add(insertObject);
        });
}


function updateAssetRegistry(namespace, tableName, updateObject, two_PC, extra) {
    tableName = getActualTableName(tableName, extra);
    console.debug('updateA', tableName);
    var className = namespace + '.' + tableName;
    var toUpdate;
    if(encPart[tableName]){
        toUpdate = getSerializer().toJSON(updateObject, {convertResourcesToRelationships: true});
        updateObject.encPayload = encrypt(toUpdate);
    }

    if(two_PC) {
        toUpdate = getSerializer().toJSON(updateObject, {convertResourcesToRelationships: true});
        emitEvent('updateA', tableName, JSON.stringify(toUpdate));
        return Promise.resolve();
    }
    return getAssetRegistry(className)
        .then(function (registry) {
            return registry.update(updateObject);
        })
}

function updateParticipantRegistry(namespace, tableName, updateObject, two_PC) {
    console.debug('updateP', tableName);
    var className = namespace + '.' + tableName;
    if(two_PC) {
        var toUpdate = getSerializer().toJSON(updateObject, {convertResourcesToRelationships: true});
        emitEvent('updateP', tableName, JSON.stringify(toUpdate));
        return Promise.resolve();
    }
    return getParticipantRegistry(className)
        .then(function (registry) {
            return registry.update(updateObject);
        })
}

function removeAssetRegistry(namespace, tableName, removeObjectId, two_PC, extra) {
    tableName = getActualTableName(tableName, extra);
    console.debug('removeA', tableName);
    var className = namespace + '.' + tableName;
    if(two_PC) {
        emitEvent('removeA', tableName, removeObjectId);
        return Promise.resolve();
    }
    return getAssetRegistry(className)
        .then(function (registry) {
            return registry.remove(removeObjectId);
        })
}


