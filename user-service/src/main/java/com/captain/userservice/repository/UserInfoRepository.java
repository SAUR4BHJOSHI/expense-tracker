package com.captain.userservice.repository;

import com.captain.userservice.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.UUID;

public interface UserInfoRepository extends JpaRepository<UserInfo, UUID> {
	@Modifying
	@Query(value = """
			INSERT INTO user_info (user_id, first_name, last_name, email, profile_status, preferred_currency, created_at, updated_at)
			VALUES (:userId, :firstName, :lastName, :email, 'INCOMPLETE', 'INR', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
			ON CONFLICT (user_id) DO NOTHING
			""", nativeQuery = true)
	int initializeProfile(@Param("userId") UUID userId,
						  @Param("firstName") String firstName,
						  @Param("lastName") String lastName,
						  @Param("email") String email);
}
