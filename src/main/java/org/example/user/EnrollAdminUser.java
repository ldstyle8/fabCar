package org.example.user;

import org.example.client.CAClient;
import org.example.util.Util;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;

// enroll org's admin user to CA, not common user
public class EnrollAdminUser {
    /**
     * Enroll admin user to OrgMSP
     * @param CA_URL: http://localhost:7054
     * @param Admin: admin name
     * @param Org：org name
     * @param Msp: org MSP
     * @param PassWord: admin password
     */
    // if need call Utill.cleanUp() in advance ???
    public static UserContext enrollAdminUser(String CA_URL, String Admin, String Org, String Msp, String PassWord) throws IllegalAccessException, InvocationTargetException, InvalidArgumentException, InstantiationException, CryptoException, NoSuchMethodException, MalformedURLException, ClassNotFoundException {
        try {
            Util.cleanUp();
            CAClient caClient = new CAClient(CA_URL, null);
            UserContext adminUserContext = new UserContext();
            adminUserContext.setName(Admin);
            adminUserContext.setAffiliation(Org);
            adminUserContext.setMspId(Msp);
            caClient.setAdminUserContext(adminUserContext);
            return adminUserContext;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
