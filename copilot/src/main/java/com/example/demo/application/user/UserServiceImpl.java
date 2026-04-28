package com.example.demo.application.user;

import com.example.demo.application.user.dto.CreateUserRequest;
import com.example.demo.application.user.dto.UpdateUserRequest;
import com.example.demo.application.user.dto.UserResponse;
import com.example.demo.application.user.mapper.UserMapper;
import com.example.demo.domain.common.BusinessException;
import com.example.demo.domain.common.ErrorCode;
import com.example.demo.domain.downstream.NotificationClient;
import com.example.demo.domain.user.User;
import com.example.demo.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link UserService}.
 * Handles all business logic for user management operations.
 *
 * @author Demo Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final NotificationClient notificationClient;
    private final PasswordEncoder passwordEncoder;

    /**
     * {@inheritDoc}
     * <p>Validates email uniqueness before creation.
     * Sends downstream notification on success (fire-and-forget).</p>
     *
     * @throws BusinessException if email already exists
     */
    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS, request.email());
        }
        User user = userMapper.toEntity(request);
        User saved = userRepository.save(user);
        notificationClient.sendUserCreatedNotification(
                saved.getId(),
                saved.getUsername(),
                saved.getEmail()
        );
        return userMapper.toResponse(saved);
    }

    /** {@inheritDoc} */
    @Override
    public UserResponse findUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, id));
    }

    /** {@inheritDoc} */
    @Override
    public Page<UserResponse> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toResponse);
    }

    /**
     * {@inheritDoc}
     * <p>Updates user data. Password is only changed if provided in the request.</p>
     *
     * @throws BusinessException if user not found or email already taken
     */
    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, id));
        if (!existing.getEmail().equals(request.email())
                && userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS, request.email());
        }
        String encodedPassword = request.password() != null
                ? passwordEncoder.encode(request.password()) : null;
        existing.update(request.username(), request.email(), encodedPassword);
        return userMapper.toResponse(userRepository.save(existing));
    }

    /**
     * {@inheritDoc}
     *
     * @throws BusinessException if user not found
     */
    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, id));
        userRepository.delete(user);
    }
}
