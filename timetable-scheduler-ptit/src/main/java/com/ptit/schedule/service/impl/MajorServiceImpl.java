package com.ptit.schedule.service.impl;

import com.ptit.schedule.dto.MajorResponse;
import com.ptit.schedule.dto.SubjectResponse;
import com.ptit.schedule.repository.MajorRepository;
import com.ptit.schedule.service.MajorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor

public class MajorServiceImpl implements MajorService {
    private final MajorRepository majorRepository;

    @Override
    public List<MajorResponse> getAllMajors() {
        return majorRepository.findAll()
                .stream()
                .map(MajorResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
