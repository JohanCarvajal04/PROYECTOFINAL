package com.app.uteq.Services;

import org.springframework.web.multipart.MultipartFile;

public interface IDriveService {
    /**
     * Sube un archivo a Google Drive y retorna el fileId.
     */
    String uploadFile(MultipartFile file) throws Exception;

    /**
     * Descarga un archivo de Google Drive por su fileId.
     */
    byte[] downloadFile(String fileId) throws Exception;

    /**
     * Elimina un archivo de Google Drive por su fileId.
     */
    void deleteFile(String fileId) throws Exception;
}
