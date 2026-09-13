package com.academic.mapper;

import com.academic.entity.CollegeSubject;
import com.academic.entity.CollegeTimetable;
import com.academic.entity.Degree;
import com.academic.entity.Staff;
import com.academic.entity.Subject;
import com.academic.request.CollegeTimetableRequest;
import com.academic.response.CollegeTimetableResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CollegeTimetableMapper {

    public static CollegeTimetable toEntity(CollegeTimetableRequest request, Degree degree, CollegeSubject subject, Staff staff) {
        if (request == null) return null;

        // Resolve subject codes/names from entity if available, fallback to request
        String resolvedSubjectCode = subject != null ? subject.getSubjectCode() : request.getSubjectCode();
        String resolvedSubjectName = subject != null ? subject.getSubjectName() : request.getSubjectName();

        // Resolve teacher names/codes from staff if available, fallback to request
        String resolvedTeacherName = null;
        String resolvedStaffCode = null;
        if (staff != null) {
            resolvedTeacherName = (staff.getFirstName() != null ? staff.getFirstName() : "")
                    + (staff.getLastName() != null && !staff.getLastName().isBlank() ? " " + staff.getLastName() : "");
            resolvedTeacherName = resolvedTeacherName.trim();
            resolvedStaffCode = staff.getStaffCode();
        }
        if (resolvedTeacherName == null || resolvedTeacherName.isBlank()) {
            resolvedTeacherName = request.getTeacherName();
        }
        if (resolvedStaffCode == null || resolvedStaffCode.isBlank()) {
            resolvedStaffCode = request.getStaffCode();
        }

        String resolvedDegreeCode = degree != null ? degree.getCode() : request.getDegreeCode();

        return CollegeTimetable.builder()
                .degree(degree)
                .degreeCode(resolvedDegreeCode != null ? resolvedDegreeCode.trim() : null)
                .program(request.getProgram() != null ? request.getProgram().trim() : null)
                .semester(request.getSemester() != null ? request.getSemester().trim() : null)
                .section(request.getSection() != null ? request.getSection().trim() : null)
                .academicYear(request.getAcademicYear() != null && !request.getAcademicYear().isBlank()
                        ? request.getAcademicYear().trim() : "2025-2026")
                .day(request.getDay() != null ? request.getDay().trim() : null)
                .timeSlot(request.getTimeSlot() != null ? request.getTimeSlot().trim() : null)
                .periodLabel(request.getPeriodLabel() != null ? request.getPeriodLabel().trim() : null)
                .startTime(request.getStartTime() != null ? request.getStartTime().trim() : null)
                .endTime(request.getEndTime() != null ? request.getEndTime().trim() : null)
                .durationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 60)
                .isBreak(request.getIsBreak() != null ? request.getIsBreak() : false)
                .subject(subject)
                .subjectCode(resolvedSubjectCode != null ? resolvedSubjectCode.trim() : null)
                .subjectName(resolvedSubjectName != null ? resolvedSubjectName.trim() : null)
                .staff(staff)
                .teacherName(resolvedTeacherName != null ? resolvedTeacherName.trim() : null)
                .staffCode(resolvedStaffCode != null ? resolvedStaffCode.trim() : null)
                .room(request.getRoom() != null ? request.getRoom().trim() : null)
                .type(request.getType() != null && !request.getType().isBlank() ? request.getType().trim() : "Theory")
                .spanPeriods(request.getSpanPeriods() != null ? request.getSpanPeriods() : 1)
                .status(request.getStatus() != null && !request.getStatus().isBlank() ? request.getStatus().trim() : "Active")
                .isDeleted(false)
                .build();
    }

    public static void updateEntity(CollegeTimetable entity, CollegeTimetableRequest request, Degree degree, CollegeSubject subject, Staff staff) {
        if (entity == null || request == null) return;

        if (degree != null) {
            entity.setDegree(degree);
            entity.setDegreeCode(degree.getCode());
        } else if (request.getDegreeCode() != null) {
            entity.setDegreeCode(request.getDegreeCode().trim());
        }

        if (request.getProgram() != null) entity.setProgram(request.getProgram().trim());
        if (request.getSemester() != null) entity.setSemester(request.getSemester().trim());
        if (request.getSection() != null) entity.setSection(request.getSection().trim());
        if (request.getAcademicYear() != null && !request.getAcademicYear().isBlank()) {
            entity.setAcademicYear(request.getAcademicYear().trim());
        }

        if (request.getDay() != null) entity.setDay(request.getDay().trim());
        if (request.getTimeSlot() != null) entity.setTimeSlot(request.getTimeSlot().trim());
        if (request.getPeriodLabel() != null) entity.setPeriodLabel(request.getPeriodLabel().trim());
        if (request.getStartTime() != null) entity.setStartTime(request.getStartTime().trim());
        if (request.getEndTime() != null) entity.setEndTime(request.getEndTime().trim());
        if (request.getDurationMinutes() != null) entity.setDurationMinutes(request.getDurationMinutes());
        if (request.getIsBreak() != null) entity.setIsBreak(request.getIsBreak());

        if (subject != null) {
            entity.setSubject(subject);
            entity.setSubjectCode(subject.getSubjectCode());
            entity.setSubjectName(subject.getSubjectName());
        } else {
            if (request.getSubjectCode() != null) entity.setSubjectCode(request.getSubjectCode().trim());
            if (request.getSubjectName() != null) entity.setSubjectName(request.getSubjectName().trim());
        }

        if (staff != null) {
            entity.setStaff(staff);
            String fullName = (staff.getFirstName() != null ? staff.getFirstName() : "")
                    + (staff.getLastName() != null && !staff.getLastName().isBlank() ? " " + staff.getLastName() : "");
            entity.setTeacherName(fullName.trim());
            entity.setStaffCode(staff.getStaffCode());
        } else {
            if (request.getTeacherName() != null) entity.setTeacherName(request.getTeacherName().trim());
            if (request.getStaffCode() != null) entity.setStaffCode(request.getStaffCode().trim());
        }

        if (request.getRoom() != null) entity.setRoom(request.getRoom().trim());
        if (request.getType() != null && !request.getType().isBlank()) entity.setType(request.getType().trim());
        if (request.getSpanPeriods() != null) entity.setSpanPeriods(request.getSpanPeriods());
        if (request.getStatus() != null && !request.getStatus().isBlank()) entity.setStatus(request.getStatus().trim());
    }

    public static CollegeTimetableResponse toResponse(CollegeTimetable entity) {
        if (entity == null) return null;

        Integer degreeId = entity.getDegree() != null ? entity.getDegree().getId() : null;
        String degreeCode = entity.getDegree() != null ? entity.getDegree().getCode() : entity.getDegreeCode();
        String degreeName = entity.getDegree() != null ? entity.getDegree().getName() : null;

        Long subjectId = entity.getSubject() != null ? entity.getSubject().getId() : null;
        String subjectCode = entity.getSubject() != null ? entity.getSubject().getSubjectCode() : entity.getSubjectCode();
        String subjectName = entity.getSubject() != null ? entity.getSubject().getSubjectName() : entity.getSubjectName();
        String subjectType = entity.getSubject() != null ? entity.getSubject().getType() : null;
        Integer subjectCredits = entity.getSubject() != null ? entity.getSubject().getCredits() : null;

        Integer staffId = entity.getStaff() != null ? entity.getStaff().getId() : null;
        String staffCode = entity.getStaff() != null ? entity.getStaff().getStaffCode() : entity.getStaffCode();
        String teacherName = entity.getTeacherName();
        String staffDept = null;
        String staffDesig = null;
        String staffEmail = null;

        if (entity.getStaff() != null) {
            Staff s = entity.getStaff();
            if (teacherName == null || teacherName.isBlank()) {
                teacherName = (s.getFirstName() != null ? s.getFirstName() : "")
                        + (s.getLastName() != null && !s.getLastName().isBlank() ? " " + s.getLastName() : "");
                teacherName = teacherName.trim();
            }
            staffEmail = s.getEmail();
            if (s.getDepartment() != null) {
                staffDept = s.getDepartment().getName();
            }
            if (s.getDesignation() != null) {
                staffDesig = s.getDesignation().getName();
            }
        }

        return CollegeTimetableResponse.builder()
                .id(entity.getId())
                .degreeId(degreeId)
                .degreeCode(degreeCode)
                .degreeName(degreeName)
                .program(entity.getProgram())
                .semester(entity.getSemester())
                .section(entity.getSection())
                .academicYear(entity.getAcademicYear())
                .day(entity.getDay())
                .timeSlot(entity.getTimeSlot())
                .periodLabel(entity.getPeriodLabel())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .durationMinutes(entity.getDurationMinutes())
                .isBreak(entity.getIsBreak())
                .subjectId(subjectId)
                .subjectCode(subjectCode)
                .subjectName(subjectName)
                .subjectType(subjectType)
                .subjectCredits(subjectCredits)
                .staffId(staffId)
                .staffCode(staffCode)
                .teacherName(teacherName)
                .staffDepartment(staffDept)
                .staffDesignation(staffDesig)
                .staffEmail(staffEmail)
                .room(entity.getRoom())
                .type(entity.getType())
                .spanPeriods(entity.getSpanPeriods())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static List<CollegeTimetableResponse> toResponseList(List<CollegeTimetable> list) {
        if (list == null) return new ArrayList<>();
        return list.stream().map(CollegeTimetableMapper::toResponse).collect(Collectors.toList());
    }
}
