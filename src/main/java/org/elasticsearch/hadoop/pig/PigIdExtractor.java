/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.elasticsearch.hadoop.pig;

import org.apache.pig.ResourceSchema.ResourceFieldSchema;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.DataType;
import org.elasticsearch.hadoop.cfg.Settings;
import org.elasticsearch.hadoop.serialization.IdExtractor;
import org.elasticsearch.hadoop.serialization.SettingsAware;
import org.elasticsearch.hadoop.util.Assert;

public class PigIdExtractor implements IdExtractor, SettingsAware {

    private String id;

    @Override
    public String id(Object target) {
        if (target instanceof PigTuple) {
            PigTuple pt = (PigTuple) target;
            ResourceFieldSchema[] fields = pt.getSchema().getSchema().getFields();


            for (int i = 0; i < fields.length; i++) {
                ResourceFieldSchema field = fields[i];
                if (id.equals(field.getName())) {
                    byte type = field.getType();
                    Assert.isTrue(
                            DataType.isAtomic(type),
                            String.format("Unsupported data type [%s] for id [%s]; use only 'primitives'", DataType.findTypeName(type), id));

                    try {
                        return pt.getTuple().get(i).toString();
                    } catch (ExecException ex) {
                        throw new IllegalStateException(String.format("Cannot retrieve id field [%s]", id), ex);
                    }
                }
            }
        }

        return null;
    }

    @Override
    public void setSettings(Settings settings) {
        id = settings.getMappingId().trim().toLowerCase();
    }
}
