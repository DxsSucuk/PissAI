package de.presti.webpanel;

import de.presti.webpanel.sql.SQLConnector;
import de.presti.webpanel.utils.GoogleImageSearcher;
import lombok.Getter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.logging.Logger;

@SpringBootApplication
public class WebpanelApplication {

    @Getter
    public static WebpanelApplication instance;

    @Getter
    private Logger logger;

    @Getter
    private SQLConnector sqlConnector;

    public static void main(String[] args) {
        SpringApplication.run(WebpanelApplication.class, args);
        instance = new WebpanelApplication();
        instance.logger = Logger.getGlobal();
        instance.sqlConnector = new SQLConnector("CnXeW7O58m", "CnXeW7O58m", "WFkPCOfouW", "remotemysql.com", 3306);
        new GoogleImageSearcher();
    }

}
