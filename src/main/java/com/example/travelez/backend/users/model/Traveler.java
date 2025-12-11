package com.example.travelez.backend.users.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "traveler")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue("TRAVELER")
public class Traveler extends User {
}
