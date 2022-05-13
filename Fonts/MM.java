import java.io.*;

public class MM {
	static int dim = 1024;
	
	public static void mm(int dim, int n) {
		if(dim != 0){
			int matA[] = new int [dim*dim];
			int matB[] = new int [dim*dim]; //invertida para cache
			int matC[] = new int [dim*dim];
			int aux;
			
			for(int i = 0; i < dim; i ++){
				for(int j = 0; j < dim; j ++){
					matA[i*dim+j] = i + j;
					matB[i*dim+j] = i + j + 1;
				}
			}
			
			for(int i = 0; i < dim; i ++){
				for(int j = 0; j < dim; j ++){
					aux = 0;
					for(int k = 0; k < dim; k ++)
						aux += matA[i*dim+k] * matB[j*dim+k];
					matC[i*dim+j] = aux;
				}
			}
			
			//for(int i = 0; i < dim; i ++){
			//	for(int j = 0; j < dim; j ++){
			//		System.out.print(matC[i*dim+j]+" ");
			//	}
			//	System.out.println();
			//}
		}
		n ++;
	}
}
