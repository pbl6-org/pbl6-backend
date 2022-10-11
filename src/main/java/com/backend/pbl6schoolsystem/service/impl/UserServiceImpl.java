package com.backend.pbl6schoolsystem.service.impl;

import com.backend.pbl6schoolsystem.common.constant.ErrorCode;
import com.backend.pbl6schoolsystem.common.enums.UserRole;
import com.backend.pbl6schoolsystem.common.exception.NotFoundException;
import com.backend.pbl6schoolsystem.mapper.UserMapper;
import com.backend.pbl6schoolsystem.model.dto.user.SchoolAdminDTO;
import com.backend.pbl6schoolsystem.model.entity.SchoolEntity;
import com.backend.pbl6schoolsystem.model.entity.UserEntity;
import com.backend.pbl6schoolsystem.repository.dsl.UserDslRepository;
import com.backend.pbl6schoolsystem.repository.jpa.RoleRepository;
import com.backend.pbl6schoolsystem.repository.jpa.SchoolRepository;
import com.backend.pbl6schoolsystem.repository.jpa.UserRepository;
import com.backend.pbl6schoolsystem.request.school.CreateSchoolAdminRequest;
import com.backend.pbl6schoolsystem.request.user.ListSchoolAdminRequest;
import com.backend.pbl6schoolsystem.response.ErrorResponse;
import com.backend.pbl6schoolsystem.response.user.GetSchoolAdminResponse;
import com.backend.pbl6schoolsystem.response.user.ListUserResponse;
import com.backend.pbl6schoolsystem.response.OnlyIdResponse;
import com.backend.pbl6schoolsystem.response.PageResponse;
import com.backend.pbl6schoolsystem.service.UserService;
import com.backend.pbl6schoolsystem.util.RequestUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {
    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final RoleRepository roleRepository;
    private final UserDslRepository userDslRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OnlyIdResponse createSchoolAdmin(CreateSchoolAdminRequest request) {
        Map<String, String> errors = new HashMap<>();
        checkInputCreateUpdateSchoolAdmin(request, errors);
        if (!errors.isEmpty()) {
            return OnlyIdResponse.builder()
                    .setSuccess(false)
                    .setErrorResponse(ErrorResponse.builder()
                            .setErrors(errors)
                            .build())
                    .build();
        }

        SchoolEntity school = schoolRepository.findById(request.getSchoolId())
                .orElseThrow(() -> new NotFoundException("Not found school with id " + request.getSchoolId()));

        UserEntity schoolAdmin = new UserEntity();
        schoolAdmin.setFirstName(request.getFirstName());
        schoolAdmin.setLastName(request.getLastName());
        schoolAdmin.setEmail(request.getEmail());
        schoolAdmin.setUsername(request.getUsername());
        schoolAdmin.setPassword(passwordEncoder.encode(request.getPassword()));
        schoolAdmin.setSchool(school);
        schoolAdmin.setRole(roleRepository.findById(UserRole.SCHOOL_ROLE.getRoleId()).get());
        userRepository.save(schoolAdmin);
        return OnlyIdResponse.builder()
                .setSuccess(true)
                .setId(schoolAdmin.getUserId())
                .setName(schoolAdmin.getFirstName() + " " + schoolAdmin.getLastName())
                .build();
    }

    @Override
    public OnlyIdResponse updateSchoolAdmin(Long schoolAdminId, CreateSchoolAdminRequest request) {
        UserEntity schoolAdmin = userRepository.findById(schoolAdminId).orElseThrow(() -> new NotFoundException("Not found User"));
        Map<String, String> errors = new HashMap<>();
        checkInputCreateUpdateSchoolAdmin(request, errors);

        if (!errors.isEmpty()) {
            return OnlyIdResponse.builder()
                    .setSuccess(false)
                    .setErrorResponse(ErrorResponse.builder()
                            .setErrors(errors)
                            .build())
                    .build();
        }

        SchoolEntity school = schoolRepository.findById(request.getSchoolId())
                .orElseThrow(() -> new NotFoundException("Not found school with id " + request.getSchoolId()));

        schoolAdmin.setFirstName(request.getFirstName());
        schoolAdmin.setLastName(request.getFirstName());
        schoolAdmin.setEmail(request.getEmail());
        schoolAdmin.setPassword(passwordEncoder.encode(request.getPassword()));
        schoolAdmin.setSchool(school);
        userRepository.save(schoolAdmin);

        return OnlyIdResponse.builder()
                .setSuccess(true)
                .setId(schoolAdmin.getUserId())
                .setName(schoolAdmin.getFirstName() + " " + schoolAdmin.getLastName())
                .build();
    }

    @Override
    public ListUserResponse getListSchoolAdmin(ListSchoolAdminRequest request) {
        request.setSchoolId(RequestUtil.defaultIfNull(request.getSchoolId(), (long) -1));
        List<UserEntity> schoolAdmins = userDslRepository.getListSchoolAdmin(request);
        return ListUserResponse.builder()
                .setPageResponse(PageResponse.builder()
                        .setTotalItems((long) schoolAdmins.size())
                        .setPage(request.getPage())
                        .setSize(request.getAll() ? Integer.MAX_VALUE : request.getSize())
                        .setTotalPages(request.getAll() ? 1 : RequestUtil.getTotalPages((long) schoolAdmins.size(), request.getSize()))
                        .build())
                .setItems(schoolAdmins.stream()
                        .map(UserMapper::entity2dto)
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public GetSchoolAdminResponse getSchoolAdmin(Long schoolAdminId) {
        UserEntity schoolAdmin = userRepository.findSchoolAdminById(schoolAdminId, UserRole.SCHOOL_ROLE.getRoleId())
                .orElseThrow(() -> new NotFoundException("Not found school admin with id " + schoolAdminId));
        return GetSchoolAdminResponse.builder()
                .setSuccess(true)
                .setSchoolAdminDTO(SchoolAdminDTO.builder()
                        .setSchoolAdminId(schoolAdmin.getUserId())
                        .setUsername(schoolAdmin.getUsername())
                        .setFirstName(schoolAdmin.getFirstName())
                        .setLastName(schoolAdmin.getLastName())
                        .setEmail(schoolAdmin.getEmail())
                        .setPassword("Anonymous")
                        .setSchoolId(schoolAdmin.getSchool().getSchoolId())
                        .setSchoolName(schoolAdmin.getSchool().getSchool())
                        .build())
                .build();
    }

    public void checkInputCreateUpdateSchoolAdmin(CreateSchoolAdminRequest request, Map<String, String> errors) {
        if (!StringUtils.hasText(request.getFirstName())) {
            errors.put("FirstName", ErrorCode.MISSING_VALUE.name());
        }
        if (!StringUtils.hasText(request.getLastName())) {
            errors.put("LastName", ErrorCode.MISSING_VALUE.name());
        }
        if (!StringUtils.hasText(request.getUsername())) {
            errors.put("UserName", ErrorCode.MISSING_VALUE.name());
        }
        if (!StringUtils.hasText(request.getPassword())) {
            errors.put("Password", ErrorCode.MISSING_VALUE.name());
        }
        if (!StringUtils.hasText(request.getEmail())) {
            errors.put("Email", ErrorCode.MISSING_VALUE.name());
        }
        if (request.getSchoolId() < 0) {
            errors.put("SchoolId", ErrorCode.MISSING_VALUE.name());
        }

        if (StringUtils.hasText(request.getEmail())) {
            boolean isExistEmail = userRepository.findOneByEmail(request.getEmail()).isPresent();
            if (isExistEmail) {
                errors.put("Email", ErrorCode.ALREADY_EXIST.name());
            }
        }

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username);
        if (user == null) {
            throw new NotFoundException("Not found username " + username);
        } else {
            Collection<SimpleGrantedAuthority> simpleGrantedAuthorities = new ArrayList<>();
            List.of(user.getRole()).forEach(role -> {
                simpleGrantedAuthorities.add(new SimpleGrantedAuthority(role.getRole()));
            });
            return new User(user.getUsername(), user.getPassword(), simpleGrantedAuthorities);
        }
    }
}

