package com.academic.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "proxy_template")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProxyTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String templateName; // e.g. "Math Substitute - Cover"

    @Column(nullable = false)
    private Long substituteTeacherId; // Staff ID

    @Column(nullable = false)
    private String substituteTeacherName; // Staff name (e.g. "Renoo Singh")

    @Column(length = 1000)
    private String remarks; // e.g. "Available for Class 9-12 Mathematics"

    @Builder.Default
    private Boolean isDeleted = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
