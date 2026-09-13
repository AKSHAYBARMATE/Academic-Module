package com.academic.service;

import com.academic.entity.CollegeSubject;
import com.academic.entity.CollegeTimetable;
import com.academic.entity.Degree;
import com.academic.entity.Staff;
import com.academic.exception.CustomException;
import com.academic.exception.ResourceNotFoundException;
import com.academic.mapper.CollegeTimetableMapper;
import com.academic.repository.CollegeSubjectRepository;
import com.academic.repository.CollegeTimetableRepository;
import com.academic.repository.DegreeRepository;
import com.academic.repository.StaffRepository;
import com.academic.request.CollegeTimetableRequest;
import com.academic.response.CollegeTimetableResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollegeTimetableServiceImpl implements CollegeTimetableService {

    private final CollegeTimetableRepository repository;
    private final CollegeSubjectRepository subjectRepository;
    private final StaffRepository staffRepository;
    private final DegreeRepository degreeRepository;

    @Override
    @Transactional
    public CollegeTimetableResponse createSlot(CollegeTimetableRequest request) {
        log.info("Creating College Timetable slot for: {} - {} - {} on {} {}",
                request.getProgram(), request.getSemester(), request.getSection(), request.getDay(), request.getTimeSlot());

        validateBasicRequest(request);

        // Conflict Checks
        validateConflicts(request, null);

        // Resolve Entities
        CollegeSubject subject = resolveSubject(request);
        Staff staff = resolveStaff(request);
        Degree degree = resolveDegree(request);

        CollegeTimetable entity = CollegeTimetableMapper.toEntity(request, degree, subject, staff);
        CollegeTimetable saved = repository.save(entity);
        log.info("College Timetable slot created successfully with id: {}", saved.getId());

        return CollegeTimetableMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CollegeTimetableResponse updateSlot(Long id, CollegeTimetableRequest request) {
        log.info("Updating College Timetable slot id: {}", id);

        if (id == null || id <= 0) {
            throw new CustomException("Invalid slot ID", "INVALID_ID", "ID must be a positive number");
        }

        CollegeTimetable existing = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable slot not found with id: " + id));

        validateBasicRequest(request);

        // Conflict Checks excluding current slot ID
        validateConflicts(request, id);

        // Resolve Entities
        CollegeSubject subject = resolveSubject(request);
        Staff staff = resolveStaff(request);
        Degree degree = resolveDegree(request);

        CollegeTimetableMapper.updateEntity(existing, request, degree, subject, staff);
        CollegeTimetable updated = repository.save(existing);
        log.info("College Timetable slot updated successfully with id: {}", updated.getId());

        return CollegeTimetableMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteSlot(Long id) {
        log.info("Soft-deleting College Timetable slot id: {}", id);

        if (id == null || id <= 0) {
            throw new CustomException("Invalid slot ID", "INVALID_ID", "ID must be a positive number");
        }

        CollegeTimetable existing = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable slot not found with id: " + id));

        existing.setIsDeleted(true);
        repository.save(existing);
        log.info("College Timetable slot soft-deleted with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public CollegeTimetableResponse getSlotById(Long id) {
        log.info("Fetching College Timetable slot id: {}", id);

        if (id == null || id <= 0) {
            throw new CustomException("Invalid slot ID", "INVALID_ID", "ID must be a positive number");
        }

        CollegeTimetable entity = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable slot not found with id: " + id));

        return CollegeTimetableMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollegeTimetableResponse> getClassSchedule(String program, String semester, String section, String academicYear) {
        log.info("Fetching class schedule for program: {}, sem: {}, sec: {}, year: {}", program, semester, section, academicYear);

        if (program == null || program.isBlank()) {
            throw new CustomException("Program is required", "REQUIRED_FIELD", "program cannot be empty");
        }
        if (semester == null || semester.isBlank()) {
            throw new CustomException("Semester is required", "REQUIRED_FIELD", "semester cannot be empty");
        }
        if (section == null || section.isBlank()) {
            throw new CustomException("Section is required", "REQUIRED_FIELD", "section cannot be empty");
        }

        List<CollegeTimetable> slots;
        if (academicYear != null && !academicYear.isBlank() && !academicYear.equalsIgnoreCase("all")) {
            slots = repository.findByProgramIgnoreCaseAndSemesterIgnoreCaseAndSectionIgnoreCaseAndAcademicYearIgnoreCaseAndIsDeletedFalse(
                    program.trim(), semester.trim(), section.trim(), academicYear.trim()
            );
        } else {
            slots = repository.findByProgramIgnoreCaseAndSemesterIgnoreCaseAndSectionIgnoreCaseAndIsDeletedFalse(
                    program.trim(), semester.trim(), section.trim()
            );
        }

        return CollegeTimetableMapper.toResponseList(slots);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollegeTimetableResponse> getTeacherSchedule(Integer staffId, String academicYear) {
        log.info("Fetching teacher schedule for staffId: {}, year: {}", staffId, academicYear);

        if (staffId == null || staffId <= 0) {
            throw new CustomException("Staff ID is required", "REQUIRED_FIELD", "staffId must be positive");
        }

        List<CollegeTimetable> slots;
        if (academicYear != null && !academicYear.isBlank() && !academicYear.equalsIgnoreCase("all")) {
            slots = repository.findByStaff_IdAndAcademicYearIgnoreCaseAndIsDeletedFalse(staffId, academicYear.trim());
        } else {
            slots = repository.findByStaff_IdAndIsDeletedFalse(staffId);
        }

        return CollegeTimetableMapper.toResponseList(slots);
    }

    @Override
    @Transactional
    public List<CollegeTimetableResponse> bulkSaveSchedule(List<CollegeTimetableRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return new ArrayList<>();
        }

        log.info("Bulk saving {} college timetable slots", requests.size());
        List<CollegeTimetable> toSave = new ArrayList<>();

        for (CollegeTimetableRequest req : requests) {
            validateBasicRequest(req);
            validateConflicts(req, null);

            CollegeSubject subject = resolveSubject(req);
            Staff staff = resolveStaff(req);
            Degree degree = resolveDegree(req);

            toSave.add(CollegeTimetableMapper.toEntity(req, degree, subject, staff));
        }

        List<CollegeTimetable> saved = repository.saveAll(toSave);
        return CollegeTimetableMapper.toResponseList(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CollegeTimetableResponse> getAllSlots(
            int page, int size, String search, String program, String semester,
            String section, String day, String academicYear, Integer staffId,
            Long subjectId, String type, String status
    ) {
        if (page < 0) page = 0;
        if (size <= 0 || size > 200) size = 20;

        Pageable pageable = PageRequest.of(page, size);

        String cleanSearch = (search != null && !search.trim().isBlank()) ? search.trim() : null;
        String cleanProg = (program != null && !program.trim().isBlank() && !program.equalsIgnoreCase("all")) ? program.trim() : null;
        String cleanSem = (semester != null && !semester.trim().isBlank() && !semester.equalsIgnoreCase("all")) ? semester.trim() : null;
        String cleanSec = (section != null && !section.trim().isBlank() && !section.equalsIgnoreCase("all")) ? section.trim() : null;
        String cleanDay = (day != null && !day.trim().isBlank() && !day.equalsIgnoreCase("all")) ? day.trim() : null;
        String cleanYear = (academicYear != null && !academicYear.trim().isBlank() && !academicYear.equalsIgnoreCase("all")) ? academicYear.trim() : null;
        String cleanType = (type != null && !type.trim().isBlank() && !type.equalsIgnoreCase("all")) ? type.trim() : null;
        String cleanStatus = (status != null && !status.trim().isBlank() && !status.equalsIgnoreCase("all")) ? status.trim() : null;

        Page<CollegeTimetable> pageResult = repository.searchAndFilter(
                cleanSearch, cleanProg, cleanSem, cleanSec, cleanDay, cleanYear, staffId, subjectId, cleanType, cleanStatus, pageable
        );

        return pageResult.map(CollegeTimetableMapper::toResponse);
    }

    private void validateBasicRequest(CollegeTimetableRequest request) {
        if (request == null) {
            throw new CustomException("Request body cannot be null", "INVALID_REQUEST", "Please provide slot details");
        }
        if (request.getProgram() == null || request.getProgram().trim().isBlank()) {
            throw new CustomException("Program is required", "REQUIRED_FIELD", "program cannot be blank");
        }
        if (request.getSemester() == null || request.getSemester().trim().isBlank()) {
            throw new CustomException("Semester is required", "REQUIRED_FIELD", "semester cannot be blank");
        }
        if (request.getSection() == null || request.getSection().trim().isBlank()) {
            throw new CustomException("Section is required", "REQUIRED_FIELD", "section cannot be blank");
        }
        if (request.getDay() == null || request.getDay().trim().isBlank()) {
            throw new CustomException("Day is required", "REQUIRED_FIELD", "day of week cannot be blank");
        }
        if (request.getTimeSlot() == null || request.getTimeSlot().trim().isBlank()) {
            throw new CustomException("Time slot is required", "REQUIRED_FIELD", "timeSlot cannot be blank");
        }
        if (request.getRoom() == null || request.getRoom().trim().isBlank()) {
            throw new CustomException("Room / Venue is required", "REQUIRED_FIELD", "room cannot be blank");
        }
    }

    private void validateConflicts(CollegeTimetableRequest req, Long excludeId) {
        String cleanYear = req.getAcademicYear() != null ? req.getAcademicYear().trim() : null;

        // 1. Section slot clash: Can't have 2 classes at the same time for the same section
        List<CollegeTimetable> sectionConflicts = repository.findSectionConflicts(
                req.getProgram().trim(), req.getSemester().trim(), req.getSection().trim(),
                req.getDay().trim(), req.getTimeSlot().trim(), cleanYear, excludeId
        );
        if (!sectionConflicts.isEmpty()) {
            CollegeTimetable conflict = sectionConflicts.get(0);
            throw new CustomException(
                    "Section schedule conflict",
                    "SECTION_SLOT_CONFLICT",
                    String.format("Section %s already has '%s' scheduled on %s (%s) in %s",
                            req.getSection(),
                            conflict.getSubjectName() != null ? conflict.getSubjectName() : conflict.getSubjectCode(),
                            conflict.getDay(),
                            conflict.getTimeSlot(),
                            conflict.getRoom())
            );
        }

        // 2. Teacher clash: A teacher can't teach two classes at the same day & time
        if (req.getStaffId() != null || (req.getTeacherName() != null && !req.getTeacherName().trim().isBlank())) {
            String tName = req.getTeacherName() != null ? req.getTeacherName().trim() : null;
            List<CollegeTimetable> teacherConflicts = repository.findTeacherConflicts(
                    req.getStaffId(), tName, req.getDay().trim(), req.getTimeSlot().trim(), cleanYear, excludeId
            );
            if (!teacherConflicts.isEmpty()) {
                CollegeTimetable conflict = teacherConflicts.get(0);
                throw new CustomException(
                        "Teacher allocation conflict",
                        "TEACHER_SLOT_CONFLICT",
                        String.format("Teacher '%s' is already booked for %s (%s - %s) on %s (%s)",
                                conflict.getTeacherName(),
                                conflict.getSubjectName() != null ? conflict.getSubjectName() : conflict.getSubjectCode(),
                                conflict.getProgram(),
                                conflict.getSection(),
                                conflict.getDay(),
                                conflict.getTimeSlot())
                );
            }
        }

        // 3. Room clash: A room can't be occupied by two classes at the same day & time
        if (req.getRoom() != null && !req.getRoom().trim().isBlank()) {
            List<CollegeTimetable> roomConflicts = repository.findRoomConflicts(
                    req.getRoom().trim(), req.getDay().trim(), req.getTimeSlot().trim(), cleanYear, excludeId
            );
            if (!roomConflicts.isEmpty()) {
                CollegeTimetable conflict = roomConflicts.get(0);
                throw new CustomException(
                        "Room occupancy conflict",
                        "ROOM_SLOT_CONFLICT",
                        String.format("Room '%s' is already booked by %s (%s - %s) on %s (%s)",
                                req.getRoom(),
                                conflict.getSubjectName() != null ? conflict.getSubjectName() : conflict.getSubjectCode(),
                                conflict.getProgram(),
                                conflict.getSection(),
                                conflict.getDay(),
                                conflict.getTimeSlot())
                );
            }
        }
    }

    private CollegeSubject resolveSubject(CollegeTimetableRequest req) {
        if (req.getSubjectId() != null && req.getSubjectId() > 0) {
            return subjectRepository.findByIdAndIsDeletedFalse(req.getSubjectId()).orElse(null);
        }
        if (req.getSubjectCode() != null && !req.getSubjectCode().trim().isBlank()) {
            return subjectRepository.findByIsDeletedFalseAndStatusOrderBySubjectCodeAsc("Active")
                    .stream()
                    .filter(s -> s.getSubjectCode().equalsIgnoreCase(req.getSubjectCode().trim()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    private Staff resolveStaff(CollegeTimetableRequest req) {
        if (req.getStaffId() != null && req.getStaffId() > 0) {
            return staffRepository.findByIdAndIsDeletedFalse(req.getStaffId()).orElse(null);
        }
        if (req.getStaffCode() != null && !req.getStaffCode().trim().isBlank()) {
            return staffRepository.findByStaffCodeIgnoreCaseAndIsDeletedFalse(req.getStaffCode().trim()).orElse(null);
        }
        return null;
    }

    private Degree resolveDegree(CollegeTimetableRequest req) {
        if (req.getDegreeId() != null && req.getDegreeId() > 0) {
            return degreeRepository.findByIdAndIsDeletedFalse(req.getDegreeId()).orElse(null);
        }
        if (req.getDegreeCode() != null && !req.getDegreeCode().trim().isBlank()) {
            return degreeRepository.findByCodeIgnoreCaseAndIsDeletedFalse(req.getDegreeCode().trim()).orElse(null);
        }
        return null;
    }
}
