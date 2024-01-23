package com.architproject.dnsresolver.controller;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter @NoArgsConstructor
public class DNSResponseDTO
{
    private String query;
    private List<String> listOfIPs;

}
