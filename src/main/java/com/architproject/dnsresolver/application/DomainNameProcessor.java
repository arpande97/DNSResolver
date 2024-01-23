package com.architproject.dnsresolver.application;

import com.architproject.dnsresolver.controller.DNSResponseDTO;
import com.architproject.dnsresolver.domain.DNSResponseEntity;
import com.architproject.dnsresolver.domain.DomainNameConverter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;


public class DomainNameProcessor
{
    private int dnsPort = 53;
    private String serverIp = "8.8.8.8";


    DomainNameConverter domainNameConverter = new DomainNameConverter();
    public DNSResponseDTO processDomainName(String domain)
    {
        byte[] requestArray = domainNameConverter.createQueryWithDomainName(domain);
        //For debug purposes
        //String requestString = convertByteArrayToHex(requestArray);
        DNSResponseEntity responseObject;
        DatagramSocket socket;
        try
        {
            socket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName(serverIp);
            DatagramPacket request = new DatagramPacket(requestArray,
                    requestArray.length, serverAddress, dnsPort);
            socket.send(request);
            DatagramPacket response = new DatagramPacket(new byte[1024],
                    1024);
            socket.receive(response);
            byte[] responseArray = response.getData();
            responseObject =
                    domainNameConverter.readResponseFromServer(responseArray, domain);
            socket.close();
            return convertEntityToDTO(responseObject);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    private DNSResponseDTO convertEntityToDTO(DNSResponseEntity responseObject)
    {
        DNSResponseDTO dto = new DNSResponseDTO();
        dto.setQuery(responseObject.getQuery());
        dto.setListOfIPs(responseObject.getListOfIPs());
        return dto;
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
//-----------header---------------------       ---------question--------------------------
//        id    flags  qd       an     ns     ar              q	                       qt    qs
//        0016  0100   0001    0000   0000   0000      03646e7306676f6f676c6503636f6d00  0001  0001