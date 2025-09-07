package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.AddMemberDTO;
import com.leo.pillpathbackend.entity.FamilyMember;
import com.leo.pillpathbackend.service.FamilyMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/members")
public class FamilyMemberController {

    private final FamilyMemberService familyMemberService;

    @Autowired
    public FamilyMemberController(FamilyMemberService familyMemberService) {
        this.familyMemberService = familyMemberService;
    }

    @PostMapping("/family-members")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> addFamilyMember(@RequestBody AddMemberDTO dto) {
        try {
            FamilyMember saved = familyMemberService.addMember(dto);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace(); // Add this for debugging
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "type", e.getClass().getSimpleName()
            ));
        }
    }

    @DeleteMapping("/family-members/{memberId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> deleteFamilyMember(@PathVariable Long memberId) {
        try {
            familyMemberService.deleteMember(memberId);
            return ResponseEntity.ok(Map.of("message", "Family member deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "type", e.getClass().getSimpleName()
            ));
        }
    }

    @GetMapping("/family-members")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getFamilyMembers() {
        try {
            List<FamilyMember> familyMembers = familyMemberService.getCurrentUserFamilyMembers();
            return ResponseEntity.ok(familyMembers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "type", e.getClass().getSimpleName()
            ));
        }
    }

    @GetMapping("/debug-auth")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> debugAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(Map.of(
                "userId", auth.getPrincipal(),
                "authorities", auth.getAuthorities()
        ));
    }
}