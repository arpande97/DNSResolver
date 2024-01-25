package com.architproject.dnsresolver.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class ResourceRecord
{
    String name;
    String aType;
    String aClass;
    int timeToLive;
    short rLength;
    String rData;

}
