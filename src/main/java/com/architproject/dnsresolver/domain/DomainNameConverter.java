package com.architproject.dnsresolver.domain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


public class DomainNameConverter
{


    public byte[] createQueryWithDomainName(String domainName)
    {
        List<Byte> requestList = new ArrayList<>();
        int id = (int)(Math.random() * 65536);
        requestList.add((byte)(id >> 8));
        requestList.add((byte)(id & 0XFF));
        requestList.add((byte)(1));
        requestList.add((byte)(0));
        requestList.add((byte)(0));
        requestList.add((byte)(1));
        requestList.add((byte)(0));
        requestList.add((byte)(0));
        requestList.add((byte)(0));
        requestList.add((byte)(0));
        requestList.add((byte)(0));
        requestList.add((byte)(0));
        byte[] encodedQuestion = encodeDomainInRFCFormat(domainName);
        for(byte b : encodedQuestion)
        {
            requestList.add(b);
        }
        requestList.add((byte) 0);
        requestList.add((byte) 1);
        requestList.add((byte) 0);
        requestList.add((byte) 1);
        byte[] requestByteArray = new byte[requestList.size()];
        int ptr = 0;
        for(byte b : requestList)
            requestByteArray[ptr++] = b;
        return requestByteArray;
    }

    public DNSResponseEntity readResponseFromServer(byte[] responseArray, String domain)
            throws IOException
    {
        DNSResponseEntity response = new DNSResponseEntity();
        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(
                responseArray));
        short id = dataInputStream.readShort();
        response.setId(id);
        short flags = dataInputStream.readByte();
        int qr = (flags & 0b10000000) >>> 7;
        int opCode = (flags & 0b01111000) >>> 3;
        dataInputStream.readByte();
        short numberOfQuestions = dataInputStream.readShort();
        response.setNumberOfQuestions(numberOfQuestions);
        short numberOfAnswerRRs = dataInputStream.readShort();
        response.setNumberOfAnswers(numberOfAnswerRRs);
        short numberOfAuthorityRRs = dataInputStream.readShort();
        response.setNumberOfAuthorities(numberOfAuthorityRRs);
        short numberOfAdditionalRRs = dataInputStream.readShort();
        response.setNumberOfAdditional(numberOfAdditionalRRs);
        String qName = parseQName(dataInputStream);
        response.setQuery(qName);
        short qType = dataInputStream.readShort();
        short qClass = dataInputStream.readShort();
        List<String> listOfIPs =
                parseIPsFromResponse(dataInputStream, numberOfAnswerRRs);
        response.setListOfIPs(listOfIPs);
        return response;
    }

    private List<String> parseIPsFromResponse(DataInputStream dataInputStream,
                                              short anCount) throws IOException
    {
        //Assumption here is this app handles one question per request
        //So we don't need to look for the name field
        List<String> listOfIPs = new ArrayList<>();
        for(int i = 0; i < anCount; i++)
        {
             short offset = dataInputStream.readShort();
             short qType = dataInputStream.readShort();
             short qClass = dataInputStream.readShort();
             int timeToLive = dataInputStream.readInt();
             int len = dataInputStream.readShort();
             StringBuilder sb = new StringBuilder();
             for(int j = 0; j < len; j++)
             {
                 int address = dataInputStream.readByte();
                 sb.append(address);
                 if(j != len - 1)
                     sb.append('.');
             }
             listOfIPs.add(String.valueOf(sb));

        }
        return listOfIPs;
    }

    private String parseQName(DataInputStream dataInputStream)
            throws IOException
    {
        StringBuilder qName = new StringBuilder();
        int labelLength = dataInputStream.readByte();
        while(labelLength > 0)
        {
            byte[] label = new byte[labelLength];
            for(int i = 0; i < labelLength; i++)
                label[i] = dataInputStream.readByte();
            qName.append(new String(label, Charset.defaultCharset()));
            labelLength = dataInputStream.readByte();
            if(labelLength > 0)
                qName.append('.');
        }
        return String.valueOf(qName);
    }


    private byte[] encodeDomainInRFCFormat(String domainName)
    {
        ByteArrayOutputStream encodedQuestion = new ByteArrayOutputStream();
        String[] labels = domainName.split("\\.");
        for(String label : labels)
        {
            int length = label.length();
            encodedQuestion.write((byte)length);
            for(char c : label.toCharArray())
            {
                encodedQuestion.write((byte) c);
            }
        }
        encodedQuestion.write((byte) 0);
        return encodedQuestion.toByteArray();
    }

}
