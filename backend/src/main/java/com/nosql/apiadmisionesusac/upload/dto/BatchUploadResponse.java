package com.nosql.apiadmisionesusac.upload.dto;

import java.util.List;

public record BatchUploadResponse(List<FileUploadResponse> archivos, String mensaje) {}
