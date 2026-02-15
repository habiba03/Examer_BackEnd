package com.online_exam.examer.proctoring;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Service
public class FileStorageService {

    // المجلد الرئيسي لتخزين صور المراقبة
    private final String BASE_STORAGE = "storage/exams";

    public String saveBase64Image(Long examId, Long studentId, String base64Image) throws IOException {

        // 1. تنظيف بيانات الصورة (إزالة الـ Header الخاص بالـ Base64 إن وجد)
        String base64Data;
        if (base64Image.contains(",")) {
            base64Data = base64Image.substring(base64Image.indexOf(",") + 1);
        } else {
            base64Data = base64Image;
        }

        // 2. تحويل النص إلى بايتات
        byte[] decodedBytes = Base64.getDecoder().decode(base64Data);

        // 3. بناء المسار: storage/exams/{examId}/{studentId}
        // استخدام normalize لضمان توافق المسارات مع أنظمة التشغيل المختلفة
        Path directoryPath = Paths.get(BASE_STORAGE, examId.toString(), studentId.toString()).normalize();

        // 4. إنشاء المجلدات إذا لم تكن موجودة
        if (Files.notExists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }

        // 5. تسمية الملف بالوقت الحالي لضمان عدم التكرار
        String fileName = System.currentTimeMillis() + ".jpg";

        // 6. تحديد المسار النهائي للملف
        Path filePath = directoryPath.resolve(fileName);

        // 7. حفظ الملف على القرص الصلب
        Files.write(filePath, decodedBytes);

        // إرجاع المسار النسبي ليتم حفظه في قاعدة البيانات
        return filePath.toString();
    }
    /// /////////////////////////////////
    public Resource loadImageAsResource(String filePathString) {
        try {
            Path filePath = Paths.get(filePathString);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("الملف غير موجود في المسار: " + filePathString);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("خطأ في الوصول للمسار", ex);
        }
    }
}