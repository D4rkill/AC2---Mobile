package com.example.provaac2;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private EditText edtNome;
    private EditText edtAnoDeLancamento;
    private EditText edtNotaPessoal;
    private Spinner spnTipo;
    private Spinner spnGenero;
    private Switch swJaAssistiu;
    private RecyclerView recyclerFilmes;
    private final List<Filmes> listaFilmes = new ArrayList<>();
    private FilmesAdapter adapter;
    private Filmes filmeEditando = null;
    private Button btnSalvar;
    private Button btnOrdenarNota;
    private Button btnOrdenarAno;
    private int semOrdenacao = 0;
    private int ordenarPorNota = 1;
    private int ordenarPorAno = 2;
    private int criterioOrdenacaoAtual = semOrdenacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        edtNome = findViewById(R.id.txtNome);
        edtAnoDeLancamento = findViewById(R.id.txtAno);
        edtNotaPessoal = findViewById(R.id.txtNota);
        spnTipo = findViewById(R.id.spnTipo);
        spnGenero = findViewById(R.id.spnGenero);
        swJaAssistiu = findViewById(R.id.swtAssistiu);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnOrdenarNota = findViewById(R.id.btnOrdenarNota);
        btnOrdenarAno = findViewById(R.id.btnOrdenarAno);
        recyclerFilmes = findViewById(R.id.lstFilmes);
        recyclerFilmes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FilmesAdapter(listaFilmes);
        recyclerFilmes.setAdapter(adapter);

        configurarSpinners();
        configurarCliques();
        carregarFilmes();
    }

    private void configurarSpinners() {
        ArrayAdapter<CharSequence> adapterTipo = ArrayAdapter.createFromResource(this, R.array.opcoesTipo, android.R.layout.simple_spinner_item);
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnTipo.setAdapter(adapterTipo);

        ArrayAdapter<CharSequence> adapterGenero = ArrayAdapter.createFromResource(this, R.array.opcoesGenero, android.R.layout.simple_spinner_item);
        adapterGenero.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnGenero.setAdapter(adapterGenero);
    }

    private void configurarCliques() {
        btnSalvar.setOnClickListener(v -> salvarFilme());
        btnOrdenarNota.setOnClickListener(v -> {
            criterioOrdenacaoAtual = ordenarPorNota;
            aplicarOrdenacao();
            Toast.makeText(this, "Ordenado por nota pessoal.", Toast.LENGTH_SHORT).show();
        });

        btnOrdenarAno.setOnClickListener(v -> {
            criterioOrdenacaoAtual = ordenarPorAno;
            aplicarOrdenacao();
            Toast.makeText(this, "Ordenado por ano de lançamento.", Toast.LENGTH_SHORT).show();
        });

        adapter.setOnItemClickListener(filmes -> {
            edtNome.setText(filmes.getNome());
            edtAnoDeLancamento.setText(
                    String.valueOf(filmes.getAnoDeLancamento())
            );
            edtNotaPessoal.setText(
                    String.valueOf(filmes.getNotaPessoal())
            );
            selecionarItemSpinner(spnTipo, filmes.getTipo());
            selecionarItemSpinner(spnGenero, filmes.getGenero());
            swJaAssistiu.setChecked(filmes.isJaAssistiu());
            filmeEditando = filmes;
            btnSalvar.setText("Atualizar Filme");
        });
    }

    private void carregarFilmes() {
        db.collection("filmes")
                .get()
                .addOnSuccessListener(query -> {
                    listaFilmes.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        Filmes f = doc.toObject(Filmes.class);
                        f.setId(doc.getId());
                        listaFilmes.add(f);
                    }
                    aplicarOrdenacao();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao carregar filmes.", Toast.LENGTH_SHORT).show();
                });
    }

    private void salvarFilme() {
        if (!validarCampos()) {
            return;
        }

        String nome = edtNome.getText().toString().trim();
        String tipo = spnTipo.getSelectedItem().toString();
        String genero = spnGenero.getSelectedItem().toString();
        String anoTexto = edtAnoDeLancamento.getText().toString().trim();
        String notaTexto = edtNotaPessoal.getText().toString().trim();
        boolean jaAssistiu = swJaAssistiu.isChecked();
        int anoDeLancamento;
        int notaPessoal;

        try {
            anoDeLancamento = Integer.parseInt(anoTexto);
        } catch (NumberFormatException e) {
            edtAnoDeLancamento.setError("Informe um ano válido.");
            edtAnoDeLancamento.requestFocus();
            return;
        }

        try {
            notaPessoal = Integer.parseInt(notaTexto);
        } catch (NumberFormatException e) {
            edtNotaPessoal.setError("Informe uma nota válida.");
            edtNotaPessoal.requestFocus();
            return;
        }

        if (filmeEditando == null) {
            Filmes filme = new Filmes(nome, tipo, genero, anoDeLancamento, notaPessoal, jaAssistiu);
            cadastrarFilme(filme);
        } else {
            atualizarFilme(nome, tipo, genero, anoDeLancamento, notaPessoal, jaAssistiu);
        }
    }

    private boolean validarCampos() {
        String nome = edtNome.getText().toString().trim();
        String anoTexto = edtAnoDeLancamento.getText().toString().trim();
        String notaTexto = edtNotaPessoal.getText().toString().trim();

        if (nome.isEmpty()) {
            edtNome.setError("Informe o nome.");
            edtNome.requestFocus();
            return false;
        }

        if (spnTipo.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Selecione o tipo corretamente.", Toast.LENGTH_SHORT).show();
            spnTipo.requestFocus();
            return false;
        }

        if (spnGenero.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Selecione o gênero corretamente.", Toast.LENGTH_SHORT).show();
            spnGenero.requestFocus();
            return false;
        }

        if (anoTexto.isEmpty()) {
            edtAnoDeLancamento.setError("Informe o ano de lançamento.");
            edtAnoDeLancamento.requestFocus();
            return false;
        }

        if (notaTexto.isEmpty()) {
            edtNotaPessoal.setError("Informe a nota pessoal.");
            edtNotaPessoal.requestFocus();
            return false;
        }
        return true;
    }

    private void cadastrarFilme(Filmes filme) {
        db.collection("filmes")
                .add(filme)
                .addOnSuccessListener(doc -> {
                    filme.setId(doc.getId());

                    Toast.makeText(this, "Filme salvo!", Toast.LENGTH_SHORT).show();
                    limparCampos();
                    carregarFilmes();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao salvar filme.", Toast.LENGTH_SHORT).show();
                });
    }

    private void atualizarFilme(String nome, String tipo, String genero, int anoDeLancamento, int notaPessoal, boolean jaAssistiu) {
        filmeEditando.setNome(nome);
        filmeEditando.setTipo(tipo);
        filmeEditando.setGenero(genero);
        filmeEditando.setAnoDeLancamento(anoDeLancamento);
        filmeEditando.setNotaPessoal(notaPessoal);
        filmeEditando.setJaAssistiu(jaAssistiu);
        db.collection("filmes")
                .document(filmeEditando.getId())
                .set(filmeEditando)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Filme atualizado!", Toast.LENGTH_SHORT).show();
                    limparCampos();
                    carregarFilmes();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao atualizar filme.", Toast.LENGTH_SHORT).show();
                });
    }

    private void aplicarOrdenacao() {
        if (criterioOrdenacaoAtual == ordenarPorNota) {
            Collections.sort(listaFilmes, (f1, f2) ->
                    Integer.compare(f2.getNotaPessoal(), f1.getNotaPessoal())
            );
        } else if (criterioOrdenacaoAtual == ordenarPorAno) {
            Collections.sort(listaFilmes, (f1, f2) ->
                    Integer.compare(f2.getAnoDeLancamento(), f1.getAnoDeLancamento())
            );
        }
        adapter.notifyDataSetChanged();
    }

    private void selecionarItemSpinner(Spinner spinner, String valor) {
        if (valor == null) {
            return;
        }

        for (int i = 0; i < spinner.getCount(); i++) {
            String item = spinner.getItemAtPosition(i).toString();

            if (item.equalsIgnoreCase(valor)) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void limparCampos() {
        edtNome.setText("");
        edtAnoDeLancamento.setText("");
        edtNotaPessoal.setText("");
        spnTipo.setSelection(0);
        spnGenero.setSelection(0);
        swJaAssistiu.setChecked(false);
        filmeEditando = null;
        btnSalvar.setText("Salvar Filme");
    }
}