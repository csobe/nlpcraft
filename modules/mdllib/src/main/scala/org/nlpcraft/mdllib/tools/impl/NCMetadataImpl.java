/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *     _   ____      ______           ______
 *    / | / / /___  / ____/________ _/ __/ /_
 *   /  |/ / / __ \/ /   / ___/ __ `/ /_/ __/
 *  / /|  / / /_/ / /___/ /  / /_/ / __/ /_
 * /_/ |_/_/ .___/\____/_/   \__,_/_/  \__/
 *        /_/
 */

package org.nlpcraft.mdllib.tools.impl;

import org.nlpcraft.mdllib.*;
import java.io.*;
import java.util.*;

/**
 * Map-based metadata container.
 */
public class NCMetadataImpl extends HashMap<String, Serializable> implements NCMetadata {
    /**
     *
     */
    public NCMetadataImpl() {
        // No-op.
    }

    /**
     *
     * @param map Map to copy.
     */
    public NCMetadataImpl(Map<String, Serializable> map) {
        super(map);
    }

    /**
     *
     * @param initCap Initial capacity.
     */
    public NCMetadataImpl(int initCap) {
        super(initCap);
    }

    /**
     * Gets mandatory metadata value.
     *
     * @param initCap Initial capacity.
     * @param loadFactor Load factor.
     */
    public NCMetadataImpl(int initCap, float loadFactor) {
        super(initCap, loadFactor);
    }

    /**
     * Gets optional string metadata value.
     *
     * @param key Metadata name.
     * @param dflt Default value to return if given name is not present.
     * @return Metadata value of default value if given name is not present.
     */
    public String getStringOrElse(String key, String dflt) {
        return getOrDefault(key, dflt).toString();
    }

    /**
     * Gets mandatory integer metadata value.
     *
     * @param key Metadata name.
     * @return Metadata value.
     * @throws IllegalArgumentException Thrown when given metadata key is not present.
     */
    public int getInteger(String key) {
        return getInteger0(key, getNotNull(key));
    }

    /**
     * Gets optional integer metadata value.
     *
     * @param key Metadata name.
     * @param dflt Default value to return if given name is not present.
     * @return Metadata value of default value if given name is not present.
     */
    public int getIntegerOrElse(String key, int dflt) {
        return getInteger0(key, getOrDefault(key, dflt));
    }

    /**
     * Gets mandatory double metadata value.
     *
     * @param key Metadata name.
     * @return Metadata value.
     * @throws IllegalArgumentException Thrown when given metadata key is not present.
     */
    public double getDouble(String key) {
        return getDouble0(key, getNotNull(key));
    }

    /**
     * Gets optional double metadata value.
     *
     * @param key Metadata name.
     * @param dflt Default value to return if given name is not present.
     * @return Metadata value of default value if given name is not present.
     */
    public double getDoubleOrElse(String key, double dflt) {
        return getDouble0(key, getOrDefault(key, dflt));
    }

    /**
     * Gets mandatory long metadata value.
     *
     * @param key Metadata name.
     * @return Metadata value.
     * @throws IllegalArgumentException Thrown when given metadata key is not present.
     */
    public long getLong(String key) {
        return getLong0(key, getNotNull(key));
    }

    /**
     * Gets mandatory long metadata value.
     *
     * @param key Metadata name.
     * @return Metadata value.
     * @throws IllegalArgumentException Thrown when given metadata key is not present.
     */
    public String getString(String key) {
        return getNotNull(key).toString();
    }

    /**
     * Gets optional long metadata value.
     *
     * @param key Metadata name.
     * @param dflt Default value to return if given name is not present.
     * @return Metadata value of default value if given name is not present.
     */
    public long getLongOrElse(String key, long dflt) {
        return getLong0(key, getOrDefault(key, dflt));
    }

    /**
     * Gets mandatory boolean metadata value.
     *
     * @param key Metadata name.
     * @return Metadata value.
     * @throws IllegalArgumentException Thrown when given metadata key is not present.
     */
    public boolean getBoolean(String key) {
        return getAs(key);
    }

    /**
     * Gets optional boolean metadata value.
     *
     * @param key Metadata name.
     * @param dflt Default value to return if given name is not present.
     * @return Metadata value of default value if given name is not present.
     */
    public boolean getBooleanOrElse(String key, boolean dflt) {
        return (Boolean)getOrDefault(key, dflt);
    }

    @Override
    public Optional<String> getStringOpt(String key) {
        return Optional.ofNullable((String)get(key));
    }

    @Override
    public Optional<Integer> getIntegerOpt(String key) {
        return Optional.ofNullable((Integer)get(key));
    }

    @Override
    public Optional<Double> getDoubleOpt(String key) {
        return Optional.ofNullable((Double)get(key));
    }

    @Override
    public Optional<Long> getLongOpt(String key) {
        return Optional.ofNullable((Long)get(key));
    }

    @Override
    public Optional<Boolean> getBooleanOpt(String key) {
        return Optional.ofNullable((Boolean)get(key));
    }

    /**
     * Gets mandatory metadata value.
     *
     * @param key Metadata key.
     * @param <T> Type of the metadata value.
     * @return Metadata value.
     * @throws IllegalArgumentException Thrown when given metadata key is not found.
     */
    @SuppressWarnings("unchecked")
    public <T> T getAs(String key) {
        return (T)getNotNull(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getOptAs(String key) {
        return Optional.ofNullable((T)get(key));
    }

    /**
     * Prepares error.
     *
     * @param key Key.
     * @param t Value.
     * @param <T> Type.
     * @return Prepared exception.
     */
    private <T> IllegalArgumentException mkTypeError(String key, T t) {
        return new IllegalArgumentException(
            String.format("Unexpected metadata key type [key=%s, type=%s]", key, t.getClass().getSimpleName())
        );
    }

    /**
     * Gets not null value.
     *
     * @param key Key.
     * @return Value.
     */
    private Serializable getNotNull(String key) {
        Serializable v = get(key);

        if (v == null)
            throw new IllegalArgumentException("Unknown metadata key: " + key);

        return v;
    }

    /**
     * Converts integer value.
     *
     * @param key Key.
     * @param v Value.
     * @return Converted value.
     */
    private int getInteger0(String key, Serializable v) {
        assert v != null;

        if (v instanceof Integer)
            return (Integer)v;
        else if (v instanceof Long)
            return ((Long)v).intValue();
        else if (v instanceof Double)
            return ((Double)v).intValue();
        else if (v instanceof Float)
            return ((Float)v).intValue();

        throw mkTypeError(key, v);
    }

    /**
     * Converts long value.
     *
     * @param key Key.
     * @param v Value.
     * @return Converted value.
     */
    private long getLong0(String key, Serializable v) {
        assert v != null;

        if (v instanceof Integer)
            return ((Integer)v).longValue();
        else if (v instanceof Long)
            return (Long)v;
        else if (v instanceof Double)
            return ((Double)v).longValue();
        else if (v instanceof Float)
            return ((Float)v).longValue();

        throw mkTypeError(key, v);
    }

    /**
     * Converts double value.
     *
     * @param key Key.
     * @param v Value.
     * @return Converted value.
     */
    private double getDouble0(String key, Serializable v) {
        assert v != null;

        if (v instanceof Integer)
            return ((Integer)v).doubleValue();
        else if (v instanceof Long)
            return ((Long)v).doubleValue();
        else if (v instanceof Double)
            return (Double)v;
        else if (v instanceof Float)
            return ((Float)v).doubleValue();

        throw mkTypeError(key, v);
    }
}