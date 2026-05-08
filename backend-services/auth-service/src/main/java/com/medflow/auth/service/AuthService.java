package com.medflow.auth.service;

import com.medflow.auth.client.PatientServiceClient;
import com.medflow.auth.domain.Role;
import com.medflow.auth.domain.RoleName;
import com.medflow.auth.domain.User;
import com.medflow.auth.dto.CreatePatientAccountRequest;
import com.medflow.auth.dto.CreatePatientAccountResponse;
import com.medflow.auth.dto.CreatePatientInternalRequest;
import com.medflow.auth.dto.PatientResponse;
import com.medflow.auth.dto.RegisterRequest;
import com.medflow.auth.exception.EmailAlreadyExistsException;
import com.medflow.auth.exception.InvalidActivationTokenException;
import com.medflow.auth.exception.InvalidMedicalDataException;
import com.medflow.auth.exception.PatientCreationException;
import com.medflow.auth.exception.UsernameAlreadyExistsException;
import com.medflow.auth.repository.RoleRepository;
import com.medflow.auth.repository.UserRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService blacklistService;
    private final ActivationTokenService activationTokenService;
    private final EmailService emailService;
    private final PatientServiceClient patientServiceClient;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder,
                       TokenBlacklistService blacklistService,
                       ActivationTokenService activationTokenService,
                       EmailService emailService,
                       PatientServiceClient patientServiceClient) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.blacklistService = blacklistService;
        this.activationTokenService = activationTokenService;
        this.emailService = emailService;
        this.patientServiceClient = patientServiceClient;
    }

    public String login(String identifier, String password) {
        var user = userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        if (!user.isActive()) {
            throw new RuntimeException("Account is disabled");
        }
        return jwtService.generateToken(user);
    }

    @Transactional
    public String register(RegisterRequest request) {
        // Validar campos mÃ©dicos
        validateBirthDate(request.getBirthDate());
        validateGender(request.getGender());
        
        // Validar unicidad
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        if (userRepository.findByUsername(request.getDpi()).isPresent()) {
            throw new UsernameAlreadyExistsException(request.getDpi());
        }

        Role patientRole = roleRepository.findByName(RoleName.PATIENT)
                .orElseThrow(() -> new RuntimeException("Rol PATIENT no encontrado en BD"));

        // Paso 1: Crear usuario en auth_schema.users
        User user = User.builder()
                .username(request.getDpi())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .secondName(request.getSecondName())
                .firstLastName(request.getFirstLastName())
                .secondLastName(request.getSecondLastName())
                .phone(request.getPhone())
                .active(false)
                .roles(Set.of(patientRole))
                .build();

        User saved = userRepository.save(user);
        log.info("[CU-00.2] Usuario creado en auth_schema.users. DPI: {}, ID: {}", request.getDpi(), saved.getId());

        String token = activationTokenService.generateToken(saved.getId().toString());
        emailService.sendActivationEmail(saved.getEmail(), saved.getFirstName(), token);
        log.info("[CU-00.2] Email de activacion enviado a: {}", saved.getEmail());

        // Paso 2: Crear paciente en patient_schema.patients
        try {
            CreatePatientInternalRequest patientRequest = CreatePatientInternalRequest.builder()
                    .id(saved.getId().toString())
                    .dpi(request.getDpi())
                    .nit(request.getNit())
                    .firstName(request.getFirstName())
                    .secondName(request.getSecondName())
                    .firstLastName(request.getFirstLastName())
                    .secondLastName(request.getSecondLastName())
                    .birthDate(request.getBirthDate())
                    .gender(request.getGender())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .department(request.getDepartment())
                    .municipality(request.getMunicipality())
                    .zone(request.getZone())
                    .address(request.getAddress())
                    .authUserId(saved.getId().toString())
                    .active(true)
                    .build();

            PatientResponse patientResponse = patientServiceClient.createPatient(patientRequest);
            log.info("[CU-00.2] Paciente creado en patient_schema.patients. DPI: {}, Patient ID: {}", 
                    request.getDpi(), patientResponse.getId());

        } catch (FeignException e) {
            log.error("[CU-00.2] Error al crear paciente en patient-service. DPI: {}, Error: {}", 
                    request.getDpi(), e.getMessage());
            throw new PatientCreationException(
                    "Error al crear registro de paciente en patient-service", e);
        } catch (Exception e) {
            log.error("[CU-00.2] Error inesperado al crear paciente. DPI: {}, Error: {}", 
                    request.getDpi(), e.getMessage());
            throw new PatientCreationException(
                    "Error inesperado al crear registro de paciente", e);
        }

        log.info("[CU-00.2] Cuenta creada, pendiente de activacion por email. Email: {}", request.getEmail());
        return null;
    }

    @Transactional
    public void activate(String token) {
        String userId = activationTokenService.validateAndConsume(token);
        if (userId == null) throw new InvalidActivationTokenException();

        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setActive(true);
        userRepository.save(user);
        log.info("[CU-00.2] Cuenta activada para: {}", user.getEmail());
    }

    @Transactional
    public CreatePatientAccountResponse createPatientAccount(CreatePatientAccountRequest request) {
        // Validar campos mÃ©dicos
        validateBirthDate(request.getBirthDate());
        validateGender(request.getGender());
        
        // Validar unicidad
        if (userRepository.findByUsername(request.getDpi()).isPresent()) {
            throw new UsernameAlreadyExistsException(request.getDpi());
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        Role patientRole = roleRepository.findByName(RoleName.PATIENT)
                .orElseThrow(() -> new RuntimeException("Rol PATIENT no encontrado en BD"));

        String tempPassword = generateTempPassword();

        // Paso 1: Crear usuario en auth_schema.users
        User user = User.builder()
                .username(request.getDpi())
                .email(request.getEmail())
                .password(passwordEncoder.encode(tempPassword))
                .firstName(request.getFirstName())
                .secondName(request.getSecondName())
                .firstLastName(request.getFirstLastName())
                .secondLastName(request.getSecondLastName())
                .phone(request.getPhone())
                .active(true)
                .roles(Set.of(patientRole))
                .build();

        User saved = userRepository.save(user);
        log.info("[CU-01] Usuario creado en auth_schema.users. DPI: {}, ID: {}", request.getDpi(), saved.getId());

        // Paso 2: Crear paciente en patient_schema.patients
        String patientId = null;
        try {
            CreatePatientInternalRequest patientRequest = CreatePatientInternalRequest.builder()
                    .id(saved.getId().toString())
                    .dpi(request.getDpi())
                    .nit(request.getNit())
                    .firstName(request.getFirstName())
                    .secondName(request.getSecondName())
                    .firstLastName(request.getFirstLastName())
                    .secondLastName(request.getSecondLastName())
                    .birthDate(request.getBirthDate())
                    .gender(request.getGender())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .department(request.getDepartment())
                    .municipality(request.getMunicipality())
                    .zone(request.getZone())
                    .address(request.getAddress())
                    .authUserId(saved.getId().toString())
                    .active(true)
                    .build();

            PatientResponse patientResponse = patientServiceClient.createPatient(patientRequest);
            patientId = patientResponse.getId();
            log.info("[CU-01] Paciente creado en patient_schema.patients. DPI: {}, Patient ID: {}", 
                    request.getDpi(), patientResponse.getId());

        } catch (FeignException e) {
            log.error("[CU-01] Error al crear paciente en patient-service. DPI: {}, Error: {}", 
                    request.getDpi(), e.getMessage());
            throw new PatientCreationException(
                    "Error al crear registro de paciente en patient-service", e);
        } catch (Exception e) {
            log.error("[CU-01] Error inesperado al crear paciente. DPI: {}, Error: {}", 
                    request.getDpi(), e.getMessage());
            throw new PatientCreationException(
                    "Error inesperado al crear registro de paciente", e);
        }

        // Paso 3: Enviar email con credenciales
        emailService.sendTempPasswordEmail(
                saved.getEmail(), saved.getFirstName(),
                saved.getUsername(), tempPassword);

        log.info("[CU-01] Cuenta paciente creada exitosamente. DPI: {}", request.getDpi());

        return new CreatePatientAccountResponse(
                saved.getId().toString(),
                patientId,
                saved.getUsername(),
                tempPassword,
                "Cuenta creada. Se enviÃ³ la contraseÃ±a temporal al correo del paciente."
        );
    }

    public void logout(String token) {
        blacklistService.addToBlacklist(token);
    }

    private String generateTempPassword() {
        char[] arr = new char[8];
        arr[0] = CHARS.charAt(RANDOM.nextInt(26));
        arr[1] = CHARS.charAt(26 + RANDOM.nextInt(26));
        arr[2] = CHARS.charAt(52 + RANDOM.nextInt(10));
        for (int i = 3; i < 8; i++) {
            arr[i] = CHARS.charAt(RANDOM.nextInt(CHARS.length()));
        }
        for (int i = arr.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char tmp = arr[i]; arr[i] = arr[j]; arr[j] = tmp;
        }
        return new String(arr);
    }

    /**
     * Valida que la fecha de nacimiento tenga formato YYYY-MM-DD y sea una fecha pasada.
     */
    private void validateBirthDate(String birthDate) {
        if (birthDate == null || birthDate.isBlank()) {
            throw new InvalidMedicalDataException("La fecha de nacimiento es requerida");
        }

        try {
            LocalDate date = LocalDate.parse(birthDate, DateTimeFormatter.ISO_LOCAL_DATE);
            if (date.isAfter(LocalDate.now()) || date.isEqual(LocalDate.now())) {
                throw new InvalidMedicalDataException("La fecha de nacimiento debe ser en el pasado");
            }
        } catch (DateTimeParseException e) {
            throw new InvalidMedicalDataException("Formato de fecha invÃ¡lido. Use YYYY-MM-DD");
        }
    }

    /**
     * Valida que el gÃ©nero sea "M" o "F".
     */
    private void validateGender(String gender) {
        if (gender == null || gender.isBlank()) {
            throw new InvalidMedicalDataException("El gÃ©nero es requerido");
        }

        if (!gender.equals("M") && !gender.equals("F")) {
            throw new InvalidMedicalDataException("El gÃ©nero debe ser M o F");
        }
    }
}
