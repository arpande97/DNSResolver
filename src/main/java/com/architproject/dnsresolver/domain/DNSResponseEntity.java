package com.architproject.dnsresolver.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter @NoArgsConstructor
public class DNSResponseEntity
{
    private String query;
    private short id;
    private short numberOfQuestions;
    private short numberOfAnswers;
    private short numberOfAuthorities;
    private short numberOfAdditional;
    private List<ResourceRecord> resourceRecords;
}
