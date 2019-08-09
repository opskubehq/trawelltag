package com.trawelltag.insurance;

import java.nio.ByteBuffer;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

public class AesCipherWithUnirest {
    protected String data;
    protected String initVector;
    protected String errorMessage;

    private AesCipherWithUnirest(String initVector, String data, String errorMessage) {
        super();

        this.initVector = initVector;
        this.data = data;
        this.errorMessage = errorMessage;
    }

    public static AesCipherWithUnirest encrypt(String secretKey, String plainText, String ref) {
        String initVector = null;
        try {
            if (!isKeyLengthValid(secretKey)) {
                throw new Exception("Secret key's length must be 128, 192 or 256 bits");
            }
            
            String first16CharsOfRefKey=StringUtils.substring(ref, 0, 16);
            System.out.println(first16CharsOfRefKey);

            byte[] initVectorBytes = first16CharsOfRefKey.getBytes(); //new String("b12287d3-5e89-49").getBytes(); //16 char of reference key
            IvParameterSpec ivParameterSpec = new IvParameterSpec(initVectorBytes);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("RIJNDAEL/CBC/NoPadding"); //cipher key
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

            byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));

            ByteBuffer byteBuffer = ByteBuffer.allocate(initVectorBytes.length + encrypted.length);
            byteBuffer.put(initVectorBytes);
            byteBuffer.put(encrypted);

            String result = Base64.getEncoder().encodeToString(byteBuffer.array());

            return new AesCipherWithUnirest(initVector, result, null);
        } catch (Throwable t) {
            t.printStackTrace();
            return new AesCipherWithUnirest(initVector, null, t.getMessage());
        }
    }

    public static boolean isKeyLengthValid(String key) {
    	System.out.println(key.length());
        return key.length() == 16 || key.length() == 24 || key.length() == 32;
    }

    public String getData() {
        return data;
    }

    public String toString() {
        return getData();
    }

    public static void main(String[] args) throws Exception {
        String secretKey = "55b5114-70f9-4481-8472-f50377711"; //32 char of signature key
        //We have added 14 space character to make it's length multiple of 16 bytes as NoPadding was required
        String sign = "ac33ff50-e0f7-47a5-ad0a-f534e9140938";
        String ref = "05f01a6d-fe18-4849-aa72-7c282f2bd9c6";
        String branchSign="4ea61fdf-9b88-4e46-a7b7-5f750839a81d";
        String userName="Aadesh_Trvl";
        
        String reference = "AT"+RandomUtils.nextInt();
        
        StringBuilder sbPolicyXml = new StringBuilder();
        sbPolicyXml.append("<policy><identity>");
        sbPolicyXml.append("<sign>");
        sbPolicyXml.append(sign);
        sbPolicyXml.append("</sign>");
        sbPolicyXml.append("<branchsign>");
        sbPolicyXml.append(branchSign);
        sbPolicyXml.append("</branchsign>");
        sbPolicyXml.append("<username>");
        sbPolicyXml.append(userName);
        sbPolicyXml.append("</username>");
        sbPolicyXml.append("<reference>");
        sbPolicyXml.append(reference);
        sbPolicyXml.append("</reference>");
        sbPolicyXml.append("</identity><plan><categorycode>DE5EE71C-098F-4CC0-B489FA8</categorycode><plancode>9b60d24f-5270-40c0-801ba</plancode><basecharges>10</basecharges><riders></riders ><totalbasecharges>11</totalbasecharges><servicetax>1</servicetax><totalcharges>11</totalcharges></plan><traveldetails><departuredate>24-JUN-2019</departuredate><days>6</days><arrivaldate>29-JUN-2019</arrivaldate></traveldetails><insured><passport></passport><contactdetails><address1>Test</address1><address2></address2><city>Test</city><district>Test</district><state>Test</state><pincode>3434</pincode><country>India</country><phoneno>545435</phoneno><mobile> 45545</mobile><emailaddress>Test@yatra.com</emailaddress></contactdetails><name>Akshay</name><dateofbirth>05-Aug-1989</dateofbirth><age>30</age><trawelltagnumber></trawelltagnumber><nominee>self</nominee><relation></relation><pastillness></pastillness></insured><otherdetails><policycomment></policycomment><universityname></universityname><universityaddress></universityaddress></otherdetails></policy>");
        
        String strPolicyXml=sbPolicyXml.toString();
        System.out.println(strPolicyXml);
        int lengthOfXml = strPolicyXml.length();

        int remainder =  lengthOfXml %16;
        
        strPolicyXml = StringUtils.rightPad(strPolicyXml, lengthOfXml+(16-remainder));
        lengthOfXml = strPolicyXml.length();

        
        AesCipherWithUnirest encrypted = AesCipherWithUnirest.encrypt(secretKey, strPolicyXml,ref);
        
        String strEncryptedPolicyXml=encrypted.getData();
        System.out.print(strEncryptedPolicyXml);
        HttpResponse<String> response = Unirest.post("http://karvatgroup.org/trawelltag/v2/CreatePolicy.aspx")
        		  .header("User-Agent", "PostmanRuntime/7.15.0")
        		  .header("Accept", "*/*")
        		  .header("Cache-Control", "no-cache")
        		  .header("Host", "karvatgroup.org")
        		  .header("accept-encoding", "gzip, deflate")
        		  .header("content-type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
        		  .header("Connection", "keep-alive")
        		  .header("cache-control", "no-cache")
        		  .body("------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"Data\"\r\n\r\n"+strEncryptedPolicyXml+"\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"Ref\"\r\n\r\n"+ref+"\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW--")
        		  .asString();
        System.out.println("\n\n"+response.getBody()+"\n\n");
        
    }
}