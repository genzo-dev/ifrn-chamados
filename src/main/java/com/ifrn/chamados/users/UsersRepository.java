package com.ifrn.chamados.users;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ifrn.chamados.common.enums.UserType;

public interface UsersRepository extends JpaRepository<Users, Long>{
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByRegistrationNumber(String registrationNumber);

    @Query("""
        SELECT u FROM Users u
        WHERE (CAST(:name AS string) IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%')))
          AND (:userType IS NULL OR u.userType = :userType)
          AND (:departmentId IS NULL OR u.department.id = :departmentId)
    """)
    Page<Users> findWithFilters(
        @Param("name") String name,
        @Param("userType") UserType userType,
        @Param("departmentId") Long departmentId,
        Pageable pageable
    );
}
