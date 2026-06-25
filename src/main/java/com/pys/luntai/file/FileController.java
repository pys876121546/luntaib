package com.pys.luntai.file;

import com.pys.luntai.common.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @Autowired
    private FileService fileService;

    /**
     * 上传图片
     * @param file 图片文件
     * @param type 文件类型目录，如：company(公司照片)、avatar(头像)等
     */
    @PostMapping("/upload")
    public Result<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "common") String type) {
        try {
            String fileUrl = fileService.uploadFile(file, type);
            return Result.success(fileUrl);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 上传公司门头照片
     */
    @PostMapping("/upload/company-photo")
    public Result<String> uploadCompanyPhoto(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = fileService.uploadFile(file, "company");
            return Result.success(fileUrl);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}