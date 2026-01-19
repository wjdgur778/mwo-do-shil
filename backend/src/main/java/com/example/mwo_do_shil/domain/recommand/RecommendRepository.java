package com.example.mwo_do_shil.domain.recommand;

import com.example.mwo_do_shil.domain.store.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecommendRepository extends JpaRepository<Store, Long> {
}
