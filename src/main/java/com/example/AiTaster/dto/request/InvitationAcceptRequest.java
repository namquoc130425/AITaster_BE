package com.example.AiTaster.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvitationAcceptRequest {

    // Expert phải tick/chấp nhận điều khoản thì mới accept được lời mời.
    @NotNull(message = "FIELD_REQUIRED")
    @AssertTrue(message = "EXPERT_TERMS_REQUIRED")
    Boolean expertAcceptedTerms;

}
