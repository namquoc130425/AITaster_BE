package com.example.AiTaster.repository;

import com.example.AiTaster.constant.ReportStatus;
import com.example.AiTaster.entity.Report;
import com.example.AiTaster.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepo extends JpaRepository<Report, Long> {

    List<Report> findByReporter(User reporter);

    List<Report> findByReportedUser(User reportedUser);

    List<Report> findByReportStatus(ReportStatus reportStatus);
}