package defaultpackage;

import java.util.*;
import java.util.Map;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;


public class Dijkstra {
	static class Grafo {
		int vertices;
		double matrizadj[][];
		Map<Integer, String> aeros; //collection na memoria para armazenar os aeroportos e mapea-los a um inteiro de forma a construir a matriz de adjacencia com indices inteiros
		String sourceVertex, destinationVertex; //as siglas de origem e destino a serem informadas pelo usuario
		
		public Grafo() { //construtor da classe
			try {
				//calcula o numero de vertices do grafo
				Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/schemajava","root", "danoninho123");
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery("select count(*) from aeroportos");
				resultSet.next();
			    this.vertices= resultSet.getInt(1);
			} catch (Exception e) { 
				e.printStackTrace();
			}
			matrizadj = new double[vertices][vertices];
			aeros = new HashMap<>();
			sourceVertex = "";
			destinationVertex = "";
		}
		
		public void mapear_aero() { //essa funcao mapeia cada aeroporto num indice inteiro (de 0 a 39, num total de 40 aeroportos)
			try {
				//acesso a table do banco de dados que contem a coluna com a lista dos aeroportos, contendo a sigla deles.
				//associamos cada sigla ao inteiro i 
				Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/schemajava","root", "danoninho123");
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery("select * from aeroportos");
				int i = 0;
				while(resultSet.next()) { //iterando sobre cada linha da tabela 
					aeros.put(i, resultSet.getString("sigla"));
					i++;
				}
			} catch (Exception e) { 
				e.printStackTrace();
			}
		}
		
		public void menu() { //menu responsavel pela interface com o usuario 
			try {
				Scanner input = new Scanner(System.in);
				System.out.println("BEM VINDO AO  MENU DE AEROPORTOS INTERNACIONAIS DO BRASIL."); 
				Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/schemajava","root", "danoninho123");
				Statement statement = connection.createStatement();
				ResultSet resultSet1 = statement.executeQuery("select distinct estado from aeroportos order by estado"); //vamos selecionar apenas os estados distintos
				String estado_origem, cidade_origem;
				String estado_destino, cidade_destino;
				System.out.println("ESCOLHA O ESTADO DESEJADO PARA O AEROPORTO DE ORIGEM:"); 
				System.out.printf("%25s", "ESTADO");
				System.out.println();
				while(resultSet1.next()) { //printar os estados por ordem alfabetica
					System.out.format("%25s",
							resultSet1.getString("estado"));
		            System.out.println();
				}
				estado_origem = input.nextLine(); //ler do usuario o estado escolhido
				System.out.println("ESCOLHA A CIDADE DESEJADA PARA O AEROPORTO DE ORIGEM:"); 
				System.out.printf("%25s", "CIDADE");
				System.out.println();
				ResultSet resultSet2 = statement.executeQuery("SELECT cidade FROM `aeroportos` WHERE `estado`='"+estado_origem+"'"+ "order by cidade"); //selecionar as cidades que sao do estado escolhido
				while(resultSet2.next()) { //printar as cidades por ordem alfabetica
					System.out.format("%25s",
							resultSet2.getString("cidade"));
		            System.out.println();
				}
				cidade_origem = input.nextLine(); //ler do usuario a cidade escolhida
				ResultSet resultSet3 = statement.executeQuery("SELECT sigla FROM `aeroportos` WHERE `cidade`='"+cidade_origem+"'"+ "order by sigla"); //selecionar as siglas que sao da cidade escolhida
				while(resultSet3.next()) {
					System.out.format("%25s", //printar as siglas por ordem alfabetica
							resultSet3.getString("sigla"));
		            System.out.println();
				}
				System.out.println("INSIRA A SIGLA DO AEROPORTO DE ORIGEM:");
				sourceVertex = input.nextLine(); //ler do usuario a sigla escolhida 
				
				//mesmo procedimento porem para receber do usuario os dados do aeroporto de destino (estado, cidade, sigla)
				ResultSet resultSet4 = statement.executeQuery("select distinct estado from aeroportos order by estado");
				System.out.println("ESCOLHA O ESTADO DESEJADO PARA O AEROPORTO DE DESTINO:"); 
				System.out.printf("%25s", "ESTADO");
				System.out.println();
				while(resultSet4.next()) {
					System.out.format("%25s",
							resultSet4.getString("estado"));
		            System.out.println();
				}
				estado_destino = input.nextLine();
				System.out.println("ESCOLHA A CIDADE DESEJADA PARA O AEROPORTO DE DESTINO:"); 
				System.out.printf("%25s", "CIDADE");
				System.out.println();
				ResultSet resultSet5 = statement.executeQuery("SELECT cidade FROM `aeroportos` WHERE `estado`='"+estado_destino+"'"+ "order by cidade");
				while(resultSet5.next()) {
					System.out.format("%25s",
							resultSet5.getString("cidade"));
		            System.out.println();
				}
				cidade_destino = input.nextLine();
				ResultSet resultSet6 = statement.executeQuery("SELECT sigla FROM `aeroportos` WHERE `cidade`='"+cidade_destino+"'"+ "order by sigla");
				while(resultSet6.next()) {
					System.out.format("%25s",
							resultSet6.getString("sigla"));
		            System.out.println();
				}
				System.out.println("INSIRA A SIGLA DO AEROPORTO DE DESTINO:");
				destinationVertex = input.nextLine();
				input.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public void criar_grafo() { //funcao para calcular as distancias e atribuir os valores aos pesos da matriz de adjacencia 
			mapear_aero();
			double lat1 =0.0, lon1=0.0, lat2=0.0, lon2=0.0, peso = 0.0;
			
			try {
				Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/schemajava","root", "danoninho123");
				Statement statement = connection.createStatement();
				//nesse for, iteramos sobre cada ligacao entre dois vertices possiveis, e selecionamos a latitude e longitude de cada ponto a e b
				//identificamos as linhas que queremos pela condicao do WHERE que obriga o ResultSet a retornar apenas aqueles cuja sigla é mapeada no inteiro correspondente aos vertice_a e vertice_b
				for(int vertice_a = 0; vertice_a < vertices; vertice_a++) {
					ResultSet va = statement.executeQuery("SELECT latitude, longitude FROM `aeroportos` WHERE `sigla`='"+aeros.get(vertice_a)+"'");
					while(va.next()) {
							//System.out.println("passei aqui1");
							lat1 = va.getDouble("latitude");
							lon1 = va.getDouble("longitude");
							//System.out.println(lat1 + " " + lon1);
					}
					for(int vertice_b = 0; vertice_b < vertices; vertice_b++) {
						ResultSet vb = statement.executeQuery("SELECT latitude, longitude FROM `aeroportos` WHERE `sigla`='"+aeros.get(vertice_b)+"'");
						while(vb.next()) {
								//System.out.println("passei aqui2");
								lat2 = vb.getDouble("latitude");
								lon2 = vb.getDouble("longitude");
								//System.out.println(lat2 + " " + lon2);
						}
						
						if ((lat1 == lat2) && (lon1 == lon2)) {
							//caso seja o mesmo ponto
							peso = 0.0;
						}
						else {
							//manipulacao matematica para calcular a distancia com base nas coordenadas
							double theta = lon1 - lon2;
							double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
							dist = Math.acos(dist);
							dist = Math.toDegrees(dist);
							dist = dist * 60 * 1.1515;
							dist = dist * 1.609344;
							
							peso = dist;
						}
						
						//atribuimos a matriz o peso equivalente a distancia entre a e b. como estamos considerando um grafo conectado e nao direcionado, precisamos atribuir nas duas direcoes possiveis
						matrizadj[vertice_a][vertice_b] = peso;
						matrizadj[vertice_b][vertice_a] = peso;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		//achar o vertice com a menor distancia que nao esta incluso ainda na minimum spanning tree
		int pegar_vertice_menor_distancia(boolean [] mst, double [] key) {
			double minkey = Integer.MAX_VALUE;
			int vertice = -1;
			for(int i = 0; i < vertices; i++) {
				if(mst[i] == false && minkey > key[i]) { //caso o vertice ainda nao esteja na arvore e sua distancia for menor do que a inicial, escolhemos ele
					minkey = key[i]; //alterar o valor da distancia e do indice do vertice a medida que escolhemos o vertice
					vertice = i;
				}
			}
			return vertice; //retorna o vertice que nao esta presente na arvore ainda que possui a menor distancia 
		}
		
		//funcao para calcular a distancia entre dois vertices
		public void pegar_menor_distancia(int vertice_origem, int vertice_destino) {
			double aux = 0;
			//fazer com que haja pelo menos uma escala, excluindo temporariamente a conexao entre origem e destino
			if(matrizadj[vertice_origem][vertice_destino] > 0) {
				aux = matrizadj[vertice_origem][vertice_destino];
				matrizadj[vertice_origem][vertice_destino] =0.0;
				matrizadj[vertice_destino][vertice_origem] =0.0;
			}
			
			boolean [] spt = new boolean[vertices];
			double[] distancia = new double[vertices];
			int INF = Integer.MAX_VALUE;
			int[] par = new int[vertices]; //array para armazenar os pais de cada vertice a medida que atualizamos o dijkstra, vamos utiliza-lo para conseguirmos recuperar o caminho depois 
			par[vertice_origem] = vertice_origem; //devemos setar o pai da origem para ele mesmo, pois essa sera nossa condicao de parada
			//inicializar todas as distancias pro infinito
			for(int i = 0; i < vertices; i++) {
				distancia[i] = INF;
			}
			distancia[vertice_origem] = 0.0;
			//criar a arvore com os menores caminhos (spt)
			for(int i = 0; i < vertices; i++) {
				//pegar o vertice com a menor distancia que ainda nao esta na arvore
				int vertice_a = pegar_vertice_menor_distancia(spt, distancia);
				//incluir esse vertice na spt
				spt[vertice_a] = true;
				//iterar sobre os vertices adjacentes a ele
				for(int vertice_b = 0; vertice_b < vertices; vertice_b++) {
					if(matrizadj[vertice_a][vertice_b] > 0.0) { //existe conexao naquele caminho 
						//verificar se o vertice adjacente ja esta na spt e se a distancia nao eh inf
						if(spt[vertice_b] == false && matrizadj[vertice_a][vertice_b] != INF) {
							//verificar se a distancia precisa ser atualizada ou nao (se compensa trocar caso seja menor)
							double newkey = matrizadj[vertice_a][vertice_b] + distancia[vertice_a];
							if(newkey < distancia[vertice_b]) { //caso isso valha, quer dizer que encontramos um melhor caminho e vamos segui-lo
								par[vertice_b] = vertice_a; //como vamos seguir esse caminho, agora o vertice_a é o pai do vertice_b, logo devemos atualiza-lo
								distancia[vertice_b] = newkey;	//atualizar a mudanca
							}
						}
					}
				}
			}
			//no fim desse for, o vetor distancia vai estar com as distancias do vertice origem ate cada vertice do grafo
			//recriar a aresta que foi excluida para obrigar a existir uma escala
			matrizadj[vertice_origem][vertice_destino] = aux;
			matrizadj[vertice_destino][vertice_origem] = aux;
			//printar o resultado do dijkstra
			printDijkstra(vertice_origem,vertice_destino, distancia, par);
			
			
		}
		public void printDijkstra(int vertice_origem, int vertice_destino, double [] key, int [] par) {
			System.out.println("Pelo algoritmo de Dijkstra, a distancia é: " + key[vertice_destino] + " km");
			System.out.println("O caminho é: ");
			ArrayList<Integer> caminho = new ArrayList<Integer>();
			caminho = recuperar_caminho(vertice_destino, par);
			String path = "";
			//printar as siglas dos aeroportos do caminho
			for(int i = caminho.size() - 1; i >=0; i--) {
				System.out.print(aeros.get(caminho.get(i)) + " ");
				path = path + aeros.get(caminho.get(i)) + " ";
			}
			System.out.println();
			//inserir na table no banco de dados
			try {
				Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/schemajava","root", "danoninho123");
				Statement statement = connection.createStatement();
				String accessDatabase = "insert into distancias(origem, destino, distancia, caminho)" + " values('" + sourceVertex + "', '" + destinationVertex + "', '" + key[vertice_destino] + "', '" + path + "')";
				int result = statement.executeUpdate(accessDatabase);
		        if (result > 0) {
		            System.out.println("Dado Inserido com sucesso.");
		        }
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println();
			
		}
		//funcao para recuperar o caminho percorrido com base no array de pais existente 
		public ArrayList<Integer> recuperar_caminho(int u, int [] par) {
			ArrayList<Integer> caminho = new ArrayList<Integer>();
			caminho.add(u);
			while(par[u] !=u) { //enquanto o pai de u for diferente dele mesmo, eu subo na arvore atualizando o pai. quando for igual, quer dizer que cheguei na origem.
				u = par[u];
				caminho.add(u);
			}
			return caminho;
		}
		
	}
	
	public static void main(String[] args) {
		int sourceVertex = 0;
		int destinationVertex= 0;
		Grafo graph = new Grafo();
		graph.criar_grafo();
		graph.menu();
		//temos a string de origem e de destino desejadas, precisamos procurar qual inteiro equivale a cada uma
		for (Map.Entry<Integer, String> entry : graph.aeros.entrySet()) {
			if (Objects.equals(graph.sourceVertex, entry.getValue())) {
				sourceVertex = entry.getKey();
		    }
		}
		for (Map.Entry<Integer, String> entry : graph.aeros.entrySet()) {
			if (Objects.equals(graph.destinationVertex, entry.getValue())) {
				destinationVertex = entry.getKey();
		    }
		}
		graph.pegar_menor_distancia(sourceVertex, destinationVertex);
	}
}
