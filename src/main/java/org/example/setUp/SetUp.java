package org.example.setUp;

import org.example.client.ChannelClient;
import org.example.client.FabricClient;
import org.example.config.Config;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SetUp {
    private static final byte[] EXPECTED_EVENT_DATA = "!".getBytes(UTF_8);
    private static final String EXPECTED_EVENT_NAME = "event";
    public Channel setChannel;
    public FabricClient setFabClient;
    public ChannelClient setChClient;
    public ChaincodeID ccid;

    public SetUp(Channel sChannel, FabricClient sFabClient, ChannelClient sChClient) {
        setChannel = sChannel;
        setFabClient = sFabClient;
        setChClient = sChClient;
        ccid = CCid();
    }

    protected ChaincodeID CCid() {
        return ChaincodeID.newBuilder().setName(Config.CHAINCODE_1_NAME).build();
    }

    public void initLedger() {
        try {
            TransactionProposalRequest request = this.setFabClient.getInstance().newTransactionProposalRequest();
            request.setChaincodeID(this.ccid);
            request.setFcn("initLedger");
            request.setArgs((byte[]) null);
            Collection<ProposalResponse> responses = this.setChClient.sendTransactionProposal(request);
            for (ProposalResponse res: responses) {
                ChaincodeResponse.Status status = res.getStatus();
                Logger.getLogger(SetUp.class.getName()).log(Level.INFO,"Invoked initLedger on "+Config.CHAINCODE_1_NAME + ". Status - " + status);
            }
        } catch (ProposalException | InvalidArgumentException e) {
            e.printStackTrace();
        }
    }

    public void creatCar(String Key, FabCar car) {
        try {
//            EventHub eventHub = this.setFabClient.getInstance().newEventHub("eventhub01", "grpc://localhost:7053");
//            this.setChannel.addEventHub(eventHub);

            TransactionProposalRequest request = this.setFabClient.getInstance().newTransactionProposalRequest();
//            ChaincodeID ccid = ChaincodeID.newBuilder().setName(Config.CHAINCODE_1_NAME).build();
            request.setChaincodeID(this.ccid);
            request.setFcn("createCar");
            String[] arguments = { Key, car.Make, car.Model, car.Colour, car.Owner};
            request.setArgs(arguments);
            request.setProposalWaitTime(1000);

            Map<String, byte[]> tm2 = new HashMap<>();
            tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8));
            tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8));
            tm2.put("result", ":)".getBytes(UTF_8));
            tm2.put(EXPECTED_EVENT_NAME, EXPECTED_EVENT_DATA);
            request.setTransientMap(tm2);
            Collection<ProposalResponse> responses = this.setChClient.sendTransactionProposal(request);
//            StringBuilder result = null;
            for (ProposalResponse res: responses) {
                ChaincodeResponse.Status status = res.getStatus();
                Logger.getLogger(SetUp.class.getName()).log(Level.INFO,"Invoked createCar on "+Config.CHAINCODE_1_NAME + ". Status - " + status);
                assert false;
//                result.append("Invoked createCar on " + Config.CHAINCODE_1_NAME + ". Status - ").append(status).append("\n");
            }
//            assert false;
//            System.out.println(result.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void queryAllCars() {
        try  {
            Collection<ProposalResponse>  responsesQuery = this.setChClient.queryByChainCode("fabcar", "queryAllCars", null);
            for (ProposalResponse pres : responsesQuery) {
                String stringResponse = new String(pres.getChaincodeActionResponsePayload());
                System.out.println(stringResponse);
            }
        } catch (ProposalException | InvalidArgumentException e) {
            e.printStackTrace();
        }
    }

    public void queryCar(String[] Keys) {
        try {
            Logger.getLogger(SetUp.class.getName()).log(Level.INFO, "Querying for a car - " + Keys[0]);
            Collection<ProposalResponse>  responses1Query = this.setChClient.queryByChainCode("fabcar", "queryCar", Keys);
            for (ProposalResponse pres : responses1Query) {
                String stringResponse = new String(pres.getChaincodeActionResponsePayload());
                System.out.println(stringResponse);
            }
        } catch (ProposalException | InvalidArgumentException e) {
            e.printStackTrace();
        }
    }

    public void changeCarOwner(String key, String owner) {
        try {
            String[] arguments = {key, owner};
            TransactionProposalRequest request = this.setFabClient.getInstance().newTransactionProposalRequest();
            request.setFcn("changeCarOwner");
            request.setArgs(arguments);
            Collection<ProposalResponse> responses = this.setChClient.sendTransactionProposal(request);
            for (ProposalResponse res: responses) {
                ChaincodeResponse.Status status = res.getStatus();
                Logger.getLogger(SetUp.class.getName()).log(Level.INFO,"Invoked changeCarOwner on "+Config.CHAINCODE_1_NAME + ". Status - " + status);
            }
        } catch (ProposalException | InvalidArgumentException e) {
            e.printStackTrace();
        }
    }

}
