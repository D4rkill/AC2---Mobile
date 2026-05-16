package com.example.provaac2;

public class Filmes {
    private String id;
    private String nome;
    private String tipo;
    private String genero;
    private int anoDeLancamento;
    private int notaPessoal;
    private boolean jaAssistiu;

    public Filmes() {} // Requisito para Firestore

    public Filmes(String nome, String tipo, String genero, int anoDeLancamento, int notaPessoal, boolean jaAssistiu){
        this.nome = nome;
        this.tipo = tipo;
        this.genero = genero;
        this.anoDeLancamento = anoDeLancamento;
        this.notaPessoal = notaPessoal;
        this.jaAssistiu = jaAssistiu;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public int getAnoDeLancamento() {
        return anoDeLancamento;
    }

    public void setAnoDeLancamento(int anoDeLancamento) {
        this.anoDeLancamento = anoDeLancamento;
    }

    public int getNotaPessoal() {
        return notaPessoal;
    }

    public void setNotaPessoal(int notaPessoal) {
        this.notaPessoal = notaPessoal;
    }

    public boolean isJaAssistiu() {
        return jaAssistiu;
    }

    public void setJaAssistiu(boolean jaAssistiu) {
        this.jaAssistiu = jaAssistiu;
    }
}

