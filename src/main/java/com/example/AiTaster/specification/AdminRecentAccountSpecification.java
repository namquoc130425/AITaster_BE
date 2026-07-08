package com.example.AiTaster.specification;

import com.example.AiTaster.constant.Role;
import com.example.AiTaster.constant.UserStatus;
import com.example.AiTaster.dto.request.AdminDashboard.AdminRecentAccountFilterRequest;
import com.example.AiTaster.dto.request.AdminDashboard.SubAdminRecentAccountFilterRequest;
import com.example.AiTaster.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdminRecentAccountSpecification {

    private AdminRecentAccountSpecification() {
    }

    public static Specification<User> filter(AdminRecentAccountFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            SubAdminRecentAccountFilterRequest filter = null;

            if (request != null) {
                filter = request.getFilter();
            }

            /*
             * Search chung:
             * - username
             * - fullName
             * - email
             * - phone
             */
            if (request != null && request.getSearch() != null && !request.getSearch().isBlank()) {
                String keyword = "%" + request.getSearch().trim().toLowerCase() + "%";

                Predicate searchByUsername =
                        cb.like(cb.lower(root.get("username")), keyword);

                Predicate searchByFullName =
                        cb.like(cb.lower(root.get("fullName")), keyword);

                Predicate searchByEmail =
                        cb.like(cb.lower(root.get("email")), keyword);

                Predicate searchByPhone =
                        cb.like(cb.lower(root.get("phone")), keyword);

                predicates.add(cb.or(
                        searchByUsername,
                        searchByFullName,
                        searchByEmail,
                        searchByPhone
                ));
            }

            /*
             * Recent account:
             * filter.minutes == null => mặc định 30 phút
             * filter.minutes <= 0    => bỏ filter thời gian
             */
            Integer minutes = null;

            if (filter != null) {
                minutes = filter.getMinutes();
            }

            if (minutes == null) {
                minutes = 30;
            }

            if (minutes > 0) {
                LocalDateTime since = LocalDateTime.now().minusMinutes(minutes);

                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("createAt"),
                                since
                        )
                );
            }

            if (filter != null) {
                Role role = filter.getRole();

                if (role != null) {
                    predicates.add(
                            cb.equal(
                                    root.get("role"),
                                    role
                            )
                    );
                }

                UserStatus userStatus = filter.getUserStatus();

                if (userStatus != null) {
                    predicates.add(
                            cb.equal(
                                    root.get("userStatus"),
                                    userStatus
                            )
                    );
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}