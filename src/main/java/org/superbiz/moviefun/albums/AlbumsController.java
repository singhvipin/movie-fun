package org.superbiz.moviefun.albums;

import org.apache.tika.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final AlbumsBean albumsBean;
    private final BlobStore blobStore;

    public AlbumsController(AlbumsBean albumsBean, BlobStore blobStore) {
        this.albumsBean = albumsBean;
        this.blobStore = blobStore;
    }


    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {
        if (uploadedFile.getSize() > 0) {
            Blob coverBlob = new Blob(
                    getCoverBlobName(albumId),
                    uploadedFile.getInputStream(),
                    uploadedFile.getContentType()
            );

            blobStore.put(coverBlob);
        }

        return format("redirect:/albums/%d", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {
        Optional<Blob> maybeCoverBlob = blobStore.get(getCoverBlobName(albumId));
        Blob coverBlob = maybeCoverBlob.orElseGet(this::buildDefaultCoverBlob);

        byte[] imageBytes = IOUtils.toByteArray(coverBlob.inputStream);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(coverBlob.contentType));
        headers.setContentLength(imageBytes.length);

        return new HttpEntity<>(imageBytes, headers);
    }

    @DeleteMapping("/covers")
    public String deleteCovers() {
        blobStore.deleteAll();
        return "redirect:/albums";
    }

    private Blob buildDefaultCoverBlob() {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream input = classLoader.getResourceAsStream("default-cover.jpg");

        return new Blob("default-cover", input, IMAGE_JPEG_VALUE);
    }

    private String getCoverBlobName(@PathVariable long albumId) {
        return format("covers/%d", albumId);
    }
}

///*package org.superbiz.moviefun.albums;
//
//import org.apache.tika.Tika;
//import org.apache.tika.io.IOUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import org.superbiz.moviefun.blobstore.Blob;
//import org.superbiz.moviefun.blobstore.BlobStore;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.URISyntaxException;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.Map;
//import java.util.Optional;
//
//import static java.lang.ClassLoader.getSystemResource;
//import static java.lang.String.format;
//import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
//
//@Controller
//@RequestMapping("/albums")
//public class AlbumsController {
//
//    private final AlbumsBean albumsBean;
//
//    @Autowired
//    private BlobStore blobStore;
//
//    public AlbumsController(AlbumsBean albumsBean) {
//        this.albumsBean = albumsBean;
//    }
//
//
//    @GetMapping
//    public String index(Map<String, Object> model) {
//        model.put("albums", albumsBean.getAlbums());
//        return "albums";
//    }
//
//    @GetMapping("/{albumId}")
//    public String details(@PathVariable long albumId, Map<String, Object> model) {
//        model.put("album", albumsBean.find(albumId));
//        return "albumDetails";
//    }
//
//    @PostMapping("/{albumId}/cover")
//    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {
//        saveUploadToFile(uploadedFile, getCoverFile(albumId));
//
//        return format("redirect:/albums/%d", albumId);
//    }
//
//    @GetMapping("/{albumId}/cover")
//    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {
//        Path coverFilePath = getExistingCoverPath(albumId);
//        ClassLoader classLoader = getClass().getClassLoader();
//        InputStream input = classLoader.getResourceAsStream("default-cover.jpg");
//
//        Optional<Blob> optionalBlob= blobStore.get(String.valueOf(albumId)) ;
//        Blob blob;
//
//        if (optionalBlob.isPresent()) {
//                blob= optionalBlob.get() ;
//        } else {
//            blob =  new Blob("default-cover", input, IMAGE_JPEG_VALUE);
//        }
//
//        // IOUtils.toByteArray( blob.inputStream) ;
//
//        byte[] imageBytes ;//= readAllBytes(coverFilePath);
//        imageBytes= IOUtils.toByteArray(blob.inputStream);
//        //HttpHeaders headers = createImageHttpHeaders(coverFilePath, imageBytes);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.parseMediaType(blob.contentType));
//        headers.setContentLength(imageBytes.length);
//
//        return new HttpEntity<>(imageBytes, headers);
//
//
//
//      /*  Optional<Blob> maybeCoverBlob = blobStore.get(getCoverBlobName(albumId));
//        Blob coverBlob = maybeCoverBlob.orElseGet(this::buildDefaultCoverBlob);
//
//        byte[] imageBytes = IOUtils.toByteArray(coverBlob.inputStream);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.parseMediaType(coverBlob.contentType));
//        headers.setContentLength(imageBytes.length);
//
//        return new HttpEntity<>(imageBytes, headers);
//        */
//
//    }
//    private HttpHeaders createImageHttpHeaders(Path coverFilePath, byte[] imageBytes) throws IOException {
//        String contentType = new Tika().detect(coverFilePath);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.parseMediaType(contentType));
//        headers.setContentLength(imageBytes.length);
//        return headers;
//    }
//
//    private File getCoverFile(@PathVariable long albumId) {
//        String coverFileName = format("%d", albumId);
//        return new File(coverFileName);
//    }
//
//    private void saveUploadToFile(@RequestParam("file") MultipartFile uploadedFile, File targetFile)
//            throws IOException {
//
//       /* targetFile.delete();
//        targetFile.getParentFile().mkdirs();
//        targetFile.createNewFile();
//
//        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
//            outputStream.write(uploadedFile.getBytes());
//        }
//        */
//        blobStore.put(new Blob(targetFile.getName(), uploadedFile.getInputStream(), "jpg"));
//    }
//
//    private Path getExistingCoverPath(@PathVariable long albumId) throws URISyntaxException {
//        File coverFile = getCoverFile(albumId);
//        Path coverFilePath = null ;
//
//
////        if (coverFile.exists()) {
////            coverFilePath = coverFile.toPath();
////        } else {
////            coverFilePath = (AlbumsController.class.getClassLoader().getResourceAsStream()
////                                .get(getSystemResource("default-cover.jpg").toURI());
////        }
//
//        return coverFilePath;
//    }
//}
//*/