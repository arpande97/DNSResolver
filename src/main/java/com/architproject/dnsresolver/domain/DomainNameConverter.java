package com.architproject.dnsresolver.domain;

import com.architproject.dnsresolver.application.DomainNameProcessor;
import com.architproject.dnsresolver.controller.DNSResponseDTO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DomainNameConverter
{
    private int DNS_PORT = 53;

    public DNSResponseEntity processDomainName(String domain, String server)
    {
        byte[] requestArray = createQueryWithDomainName(domain);
        DNSResponseEntity responseObject;
        DatagramSocket socket;
        try
        {
            socket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName(server);
            DatagramPacket request = new DatagramPacket(requestArray,
                    requestArray.length, serverAddress, DNS_PORT);
            socket.send(request);
            DatagramPacket response = new DatagramPacket(new byte[1024],
                    1024);
            socket.receive(response);
            byte[] responseArray = response.getData();
            responseObject =
                    readResponseFromServer(responseArray, domain);
            socket.close();
            return responseObject;
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    private byte[] createQueryWithDomainName(String domainName)
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

    private DNSResponseEntity readResponseFromServer(byte[] responseArray, String domain)
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
        int totalRecordsToParse = numberOfAnswerRRs == 0 ?
                numberOfAuthorityRRs : numberOfAnswerRRs;
        response.setResourceRecords(new ArrayList<>());
        for(int i = 0; i < totalRecordsToParse; i++)
        {
            ResourceRecord record = parseAnswerSection(dataInputStream, numberOfAnswerRRs != 0, responseArray);
            response.getResourceRecords().add(record);
        }

        return response;
    }

    private ResourceRecord parseAnswerSection(DataInputStream dataInputStream, boolean isAnswerRR, byte[] responseArray)
            throws IOException
    {
        ResourceRecord record = new ResourceRecord();
        String name = parseName(dataInputStream, responseArray);
        record.setName(name);
        short aType = dataInputStream.readByte();
        String type = aType == 1 ? "A" : "NS";
        record.setAType(type);
        short aClass = dataInputStream.readShort();
        record.setAClass("IN");
        int ttl = dataInputStream.readInt();
        record.setTimeToLive(ttl);
        short rLength = dataInputStream.readShort();
        record.setRLength(rLength);
        String rData = !isAnswerRR ? parseNameServer(dataInputStream, rLength, responseArray) : parseIP(dataInputStream, rLength);
        record.setRData(rData);
        return record;
    }

    private String parseNameServer(DataInputStream dataInputStream, short length, byte[] responseArray)
            throws IOException
    {
        int counter = 0;
        StringBuilder sb = new StringBuilder();
        while(counter < length)
        {
            byte pointerOrLengthByte = dataInputStream.readByte();
            int pointerBits = (pointerOrLengthByte & 0b11000000 ) >>> 6;
            if(pointerBits == 3)
            {
                int offset = ((pointerOrLengthByte & 0b00111111) << 8 ) | (dataInputStream.readByte());
                String labelAtOffset = findLabelAtOffset(responseArray, offset);
                sb.append(labelAtOffset);
                counter += 2;
            }
            else
            {
                byte[] name = new byte[pointerOrLengthByte];
                for(int i = 0; i < pointerOrLengthByte; i++)
                    name[i] = dataInputStream.readByte();
                sb.append(new String(name, Charset.defaultCharset()));
                counter += 1 + pointerOrLengthByte;
            }
            if(counter + 1 < length)
                sb.append('.');
        }
        return String.valueOf(sb);
    }
    private String findLabelAtOffset(byte[] responseArray, int offset)
            throws IOException
    {
        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(
                responseArray));
        for(int i = 0; i < offset; i++)
            dataInputStream.readByte();
        StringBuilder name = new StringBuilder();
        int labelLength = dataInputStream.readByte();
        while(labelLength > 0)
        {
            byte[] label = new byte[labelLength];
            for(int i = 0; i < labelLength; i++)
                label[i] = dataInputStream.readByte();
            name.append(new String(label, Charset.defaultCharset()));
            labelLength = dataInputStream.readByte();
            if(labelLength > 0)
                name.append('.');
        }
        return String.valueOf(name);

    }
    private String parseName(DataInputStream dataInputStream, byte[] responseArray) throws IOException
    {
        byte pointerOrLengthByte = dataInputStream.readByte();
        StringBuilder sb = new StringBuilder();
        while(pointerOrLengthByte != 0)
        {
            int pointerBits = (pointerOrLengthByte & 0b11000000) >>> 6;
            if(pointerBits == 3)
            {
                int offset = ((pointerOrLengthByte & 0b00111111) << 8 ) | (dataInputStream.readByte());
                String labelAtOffset = findLabelAtOffset(responseArray, offset);
                sb.append(labelAtOffset);
            }
            else
            {
                byte[] name = new byte[pointerOrLengthByte];
                for(int i = 0; i < pointerOrLengthByte; i++)
                    name[i] = dataInputStream.readByte();
                sb.append(new String(name, Charset.defaultCharset()));
            }
            pointerOrLengthByte = dataInputStream.readByte();
            if(pointerOrLengthByte > 0)
                sb.append('.');
        }
        return String.valueOf(sb);
    }
    private String parseIP(DataInputStream dataInputStream,
                                              short len) throws IOException
    {

        StringBuilder sb = new StringBuilder();
        for(int j = 0; j < len; j++)
        {
            int address = dataInputStream.readByte() & 0XFF;
            sb.append(address);
            if(j != len - 1)
                sb.append('.');
        }
        return String.valueOf(sb);
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

    private String convertByteArrayToHex(byte[] requestArray)
    {
        StringBuilder hexStringBuilder = new StringBuilder();
        for(byte b : requestArray)
        {
            hexStringBuilder.append(String.format("%02x", b));
        }
        return hexStringBuilder.toString();
    }

}
