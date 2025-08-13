package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.FamilyMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {
}