/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.persist.nodegenerator;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.ChildNodeEntry;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportOrgNameNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.persist.components.Class;
import io.ballerina.persist.components.Function;
import io.ballerina.persist.components.IfElse;
import io.ballerina.persist.components.TypeDescriptor;
import io.ballerina.persist.objects.Entity;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


/**
 * Class to ballerina files as syntax trees.
 */
public class BalSyntaxTreeGenerator {
    private static final PrintStream errStream = System.err;

    /**
     * method to read ballerina files.
     */
    public static ArrayList<Entity> readBalFiles(Path filePath, String module) throws IOException {
        ArrayList<Entity> entityArray = new ArrayList<>();
        int index = -1;
        ArrayList<String> keys = new ArrayList<>();
        String tableName = null;
        int count = 0;
        SyntaxTree balSyntaxTree = SyntaxTree.from(TextDocuments.from(Files.readString(filePath)));
        ModulePartNode rootNote = balSyntaxTree.rootNode();
        NodeList<ModuleMemberDeclarationNode> nodeList = rootNote.members();
        for (ModuleMemberDeclarationNode i : nodeList) {
            if (i.kind().toString().equals("TYPE_DEFINITION")) {
                Collection<ChildNodeEntry> temp = i.childEntries();
                for (ChildNodeEntry j : temp) {
                    if (j.name().equals("metadata")) {
                        MetadataNode metaD = (MetadataNode) j.node().get();
                        NodeList<AnnotationNode> annotations = metaD.annotations();
                        for (AnnotationNode annotation : annotations) {
                            Node annotReference = annotation.annotReference();
                            if (annotReference.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
                                QualifiedNameReferenceNode qualifiedNameRef =
                                        (QualifiedNameReferenceNode) annotReference;
                                if (qualifiedNameRef.identifier().text().equals("Entity") && qualifiedNameRef
                                        .modulePrefix().text().equals("persist") &&
                                        annotation.annotValue().isPresent()) {
                                    index += 1;
                                    for (MappingFieldNode fieldNode : annotation.annotValue().get().fields()) {
                                        if (fieldNode.kind() == SyntaxKind.SPECIFIC_FIELD) {
                                            SpecificFieldNode specificField = (SpecificFieldNode) fieldNode;
                                            if (specificField.fieldName().kind() == SyntaxKind.IDENTIFIER_TOKEN) {
                                                ExpressionNode valueNode =
                                                        ((SpecificFieldNode) fieldNode).valueExpr().get();
                                                if (valueNode instanceof ListConstructorExpressionNode) {
                                                    keys = new ArrayList<>();
                                                    Iterator listIterator = ((ListConstructorExpressionNode) valueNode)
                                                            .expressions().iterator();
                                                    count = 0;
                                                    while (listIterator.hasNext()) {
                                                        keys.add(count, listIterator.next().toString());
                                                        count += 1;
                                                    }
                                                } else if (valueNode instanceof BasicLiteralNode) {
                                                    tableName = ((BasicLiteralNode) valueNode).literalToken().text()
                                                            .replaceAll("\"", "");
                                                }
                                            }
                                        }
                                    }
                                    entityArray.add(index, new Entity(getArray(keys), tableName, module));

                                }

                            }

                        }
                    }
                }
                RecordTypeDescriptorNode recordDesc = (RecordTypeDescriptorNode) ((TypeDefinitionNode) i)
                        .typeDescriptor();
                entityArray.get(index).setEntityName(((TypeDefinitionNode) i).typeName().text());
                for (Node k : recordDesc.fields()) {
                    if (k.kind().toString().equals("RECORD_FIELD_WITH_DEFAULT_VALUE")) {
                        RecordFieldWithDefaultValueNode fieldNode = (RecordFieldWithDefaultValueNode) k;
                        String fName = fieldNode.fieldName().text();
                        String fType = fieldNode.typeName().toString();
                        HashMap<String, String> map = new HashMap<String, String>();
                        if (((RecordFieldWithDefaultValueNode) k).metadata().isEmpty()) {
                            map.put("fieldName", fName);
                            map.put("fieldType", fType);
                            map.put("autoGenerated", "false");
                            entityArray.get(index).addField(map);
                        } else {
                            MetadataNode fieldMetaD = ((RecordFieldWithDefaultValueNode) k).metadata().get();
                            map.put("fieldName", fName);
                            map.put("fieldType", fType);
                            map.put("autoGenerated", readMetaData(fieldMetaD));
                            entityArray.get(index).addField(map);
                        }
                    } else if (k.kind().toString().equals("RECORD_FIELD")) {
                        RecordFieldNode fieldNode = (RecordFieldNode) k;
                        String fName = fieldNode.fieldName().text();
                        String fType = fieldNode.typeName().toString();
                        HashMap<String, String> map = new HashMap<String, String>();
                        if (((RecordFieldNode) k).metadata().isEmpty()) {
                            map.put("fieldName", fName);
                            map.put("fieldType", fType);
                            map.put("autoGenerated", "false");
                            entityArray.get(index).addField(map);
                        } else {
                            MetadataNode fieldMetaD = ((RecordFieldNode) k).metadata().get();
                            map.put("fieldName", fName);
                            map.put("fieldType", fType);
                            map.put("autoGenerated", readMetaData(fieldMetaD));
                            entityArray.get(index).addField(map);
                        }
                    }
                }
            }
        }
        return entityArray;
    }

    public static SyntaxTree generateBalFile(Entity entity) {

        boolean keyAutoInc = false;
        HashMap<String, String> keys = new HashMap<String, String>();
        String keyType = "int";
        NodeList<ImportDeclarationNode> imports = AbstractNodeFactory.createEmptyNodeList();
        NodeList<ModuleMemberDeclarationNode> moduleMembers = AbstractNodeFactory.createEmptyNodeList();
        List<Node> subFields = new ArrayList<>();
        boolean hasTime = false;
        for (HashMap field : entity.getFields()) {
            if (field.get("fieldType").toString().contains("time") && !hasTime) {
                hasTime = true;
            }
            if (entity.getKeys().length > 1) {
                for (String key : entity.getKeys()) {
                    if (field.get("fieldName").toString().equals(key.trim().replaceAll("\"", ""))) {
                        keys.put(field.get("fieldName").toString(), field.get("fieldType")
                                .toString().trim().replaceAll(" ", ""));
                        keyType = field.get("fieldType").toString().trim().replaceAll(" ", "");
                    }
                }
            } else {
                if (field.get("fieldName").toString().equals(entity.getKeys()[0].trim().replaceAll("\"", ""))) {
                    keyType = field.get("fieldType").toString().trim().replaceAll(" ", "");
                }
            }
            if (!subFields.isEmpty()) {
                subFields.add(NodeFactory.createBasicLiteralNode(SyntaxKind.STRING_LITERAL,
                        AbstractNodeFactory.createLiteralValueToken(SyntaxKind.STRING_LITERAL, ", "
                                        + System.lineSeparator(),
                                NodeFactory.createEmptyMinutiaeList(), NodeFactory.createEmptyMinutiaeList())));
            }
            if (field.get("autoGenerated").equals("true")) {
                subFields.add(NodeFactory.createSpecificFieldNode(null,
                        AbstractNodeFactory.createIdentifierToken(field.get("fieldName").toString()),
                        SyntaxTreeConstants.SYNTAX_TREE_COLON,
                        NodeParser.parseExpression(String.format(
                                "{columnName: \"%s\", 'type: %s, autoGenerated: %s}",
                                field.get("fieldName").toString().trim().replaceAll("'", ""),
                                field.get("fieldType").toString().trim().replaceAll(" ", ""),
                                field.get("autoGenerated").toString().trim()))));
                if (field.get("fieldName").toString().equals(entity.getKeys()[0].trim().replaceAll("\"",
                        ""))) {
                    keyAutoInc = true;
                }

            } else {
                subFields.add(NodeFactory.createSpecificFieldNode(null,
                        AbstractNodeFactory.createIdentifierToken(field.get("fieldName").toString()),
                        SyntaxTreeConstants.SYNTAX_TREE_COLON,
                        NodeParser.parseExpression(String.format("{columnName: \"%s\", 'type: %s}",
                                field.get("fieldName").toString().trim().replaceAll("'", ""),
                                field.get("fieldType").toString().trim().replaceAll(" ", "")
                        ))));
            }

        }
        imports = imports.add(getImportDeclarationNode("ballerina", "sql"));
        imports = imports.add(getImportDeclarationNode("ballerinax", "mysql"));
        if (hasTime) {
            imports = imports.add(getImportDeclarationNode("ballerina", "time"));
        }
        imports = imports.add(getImportDeclarationNode("ballerina", "persist"));
        if (entity.getModule().equals("")) {
            imports = imports.add(NodeParser.parseImportDeclaration(String.format("import %s as entities;",
                    entity.getPackageName())));
        } else {
            imports = imports.add(NodeParser.parseImportDeclaration(String.format("import %s as entities;",
                    entity.getPackageName() + "." + entity.getModule())));
        }

        String className = entity.getEntityName();
        if (!entity.getModule().equals("")) {
            className = entity.getModule().substring(0, 1).toUpperCase() + entity.getModule().substring(1)
                    + entity.getEntityName();
        }
        Class client = new Class(className + "Client", false);
        client.addQualifiers(new String[]{"client"});
        client.addMember(NodeFactory.createBasicLiteralNode(SyntaxKind.STRING_LITERAL,
                AbstractNodeFactory.createLiteralValueToken(SyntaxKind.STRING_LITERAL, " ",
                        NodeFactory.createEmptyMinutiaeList(), NodeFactory.createEmptyMinutiaeList())), false);
        client.addMember(TypeDescriptor.getObjectFieldNode(
                        "private",
                        new String[]{"final"},
                        TypeDescriptor.getBuiltinSimpleNameReferenceNode("string"),
                        "entityName", NodeFactory.createBasicLiteralNode(SyntaxKind.STRING_LITERAL,
                                AbstractNodeFactory.createLiteralValueToken(SyntaxKind.STRING_LITERAL,
                                        "\"" + entity.getEntityName() + "\"",
                                        NodeFactory.createEmptyMinutiaeList(), NodeFactory.createEmptyMinutiaeList()))),
                true);
        client.addMember(TypeDescriptor.getObjectFieldNode(
                        "private",
                        new String[]{"final"},
                        TypeDescriptor.getQualifiedNameReferenceNode("sql", "ParameterizedQuery"),
                        "tableName", NodeFactory.createBasicLiteralNode(SyntaxKind.STRING_LITERAL,
                                AbstractNodeFactory.createLiteralValueToken(SyntaxKind.STRING_LITERAL,
                                        "`" + entity.getTableName() + "`",
                                        NodeFactory.createEmptyMinutiaeList(), NodeFactory.createEmptyMinutiaeList()))),
                false);

        client.addMember(TypeDescriptor.getObjectFieldNode(
                        "private",
                        new String[]{"final"},
                        TypeDescriptor.getSimpleNameReferenceNode("map<persist:FieldMetadata>"),
                        "fieldMetadata", NodeFactory.createMappingConstructorExpressionNode(
                                SyntaxTreeConstants.SYNTAX_TREE_OPEN_BRACE, AbstractNodeFactory
                                        .createSeparatedNodeList(subFields),
                                SyntaxTreeConstants.SYNTAX_TREE_CLOSE_BRACE)
                )
                , true);
        String keysString = "";
        for (String i : entity.getKeys()) {
            if (!keysString.equals("")) {
                keysString += ", ";
            }
            keysString += i;
        }

        client.addMember(TypeDescriptor.getObjectFieldNode(
                "private",
                new String[]{},
                TypeDescriptor.getArrayTypeDescriptorNode("string"),
                "keyFields", NodeFactory.createListConstructorExpressionNode(
                        SyntaxTreeConstants.SYNTAX_TREE_OPEN_BRACKET, AbstractNodeFactory
                                .createSeparatedNodeList(NodeFactory.createBasicLiteralNode(SyntaxKind.STRING_LITERAL,
                                        AbstractNodeFactory.createLiteralValueToken(SyntaxKind.STRING_LITERAL,
                                                keysString, NodeFactory.createEmptyMinutiaeList(),
                                                NodeFactory.createEmptyMinutiaeList())))
                        , SyntaxTreeConstants.SYNTAX_TREE_CLOSE_BRACKET)
        ), false);

        client.addMember(TypeDescriptor.getObjectFieldNodeWithoutExpression(
                "private",
                new String[]{},
                TypeDescriptor.getQualifiedNameReferenceNode("persist",
                        "SQLClient"),
                "persistClient"), true);

        Function init = new Function("init");
        init.addQualifiers(new String[]{"public"});
        init.addReturns(TypeDescriptor.getOptionalTypeDescriptorNode("", "error"));
        init.addStatement(NodeParser.parseStatement(
                "mysql:Client dbClient = check new (host = host, user = user, password = password, database " +
                        "= database, port = port);"));
        init.addStatement(NodeParser.parseStatement("self.persistClient " +
                "= check new (self.entityName, self.tableName, self.fieldMetadata, self.keyFields, dbClient);"));
        client.addMember(init.getFunctionDefinitionNode(), true);

        Function create = new Function("create");
        create.addRequiredParameter(
                TypeDescriptor.getQualifiedNameReferenceNode("entities", entity.getEntityName()),
                "value"
        );
        create.addQualifiers(new String[]{"remote"});

        if (keys.size() > 1) {
            create.addReturns(TypeDescriptor.getUnionTypeDescriptorNode(SyntaxTreeConstants.SYNTAX_TREE_VAR_ANY,
                    TypeDescriptor.getOptionalTypeDescriptorNode("", "error")));
            create.addStatement(NodeParser.parseStatement("sql:ExecutionResult result = " +
                    "check self.persistClient.runInsertQuery(value);"));
            if (!keyAutoInc) {
                IfElse valueNilCheck = new IfElse(NodeParser.parseExpression("result.lastInsertId is ()"));
                String ret = "";
                for (String key : keys.keySet()) {
                    if (!ret.equals("")) {
                        ret += ", ";
                    }
                    ret += "value." + key;
                }
                valueNilCheck.addIfStatement(NodeParser.parseStatement(String.format("return [%s];", ret)));
                create.addIfElseStatement(valueNilCheck.getIfElseStatementNode());
            }
            create.addStatement(NodeParser.parseStatement("return result.lastInsertId;"));

        }  else if (keyType.equals("int")) {
            create.addReturns(TypeDescriptor.getUnionTypeDescriptorNode(SyntaxTreeConstants.SYNTAX_TREE_VAR_INT,
                    TypeDescriptor.getOptionalTypeDescriptorNode("", "error")));
            create.addStatement(NodeParser.parseStatement("sql:ExecutionResult result = " +
                    "check self.persistClient.runInsertQuery(value);"));

            if (!keyAutoInc) {
                IfElse valueNilCheck = new IfElse(NodeParser.parseExpression("result.lastInsertId is ()"));
                valueNilCheck.addIfStatement(NodeParser.parseStatement(String.format("return value.%s;",
                        entity.getKeys()[0].trim().replaceAll("\"", ""))));
                create.addIfElseStatement(valueNilCheck.getIfElseStatementNode());
            }
            create.addStatement(NodeParser.parseStatement(String.format("return <%s>result.lastInsertId;", keyType)));
        } else if (keyType.equals("string")) {
            create.addReturns(TypeDescriptor.getUnionTypeDescriptorNode(SyntaxTreeConstants.SYNTAX_TREE_VAR_STRING,
                    TypeDescriptor.getOptionalTypeDescriptorNode("", "error")));
            create.addStatement(NodeParser.parseStatement("sql:ExecutionResult result = " +
                    "check self.persistClient.runInsertQuery(value);"));
            if (!keyAutoInc) {
                IfElse valueNilCheck = new IfElse(NodeParser.parseExpression("result.lastInsertId is ()"));
                valueNilCheck.addIfStatement(NodeParser.parseStatement(String.format("return value.%s;",
                        entity.getKeys()[0].trim().replaceAll("\"", ""))));
                create.addIfElseStatement(valueNilCheck.getIfElseStatementNode());
            }
            create.addStatement(NodeParser.parseStatement(String.format("return <%s>result.lastInsertId;", keyType)));
        }
        client.addMember(create.getFunctionDefinitionNode(), true);

        Function readByKey = new Function("readByKey");
        String keyString = "key";
        if (keys.size() > 1) {
            keyString = "";
            int suffix = 0;
            for (String key : keys.keySet()) {
                readByKey.addRequiredParameter(
                        TypeDescriptor.getBuiltinSimpleNameReferenceNode(keys.get(key)), "key" +
                                String.valueOf(suffix));
                if (!keyString.equals("")) {
                    keyString += ", ";
                }
                keyString += "key" + String.valueOf(suffix);
                suffix += 1;

            }
        } else {
            readByKey.addRequiredParameter(
                    TypeDescriptor.getBuiltinSimpleNameReferenceNode(keyType),
                    "key"
            );;
        }


        readByKey.addQualifiers(new String[]{"remote"});
        readByKey.addReturns(TypeDescriptor.getUnionTypeDescriptorNode(
                TypeDescriptor.getQualifiedNameReferenceNode("entities", entity.getEntityName()),
                TypeDescriptor.getSimpleNameReferenceNode ("error")));
        readByKey.addStatement(NodeParser.parseStatement(String.format("return (check self.persistClient." +
                "runReadByKeyQuery(%s, %s)).cloneWithType(%s);", "entities:" + entity.getEntityName(), keyString,
                "entities:" + entity.getEntityName())));
        client.addMember(readByKey.getFunctionDefinitionNode(), true);

        Function read = new Function("read");
        read.addRequiredParameterWithDefault(
                TypeDescriptor.getOptionalTypeDescriptorNode("", TypeDescriptor
                        .getMapTypeDescriptorNode(SyntaxTreeConstants.SYNTAX_TREE_VAR_ANYDATA).toSourceCode()),
                "filter");
        read.addQualifiers(new String[]{"remote"});
        read.addReturns(TypeDescriptor.getUnionTypeDescriptorNode(TypeDescriptor.getStreamTypeDescriptorNode(
                TypeDescriptor.getQualifiedNameReferenceNode("entities", entity.getEntityName()),
                        TypeDescriptor.getOptionalTypeDescriptorNode("", "error")),
                TypeDescriptor.getSimpleNameReferenceNode("error")));
        read.addStatement(NodeParser.parseStatement(String.format("stream<anydata, error?> result = " +
                "check self.persistClient.runReadQuery(%s, filter);", "entities:" + entity.getEntityName())));
        read.addStatement(NodeParser.parseStatement(String.format("return new stream<%s, " +
                "error?>(new %sStream(result));", "entities:" + entity.getEntityName(), className)));
        client.addMember(read.getFunctionDefinitionNode(), true);

        Function update = new Function("update");
        update.addRequiredParameter(
                TypeDescriptor.getRecordTypeDescriptorNode(),
                "'object"
        );
        update.addRequiredParameter(
                TypeDescriptor.getBuiltinSimpleNameReferenceNode(TypeDescriptor
                        .getMapTypeDescriptorNode(SyntaxTreeConstants.SYNTAX_TREE_VAR_ANYDATA).toSourceCode()),
                "filter"
        );
        update.addQualifiers(new String[]{"remote"});
        update.addReturns(TypeDescriptor.getOptionalTypeDescriptorNode("", "error"));
        update.addStatement(NodeParser.parseStatement("_ = " +
                "check self.persistClient.runUpdateQuery('object, filter);"));
        client.addMember(update.getFunctionDefinitionNode(), true);


        Function delete = new Function("delete");
        delete.addRequiredParameter(
                TypeDescriptor.getBuiltinSimpleNameReferenceNode(TypeDescriptor
                        .getMapTypeDescriptorNode(SyntaxTreeConstants.SYNTAX_TREE_VAR_ANYDATA).toSourceCode()),
                "filter"
        );
        delete.addQualifiers(new String[]{"remote"});
        delete.addReturns(TypeDescriptor.getOptionalTypeDescriptorNode("", "error"));
        delete.addStatement(NodeParser.parseStatement("_ = check self.persistClient.runDeleteQuery(filter);"));
        client.addMember(delete.getFunctionDefinitionNode(), true);

        Function close = new Function("close");
        close.addQualifiers(new String[]{});
        close.addReturns(TypeDescriptor.getOptionalTypeDescriptorNode("", "error"));
        close.addStatement(NodeParser.parseStatement("return self.persistClient.close();"));
        client.addMember(close.getFunctionDefinitionNode(), true);

        moduleMembers = moduleMembers.add(client.getClassDefinitionNode());

        Class clientStream = new Class(className + "Stream", true);

        clientStream.addMember(NodeFactory.createBasicLiteralNode(SyntaxKind.STRING_LITERAL,
                AbstractNodeFactory.createLiteralValueToken(SyntaxKind.STRING_LITERAL, " ",
                        NodeFactory.createEmptyMinutiaeList(), NodeFactory.createEmptyMinutiaeList())), true);

        clientStream.addMember(TypeDescriptor.getObjectFieldNodeWithoutExpression(
                        "private",
                        new String[]{},
                        TypeDescriptor.getStreamTypeDescriptorNode(
                            SyntaxTreeConstants.SYNTAX_TREE_VAR_ANYDATA,
                            TypeDescriptor.getOptionalTypeDescriptorNode("", "error")),
                        "anydataStream"), true);

        Function initStream = new Function("init");
        initStream.addQualifiers(new String[]{"public", "isolated"});
        initStream.addStatement(NodeParser.parseStatement(
                "self.anydataStream = anydataStream;"));
        initStream.addRequiredParameter(
                TypeDescriptor.getStreamTypeDescriptorNode(
                        SyntaxTreeConstants.SYNTAX_TREE_VAR_ANYDATA,
                        TypeDescriptor.getOptionalTypeDescriptorNode("", "error")),
                "anydataStream");
        clientStream.addMember(initStream.getFunctionDefinitionNode(), true);

        Function nextStream = new Function("next");
        nextStream.addQualifiers(new String[]{"public", "isolated"});
        nextStream.addReturns(NodeParser.parseTypeDescriptor(String.format("record {|%s value;|}|error?",
                "entities:" + entity.getEntityName())));
        nextStream.addStatement(NodeParser.parseStatement("var streamValue = self.anydataStream.next();"));

        IfElse streamValueNilCheck = new IfElse(NodeParser.parseExpression("streamValue is ()"));
        streamValueNilCheck.addIfStatement(NodeParser.parseStatement("return streamValue;"));
        IfElse streamValueErrorCheck = new IfElse(NodeParser.parseExpression("(streamValue is error)"));
        streamValueErrorCheck.addIfStatement(NodeParser.parseStatement("return streamValue;"));
        streamValueErrorCheck.addElseStatement(NodeParser.parseStatement(String.format(
                "record {|%s value;|} nextRecord = {value: check streamValue.value.cloneWithType(%s)};",
                "entities:" + entity.getEntityName(), "entities:" + entity.getEntityName())));
        streamValueErrorCheck.addElseStatement(NodeParser.parseStatement("return nextRecord;"));
        streamValueNilCheck.addElseBody(streamValueErrorCheck);
        nextStream.addIfElseStatement(streamValueNilCheck.getIfElseStatementNode());
        clientStream.addMember(nextStream.getFunctionDefinitionNode(), true);

        Function closeStream = new Function("close");
        closeStream.addQualifiers(new String[]{"public", "isolated"});
        closeStream.addReturns(TypeDescriptor.getOptionalTypeDescriptorNode("", "error"));
        closeStream.addStatement(NodeParser.parseStatement("return self.anydataStream.close();"));
        clientStream.addMember(closeStream.getFunctionDefinitionNode(), true);

        moduleMembers = moduleMembers.add(clientStream.getClassDefinitionNode());

        Token eofToken = AbstractNodeFactory.createIdentifierToken("");
        ModulePartNode modulePartNode = NodeFactory.createModulePartNode(imports, moduleMembers, eofToken);
        TextDocument textDocument = TextDocuments.from("");
        SyntaxTree balTree = SyntaxTree.from(textDocument);
        return balTree.modifyWith(modulePartNode);
    }

    public static SyntaxTree generateConfigBalFile() {
        NodeList<ImportDeclarationNode> imports = AbstractNodeFactory.createEmptyNodeList();
        NodeList<ModuleMemberDeclarationNode> moduleMembers = AbstractNodeFactory.createEmptyNodeList();

        moduleMembers = moduleMembers.add(NodeParser.parseModuleMemberDeclaration("configurable int port = ?;"));
        moduleMembers = moduleMembers.add(NodeParser.parseModuleMemberDeclaration(
                "configurable string host = ?;"));
        moduleMembers = moduleMembers.add(NodeParser.parseModuleMemberDeclaration(
                "configurable string user = ?;"));
        moduleMembers = moduleMembers.add(NodeParser.parseModuleMemberDeclaration(
                "configurable string database = ?;"));
        moduleMembers = moduleMembers.add(NodeParser.parseModuleMemberDeclaration(
                "configurable string password = ?;"));

        Token eofToken = AbstractNodeFactory.createIdentifierToken("");
        ModulePartNode modulePartNode = NodeFactory.createModulePartNode(imports, moduleMembers, eofToken);
        TextDocument textDocument = TextDocuments.from("");
        SyntaxTree balTree = SyntaxTree.from(textDocument);
        return balTree.modifyWith(modulePartNode);
    }

    private static String[] getArray(ArrayList<String> arrL) {
        String[] output = new String[arrL.size()];
        for (int i = 0; i < arrL.size(); i++) {
            output[i] = arrL.get(i);
        }
        return output;
    }

    public static ImportDeclarationNode getImportDeclarationNode(String orgName, String moduleName) {
        Token orgNameToken = AbstractNodeFactory.createIdentifierToken(orgName);
        ImportOrgNameNode importOrgNameNode = NodeFactory.createImportOrgNameNode(
                orgNameToken,
                SyntaxTreeConstants.SYNTAX_TREE_SLASH
        );
        Token moduleNameToken = AbstractNodeFactory.createIdentifierToken(moduleName);
        SeparatedNodeList<IdentifierToken> moduleNodeList =
                AbstractNodeFactory.createSeparatedNodeList(moduleNameToken);

        return NodeFactory.createImportDeclarationNode(
                SyntaxTreeConstants.SYNTAX_TREE_KEYWORD_IMPORT,
                importOrgNameNode,
                moduleNodeList,
                null,
                SyntaxTreeConstants.SYNTAX_TREE_SEMICOLON
        );
    }

    private static String readMetaData(MetadataNode metaD) {
        NodeList<AnnotationNode> annotations = metaD.annotations();
        for (AnnotationNode annotation : annotations) {
            Node annotReference = annotation.annotReference();
            if (annotReference.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
                QualifiedNameReferenceNode qualifiedNameRef =
                        (QualifiedNameReferenceNode) annotReference;
                if (qualifiedNameRef.identifier().text().equals("AutoIncrement") && qualifiedNameRef
                        .modulePrefix().text().equals("persist")) {
                    return "true";
                }
            }
        }
        return "false";
    }
}
