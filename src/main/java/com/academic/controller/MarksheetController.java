package com.academic.controller;


import com.academic.dto.MarksheetRequest;
import com.academic.exception.ResourceNotFoundException;
import com.academic.response.MarksheetDetailResponse;
import com.academic.response.StandardResponse;
import com.academic.service.MarksheetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/academic-module/marksheets")
@RequiredArgsConstructor
@Slf4j
public class MarksheetController {

    private final MarksheetService service;

    @PostMapping("/createMarksheet")
    public StandardResponse<?> create(@RequestBody MarksheetRequest request) {
        return service.saveMarksheet(request);
    }

    @PutMapping("/updateMarksheet/{id}")
    public StandardResponse<?> update(
            @PathVariable Long id,
            @RequestBody MarksheetRequest request
    ) {
        return service.update(id, request);
    }

    @GetMapping("/getAllMarksheet")
    public StandardResponse<?> getAll(
            @RequestParam(required = false) Integer classId,
            @RequestParam(required = false) Integer examTypeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return service.getAll(classId, examTypeId, page, size);
    }

    @GetMapping("getMarksheetById/{id}")
    public StandardResponse<?> getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @DeleteMapping("deleteMarksheet/{id}")
    public StandardResponse<?> delete(@PathVariable Long id) {
        return service.delete(id);
    }


    @GetMapping("/downloadMarksheet")
    public ResponseEntity<byte[]> downloadMarksheet(
            @RequestParam Long studentId,
            @RequestParam Integer sessionId,
            @RequestParam(required = false, defaultValue = "ANNUAL") String type
            ,@RequestParam Integer examTypeId
    ) {

        byte[] pdf = service
                .generateMarksheetPdf(studentId, sessionId, type,examTypeId);

        String fileName = "marksheet_" + type.toLowerCase() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + fileName)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

}
