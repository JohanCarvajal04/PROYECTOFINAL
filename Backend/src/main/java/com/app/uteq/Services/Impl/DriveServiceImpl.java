package com.app.uteq.Services.Impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.app.uteq.Services.IDriveService;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class DriveServiceImpl implements IDriveService {

    private final Drive driveService;

    @Value("${google.drive.folder.id}")
    private String parentFolderId;

    @Override
    public String uploadFile(MultipartFile file) throws Exception {
        File fileMetadata = new File();
        fileMetadata.setName(UUID.randomUUID() + "_" + file.getOriginalFilename());
        fileMetadata.setParents(Collections.singletonList(parentFolderId));

        InputStreamContent mediaContent = new InputStreamContent(
                file.getContentType(),
                new ByteArrayInputStream(file.getBytes())
        );

        File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();

        return uploadedFile.getId(); // Este ID se guarda en AttachedDocuments.filePath
    }

    @Override
    public byte[] downloadFile(String fileId) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
        return outputStream.toByteArray();
    }

    @Override
    public void deleteFile(String fileId) throws Exception {
        driveService.files().delete(fileId).execute();
    }
}