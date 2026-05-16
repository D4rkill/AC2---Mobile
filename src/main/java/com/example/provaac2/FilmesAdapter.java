package com.example.provaac2;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FilmesAdapter extends RecyclerView.Adapter<FilmesAdapter.ViewHolder> {

    private final List<Filmes> filmes;
    private final Set<String> idsDeletando = new HashSet<>();

    public interface OnItemClickListener {
        void onItemClick(Filmes filmes);
    }
    private OnItemClickListener listener;
    public FilmesAdapter(List<Filmes> filmes) {
        this.filmes = filmes;
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(v);
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        Filmes f = filmes.get(pos);

        holder.txt1.setText(f.getNome());

        holder.txt2.setText("Gênero: " + f.getGenero() + " | Tipo: " + f.getTipo() + " | Ano: " + f.getAnoDeLancamento() + " | Nota: " + f.getNotaPessoal());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(f);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            int position = holder.getBindingAdapterPosition();

            if (position == RecyclerView.NO_POSITION) {
                return true;
            }

            Filmes filmeSelecionado = filmes.get(position);

            new AlertDialog.Builder(v.getContext()).setTitle("Excluir filme").setMessage("Deseja excluir \"" + filmeSelecionado.getNome() + "\"?")
                    .setPositiveButton("Excluir", (dialog, which) -> {
                        deletarFilme(filmeSelecionado.getId(), v);
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
            return true;
        });
    }

    private void deletarFilme(String id, View view) {
        if (id == null || id.trim().isEmpty()) {
            Toast.makeText(view.getContext(), "Erro: filme sem ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (idsDeletando.contains(id)) {
            return;
        }

        idsDeletando.add(id);

        FirebaseFirestore.getInstance()
                .collection("filmes")
                .document(id)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    idsDeletando.remove(id);

                    int position = encontrarPosicaoPorId(id);

                    if (position != -1) {
                        filmes.remove(position);
                        notifyItemRemoved(position);
                    } else {
                        notifyDataSetChanged();
                    }

                    Toast.makeText(view.getContext(), "Filme deletado!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    idsDeletando.remove(id);
                    Toast.makeText(view.getContext(), "Erro ao deletar filme.", Toast.LENGTH_SHORT).show();
                });
    }

    private int encontrarPosicaoPorId(String id) {
        for (int i = 0; i < filmes.size(); i++) {
            Filmes filme = filmes.get(i);

            if (filme.getId() != null && filme.getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return filmes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txt1, txt2;

        public ViewHolder(View itemView) {
            super(itemView);

            txt1 = itemView.findViewById(android.R.id.text1);
            txt2 = itemView.findViewById(android.R.id.text2);
        }
    }
}