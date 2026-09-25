package com.hyperbrains.hms.service.dto.view;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/** A lab or radiology user recording what came back. */
public class EnterResultRequestDTO implements Serializable {

    @NotBlank
    @Size(max = 10000)
    private String resultValue;

    @Size(max = 10000)
    private String notes;

    /** Where the image lives, for radiology. */
    @Size(max = 2000)
    private String imageReference;

    public String getResultValue() {
        return resultValue;
    }

    public void setResultValue(String resultValue) {
        this.resultValue = resultValue;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getImageReference() {
        return imageReference;
    }

    public void setImageReference(String imageReference) {
        this.imageReference = imageReference;
    }
}
