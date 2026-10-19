package br.com.flowopsbackend.mapper;

import br.com.flowopsbackend.dto.response.ProjetoMembroResponse;
import br.com.flowopsbackend.dto.response.ProjetoResponse;
import br.com.flowopsbackend.model.ProjetoEntity;
import br.com.flowopsbackend.model.ProjetoMembroEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ProjetoMapper {

    public ProjetoResponse toResponse(ProjetoEntity projeto, BigDecimal progresso) {
        return new ProjetoResponse(
                projeto.getId(),
                projeto.getEmpresa().getId(),
                projeto.getEmpresa().getNomeFantasia(),
                projeto.getGestorResponsavel().getId(),
                projeto.getGestorResponsavel().getNomeCompleto(),
                projeto.getNome(),
                projeto.getDescricao(),
                projeto.getDataInicioPrevista(),
                projeto.getDataFimPrevista(),
                projeto.getStatus(),
                progresso
        );
    }

    public ProjetoMembroResponse toMembroResponse(ProjetoMembroEntity membro) {
        return new ProjetoMembroResponse(
                membro.getId(), membro.getUsuario().getId(), membro.getUsuario().getNomeCompleto(),
                membro.getUsuario().getEmail(), membro.isAtivo(), membro.getConcedidoEm()
        );
    }
}
