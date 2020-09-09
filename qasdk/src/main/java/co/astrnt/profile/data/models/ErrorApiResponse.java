package co.astrnt.profile.data.models;

import java.util.List;

public class ErrorApiResponse {

    private List<String> errorMessages;

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages;
    }
}
