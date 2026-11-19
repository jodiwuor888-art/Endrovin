package com.portfolio.service;

import com.portfolio.config.PortfolioProperties;
import com.portfolio.entity.UploadedFile;
import com.portfolio.exception.FileStorageException;
import com.portfolio.repository.UploadedFileRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootLocation;
    private final PortfolioProperties properties;
    private final UploadedFileRepository uploadedFileRepository;

    private static final List<String> DANGEROUS_EXTENSIONS = Arrays.asList(
            "exe", "bat", "cmd", "sh", "php", "jsp", "asp", "aspx", "cgi", "pl", "py", "jar", "war", "vbs", "ps1"
    );

    public FileStorageService(PortfolioProperties properties, UploadedFileRepository uploadedFileRepository) {
        this.properties = properties;
        this.uploadedFileRepository = uploadedFileRepository;
        this.rootLocation = Paths.get(properties.getUpload().getDir()).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
            Files.createDirectories(rootLocation.resolve("profiles"));
            Files.createDirectories(rootLocation.resolve("projects"));
            Files.createDirectories(rootLocation.resolve("certificates"));
            Files.createDirectories(rootLocation.resolve("cv"));
        } catch (IOException e) {
            throw new FileStorageException("Could not initialize upload directories", e);
        }
    }

    public UploadedFile storeFile(MultipartFile file, String category, String username) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Cannot upload empty file");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            originalFilename = "unknown";
        }
        originalFilename = StringUtils.cleanPath(originalFilename);

        // Security Check 1: Directory traversal protection
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw new FileStorageException("Filename contains invalid path sequence: " + originalFilename);
        }

        // Security Check 2: Extension verification
        String fileExtension = getFileExtension(originalFilename).toLowerCase();
        if (DANGEROUS_EXTENSIONS.contains(fileExtension)) {
            throw new FileStorageException("Executable or scripted file types are strictly prohibited: " + fileExtension);
        }

        // Security Check 3: Content-Type & Size validation based on category
        String contentType = file.getContentType();
        long size = file.getSize();

        validateCategoryConstraints(category, fileExtension, contentType, size, file);

        // Subfolder routing
        String subFolder = resolveSubfolder(category);
        String uniqueFileName = UUID.randomUUID().toString() + "_" + sanitizeFilename(originalFilename);

        try {
            Path targetLocation = this.rootLocation.resolve(subFolder).resolve(uniqueFileName);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            String publicUrl = "/uploads/" + subFolder + "/" + uniqueFileName;

            UploadedFile uploadedFile = new UploadedFile(
                    originalFilename,
                    uniqueFileName,
                    contentType != null ? contentType : "application/octet-stream",
                    category,
                    size,
                    fileExtension,
                    publicUrl,
                    username
            );

            return uploadedFileRepository.save(uploadedFile);

        } catch (IOException ex) {
            throw new FileStorageException("Failed to store file " + originalFilename, ex);
        }
    }

    private void validateCategoryConstraints(String category, String extension, String contentType, long size, MultipartFile file) {
        if ("CV".equalsIgnoreCase(category)) {
            if (size > properties.getUpload().getMaxDocumentBytes()) {
                throw new FileStorageException("CV document size exceeds limit of 10MB");
            }
            if (!"pdf".equalsIgnoreCase(extension)) {
                throw new FileStorageException("CV document must be in PDF format");
            }
            validatePdfMagicNumber(file);
        } else {
            // Images (PROFILE_PHOTO, PROJECT_IMAGE, CERTIFICATE)
            if (size > properties.getUpload().getMaxImageBytes()) {
                throw new FileStorageException("Image file size exceeds limit of 5MB");
            }
            List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "webp", "gif");
            if (!allowedExtensions.contains(extension)) {
                throw new FileStorageException("Only image files (JPG, PNG, WEBP, GIF) are allowed for " + category);
            }
            validateImageMagicNumber(file, extension);
        }
    }

    private void validateImageMagicNumber(MultipartFile file, String extension) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[12];
            int read = is.read(header);
            if (read < 4) {
                throw new FileStorageException("Corrupt image file");
            }

            // PNG signature: 89 50 4E 47
            if ("png".equalsIgnoreCase(extension)) {
                if (header[0] != (byte) 0x89 || header[1] != 0x50 || header[2] != 0x4E || header[3] != 0x47) {
                    throw new FileStorageException("Invalid PNG signature");
                }
            }
            // JPEG signature: FF D8 FF
            else if ("jpg".equalsIgnoreCase(extension) || "jpeg".equalsIgnoreCase(extension)) {
                if (header[0] != (byte) 0xFF || header[1] != (byte) 0xD8 || header[2] != (byte) 0xFF) {
                    throw new FileStorageException("Invalid JPEG signature");
                }
            }
            // GIF signature: 47 49 46 38
            else if ("gif".equalsIgnoreCase(extension)) {
                if (header[0] != 0x47 || header[1] != 0x49 || header[2] != 0x46 || header[3] != 0x38) {
                    throw new FileStorageException("Invalid GIF signature");
                }
            }
            // WebP signature: RIFF at 0-3 (52 49 46 46) and WEBP at 8-11 (57 45 42 50)
            else if ("webp".equalsIgnoreCase(extension)) {
                if (read < 12 || header[0] != 0x52 || header[1] != 0x49 || header[2] != 0x46 || header[3] != 0x46
                        || header[8] != 0x57 || header[9] != 0x45 || header[10] != 0x42 || header[11] != 0x50) {
                    throw new FileStorageException("Invalid WebP signature");
                }
            }
        } catch (IOException e) {
            throw new FileStorageException("Unable to verify file signature", e);
        }
    }

    private void validatePdfMagicNumber(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[4];
            int read = is.read(header);
            // PDF signature: %PDF (25 50 44 46)
            if (read < 4 || header[0] != 0x25 || header[1] != 0x50 || header[2] != 0x44 || header[3] != 0x46) {
                throw new FileStorageException("Invalid PDF document signature");
            }
        } catch (IOException e) {
            throw new FileStorageException("Unable to verify PDF document signature", e);
        }
    }

    private String resolveSubfolder(String category) {
        if ("PROFILE_PHOTO".equalsIgnoreCase(category)) return "profiles";
        if ("PROJECT_IMAGE".equalsIgnoreCase(category)) return "projects";
        if ("CERTIFICATE".equalsIgnoreCase(category)) return "certificates";
        if ("CV".equalsIgnoreCase(category)) return "cv";
        return "projects";
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }

    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
