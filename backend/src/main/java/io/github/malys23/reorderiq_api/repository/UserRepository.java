package io.github.malys23.reorderiq_api.repository;

import io.github.malys23.reorderiq_api.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Integer> {
}
