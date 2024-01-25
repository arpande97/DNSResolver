package com.architproject.dnsresolver.controller;

import com.architproject.dnsresolver.domain.ResourceRecord;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter @NoArgsConstructor
public class DNSResponseDTO
{
    private String query;
    private short numberOfAnswers;
    private List<ResourceRecord> records;

}
