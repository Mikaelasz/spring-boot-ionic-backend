package com.mikaelasoares.cursomc.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mikaelasoares.cursomc.domain.Estado;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface EstadoRepository extends JpaRepository<Estado, Integer>{

    @Transactional(readOnly=true)
    public List<Estado> findAllByOrderByNome();
	
}
