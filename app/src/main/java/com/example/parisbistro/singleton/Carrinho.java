package com.example.parisbistro.singleton;

import com.example.parisbistro.model.Produto;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Carrinho {

    private static List<Produto> produtosCarrinho;

    public static List<Produto> getInstance(){
        if (produtosCarrinho == null){
            produtosCarrinho = new ArrayList<Produto>();
        }

        return produtosCarrinho;
    }


    public static String valorTotal(){
        double valorTotal = 0;
        for (Produto produto: produtosCarrinho){
            double v = Double.valueOf(produto.getValor());
            valorTotal = valorTotal + v;
        }

        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        String valor = decimalFormat.format(valorTotal);

        return valor;
    }

}
