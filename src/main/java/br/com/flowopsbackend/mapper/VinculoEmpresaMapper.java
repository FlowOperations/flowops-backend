package br.com.flowopsbackend.mapper;

import br.com.flowopsbackend.dto.response.VinculoEmpresaResponse;
import br.com.flowopsbackend.model.VinculoEmpresaEntity;
import org.springframework.stereotype.Component;

@Component
public class VinculoEmpresaMapper {

    public VinculoEmpresaResponse toResponse(VinculoEmpresaEntity vinculo) {
        return new VinculoEmpresaResponse(
                vinculo.getId(),
                vinculo.getUsuario().getId(),
                vinculo.getUsuario().getNomeCompleto(),
                vinculo.getUsuario().getEmail(),
                vinculo.getEmpresa().getId(),
                vinculo.getEmpresa().getNomeFantasia(),
                vinculo.getCargo(),
                vinculo.getPapel(),
                vinculo.getStatus(),
                vinculo.getSolicitadoEm(),
                vinculo.getAnalisadoEm(),
                vinculo.isAtivo()
        );
    }
}
