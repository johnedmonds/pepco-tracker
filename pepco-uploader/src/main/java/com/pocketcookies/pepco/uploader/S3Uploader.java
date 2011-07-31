package com.pocketcookies.pepco.uploader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

public class S3Uploader {
	private final AmazonS3 s3Client;
	private final String bucket;
	private final String websiteRoot;

	public S3Uploader(AmazonS3 s3Client, String bucket, String websiteRoot) {
		this.s3Client = s3Client;
		this.bucket = bucket;
		this.websiteRoot = websiteRoot;
	}

	public void upload(String url) throws IOException {
		URL urlToDownload = new URL(websiteRoot + "/" + url);
		URLConnection conn = urlToDownload.openConnection();
		InputStream is = urlToDownload.openStream();

		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(conn.getContentLength());
		metadata.setContentType(conn.getContentType());
		PutObjectRequest req = new PutObjectRequest(bucket, url, is, metadata);
		req.setCannedAcl(CannedAccessControlList.PublicRead);
		s3Client.putObject(req);
	}

	public static void main(String[] args) throws IOException {
		String accessKeyID = args[0];
		String secretKeyID = args[1];
		String websiteRoot = args[2];

		S3Uploader s3Uploader = new S3Uploader(new AmazonS3Client(
				new BasicAWSCredentials(accessKeyID, secretKeyID)),
				"pepco-web", websiteRoot);
		s3Uploader.upload("summary-data");
		s3Uploader.upload("index.html");
	}
}
