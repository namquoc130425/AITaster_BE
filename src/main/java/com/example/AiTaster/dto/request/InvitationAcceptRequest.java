package com.example.AiTaster.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public class InvitationAcceptRequest {

    // Expert phải tick/chấp nhận điều khoản thì mới accept được lời mời.
    @NotNull(message = "FIELD_REQUIRED")
    @AssertTrue(message = "EXPERT_TERMS_REQUIRED")
    Boolean expertAcceptedTerms;

}
