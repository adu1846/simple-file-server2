package io.devin.controler;

import io.devin.services.FileService;
import io.devin.services.OperationNotAllowedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class FileServerController {

    private static final Logger LOG = LoggerFactory.getLogger(FileServerController.class);

    public static final String DOWNLOAD_PREFIX = "/";
    public static final String UPLOAD_PREFIX = "/upload2/";

    private final FileService fileService;
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public FileServerController(FileService fileService,
                                HttpServletRequest httpServletRequest) {
        this.fileService = fileService;
        this.httpServletRequest = httpServletRequest;
    }

    @GetMapping(DOWNLOAD_PREFIX + "**")
    public ResponseEntity<Resource> downloadFile() {
        try {
            String contextPath = httpServletRequest.getRequestURI();
            try {
                contextPath = URLDecoder.decode(contextPath,"gbk");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            Path filePath = Paths.get(contextPath.substring(DOWNLOAD_PREFIX.length()));
            LOG.info("downloadFile: {}", filePath);
            Resource resource = fileService.loadFileAsResource(filePath);
            if(resource==null){
                resource = fileService.loadFileAsResource(filePath+".txt");
            }
            String contentType = "application/octet-stream";
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (OperationNotAllowedException e) {
            throw new RuntimeException(e);
        }
    }


    @PostMapping(UPLOAD_PREFIX + "**")
    public ResponseEntity<Resource> fileUpload(@RequestParam("file") MultipartFile file) {
        try {
            String contextPath = httpServletRequest.getRequestURI();
            try {
                contextPath = URLDecoder.decode(contextPath,"utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            Path filePath = Paths.get(contextPath.substring(UPLOAD_PREFIX.length()));
            LOG.info("upload: {}", filePath);
            fileService.saveFile(filePath, file.getInputStream());
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (OperationNotAllowedException e) {
            throw new RuntimeException(e);
        }
    }


//    @PostMapping(CREATEDIR_PREFIX + "**")
//    public ResponseEntity<Resource> createDirectory() {
//        try {
//            String contextPath = httpServletRequest.getRequestURI();
//            SessionId sessionId = new SessionId(httpServletRequest.getSession().getId());
//            Optional<UserData> userData = securityService.isAuthorized(sessionId);
//            if (userData.isPresent()) {
//                Path filePath = Paths.get(contextPath.substring((URI_PREFIX + CREATEDIR_PREFIX).length()));
//                LOG.info("createDirectory: {}", filePath);
//                fileService.createDirectory(userData.get(), filePath);
//                return ResponseEntity.ok().build();
//            }
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//        } catch (IOException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        } catch (OperationNotAllowedException e) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//        }
//    }

}
