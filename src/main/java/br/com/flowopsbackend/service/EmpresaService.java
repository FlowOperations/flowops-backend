package br.com.flowopsbackend.service;

import br.com.flowopsbackend.dto.request.EmpresaRequest;
import br.com.flowopsbackend.dto.response.EmpresaResponse;
import br.com.flowopsbackend.mapper.EmpresaMapper;
import br.com.flowopsbackend.model.EmpresaEntity;
import br.com.flowopsbackend.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final EmpresaMapper empresaMapper;

    @Transactional
    public EmpresaResponse criar(EmpresaRequest request) {
        validarCnpjDisponivel(request.cnpj());
        EmpresaEntity empresaEntity = empresaMapper.toEntity(request);
        return empresaMapper.toResponse(empresaRepository.save(empresaEntity));
    }

    public List<EmpresaResponse> listar() {
        return empresaRepository.findAll()
                .stream()
                .map(empresaMapper::toResponse)
                .toList();
    }

    public EmpresaResponse buscarPorId(Long id) {
        return empresaMapper.toResponse(buscarEntidade(id));
    }

    @Transactional
    public EmpresaResponse atualizar(Long id, EmpresaRequest request) {
        EmpresaEntity empresaEntity = buscarEntidade(id);
        validarCnpjDisponivel(request.cnpj(), id);
        empresaMapper.updateEntity(empresaEntity, request);
        return empresaMapper.toResponse(empresaEntity);
    }

    @Transactional
    public void excluir(Long id) {
        EmpresaEntity empresaEntity = buscarEntidade(id);
        empresaRepository.delete(empresaEntity);
    }

    private EmpresaEntity buscarEntidade(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada!"));
    }

    private void validarCnpjDisponivel(String cnpj) {
        if (empresaRepository.existsByCnpj(cnpj)) {
            throw cnpjDuplicado();
        }
    }

    private void validarCnpjDisponivel(String cnpj, Long id) {
        if (empresaRepository.existsByCnpjAndIdNot(cnpj, id)) {
            throw cnpjDuplicado();
        }
    }

    private ResponseStatusException cnpjDuplicado() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "CNPJ já cadastrado!");
    }
}
