package com.example.AiTaster.specification;

import com.example.AiTaster.constant.ServiceStatus;
import com.example.AiTaster.dto.request.ExpertProduct.ExpertServiceFillerRequest;
import com.example.AiTaster.dto.request.ExpertProduct.SubExpertServiceFilterRequest;
import com.example.AiTaster.entity.ExpertService;
import com.example.AiTaster.entity.Skill;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ExpertServiceSpecification {
    private ExpertServiceSpecification() {
    }

public static Specification<ExpertService> filter(ExpertServiceFillerRequest request) {
    return(root,query,cb) -> {
        // root đại diện cho entity ExpertService.
        // query là câu truy vấn đang được tạo.
        // cb là CriteriaBuilder dùng để tạo điều kiện: equal, like, greaterThan, lessThan...
        List<Predicate> predicates = new ArrayList<>();
        // Chứa danh sách điều kiện.
        SubExpertServiceFilterRequest filter = null;

        if (request != null) {
            filter = request.getFilter();
        }
        // Điều kiện 1: chỉ lấy service đang OPEN.
        predicates.add(cb.equal(root.get("serviceStatus"), ServiceStatus.OPEN));


        // Điều kiện 2: tìm kiếm theo name và description.
        if(request != null && request.getSearch() != null &&  !request.getSearch().isBlank()) {
            // Tạo keyword dạng %dữ liệu người dùng nhập%, bỏ khoảng trắng đầu cuối và viết thường.
            String keyword = "%" + request.getSearch().trim().toLowerCase() + "%";

            // like là tìm gần đúng, lower chuyển thành chữ thường.
            Predicate searchByName = cb.like(cb.lower(root.get("serviceName")),keyword);
            Predicate searchByDescription = cb.like(cb.lower(root.get("serviceDescription")), keyword);
            // or chỉ cần đúng một trong hai điều kiện.
            predicates.add(cb.or(searchByName, searchByDescription));


        }

        if(filter != null) {

            // Lọc theo category.
            if(filter.getCategoryId() != null ) {
                predicates.add(cb.equal(root.get("category").get("categoryId"),filter.getCategoryId()));
            }
            if(filter.getExpertProfileId() != null) {
                predicates.add(cb.equal(root.get("expertProfile").get("expertProfileId"), filter.getExpertProfileId()));
            }
            // Lọc theo skill.
            // Vì expert service có nhiều skill nên phải dùng join.
            //INNER JOIN: Chỉ lấy dữ liệu khớp ở cả 2 bảng.
            if(filter.getSkillIds() != null && !filter.getSkillIds().isEmpty()) {
                Join<ExpertService, Skill> skillJoin = root.join("skills", JoinType.INNER);
                predicates.add(skillJoin.get("skillId").in(filter.getSkillIds()));
                query.distinct(true); // bỏ trùng
            }

            // Lọc theo giá tiền >=.
            if(filter.getFeeFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("serviceFee"),filter.getFeeFrom()));
            }

            // Lọc theo giá tiền <=.
            if(filter.getFeeTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("serviceFee"),filter.getFeeTo()));
            }

        }


        return cb.and(predicates.toArray(new Predicate[0]));

    };

}

}
