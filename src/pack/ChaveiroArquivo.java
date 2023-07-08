package pack;

import java.io.File;
import java.util.List;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

/*
    Analogia a um "chaveiro", mas este é de arquivo :)
    1 - não modifique o que foi implementado
    2 - você tem liberade para criar mais métodos e/ou criar novas classes
    3 - você pode fazer com que esta classe herde de uma outra
 */
public class ChaveiroArquivo {

    private final File arquivoSegredo;
    private String caminhoArquivo;

    public ChaveiroArquivo(File arquivoSegredo) {
        this.arquivoSegredo = arquivoSegredo;

        if (this.arquivoSegredo != null) {
            //caminho onde vamos ext. o arquivo compactado
            this.caminhoArquivo = this.arquivoSegredo.getAbsoluteFile().getParent();
        }
    }

    //método que permite realizar um teste com a senha passada como parâmetro para um dado arquivo
    public boolean tentaSenha(String senha) {
        try {
            ZipFile zipFile = new ZipFile(this.arquivoSegredo);

            //esta com senha
            if (zipFile.isEncrypted()) {
                //tentativa da senha aqui
                zipFile.setPassword(senha.toCharArray());
            }
            List fileHeaderList = zipFile.getFileHeaders();

            //solução genérica para qualquer tamanho de Header
            for (int i = 0; i < fileHeaderList.size(); i++) {
                FileHeader fileHeader = (FileHeader) fileHeaderList.get(i);
                //onde o arquivo será armazenado
                zipFile.extractFile(fileHeader, this.caminhoArquivo);
                //System.out.println("encontramos a senha e o arquivo");
            }
            return true;

        } catch (ZipException e) {
            //System.out.println("Erro tente novamente");
            return false;
        }
    }
}