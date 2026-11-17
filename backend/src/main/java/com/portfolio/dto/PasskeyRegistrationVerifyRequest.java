package com.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class PasskeyRegistrationVerifyRequest {

    @NotBlank(message = "Device name is required")
    private String deviceName;

    @NotBlank(message = "Device type is required")
    private String deviceType; // "LAPTOP_WINDOWS_HELLO", "ANDROID_PHONE", "IPHONE_BIOMETRIC", "PLATFORM_PIN"

    @NotBlank(message = "Credential ID is required")
    private String id;

    private String rawId;
    private String type = "public-key";

    private ResponseData response;

    public static class ResponseData {
        private String clientDataJSON;
        private String attestationObject;
        private List<String> transports;

        public String getClientDataJSON() {
            return clientDataJSON;
        }

        public void setClientDataJSON(String clientDataJSON) {
            this.clientDataJSON = clientDataJSON;
        }

        public String getAttestationObject() {
            return attestationObject;
        }

        public void setAttestationObject(String attestationObject) {
            this.attestationObject = attestationObject;
        }

        public List<String> getTransports() {
            return transports;
        }

        public void setTransports(List<String> transports) {
            this.transports = transports;
        }
    }

    public PasskeyRegistrationVerifyRequest() {
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRawId() {
        return rawId;
    }

    public void setRawId(String rawId) {
        this.rawId = rawId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ResponseData getResponse() {
        return response;
    }

    public void setResponse(ResponseData response) {
        this.response = response;
    }
}
