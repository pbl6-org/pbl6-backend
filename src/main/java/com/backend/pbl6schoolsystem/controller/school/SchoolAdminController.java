package com.backend.pbl6schoolsystem.controller.school;

import com.backend.pbl6schoolsystem.converter.UserConverter;
import com.backend.pbl6schoolsystem.model.dto.common.ListDTO;
import com.backend.pbl6schoolsystem.model.dto.common.MessageDTO;
import com.backend.pbl6schoolsystem.model.dto.common.OnlyIdDTO;
import com.backend.pbl6schoolsystem.model.dto.user.SchoolAdminDTO;
import com.backend.pbl6schoolsystem.request.school.CreateSchoolAdminRequest;
import com.backend.pbl6schoolsystem.request.user.ListSchoolAdminRequest;
import com.backend.pbl6schoolsystem.response.NoContentResponse;
import com.backend.pbl6schoolsystem.response.user.GetSchoolAdminResponse;
import com.backend.pbl6schoolsystem.response.user.ListUserResponse;
import com.backend.pbl6schoolsystem.response.OnlyIdResponse;
import com.backend.pbl6schoolsystem.response.Response;
import com.backend.pbl6schoolsystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/schooladmins")
@RequiredArgsConstructor
public class SchoolAdminController {
    private final UserService userService;
    private final UserConverter userConverter;

    @Operation(summary = "List school admin")
    @GetMapping
    private Response<ListDTO<SchoolAdminDTO>> getListSchoolAdmin(@ModelAttribute @Valid ListSchoolAdminRequest request) {
        ListUserResponse response = userService.getListSchoolAdmin(request);
        return userConverter.getResponse(response);
    }

    @Operation(summary = "Get school admin")
    @GetMapping("/{id}")
    private Response<SchoolAdminDTO> getSchoolAdmin(@PathVariable("id") Long schoolAdminId) {
        GetSchoolAdminResponse response = userService.getSchoolAdmin(schoolAdminId);
        return userConverter.getResponse(response);
    }


    @Operation(summary = "Create school admin")
    @PostMapping
    private Response<OnlyIdDTO> createSchoolAdmin(@RequestBody CreateSchoolAdminRequest request) {
        OnlyIdResponse response = userService.createSchoolAdmin(request);
        if (response.getSuccess()) {
            return userConverter.getResponse(response);
        } else {
            return userConverter.getError(response.getErrorResponse());
        }
    }

    @Operation(summary = "Update school admin")
    @PutMapping("/{id}")
    private Response<OnlyIdDTO> updateSchoolAdmin(@PathVariable("id") Long schoolAdminId, @RequestBody CreateSchoolAdminRequest request) {
        OnlyIdResponse response = userService.updateSchoolAdmin(schoolAdminId, request);
        if (response.getSuccess()) {
            return userConverter.getResponse(response);
        } else {
            return userConverter.getError(response.getErrorResponse());
        }
    }

    @Operation(summary = "Delete school admin")
    @DeleteMapping("/{id}")
    private Response<MessageDTO> deleteSchoolAdmin(@PathVariable("id") Long schoolAdminId) {
        NoContentResponse response = userService.deleteSchoolAdmin(schoolAdminId);
        if (response.getSuccess()) {
            return userConverter.getResponse(response);
        } else {
            return userConverter.getError(response.getErrorResponse());
        }
    }
}
