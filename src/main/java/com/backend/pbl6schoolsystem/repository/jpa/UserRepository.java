package com.backend.pbl6schoolsystem.repository.jpa;

import com.backend.pbl6schoolsystem.model.entity.UserEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    @Query("SELECT u FROM UserEntity u" +
            " LEFT JOIN FETCH u.role r" +
            " LEFT JOIN FETCH u.school s" +
            " WHERE u.username = :username")
    UserEntity findByUsername(String username);

    @Query("SELECT s FROM UserEntity s" +
            " LEFT JOIN FETCH s.school sch" +
            " WHERE s.role.roleId = :roleId" +
            " AND s.userId = :studentId")
    Optional<UserEntity> findOneById(@Param("studentId") Long studentId, @Param("roleId") Long roleId);

    @Query("SELECT ps.parent FROM ParentStudentEntity ps" +
            " WHERE ps.student.userId = :studentId")
    List<UserEntity> findParentsByStudent(Long studentId);

    @Query("SELECT u FROM UserEntity u" +
            " WHERE u.school.schoolId = :schoolId")
    List<UserEntity> findBySchool(Long schoolId);

    @Query("SELECT u FROM UserEntity u" +
            " WHERE u.email = :email")
    Optional<UserEntity> findOneByEmail(String email);

    @Query("SELECT u FROM UserEntity u" +
            " LEFT JOIN FETCH u.school s" +
            " WHERE u.userId = :schoolAdminId" +
            " AND u.role.roleId = :roleId")
    Optional<UserEntity> findSchoolAdminById(Long schoolAdminId, Long roleId);

    @Query("SELECT u FROM UserEntity u" +
            " WHERE u.school.schoolId = :schoolId" +
            " AND u.email = :email")
    Optional<UserEntity> findByEmailInSchool(String email, Long schoolId);
}