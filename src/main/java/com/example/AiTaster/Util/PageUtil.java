package com.example.AiTaster.Util;

import com.example.AiTaster.dto.request.PageRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.query.SortDirection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

//hàm này như kiểu validae dữ liệu
// nếu dữ liệu nào null bên FE thì sẽ lấy dữ liệu bên này để set vào !!!
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PageUtil {
    public static Pageable createPageable(PageRequest request) {
     //người dùng không truyền phân trang hệ thống sẽ tự tạo request mặc định khi thiệt lập trong pageRequest
     if(request == null) {
           request = PageRequest.builder().build();
     }

     // nếu bên fe truyền về page âm thì sẽ mặc định là page 0 trang đầu tiên
     int page = Math.max(request.getPage(),0);

     int size = request.getSize();

     // nếu FE truyền size < 0 thì sẽ lấy mặc định là 10
     if(size <= 0) {
         size = 10;
     }
     // nhiều hơn 50 thì chỉ có ra dữ liệu là 50 thôi
     if(size > 50 ) {
         size = 50;
     }

        String sortBy = request.getSortBy();
      // nếu fe không truyền dữ liệu sort thì be sẽ sort theo createAt
        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "createAt";
        }
        // neu Sort là Ascending thì sort theo tăng , không thì ngược lại
        Sort sort = request.getSortDirection() == SortDirection.ASCENDING
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        return org.springframework.data.domain.PageRequest.of(
                page,
                size,
                sort
        );
    }
}
