/**
 * SPDX-License-Identifier: Apache-2.0
 * <p>
 * Copyright 2019 Terse Systems <will@tersesystems.com>
 * Copyright 2023 Suomen Kanuuna Oy
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.teragrep.mxj_01;

import com.teragrep.mxj_01.model.Address;
import com.teragrep.mxj_01.model.User;
import org.junit.jupiter.api.Test;

import javax.management.Descriptor;
import javax.management.DynamicMBean;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.openmbean.*;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CompositeDataTests {

    private final OpenTypeMapper openTypeMapper = OpenTypeMapper.getInstance();

    @Test
    public void testDynamicBeanWithCompositeAttribute() {
        Address address = new Address("street1", "city", "state");
        User user = new User("name", 12, address);

        // https://docs.oracle.com/javase/8/docs/api/javax/management/openmbean/CompositeType.html
        final CompositeDataWriter<Address> addressBuilder = CompositeDataWriter.builder(Address.class)
                .withTypeName("address")
                .withTypeDescription("Address")
                .withSimpleAttribute(java.util.Date.class, "date", e -> new java.util.Date())
                .withSimpleAttribute(String.class, "street1", Address::getStreet1)
                .withSimpleAttribute(String.class, "city", Address::getCity)
                .withSimpleAttribute(String.class, "state", Address::getState)
                .build();

        AttributeInfo<CompositeData> info = AttributeInfo.builder(CompositeData.class).withName("address")
                .withDescription("Address")
                .withSupplier(() -> addressBuilder.apply(user.getAddress()))
                .build();

        MBeanAttributeInfo expected = new MBeanAttributeInfo("address", "javax.management.openmbean.CompositeData", "Address", true, false, false, compositeDescriptor());
        MBeanAttributeInfo actual = info.getMBeanAttributeInfo();

        assertThat(actual.getType()).isEqualTo(expected.getType());
        assertThat(actual.getName()).isEqualTo(expected.getName());
    }

    private Descriptor compositeDescriptor() {
        Descriptor nameDescriptor = new javax.management.modelmbean.DescriptorSupport();
        try {
            CompositeType compositeType = new CompositeType("address", "Address",
                    new String[]{"street1", "city", "state"},
                    new String[]{"Street 1", "City", "State"},
                    new OpenType[]{SimpleType.STRING, SimpleType.STRING, SimpleType.STRING});

            nameDescriptor.setField("openType", compositeType);
            nameDescriptor.setField("originalType", "");
            nameDescriptor.setField("enabled", true);
            return nameDescriptor;
        } catch (OpenDataException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testDynamicBeanWithCompositeAttributeDescriptor() {
        Address address = new Address("street1", "city", "state");
        User user = new User("name", 12, address);

        final CompositeDataWriter<Address> addressBuilder = CompositeDataWriter.builder(Address.class)
                .withTypeName("address")
                .withTypeDescription("Address")
                .withSimpleAttribute(String.class, "street1", Address::getStreet1)
                .withSimpleAttribute(String.class, "city", Address::getCity)
                .withSimpleAttribute(String.class, "state", Address::getState)
                .build();

        Descriptor addressDescriptor = DescriptorSupport.builder().withSince("1.0").withLocale("en-US").build();
        AttributeInfo<CompositeData> info = AttributeInfo.builder(CompositeData.class)
                .withName("address")
                .withDescription("Address")
                .withSupplier(() -> addressBuilder.apply(user.getAddress()))
                .withDescriptor(addressDescriptor)
                .build();

        javax.management.modelmbean.DescriptorSupport combined = DescriptorSupport.builder().withDescriptor(compositeDescriptor()).withDescriptor(addressDescriptor).build();
        MBeanAttributeInfo expected = new MBeanAttributeInfo("address", "javax.management.openmbean.CompositeData", "Address", true, false, false, combined);
        MBeanAttributeInfo actual = info.getMBeanAttributeInfo();

        assertThat(actual.getType()).isEqualTo(expected.getType());
        assertThat(actual.getName()).isEqualTo(expected.getName());
    }

    @Test
    public void testDynamicBeanWithTabularAttribute() {
        CompositeDataWriter<Address> addressWriter = CompositeDataWriter.builder(Address.class)
                .withTypeName("address")
                .withTypeDescription("Address")
                .withSimpleAttribute(String.class, "street1", Address::getStreet1)
                .withSimpleAttribute(String.class, "city", Address::getCity)
                .withSimpleAttribute(String.class, "state", Address::getState)
                .build();

        final TabularDataWriter<Address> addressesWriter = TabularDataWriter.builder(Address.class)
                .withTypeName("addresses")
                .withTypeDescription("Addresses")
                .withIndexName("street1")
                .withCompositeDataWriter(addressWriter)
                .build();

        Address address1 = new Address("street1", "city", "state");
        Address address2 = new Address("street2", "city", "state");
        Address address3 = new Address("street3", "city", "state");
        List<Address> addresses = Arrays.asList(address1, address2, address3);

        final DynamicMBean userBean = new DynamicBean.Builder()
                .withTabularAttribute("addresses", () -> addresses, addressesWriter)
                .build();
        MBeanInfo mBeanInfo = userBean.getMBeanInfo();
        MBeanAttributeInfo expected = new MBeanAttributeInfo("addresses", "javax.management.openmbean.TabularData", "Addresses", true, false, false);
        MBeanAttributeInfo actual = mBeanInfo.getAttributes()[0];
        assertThat(actual.getType()).isEqualTo(expected.getType());
    }
}
