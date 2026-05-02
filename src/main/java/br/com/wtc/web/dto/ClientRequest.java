package br.com.wtc.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ClientRequest(

        @NotBlank(message = "Nome é obrigatório") String name,

        @NotBlank(message = "E-mail é obrigatório") @Email(message = "E-mail inválido") String email,

        String phone,

        // "ACTIVE", "INACTIVE", "LEAD" — default LEAD se não informado
        String status,

        Integer score,

        List<String> tags,

        String divisionId,

        String groupId) {
}