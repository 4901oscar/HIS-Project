package com.medflow.auth.controller;

import com.medflow.auth.dto.CreateEmployeeRequest;
import com.medflow.auth.dto.EmployeeResponse;
import com.medflow.auth.dto.UpdateEmployeeRequest;
import com.medflow.auth.service.UserManagementService;
import com.medflow.auth.service.UserManagementService.EmployeeCreationResult;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * CU-02: Administración de personal (solo ADMIN).
 * Base path: /api/users/empleados
 */
@RestController
@RequestMapping("/api/users/empleados")
public class UserManagementController {

    private static final Logger log = LoggerFactory.getLogger(UserManagementController.class);

    private final UserManagementService userManagementService;

    public UserManagementController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    /** CU-02: Registrar nuevo empleado. Devuelve datos + contraseña temporal. */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createEmployee(
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @Valid @RequestBody CreateEmployeeRequest request) {

        userManagementService.requireAdmin(authHeader);
        EmployeeCreationResult result = userManagementService.createEmployee(request, userId);
        log.info("[CU-02] Empleado creado por admin. username={}", result.employee().getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "empleado", result.employee(),
            "contrasenaTemporalParaEntregar", result.temporaryPassword(),
            "mensaje", "Empleado registrado. Entregue la contraseña temporal al trabajador."
        ));
    }

    /** CU-02: Listar empleados. Filtros opcionales: rol, activo. */
    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> listEmployees(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) Boolean activo) {

        userManagementService.requireAdmin(authHeader);
        return ResponseEntity.ok(userManagementService.listEmployees(rol, activo));
    }

    /** CU-02: Obtener empleado por ID. */
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployee(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id) {

        userManagementService.requireAdmin(authHeader);
        return ResponseEntity.ok(userManagementService.getEmployee(id));
    }

    /** CU-02: Actualizar datos del empleado. */
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id,
            @Valid @RequestBody UpdateEmployeeRequest request) {

        userManagementService.requireAdmin(authHeader);
        return ResponseEntity.ok(userManagementService.updateEmployee(id, request, null));
    }

    /** CU-02: Activar / desactivar cuenta del empleado. */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<EmployeeResponse> toggleActive(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id) {

        userManagementService.requireAdmin(authHeader);
        return ResponseEntity.ok(userManagementService.toggleActive(id, null));
    }
}
