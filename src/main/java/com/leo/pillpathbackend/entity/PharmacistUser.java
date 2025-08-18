// src/main/java/com/leo/pillpathbackend/entity/PharmacistUser.java
package com.leo.pillpathbackend.entity;

import com.leo.pillpathbackend.entity.enums.UserType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("PHARMACIST")
@Getter
@Setter
@NoArgsConstructor
public class PharmacistUser extends User {

    @Override
    public UserType getUserType() {
        return UserType.PHARMACIST;
    }
}