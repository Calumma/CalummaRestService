package com.calumma.backend.web.repository.core.symbol;

import javax.persistence.Entity;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Set;

public class FieldParser {

    public static ParsedField parseField(Class actualLayer, String fieldName) throws NoSuchFieldException {
        return new ParsedField(
                getColumnName(fieldName),
                getTypeOfColumn(actualLayer, fieldName)
        );
    }

    public static Type getTypeOfColumn(Class actualLayer, String fieldName) throws NoSuchFieldException {

        String[] joins = getJoinTables(fieldName);
        String columnName = getColumnName(fieldName);

        String[] present_fields = Arrays.stream(actualLayer.getDeclaredFields()).map(Field::getName)
                .toArray(String[]::new);

        if (joins != null) {
            for (String join : joins) {
                Field potentialLayer = null;
                if (Arrays.asList(present_fields).contains(join))
                    potentialLayer = actualLayer.getDeclaredField(join);
                else if(getSuperClassField(actualLayer, join) != null){
                    potentialLayer = getSuperClassField(actualLayer, join);
                } else
                    potentialLayer = getSuperClassField(actualLayer, columnName);

                if (potentialLayer.getType().getTypeName().equals("java.util.List")) {
                    ParameterizedType stringListType = (ParameterizedType) potentialLayer.getGenericType();
                    actualLayer = (Class) stringListType.getActualTypeArguments()[0];
                } else {
                    if (potentialLayer.getType().getAnnotation(Entity.class) != null)
                        actualLayer = potentialLayer.getType();
                }
                present_fields = Arrays.stream(actualLayer.getDeclaredFields()).map(Field::getName)
                        .toArray(String[]::new);
            }
        }

        if (!Arrays.stream(present_fields).anyMatch(columnName::equals)) {
            return getSuperClassField(actualLayer, columnName).getType();
        }

        if (actualLayer.getDeclaredField(columnName).getType().getTypeName().equals("java.util.List")) {
            ParameterizedType stringListType = (ParameterizedType) actualLayer.getDeclaredField(columnName).getGenericType();
            return (Class) stringListType.getActualTypeArguments()[0];
        }

        return actualLayer.getDeclaredField(columnName).getType();
    }

    private static Field getSuperClassField(Class actualLayer, String columnName)
            throws NoSuchFieldException {
        Class nextLayer = actualLayer;
        String[] present_fields = Arrays.stream(actualLayer.getDeclaredFields()).map(x -> x.getName())
                .toArray(String[]::new);

        while (nextLayer != null && !Arrays.stream(present_fields).anyMatch(columnName::equals)) {
            nextLayer = nextLayer.getSuperclass();
            if (nextLayer != Object.class)
                actualLayer = nextLayer;

            present_fields = Arrays.stream(actualLayer.getDeclaredFields()).map(x -> x.getName())
                    .toArray(String[]::new);
        }

        return actualLayer.getDeclaredField(columnName);
    }

    public static From joinOrGetJoinedExpression(Root root, Class rootEntity,
                                                 String fieldPath) throws NoSuchFieldException {

        From query = root;
        Set<Join> joinSet = root.getJoins();
        String[] joinTables = getJoinTables(fieldPath);

        boolean canAlreadyBeJoined = true;
        Class layerEntity = rootEntity;
        if (joinTables != null && joinTables.length > 0) {
            for (String join : joinTables) {
                Class potentialJoin = getFieldType(layerEntity, join);
                if (canAlreadyBeJoined) {
                    From potentialQuery = getJoinedFromIfExists(joinSet, potentialJoin);
                    if (potentialQuery == null)
                        canAlreadyBeJoined = false;
                    else {
                        query = potentialQuery;
                        layerEntity = potentialJoin;
                    }
                }
                if (!canAlreadyBeJoined) {
                    query = query.join(join, JoinType.LEFT);
                    layerEntity = potentialJoin;
                }
            }
        }

        return query;
    }

    private static Class getFieldType(Class layerEntity, String join) throws NoSuchFieldException {
        String[] present_fields = Arrays.stream(layerEntity.getDeclaredFields()).map(x -> x.getName())
                .toArray(String[]::new);

        Field potentialJoin = null;

        if (Arrays.stream(present_fields).anyMatch(join::equals)) {
            potentialJoin = layerEntity.getDeclaredField(join);
        } else {
            potentialJoin = getSuperClassField(layerEntity, join);
        }

        Class potentialJoinClass = potentialJoin.getType();

        if (potentialJoin.getType().getTypeName().equals("java.util.List")) {
            ParameterizedType stringListType = (ParameterizedType) potentialJoin.getGenericType();
            potentialJoinClass = (Class) stringListType.getActualTypeArguments()[0];
        }

        return potentialJoinClass;
    }

    private static From getJoinedFromIfExists(Set<Join> joinSet, Class potentialJoin) {
        From query = null;

        for (Join joinedTable : joinSet) {
            if (joinedTable.getJavaType().equals(potentialJoin)) {
                query = joinedTable;
            }
        }

        return query;
    }

    public static String getColumnName(String name) {
        String[] pathToColumnName = name.split("\\.");
        return pathToColumnName[pathToColumnName.length - 1];
    }

    public static String[] getJoinTables(String name) {
        String[] pathToColumnName = name.split("\\.");

        if (pathToColumnName.length < 2)
            return null;
        return Arrays.copyOf(pathToColumnName, pathToColumnName.length - 1);
    }

}