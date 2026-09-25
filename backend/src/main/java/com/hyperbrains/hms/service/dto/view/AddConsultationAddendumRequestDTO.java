package com.hyperbrains.hms.service.dto.view;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
/** A note appended to a consultation that has already been completed. */
public class AddConsultationAddendumRequestDTO implements Serializable {

    @NotBlank
    @Size(max = 10000)
    private String body;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
