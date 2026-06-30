package com.example.AiTaster.entity;

import com.example.AiTaster.constant.PaymentMethod;
import com.example.AiTaster.constant.PaymentReferenceType;
import com.example.AiTaster.constant.PaymentStatus;
import com.example.AiTaster.constant.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long paymentTransactionId;

    // ví giữ tiền của Project
    Long projectEscrowId;

    Long expertServiceId;

    // người trả tiền
    Long senderId;

    //người nhận tiền
    Long receiverId;

    //ví nguồn, ví riêng của người dùng - user dùng ví trong app để thanh toán
    Long sourceWalletId;

    //ví đích, ví của hệ thống
    Long targetWalletId;

    //tiền mua sản phẩm , tiền gốc mà user bỏ ra
    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal grossAmount;

    //tiền phí sàn
    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal feeAmount;

    //tiền thực tế đã trừ phí sàn đến tay người nhận
    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal netAmount;

//    //tiền thanh toán
//    @Column(nullable = false, precision = 12, scale = 2)
//    BigDecimal amount;

    // loại tiền
    @Column(nullable = false, length = 10)
    String currency;

    // dịch vụ thanh toán nào Project , probosal
    // nghiệp vụ tiền gì để biết xữ lý : giu tien Project , probosal , hoàn tiền project,  v...
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    TransactionType transactionType;

    //Phương thức thanh toán ví , momo , fpt
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    PaymentMethod paymentMethod;

    // trạng thái giao dịch
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    PaymentStatus paymentStatus;

    // dữ liệu của dịch vụ cần thanh toán : projectid =1 ,serviceai =2 , probosla4
    //lấy id object đang được thanh toán
    @Column(nullable = false)
    Long referenceId;

     // từ referenceId mình sẽ biết được thuộc loại object nào : project ,probosal
    //dùng bản này cho nhiều loại thanh toán
    //referencetype :
    // giao dịch này thuộc về object
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    PaymentReferenceType paymentReferenceType;

    //nhà cung cấp thanh toán :SEPAY
    String providerName;

    //mã giao dịch từ SePay gữi về khi có giao dịch
    @Column(unique = true)
    String providerTransactionCode;

    //mã hệ thống tạo cho user thanh toán
    @Column(nullable = false, unique = true, length = 80)
    String paymentCode;

    @Column(columnDefinition = "TEXT")
    String description;

    // nội dung ck Sepay gữi về
    @Column(columnDefinition = "Text")
    String providerContent;

    // time thanh toán thành công
    LocalDateTime paidAt;

    // hạn thanh toán của project trong vòng 24h lấy từ lúc expert accept
    LocalDateTime expiredAt;

    @CreationTimestamp
    LocalDateTime createAt;

    @UpdateTimestamp
    LocalDateTime updateAt;

    @PrePersist
    public void prePersist() {
        if (paymentStatus == null)  paymentStatus = PaymentStatus.PENDING;
        if (currency == null) currency = "VND";
        if (paymentMethod == null) paymentMethod = PaymentMethod.SEPAY;
        if (providerName == null) providerName = "SEPAY";
        if (feeAmount == null)     feeAmount = BigDecimal.ZERO;
    }






}
