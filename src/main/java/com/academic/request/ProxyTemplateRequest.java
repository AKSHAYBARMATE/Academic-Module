package com.academic.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProxyTemplateRequest {

    @NotBlank(message = "Template identification name is required")
    private String templateName; // e.g. "Math Substitute - Cover"

    @NotNull(message = "Substitute teacher ID is required")
    private Long substituteTeacherId;

    private String substituteTeacherName;

    private String remarks; // Bio/remarks
}
