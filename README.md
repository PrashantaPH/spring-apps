Cloud Watch console path :-
EBS-Service -> PLATEBSILLUSBS204
LegacyEBS-Service -> LEGACYEBSBS204
Illustration-UI -> IllustrationWeb-204
EBS-UI -> PlatEBSWeb-204

EBS-DTS-Service -> PlatformDTS204
Legacy-DTS-Service -> legacy-dts-204

BitBucket 
UserName : Prashanta.Hanumantagoudra
Password : Prashanta.Hanumantagoudra02

	Tasks:-

# Sep to Oct/22 Working on CRA Mule API FT-25357

# Nov/22 Working EBS-DTS-Service sonar code smell fixes FT-25367

# Feb/23 Working on LegacyEBS-Service Sonar fixes FT-31275

# Mar/23 Working on Defect 
	Getting "IT ServiceDesk" error on amending RA EBS which has removed Tax rates. FT-32224

# Apr/23 Working on userstory
	Stop mapping "Greater of AWE and 10%" to "NAE" in illustrations (RFC4463E) FT-28900
	
# May/23 Workign on EBS-DTS-Service property files having SonarQube Critical bug and vulnerability FT-36983
	
# Jun/23 Working on IMA phase 1
	Rename changes for 'Gilts' to 'Global Government Bond' and 'Index Linked Gilts' to 'Global Government Inflation Linked Bond'
	Applicable for Bluedoor and Asia products 
	FT-36975,FT-36977,FT-36978,FT-37298,FT-37299,FT-37300,FT-37302,FT-37303,FT-37304,FT-37305,FT-37306,FT-37308,
	FT-37309,FT-37310,FT-37311,FT-37312,FT-37313,FT-37314,FT-37315,FT-37316,FT-37413,FT-37414,FT-39456
	
# Jun to July/23 Working on 
	EBS-DTS-Service
		AmazonS3 warning issue fix and sonar qube critical code smell fix FT-38657
	Legacy-Lambda
		Turn off the secret values printing in the logs FT-39641
	EBS-Lambda
		Turn off the secret values printing in the logs FT-41096
	
# Aug/23 Working IMA Phase 2 
	IMA Phase 2 - Update Portfolio weightings for Legacy Offshore Bonds  FT-41660 
	R1260 D21 changes for platform product portfolio weightings changes and fund charges changes - Bluedoor and Asia products D21 mock changes
	FT-41649,FT-41652,FT-41665,FT-41653,FT-41654,FT-41666,FT-41655,FT-41657,FT-41667,FT-41658,FT-41659,FT-41668,FT-41663,FT-41670,FT-42962
	
	Legacy-DTS-Service
		Container scan critical and high issues fix for FT-43175
		SonarQube critical code smell and AmazonS3Client warning issue fix
	
# Sep to Oct/23 Workign On CATODD Pahse 2 Applicatble for PUT,PIB,PIIB,PIRIB only for trust flow
	Added the IsMagicPlatformEnabled tag to acord response from BackgroundAssumptionsService FT-45319
	Development for 'read' action from DocumentService in EBS FT-43775
	Updated the 'Trust' type for on the fly trust records created via Illustrations FT-45831
	
	Flag (IsMagicPlatformEnabled) check from BackgroundAssumptionsService API.
	NB and Unmatched top-up set the ClientId to get the TrackingItemId from Tracking item service API and set the Tracking item id to document service to get the TRS document details from ibiz.
	Matched top-up set the ClinetName to get the TrackingItemId  from Tracking item service API and set the Tracking item id to document service API to get the TRS document details from ibiz.
	Develop logic to compare the roles (Account Owner, Trustee) in EBS for the clients available in the extract from MAGIC.
	
# Nov/23 
	Working on AccountServiceRADS total=8 fixed=5
		Fix AccountServiceRADS container scan and security hotspots FT-47321
	
	Working on AccountMatchingDS total=24 fixed
		Fix AccountMatchingDS container scan
		Note : NINumber regarding this issue i have chages the EBS-Service req 
		Ex : https://sjpbitbucket.sjp.co.uk/projects/NB/repos/ebs-service/commits/171078fe498cb1216e40c9e16e7eec4950e50224#BCSIntegration%2Fsrc%2Fmain%2Fjava%2Fcom%2Fseec%2Finsurance%2Flife%2Fillustrationinquiry%2Fintegration%2Fswiftdb%2FCompoundIllustrationInquiry.java
	
	Working on EBS-Service CATODD
		Enhancement : Display only 'Trust' type or records with NULL type in search results while adding a trust as account owner in Illustrations FT-46722
			
# Dec/23
	Working on EBS-Service ID&V
		Corporate IDV defect Fix FT-49448
		
# Jan/24
	Suppress 'ReasonForTRSWarning' tag in S3 XML FT-49810
	
	Note : EBS-Service and LegacyEBS-Service
	Enhancement: Corporate Sub type field from iBusiness to EBS and vice versa FT-49946
	Note : Handling the trust,corporate and null type 
			* Null type trust type means not populating the field. only populating when the corporate type has chosed.
			
# Feb/24
	Suppress 'ReasonForPSCWarning' tag in S3 XML FT-50305
	
	Corretto jdk 11 has been updated FT-52512,FT-52513,FT-52514,FT-52515,FT-52516,FT-52517,FT-52518,FT-52519
	
# March/24
	Enhancement : Catalyst Pre-populate Illustration Amend/Copy FT-53361
	
	Sonarqube Critical Code Smell Fixes FT-53990 -> IllustrationOBPasRequestMappingUtil.java (Note: Before fix 45, After fix 13)
	
#

P25064
SJPT3sting

Note : if we need to revome the tag before genarating a S3 xml 
ApplicationSubmitSwiftIntegration.java
	composeTxLifeForS3-> method
	
* PSC Not applicable for Salsefors client for that saparate user story

	
 Accord jar generation:	
set JAVA_HOME=jdk1.8.0_192
set JAVA=%JAVA_HOME%\bin\java
set cp=%CLASSPATH%
set JAVAC=%JAVA_HOME%\bin\javac 
set cp=.;.\castor-1.0.5.jar;.\castor-1.0.5-xml.jar;.\commons-logging-api.jar;.\castor-1.0.5-commons.jar;.\castor-1.0.5-srcgen-ant-task.jar;.\xercesImpl.jar;.\log4j.jar;.\xmlParserAPIs.jar;.\xmlparserv2.jar;
 
%JAVA% -classpath %CP%  -Xms512m -Xmx512m org.exolab.castor.builder.SourceGeneratorMain -f -i TXLife2.20.00.xsd  -package com.seec.insurance.life.objects

 Imp Note
 EBS-Service
 All platform products mentained by intellect
 IIB, IRIB - illustration creation by  intellect.
 
 Legacy products (LegacyEBS-Service)
 HKUT, HKIIB, SHIIB, SGIIB - These products mentained by SSP only illustration creation.
 IIB, IRIB, HKUT, HKIIB, SHIIB, SGIIB - But EBS creation handled by  intellect.
 
 EBS WebService local test
 public static void main(String[] args) {
		
		Endpoint.publish("http://localhost:8080/BCSWebservice/", new AccountMigrationSearchServiceImpl());
	}
	
Note : Matched Topup 
ISA,JISA,UT <AA:TransactionContext>ILLCAL</AA:TransactionContext>


Optional.ofNullable(contributionHistory.getContributionDate())
			.ifPresent(content -> objContHistory.setRPInstalmentDate(content));
			
release/FT2-TomcatUpgrade -> Fixed 83 issues for cognitive comflexity


To test in local EBS-DTS-Service
----------------------------------------------------
DataTransformationProp.properties -> is.test=true
printRequest.setDocumentName(setDocName(applicationID));(PP_Master)
DataTransformationUtil.java -> System.getenv(XPRESSION_ADAPTER_URL)(https://cube-dev.sjp.co.uk/DIT/XpressionAdapter/processPrint)
http://localhost:8080/platformdtssoapapp/datatransfermation.wsdl
https://platformservicestest.sjp.co.uk/204/platform-dts-service/platformdtssoapapp/datatransfermation.wsdl


To decomplie the jar in eclipse 
Step1: jd-eclipse-2.0.0 need to download this zip
Step2: In eclipse go to new software install option and drag that zip here and install it.
Step3: Go to preferences and select the (File Associations) and *.class in down JD Class file viewer this set as default
Step4: Go to preferences and select the (File Associations) and *.class without source in down JD Class file viewer this set as default










