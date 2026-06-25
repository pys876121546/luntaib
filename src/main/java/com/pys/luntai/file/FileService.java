package com.pys.luntai.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class FileService {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Value("${file.server.url:http://localhost:8080}")
    private String serverUrl;

    /**
     * 上传文件
     */
    public String uploadFile(MultipartFile file, String type) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("只允许上传图片文件");
        }

        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String filename = UUID.randomUUID().toString().replace("-", "") + extension;

        // 按日期创建目录
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String relativePath = type + "/" + dateDir;
        Path targetDir = Paths.get(uploadPath, relativePath);

        // 创建目录
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        // 保存文件
        Path targetPath = targetDir.resolve(filename);
        Files.copy(file.getInputStream(), targetPath);

        // 返回访问URL
        return serverUrl + "/uploads/" + relativePath + "/" + filename;
    }

    /**
     * 删除文件
     */
    public void deleteFile(String fileUrl) {
        try {
            String relativePath = fileUrl.replace(serverUrl + "/uploads/", "");
            Path filePath = Paths.get(uploadPath, relativePath);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("删除文件失败", e);
        }
    }
}
