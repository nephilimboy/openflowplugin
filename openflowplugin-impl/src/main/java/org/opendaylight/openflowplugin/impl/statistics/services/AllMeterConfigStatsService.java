/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import org.opendaylight.openflowjava.protocol.api.util.BinContent;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.AbstractSimpleService;
import org.opendaylight.openflowplugin.impl.services.RequestInputUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterConfigStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterConfigStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterConfigCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterConfigCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.meter.config._case.MultipartRequestMeterConfigBuilder;

final class AllMeterConfigStatsService extends AbstractSimpleService<GetAllMeterConfigStatisticsInput, GetAllMeterConfigStatisticsOutput> {
    private static final MultipartRequestMeterConfigCase METER_CONFIG_CASE;

    static {
        MultipartRequestMeterConfigCaseBuilder caseBuilder =
                new MultipartRequestMeterConfigCaseBuilder();
        MultipartRequestMeterConfigBuilder mprMeterConfigBuild =
                new MultipartRequestMeterConfigBuilder();
        mprMeterConfigBuild.setMeterId(new MeterId(BinContent.intToUnsignedLong(Meter.OFPMALL.getIntValue())));
        caseBuilder.setMultipartRequestMeterConfig(mprMeterConfigBuild.build());

        METER_CONFIG_CASE = caseBuilder.build();
    }

    AllMeterConfigStatsService(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext, GetAllMeterConfigStatisticsOutput.class);
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final GetAllMeterConfigStatisticsInput input) {
        MultipartRequestInputBuilder mprInput = RequestInputUtils
                .createMultipartHeader(MultipartType.OFPMPMETERCONFIG, xid.getValue(), getVersion());
        return mprInput.setMultipartRequestBody(METER_CONFIG_CASE).build();
    }
}