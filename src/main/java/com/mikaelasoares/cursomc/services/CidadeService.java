package com.mikaelasoares.cursomc.services;

import com.mikaelasoares.cursomc.domain.Cidade;
import com.mikaelasoares.cursomc.repositories.CidadeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CidadeService {

    @Autowired
    private CidadeRepository repo;

    public List<Cidade> findByEstado(Integer estadoId){
        return repo.findCidades(estadoId);
    }
}
