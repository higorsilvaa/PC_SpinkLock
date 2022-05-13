import java.io.*;
import java.util.ArrayList;

public class main {
	//Controle do experimento
	//Higor: externalizar isso
	static int L = 1000000; //Número de iterações por thread
	static int I = 2;       //Quantidade de threads inicial
	static int P = 2;       //Acréscimo de P threads em I por teste
	static int Q = 8;       //Quantidade de vezes que I será incrementado P vezes
	static int QT = 12;     //Quantidade de testes para cada caso de testes
	static int dim = 16;    //Dimensão das matrizes a serem multiplicadas

	//controla o tempo máximo que uma thread pode ficar executando
	static long horKill = 0, minKill = 10, segKill = 0;
	static long marKill = 500; // 1/2 segundo de margem
	static long milKill = ((horKill * 60 + minKill) * 60 + segKill) * 1000;

	//Apenas soma um quando dim = 0 para verificar se as threads estão respeitando o lock
	static int n;
	
	public static class GetLock extends Thread {
		Lock lock;
		
		public GetLock(Lock lock){
			this.lock = lock;
		}
		
		public void run() {
			for(int i = 0; i < (int)(L/(dim+1)); i++){
				lock.lock();
				MM.mm(dim,n);
				lock.unlock();
			}
		}
	}
	
	//escreve os resultados num arquivo
	public static void write(double[][] matrix, int QL, int QC, String name) {
		File arquivo = new File(name);
		try( FileWriter fw = new FileWriter(arquivo) ){
			for(int l=0; l < QL; l ++){
				for(int c=0; c < QC; c ++){
					fw.write(Double.toString(matrix[l][c]));
					if(c+1!=QC)
						fw.write(",");
				}
				fw.write("\n");
			}
			fw.flush();
		}catch(IOException ex){
		  ex.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		if(marKill >= milKill){
			System.out.println("Erro: Margem está maior do que o tempo de espera da thread!");
			System.exit(-1);
		}
	
		ArrayList<Lock> lock = new ArrayList<Lock>();
		ArrayList<GetLock> p; //lista de threads
		double tempoInicial, media, maior, menor;
		ArrayList<Double> tempoFinal;
		long t;
		int indMaior, indMenor;
		double[][] matrix_tempos;
		
		lock.add(new TASLock());
		lock.add(new TTASLock());
		lock.add(new BackoffLock());
		lock.add(new SemaphoreB());
		lock.add(new ALock(2));//só para inicializar o lock
		lock.add(new CLHLock());
		lock.add(new MCSLock());
		lock.add(new CompositeLock());
		
		matrix_tempos = new double [Q][1+lock.size()];//1ª coluna é destinada para o número de threads
		
		//inicializa matriz dos tempos com NaN
		for(int i = 0; i < Q; i ++){
			matrix_tempos[i][0] = (double)(P*i+I);
			for(int j = 1; j < 1+lock.size(); j ++)
				matrix_tempos[i][j] = Float.NaN;
		}
		
		for(int c=1; c < lock.size()+1; c ++){//percorre os locks disponíveis
			//Somente para debug
			if(lock.get(c-1) instanceof TASLock)
				System.out.println("TASLock");
			else if(lock.get(c-1) instanceof TTASLock)
				System.out.println("TTASLock");
			else if(lock.get(c-1) instanceof BackoffLock)
				System.out.println("BackoffLock");
			else if(lock.get(c-1) instanceof SemaphoreB)
				System.out.println("Semaphore");
			else if(lock.get(c-1) instanceof ALock)
				System.out.println("Alock");
			else if(lock.get(c-1) instanceof CLHLock)
				System.out.println("CLHLock");
			else if(lock.get(c-1) instanceof MCSLock)
				System.out.println("MCSLock");
			else if(lock.get(c-1) instanceof CompositeLock)
				System.out.println("CompositeLock");
			else
				System.out.println("Error");
			
			//Realiza o experimento para cada algoritmo
			for(int N = I; N < Q*P+I; N+=P){
				System.out.println(N + " threads");
				
				try {
					tempoFinal = new ArrayList<Double>();
					for(int i = 0; i < QT; i++){//realiza QT vezes a mesma coisa para tirar a média do tempo
						n = 0;//somente para controle das treads
						
						p = new ArrayList<GetLock>();
						
						//Se for o ALock, corrige o número de threads máxima
						if(lock.get(c-1) instanceof ALock)
							lock.set(c-1,new ALock(N));
					
						for(int j = 0; j < N; j++)
							p.add(new GetLock(lock.get(c-1)));
						
						tempoInicial = System.currentTimeMillis();
						for(GetLock threads : p)
							threads.start();
						
						for(GetLock threads : p){
							t = System.currentTimeMillis();
							
							threads.join(milKill); //espera milKill milisegundos
							
							t = System.currentTimeMillis()-t;
							
							if(t >= milKill-marKill)//se ela foi morta, encerra os testes porque algo pode ter dado errado
								throw new InterruptedException("Foi morta!");
						}

						tempoFinal.add((System.currentTimeMillis()-tempoInicial)/Math.pow(10.0,3.0));
					}
					
					if(QT > 2){
						maior = tempoFinal.get(0);
						menor = tempoFinal.get(0);
						indMaior = 0;
						indMenor = 0;
						
						for(int i = 1; i < tempoFinal.size(); i ++){
							if(maior < tempoFinal.get(i)){
								maior = tempoFinal.get(i);
								indMaior = i;
							}
							if(menor > tempoFinal.get(i)){
								menor = tempoFinal.get(i);
								indMenor = i;
							}
						}
						
						//remover o melhor e o pior
						tempoFinal.remove(indMenor);
						//agora de indMenor para cima eu devo decrementar
						if(indMaior > indMenor)
							indMaior --;
						tempoFinal.remove(indMaior);
					}
					
					if(QT == 1)
						media = tempoFinal.get(0);
					else{
						media = 0.0;
						for(Double i : tempoFinal)
							media += i;
						media = media/tempoFinal.size();
					}

					System.out.println(media+"\n");
					
					matrix_tempos[(N-I)/P][c] = media;
					
					//salva os tempos parciais
					write(matrix_tempos, Q, 1+lock.size(), "../Results/Tempos.txt");
				} catch (InterruptedException e) {
					System.out.println(e);
					break;
				}
			}
		}
		
		write(matrix_tempos, Q, 1+lock.size(), "../Results/Tempos.txt");
	}
}
