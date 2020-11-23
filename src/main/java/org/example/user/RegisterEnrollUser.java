package org.example.user;

import org.example.client.CAClient;
import org.example.util.Util;

public class RegisterEnrollUser {
    public UserContext userContext;
    public CAClient caClient;
    public UserContext adminUserContext;
    public void enrollAdminUser(String CA_URL, String Admin, String Org, String Msp, String PassWord) throws Exception {
        Util.cleanUp();
        caClient = new CAClient(CA_URL, null);
        adminUserContext = new UserContext();
        adminUserContext.setName(Admin);
        adminUserContext.setAffiliation(Org);
        adminUserContext.setMspId(Msp);
        caClient.setAdminUserContext(adminUserContext);
        adminUserContext = caClient.enrollAdminUser(Admin, PassWord);
    }

    public void registerAndEnrollUser(String name, String Org, String Msp) throws Exception {
        userContext = new UserContext();
        userContext.setName(name);
        userContext.setAffiliation(Org);
        userContext.setMspId(Msp);
        // register
        String eSecret = caClient.registerUser(name, Org);
        // enroll
        userContext = caClient.enrollUser(userContext, eSecret);
    }
}
