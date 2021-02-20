package uniandes.lym.robot.control;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;

import javax.swing.SwingUtilities;

import uniandes.lym.robot.kernel.*;



/**
 * Receives commands and relays them to the Robot. 
 */

public class Interpreter   {
	
	enum estado{
		define,
		walkDropFreePickGrabDefineValue,
		rotate,
		walkTo,
		look,
		NOP,
		block,
		IF,
		not,
		blocked,
		facing,
		can,
		none,
		esperandoVariable,
		walkTo2,
		defineParams,
		defineValue,
		defineFunction
	}
	
	enum estadoBloque{
		defineBloque,
		blockBloque,
		ifBloque,
		noneBloque		
	}
	
	/**
	 * Robot's world
	 */
	private RobotWorldDec world;
	
	private HashSet<String> hSet = new HashSet<String>();
	
	private HashSet<String> hSetVariables = new HashSet<String>();
	
	private estado estadoActual = estado.none;
	
	private estadoBloque estadobloque = estadoBloque.noneBloque;
	
	
	public Interpreter()
	  {
		hSet.add("define");
		hSet.add("walk");
		hSet.add("rotate");
		hSet.add("look");
		hSet.add("drop");
		hSet.add("free");
		hSet.add("pick");
		hSet.add("grab");
		hSet.add("walkTo");
		hSet.add("NOP");
		hSet.add("block");
		hSet.add("if");
		hSet.add("not");
		hSet.add("blocked?");
		hSet.add("facing?");
		hSet.add("can");
	  }
	
	


    /**
	 * Creates a new interpreter for a given world
	 * @param world 
	 */


	public Interpreter(RobotWorld mundo)
      {
		this.world =  (RobotWorldDec) mundo;
		
	  }
	
	
	/**
	 * sets a the world
	 * @param world 
	 */

	public void setWorld(RobotWorld m) 
	{
		world = (RobotWorldDec) m;
		
	}
	  
	
	
	/**
	 *  Processes a sequence of commands. A command is a letter  followed by a ";"
	 *  The command can be:
	 *  M:  moves forward
	 *  R:  turns right
	 *  
	 * @param input Contiene una cadena de texto enviada para ser interpretada
	 */
	
	public String process(String input) throws Error
     {   
		estado estadoActual = estado.none;
		
		 
		StringBuffer output=new StringBuffer("SYSTEM RESPONSE: -->\n");	
		
		int i;
	   int n;
	   boolean ok = true;
	   n= input.length();
	    
	    i  = 0;
	    try	    {
	    while (i < n &&  ok) {
	    	switch (input.charAt(i)) {
	    		case 'M' : world.moveForward(1); output.append("move \n");break;
	    		case 'R': world.turnRight(); output.append("turnRignt \n");break;
	    		case 'C': world.putChips(1); output.append("putChip \n");break;
	    		case 'B': world.putBalloons(1); output.append("putBalloon \n");break;
	    		case  'c': world.pickChips(1); output.append("getChip \n");break;
	    		case  'b': world.grabBalloons(1); output.append("getBalloon \n");break;
	    		default: output.append(" Unrecognized command:  "+ input.charAt(i)); ok=false;
	          }
	
	    	  if (ok) {
	    		 if  (i+1 == n)  { output.append("expected ';' ; found end of input; ");  ok = false ;}
	    		 else if (input.charAt(i+1) == ';') 
	    		 {
	    			 i= i+2;
	    			 try {
	    			        Thread.sleep(1000);
	    			    } catch (InterruptedException e) {
	    			        System.err.format("IOException: %s%n", e);
	    			    }
	    			 
	    		 }
	    		 else {output.append(" Expecting ;  found: "+ input.charAt(i+1)); ok=false;
	    	 }
	    	 }
	    
	    
	    }
	    
	    }
	 catch (Error e ){
	 output.append("Error!!!  "+e.getMessage());	 
 }
	    return output.toString();
	    }
	
	
	public void funcionaElCodigo(BufferedReader br, File file)
	{					
		try (FileReader fr = new FileReader(file))  {
			// variable que nos ayuda a verificar que todos los parentesis estén bien
			int parentesis=0; 
			// cada renglon del txt
			String line;	
			// palabra que estemos leyendo actualmente
			String palabraActual = "";	
			//variable que cuenta los parentesis de un block para saber cuando ya esté cerrado
			int parentesisBlock=1;
			//variable que nos ayuda a verificar cuantas condiciones vienen luego de una F de función
			int largoFuncion=0; 
			// variable que mide los paretesis de una condicion en un if
			int ifNot=0;
					
			//empezamos a leer por lineas el txt
			while((line = br.readLine()) != null){
				
				//con esto sabemos lo largo de cada renglon en un String
				int largo = line.length();
			    //Empezamos a recorrer el renglon por caracteres para crear las palabras
			    for (int i =0; i < largo; i++) {			    	
			    	if(line.charAt(i)== '(') parentesis++;
			    	if(line.charAt(i)== ')') parentesis--;
			    	//si hay parentesis mal úbicados lanzamos una respuesta de error
			    	if(parentesis < 0) System.out.println ("La cantidad de parentesis abiertos y cerrados no es la misma");
			    	//si luego de leer un define leemos la plabra y sigue de unos parentesis tenemos que leer los parametros
					if(estadoActual.equals(estadoBloque.defineBloque) &&  line.charAt(i)!= '(' ) estadoActual= estado.defineParams;
			    	
					if(estadobloque.equals(estadoBloque.blockBloque) && line.charAt(i)== ')') parentesisBlock--;
					if(estadobloque.equals(estadoBloque.blockBloque) && line.charAt(i)== '(') parentesisBlock++;
					//esto nos ayuda a salir del bloque 
					if(parentesisBlock == 0) estadobloque= estadoBloque.noneBloque;
					
					
					//Acá consolidamos las palabras
			    	if (line.charAt(i)!= ')' && line.charAt(i)!= '(' && line.charAt(i)!= ' ' && line.charAt(i)!= '\n') {			    		
			    		palabraActual += line.charAt(i);			    				    		
			    	}
			    	
			    	//Con esto sabemos la cantidad de parametros que habrían en una función
			    	if(estadoActual.equals(estado.defineFunction)) largoFuncion = palabraActual.length()-1;
			    	//Si ya añadimos los parametreos al set de variables cambiamos el estado a none
			    	if(estadoActual.equals(estado.defineFunction) && largoFuncion ==0) estadoActual = estado.none;
			    	
			    	//aca empezamos a definir los estados en los que nos encontremos para saber qué necesitamos recibir luego
			    	else {
			    		//definimos el estado en el que nos econtramos dependiendo la palabra que acabemos de leer.
			    		if(hSet.contains(palabraActual)) {
			    			cambioDeEstado(palabraActual);		    			
			    			
			    			System.out.println("La palabra funciona!");
			    			System.out.println("la palabra actual es: " + palabraActual);			    			
			    		}
			    		//si la palabra no se encuentra en el set del abecedario partimos a leer cual es la palabra siguiente. 
			    		else {
			    			//si estamos en estado define, tenemos que mirar cuál es la palabra que le sigue. 
			    			if(estadoActual.equals(estado.define)) {
			    				
			    				estadobloque = estadoBloque.defineBloque;
			    				// si la palabra que le sigue a un define empieza por F significa que sifue una función.
			    				if(palabraActual.charAt(0)=='f' ) {
			    					estadoActual= estado.defineFunction;
			    				}
			    				
			    				//si lo que le sigue a una función no es una f, se añadirá al set de variables.
			    				hSetVariables.add(palabraActual);
			    				//verificamos que el value exista como una variable previamente definida o que sea un int al ponerla en este estado.
			    				estadoActual = estado.walkDropFreePickGrabDefineValue;		    				
			    			}
			    			
			    			else if(estadoActual.equals(estado.block)) {
			    				//creamos este bloque para saber cuando acabe el bloque pues, puede cambiar de estado dependiendo de las palabras dentro de él
			    				estadobloque = estadoBloque.blockBloque;
			    				//verificamos que la palabra que le siga al bloque esté dentro de nuestro set de variables o en el abecedario.
			    				if(hSet.contains(palabraActual) || hSetVariables.contains(palabraActual)){
			    					funcionaElEstadoActual(palabraActual);				    					
			    				}
			    				// si acabamos el bloque volvemos a un estado nulo
			    				if(parentesisBlock<1){
			    					estadoActual=estado.none;
			    					estadobloque = estadoBloque.noneBloque;
			    				}
			    			}
			    			//si estamos esperando una función necesitamos meter las variables que vienen luego de conocer la cantidad de parametros de la función.
			    			else if (estadoActual.equals(estado.defineFunction)) {
			    				hSetVariables.add(palabraActual);
			    				largoFuncion--;
			    			}
			    			
			    			else if(estadoActual.equals(estado.IF)) {
			    				//Entramos a un tipo de estado de bloque, para ver cuándo se cierra el if 
			    				estadobloque= estadoBloque.blockBloque;
			    				//ya que estamos en la palabra que le sigue a blocked podemos devolver el estado a nulo
			    				if(palabraActual.equals("blocked?") ) estadoActual = estado.none;
			    				//Tenemos ahora que verificar que luego de facing recibamos una cardinalidad
			    				if(palabraActual.equals("facing?") ) {
			    					estadoActual = estado.facing;
			    				}
			    				//Tenemos que verificar que luego de un can tengamos un "grap, drop, free o pick"
			    				if(palabraActual.equals("can")) {
			    					estadoActual = estado.can;
			    				}
			    				//verificamos que luego del not podamos meter una de las condiciones anteriores.
			    				if(palabraActual.equals("not") && ifNot <=0) {
			    					estadoActual = estado.IF;
			    					//Con este int sabemos que salimos del not
			    					ifNot--;
			    				}
			    				else {
			    					System.out.println("La palabra " + palabraActual + " no es valida");
			    				}
			    				
			    			}
			    			// si estamos en facing tenemos que mirar que lo que siga cumpla  que sea una cardinalidad
			    			else if(estadoActual.equals(estado.facing))	{
			    				estadoActual = estado.look;
			    				
			    			}
			    			//si estamos en can tenemos que verificar que sea uno de los comandos que podemos meter.
			    			else if(estadoActual.equals(estado.can))	{
			    				if(palabraActual.equals("grab") || palabraActual.equals("free") || palabraActual.equals("pick") || palabraActual.equals("drop")) estadoActual= estado.none;
			    				else {
			    					System.out.println("La palabra " + palabraActual + " no es valida");
			    				}
			    			}
			    			//verificamos las palabras del abecedario.
			    			else {
			    				funcionaElEstadoActual(palabraActual);
			    			}
			    			//reiniciamos la palabra actual para seguir leyendo
			    			palabraActual = "";
			    		}		    			
		    		}
			    }
			}
			
			//verificamos que la variable al final del todo nos indique 0 para saber que los parentesis estaban bien :D
			System.out.println(parentesis);		
			
			
		} 
		
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	//definimos los estados con los que arrancamos.
	public void cambioDeEstado(String pPalabraActual) {
		
		if(pPalabraActual.equals("define")) {
			estadoActual = estado.define;
		}
		else if(pPalabraActual.equals("rotate")) {
			estadoActual = estado.rotate;
		}
		else if(pPalabraActual.equals("if")) {
			estadoActual = estado.IF;
		}
		else if(pPalabraActual.equals("walk") || pPalabraActual.equals("drop") || pPalabraActual.equals("free") ||  pPalabraActual.equals("pick") || pPalabraActual.equals("grab")) {
			estadoActual = estado.walkDropFreePickGrabDefineValue;
		}
		else if(pPalabraActual.equals("look")) {
			estadoActual = estado.look;
		}		
		else if(pPalabraActual.equals("walkTo")) {
			estadoActual = estado.walkTo;
		}
		else if(pPalabraActual.equals("NOP")) {
			estadoActual = estado.NOP;
		}
		else if(pPalabraActual.equals("block")) {
			estadoActual = estado.block;
		}
		else if(pPalabraActual.equals("not")) {
			estadoActual = estado.not;
		}
		else if(pPalabraActual.equals("blocked")) {
			estadoActual = estado.blocked;
		}
		else if(pPalabraActual.equals("facing")) {
			estadoActual = estado.facing;
		}
		else if(pPalabraActual.equals("can")) {
			estadoActual = estado.can;
		}				
	}
	
	//hacemos las verificaciones sobre cada estado apra saber que están bien
	public void funcionaElEstadoActual(String palabraActual) {
		
		if(estadoActual.equals(estado.walkDropFreePickGrabDefineValue)) {
			if(hSetVariables.contains(palabraActual)) {
				estadoActual= estado.none;
			}			    				
			else if(Character.isDigit(palabraActual.charAt(0))){
				estadoActual= estado.none;
			}
			else {
				System.out.println("La palabra " + palabraActual + " no es valida");
			}
		}
		else if(estadoActual.equals(estado.rotate)) {
			if (palabraActual.equals("back") || palabraActual.equals("left") || palabraActual.equals("right") ) {
				estadoActual= estado.none;
			}
			else {
				System.out.println("La palabra " + palabraActual + " no es valida");
			}
		}
		else if(estadoActual.equals(estado.look)) {
			if (palabraActual.equals("N") || palabraActual.equals("E") || palabraActual.equals("W") || palabraActual.equals("S") ) {
				estadoActual= estado.none;
			}
			else {
				System.out.println("La palabra " + palabraActual + " no es valida");
			}
		}
		else if(estadoActual.equals(estado.walkTo)) {
			if(Character.isDigit(palabraActual.charAt(0))){
				estadoActual= estado.walkTo2;
			}
			else {
				System.out.println("La palabra " + palabraActual + " no es valida");
			}
		}
		else if(estadoActual.equals(estado.walkTo2)) {
			if (palabraActual.equals("N") || palabraActual.equals("E") || palabraActual.equals("W") || palabraActual.equals("S") ) {
				estadoActual= estado.none;
			}
			else {
				System.out.println("La palabra " + palabraActual + " no es valida");
			}
		}
		else if(estadoActual.equals(estado.NOP)) {
			estadoActual= estado.none;
		}
		
	}


	
}
	    