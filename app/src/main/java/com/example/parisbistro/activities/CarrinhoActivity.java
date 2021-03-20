package com.example.parisbistro.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.parisbistro.R;
import com.example.parisbistro.adapters.AdapterRecyclerViewAdicional;
import com.example.parisbistro.adapters.AdapterRecyclerViewCarrinho;
import com.example.parisbistro.model.Adicional;
import com.example.parisbistro.model.Produto;
import com.example.parisbistro.singleton.Carrinho;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CarrinhoActivity extends AppCompatActivity implements View.OnClickListener, AdapterRecyclerViewCarrinho.CarrinhoClick {

    private RecyclerView recyclerView;
    private TextView textView_continuar;
    private TextView textView_valorTotal;

    private AdapterRecyclerViewCarrinho adapterRecyclerViewCarrinho;
    private List<Produto> produtos = new ArrayList<Produto>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrinho);

        configToolbar();


        recyclerView = findViewById(R.id.recyclerView_carrinho);
        textView_continuar = findViewById(R.id.textView_carrinho_continuar);
        textView_valorTotal = findViewById(R.id.textView_carrinho_valorTotal);

        textView_continuar.setOnClickListener(this);

        produtos = Carrinho.getInstance();

        configRecyclerView();

        String valorTotal = Carrinho.valorTotal();
        textView_valorTotal.setText("R$ "+valorTotal);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.textView_carrinho_continuar:
                    buttonCarrinhoContinuar();
                break;

        }
    }

    private void configToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView textView = findViewById(R.id.textView_toolbar);
        textView.setText("Meus pedidos");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
        }
        return true;
    }


    private void configRecyclerView() {
        adapterRecyclerViewCarrinho = new AdapterRecyclerViewCarrinho(this, produtos, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapterRecyclerViewCarrinho);
    }

    @Override
    public void carrinhoOnClick(Produto produto) {
        produtos.remove(produto);
        adapterRecyclerViewCarrinho.notifyDataSetChanged();
        atualizarValorTotalProdutos();
        Toast.makeText(getBaseContext(), "O item " + produto.getNome() + " foi removido", Toast.LENGTH_SHORT).show();
    }

    private void atualizarValorTotalProdutos(){
        double valorTotal = 0;
        for (Produto produto: produtos){
            double valor = Double.valueOf(produto.getValor());
            valorTotal = valorTotal + valor;
        }

        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        String valorTotalString = decimalFormat.format(valorTotal);

        textView_valorTotal.setText("R$ " + valorTotalString);
    }

    private void buttonCarrinhoContinuar(){
        if (produtos.isEmpty()){
            Toast.makeText(getBaseContext(), "Nenhum item foi adicionado ao carrinho", Toast.LENGTH_SHORT).show();
        }else{
            startActivity(new Intent(this, LoginActivity.class));
           //dialogoOpcaoPagamento();
        }
    }

    private void dialogoOpcaoPagamento(){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("Escolha uma opção")
                .setPositiveButton("Retirar pedido no local", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setNegativeButton("Receber pedido em casa", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();

        dialog.show();
    }
}