package org.superbiz.moviefun.blobstore;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;
import org.apache.tika.Tika;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class S3Store implements BlobStore {

    private final AmazonS3 s3;
    private final String bucketName;
    private final Tika tika = new Tika();

    public S3Store(AmazonS3 s3, String bucketName) {
        this.s3 = s3;
        this.bucketName = bucketName;
    }


    @Override
    public void put(Blob blob) throws IOException {
        s3.putObject(bucketName, blob.name, blob.inputStream, new ObjectMetadata());
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        if (!s3.doesObjectExist(bucketName, name)) {
            return Optional.empty();
        }

        try (S3Object s3Object = s3.getObject(bucketName, name)) {
            S3ObjectInputStream content = s3Object.getObjectContent();

            byte[] bytes = IOUtils.toByteArray(content);

            return Optional.of(new Blob(
                    name,
                    new ByteArrayInputStream(bytes),
                    tika.detect(bytes)
            ));
        }
    }

    @Override
    public void deleteAll() {
        List<S3ObjectSummary> summaries = s3
                .listObjects(bucketName)
                .getObjectSummaries();

        for (S3ObjectSummary summary : summaries) {
            s3.deleteObject(bucketName, summary.getKey());
        }
    }
}

//package org.superbiz.moviefun.blobstore;
//
//import com.amazonaws.services.apigateway.model.Op;
//import com.amazonaws.services.s3.AmazonS3Client;
//import com.amazonaws.services.s3.model.*;
//import org.apache.tika.Tika;
//import org.apache.tika.io.IOUtils;
//
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.util.Iterator;
//import java.util.Optional;
//
//public class S3Store implements BlobStore {
//
//    private AmazonS3Client s3Client;
//    private String photoStorageBucket;
//
//    private final Tika tika = new Tika();
//
//    public S3Store (AmazonS3Client s3Client, String photoStorageBucket) {
//
//        this.s3Client=s3Client;
//        this.photoStorageBucket=photoStorageBucket;
//
//    }
//
//    @Override
//    public void put(Blob blob) throws IOException {
//                    s3Client.putObject(photoStorageBucket,blob.name, blob.inputStream, new ObjectMetadata());
//    }
//
//    @Override
//    public Optional<Blob> get(String name) throws IOException {
//
//        if (!s3Client.doesObjectExist(photoStorageBucket, name)) {
//            return Optional.empty();
//        }
//
//        try (S3Object s3Object = s3Client.getObject(photoStorageBucket, name)) {
//            S3ObjectInputStream content = s3Object.getObjectContent();
//
//            byte[] bytes = IOUtils.toByteArray(content);
//
//            return Optional.of(new Blob(
//                    name,
//                    new ByteArrayInputStream(bytes),
//                    tika.detect(bytes)
//            ));
//        }
//
//       /* S3Object object = s3Client.getObject(photoStorageBucket,name);
//        S3ObjectInputStream s3ObjectInputStream =  object.getObjectContent();
//
//        byte bytes[]= IOUtils.toByteArray(s3ObjectInputStream);
//
//
//        Blob blob = new Blob(name, new ByteArrayInputStream(bytes), object.getBucketName(),tika.detect(bytes) );
//        Optional<Blob> optionalBlob = Optional.of(blob);
//        return optionalBlob ;
//        //return Optional.empty();*/
//
//    }
//
//    @Override
//    public void deleteAll() {
//        ObjectListing objectListing = s3Client.listObjects(photoStorageBucket);
//        while (true) {
//            Iterator<S3ObjectSummary> objIter = objectListing.getObjectSummaries().iterator();
//            while (objIter.hasNext()) {
//                s3Client.deleteObject(photoStorageBucket, objIter.next().getKey());
//            }
//
//            // If the bucket contains many objects, the listObjects() call
//            // might not return all of the objects in the first listing. Check to
//            // see whether the listing was truncated. If so, retrieve the next page of objects
//            // and delete them.
//            if (objectListing.isTruncated()) {
//                objectListing = s3Client.listNextBatchOfObjects(objectListing);
//            } else {
//                break;
//            }
//        }
//
//    }
//}
