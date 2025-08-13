package com.leo.pillpathbackend.service.impl;

import com.leo.pillpathbackend.dto.AddMemberDTO;
import com.leo.pillpathbackend.entity.FamilyMember;
import com.leo.pillpathbackend.repository.FamilyMemberRepository;
import com.leo.pillpathbackend.service.FamilyMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class FamilyMemberServiceImpl implements FamilyMemberService {

    private final FamilyMemberRepository familyMemberRepository;

    @Autowired
    public FamilyMemberServiceImpl(FamilyMemberRepository familyMemberRepository) {
        this.familyMemberRepository = familyMemberRepository;
    }

    @Override
    public FamilyMember addMember(AddMemberDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long customerId = (Long) auth.getPrincipal();

        FamilyMember member = new FamilyMember();
        member.setName(dto.getName());
        member.setRelation(dto.getRelation());
        member.setAge(dto.getAge());
        member.setProfilePicture(dto.getProfilePicture());
        member.setEmail(dto.getEmail());
        member.setPhone(dto.getPhone());
        member.setLastPrescriptionDate(dto.getLastPrescriptionDate());
        member.setActivePrescriptions(dto.getActivePrescriptions());
        member.setTotalPrescriptions(dto.getTotalPrescriptions());
        member.setAllergies(dto.getAllergies());
        member.setBloodType(dto.getBloodType());
        member.setMedicalConditions(dto.getMedicalConditions());
        member.setCurrentMedications(dto.getCurrentMedications());

        // Use userId instead of customerId
        member.setUserId(customerId);

        return familyMemberRepository.save(member);
    }
}