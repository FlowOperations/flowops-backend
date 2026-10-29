package br.com.flowopsbackend.model.enums;

public enum TipoAcessoUsuario {
    ADMIN_FLOWOPS,
    GESTOR,
    COORDENADOR,
    FUNCIONARIO;

    public PerfilSistema perfilSistema() {
        return this == ADMIN_FLOWOPS ? PerfilSistema.ADMIN_FLOWOPS : PerfilSistema.USUARIO;
    }

    public PapelEmpresa papelEmpresa() {
        return switch (this) {
            case ADMIN_FLOWOPS -> null;
            case GESTOR -> PapelEmpresa.GESTOR;
            case COORDENADOR -> PapelEmpresa.COORDENADOR;
            case FUNCIONARIO -> PapelEmpresa.FUNCIONARIO;
        };
    }
}
