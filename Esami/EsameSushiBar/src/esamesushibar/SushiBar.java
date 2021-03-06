/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esamesushibar;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author flavio
 */

// oggetto condiviso tra i thread clienti
public class SushiBar {
    
    // attributi di sincronizzazione
    // semaforo contatore che gestisce il numero di sedie disponibili
    private Semaphore ingresso;
    
    /* sistema per sospendere i thread una volta entrati nel sushibar
       in attesa di essere esattamente in 5 */
    // utilizziamo una variabile condition visto che devo svegliare i clienti seduti tutti in una volta sola
    
    // variabile condition sulla quale sospendere tutti i thread entrati in attesa del quinto cliente
    private Condition postiCompleti;
    
    // lock per gestire la mutua esclusione sulla variabile contatore e dal quale ottenere 
    private ReentrantLock lock;
    
    // variabile intera per contare i clienti effettivamente seduti al bar
    private int clientiSeduti;
    
    // metodo costruttore
    public SushiBar(){
        this.lock = new ReentrantLock();
        this.postiCompleti = this.lock.newCondition();
        // mettiamo il flag di gestione risvegli di tipo FIFO
        this.ingresso = new Semaphore(5,true);
        this.clientiSeduti = 0;
    }
    
    public int getClientiSeduti (){
        return clientiSeduti;
    }
    
    // metodo entra invocato dai clienti per chiedere di sedersi al tavolo
    public void entra(Cliente c) throws InterruptedException{
        System.out.println ("Cliente "+c.getIndex()+" chiede di entrare nel sushiBar");
        
        // acquisisco una sedia se disponibile altrimenti mi blocco in attesa che queste si liberino
        this.ingresso.acquire();
        
        // se sono qui significa che ho trovato una sedia disponibile cioè sono uno dei 5 che potrà mangiare
        // controllo se sono il quinto cliente altrimenti attendo l'arrivo di altri clienti
        
        // INIZIO SEZIONE CRITICA
        this.lock.lock();
        try{
            // incremento il numero di clienti entrati
            this.clientiSeduti ++;
            if (this.clientiSeduti < 5)
                // se non siamo ancora in 5 mi metto a dormire sulla varibile condition
                this.postiCompleti.await();
            else{
                // il quinto cliente che entra non si sospende ma sveglia tutti i clienti in attesa sulla condition
                System.out.println("Cliente "+c.getIndex()+" e' il quinto cliente....\n Sveglio tutti.");
                postiCompleti.signalAll();
                // i 5 clienti che possono mangiare escono dal metodo entra().
                // dovranno simulare il tempo necessario per mangiare e poi invocheranno il metodo esci per liberare le sedie.
            }
            
        }
        finally{
            // FINE SEZIONE CRITICA
            this.lock.unlock();
        }
    } // end metodo entra
    
    // metodo esci invocato dai clienti quando avranno finito di mangiare
    public void esci(Cliente c){
        // acquisisco il lock per la mutua esclusione
        this.lock.lock();
        
        try{
            // decremento la variabile clientiSeduti
            this.clientiSeduti--;
            
            // controllo se sono l'ultimo ad uscire 
            if (this.clientiSeduti == 0){
                System.out.println("Cliente "+c.getIndex()+" e' l'ultimo ad uscire");
                
                // faccio avanzare nella seconda fase 5 dei thread che stanno dormendo sul semaforo d'ingresso
                this.ingresso.release(5);
                /* IMPLEMENTAZIONE A BARRIERA */
            }
            System.out.println("Cliente "+c.getIndex()+ " e' uscito dal bar");
        }finally{
            this.lock.unlock();
        }
    } // end metodo esci
}// end classe
