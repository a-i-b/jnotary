jnotary
=======

Implementation of Data Validation and Certification Server (RFC 3029)

jnotary is early implementation of DVCS (RFC 3029), built using Java (JEE) technology.
jnotary is open source software.
It is implemented as REST service with use of JBoss AS 7.1. Bouncy Castle Crypto API is used for object generation and many other purposes.

Implemented main features:
- library for generation/parsing of DVCS objects;
- cpd, ccpd, vsd and vpkc services;
- administration console with build-in JBoss authentication;
- simple command line client to execute DVCS query.

Details of implementation

Currently supported features:
- various signature and digest algorithms, that are configured in administration console;
- certificate verification with CRL
- using CRL from certificate's extention;
- CRL cashe;
- ability to set CRL URI for root certificate, if there is no extention with CRL URI;
- ability to add root certificate for user certificate verification;
- vpkc service can verifying only one certificate per query.


Not supported featurers, that are have to in future implemented
- certificate verification with OCSP;
- Timestamp verification;
- CADES signature verification;
- vpkc service: cerificate chain verification;
- authentification for REST service.


Installing jnotary

1. Install JBoss AS 7.1
2. Create JBoss "application user" with add-aser script
User name: admin
Role: dvcs
3. Compile project with maven
mvn clean install -Dmaven.test.skip=true
4. Deploy (copy) dvcs-srv.war from taget directory to JBoss deployments directory
5. Create user key store (with certicate and private key) in PKCS#12 format und store in directory, that is available for JEE application
6. Create trusted root's store in JKS format und store in directory, that is available for JEE application
7. Add to trusted root's store all root certificates, that you need
8. Start JBoss Server
9. Open URL <server address>:8080/dvcs-srv/admin
10. Enter login/password
11. On tabpanel "Settins" enter path to user key store, and also passwords and etc
12. On tabpanel "Trusted roots" enter path to trusted root's  store, password and etc. You will see complete list of certificates in store
13. If you user certificates contains no extentions with CRL URL, you can add URL to CRL on tabpanel "Additional CRLs"
14. Run client with appropriate parameters "java -jar dvcs-client-0.0.1-SNAPSHOT.jar options input-file output-file"


By questions und problems, please contact
balaschow.a@gmail.com





