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
import java.util.Arrays;

@Service
public class FileServiceImpl implements FileService {

    private static final Logger LOG = LoggerFactory.getLogger(FileServiceImpl.class);

    // ZIP file magic bytes signatures
    private static final byte[] ZIP_MAGIC = {0x50, 0x4B, 0x03, 0x04};        // Standard ZIP
    private static final byte[] ZIP_MAGIC_EMPTY = {0x50, 0x4B, 0x05, 0x06};  // Empty ZIP archive
    private static final byte[] ZIP_MAGIC_SPANNED = {0x50, 0x4B, 0x07, 0x08}; // Spanned ZIP archive

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
    public void saveFile(Path filePath, InputStream inputStream) throws IOException, OperationNotAllowedException {
        try {
            LOG.info("saveFile: {}", filePath);
            Path resolvedFilePath = this.fileStorageLocation.resolve(filePath).normalize();

            // Validate file extension - only allow .zip files
            String fileName = resolvedFilePath.getFileName().toString();
            if (!fileName.toLowerCase().endsWith(".zip")) {
                throw new OperationNotAllowedException("Only .zip files are allowed for upload");
            }

            // Validate file content magic bytes - must be a valid ZIP archive
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            bufferedInputStream.mark(4);
            byte[] header = new byte[4];
            int bytesRead = bufferedInputStream.read(header);
            bufferedInputStream.reset();

            if (bytesRead < 4) {
                throw new OperationNotAllowedException("File too small to be a valid ZIP archive");
            }
            if (!Arrays.equals(header, ZIP_MAGIC)
                    && !Arrays.equals(header, ZIP_MAGIC_EMPTY)
                    && !Arrays.equals(header, ZIP_MAGIC_SPANNED)) {
                throw new OperationNotAllowedException("File content is not a valid ZIP archive (invalid magic bytes)");
            }

            Path parent = resolvedFilePath.getParent();
            if (Files.notExists(parent)){
                Files.createDirectories(parent);
            }

            if (Files.exists(resolvedFilePath)){
                throw new IOException(String.format("File [%s] already exists！", resolvedFilePath.toString()));
            }

            FileOutputStream out = new FileOutputStream(resolvedFilePath.toFile());
            IOUtils.copy(bufferedInputStream, out);
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
