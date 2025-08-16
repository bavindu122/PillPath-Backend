package com.leo.pillpathbackend.service;

import com.leo.pillpathbackend.dto.AddMemberDTO;
import com.leo.pillpathbackend.entity.FamilyMember;
import java.util.List;

public interface FamilyMemberService {
    FamilyMember addMember(AddMemberDTO dto);
    void deleteMember(Long memberId);
    List<FamilyMember> getFamilyMembersByUserId(Long userId);
    List<FamilyMember> getCurrentUserFamilyMembers();
}