package com.rite.products.convertrite.controller;

import com.rite.products.convertrite.service.CrLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/convertritecore")
@Slf4j
//@CrossOrigin
public class CrLogController {

    @Value("${logging.file.name}")
    private String coreLogFilePath;

    @Autowired
    CrLogService crLogService;

    @GetMapping("/logfile")
    @ResponseBody
    public void viewLogs(@RequestParam(required = false) String date, HttpServletResponse response) throws Exception {
        Path logPath;
        String logFileName;
        String filePath = coreLogFilePath;

        // Validate and sanitize the date parameter
        if (date != null) {
            if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                throw new IllegalArgumentException("Invalid date format. Expected format: yyyy-MM-dd");
            }
            logFileName = filePath + "." + date + ".0.gz";
        } else {
            logFileName = filePath;
        }

        // Resolve the path safely
        logPath = Paths.get(logFileName).toAbsolutePath().normalize();
        Path basePath = Paths.get(filePath).toAbsolutePath().normalize();

        //Prevent Path Traversal Attack
        if (!logPath.startsWith(basePath)) {
            throw new SecurityException("Access to the requested file is not allowed.");
        }

        log.info("Fetching log file from path: {}", logPath);

        if (!Files.exists(logPath) || !Files.isReadable(logPath)) {
            throw new IllegalArgumentException("Log file not found or inaccessible.");
        }

        //Process log file securely
        if (logFileName.endsWith(".gz")) {
            crLogService.convertDecompressedLogFileToString(logPath, response);
        } else {
            crLogService.convertLogFileToString(logPath, response);
        }
    }


}
