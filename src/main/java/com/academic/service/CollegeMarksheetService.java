package com.academic.service;

import com.academic.dto.CollegeMarksheetResponse;
import com.academic.dto.GazetteUploadResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CollegeMarksheetService {

    Page<CollegeMarksheetResponse> getAllMarksheets(
            String degreeCode,
            String programCode,
            String semester,
            String academicYear,
            String examSession,
            String resultStatus,
            String search,
            Pageable pageable
    );

    CollegeMarksheetResponse getById(Long id);

    GazetteUploadResponse uploadAndIngestGazette(
            MultipartFile file,
            String degreeCode,
            String programCode,
            String semester,
            String academicYear,
            String examSession
    );

    byte[] getExcelTemplate();

    boolean togglePublish(Long id, Boolean published);

    int batchPublish(List<Long> ids, Boolean published);

    void deleteMarksheet(Long id);

    int autoLinkUnlinkedMarksheets();
}
