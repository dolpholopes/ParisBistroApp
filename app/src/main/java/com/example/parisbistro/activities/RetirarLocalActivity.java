package com.example.parisbistro.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.parisbistro.R;
import com.example.parisbistro.model.Produto;
import com.example.parisbistro.singleton.Carrinho;
import com.example.parisbistro.util.DialogProgress;
import com.example.parisbistro.util.PdfCreator;
import com.example.parisbistro.util.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

public class RetirarLocalActivity extends AppCompatActivity implements View.OnClickListener {

    private Button button_recuperarDadosUsuario;
    private Button button_finalizarPedido;
    private EditText editText_nome;
    private EditText editText_contato;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retirar_local);

        configToolbar();

        firestore = FirebaseFirestore.getInstance();

        button_recuperarDadosUsuario = findViewById(R.id.button_pedidoRetirar_carregarDadosUsuario);
        editText_nome = findViewById(R.id.editText_pedidoRetirar_usuarioNome);
        editText_contato = findViewById(R.id.editText_pedidoRetirar_usuarioTelefone);
        button_finalizarPedido = findViewById(R.id.button_pedidoRetirar_finalizarPedido);

        button_recuperarDadosUsuario.setOnClickListener(this);
        button_finalizarPedido.setOnClickListener(this);
    }


    private void configToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView textView = findViewById(R.id.textView_toolbar);
        textView.setText("Retirar pedido no local");
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
        }
        return true;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_pedidoRetirar_carregarDadosUsuario:
                buttonCarregarDadosUsuario();
                break;

            case R.id.button_pedidoRetirar_finalizarPedido:
                buttonFinalizarPedido();
                break;
        }
    }

    private void buttonCarregarDadosUsuario(){
        DialogProgress dialogProgress = new DialogProgress();
        dialogProgress.show(getSupportFragmentManager(),"1");

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firestore.collection("usuarios").document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.exists()){
                    String nome = (String) documentSnapshot.getData().get("nome");
                    String contato = (String) documentSnapshot.getData().get("contato");

                    editText_nome.setText(nome);
                    editText_contato.setText(contato);
                }else{
                    Toast.makeText(getBaseContext(), "Ainda n??o foi realizado nenhum pedido", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialogProgress.dismiss();
                Toast.makeText(getBaseContext(), "Erro ao recuperar os dados", Toast.LENGTH_SHORT).show();
            }
        });
        dialogProgress.dismiss();
    }

    private void buttonFinalizarPedido(){
        String nome = editText_nome.getText().toString();
        String contato = editText_contato.getText().toString();
        String forma_pagamento = "<b> Retirada no local <b>";

        if (nome.trim().isEmpty() || contato.trim().isEmpty()){
            Toast.makeText(getBaseContext(), "Preencha os dados obrigatorios para entrega", Toast.LENGTH_SHORT).show();
        }else{
            if (Util.statusInternet_MoWi(getBaseContext())){
                confirmarPedido(nome,contato,forma_pagamento);
            }else{
                Toast.makeText(getBaseContext(), (R.string.sem_conexao), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void confirmarPedido(String nome, String contato, String forma_pagamento){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Confirmar Pedido")
                .setMessage("\nGostaria de realizar o pedido?")
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        salvarDadosUsuarioPagamentoPessoalmenteFirebase(nome, contato, forma_pagamento);
                    }
                }).create();
        dialog.show();
    }

    private void salvarDadosUsuarioPagamentoPessoalmenteFirebase(String nome, String contato, String forma_pagamento){

        DialogProgress dialogProgress = new DialogProgress();
        dialogProgress.show(getSupportFragmentManager(),"1");

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        HashMap<String,Object> dadosUsuarios = new HashMap<>();
        dadosUsuarios.put("nome", nome);
        dadosUsuarios.put("contato", contato);

        DocumentReference reference = firestore.collection("usuarios").document(uid);

        reference.set(dadosUsuarios).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dialogProgress.dismiss();
                if (task.isSuccessful()){
                    obterToken(nome, contato, forma_pagamento);
                }else{
                    Toast.makeText(getBaseContext(), "Erro ao salvar os dados: "+ task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void obterToken(String nome, String contato, String forma_pagamento){
        DialogProgress dialogProgress = new DialogProgress();
        dialogProgress.show(getSupportFragmentManager(),"2");

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(
                new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        dialogProgress.dismiss();
                        if (task.isSuccessful()){
                            String token = task.getResult().getToken();
                            salvarPedidoFirebase(nome, contato, forma_pagamento, token);
                        }else{
                            String erro = "Sem token";
                            salvarPedidoFirebase(nome, contato, forma_pagamento, erro);
                        }
                    }
                });
    }

    private void salvarPedidoFirebase(String nome,String  contato, String forma_pagamento,String  token){
        DialogProgress dialogProgress = new DialogProgress();
        dialogProgress.show(getSupportFragmentManager(),"3");

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String idPedido = firestore.collection("pedidos").document().getId();
        String totalValorProdutos = valorTotalProdutos();

        long data = Timestamp.now().toDate().getTime();

        String todosProdutos = "";

        for (Produto produto: Carrinho.getInstance().getProdutosCarrinho()){
            todosProdutos = todosProdutos + produto.getNome() + " <br> " +
                    produto.getAdicional() + " <br> " +
                    produto.getObservacao() + " <br><br> ";
        }

        HashMap<String, Object> dadosPedido = new HashMap<>();
        dadosPedido.put("cliente_contato",contato);
        dadosPedido.put("cliente_endereco",forma_pagamento);
        dadosPedido.put("cliente_nome",nome);
        dadosPedido.put("cliente_uid",uid);

        dadosPedido.put("pedido_dados",todosProdutos);
        dadosPedido.put("pedido_data", data);
        dadosPedido.put("pedido_forma_pagamento",forma_pagamento);

        dadosPedido.put("pedido_id",idPedido);
        dadosPedido.put("pedido_status","em andamento");
        dadosPedido.put("pedido_valor",totalValorProdutos);
        dadosPedido.put("token_msg",token);

        DocumentReference reference = firestore.collection("pedidos").document(idPedido);
        String finalTotalProdutos = todosProdutos;
        reference.set(dadosPedido).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dialogProgress.dismiss();
                if (task.isSuccessful()){
                    dialogExibirPdf(idPedido,nome,data, finalTotalProdutos,totalValorProdutos);
                }else{
                    Toast.makeText(getBaseContext(), "Erro ao salvar o pedido", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void dialogExibirPdf(String idPedido,String nome, long data, String produtos, String totalValorProdutos){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Pedido efetuado com Sucesso")
                .setCancelable(false)
                .setMessage("\nGostaria de Gerar o comprovante do Pedido ?")
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override

                    public void onClick(DialogInterface dialog, int which) {

                        try {
                            gerarPdf(idPedido,nome,data, produtos,totalValorProdutos);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (DocumentException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).setNegativeButton("N??o", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        voltarActivityPrincipal();
                    }
                }).create();
        dialog.show();
    }


    private void gerarPdf(String idPedido,String nome, long data, String produtos, String totalValorProdutos) throws IOException, DocumentException {

        ParcelFileDescriptor descriptor = null;
        OutputStream outputStream;
        File pdf = null;
        Uri uri = null;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){

            ContentValues contentValues = new ContentValues();

            contentValues.put(MediaStore.MediaColumns.MIME_TYPE,"application/pdf");
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,"Pedido"+idPedido);
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS+"/Recibo/");

            ContentResolver resolver = getContentResolver();
            uri = resolver.insert(MediaStore.Downloads.getContentUri("external"),contentValues);
            descriptor = resolver.openFileDescriptor(uri,"rw");
            outputStream = new FileOutputStream(descriptor.getFileDescriptor());

        }else{

            File diretorioRaiz = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File diretorio = new File(diretorioRaiz.getPath()+"/Recibo/");

            if(  !diretorio.exists()){
                diretorio.mkdir();
            }

            String nomeArquivo = diretorio.getPath()+"/Pedido"+idPedido+".pdf";
            pdf = new File(nomeArquivo);
            outputStream = new FileOutputStream(pdf);
        }

        Document document = new Document();

        PdfCreator pdfCreator = new PdfCreator();

        PdfWriter pdfWriter = PdfWriter.getInstance(document,outputStream);

        pdfWriter.setBoxSize("box",new Rectangle(0,0,0,0));
        pdfWriter.setPageEvent(pdfCreator);

        document.open();

        String id = "ID Pedido:" + idPedido;
        String nomeCliente = "\nNome: \n"+nome;
        String dataPedido = "\nData e Hora: \n"+  Util.dataPedido(data);
        String pedido = "\nPedido: \n"+ produtos.replaceAll("<br>","\n");
        String valorPedido = "\nValor Total: \n"+totalValorProdutos;

        Font font = new Font(Font.FontFamily.TIMES_ROMAN,10, Font.NORMAL);

        Paragraph paragraph = new Paragraph("Deleviry Lanches",font);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        document.add(paragraph);

        Image image = null;

        Bitmap bitmap = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.logo_branca);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG,100,bytes);
        image = Image.getInstance(bytes.toByteArray());
        image.scaleAbsolute(70f,50f);
        image.setAlignment(Element.ALIGN_RIGHT);

        document.add(image);

        paragraph = new Paragraph(id,font);
        document.add(paragraph);

        paragraph = new Paragraph(nomeCliente,font);
        document.add(paragraph);

        paragraph = new Paragraph(dataPedido,font);
        document.add(paragraph);

        paragraph = new Paragraph(pedido,font);
        document.add(paragraph);

        paragraph = new Paragraph(valorPedido,font);
        document.add(paragraph);

        document.close();
        outputStream.close();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            descriptor.close();
            visualizarPdf(pdf, uri);
        }else{
            visualizarPdf(pdf,uri);
        }
    }


    private void visualizarPdf(File pdf, Uri uri){

        PackageManager packageManager = getPackageManager();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setType("application/pdf");

        List<ResolveInfo> lista = packageManager.queryIntentActivities(intent,PackageManager.MATCH_DEFAULT_ONLY);

        if(lista.size() > 0){

            Intent intent1 = new Intent(Intent.ACTION_VIEW);
            intent1.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if(pdf == null){ // versao mais nova android
                intent1.setDataAndType(uri,"application/pdf");
            }else{ // versao mais antiga android
                Uri uri1 = FileProvider.getUriForFile(getBaseContext(),"com.example.parisbistro",pdf);
                intent1.setDataAndType(uri1,"application/pdf");
            }

            startActivityForResult(intent1,1234);

        }else{
            erroAbrirPdf();
        }
    }

    private void erroAbrirPdf(){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Erro ao Abrir PDF")
                .setCancelable(false)
                .setMessage("\nN??o foi detectado um leitor PDF no seu Dispositivo. Caso queira visualizar a sua nota do Pedido Futuramente acesse a pasta de downlaods do seu Dispositivo.")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        voltarActivityPrincipal();
                    }
                }).create();

        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( requestCode == 1234 ){
            voltarActivityPrincipal();
        }
    }

    private void voltarActivityPrincipal(){

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Acompanhe seu pedido")
                .setCancelable(false)
                .setMessage("\nClique no Menu do canto esquerdo da tela principal e acompanhe o Status do seu Pedido")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Carrinho.getInstance().getProdutosCarrinho().clear();
                        Intent intent = new Intent(getBaseContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    }
                }).create();
        dialog.show();
    }

    private String valorTotalProdutos(){
        double valorTotal = 0;
        for (Produto produto: Carrinho.getInstance().getProdutosCarrinho()){
            double valor = Double.valueOf(produto.getValor());
            valorTotal = valorTotal + valor;
        }

        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        String valorTotalString = decimalFormat.format(valorTotal);
        return valorTotalString;
    }
}