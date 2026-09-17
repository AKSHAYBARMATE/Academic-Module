package com.academic.controller;

import com.academic.dto.CollegeMarksheetResponse;
import com.academic.dto.GazetteUploadResponse;
import com.academic.response.StandardResponse;
import com.academic.service.CollegeMarksheetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/academic-module/college-marksheets")
@RequiredArgsConstructor
@Slf4j
public class CollegeMarksheetController {

    private final CollegeMarksheetService marksheetService;

    /**
     * 1. Download official Excel template for University Gazette upload (.xlsx)
     */
    @GetMapping("/download-template")
    public ResponseEntity<byte[]> downloadExcelTemplate() {
        log.info("API call: GET /college-marksheets/download-template");
        byte[] excelData = marksheetService.getExcelTemplate();

        String fileName = "University_Gazette_Upload_Template.xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelData);
    }

    /**
     * 2. Upload and validate University Result Gazette (.xlsx)
     */
    @PostMapping(value = "/upload-gazette", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StandardResponse<GazetteUploadResponse>> uploadGazette(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false, defaultValue = "B.Tech") String degreeCode,
            @RequestParam(required = false, defaultValue = "CSE") String programCode,
            @RequestParam(required = false, defaultValue = "Semester 5") String semester,
            @RequestParam(required = false, defaultValue = "2024-2025") String academicYear,
            @RequestParam(required = false, defaultValue = "Winter 2024") String examSession
    ) {
        log.info("API call: POST /college-marksheets/upload-gazette - file: {}, program: {}, session: {}",
                file != null ? file.getOriginalFilename() : "null", programCode, examSession);

        GazetteUploadResponse response = marksheetService.uploadAndIngestGazette(
                file, degreeCode, programCode, semester, academicYear, examSession
        );

        if (response.isSuccess()) {
            return ResponseEntity.ok(StandardResponse.success(response, response.getMessage()));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(StandardResponse.error(response.getMessage(), "VALIDATION_ERROR", response.getMessage()));
        }
    }

    /**
     * 3. Get all marksheets with multi-filter support & pagination
     */
    @GetMapping("/getAll")
    public ResponseEntity<StandardResponse<Page<CollegeMarksheetResponse>>> getAllMarksheets(
            @RequestParam(required = false) String degreeCode,
            @RequestParam(required = false) String programCode,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String examSession,
            @RequestParam(required = false) String resultStatus,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("API call: GET /college-marksheets/getAll - degree: {}, program: {}, sem: {}, search: {}",
                degreeCode, programCode, semester, search);

        Pageable pageable = PageRequest.of(page, size);
        Page<CollegeMarksheetResponse> responsePage = marksheetService.getAllMarksheets(
                degreeCode, programCode, semester, academicYear, examSession, resultStatus, search, pageable
        );

        return ResponseEntity.ok(StandardResponse.success(responsePage, "College marksheets fetched successfully"));
    }

    /**
     * 4. Get marksheet detail by ID
     */
    @GetMapping("/getById/{id}")
    public ResponseEntity<StandardResponse<CollegeMarksheetResponse>> getById(@PathVariable Long id) {
        log.info("API call: GET /college-marksheets/getById/{}", id);
        CollegeMarksheetResponse response = marksheetService.getById(id);
        return ResponseEntity.ok(StandardResponse.success(response, "College marksheet details fetched successfully"));
    }

    /**
     * 5. Toggle publish status for student portal
     */
    @PutMapping("/toggle-publish/{id}")
    public ResponseEntity<StandardResponse<Boolean>> togglePublish(
            @PathVariable Long id,
            @RequestParam(required = false) Boolean published
    ) {
        log.info("API call: PUT /college-marksheets/toggle-publish/{} - published: {}", id, published);
        boolean state = marksheetService.togglePublish(id, published);
        return ResponseEntity.ok(StandardResponse.success(state, "Marksheet publication status updated"));
    }

    /**
     * 6. Batch publish/unpublish selected marksheets
     */
    @PutMapping("/batch-publish")
    public ResponseEntity<StandardResponse<Integer>> batchPublish(
            @RequestBody List<Long> ids,
            @RequestParam Boolean published
    ) {
        log.info("API call: PUT /college-marksheets/batch-publish - count: {}, published: {}", ids != null ? ids.size() : 0, published);
        int updated = marksheetService.batchPublish(ids, published);
        return ResponseEntity.ok(StandardResponse.success(updated, "Batch publish operation completed"));
    }

    /**
     * 7. Soft delete marksheet
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<StandardResponse<Void>> deleteMarksheet(@PathVariable Long id) {
        log.info("API call: DELETE /college-marksheets/delete/{}", id);
        marksheetService.deleteMarksheet(id);
        return ResponseEntity.ok(StandardResponse.success("Marksheet deleted successfully"));
    }
}
