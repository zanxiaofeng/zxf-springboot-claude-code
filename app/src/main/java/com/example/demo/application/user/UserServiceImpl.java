package com.example.demo.application.user;

import com.example.demo.application.user.dto.CreateUserRequest;
import com.example.demo.application.user.dto.UserResponse;
import com.example.demo.application.user.mapper.UserMapper;
import com.example.demo.domain.common.BusinessException;
import com.example.demo.domain.user.User;
import com.example.demo.domain.user.UserRepository;
import com.example.demo.interfaces.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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

    /**
     * {@inheritDoc}
     * <p>Validates email uniqueness before creation.</p>
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
        return userMapper.toResponse(saved);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<UserResponse> findUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toResponse);
    }

    /** {@inheritDoc} */
    @Override
    public Page<UserResponse> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toResponse);
    }

    /**
     * {@inheritDoc}
     * <p>Fully replaces user data with the request.</p>
     *
     * @throws BusinessException if user not found
     */
    @Override
    @Transactional
    public UserResponse updateUser(Long id, CreateUserRequest request) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, id));
        existing.update(User.builder()
                .username(request.username())
                .email(request.email())
                .password(request.password())
                .build());
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
        if (!userRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, id);
        }
        userRepository.deleteById(id);
    }
}
