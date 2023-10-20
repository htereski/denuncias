package com.denuncias.models.repositories;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.denuncias.models.daos.ComentarioDAO;
import com.denuncias.models.daos.DenunciaDAO;
import com.denuncias.models.daos.UsuarioDAO;
import com.denuncias.models.entities.Comentario;
import com.denuncias.models.entities.Denuncia;
import com.denuncias.models.entities.TipoDenuncia;
import com.denuncias.models.entities.TipoStatus;
import com.denuncias.models.entities.Usuario;
import com.github.hugoperlin.results.Resultado;

public class DenunciaRepositoryImpl implements DenunciaRepository {

    private DenunciaDAO denunciaDAO;

    private UsuarioDAO usuarioDAO;

    private ComentarioDAO comentarioDAO;

    public DenunciaRepositoryImpl(DenunciaDAO denunciaDAO, UsuarioDAO usuarioDAO, ComentarioDAO comentarioDAO) {
        this.denunciaDAO = denunciaDAO;
        this.usuarioDAO = usuarioDAO;
        this.comentarioDAO = comentarioDAO;
    }

    @Override
    public Resultado cadastrar(Usuario aluno, String titulo, String descricao, String local, TipoDenuncia tipo,
            TipoStatus status, LocalDate data, LocalTime hora) {

        if (titulo.isBlank() || titulo.isEmpty()) {
            return Resultado.erro("Titulo inválido");
        }

        if (descricao.isBlank() || descricao.isEmpty()) {
            return Resultado.erro("Descrição inválida");
        }

        if (local.isBlank() || local.isEmpty()) {
            return Resultado.erro("Local inválido");
        }

        if (aluno == null) {
            return Resultado.erro("Usuário inválido");
        }

        if (tipo == null) {
            return Resultado.erro("Tipo inválido");
        }

        if (status == null) {
            return Resultado.erro("Status inválido");
        }

        if (data == null) {
            return Resultado.erro("Data inválida");
        }

        if (hora == null) {
            return Resultado.erro("Hora inválida");
        }

        Denuncia denuncia = new Denuncia(aluno, titulo, descricao, local, tipo, status, null, data, hora);

        return denunciaDAO.criar(denuncia);
    }

    @Override
    public Resultado abertas() {
        return denunciaDAO.getAbertas();
    }

    @Override
    public Resultado porUsuario(int usuarioId) {

        Resultado r1 = denunciaDAO.getByUsuarioId(usuarioId);

        if (r1.foiErro()) {
            return Resultado.erro(r1.getMsg());
        }

        List<Denuncia> denuncias;

        if (r1.foiSucesso()) {
            denuncias = (List) r1.comoSucesso().getObj();

            if (denuncias.size() == 0) {
                return Resultado.erro("Nenhuma denúncia registrada!");
            }
        }

        denuncias = (List) r1.comoSucesso().getObj();

        Resultado r2 = usuarioDAO.getById(usuarioId);

        if (r2.foiErro()) {
            return Resultado.erro(r2.getMsg());
        }

        Usuario aluno = (Usuario) r2.comoSucesso().getObj();

        for (Denuncia denuncia : denuncias) {

            Resultado r3 = comentarioDAO.getByDenunciaId(denuncia.getId());

            if (r3.foiErro()) {
                return Resultado.erro(r3.getMsg());
            }

            List<Comentario> comentarios = (List) r3.comoSucesso().getObj();

            for (Comentario comentario : comentarios) {
                Resultado r4 = usuarioDAO.getByComentarioId(comentario.getId());

                if (r4.foiErro()) {
                    return Resultado.erro(r4.getMsg());
                }

                Usuario moderador = (Usuario) r4.comoSucesso().getObj();

                comentario.setModerador(moderador);
            }

            denuncia.setComentarios(comentarios);
            denuncia.setAluno(aluno);
        }

        return Resultado.sucesso("Denúncias abertas", denuncias);
    }
}
