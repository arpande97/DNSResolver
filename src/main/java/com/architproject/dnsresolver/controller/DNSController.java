package com.architproject.dnsresolver.controller;


import com.architproject.dnsresolver.application.DomainNameProcessor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class DNSController
{
    private DomainNameProcessor domainNameProcessor = new DomainNameProcessor();
    @PostMapping("/resolver")
    public DNSResponseDTO resolveDomainName(@RequestBody String domainName)
    {

        DNSResponseDTO responseDTO =
                domainNameProcessor.processDomainName(domainName);
        return responseDTO;
    }
}
