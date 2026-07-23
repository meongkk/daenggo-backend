package com.daenggo.backend.pet.dto;

import com.daenggo.backend.pet.entity.Breed;
import com.daenggo.backend.pet.entity.Pet;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PetResponseDto {

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Summary {

        private final Long petId;
        private final String name;
        private final String profileImageUrl;

        public static Summary from(final Pet pet) {
            return new Summary(
                    pet.getId(),
                    pet.getName(),
                    pet.getImage()
            );
        }
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Detail {

        private final Long petId;
        private final String name;
        private final Long breedId;
        private final String breedName;
        private final String breedText;
        private final BigDecimal weight;
        private final String size;
        private final String profileImageUrl;
        private final String registrationNumber;
        private final String vaccine;
        private final boolean primary;
        private final LocalDateTime createdAt;

        public static Detail from(final Pet pet) {
            final Breed breed = pet.getBreed();
            return new Detail(
                    pet.getId(),
                    pet.getName(),
                    breed == null ? null : breed.getId(),
                    breed == null ? null : breed.getName(),
                    pet.getBreedText(),
                    pet.getWeight(),
                    pet.getSize(),
                    pet.getImage(),
                    pet.getRegistrationNumber(),
                    pet.getVaccine(),
                    pet.isPrimary(),
                    pet.getCreatedAt()
            );
        }
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class IdCard {

        private final Long petId;
        private final String name;
        private final String breedName;
        private final String breedText;
        private final BigDecimal weight;
        private final String size;
        private final String profileImageUrl;
        private final String registrationNumber;
        private final String vaccine;
        private final String ownerNickname;
        private final String qrCode;

        public static IdCard from(final Pet pet, final String qrCode) {
            final Breed breed = pet.getBreed();
            return new IdCard(
                    pet.getId(),
                    pet.getName(),
                    breed == null ? null : breed.getName(),
                    pet.getBreedText(),
                    pet.getWeight(),
                    pet.getSize(),
                    pet.getImage(),
                    pet.getRegistrationNumber(),
                    pet.getVaccine(),
                    pet.getUser().getNickname(),
                    qrCode
            );
        }
    }
}
