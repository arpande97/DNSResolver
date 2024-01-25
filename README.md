Run the resolver with: mvn spring-boot:run -Dcheckstyle.skip -Ddocker.skip

Usage: sh domainNameResolver.sh www.google.com

Handles only 'A' Type records (future work: extend to CNAME records)

Recursively queries name servers if there are no answer resource records and only 'NS' records 
are sent back:

<img width="1487" alt="Screen Shot 2024-01-25 at 18 08 38 PM" src="https://github.com/arpande97/DNSResolver/assets/62608663/eed891d7-45cf-45c0-8497-aea025daa7ad">
