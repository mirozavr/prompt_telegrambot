package com.prompt_telegram.repository;

import com.prompt_telegram.entity.StableDiffusionQueryes;
import org.springframework.data.repository.CrudRepository;

public interface StableDiffusionQueryRepository extends CrudRepository<StableDiffusionQueryes, Long> {
}
