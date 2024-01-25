Run the resolver with: mvn spring-boot:run -Dcheckstyle.skip -Ddocker.skip
Usage: sh domainNameResolver.sh www.google.com
Handles only 'A' Type records (future work: extend to CNAME records)

Recursively queries name servers if there are no answer resource records and only 'NS' records 
are sent back:
![](../../Screen Shot 2024-01-25 at 18.08.38 PM.png)