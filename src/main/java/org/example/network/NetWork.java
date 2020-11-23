package org.example.network;

import org.example.client.ChannelClient;
import org.example.client.FabricClient;
import org.example.config.Config;
import org.example.user.UserContext;
import org.example.util.Util;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NetWork {
    public Channel net_Channel;
    public FabricClient fab_Client;
    public UserContext org1Admin= new UserContext();
    public UserContext org2Admin= new UserContext();

    public void init() {
        try {
            CryptoSuite.Factory.getCryptoSuite();
            //Util.cleanUp();
            // Construct Channel
            File pkFolder1 = new File(Config.ORG1_USR_ADMIN_PK);
            File[] pkFiles1 = pkFolder1.listFiles();
            File certFolder1 = new File(Config.ORG1_USR_ADMIN_CERT);
            File[] certFiles1 = certFolder1.listFiles();
            //assert certFiles1 != null;
            Enrollment enrollOrg1Admin = Util.getEnrollment(Config.ORG1_USR_ADMIN_PK, pkFiles1[0].getName(),
                    Config.ORG1_USR_ADMIN_CERT, certFiles1[0].getName());
            org1Admin.setEnrollment(enrollOrg1Admin);
            org1Admin.setMspId(Config.ORG1_MSP);
            org1Admin.setName(Config.ADMIN);

            File pkFolder2 = new File(Config.ORG2_USR_ADMIN_PK);
            File[] pkFiles2 = pkFolder2.listFiles();
            File certFolder2 = new File(Config.ORG2_USR_ADMIN_CERT);
            File[] certFiles2 = certFolder2.listFiles();
            //assert certFiles2 != null;
            Enrollment enrollOrg2Admin = Util.getEnrollment(Config.ORG2_USR_ADMIN_PK, pkFiles2[0].getName(),
                    Config.ORG2_USR_ADMIN_CERT, certFiles2[0].getName());
            org2Admin.setEnrollment(enrollOrg2Admin);
            org2Admin.setMspId(Config.ORG2_MSP);
            org2Admin.setName(Config.ADMIN);

            FabricClient fabClient = new FabricClient(org1Admin);
            this.fab_Client = fabClient;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // create a chnnel
    public NetWork createChannel(String ChannelPath, String ChannelName) {
        try {
            Util.cleanUp();
            Orderer orderer = fab_Client.getInstance().newOrderer(Config.ORDERER_NAME, Config.ORDERER_URL);
            ChannelConfiguration channelConfiguration = new ChannelConfiguration(new File(ChannelPath));

            byte[] channelConfigurationSignatures = fab_Client.getInstance()
                    .getChannelConfigurationSignature(channelConfiguration, org1Admin);

            Channel mychannel = fab_Client.getInstance().newChannel(ChannelName, orderer, channelConfiguration,
                    channelConfigurationSignatures);
            this.net_Channel = mychannel;

            Peer peer0_org1 = fab_Client.getInstance().newPeer(Config.ORG1_PEER_0, Config.ORG1_PEER_0_URL);
            Peer peer1_org1 = fab_Client.getInstance().newPeer(Config.ORG1_PEER_1, Config.ORG1_PEER_1_URL);
            Peer peer0_org2 = fab_Client.getInstance().newPeer(Config.ORG2_PEER_0, Config.ORG2_PEER_0_URL);
            Peer peer1_org2 = fab_Client.getInstance().newPeer(Config.ORG2_PEER_1, Config.ORG2_PEER_1_URL);

            EventHub eventHub = fab_Client.getInstance().newEventHub("eventhub01", "grpc://localhost:7053");
            mychannel.addEventHub(eventHub);

            mychannel.joinPeer(peer0_org1);
            mychannel.joinPeer(peer1_org1);

            mychannel.addOrderer(orderer);

            mychannel.initialize();

            fab_Client.getInstance().setUserContext(org2Admin);
            mychannel =fab_Client.getInstance().getChannel("mychannel");
            mychannel.joinPeer(peer0_org2);
            mychannel.joinPeer(peer1_org2);

            Logger.getLogger(NetWork.class.getName()).log(Level.INFO, "Channel created "+mychannel.getName());
            Collection peers = mychannel.getPeers();
            Iterator peerIter = peers.iterator();
            while (peerIter.hasNext())
            {
                Peer pr = (Peer) peerIter.next();
                Logger.getLogger(NetWork.class.getName()).log(Level.INFO,pr.getName()+ " at " + pr.getUrl());
            }
            return new NetWork();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // deploy and instantiate chaincode and return a ChannelClient
    public void deployInstantiate() throws InvalidArgumentException, IOException, ProposalException, ChaincodeEndorsementPolicyParseException {
        try {
            this.net_Channel = this.fab_Client.getInstance().newChannel(Config.CHANNEL_NAME);
            Orderer orderer = this.fab_Client.getInstance().newOrderer(Config.ORDERER_NAME, Config.ORDERER_URL);
            Peer peer0_org1 = this.fab_Client.getInstance().newPeer(Config.ORG1_PEER_0, Config.ORG1_PEER_0_URL);
            Peer peer1_org1 = this.fab_Client.getInstance().newPeer(Config.ORG1_PEER_1, Config.ORG1_PEER_1_URL);
            Peer peer0_org2 = this.fab_Client.getInstance().newPeer(Config.ORG2_PEER_0, Config.ORG2_PEER_0_URL);
            Peer peer1_org2 = this.fab_Client.getInstance().newPeer(Config.ORG2_PEER_1, Config.ORG2_PEER_1_URL);
            this.net_Channel.addOrderer(orderer);
            this.net_Channel.addPeer(peer0_org1);
            this.net_Channel.addPeer(peer1_org1);
            this.net_Channel.addPeer(peer0_org2);
            this.net_Channel.addPeer(peer1_org2);
            this.net_Channel.initialize();

            List<Peer> org1Peers = new ArrayList<Peer>();
            org1Peers.add(peer0_org1);
            org1Peers.add(peer1_org1);

            List<Peer> org2Peers = new ArrayList<Peer>();
            org2Peers.add(peer0_org2);
            org2Peers.add(peer1_org2);

            Collection<ProposalResponse> response = fab_Client.deployChainCode(Config.CHAINCODE_1_NAME,
                    Config.CHAINCODE_1_PATH, Config.CHAINCODE_ROOT_DIR, TransactionRequest.Type.GO_LANG.toString(),
                    Config.CHAINCODE_1_VERSION, org1Peers);

            for (ProposalResponse res : response) {
                Logger.getLogger(NetWork.class.getName()).log(Level.INFO,
                        Config.CHAINCODE_1_NAME + "- Chain code deployment " + res.getStatus());
            }
            fab_Client.getInstance().setUserContext(org2Admin);
            response = fab_Client.deployChainCode(Config.CHAINCODE_1_NAME,
                    Config.CHAINCODE_1_PATH, Config.CHAINCODE_ROOT_DIR, TransactionRequest.Type.GO_LANG.toString(),
                    Config.CHAINCODE_1_VERSION, org2Peers);

            for (ProposalResponse res : response) {
                Logger.getLogger(NetWork.class.getName()).log(Level.INFO,
                        Config.CHAINCODE_1_NAME + "- Chain code deployment " + res.getStatus());
            }

            ChannelClient channelClient = new ChannelClient(net_Channel.getName(), net_Channel, fab_Client);

            String[] arguments = { "" };
            response = channelClient.instantiateChainCode(Config.CHAINCODE_1_NAME, Config.CHAINCODE_1_VERSION,
                    Config.CHAINCODE_1_PATH, TransactionRequest.Type.GO_LANG.toString(), "init", arguments, null);

            for (ProposalResponse res : response) {
                Logger.getLogger(NetWork.class.getName()).log(Level.INFO,
                        Config.CHAINCODE_1_NAME + "- Chain code instantiation " + res.getStatus());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
