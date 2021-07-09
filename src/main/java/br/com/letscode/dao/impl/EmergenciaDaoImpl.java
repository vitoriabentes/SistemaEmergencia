package br.com.letscode.dao.impl;

import br.com.letscode.dao.EmergenciaDao;
import br.com.letscode.dominio.Medicamento;
import br.com.letscode.dominio.Paciente;
import jakarta.annotation.PostConstruct;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EmergenciaDaoImpl implements EmergenciaDao {

    private Path path;

    @PostConstruct
    public void init() {
        try {
            path = Paths.get("medicamentos.csv");
            if (!path.toFile().exists()) {
                Files.createFile(path);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Override
    public Medicamento inserirArquivo(Medicamento medicamento) throws IOException {
        write(format(medicamento), StandardOpenOption.APPEND);
        return medicamento;
    }

    private void write(String clienteStr, StandardOpenOption option) throws IOException {
        try(BufferedWriter bf = Files.newBufferedWriter(path, option)){
            bf.flush();
            bf.write(clienteStr);
        }
    }

    @Override
    public List<Medicamento> getAll() throws IOException {
        List<Medicamento> medicamentos;
        try (BufferedReader br = Files.newBufferedReader(path)) {
            medicamentos = br.lines().filter(Objects::nonNull).filter(Predicate.not(String::isEmpty)).map(this::convert).collect(Collectors.toList());
        }
        return medicamentos;
    }

    @Override
    public Optional<Medicamento> findFirstByCpf(String cpf) throws IOException {
        List<Medicamento> medicamentos = getAll();
        return  medicamentos.stream().filter(medicamento -> medicamento.getPaciente().getCpf().equals(cpf)).findFirst();
    }

    @Override
    public List<Medicamento> findByCpf(String cpf) throws IOException {
        List<Medicamento> medicamentos = getAll();
        return  medicamentos.stream().filter(medicamento -> medicamento.getPaciente().getCpf().equals(cpf)).collect(Collectors.toList());
    }

    @Override
    public Optional<Medicamento> findByName(String principioAtivo) throws IOException {
        List<Medicamento> medicamentos = getAll();
        return  medicamentos.stream().filter(medicamento -> medicamento.getPrincipioAtivo().equals(principioAtivo)).findFirst();
    }

    @Override
    public List<Medicamento> findByPrincipioAtivo(String principioAtivo) throws IOException {
        List<Medicamento> medicamentos = getAll();
        return medicamentos.stream().filter(medicamento -> medicamento.getPrincipioAtivo().equals(principioAtivo)).collect(Collectors.toList());
    }

        @Override
    public Medicamento alterarArquivo(Medicamento medicamento, String identificador) throws IOException {
        List<Medicamento> medicamentos = getAll();
        Optional<Medicamento> optionalPaciente = medicamentos.stream()
                .filter(pacienteSearch -> pacienteSearch.getIdentificador().equals(identificador)).findFirst();
        if(optionalPaciente.isPresent()) {
            optionalPaciente.get().setPrincipioAtivo(medicamento.getPrincipioAtivo());
            reescreverArquivo(medicamentos);
            return optionalPaciente.get();
        }
        return medicamento;
    }

    private void reescreverArquivo(List<Medicamento> medicamentos) throws IOException {
        StringBuilder builder = new StringBuilder();
        for (Medicamento medicamentoBuilder: medicamentos) {
            builder.append(format(medicamentoBuilder));
        }
        write(builder.toString(), StandardOpenOption.CREATE);
    }

    @Override
    public void removerItemArquivo(String identificador) throws IOException {
        List<Medicamento> medicamentos = getAll();
        List<Medicamento> medicamentoResultante = new ArrayList<>();
        for (Medicamento medicamento : medicamentos){
            if(!medicamento.getIdentificador().equals(identificador)){
                medicamentoResultante.add(medicamento);
            }
        }
        eraseContent();
        reescreverArquivo(medicamentoResultante);
    }

    private String format(Medicamento medicamento) {
        return String.format("%s;%s;%s;%d;%d;%s;%s;%s\r\n",
                medicamento.getIdentificador(),
                medicamento.getPrincipioAtivo(),
                medicamento.getFabricante(),
                medicamento.getDosagem(),
                medicamento.getPeriodicidade(),
                medicamento.getHorarioDose(),
                medicamento.getPaciente().getNome(),
                medicamento.getPaciente().getCpf());
    }

    private Medicamento convert(String linha) {
        StringTokenizer token = new StringTokenizer(linha, ";");
        return Medicamento.builder()
                .identificador(token.nextToken())
                .principioAtivo(token.nextToken())
                .fabricante(token.nextToken())
                .dosagem(Integer.parseInt(token.nextToken()))
                .periodicidade(Integer.parseInt(token.nextToken()))
                .horarioDose(token.nextToken())
                .paciente(Paciente.builder()
                        .nome(token.nextToken())
                        .cpf(token.nextToken())
                        .build())
                .build();
    }

    public void eraseContent() throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(path);
        writer.write("");
        writer.flush();
    }
}
