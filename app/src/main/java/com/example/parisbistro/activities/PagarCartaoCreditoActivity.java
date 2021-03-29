package com.example.parisbistro.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.parisbistro.R;
import com.example.parisbistro.model.Produto;
import com.example.parisbistro.singleton.Carrinho;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.List;

public class PagarCartaoCreditoActivity extends AppCompatActivity {

    private String nome;
    private String contato;
    private String endereco;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagar_cartao_credito);

        nome = getIntent().getStringExtra("nome");
        contato = getIntent().getStringExtra("contato");
        endereco = getIntent().getStringExtra("endereco");

        firestore = FirebaseFirestore.getInstance();

        criarJsonObject();
    }

    private void criarJsonObject(){
        JsonObject dados = new JsonObject();
        JsonArray itemsList = new JsonArray();
        JsonObject item = new JsonObject();

        JsonObject email = new JsonObject();

        JsonObject excluded_payments_types = new JsonObject();
        JsonArray ids = new JsonArray();
        JsonObject removerBoleto = new JsonObject();


        List<Produto> produtos = Carrinho.getInstance();
        for (Produto produto: produtos){
            item = new JsonObject();
            item.addProperty("title",produto.getNome());
            item.addProperty("description",produto.getDescricao());
            item.addProperty("quantity",1);
            item.addProperty("currency_id","BRL");
            item.addProperty("unit_price",Double.parseDouble(produto.getValor()));
            itemsList.add(item);
        }
        dados.add("items", itemsList);

        String emailUsuario = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        email.addProperty("email",emailUsuario);
        dados.add("payer", email);


        removerBoleto.addProperty("id", "ticket");
        ids.add(removerBoleto);
        excluded_payments_types.add("excluded_payment_types", ids);
        excluded_payments_types.addProperty("installmments",2);
        dados.add("payments_methods", excluded_payments_types);


    }


}