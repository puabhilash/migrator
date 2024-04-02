/**
 * @author Pulluri.Abhilash
 * */
package com.tcs.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

/**
 * The Class AmazonServices.
 */
@Service
public class AmazonServices {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(AmazonServices.class);
	
	/** The bucket name. */
	@Value("${s3.bucketname}")
	private String bucketName;
	
	/** The access key. */
	@Value("${s3.accesskey}")
	private String accessKey;
	
	/** The secret key. */
	@Value("${s3.secretkey}")
	private String secretKey;
	
	/** The location. */
	@Value("${s3.location}")
	private String location;
	
	/**
	 * Gets the s 3 session.
	 *
	 * @return the s 3 session
	 */
	public AmazonS3 gets3Session() {
		AmazonS3 amazonS3 = null;
		try {
			final AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
			amazonS3=AmazonS3ClientBuilder
					.standard()
					.withRegion(location)
					.withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
					.withClientConfiguration(new ClientConfiguration().withConnectionTimeout(1000*1000).withMaxConnections(200).withSocketTimeout(1000*1000).withClientExecutionTimeout(1000*1000))
					.build();
		}catch (Exception exception) {
			LOGGER.error("exception occured while executing method gets3Session {}",exception);
		}
		return amazonS3;
	}
	
	/**
	 * Read S 3 object.
	 *
	 * @param amazonS3 the amazon S 3
	 * @param filePath the file path
	 * @return the s 3 object input stream
	 */
	public S3Object readS3Object(final AmazonS3 amazonS3,final String filePath) {
		S3Object object = null;
		try {
			object =amazonS3.getObject(new GetObjectRequest(bucketName, filePath));
//			LOGGER.info("object name {}",object.getKey());
//			LOGGER.info("mimetype {} size {}",object.getObjectMetadata().getContentType(),object.getObjectMetadata().getContentLength());
			
		}catch (AmazonS3Exception amazonS3Exception) {
			LOGGER.error("amazonS3Exception occured while executing method readS3Object {}",amazonS3Exception);
		}finally {
//				try {
//					if(null!=object) {
//						object.close();
//					}
//					if(null!=objectData) {
//						objectData.close();
//					}
//				} catch (IOException ioException) {
//					LOGGER.error("ioException occured while executing method readS3Object {}",ioException);
//				}
		}
		return object;
	}
}
