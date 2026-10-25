package com.gdgoc.studyhub.domain.user.service;

import com.gdgoc.studyhub.domain.user.dto.UserResponse;
import com.gdgoc.studyhub.domain.user.repository.UserRepository;
import com.gdgoc.studyhub.global.exception.BusinessException;
import com.gdgoc.studyhub.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getUser(Long userId) {
        return userRepository.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
