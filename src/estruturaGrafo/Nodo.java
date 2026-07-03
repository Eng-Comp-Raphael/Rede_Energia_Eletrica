package estruturaGrafo;
/**
 * @file Nodo.java
 * @author Raphael Batista
 * @date 2026
 * @brief Ponteiros das arvores
 */

public class Nodo<TIPO> {
    private TIPO nome;
    private Nodo<TIPO> direita;
    private Nodo<TIPO> esquerda;
    private boolean ativo;

    public Nodo(TIPO nome){
        this.nome = nome;
        this.direita = null;
        this.esquerda = null;
        this.ativo = true;
    }

    public TIPO getNome() {
        return nome;
    }

    public void setNome(TIPO nome) {
        this.nome = nome;
    }

    public Nodo<TIPO> getDireita() {
        return direita;
    }

    public void setDireita(Nodo<TIPO> direita) {
        this.direita = direita;
    }

    public Nodo<TIPO> getEsquerda() {
        return esquerda;
    }

    public void setEsquerda(Nodo<TIPO> esquerda) {
        this.esquerda = esquerda;
    }

    public boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
    
}
