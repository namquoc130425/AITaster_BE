package com.example.AiTaster.repository;

import com.example.AiTaster.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


//CrudRepository<RefreshToken, String> cho phép save, findById, saveAll.
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {

    List<RefreshToken> findAllByUserId(Long userId);

}
