package br.com.flowopsbackend.mapper;

import br.com.flowopsbackend.dto.request.EmpresaRequest;
import br.com.flowopsbackend.dto.response.EmpresaResponse;
import br.com.flowopsbackend.model.EmpresaEntity;
import org.springframework.stereotype.Component;

@Component
public class EmpresaMapper {

    public EmpresaEntity toEntity(EmpresaRequest request) {
        return new EmpresaEntity(
                request.razaoSocial(),
                request.nomeFantasia(),
                request.cnpj(),
                request.flgAtivo()
        );
    }

    public EmpresaResponse toResponse(EmpresaEntity empresaEntity) {
        return new EmpresaResponse(
                empresaEntity.getId(),
                empresaEntity.getRazaoSocial(),
                empresaEntity.getNomeFantasia(),
                empresaEntity.getCnpj(),
                empresaEntity.isFlgAtivo()
        );
    }

    public void updateEntity(EmpresaEntity empresaEntity, EmpresaRequest request) {
        empresaEntity.atualizar(
                request.razaoSocial(),
                request.nomeFantasia(),
                request.cnpj(),
                request.flgAtivo()
        );
    }
}
