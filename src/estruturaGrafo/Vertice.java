package estruturaGrafo;
public class Vertice<TIPO extends Comparable<TIPO>> extends Nodo<TIPO> { // arvores

    public Vertice(TIPO nome) { // o vertice é o proprio Nodo raiz da arvore
        super(nome); 
    }

    private Nodo<TIPO> addRec(Nodo<TIPO> atual, TIPO nome) {
        if (atual == null) {
            return new Nodo<TIPO>(nome);
        }
        if (nome.compareTo(atual.getNome()) < 0) {
            atual.setEsquerda(addRec(atual.getEsquerda(), nome));
        } else {
            atual.setDireita(addRec(atual.getDireita(), nome));
}
        return atual;
    }

    public void add(TIPO nome) {
        if(nome.compareTo(this.getNome()) < 0){
            this.setEsquerda(addRec(this.getEsquerda(), nome));
        }else{
            this.setDireita(addRec(this.getDireita(), nome));
        }
    }

      public void printInsert(Nodo<TIPO> raiz) {
        if (raiz != null) {
            System.out.print(raiz.getNome() + "-");
            printInsert(raiz.getEsquerda());
            printInsert(raiz.getDireita());
        }
    }

    public void printOrdem(Nodo<TIPO> raiz) {
        if (raiz != null) {
            printOrdem(raiz.getEsquerda());
            System.out.print(raiz.getNome() + "-");
            printOrdem(raiz.getDireita());
        }
    }
}
