package com.academic.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class DeviceTokenRequest {

    private String deviceToken;

    private String deviceType;
}
