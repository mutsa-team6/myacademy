package com.project.myacademy.repository;

import com.project.myacademy.domain.entity.WaitingList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WaitingListRepository extends JpaRepository<WaitingList, Long> {
}
