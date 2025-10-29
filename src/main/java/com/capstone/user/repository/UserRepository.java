package com.capstone.user.repository;

import com.capstone.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** 사용자 이름으로 조회 */
    Optional<User> findByUsername(String username);

    /** 이메일로 사용자 조회 */
    Optional<User> findByEmail(String email);

    /** 전화번호로 사용자 조회 */
    Optional<User> findByPhone(String phone);

    /** OAuth2 제공자와 제공자 ID로 사용자 조회 */
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

}
