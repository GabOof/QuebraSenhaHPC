package pack;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class QuebraSenhaHPC extends Thread {

    public static void main(String[] args) {
        JFileChooser janela = new JFileChooser();

        //monitora a ação do usuário na árvore de diretório do sistema
        int operacao = janela.showOpenDialog(null);
        if (operacao == JFileChooser.APPROVE_OPTION) {

            //ref. do arquivo selecionado pelo user...
            File arquivo = janela.getSelectedFile();

            //será que o arquivo é ext. .zip???
            if (!arquivo.getAbsolutePath().contains(".zip")) {
                JOptionPane.showMessageDialog(null, "O arquivo selecionado deve ter ext. do tipo .zip", "Arquivo incorreto", JOptionPane.WARNING_MESSAGE);
                System.exit(0);
            }

            ChaveiroArquivo chaveiro = new ChaveiroArquivo(arquivo);

            // Divide o conjunto de senhas em quatro partes iguais
            int numPasswords = (int) Math.pow(95, 5);
            int chunkSize = numPasswords / Runtime.getRuntime().availableProcessors();

            // Cria um array de threads
            Thread[] threads = new Thread[Runtime.getRuntime().availableProcessors()];

            // Inicializa cada thread com uma parte diferente das senhas
            for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
                final int start = i * chunkSize;
                final int end = (i == Runtime.getRuntime().availableProcessors() - 1) ? numPasswords : (i + 1) * chunkSize;
                threads[i] = new QuebraSenhaThread(start, end, chaveiro);
                threads[i].start();
            }

            // Aguarda todas as threads terminarem
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    // Ignora interrupções
                }
            }

            //"Chaveiro(s)" começa a trabalhar aqui ;)
            ChaveiroArquivo trab = new ChaveiroArquivo(arquivo);

            /*   //executa se a senha estiver correta (a senha tem tamanho de 5 caracteres)
            if (trab.tentaSenha("chute de uma senha, você pode alterar isso aqui")) {
                System.out.println("Senha correta");
            }*/
        } else {
            JOptionPane.showMessageDialog(null, "O arquivo não foi selecioando", "Arquivo???", JOptionPane.WARNING_MESSAGE);
        }
    }
}

class QuebraSenhaThread extends Thread {

    private final int start;
    private final int end;
    private final ChaveiroArquivo chaveiro;

    public QuebraSenhaThread(int start, int end, ChaveiroArquivo chaveiro) {
        this.start = start;
        this.end = end;
        this.chaveiro = chaveiro;
    }
    //criando um boolean para que assim que uma das threads encontrar a senha, todas parem
    public static volatile boolean senhaEncontrada = false;

    @Override
    public void run() {
        for (int i = start; i < end; i++) {
            String password = toPassword(i);
            if (chaveiro.tentaSenha(password)) {
                System.out.println("Senha correta: " + password);
                senhaEncontrada = true;
                stopAllThreads(Thread.currentThread().getThreadGroup());
                break;
            }
            if (senhaEncontrada) {
                System.exit(0);
            }
        }
    }

    private static String toPassword(int index) {
        char[] password = new char[5];
        for (int i = 0; i < 5; i++) {
            password[i] = (char) ((index % 94) + 33); // O intervalo é de 33 a 126, então são 94 caracteres possíveis
            index /= 94;
        }
        return new String(password);

    }

// Interrompe todas as threads
    private void stopAllThreads(ThreadGroup threadGroup) {
        // Obtém a quantidade de threads ativas no grupo
        int activeThreadCount = threadGroup.activeCount();
        // Cria um array para armazenar as threads ativas
        Thread[] threads = new Thread[activeThreadCount];
        // Preenche o array com as threads ativas
        threadGroup.enumerate(threads);
        // Itera sobre o array de threads, interrompendo cada uma delas
        for (Thread thread : threads) {
            thread.interrupt();
        }
    }
}
