package pet.db.jdbc.service;

import jakarta.annotation.Nullable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import pet.db.jdbc.entity.User;
import pet.db.jdbc.repository.ArticleRepository;
import pet.db.jdbc.repository.AuthorshipOfArticleRepository;
import pet.db.jdbc.repository.UserRepository;
import pet.db.jdbc.tool.exception.DuplicateUserException;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final ArticleRepository articleRepository;

    private final AuthorshipOfArticleRepository authorshipOfArticleRepository;

    @Override
    public User create(User user) throws DuplicateUserException {
        validateUserUniqueness(user, null);
        return userRepository.save(user);
    }

    @Override
    public User updateById(User user, Integer id) throws DuplicateUserException {
        User targetUser = userRepository.findById(id).orElseThrow(NoSuchElementException::new);
        validateUserUniqueness(user, targetUser);
        return userRepository.updateById(user, id);
    }

    @Override
    public void deleteById(Integer id) {
        userRepository.deleteById(id);
    }

    @Override
    public Optional<User> findById(Integer id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public List<Integer> findAuthorIdsByArticleId(Integer articleId) {
        return findAuthorsByArticleId(articleId).stream()
                .map(User::getId)
                .toList();
    }

    @Override
    public List<User> findAuthorsByArticleId(Integer articleId) {
        if (!articleRepository.existsById(articleId)) {
            throw new NoSuchElementException(
                    "Attempt to find list of users by non existent user id = %d".formatted(articleId));
        }
        List<Integer> authorsIds = authorshipOfArticleRepository.findAuthorIdsByArticleId(articleId);
        return findByIds(authorsIds);
    }

    @Override
    public List<User> findByIds(List<Integer> userIds) {
        if (!userIds.isEmpty()) {
            return userRepository.findByIds(userIds);
        }
        return new ArrayList<>();
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public boolean existsById(Integer id) {
        return userRepository.existsById(id);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private void validateUserUniqueness(User user, @Nullable User existingUser) throws DuplicateUserException {
        validateUsernameUniqueness(user, existingUser);
        validateUserEmailUniqueness(user, existingUser);
    }


    private void validateUsernameUniqueness(User user, @Nullable User existingUser) throws DuplicateUserException {
        if (existingUser == null || !Objects.equals(existingUser.getUsername(), user.getUsername())) {
            if (existsByUsername(user.getUsername())) {
                throw new DuplicateUserException("username",
                        "User with username %s already exists.".formatted(user.getUsername()));
            }
        }
    }

    private void validateUserEmailUniqueness(User user, @Nullable User existingUser) throws DuplicateUserException {
        if (existingUser == null || !Objects.equals(existingUser.getEmail(), user.getEmail())) {
            if (existsByEmail(user.getEmail())) {
                throw new DuplicateUserException("email",
                        "User with email %s already exists.".formatted(user.getEmail()));
            }
        }
    }

}
