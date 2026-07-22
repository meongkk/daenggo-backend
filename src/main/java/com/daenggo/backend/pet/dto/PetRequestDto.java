package com.daenggo.backend.pet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PetRequestDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Create {

        @NotBlank
        @Size(max = 50)
        private String name;

        @NotNull
        private Long breedId;

        @NotNull
        @DecimalMin("0.01")
        @Digits(integer = 3, fraction = 2)
        private BigDecimal weight;

        @NotBlank
        @Size(max = 20)
        private String size;

        @Size(max = 500)
        private String profileImageUrl;

        @Size(max = 50)
        private String registrationNumber;

        @Size(max = 50)
        private String vaccine;

        @Size(max = 50)
        private String breedText;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Update {

        @Size(max = 50)
        private String name;

        private Long breedId;

        @DecimalMin("0.01")
        @Digits(integer = 3, fraction = 2)
        private BigDecimal weight;

        @Size(max = 20)
        private String size;

        @Size(max = 500)
        private String profileImageUrl;

        @Size(max = 50)
        private String registrationNumber;

        @Size(max = 50)
        private String vaccine;

        @Size(max = 50)
        private String breedText;
    }
}
