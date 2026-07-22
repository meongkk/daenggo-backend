package com.daenggo.backend.pet.service;

import com.daenggo.backend.pet.dto.PetResponseDto;
import com.daenggo.backend.pet.repository.PetRepository;
import com.daenggo.backend.user.entity.User;
import com.daenggo.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetService {

    private final PetRepository petRepository;
    private final UserRepository userRepository;

    public List<PetResponseDto.Summary> getMyPets(final String email) {
        final User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "회원을 찾을 수 없습니다."
                ));

        return petRepository.findAllByUserId(user.getId())
                .stream()
                .map(PetResponseDto.Summary::from)
                .toList();
    }
}
