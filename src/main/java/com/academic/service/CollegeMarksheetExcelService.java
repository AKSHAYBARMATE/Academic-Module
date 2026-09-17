package com.academic.service;

import com.academic.dto.ExcelValidationError;
import com.academic.dto.GazetteUploadResponse;
import com.academic.entity.CollegeMarksheet;
import com.academic.entity.CollegeMarksheetSubject;
import com.academic.entity.Student;
import com.academic.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollegeMarksheetExcelService {

    private final StudentRepository studentRepository;

    public static final String[] REQUIRED_HEADERS = {
            "UNIVERSITY_PRN",
            "ADMISSION_NO",
            "STUDENT_NAME",
            "SUBJECT_CODE",
            "SUBJECT_NAME",
            "SUBJECT_TYPE",
            "CREDITS",
            "INTERNAL_MAX",
            "INTERNAL_OBTAINED",
            "EXTERNAL_MAX",
            "EXTERNAL_OBTAINED",
            "GRADE_POINT",
            "LETTER_GRADE",
            "STATUS"
    };

    private static final Set<String> VALID_GRADES = Set.of("O", "A+", "A", "B+", "B", "C", "P", "F", "AB", "Ab", "ab");
    private static final Set<String> VALID_STATUSES = Set.of("PASS", "FAIL", "ABSENT", "EXEMPTED", "Pass", "Fail", "Absent");

    /**
     * Generates a professionally styled Excel Template for University Gazette Upload (.xlsx)
     */
    public byte[] generateExcelTemplate() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Sheet 1: Marksheet_Data
            Sheet dataSheet = workbook.createSheet("Marksheet_Data");

            // Cell Styles
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontName("Calibri");
            headerFont.setFontHeightInPoints((short) 11);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);

            CellStyle borderStyle = workbook.createCellStyle();
            borderStyle.setBorderBottom(BorderStyle.THIN);
            borderStyle.setBorderTop(BorderStyle.THIN);
            borderStyle.setBorderRight(BorderStyle.THIN);
            borderStyle.setBorderLeft(BorderStyle.THIN);

            // Create Header Row
            Row headerRow = dataSheet.createRow(0);
            headerRow.setHeightInPoints(24);
            for (int i = 0; i < REQUIRED_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(REQUIRED_HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // Sample Row 1: Student COL2026001 (Rahul Sharma) - Subject 1
            Row sample1 = dataSheet.createRow(1);
            sample1.createCell(0).setCellValue("22019481001");
            sample1.createCell(1).setCellValue("COL2026001");
            sample1.createCell(2).setCellValue("Rahul Sharma");
            sample1.createCell(3).setCellValue("CS501");
            sample1.createCell(4).setCellValue("Design & Analysis of Algorithms");
            sample1.createCell(5).setCellValue("Theory");
            sample1.createCell(6).setCellValue(4.0);
            sample1.createCell(7).setCellValue(30);
            sample1.createCell(8).setCellValue(28);
            sample1.createCell(9).setCellValue(70);
            sample1.createCell(10).setCellValue(64);
            sample1.createCell(11).setCellValue(10.0);
            sample1.createCell(12).setCellValue("O");
            sample1.createCell(13).setCellValue("Pass");

            // Sample Row 2: Student COL2026001 (Rahul Sharma) - Subject 2
            Row sample2 = dataSheet.createRow(2);
            sample2.createCell(0).setCellValue("22019481001");
            sample2.createCell(1).setCellValue("COL2026001");
            sample2.createCell(2).setCellValue("Rahul Sharma");
            sample2.createCell(3).setCellValue("CS502");
            sample2.createCell(4).setCellValue("Database Management Systems");
            sample2.createCell(5).setCellValue("Theory");
            sample2.createCell(6).setCellValue(4.0);
            sample2.createCell(7).setCellValue(30);
            sample2.createCell(8).setCellValue(27);
            sample2.createCell(9).setCellValue(70);
            sample2.createCell(10).setCellValue(61);
            sample2.createCell(11).setCellValue(9.0);
            sample2.createCell(12).setCellValue("A+");
            sample2.createCell(13).setCellValue("Pass");

            // Sample Row 3: Student 9293992 (Abhay Mohite) - Subject 1
            Row sample3 = dataSheet.createRow(3);
            sample3.createCell(0).setCellValue("22019481002");
            sample3.createCell(1).setCellValue("9293992");
            sample3.createCell(2).setCellValue("Abhay Mohite");
            sample3.createCell(3).setCellValue("CS501");
            sample3.createCell(4).setCellValue("Design & Analysis of Algorithms");
            sample3.createCell(5).setCellValue("Theory");
            sample3.createCell(6).setCellValue(4.0);
            sample3.createCell(7).setCellValue(30);
            sample3.createCell(8).setCellValue(25);
            sample3.createCell(9).setCellValue(70);
            sample3.createCell(10).setCellValue(59);
            sample3.createCell(11).setCellValue(9.0);
            sample3.createCell(12).setCellValue("A+");
            sample3.createCell(13).setCellValue("Pass");

            for (int i = 0; i < REQUIRED_HEADERS.length; i++) {
                sample1.getCell(i).setCellStyle(borderStyle);
                sample2.getCell(i).setCellStyle(borderStyle);
                sample3.getCell(i).setCellStyle(borderStyle);
                dataSheet.autoSizeColumn(i);
            }

            // Sheet 2: Instructions_And_Rules
            Sheet instructionsSheet = workbook.createSheet("Instructions_And_Rules");
            Row instTitle = instructionsSheet.createRow(0);
            instTitle.createCell(0).setCellValue("GUIDELINES & VALIDATION RULES FOR GAZETTE UPLOAD");

            String[] rules = {
                    "1. Each row corresponds to a single course result for a student.",
                    "2. Multiple rows sharing the same UNIVERSITY_PRN will be automatically grouped into one consolidated student marksheet.",
                    "3. Mandatory Fields: UNIVERSITY_PRN, ADMISSION_NO, STUDENT_NAME, SUBJECT_CODE, SUBJECT_NAME, CREDITS, INTERNAL_MAX, INTERNAL_OBTAINED, EXTERNAL_MAX, EXTERNAL_OBTAINED.",
                    "4. Student Verification: ADMISSION_NO must match an active student admission number in the College Student Directory (e.g. COL2026001, 9293992). The student's database ID (foreign key) is automatically linked to the marksheet.",
                    "5. Mark Validations: INTERNAL_OBTAINED must be >= 0 and <= INTERNAL_MAX. EXTERNAL_OBTAINED must be >= 0 and <= EXTERNAL_MAX.",
                    "6. Supported Letter Grades (CBCS 10-Point Scale): O (10), A+ (9), A (8), B+ (7), B (6), C (5), P (4), F (0), Ab (0).",
                    "7. Overall Result Status (Distinction, First Class, Second Class, ATKT, Fail) and SGPA/CGPA will be auto-calculated if left blank.",
                    "8. Do NOT alter or re-order column headers in the 'Marksheet_Data' sheet."
            };

            for (int i = 0; i < rules.length; i++) {
                Row r = instructionsSheet.createRow(i + 2);
                r.createCell(0).setCellValue(rules[i]);
            }
            instructionsSheet.autoSizeColumn(0);

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate Excel marksheet template", e);
            throw new RuntimeException("Could not generate Excel template: " + e.getMessage());
        }
    }

    /**
     * Parses and rigorously validates an uploaded University Gazette Excel (.xlsx) file
     */
    public ParsingResult parseAndValidateExcel(
            InputStream inputStream,
            String degreeCode,
            String programCode,
            String semester,
            String academicYear,
            String examSession
    ) {
        List<ExcelValidationError> errors = new ArrayList<>();
        Map<String, StudentAggregateData> studentMap = new LinkedHashMap<>();

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheet("Marksheet_Data");
            if (sheet == null) {
                sheet = workbook.getSheetAt(0); // fallback to primary sheet
            }

            if (sheet == null || sheet.getLastRowNum() < 1) {
                errors.add(ExcelValidationError.builder()
                        .rowNumber(1)
                        .columnName("FILE")
                        .invalidValue("Empty")
                        .errorMessage("The uploaded Excel sheet contains no data rows.")
                        .build());
                return new ParsingResult(Collections.emptyList(), errors, 0, 0);
            }

            // 1. Validate Header Row
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                errors.add(ExcelValidationError.builder()
                        .rowNumber(1)
                        .columnName("HEADER")
                        .invalidValue("Missing")
                        .errorMessage("Header row is missing.")
                        .build());
                return new ParsingResult(Collections.emptyList(), errors, 0, 0);
            }

            Map<String, Integer> headerIndexMap = new HashMap<>();
            for (Cell cell : headerRow) {
                String headerVal = getCellString(cell).trim().toUpperCase();
                headerIndexMap.put(headerVal, cell.getColumnIndex());
            }

            // Resolve Admission No / Roll No column (supports ADMISSION_NO, COLLEGE_ROLL_NO, ROLL_NO, etc.)
            Integer admissionCol = headerIndexMap.get("ADMISSION_NO");
            if (admissionCol == null) admissionCol = headerIndexMap.get("COLLEGE_ROLL_NO");
            if (admissionCol == null) admissionCol = headerIndexMap.get("ROLL_NO");
            if (admissionCol == null) admissionCol = headerIndexMap.get("ADMISSION_NUMBER");
            if (admissionCol == null) admissionCol = headerIndexMap.get("STUDENT_ADMISSION_NO");
            if (admissionCol == null) admissionCol = headerIndexMap.get("COLLEGE_ROLL_NO / ADMISSION_NO");
            if (admissionCol == null) {
                errors.add(ExcelValidationError.builder()
                        .rowNumber(1)
                        .columnName("ADMISSION_NO")
                        .invalidValue("Missing")
                        .errorMessage("Required header column 'ADMISSION_NO' (or 'COLLEGE_ROLL_NO') was not found.")
                        .build());
            }

            for (String requiredHeader : REQUIRED_HEADERS) {
                if ("ADMISSION_NO".equals(requiredHeader)) {
                    continue; // Checked above with aliases
                }
                if (!headerIndexMap.containsKey(requiredHeader)) {
                    errors.add(ExcelValidationError.builder()
                            .rowNumber(1)
                            .columnName(requiredHeader)
                            .invalidValue("Missing")
                            .errorMessage("Required header column '" + requiredHeader + "' was not found.")
                            .build());
                }
            }

            if (!errors.isEmpty() || admissionCol == null) {
                return new ParsingResult(Collections.emptyList(), errors, 0, 0);
            }

            // ── Step 2: Batch Pre-fetch Student Records from DB in a SINGLE Query ──
            Set<String> distinctAdmissionNos = new LinkedHashSet<>();
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowEmpty(row)) continue;
                String adm = getCellString(row.getCell(admissionCol)).trim();
                if (!adm.isEmpty()) {
                    distinctAdmissionNos.add(adm.toLowerCase());
                }
            }

            Map<String, Student> studentDbMap = new HashMap<>();
            if (!distinctAdmissionNos.isEmpty()) {
                List<Student> dbStudents = studentRepository.findByAdmissionNoInIgnoreCase(distinctAdmissionNos);
                for (Student s : dbStudents) {
                    if (s.getAdmissionNo() != null) {
                        studentDbMap.put(s.getAdmissionNo().trim().toLowerCase(), s);
                    }
                }
            }

            int totalRows = 0;
            int totalSubjects = 0;

            // 3. Row by Row Validation and Extraction
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }
                totalRows++;
                int displayRowNum = r + 1; // 1-based display row

                String prn = getCellString(row.getCell(headerIndexMap.get("UNIVERSITY_PRN"))).trim();
                String admissionNo = getCellString(row.getCell(admissionCol)).trim();
                String studentName = getCellString(row.getCell(headerIndexMap.get("STUDENT_NAME"))).trim();
                String subCode = getCellString(row.getCell(headerIndexMap.get("SUBJECT_CODE"))).trim();
                String subName = getCellString(row.getCell(headerIndexMap.get("SUBJECT_NAME"))).trim();
                String subType = getCellString(row.getCell(headerIndexMap.get("SUBJECT_TYPE"))).trim();

                // Non-empty validations
                if (prn.isEmpty()) {
                    errors.add(new ExcelValidationError(displayRowNum, "UNIVERSITY_PRN", prn, "University PRN cannot be empty"));
                }
                if (admissionNo.isEmpty()) {
                    errors.add(new ExcelValidationError(displayRowNum, "ADMISSION_NO", admissionNo, "Admission No cannot be empty"));
                } else if (!studentDbMap.containsKey(admissionNo.toLowerCase())) {
                    errors.add(new ExcelValidationError(
                            displayRowNum,
                            "ADMISSION_NO",
                            admissionNo,
                            "Student with Admission No '" + admissionNo + "' is not present in the database. Marksheet cannot be processed."
                    ));
                }
                if (studentName.isEmpty()) {
                    errors.add(new ExcelValidationError(displayRowNum, "STUDENT_NAME", studentName, "Student Name cannot be empty"));
                }
                if (subCode.isEmpty()) {
                    errors.add(new ExcelValidationError(displayRowNum, "SUBJECT_CODE", subCode, "Subject Code cannot be empty"));
                }
                if (subName.isEmpty()) {
                    errors.add(new ExcelValidationError(displayRowNum, "SUBJECT_NAME", subName, "Subject Name cannot be empty"));
                }

                // Credits validation
                Double credits = getCellDouble(row.getCell(headerIndexMap.get("CREDITS")));
                if (credits == null || credits <= 0) {
                    errors.add(new ExcelValidationError(displayRowNum, "CREDITS", String.valueOf(credits), "Credits must be a positive number"));
                    credits = 3.0;
                }

                // Marks Validation
                Integer internalMax = getCellInteger(row.getCell(headerIndexMap.get("INTERNAL_MAX")));
                Integer internalObt = getCellInteger(row.getCell(headerIndexMap.get("INTERNAL_OBTAINED")));
                Integer externalMax = getCellInteger(row.getCell(headerIndexMap.get("EXTERNAL_MAX")));
                Integer externalObt = getCellInteger(row.getCell(headerIndexMap.get("EXTERNAL_OBTAINED")));

                if (internalMax == null || internalMax < 0) {
                    errors.add(new ExcelValidationError(displayRowNum, "INTERNAL_MAX", String.valueOf(internalMax), "Internal Max Marks must be a non-negative integer"));
                    internalMax = 30;
                }
                if (internalObt == null || internalObt < 0) {
                    errors.add(new ExcelValidationError(displayRowNum, "INTERNAL_OBTAINED", String.valueOf(internalObt), "Internal Obtained Marks must be a non-negative integer"));
                    internalObt = 0;
                } else if (internalObt > internalMax) {
                    errors.add(new ExcelValidationError(displayRowNum, "INTERNAL_OBTAINED", String.valueOf(internalObt),
                            "Internal Obtained (" + internalObt + ") cannot exceed Internal Max (" + internalMax + ")"));
                }

                if (externalMax == null || externalMax < 0) {
                    errors.add(new ExcelValidationError(displayRowNum, "EXTERNAL_MAX", String.valueOf(externalMax), "External Max Marks must be a non-negative integer"));
                    externalMax = 70;
                }
                if (externalObt == null || externalObt < 0) {
                    errors.add(new ExcelValidationError(displayRowNum, "EXTERNAL_OBTAINED", String.valueOf(externalObt), "External Obtained Marks must be a non-negative integer"));
                    externalObt = 0;
                } else if (externalObt > externalMax) {
                    errors.add(new ExcelValidationError(displayRowNum, "EXTERNAL_OBTAINED", String.valueOf(externalObt),
                            "External Obtained (" + externalObt + ") cannot exceed External Max (" + externalMax + ")"));
                }

                // Grade & Status Validation
                String letterGrade = getCellString(row.getCell(headerIndexMap.get("LETTER_GRADE"))).trim().toUpperCase();
                Double gradePoint = getCellDouble(row.getCell(headerIndexMap.get("GRADE_POINT")));
                String status = getCellString(row.getCell(headerIndexMap.get("STATUS"))).trim();

                int totalMarks = (internalObt != null ? internalObt : 0) + (externalObt != null ? externalObt : 0);
                int maxTotalMarks = (internalMax != null ? internalMax : 0) + (externalMax != null ? externalMax : 0);

                if (letterGrade.isEmpty() || !VALID_GRADES.contains(letterGrade)) {
                    // Auto-compute letter grade and grade point from marks
                    double pct = maxTotalMarks > 0 ? (totalMarks * 100.0 / maxTotalMarks) : 0;
                    letterGrade = computeLetterGrade(pct);
                    gradePoint = computeGradePoint(letterGrade);
                } else if (gradePoint == null) {
                    gradePoint = computeGradePoint(letterGrade);
                }

                if (status.isEmpty() || !VALID_STATUSES.contains(status)) {
                    status = "F".equalsIgnoreCase(letterGrade) || "AB".equalsIgnoreCase(letterGrade) ? "Fail" : "Pass";
                }

                if (errors.isEmpty() && !prn.isEmpty()) {
                    Student matchedStudent = studentDbMap.get(admissionNo.toLowerCase());
                    Long matchedStudentId = (matchedStudent != null && matchedStudent.getId() != null)
                            ? matchedStudent.getId().longValue() : null;
                    String finalFatherName = matchedStudent != null ? matchedStudent.getFatherName() : null;
                    String finalMotherName = matchedStudent != null ? matchedStudent.getMotherName() : null;

                    // Group under Student PRN
                    StudentAggregateData studentData = studentMap.computeIfAbsent(prn, k -> new StudentAggregateData(
                            matchedStudentId, admissionNo, prn, admissionNo, studentName,
                            finalFatherName, finalMotherName,
                            degreeCode, programCode, semester, academicYear, examSession
                    ));

                    CollegeMarksheetSubject sub = CollegeMarksheetSubject.builder()
                            .subjectCode(subCode)
                            .subjectName(subName)
                            .subjectType(subType.isEmpty() ? "Theory" : subType)
                            .credits(credits)
                            .internalMaxMarks(internalMax)
                            .internalObtainedMarks(internalObt)
                            .externalMaxMarks(externalMax)
                            .externalObtainedMarks(externalObt)
                            .totalMarks(totalMarks)
                            .maxTotalMarks(maxTotalMarks)
                            .gradePoint(gradePoint)
                            .letterGrade(letterGrade)
                            .status(status)
                            .isBacklog("Fail".equalsIgnoreCase(status) || "Absent".equalsIgnoreCase(status))
                            .attemptNumber(1)
                            .build();

                    studentData.subjects.add(sub);
                    totalSubjects++;
                }
            }

            if (!errors.isEmpty()) {
                return new ParsingResult(Collections.emptyList(), errors, totalRows, 0);
            }

            // Build CollegeMarksheet Entities with Aggregations
            List<CollegeMarksheet> marksheetEntities = new ArrayList<>();
            int linkedCount = 0;
            int unlinkedCount = 0;
            for (StudentAggregateData data : studentMap.values()) {
                if (data.studentId != null) {
                    linkedCount++;
                } else {
                    unlinkedCount++;
                }
                CollegeMarksheet marksheet = buildMarksheetEntity(data);
                marksheetEntities.add(marksheet);
            }

            return new ParsingResult(marksheetEntities, errors, totalRows, totalSubjects, linkedCount, unlinkedCount);
        } catch (Exception e) {
            log.error("Failed to parse Gazette Excel", e);
            errors.add(ExcelValidationError.builder()
                    .rowNumber(0)
                    .columnName("FILE")
                    .invalidValue("Corrupt/Unreadable")
                    .errorMessage("Could not read Excel file: " + e.getMessage())
                    .build());
            return new ParsingResult(Collections.emptyList(), errors, 0, 0);
        }
    }

    private CollegeMarksheet buildMarksheetEntity(StudentAggregateData data) {
        double totalCreditsOffered = 0;
        double totalCreditsEarned = 0;
        double weightedPointsSum = 0;
        double totalMarksObt = 0;
        double totalMarksMax = 0;
        int backlogCount = 0;

        for (CollegeMarksheetSubject sub : data.subjects) {
            totalMarksObt += sub.getTotalMarks();
            totalMarksMax += sub.getMaxTotalMarks();

            if (!"Audit".equalsIgnoreCase(sub.getSubjectType())) {
                totalCreditsOffered += sub.getCredits();
                if (!"Fail".equalsIgnoreCase(sub.getStatus()) && !"Absent".equalsIgnoreCase(sub.getStatus())) {
                    totalCreditsEarned += sub.getCredits();
                }
                weightedPointsSum += (sub.getGradePoint() * sub.getCredits());
            }

            if (sub.getIsBacklog()) {
                backlogCount++;
            }
        }

        double sgpa = totalCreditsOffered > 0 ? Math.round((weightedPointsSum / totalCreditsOffered) * 100.0) / 100.0 : 0.0;
        double cgpa = sgpa; // initialized to current SGPA for fresh upload
        double percentage = totalMarksMax > 0 ? Math.round((totalMarksObt / totalMarksMax * 100.0) * 100.0) / 100.0 : 0.0;

        String resultStatus;
        if (backlogCount > 0) {
            resultStatus = "ATKT";
        } else if (sgpa >= 8.5) {
            resultStatus = "FIRST_CLASS_DISTINCTION";
        } else if (sgpa >= 6.5) {
            resultStatus = "FIRST_CLASS";
        } else if (sgpa >= 5.0) {
            resultStatus = "SECOND_CLASS";
        } else {
            resultStatus = "PASS";
        }

        CollegeMarksheet ms = CollegeMarksheet.builder()
                .studentId(data.studentId)
                .admissionNo(data.admissionNo)
                .studentName(data.studentName)
                .fatherName(data.fatherName)
                .motherName(data.motherName)
                .universityPrn(data.universityPrn)
                .collegeRollNo(data.collegeRollNo != null ? data.collegeRollNo : data.admissionNo)
                .degreeCode(data.degreeCode)
                .programCode(data.programCode)
                .programName(data.programCode)
                .semester(data.semester)
                .academicYear(data.academicYear)
                .examSession(data.examSession)
                .examType("REGULAR")
                .totalCreditsOffered(totalCreditsOffered)
                .totalCreditsEarned(totalCreditsEarned)
                .sgpa(sgpa)
                .cgpa(cgpa)
                .totalMarksObtained(totalMarksObt)
                .totalMaxMarks(totalMarksMax)
                .percentage(percentage)
                .resultStatus(resultStatus)
                .backlogCount(backlogCount)
                .marksheetNumber("MS-" + (data.examSession != null ? data.examSession.replaceAll("\\s+", "") : "SEM") + "-" + data.universityPrn)
                .issueDate(LocalDate.now())
                .verificationCode("VER-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .published(true)
                .subjects(new ArrayList<>())
                .build();

        for (CollegeMarksheetSubject sub : data.subjects) {
            sub.setCollegeMarksheet(ms);
            ms.getSubjects().add(sub);
        }

        return ms;
    }

    private String computeLetterGrade(double percentage) {
        if (percentage >= 90) return "O";
        if (percentage >= 80) return "A+";
        if (percentage >= 70) return "A";
        if (percentage >= 60) return "B+";
        if (percentage >= 50) return "B";
        if (percentage >= 45) return "C";
        if (percentage >= 40) return "P";
        return "F";
    }

    private double computeGradePoint(String letterGrade) {
        switch (letterGrade.toUpperCase()) {
            case "O": return 10.0;
            case "A+": return 9.0;
            case "A": return 8.0;
            case "B+": return 7.0;
            case "B": return 6.0;
            case "C": return 5.0;
            case "P": return 4.0;
            default: return 0.0;
        }
    }

    private String getCellString(Cell cell) {
        if (cell == null) return "";
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell);
    }

    private Double getCellDouble(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }
        try {
            String str = getCellString(cell).trim();
            return str.isEmpty() ? null : Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer getCellInteger(Cell cell) {
        Double d = getCellDouble(cell);
        return d != null ? d.intValue() : null;
    }

    private boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK && !getCellString(cell).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static class ParsingResult {
        public final List<CollegeMarksheet> marksheets;
        public final List<ExcelValidationError> errors;
        public final int totalRows;
        public final int totalSubjects;
        public final int linkedCount;
        public final int unlinkedCount;

        public ParsingResult(List<CollegeMarksheet> marksheets, List<ExcelValidationError> errors, int totalRows, int totalSubjects, int linkedCount, int unlinkedCount) {
            this.marksheets = marksheets;
            this.errors = errors;
            this.totalRows = totalRows;
            this.totalSubjects = totalSubjects;
            this.linkedCount = linkedCount;
            this.unlinkedCount = unlinkedCount;
        }

        public ParsingResult(List<CollegeMarksheet> marksheets, List<ExcelValidationError> errors, int totalRows, int totalSubjects) {
            this(marksheets, errors, totalRows, totalSubjects, 0, 0);
        }
    }

    private static class StudentAggregateData {
        final Long studentId;
        final String admissionNo;
        final String universityPrn;
        final String collegeRollNo;
        final String studentName;
        final String fatherName;
        final String motherName;
        final String degreeCode;
        final String programCode;
        final String semester;
        final String academicYear;
        final String examSession;
        final List<CollegeMarksheetSubject> subjects = new ArrayList<>();

        StudentAggregateData(Long studentId, String admissionNo, String universityPrn, String collegeRollNo,
                             String studentName, String fatherName, String motherName,
                             String degreeCode, String programCode, String semester,
                             String academicYear, String examSession) {
            this.studentId = studentId;
            this.admissionNo = admissionNo;
            this.universityPrn = universityPrn;
            this.collegeRollNo = collegeRollNo;
            this.studentName = studentName;
            this.fatherName = fatherName;
            this.motherName = motherName;
            this.degreeCode = degreeCode;
            this.programCode = programCode;
            this.semester = semester;
            this.academicYear = academicYear;
            this.examSession = examSession;
        }
    }
}
