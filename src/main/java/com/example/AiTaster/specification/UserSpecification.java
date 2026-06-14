package com.example.AiTaster.specification;

import com.example.AiTaster.constant.Role;
import com.example.AiTaster.constant.UserStatus;
import com.example.AiTaster.dto.request.Admin.SubUserFilterRequest;
import com.example.AiTaster.dto.request.Admin.UserFilterRequest;
import com.example.AiTaster.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {
    private UserSpecification() {
    }

    public static Specification<User> filter(UserFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            SubUserFilterRequest filter = null;

            if (request != null) {
                filter = request.getFilter();
            }

            if (request != null && request.getSearch() != null && !request.getSearch().isBlank()) {
                String keyword = "%" + request.getSearch().trim().toLowerCase() + "%";

                Predicate searchByFullName = cb.like(cb.lower(root.get("fullName")), keyword);
                Predicate searchByEmail = cb.like(cb.lower(root.get("email")), keyword);
                Predicate searchByUsername = cb.like(cb.lower(root.get("username")), keyword);
                Predicate searchByPhone = cb.like(cb.lower(root.get("phone")), keyword);

                predicates.add(cb.or(
                        searchByFullName,
                        searchByEmail,
                        searchByUsername,
                        searchByPhone
                ));
            }

            if (filter != null) {
                Role role = filter.getRole();
                UserStatus userStatus = filter.getUserStatus();

                if (role != null) {
                    predicates.add(cb.equal(root.get("role"), role));
                }

                if (userStatus != null) {
                    predicates.add(cb.equal(root.get("userStatus"), userStatus));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
