package com.example.parisbistro.singleton;

import com.example.parisbistro.model.Produto;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Carrinho {

    private volatile static Carrinho carrinho;
    private  List<Produto> produtosCarrinho;

    public static Carrinho getInstance(){
        if (carrinho == null){

            synchronized (Carrinho.class){
                if (carrinho == null){
                    carrinho = new Carrinho();
                }
            }
        }
        return carrinho;
    }

    public Carrinho(){
        produtosCarrinho = new ArrayList<Produto>();
    }

    public  List<Produto> getProdutosCarrinho(){
        return this.produtosCarrinho;
    }




}
