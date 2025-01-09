package conference.management.service;

import conference.management.model.LectureRequest;
import conference.management.model.User;
import conference.management.repository.LectureRepository;
import conference.management.repository.UserRepository;
import conference.management.repository.entity.LectureEntity;
import conference.management.repository.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserService userService;
    private UserRepository userRepository;
    private LectureRepository lectureRepository;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        lectureRepository = mock(LectureRepository.class);
        userService = new UserService(userRepository, lectureRepository, null, null);
    }

    @Test
    void shouldRegisterUserForLectureSuccessfully() {
        // Arrange
        String login = "user1";
        LectureRequest request = new LectureRequest(1, 1);

        UserEntity user = new UserEntity(1, login, "user1@example.com", Set.of());
        LectureEntity lecture = new LectureEntity(1, "Java Basics", 1, 1, null, 5, Set.of());

        when(userRepository.findByLogin(login)).thenReturn(Optional.of(user));
        when(lectureRepository.findByPathNumberAndLectureNumber(1, 1)).thenReturn(Optional.of(lecture));

        // Act
        userService.registerForLecture(login, request);

        // Assert
        assertTrue(lecture.getUsers().contains(user));
        verify(userRepository).save(user);
        verify(lectureRepository).save(lecture);
    }

    @Test
    void shouldFailWhenLectureIsFull() {
        // Arrange
        String login = "user1";
        LectureRequest request = new LectureRequest(1, 1);

        UserEntity user = new UserEntity(1, login, "user1@example.com", Set.of());
        LectureEntity lecture = new LectureEntity(1, "Java Basics", 1, 1, null, 5, Set.of(
            mock(UserEntity.class), mock(UserEntity.class), mock(UserEntity.class), mock(UserEntity.class), mock(UserEntity.class)
        ));

        when(userRepository.findByLogin(login)).thenReturn(Optional.of(user));
        when(lectureRepository.findByPathNumberAndLectureNumber(1, 1)).thenReturn(Optional.of(lecture));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> userService.registerForLecture(login, request));
        assertEquals("The lecture is already full.", exception.getMessage());
    }

    @Test
    void shouldFailWhenUserAlreadyRegisteredForLecture() {
        // Arrange
        String login = "user1";
        LectureRequest request = new LectureRequest(1, 1);

        UserEntity user = new UserEntity(1, login, "user1@example.com", Set.of());
        LectureEntity lecture = new LectureEntity(1, "Java Basics", 1, 1, null, 5, Set.of(user));

        when(userRepository.findByLogin(login)).thenReturn(Optional.of(user));
        when(lectureRepository.findByPathNumberAndLectureNumber(1, 1)).thenReturn(Optional.of(lecture));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> userService.registerForLecture(login, request));
        assertEquals("User is already registered for this lecture.", exception.getMessage());
    }

    @Test
    void shouldFailWhenLectureDoesNotExist() {
        // Arrange
        String login = "user1";
        LectureRequest request = new LectureRequest(1, 1);

        UserEntity user = new UserEntity(1, login, "user1@example.com", Set.of());

        when(userRepository.findByLogin(login)).thenReturn(Optional.of(user));
        when(lectureRepository.findByPathNumberAndLectureNumber(1, 1)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> userService.registerForLecture(login, request));
        assertEquals("Invalid path number or lecture number provided.", exception.getMessage());
    }

    @Test
    void shouldUpdateEmailSuccessfully() {
        // Arrange
        String login = "user1";
        String newEmail = "newuser1@example.com";

        UserEntity user = new UserEntity(1, login, "user1@example.com", Set.of());
        when(userRepository.findByLogin(login)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);

        // Act
        User updatedUser = userService.updateUser(login, newEmail);

        // Assert
        assertEquals(newEmail, user.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void shouldFailToUpdateEmailIfAlreadyTaken() {
        // Arrange
        String login = "user1";
        String newEmail = "alreadytaken@example.com";

        UserEntity user = new UserEntity(1, login, "user1@example.com", Set.of());
        when(userRepository.findByLogin(login)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(newEmail)).thenReturn(Optional.of(new UserEntity()));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> userService.updateUser(login, newEmail));
        assertEquals("The email is already in use.", exception.getMessage());
    }

    @Test
    void shouldFailToUpdateIfUserDoesNotExist() {
        // Arrange
        String login = "user1";
        String newEmail = "newuser1@example.com";

        when(userRepository.findByLogin(login)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> userService.updateUser(login, newEmail));
        assertEquals("Invalid login provided.", exception.getMessage());
    }
}
