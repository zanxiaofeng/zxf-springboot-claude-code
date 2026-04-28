package com.example.demo.infrastructure.persistence;

import com.example.demo.domain.user.User;
import com.example.demo.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adapter that bridges the {@link UserRepository} domain interface
 * to the JPA {@link UserJpaRepository} implementation.
 *
 * @author Demo Team
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class UserJpaAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;

    /** {@inheritDoc} */
    @Override
    public User save(User user) {
        return jpaRepository.save(user);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email);
    }

    /** {@inheritDoc} */
    @Override
    public Page<User> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }

    /** {@inheritDoc} */
    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    /** {@inheritDoc} */
    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    /** {@inheritDoc} */
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
