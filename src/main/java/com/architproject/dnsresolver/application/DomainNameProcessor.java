package com.architproject.dnsresolver.application;

import com.architproject.dnsresolver.controller.DNSResponseDTO;
import com.architproject.dnsresolver.domain.DNSResponseEntity;
import com.architproject.dnsresolver.domain.DomainNameConverter;
import com.architproject.dnsresolver.domain.ResourceRecord;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;


public class DomainNameProcessor
{
    private int DNS_PORT = 53;
    private String DEFAULT_SERVER_IP = "198.41.0.4";


    DomainNameConverter domainNameConverter = new DomainNameConverter();
    public DNSResponseDTO processDomain(String domain)
    {

        DNSResponseEntity responseEntity = domainNameConverter.processDomainName(domain, DEFAULT_SERVER_IP);
        if(checkIfAnswerRRsAreEmpty(responseEntity))
        {
            responseEntity = queryForNameServers(responseEntity, domain);
        }
        return convertEntityToDTO(responseEntity);
    }
    private boolean checkIfAnswerRRsAreEmpty(DNSResponseEntity response)
    {
        return response.getNumberOfAnswers() == 0;
    }
    private DNSResponseEntity queryForNameServers(DNSResponseEntity response, String domain)
    {
        List<String> nameServers = new ArrayList<>();
        List<ResourceRecord> records = response.getResourceRecords();
        for(ResourceRecord record : records)
        {
            nameServers.add(record.getRData());
        }
        for(String nameServer : nameServers)
        {
            System.out.println("Querying " + nameServer);
            DNSResponseEntity responseFromNameServer = domainNameConverter.processDomainName(domain, nameServer);
            if(responseFromNameServer.getNumberOfAnswers() != 0)
                return responseFromNameServer;
            else
                return queryForNameServers(responseFromNameServer, domain);
        }
        return null;
    }

    private DNSResponseDTO convertEntityToDTO(DNSResponseEntity responseObject)
    {
        DNSResponseDTO dto = new DNSResponseDTO();
        dto.setQuery(responseObject.getQuery());
        dto.setRecords(responseObject.getResourceRecords());
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