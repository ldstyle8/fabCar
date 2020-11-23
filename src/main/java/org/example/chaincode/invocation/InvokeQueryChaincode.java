package org.example.chaincode.invocation;

import org.example.client.CAClient;
import org.example.client.ChannelClient;
import org.example.client.FabricClient;
import org.example.config.Config;
import org.example.user.UserContext;
import org.example.util.Util;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.ChaincodeResponse.Status;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

public class InvokeQueryChaincode {
    private static final byte[] EXPECTED_EVENT_DATA = "!".getBytes(UTF_8);
    private static final String EXPECTED_EVENT_NAME = "event";
    public FabricClient fab_Client;
    public ChannelClient channel_Client;
    public void init()
    {
        try {
            Util.cleanUp();
            String caUrl = Config.CA_ORG1_URL;
            CAClient caClient = new CAClient(caUrl, null);
            // Enroll Admin to Org1MSP
            UserContext adminUserContext = new UserContext();

            adminUserContext.setName(Config.ADMIN);
            adminUserContext.setAffiliation(Config.ORG1);
            adminUserContext.setMspId(Config.ORG1_MSP);
            caClient.setAdminUserContext(adminUserContext);
            adminUserContext = caClient.enrollAdminUser(Config.ADMIN, Config.ADMIN_PASSWORD);

            this.fab_Client =new FabricClient(adminUserContext);

            this.channel_Client = fab_Client.createChannelClient(Config.CHANNEL_NAME);
            Channel channel = channel_Client.getChannel();
            Peer peer = fab_Client.getInstance().newPeer(Config.ORG1_PEER_0, Config.ORG1_PEER_0_URL);
            EventHub eventHub = fab_Client.getInstance().newEventHub("eventhub01", "grpc://localhost:7053");
            Orderer orderer = fab_Client.getInstance().newOrderer(Config.ORDERER_NAME, Config.ORDERER_URL);
            channel.addPeer(peer);
            channel.addEventHub(eventHub);
            channel.addOrderer(orderer);
            channel.initialize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void invoke_chaincode(String[] arguments) {
        try {
            TransactionProposalRequest request = fab_Client.getInstance().newTransactionProposalRequest();
            ChaincodeID ccid = ChaincodeID.newBuilder().setName(Config.CHAINCODE_1_NAME).build();
            request.setChaincodeID(ccid);
            request.setFcn("createCar");
            request.setArgs(arguments);
            request.setProposalWaitTime(1000);

            Map<String, byte[]> tm2 = new HashMap<>();
            tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8));
            tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8));
            tm2.put("result", ":)".getBytes(UTF_8));
            tm2.put(EXPECTED_EVENT_NAME, EXPECTED_EVENT_DATA);
            request.setTransientMap(tm2);
            Collection<ProposalResponse> responses = channel_Client.sendTransactionProposal(request);
            for (ProposalResponse res: responses) {
                Status status = res.getStatus();
                Logger.getLogger(InvokeQueryChaincode.class.getName()).log(Level.INFO,"Invoked createCar on "+Config.CHAINCODE_1_NAME + ". Status - " + status);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void queryAllcars(){
        try {
            Logger.getLogger(InvokeQueryChaincode.class.getName()).log(Level.INFO, "Querying for all cars ...");
            Collection<ProposalResponse>  responsesQuery = channel_Client.queryByChainCode("fabcar", "queryAllCars", null);
            for (ProposalResponse pres : responsesQuery) {
                String stringResponse = new String(pres.getChaincodeActionResponsePayload());
                Logger.getLogger(InvokeQueryChaincode.class.getName()).log(Level.INFO, stringResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void queryOnecar(String[] args_car){
        try {
            Logger.getLogger(InvokeQueryChaincode.class.getName()).log(Level.INFO, "Querying for a car - " + args_car[0]);

            Collection<ProposalResponse>  responses1Query = channel_Client.queryByChainCode("fabcar", "queryCar", args_car);
            for (ProposalResponse pres : responses1Query) {
                String stringResponse = new String(pres.getChaincodeActionResponsePayload());
                Logger.getLogger(InvokeQueryChaincode.class.getName()).log(Level.INFO, stringResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
