package com.cineca.app.frontend;

// Importazioni vaarie per spring boot e swing
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import javax.swing.SwingUtilities;

//legge application.properties e application-secret.properties e connessione db
@SpringBootApplication

//senza sta riga Spring cercherebbe solo su com.cineca.app.frontend, noi vogliamo che cerchi in tutto il progetto le classi annotate  --> @Component, @Service, @Controller
@ComponentScan(basePackages = "com.cineca.app")

//cerca le interfacce repository e fa da solo le query sql 
@EnableJpaRepositories(basePackages = "com.cineca.app.backend.repository")

//cerca le classi modelli annotate con @Entity, "queste classi corrispondono alle tabelle del db"
@EntityScan(basePackages = "com.cineca.app.backend.modelli")
public class Main {

    public static void main(String[] args) {
        
        // scatola con tutti gli oggetti , usiamo il builder perchè dopo dobbiamo condifurare un parametro speciale ovveero headless false,
        //ovvero che la applicazne ha una interfaccia grafica
        //di default spring boot imposta headless true per le applicazioni web
        ConfigurableApplicationContext context = new SpringApplicationBuilder(Main.class)
                .headless(false)
                //avvia tutto, legge application.properties, crea gli oggetti annotati con @Component, @Service, @Repository, @Controller ecc
                .run(args);

        //Aspettiamo che la GUI venga creata nel thread corretto di Swing
        SwingUtilities.invokeLater(() -> {
            try {
                //invece che fare una finestra scollegata, carichiamo la GUI nella finestra principale gestita da Spring
                MainFrame frame = context.getBean(MainFrame.class);
                //mettiamo la finestra effettivamente visibile
                frame.setVisible(true);
            } catch (Exception e) {
                System.err.println("ERRORE GUI: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}