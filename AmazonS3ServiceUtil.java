package com.seec.insurance.life.illustrationinquiry.integration.swiftdb;

import java.io.ByteArrayInputStream;
import java.rmi.dgc.VMID;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.seec.insurance.common.response.object.ResultObject;
import com.seec.insurance.life.common.txutils.LifeTXUtils;
import com.seec.insurance.life.common.utils.BCSConstants;
import com.seec.insurance.life.common.utils.CastorLifeUtils;
import com.seec.insurance.life.illustrationinquiry.common.IllustrationInquiryTXUtil;
import com.seec.insurance.life.objects.OLifE;
import com.seec.insurance.life.objects.TXLife;
import com.seec.insurance.life.objects.TXLifeResponse;
import com.seec.insurance.life.objects.TransResult;
import com.seec.insurance.life.objects.TransType;
import com.seec.insurance.log.SEECLog;
import com.seec.insurance.log.SEECLogFactory;

public class AmazonS3ServiceUtil {
	
	private SEECLog log = SEECLogFactory.getLog(AmazonS3ServiceUtil.class);
	
	private static final String BUCKETNAME = System.getProperty("S3_BUCKET_NAME");
	
	private static final String FLAG = System.getProperty("ILLTOPUP_XML");
	
	private static final String CLIENTTYPE = "CLIENTTYPE";

	public void generteXmlAndUploadToS3(ResultObject resultObject, String action, HashMap<String, String> illuParamshashMap, String planNumber) {
		TransResult transResult = new TransResult();
		log.info("Illustration topup flag : "+ FLAG);
		try {
			if (resultObject != null && Boolean.parseBoolean(FLAG)) {
				TXLife myReturnTXLife = null;
				if (transResult.getResultCode() != null 
						&& BCSConstants.RESULT_SUCCESS.equalsIgnoreCase(transResult.getResultCode().getContent())) {
					log.info("TransResult Content: " + transResult.getResultCode().getContent());
					myReturnTXLife = IllustrationInquiryTXUtil.componseResponseTXLife(resultObject, null);
					
					TransType transType = new TransType();
					transType.setTc(204);
					transType.setContent("OLI_TRANS_INQPAR");
					
					TXLifeResponse txLifeResponse = LifeTXUtils.getTXLifeResponseFromTXLife(myReturnTXLife);
					txLifeResponse.setTransRefGUID( new VMID().toString() );
					txLifeResponse.setTransType(transType);
					
					String illResponseXml = CastorLifeUtils.marshallTxLife(myReturnTXLife);
					
					String key = prepareS3Key(action, illuParamshashMap, planNumber);
					if(!key.isEmpty()) {
						invokeS3Service(key, illResponseXml.getBytes());
					}
				} else {
					log.error("Exception occured while parsing acord response..!");
				}
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	public ResultObject getTopupXmlFromS3(HashMap<String, String> illuParamshashMap) {
		ResultObject resultObject = new ResultObject();
		TXLife txLife = null;
		log.info("Illustration topup flag : "+ FLAG);
		if(Boolean.parseBoolean(FLAG)) {
			String action = illuParamshashMap.get("ActionType");
			String key = prepareS3Key(action, illuParamshashMap, illuParamshashMap.get("PlanNumber"));
			if(!key.isEmpty()) {
				String responseXml = getXmlFromS3Bucket(key);
				try {
					txLife = CastorLifeUtils.unmarshallTxLife(responseXml);
				} catch (MarshalException | ValidationException e) {
					e.printStackTrace();
				}
				OLifE oLifE = LifeTXUtils.getOLife(txLife);
				TransResult transResult = LifeTXUtils.getTransResult(txLife);
				if(oLifE != null) {
//					resultObject = composeResultObject(oLifE);
				} else if(transResult != null ) {
					resultObject.setTransResult(transResult);
				}
			}
		}
		return resultObject;
	}
	
	private String getXmlFromS3Bucket(String partialKey) {
		AmazonS3 amazonS3 = getS3Client();
		
		ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request();
		listObjectsV2Request.setBucketName(BUCKETNAME);
		listObjectsV2Request.setPrefix(partialKey);
		
		ListObjectsV2Result listObjectsV2Result = amazonS3.listObjectsV2(listObjectsV2Request);
		
		List<S3ObjectSummary> objectSummaries = listObjectsV2Result.getObjectSummaries();
		
		return amazonS3.getObjectAsString(BUCKETNAME, objectSummaries.get(0).getKey());
	}
	
	private String prepareS3Key(String action,  HashMap<String, String> illuParamshashMap, String planNumber) {
		String key = "";
		String folderName = "";
		if(action.equalsIgnoreCase("ILLODSSEARCH")) {
			folderName = "ODSSearch/";
			if(illuParamshashMap.get(CLIENTTYPE).equalsIgnoreCase("INDIVIDUAL")) {
				String firstName = illuParamshashMap.get("FirstName");
				String lastName = illuParamshashMap.get("LastName");
				key = folderName+firstName+"_"+lastName.charAt(0);
			} else if(illuParamshashMap.get(CLIENTTYPE).equalsIgnoreCase("COMPANY") 
					|| illuParamshashMap.get(CLIENTTYPE).equalsIgnoreCase("TRUST")) {
				String companyName = illuParamshashMap.get("CompanyName");
				key = folderName+companyName;
			}
			
		} else if(action.equalsIgnoreCase("OLI_FEEDER_INQUIRY")) {
			folderName = "ODSSearch/";
			String fPlanNumber = illuParamshashMap.get("PlanNumber");
			key = folderName+"F_"+fPlanNumber;
			
		}else if(action.equalsIgnoreCase("ILLCOMPOUNDSEARCH")) {
			folderName = "AccountSearch/";
			key = folderName+planNumber;
			
		} else if(action.equalsIgnoreCase("ILLCOMPOUNDINQUIRY")) {
			folderName = "AccountInquiry/";
			key = folderName+planNumber;
			
		}
		return key;
	}
	
	private void invokeS3Service(String key, byte[] byteArr) {
		java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
		SimpleDateFormat sdFrmt = new SimpleDateFormat("ddMMyyyy_HHmmssSSS");
		String timeFormat = sdFrmt.format(date);
		
		key = key + "_" + timeFormat + ".xml";
		int startIndex = key.indexOf("/");
		try {
			log.debug("Started connecting S3 from illustration with Key: " + key.substring(startIndex+1));
			AmazonS3 s3Client = getS3Client();
			log.info("S3 connected successfully...!");
			PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKETNAME, key , new ByteArrayInputStream(byteArr), null);

			s3Client.putObject(putObjectRequest);
			log.info("Uploaded to S3 bucket with key: "+ key);
			
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	private AmazonS3 getS3Client() {
		return AmazonS3ClientBuilder.standard().withRegion(Regions.EU_WEST_1).build();
	}

}
