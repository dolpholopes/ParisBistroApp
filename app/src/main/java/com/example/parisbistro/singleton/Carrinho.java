package com.example.parisbistro.singleton;

import com.example.parisbistro.model.Produto;

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

}
