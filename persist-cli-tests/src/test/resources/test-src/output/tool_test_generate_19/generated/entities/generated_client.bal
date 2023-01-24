// AUTO-GENERATED FILE. DO NOT MODIFY.

// This file is an auto-generated file by Ballerina persistence layer for entities.
// It should not be modified by hand.

import ballerina/persist;
import ballerina/sql;
import ballerina/time;
import ballerinax/mysql;

const MULTIPLEASSOCIATIONS = "MultipleAssociations";
const USER = "User";
const PROFILE = "Profile";

public client class EntitiesClient {

    private final mysql:Client dbClient;

    private final map<persist:SQLClient> persistClients;

    private final record {|persist:Metadata...;|} metadata = {
        "multipleassociations": {
            entityName: "MultipleAssociations",
            tableName: `MultipleAssociations`,
            id: {columnName: "id", 'type: int},
            name: {columnName: "name", 'type: string},
            profileId: {columnName: "profileId", 'type: int},
            userId: {columnName: "userId", 'type: int},
            keyFields: ["id"]
        },
        "user": {
            entityName: "User",
            tableName: `User`,
            id: {columnName: "id", 'type: int},
            name: {columnName: "name", 'type: string},
            profileId: {columnName: "profileId", 'type: int},
            keyFields: ["id"]
        },
        "profile": {
            entityName: "Profile",
            tableName: `Profile`,
            id: {columnName: "id", 'type: int},
            name: {columnName: "name", 'type: string},
            keyFields: ["id"]
        }
    };

    public function init() returns persist:Error? {
        self.dbClient = check new (host = host, user = user, password = password, database = database, port = port);
        self.persistClients = {
            multipleassociations: check new (self.dbClient, self.metadata.get(MULTIPLEASSOCIATIONS),
            user: check new (self.dbClient, self.metadata.get(USER),
            profile: check new (self.dbClient, self.metadata.get(PROFILE)        };
    }

    isolated resource function get multipleassociations() returns stream<MultipleAssociations, error?> {
        stream<record {}, sql:Error?>|persist:Error result = self.persistClients.get(MULTIPLEASSOCIATIONS).runReadQuery(MultipleAssociations);
        if result is persist:Error {
            return new stream<MultipleAssociations, persist:Error?>(new MultipleAssociationsStream((), result));
        } else {
            return new stream<MultipleAssociations, persist:Error?>(new MultipleAssociationsStream(result));
        }
    }
    isolated resource function get multipleassociations/[int id]() returns MultipleAssociations|error {
        return (check self.persistClients.get(MULTIPLEASSOCIATIONS).runReadByKeyQuery(MultipleAssociations, id)).cloneWithType(MultipleAssociations);
    }
    isolated resource function post multipleassociations(MultipleAssociationsInsert[] data) returns [int][]|persist:Error {
        _ = check self.persistClients.get("multipleassociations").runBatchInsertQuery(data);
        return from MultipleAssociationsInsert inserted in data
            select [inserted.id];
    }
    isolated resource function put multipleassociations/[int id](MultipleAssociationsUpdate value) returns MultipleAssociations|persist:Error {
        _ = check self.persistClients.get("multipleassociations").runUpdateQuery({"id": id, }, data);
        return self->/multipleassociations/[id].get();
    }
    isolated resource function delete multipleassociations/[int id]() returns MultipleAssociations|persist:Error {
        MultipleAssociations 'object = check self->/multipleassociations/[id].get();
        _ = check self.persistClients.get("multipleassociations").runDeleteQuery({"id": id, });
        return 'object;
    }

    isolated resource function get user() returns stream<User, error?> {
        stream<record {}, sql:Error?>|persist:Error result = self.persistClients.get(USER).runReadQuery(User);
        if result is persist:Error {
            return new stream<User, persist:Error?>(new UserStream((), result));
        } else {
            return new stream<User, persist:Error?>(new UserStream(result));
        }
    }
    isolated resource function get user/[int id]() returns User|error {
        return (check self.persistClients.get(USER).runReadByKeyQuery(User, id)).cloneWithType(User);
    }
    isolated resource function post user(UserInsert[] data) returns [int][]|persist:Error {
        _ = check self.persistClients.get("user").runBatchInsertQuery(data);
        return from UserInsert inserted in data
            select [inserted.id];
    }
    isolated resource function put user/[int id](UserUpdate value) returns User|persist:Error {
        _ = check self.persistClients.get("user").runUpdateQuery({"id": id, }, data);
        return self->/user/[id].get();
    }
    isolated resource function delete user/[int id]() returns User|persist:Error {
        User 'object = check self->/user/[id].get();
        _ = check self.persistClients.get("user").runDeleteQuery({"id": id, });
        return 'object;
    }

    isolated resource function get profile() returns stream<Profile, error?> {
        stream<record {}, sql:Error?>|persist:Error result = self.persistClients.get(PROFILE).runReadQuery(Profile);
        if result is persist:Error {
            return new stream<Profile, persist:Error?>(new ProfileStream((), result));
        } else {
            return new stream<Profile, persist:Error?>(new ProfileStream(result));
        }
    }
    isolated resource function get profile/[int id]() returns Profile|error {
        return (check self.persistClients.get(PROFILE).runReadByKeyQuery(Profile, id)).cloneWithType(Profile);
    }
    isolated resource function post profile(ProfileInsert[] data) returns [int][]|persist:Error {
        _ = check self.persistClients.get("profile").runBatchInsertQuery(data);
        return from ProfileInsert inserted in data
            select [inserted.id];
    }
    isolated resource function put profile/[int id](ProfileUpdate value) returns Profile|persist:Error {
        _ = check self.persistClients.get("profile").runUpdateQuery({"id": id, }, data);
        return self->/profile/[id].get();
    }
    isolated resource function delete profile/[int id]() returns Profile|persist:Error {
        Profile 'object = check self->/profile/[id].get();
        _ = check self.persistClients.get("profile").runDeleteQuery({"id": id, });
        return 'object;
    }

    public function close() returns persist:Error? {
        _ = check self.dbClient.close();
    }
}

public class MultipleAssociationsStream {

    private stream<anydata, sql:Error?>? anydataStream;
    private persist:Error? err;

    public isolated function init(stream<anydata, sql:Error?>? anydataStream, persist:Error? err = ()) {
        self.anydataStream = anydataStream;
        self.err = err;
    }

    public isolated function next() returns record {|MultipleAssociations value;|}|persist:Error? {
        if self.err is persist:Error {
            return <persist:Error>self.err;
        } else if self.anydataStream is stream<anydata, sql:Error?> {
            var anydataStream = <stream<anydata, sql:Error?>>self.anydataStream;
            var streamValue = anydataStream.next();
            if streamValue is () {
                return streamValue;
            } else if (streamValue is sql:Error) {
                return <persist:Error>error(streamValue.message());
            } else {
                record {|MultipleAssociations value;|} nextRecord = {value: check streamValue.value.cloneWithType(MultipleAssociations)};
                return nextRecord;
            }
        } else {
            return ();
        }
    }

    public isolated function close() returns persist:Error? {
        check closeEntityStream(self.anydataStream);
    }
}

public class UserStream {

    private stream<anydata, sql:Error?>? anydataStream;
    private persist:Error? err;

    public isolated function init(stream<anydata, sql:Error?>? anydataStream, persist:Error? err = ()) {
        self.anydataStream = anydataStream;
        self.err = err;
    }

    public isolated function next() returns record {|User value;|}|persist:Error? {
        if self.err is persist:Error {
            return <persist:Error>self.err;
        } else if self.anydataStream is stream<anydata, sql:Error?> {
            var anydataStream = <stream<anydata, sql:Error?>>self.anydataStream;
            var streamValue = anydataStream.next();
            if streamValue is () {
                return streamValue;
            } else if (streamValue is sql:Error) {
                return <persist:Error>error(streamValue.message());
            } else {
                record {|User value;|} nextRecord = {value: check streamValue.value.cloneWithType(User)};
                return nextRecord;
            }
        } else {
            return ();
        }
    }

    public isolated function close() returns persist:Error? {
        check closeEntityStream(self.anydataStream);
    }
}

public class ProfileStream {

    private stream<anydata, sql:Error?>? anydataStream;
    private persist:Error? err;

    public isolated function init(stream<anydata, sql:Error?>? anydataStream, persist:Error? err = ()) {
        self.anydataStream = anydataStream;
        self.err = err;
    }

    public isolated function next() returns record {|Profile value;|}|persist:Error? {
        if self.err is persist:Error {
            return <persist:Error>self.err;
        } else if self.anydataStream is stream<anydata, sql:Error?> {
            var anydataStream = <stream<anydata, sql:Error?>>self.anydataStream;
            var streamValue = anydataStream.next();
            if streamValue is () {
                return streamValue;
            } else if (streamValue is sql:Error) {
                return <persist:Error>error(streamValue.message());
            } else {
                record {|Profile value;|} nextRecord = {value: check streamValue.value.cloneWithType(Profile)};
                return nextRecord;
            }
        } else {
            return ();
        }
    }

    public isolated function close() returns persist:Error? {
        check closeEntityStream(self.anydataStream);
    }
}

