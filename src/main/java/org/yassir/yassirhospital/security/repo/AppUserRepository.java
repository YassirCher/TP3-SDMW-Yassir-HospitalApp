package org.yassir.yassirhospital.security.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.yassir.yassirhospital.security.entities.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, String> {
    AppUser findByUsername(String username);
}
