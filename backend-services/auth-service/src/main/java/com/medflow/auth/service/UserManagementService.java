package com.medflow.auth.service;

import com.medflow.auth.domain.Role;
import com.medflow.auth.domain.RoleName;
import com.medflow.auth.domain.User;
import com.medflow.auth.dto.CreateEmployeeRequest;
import com.medflow.auth.dto.EmployeeResponse;
import com.medflow.auth.dto.UpdateEmployeeRequest;
import com.medflow.auth.exception.EmailAlreadyExistsException;
import com.medflow.auth.exception.EmployeeNotFoundException;
import com.medflow.auth.repository.RoleRepository;
import com.medflow.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserManagementService {

    private static final Logger log = LoggerFactory.getLogger(UserManagementService.class);
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final Set<RoleName> EMPLOYEE_ROLES = Set.of(
        RoleName.ADMISSION, RoleName.VITAL_SIGNS, RoleName.DOCTOR,
        RoleName.LABORATORY, RoleName.PHARMACY, RoleName.CASHIER
    );

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService blacklistService;
    private final EmailService emailService;

    public UserManagementService(UserRepository userRepository,
                                  RoleRepository roleRepository,
                                  PasswordEncoder passwordEncoder,
                                  JwtService jwtService,
                                  TokenBlacklistService blacklistService,
                                  EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.blacklistService = blacklistService;
        this.emailService = emailService;
    }

    @Transactional
    public EmployeeCreationResult createEmployee(CreateEmployeeRequest request) {
        RoleName roleName = resolveEmployeeRole(request.getRoleName());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + roleName));

        String username = generateUsername(request.getFirstLastName(), request.getFirstName());
        String tempPassword = generateTempPassword();

        User user = User.builder()
            .username(username)
            .email(request.getEmail())
            .password(passwordEncoder.encode(tempPassword))
            .firstName(request.getFirstName())
            .secondName(request.getSecondName())
            .firstLastName(request.getFirstLastName())
            .secondLastName(request.getSecondLastName())
            .phone(request.getPhone())
            .active(true)
            .roles(Set.of(role))
            .build();

        User saved = userRepository.save(user);
        emailService.sendEmployeeCreatedEmail(
                saved.getEmail(), saved.getFirstName(), username, tempPassword);
        log.info("[CU-02] Empleado creado. username={}, rol={}", username, roleName);

        return new EmployeeCreationResult(EmployeeResponse.fromUser(saved), tempPassword);
    }

    public List<EmployeeResponse> listEmployees(String roleFilter, Boolean activeFilter) {
        return userRepository.findAll().stream()
            .filter(u -> u.getRoles().stream()
                .anyMatch(r -> EMPLOYEE_ROLES.contains(r.getName())))
            .filter(u -> roleFilter == null || u.getRoles().stream()
                .anyMatch(r -> r.getName().name().equalsIgnoreCase(roleFilter)))
            .filter(u -> activeFilter == null || u.isActive() == activeFilter)
            .map(EmployeeResponse::fromUser)
            .collect(Collectors.toList());
    }

    public EmployeeResponse getEmployee(String id) {
        return EmployeeResponse.fromUser(findEmployeeById(id));
    }

    @Transactional
    public EmployeeResponse updateEmployee(String id, UpdateEmployeeRequest request) {
        User user = findEmployeeById(id);

        if (request.getFirstName() != null && !request.getFirstName().isBlank())
            user.setFirstName(request.getFirstName());
        if (request.getSecondName() != null)
            user.setSecondName(request.getSecondName().isBlank() ? null : request.getSecondName());
        if (request.getFirstLastName() != null && !request.getFirstLastName().isBlank())
            user.setFirstLastName(request.getFirstLastName());
        if (request.getSecondLastName() != null)
            user.setSecondLastName(request.getSecondLastName().isBlank() ? null : request.getSecondLastName());
        if (request.getPhone() != null)
            user.setPhone(request.getPhone().isBlank() ? null : request.getPhone());

        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent())
                throw new EmailAlreadyExistsException(request.getEmail());
            user.setEmail(request.getEmail());
        }

        if (request.getRoleName() != null && !request.getRoleName().isBlank()) {
            RoleName roleName = resolveEmployeeRole(request.getRoleName());
            Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + roleName));
            user.setRoles(Set.of(role));
        }

        User saved = userRepository.save(user);
        log.info("[CU-02] Empleado actualizado. id={}", id);
        return EmployeeResponse.fromUser(saved);
    }

    @Transactional
    public EmployeeResponse toggleActive(String id) {
        User user = findEmployeeById(id);
        user.setActive(!user.isActive());
        User saved = userRepository.save(user);
        log.info("[CU-02] Estado empleado cambiado. id={}, activo={}", id, saved.isActive());
        return EmployeeResponse.fromUser(saved);
    }

    public void requireAdmin(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token requerido");
        }
        String token = authHeader.substring(7);
        if (blacklistService.isBlacklisted(token) || !jwtService.isValid(token)) {
            throw new RuntimeException("Invalid token");
        }
        String roles = jwtService.extractRoles(token);
        if (roles == null || !roles.contains("ADMIN")) {
            throw new RuntimeException("Acceso denegado: se requiere rol ADMIN");
        }
    }

    private User findEmployeeById(String id) {
        User user = userRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new EmployeeNotFoundException(id));
        boolean isEmployee = user.getRoles().stream()
            .anyMatch(r -> EMPLOYEE_ROLES.contains(r.getName()));
        if (!isEmployee) throw new EmployeeNotFoundException(id);
        return user;
    }

    private RoleName resolveEmployeeRole(String roleName) {
        try {
            RoleName name = RoleName.valueOf(roleName.toUpperCase());
            if (!EMPLOYEE_ROLES.contains(name)) {
                throw new IllegalArgumentException("Rol no permitido para empleados: " + roleName);
            }
            return name;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Rol invalido: " + roleName +
                ". Valores permitidos: ADMISSION, VITAL_SIGNS, DOCTOR, LABORATORY, PHARMACY, CASHIER");
        }
    }

    private String generateUsername(String firstLastName, String firstName) {
        String base = normalize(firstLastName) + "." + normalize(firstName);
        if (userRepository.findByUsername(base).isEmpty()) return base;
        int counter = 2;
        while (userRepository.findByUsername(base + counter).isPresent()) counter++;
        return base + counter;
    }

    private String normalize(String text) {
        String normalized = Normalizer.normalize(text.trim().toLowerCase(), Normalizer.Form.NFD);
        return normalized.replaceAll("[^a-z]", "");
    }

    private String generateTempPassword() {
        char[] arr = new char[10];
        arr[0] = CHARS.charAt(RANDOM.nextInt(26));
        arr[1] = CHARS.charAt(26 + RANDOM.nextInt(26));
        arr[2] = CHARS.charAt(52 + RANDOM.nextInt(10));
        for (int i = 3; i < 10; i++) arr[i] = CHARS.charAt(RANDOM.nextInt(CHARS.length()));
        for (int i = arr.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char tmp = arr[i]; arr[i] = arr[j]; arr[j] = tmp;
        }
        return new String(arr);
    }

    public record EmployeeCreationResult(EmployeeResponse employee, String temporaryPassword) {}
}
