package org.yassir.yassirhospital.security.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.yassir.yassirhospital.security.entities.AppRole;

public interface AppRoleRepository extends JpaRepository<AppRole, String> {
}