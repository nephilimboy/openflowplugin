/*
 * Copyright IBM Corporation, 2013.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.translator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.opendaylight.openflowplugin.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.FlowStatsResponseConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.GroupStatsResponseConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.MeterStatsResponseConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Counter64;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.AggregateFlowStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev131103.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupDescStatsUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupFeaturesUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupStatisticsUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Chaining;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.ChainingChecks;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupAll;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupCapability;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupFf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupIndirect;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupSelect;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.SelectLiveness;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.SelectWeight;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterConfigStatsUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterFeaturesUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterStatisticsUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandDrop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandDscpRemark;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBurst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterCapability;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterKbps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterPktps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyAggregateCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterConfigCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.aggregate._case.MultipartReplyAggregate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.MultipartReplyFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.MultipartReplyGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.desc._case.MultipartReplyGroupDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.features._case.MultipartReplyGroupFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.MultipartReplyMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.MultipartReplyMeterConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.features._case.MultipartReplyMeterFeatures;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class converts multipart reply messages to the notification objects defined
 * by statistics provider (manager ).
 * 
 * @author avishnoi@in.ibm.com
 *
 */
public class MultipartReplyTranslator implements IMDMessageTranslator<OfHeader,  List<DataObject>> {

    protected static final Logger logger = LoggerFactory
            .getLogger(MultipartReplyTranslator.class);
    
    private static FlowStatsResponseConvertor flowStatsConvertor = new FlowStatsResponseConvertor();
    private static GroupStatsResponseConvertor groupStatsConvertor = new GroupStatsResponseConvertor();
    private static MeterStatsResponseConvertor meterStatsConvertor = new MeterStatsResponseConvertor();

    @Override
    public  List<DataObject> translate(SwitchConnectionDistinguisher cookie, SessionContext sc, OfHeader msg) {
        
        List<DataObject> listDataObject = new CopyOnWriteArrayList<DataObject>();

        if(msg instanceof MultipartReplyMessage){
            MultipartReplyMessage mpReply = (MultipartReplyMessage)msg;
            NodeId node = this.nodeIdFromDatapathId(sc.getFeatures().getDatapathId());
            switch (mpReply.getType()){
            case OFPMPFLOW: {
                logger.info("Received flow statistics reponse from openflow {} switch",msg.getVersion()==1?"1.0":"1.3+");
                FlowsStatisticsUpdateBuilder message = new FlowsStatisticsUpdateBuilder();
                message.setId(node);
                message.setMoreReplies(mpReply.getFlags().isOFPMPFREQMORE());
                message.setTransactionId(generateTransactionId(mpReply.getXid()));
                MultipartReplyFlowCase caseBody = (MultipartReplyFlowCase)mpReply.getMultipartReplyBody();
                MultipartReplyFlow replyBody = caseBody.getMultipartReplyFlow();
                message.setFlowAndStatisticsMapList(flowStatsConvertor.toSALFlowStatsList(replyBody.getFlowStats()));
                
                logger.info("Converted flow statistics : {}",message.build().toString());
                listDataObject.add(message.build());
                return listDataObject;
            }
            case OFPMPAGGREGATE: {
                logger.info("Received aggregate flow statistics reponse from openflow {} switch",msg.getVersion()==1?"1.0":"1.3+");
                AggregateFlowStatisticsUpdateBuilder message = new AggregateFlowStatisticsUpdateBuilder();
                message.setId(node);
                message.setMoreReplies(mpReply.getFlags().isOFPMPFREQMORE());
                message.setTransactionId(generateTransactionId(mpReply.getXid()));
                
                MultipartReplyAggregateCase caseBody = (MultipartReplyAggregateCase)mpReply.getMultipartReplyBody();
                MultipartReplyAggregate replyBody = caseBody.getMultipartReplyAggregate();
                message.setByteCount(new Counter64(replyBody.getByteCount()));
                message.setPacketCount(new Counter64(replyBody.getPacketCount()));
                message.setFlowCount(new Counter32(replyBody.getFlowCount()));
                
                logger.info("Converted aggregate flow statistics : {}",message.build().toString());
                listDataObject.add(message.build());
                return listDataObject;
                
            }
            case OFPMPGROUP:{
                logger.info("Received group statistics multipart reponse");
                GroupStatisticsUpdatedBuilder message = new GroupStatisticsUpdatedBuilder();
                message.setId(node);
                message.setMoreReplies(mpReply.getFlags().isOFPMPFREQMORE());
                message.setTransactionId(generateTransactionId(mpReply.getXid()));
                MultipartReplyGroupCase caseBody = (MultipartReplyGroupCase)mpReply.getMultipartReplyBody();
                MultipartReplyGroup replyBody = caseBody.getMultipartReplyGroup();
                message.setGroupStats(groupStatsConvertor.toSALGroupStatsList(replyBody.getGroupStats()));
                
                logger.debug("Converted group statistics : {}",message.toString());
                listDataObject.add(message.build());
                return listDataObject;
            }
            case OFPMPGROUPDESC:{
                logger.info("Received group description statistics multipart reponse");
                
                GroupDescStatsUpdatedBuilder message = new GroupDescStatsUpdatedBuilder();
                message.setId(node);
                message.setMoreReplies(mpReply.getFlags().isOFPMPFREQMORE());
                message.setTransactionId(generateTransactionId(mpReply.getXid()));
                MultipartReplyGroupDescCase caseBody = (MultipartReplyGroupDescCase)mpReply.getMultipartReplyBody();
                MultipartReplyGroupDesc replyBody = caseBody.getMultipartReplyGroupDesc();

                message.setGroupDescStats(groupStatsConvertor.toSALGroupDescStatsList(replyBody.getGroupDesc()));
                
                logger.debug("Converted group statistics : {}",message.toString());
                listDataObject.add(message.build());
                return listDataObject;
            }
            case OFPMPGROUPFEATURES: {
                logger.info("Received group features multipart reponse");
                GroupFeaturesUpdatedBuilder message = new GroupFeaturesUpdatedBuilder();
                message.setId(node);
                message.setMoreReplies(mpReply.getFlags().isOFPMPFREQMORE());
                message.setTransactionId(generateTransactionId(mpReply.getXid()));
                MultipartReplyGroupFeaturesCase caseBody = (MultipartReplyGroupFeaturesCase)mpReply.getMultipartReplyBody();
                MultipartReplyGroupFeatures replyBody = caseBody.getMultipartReplyGroupFeatures();
                List<Class<? extends GroupType>> supportedGroups = 
                        new ArrayList<Class<? extends GroupType>>();
                
                if(replyBody.getTypes().isOFPGTALL()){
                    supportedGroups.add(GroupAll.class);
                }
                if(replyBody.getTypes().isOFPGTSELECT()){
                    supportedGroups.add(GroupSelect.class);
                }
                if(replyBody.getTypes().isOFPGTINDIRECT()){
                    supportedGroups.add(GroupIndirect.class);
                }
                if(replyBody.getTypes().isOFPGTFF()){
                    supportedGroups.add(GroupFf.class);
                }
                message.setGroupTypesSupported(supportedGroups);
                message.setMaxGroups(replyBody.getMaxGroups());
                
                List<Class<? extends GroupCapability>> supportedCapabilities = 
                        new ArrayList<Class<? extends GroupCapability>>();
                
                if(replyBody.getCapabilities().isOFPGFCCHAINING()){
                    supportedCapabilities.add(Chaining.class);
                }
                if(replyBody.getCapabilities().isOFPGFCCHAININGCHECKS()){
                    supportedCapabilities.add(ChainingChecks.class);
                }
                if(replyBody.getCapabilities().isOFPGFCSELECTLIVENESS()){
                    supportedCapabilities.add(SelectLiveness.class);
                }
                if(replyBody.getCapabilities().isOFPGFCSELECTWEIGHT()){
                    supportedCapabilities.add(SelectWeight.class);
                }

                message.setGroupCapabilitiesSupported(supportedCapabilities);
                
                message.setActions(getGroupActionsSupportBitmap(replyBody.getActionsBitmap()));
                listDataObject.add(message.build());

                //augmentGroupFeaturesToNode(message);
                
                //Send update notification to all the listeners 
                return listDataObject;
            }
            case OFPMPMETER: {
                logger.info("Received meter statistics multipart reponse");
                MeterStatisticsUpdatedBuilder message = new MeterStatisticsUpdatedBuilder();
                message.setId(node);
                message.setMoreReplies(mpReply.getFlags().isOFPMPFREQMORE());
                message.setTransactionId(generateTransactionId(mpReply.getXid()));
                
                MultipartReplyMeterCase caseBody = (MultipartReplyMeterCase)mpReply.getMultipartReplyBody();
                MultipartReplyMeter replyBody = caseBody.getMultipartReplyMeter();
                message.setMeterStats(meterStatsConvertor.toSALMeterStatsList(replyBody.getMeterStats()));

                listDataObject.add(message.build());
                return listDataObject;
            }
            case OFPMPMETERCONFIG:{
                logger.info("Received meter config statistics multipart reponse");
                
                MeterConfigStatsUpdatedBuilder message = new MeterConfigStatsUpdatedBuilder();
                message.setId(node);
                message.setMoreReplies(mpReply.getFlags().isOFPMPFREQMORE());
                message.setTransactionId(generateTransactionId(mpReply.getXid()));
                
                MultipartReplyMeterConfigCase caseBody = (MultipartReplyMeterConfigCase)mpReply.getMultipartReplyBody();
                MultipartReplyMeterConfig replyBody = caseBody.getMultipartReplyMeterConfig();
                message.setMeterConfigStats(meterStatsConvertor.toSALMeterConfigList(replyBody.getMeterConfig()));
                
                listDataObject.add(message.build());
                return listDataObject;
            }
            case OFPMPMETERFEATURES:{
                logger.info("Received meter features multipart reponse");
                //Convert OF message and send it to SAL listener
                MeterFeaturesUpdatedBuilder message = new MeterFeaturesUpdatedBuilder();
                message.setId(node);
                message.setMoreReplies(mpReply.getFlags().isOFPMPFREQMORE());
                message.setTransactionId(generateTransactionId(mpReply.getXid()));
                
                MultipartReplyMeterFeaturesCase caseBody = (MultipartReplyMeterFeaturesCase)mpReply.getMultipartReplyBody();
                MultipartReplyMeterFeatures replyBody = caseBody.getMultipartReplyMeterFeatures();
                message.setMaxBands(replyBody.getMaxBands());
                message.setMaxColor(replyBody.getMaxColor());
                message.setMaxMeter(new Counter32(replyBody.getMaxMeter()));
                
                List<Class<? extends MeterCapability>> supportedCapabilities = 
                        new ArrayList<Class<? extends MeterCapability>>();
                if(replyBody.getCapabilities().isOFPMFBURST()){
                    supportedCapabilities.add(MeterBurst.class);
                }
                if(replyBody.getCapabilities().isOFPMFKBPS()){
                    supportedCapabilities.add(MeterKbps.class);
                    
                }
                if(replyBody.getCapabilities().isOFPMFPKTPS()){
                    supportedCapabilities.add(MeterPktps.class);
                    
                }
                if(replyBody.getCapabilities().isOFPMFSTATS()){
                    supportedCapabilities.add(MeterStats.class);
                    
                }
                message.setMeterCapabilitiesSupported(supportedCapabilities);
                
                List<Class<? extends MeterBand>> supportedMeterBand = 
                        new ArrayList<Class <? extends MeterBand>>();
                if(replyBody.getBandTypes().isOFPMBTDROP()){
                    supportedMeterBand.add(MeterBandDrop.class);
                }
                if(replyBody.getBandTypes().isOFPMBTDSCPREMARK()){
                    supportedMeterBand.add(MeterBandDscpRemark.class);
                }
                message.setMeterBandSupported(supportedMeterBand);
                listDataObject.add(message.build());

                //augmentMeterFeaturesToNode(message);

                //Send update notification to all the listeners 
                return listDataObject;
            }
            default:
                return listDataObject;
            }
        }
        
        return listDataObject;
    }
    
    private NodeId nodeIdFromDatapathId(BigInteger datapathId) {
        String current = datapathId.toString();
        return new NodeId("openflow:" + current);
    }
    
    private TransactionId generateTransactionId(Long xid){
        String stringXid =xid.toString();
        BigInteger bigIntXid = new BigInteger( stringXid );
        return new TransactionId(bigIntXid);

    }

    /* 
     * Method returns the bitmap of actions supported by each group.
     * TODO: My recommendation would be, its good to have a respective model of 
     * 'type bits', which will generate a class where all these flags will eventually
     * be stored as boolean. It will be convenient for application to check the
     * supported action, rather then doing bitmap operation.
     * @param actionsSupported
     * @return
     */
    private List<Long> getGroupActionsSupportBitmap(List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ActionType> actionsSupported){
        List<Long> supportActionByGroups = new ArrayList<Long>();
        for(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ActionType supportedActions : actionsSupported){
            long supportActionBitmap = 0;
            supportActionBitmap |= supportedActions.isOFPATOUTPUT()?(1 << 0): ~(1 << 0);
            supportActionBitmap |= supportedActions.isOFPATCOPYTTLOUT()?(1 << 11): ~(1 << 11);
            supportActionBitmap |= supportedActions.isOFPATCOPYTTLIN()?(1 << 12): ~(1 << 12);
            supportActionBitmap |= supportedActions.isOFPATSETMPLSTTL()?(1 << 15): ~(1 << 15);
            supportActionBitmap |= supportedActions.isOFPATDECMPLSTTL()?(1 << 16): ~(1 << 16);
            supportActionBitmap |= supportedActions.isOFPATPUSHVLAN()?(1 << 16): ~(1 << 16);
            supportActionBitmap |= supportedActions.isOFPATPOPVLAN()?(1 << 17): ~(1 << 17);
            supportActionBitmap |= supportedActions.isOFPATPUSHMPLS()?(1 << 18): ~(1 << 18);
            supportActionBitmap |= supportedActions.isOFPATPOPMPLS()?(1 << 19): ~(1 << 19);
            supportActionBitmap |= supportedActions.isOFPATSETQUEUE()?(1 << 20): ~(1 << 20);
            supportActionBitmap |= supportedActions.isOFPATGROUP()?(1 << 21): ~(1 << 21);
            supportActionBitmap |= supportedActions.isOFPATSETNWTTL()?(1 << 22): ~(1 << 22);
            supportActionBitmap |= supportedActions.isOFPATDECNWTTL()?(1 << 23): ~(1 << 23);
            supportActionBitmap |= supportedActions.isOFPATSETFIELD()?(1 << 24): ~(1 << 24);
            supportActionBitmap |= supportedActions.isOFPATPUSHPBB()?(1 << 25): ~(1 << 25);
            supportActionBitmap |= supportedActions.isOFPATPOPPBB()?(1 << 26): ~(1 << 26);
            supportActionBitmap |= supportedActions.isOFPATEXPERIMENTER()?(1 << 27): ~(1 << 27);
            supportActionByGroups.add(new Long(supportActionBitmap));
        }
        return supportActionByGroups;
    }

}
