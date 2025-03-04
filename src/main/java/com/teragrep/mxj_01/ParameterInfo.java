/*
  SPDX-License-Identifier: Apache-2.0

  Copyright 2019 Terse Systems <will@tersesystems.com>
  Copyright 2023 Suomen Kanuuna Oy

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package com.teragrep.mxj_01;

import javax.management.Descriptor;
import javax.management.MBeanParameterInfo;
import javax.management.openmbean.OpenType;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Parameter info.  Used to make method parameter names a bit more intelligible.
 */
public final class ParameterInfo<T> {
    private final Class<T> type;
    private final String name;
    private final String description;
    private final Descriptor descriptor;

    public ParameterInfo(Class<T> type, String name, String description, Descriptor descriptor) {
        this.type = requireNonNull(type);
        this.name = name;
        this.description = description;
        this.descriptor = descriptor;
    }

    public Class<T> getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Optional<Descriptor> getDescriptor() {
        return Optional.ofNullable(descriptor);
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public static Builder builder() {
        return new Builder();
    }

    public MBeanParameterInfo getMBeanParameterInfo() {
        String description = getDescription().orElse(null);
        Descriptor descriptor = getDescriptor().orElse(null);
        return new MBeanParameterInfo(getName(), getType().getTypeName(), description, descriptor);
    }

    // only type and name are required here
    public static class Builder {
        private final OpenTypeMapper openTypeMapper = OpenTypeMapper.getInstance();
        private final DescriptorSupport.Builder descriptorBuilder = DescriptorSupport.builder();

        private Class<?> type;
        private String name;
        private String description;

        Builder() {}

        public Builder withClassType(Class<?> type) {
            this.type = type;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withDescriptor(Descriptor descriptor) {
            descriptorBuilder.withDescriptor(descriptor);
            return this;
        }

        public ParameterInfo build() {
            OpenType<?> openType = openTypeMapper.fromClass(type);
            Descriptor descriptor = descriptorBuilder
                    .withField("openType", openType)
                    .withField("originalType", type.getName())
                    .build();

            return new ParameterInfo(type, name, description, descriptor);
        }
    }

}
