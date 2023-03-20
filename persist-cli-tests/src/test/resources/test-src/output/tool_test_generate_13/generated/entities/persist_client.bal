// AUTO-GENERATED FILE. DO NOT MODIFY.

// This file is an auto-generated file by Ballerina persistence layer for model.
// It should not be modified by hand.

import ballerina/persist;
import ballerina/jballerina.java;
import ballerinax/mysql;

const PROFILE = "profiles";
const USER = "users";
const MULTIPLE_ASSOCIATIONS = "multipleassociations";

public client class Client {
    *persist:AbstractPersistClient;

    private final mysql:Client dbClient;

    private final map<persist:SQLClient> persistClients;

    private final record {|persist:Metadata...;|} metadata = {
        "profiles": {
            entityName: "Profile",
            tableName: `Profile`,
            fieldMetadata: {
                id: {columnName: "id"},
                name: {columnName: "name"},
                ownerId: {columnName: "ownerId"},
                multipleassociationsId: {columnName: "multipleassociationsId"},
                "owner.id": {relation: {entityName: "owner", refField: "id"}},
                "owner.name": {relation: {entityName: "owner", refField: "name"}},
                "owner.multipleassociationsId": {relation: {entityName: "multipleAssociations", refField: "multipleassociationsId"}},
                "multipleAssociations.id": {relation: {entityName: "multipleAssociations", refField: "id"}},
                "multipleAssociations.name": {relation: {entityName: "multipleAssociations", refField: "name"}}
            },
            keyFields: ["id"],
            joinMetadata: {
                owner: {entity: User, fieldName: "owner", refTable: "User", refColumns: ["id"], joinColumns: ["ownerId"], 'type: persist:ONE_TO_ONE},
                multipleAssociations: {entity: MultipleAssociations, fieldName: "multipleAssociations", refTable: "MultipleAssociations", refColumns: ["id"], joinColumns: ["multipleassociationsId"], 'type: persist:ONE_TO_ONE}
            }
        },
        "users": {
            entityName: "User",
            tableName: `User`,
            fieldMetadata: {
                id: {columnName: "id"},
                name: {columnName: "name"},
                multipleassociationsId: {columnName: "multipleassociationsId"},
                "profile.id": {relation: {entityName: "profile", refField: "id"}},
                "profile.name": {relation: {entityName: "profile", refField: "name"}},
                "profile.ownerId": {relation: {entityName: "owner", refField: "ownerId"}},
                "profile.multipleassociationsId": {relation: {entityName: "multipleAssociations", refField: "multipleassociationsId"}},
                "multipleAssociations.id": {relation: {entityName: "multipleAssociations", refField: "id"}},
                "multipleAssociations.name": {relation: {entityName: "multipleAssociations", refField: "name"}}
            },
            keyFields: ["id"],
            joinMetadata: {
                profile: {entity: Profile, fieldName: "profile", refTable: "Profile", refColumns: ["ownerId"], joinColumns: ["id"], 'type: persist:ONE_TO_ONE},
                multipleAssociations: {entity: MultipleAssociations, fieldName: "multipleAssociations", refTable: "MultipleAssociations", refColumns: ["id"], joinColumns: ["multipleassociationsId"], 'type: persist:ONE_TO_ONE}
            }
        },
        "multipleassociations": {
            entityName: "MultipleAssociations",
            tableName: `MultipleAssociations`,
            fieldMetadata: {
                id: {columnName: "id"},
                name: {columnName: "name"},
                "profile.id": {relation: {entityName: "profile", refField: "id"}},
                "profile.name": {relation: {entityName: "profile", refField: "name"}},
                "profile.ownerId": {relation: {entityName: "owner", refField: "ownerId"}},
                "profile.multipleassociationsId": {relation: {entityName: "multipleAssociations", refField: "multipleassociationsId"}},
                "owner.id": {relation: {entityName: "owner", refField: "id"}},
                "owner.name": {relation: {entityName: "owner", refField: "name"}},
                "owner.multipleassociationsId": {relation: {entityName: "multipleAssociations", refField: "multipleassociationsId"}}
            },
            keyFields: ["id"],
            joinMetadata: {
                profile: {entity: Profile, fieldName: "profile", refTable: "Profile", refColumns: ["multipleassociationsId"], joinColumns: ["id"], 'type: persist:ONE_TO_ONE},
                owner: {entity: User, fieldName: "owner", refTable: "User", refColumns: ["multipleassociationsId"], joinColumns: ["id"], 'type: persist:ONE_TO_ONE}
            }
        }
    };

    public function init() returns persist:Error? {
        mysql:Client|error dbClient = new (host = host, user = user, password = password, database = database, port = port);
        if dbClient is error {
            return <persist:Error>error(dbClient.message());
        }
        self.dbClient = dbClient;
        self.persistClients = {
            profiles: check new (self.dbClient, self.metadata.get(PROFILE)),
            users: check new (self.dbClient, self.metadata.get(USER)),
            multipleassociations: check new (self.dbClient, self.metadata.get(MULTIPLE_ASSOCIATIONS))
        };
    }

    isolated resource function get profiles(ProfileTargetType targetType = <>) returns stream<targetType, persist:Error?> = @java:Method {
        'class: "io.ballerina.stdlib.persist.QueryProcessor",
        name: "query"
    } external;

    isolated resource function get profiles/[int id](ProfileTargetType targetType = <>) returns targetType|persist:Error = @java:Method {
        'class: "io.ballerina.stdlib.persist.QueryProcessor",
        name: "queryOne"
    } external;

    isolated resource function post profiles(ProfileInsert[] data) returns int[]|persist:Error {
        _ = check self.persistClients.get(PROFILE).runBatchInsertQuery(data);
        return from ProfileInsert inserted in data
            select inserted.id;
    }

    isolated resource function put profiles/[int id](ProfileUpdate value) returns Profile|persist:Error {
        _ = check self.persistClients.get(PROFILE).runUpdateQuery(id, value);
        return self->/profiles/[id].get();
    }

    isolated resource function delete profiles/[int id]() returns Profile|persist:Error {
        Profile result = check self->/profiles/[id].get();
        _ = check self.persistClients.get(PROFILE).runDeleteQuery(id);
        return result;
    }

    isolated resource function get users(UserTargetType targetType = <>) returns stream<targetType, persist:Error?> = @java:Method {
        'class: "io.ballerina.stdlib.persist.QueryProcessor",
        name: "query"
    } external;

    isolated resource function get users/[int id](UserTargetType targetType = <>) returns targetType|persist:Error = @java:Method {
        'class: "io.ballerina.stdlib.persist.QueryProcessor",
        name: "queryOne"
    } external;

    isolated resource function post users(UserInsert[] data) returns int[]|persist:Error {
        _ = check self.persistClients.get(USER).runBatchInsertQuery(data);
        return from UserInsert inserted in data
            select inserted.id;
    }

    isolated resource function put users/[int id](UserUpdate value) returns User|persist:Error {
        _ = check self.persistClients.get(USER).runUpdateQuery(id, value);
        return self->/users/[id].get();
    }

    isolated resource function delete users/[int id]() returns User|persist:Error {
        User result = check self->/users/[id].get();
        _ = check self.persistClients.get(USER).runDeleteQuery(id);
        return result;
    }

    isolated resource function get multipleassociations(MultipleAssociationsTargetType targetType = <>) returns stream<targetType, persist:Error?> = @java:Method {
        'class: "io.ballerina.stdlib.persist.QueryProcessor",
        name: "query"
    } external;

    isolated resource function get multipleassociations/[int id](MultipleAssociationsTargetType targetType = <>) returns targetType|persist:Error = @java:Method {
        'class: "io.ballerina.stdlib.persist.QueryProcessor",
        name: "queryOne"
    } external;

    isolated resource function post multipleassociations(MultipleAssociationsInsert[] data) returns int[]|persist:Error {
        _ = check self.persistClients.get(MULTIPLE_ASSOCIATIONS).runBatchInsertQuery(data);
        return from MultipleAssociationsInsert inserted in data
            select inserted.id;
    }

    isolated resource function put multipleassociations/[int id](MultipleAssociationsUpdate value) returns MultipleAssociations|persist:Error {
        _ = check self.persistClients.get(MULTIPLE_ASSOCIATIONS).runUpdateQuery(id, value);
        return self->/multipleassociations/[id].get();
    }

    isolated resource function delete multipleassociations/[int id]() returns MultipleAssociations|persist:Error {
        MultipleAssociations result = check self->/multipleassociations/[id].get();
        _ = check self.persistClients.get(MULTIPLE_ASSOCIATIONS).runDeleteQuery(id);
        return result;
    }

    public function close() returns persist:Error? {
        error? result = self.dbClient.close();
        if result is error {
            return <persist:Error>error(result.message());
        }
        return result;
    }
}

