package com.gachiganjik.gachiganjik_server.domain.album.repository;

import com.gachiganjik.gachiganjik_server.domain.album.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByCategoryNameIn(List<String> categoryNames);
}