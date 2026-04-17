package com.medflow.auth.service;

import com.medflow.auth.domain.Role;
import com.medflow.auth.domain.RoleName;
import com.medflow.auth.domain.User;
import com.medflow.auth.dto.CreatePatientAccountRequest;
import com.medflow.auth.dto.CreatePatientAccountResponse;
import com.medflow.auth.dto.RegisterRequest;
import com.medflow.auth.exception.EmailAlreadyExistsException;
import com.medflow.auth.exception.InvalidActivationTokenException;
import com.medflow.auth.exception.UsernameAlreadyExistsException;
import com.medflow.auth.repository.RoleRepository;
import com.medflow.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
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

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder,
                       TokenBlacklistService blacklistService,
                       ActivationTokenService activationTokenService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.blacklistService = blacklistService;
        this.activationTokenService = activationTokenService;
    }

    /**
     * CU-00.1: Login por username O correo electrónico.
     */
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

    /**
     * CU-00.2: Auto-registro de paciente desde el portal.
     * Cuenta queda inactiva (active=false) hasta confirmar email.
     * @return token de activación para incluir en el link del correo.
     */
    @Transactional
    public String register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        if (userRepository.findByUsername(request.getDpi()).isPresent()) {
            throw new UsernameAlreadyExistsException(request.getDpi());
        }

        Role patientRole = roleRepository.findByName(RoleName.PATIENT)
                .orElseThrow(() -> new RuntimeException("Rol PATIENT no encontrado en BD"));

        String fullName = buildFullName(request);

        User user = User.builder()
                .username(request.getDpi())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(fullName)
                .active(true)
                .roles(Set.of(patientRole))
                .build();

        userRepository.save(user);
        log.info("[CU-00.2] Cuenta creada y activa. Email: {}", request.getEmail());
        return null;
    }

    /**
     * CU-00.2: Activa la cuenta al hacer clic en el link del correo.
     */
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

    /**
     * CU-01: Admisión crea cuenta de paciente con contraseña temporal.
     * Llamado por patient-service vía HTTP interno.
     */
    @Transactional
    public CreatePatientAccountResponse createPatientAccount(CreatePatientAccountRequest request) {
        if (userRepository.findByUsername(request.getDpi()).isPresent()) {
            throw new UsernameAlreadyExistsException(request.getDpi());
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        Role patientRole = roleRepository.findByName(RoleName.PATIENT)
                .orElseThrow(() -> new RuntimeException("Rol PATIENT no encontrado en BD"));

        String tempPassword = generateTempPassword();

        User user = User.builder()
                .username(request.getDpi())
                .email(request.getEmail())
                .password(passwordEncoder.encode(tempPassword))
                .fullName(request.getFullName())
                .active(true)
                .roles(Set.of(patientRole))
                .build();

        User saved = userRepository.save(user);
        log.info("[CU-01] Cuenta paciente creada. DPI: {}", request.getDpi());

        return new CreatePatientAccountResponse(
                saved.getId().toString(),
                saved.getUsername(),
                tempPassword,
                "Cuenta creada. Entregue la contraseña temporal al paciente."
        );
    }

    /** Logout: invalida el token en la blacklist. */
    public void logout(String token) {
        blacklistService.addToBlacklist(token);
    }

    private String buildFullName(RegisterRequest request) {
        StringBuilder sb = new StringBuilder(request.getFirstName());
        if (request.getSecondName() != null && !request.getSecondName().isBlank())
            sb.append(" ").append(request.getSecondName());
        sb.append(" ").append(request.getFirstLastName());
        if (request.getSecondLastName() != null && !request.getSecondLastName().isBlank())
            sb.append(" ").append(request.getSecondLastName());
        return sb.toString();
    }

    private String generateTempPassword() {
        char[] arr = new char[8];
        arr[0] = CHARS.charAt(RANDOM.nextInt(26));           // mayúscula
        arr[1] = CHARS.charAt(26 + RANDOM.nextInt(26));      // minúscula
        arr[2] = CHARS.charAt(52 + RANDOM.nextInt(10));      // número
        for (int i = 3; i < 8; i++) {
            arr[i] = CHARS.charAt(RANDOM.nextInt(CHARS.length()));
        }
        for (int i = arr.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char tmp = arr[i]; arr[i] = arr[j]; arr[j] = tmp;
        }
        return new String(arr);
    }
}
