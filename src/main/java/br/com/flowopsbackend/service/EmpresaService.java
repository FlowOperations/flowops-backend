package br.com.flowopsbackend.service;

import br.com.flowopsbackend.dto.request.EmpresaRequest;
import br.com.flowopsbackend.dto.response.EmpresaResponse;
import br.com.flowopsbackend.model.Empresa;
import br.com.flowopsbackend.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    @Transactional
    public EmpresaResponse criar(EmpresaRequest request) {
        validarCnpjDisponivel(request.cnpj());
        Empresa empresa = new Empresa(
                request.razaoSocial(),
                request.nomeFantasia(),
                request.cnpj(),
                request.ativa()
        );
        return EmpresaResponse.from(empresaRepository.save(empresa));
    }

    public List<EmpresaResponse> listar() {
        return empresaRepository.findAll()
                .stream()
                .map(EmpresaResponse::from)
                .toList();
    }

    public EmpresaResponse buscarPorId(Long id) {
        return EmpresaResponse.from(buscarEntidade(id));
    }

    @Transactional
    public EmpresaResponse atualizar(Long id, EmpresaRequest request) {
        Empresa empresa = buscarEntidade(id);
        validarCnpjDisponivel(request.cnpj(), id);
        empresa.atualizar(
                request.razaoSocial(),
                request.nomeFantasia(),
                request.cnpj(),
                request.ativa()
        );
        return EmpresaResponse.from(empresa);
    }

    @Transactional
    public void excluir(Long id) {
        Empresa empresa = buscarEntidade(id);
        empresaRepository.delete(empresa);
    }

    private Empresa buscarEntidade(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Empresa não encontrada"
                ));
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
        return new ResponseStatusException(HttpStatus.CONFLICT, "CNPJ já cadastrado");
    }
}
