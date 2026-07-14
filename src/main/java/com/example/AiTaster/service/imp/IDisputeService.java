package com.example.AiTaster.service.imp;

import com.example.AiTaster.dto.request.CreateDisputeRequest;
import com.example.AiTaster.dto.request.DisputeFilterRequest;
import com.example.AiTaster.dto.request.ResolveDisputeRequest;
import com.example.AiTaster.dto.response.DisputeResponse;
import com.example.AiTaster.dto.response.PageResponse;

public interface IDisputeService {

    DisputeResponse create(Long projectId, CreateDisputeRequest request);

    PageResponse<DisputeResponse> filterAdmin(DisputeFilterRequest request);

    DisputeResponse resolve(Long disputeId, ResolveDisputeRequest request);
}
