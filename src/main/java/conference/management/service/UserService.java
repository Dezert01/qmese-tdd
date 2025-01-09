package conference.management.service;

import conference.management.mapper.LectureMapper;
import conference.management.mapper.UserMapper;
import conference.management.model.Lecture;
import conference.management.model.LectureRequest;
import conference.management.model.User;
import conference.management.repository.LectureRepository;
import conference.management.repository.UserRepository;
import conference.management.repository.entity.LectureEntity;
import conference.management.repository.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final LectureRepository lectureRepository;
    private final UserMapper userMapper;
    private final LectureMapper lectureMapper;

    
    public List<User> obtainRegisteredUsers() {
        return userRepository.findAll().stream()
            .filter(user -> !user.getLectures().isEmpty())
            .map(userMapper::toUser)
            .toList();
    }

    public List<User> obtainAllUsers() {
        return userRepository.findAll().stream()
            .map(userMapper::toUser)
            .toList();
    }

    public List<Lecture> obtainLecturesByUser(String login) {
        Optional<UserEntity> possibleUser = userRepository.findByLogin(login);
        if (possibleUser.isEmpty()) {
            throw new IllegalArgumentException("Invalid login provided.");
        }
        return possibleUser.get().getLectures().stream()
            .map(lectureMapper::toLecture)
            .toList();
    }

    @Transactional
    public User updateUser(String login, String newEmail) {
        // Find the user by login
        UserEntity userEntity = findUser(login);
    
        // Check if the new email is already in use
        Optional<UserEntity> userWithEmail = userRepository.findByEmail(newEmail);
        if (userWithEmail.isPresent() && !userWithEmail.get().getLogin().equals(login)) {
            throw new IllegalArgumentException("The email is already in use.");
        }
    
        // Update the user's email
        userEntity.setEmail(newEmail);
    
        // Save the updated user
        UserEntity updatedUser = userRepository.save(userEntity);
    
        // Return the updated user mapped to the model
        return userMapper.toUser(updatedUser);
    }

    @Transactional
    public void registerForLecture(String login, LectureRequest lectureRequest) {
        // Find the user by login
        UserEntity userEntity = findUser(login);
    
        // Find the lecture by pathNumber and lectureNumber
        LectureEntity lectureEntity = findLecture(lectureRequest.pathNumber(), lectureRequest.lectureNumber());
    
        // Ensure the lecture has available capacity
        if (lectureEntity.getUsers().size() >= lectureEntity.getCapacity()) {
            throw new IllegalArgumentException("The lecture is already full.");
        }
    
        // Check if the user is already registered for this lecture
        if (lectureEntity.getUsers().contains(userEntity)) {
            throw new IllegalArgumentException("User is already registered for this lecture.");
        }
    
        // Add the user to the lecture
        lectureEntity.getUsers().add(userEntity);
        userEntity.getLectures().add(lectureEntity);
    
        // Save the updated entities
        userRepository.save(userEntity);
        lectureRepository.save(lectureEntity);
    }

    @Transactional
    public void cancelReservation(String login, LectureRequest lectureRequest) {
        UserEntity userEntity = findUser(login);
        LectureEntity lectureEntity = findLecture(lectureRequest.pathNumber(), lectureRequest.lectureNumber());

        Set<LectureEntity> lectures = userEntity.getLectures();
        lectures.remove(lectureEntity);
        userEntity.setLectures(lectures);
        userRepository.save(userEntity);
    }

    private LectureEntity findLecture(Integer pathNumber, Integer lectureNumber) {
        Optional<LectureEntity> possibleLecture = lectureRepository.findByPathNumberAndLectureNumber(pathNumber, lectureNumber);
        if (possibleLecture.isEmpty()) {
            throw new IllegalArgumentException("Invalid path number or lecture number provided.");
        }
        return possibleLecture.get();
    }

    private UserEntity findUser(String login) {
        Optional<UserEntity> possibleUserByLogin = userRepository.findByLogin(login);
        if (possibleUserByLogin.isEmpty()) {
            throw new IllegalArgumentException("Invalid login provided.");
        }
        return possibleUserByLogin.get();
    }
    
}
