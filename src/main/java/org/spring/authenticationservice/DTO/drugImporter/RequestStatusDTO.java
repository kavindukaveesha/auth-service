package org.spring.authenticationservice.DTO.drugImporter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.spring.authenticationservice.model.Enum.RequestStatusEnum;

public class RequestStatusDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private Long requestId;
        private Long drugImporterId;
        private RequestStatusEnum status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long requestId;
        private Long drugImporterId;
        private RequestStatusEnum status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private RequestStatusEnum status;
    }
}
