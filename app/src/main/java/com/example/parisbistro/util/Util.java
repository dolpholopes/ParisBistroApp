package com.example.parisbistro.util;


import android.content.Context;

import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;


public class Util {


    public static boolean validate(AppCompatActivity activity, int requestCode, String[] permissions) {

        List<String> list = new ArrayList<String>();

        for (String permission : permissions) {

            boolean ok = ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
            if (!ok) { // se for false
                list.add(permission);
            }
        }
        if (list.isEmpty()) {
            return true;
        }

        String[] newPermissions = new String[list.size()];
        list.toArray(newPermissions);

        //solicita a permissao

        ActivityCompat.requestPermissions(activity, newPermissions, requestCode);
        return false;
    }







    public static boolean statusInternet_MoWi(Context context) {

        ConnectivityManager conexao = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (conexao != null) {

            // PARA DISPOSTIVOS NOVOS
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                NetworkCapabilities recursosRede = conexao.getNetworkCapabilities(conexao.getActiveNetwork());

                if (recursosRede != null) {//VERIFICAMOS SE RECUPERAMOS ALGO

                    if (recursosRede.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {

                        //VERIFICAMOS SE DISPOSITIVO TEM 3G
                        return true;

                    } else if (recursosRede.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {

                        //VERIFICAMOS SE DISPOSITIVO TEM WIFFI
                        return true;

                    }

                    //NÃO POSSUI UMA CONEXAO DE REDE VÁLIDA

                    return false;

                }

            } else {//COMECO DO ELSE

                // PARA DISPOSTIVOS ANTIGOS  (PRECAUÇÃO)
                NetworkInfo informacao = conexao.getActiveNetworkInfo();


                if (informacao != null && informacao.isConnected()) {
                    return true;
                } else
                    return false;


            }//FIM DO ELSE
        }


        return false;
    }

    public static String errorFirebase(String error){
        if (error.contains("email address is badly")){
            return "Email inválido";
        }
        else if (error.contains("The password is invalid")){
            return "Senha inválida";
        }
        else if (error.contains("There is no user record")){
                return "Email não cadastrado no sistema";
        }
        else if (error.contains("insufficient permissions")){
            return "Usuario não autorizado";
        }
        else if (error.contains("least 6 characters")){
            return "Insira uma senha de no mínimo de 6 caracteres";
        }
        else if (error.contains("interrupted connection")){
            return "Erro de conexção";
        }
        else if (error.contains("address is already")){
            return "Email já cadastrado";
        }
        else{
            return error;
        }
    }



    }
