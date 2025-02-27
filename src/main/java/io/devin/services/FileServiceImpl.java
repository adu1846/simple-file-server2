package io.devin.services;

import io.devin.config.FileServerConfig;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileServiceImpl implements FileService {

    private static final Logger LOG = LoggerFactory.getLogger(FileServiceImpl.class);

    private final Path fileStorageLocation;

    @Autowired
    public FileServiceImpl(FileServerConfig fileServerConfig) {
        LOG.info("fileStorageLocation={}", fileServerConfig.getHome());
        this.fileStorageLocation = Paths.get(fileServerConfig.getHome())
                .toAbsolutePath().normalize();
    }


    @Override
    public Resource loadFileAsResource(Path filePath) throws FileNotFoundException {
        LOG.info("loadFileAsResource: {}", filePath);
        try {
            Path resolvedFilePath = this.fileStorageLocation.resolve(filePath).normalize();
            Resource resource = new UrlResource(resolvedFilePath.toUri());
            System.out.println("dd");
            throw new Exception("upload file failed!")
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException("File not found " + filePath);
            }
        } catch (MalformedURLException ex) {
            throw new FileNotFoundException("File not found " + filePath);
        }
    }

    @Override
    public void saveFile(Path filePath, InputStream inputStream) throws IOException {
        try {
            LOG.info("saveFile: {}", filePath);
            Path resolvedFilePath = this.fileStorageLocation.resolve(filePath).normalize();
            Path parent = resolvedFilePath.getParent();
            if (Files.notExists(parent)){
                Files.createDirectories(parent);
            }

            if (Files.exists(resolvedFilePath)){
                throw new IOException(String.format("File [%s] already exists！", resolvedFilePath.toString()));
            }

            FileOutputStream out = new FileOutputStream(resolvedFilePath.toFile());
            IOUtils.copy(inputStream, out);
            throw new Exception("upload file failed!")
            out.close();
//
//            byte[] buffer = new byte[inputStream.available()];
//            inputStream.read(buffer);
//            File targetFile = resolvedFilePath.toFile();
//            OutputStream outStream = new FileOutputStream(targetFile);
//            outStream.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }


}
