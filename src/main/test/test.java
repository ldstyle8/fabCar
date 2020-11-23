import org.example.chaincode.invocation.InvokeQueryChaincode;
import org.example.network.NetWork;

import org.example.config.Config;
import org.example.user.RegisterEnrollUser;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import java.io.IOException;

class Test {
    private static final NetWork net = new NetWork();
    private RegisterEnrollUser myreu = new RegisterEnrollUser();
    InvokeQueryChaincode CC = new InvokeQueryChaincode();
    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        System.out.println("test begin");
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        System.out.println("test end");
    }

    @org.junit.jupiter.api.Test
    void createChannel() {
        net.init();
        net.createChannel(Config.CHANNEL_CONFIG_PATH,Config.CHANNEL_NAME);
    }

    @org.junit.jupiter.api.Test
    void deployInstantiate() {
        try {
            net.init();
            net.deployInstantiate();
        } catch (InvalidArgumentException | ChaincodeEndorsementPolicyParseException | ProposalException | IOException e) {
            e.printStackTrace();
        }
    }

    @org.junit.jupiter.api.Test
    void reu() throws Exception {
        myreu.enrollAdminUser(Config.CA_ORG1_URL,Config.ADMIN,Config.ORG1,Config.ORG1_MSP,Config.ADMIN_PASSWORD);
        myreu.registerAndEnrollUser("user"+System.currentTimeMillis(),Config.ORG1,Config.ORG1_MSP);
    }

    @org.junit.jupiter.api.Test
    void invokeChaincode() {
        CC.init();
        String[] arguments= { "CAR1", "Chevy", "Volt", "Red", "Nick" };
        String[] arguments2= { "CAR2", "Chevy", "Volt", "White", "Bella" };
        CC.invoke_chaincode(arguments);
        CC.invoke_chaincode(arguments2);
    }
    @org.junit.jupiter.api.Test
    void QueryChaincode() {
        CC.init();
        String[] args1= {"CAR1"};
        String[] args2= {"CAR2"};
        CC.queryAllcars();
        CC.queryOnecar(args1);
        CC.queryOnecar(args2);
    }
}